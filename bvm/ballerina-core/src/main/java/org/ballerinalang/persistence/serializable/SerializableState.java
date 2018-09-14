/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.persistence.serializable;

import org.ballerinalang.bre.bvm.AsyncInvocableWorkerResponseContext;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.bre.bvm.WorkerResponseContext;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.persistence.Deserializer;
import org.ballerinalang.persistence.Serializer;
import org.ballerinalang.persistence.serializable.reftypes.Serializable;
import org.ballerinalang.persistence.serializable.reftypes.SerializableRefType;
import org.ballerinalang.persistence.serializable.responses.SerializableResponseContext;
import org.ballerinalang.persistence.serializable.responses.impl.SerializableAsyncResponse;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.ProgramFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.ballerinalang.persistence.Serializer.REMOVABLE_TYPES_INITIATION;

/**
 * This class represents a serializable state. This holds the required functionality to persist the context.
 *
 * @since 0.981.1
 */
public class SerializableState {

    private String id;

    // Set of worker execution context hashcodes which resides as leaf nodes of the context hierarchy
    private Set<String> sCurrentCtxKeys = new HashSet<>();

    // Map of worker execution context hashcode vs serializable context
    private Map<String, SerializableContext> sContexts = new HashMap<>();

    // Map of response context hashcode vs serializable response context
    private Map<String, SerializableResponseContext> sRespContexts = new HashMap<>();

    // Map of BRefType object hashcode vs serializable BRefType
    private Map<String, SerializableRefType> sRefTypes = new HashMap<>();

    // Map of global properties used in context hierarchy
    public HashMap<String, Object> globalProps = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SerializableState(String id, List<WorkerExecutionContext> ctxList) {
        this.id = id;
        HashSet<String> updatedObjectSet = new HashSet<>();
        ctxList.forEach(ctx -> populateContext(ctx, ctx.ip, true, false, updatedObjectSet, null));
    }

    public SerializableState(String id, WorkerExecutionContext executionContext) {
        this.id = id;
        HashSet<String> updatedObjectSet = new HashSet<>();
        populateContext(executionContext, executionContext.ip, true, false, updatedObjectSet, null);
    }

    /**
     * Create a checkpoint on runtime state.
     *
     * @param ctx Worker Execution context to be updated
     * @param ip  Instruction point
     * @return Updated serialized state as a string
     */
    public synchronized String checkPoint(WorkerExecutionContext ctx, int ip) {
        HashSet<String> updatedObjectSet = new HashSet<>();
        populateContext(ctx, ip, false, true, updatedObjectSet, null);
        cleanCompletedContexts();
        return serialize();
    }

    /**
     * Register new worker execution contexts in the serialization state.
     *
     * @param parentCtx Parent worker execution context
     * @param ctxList   List of worker execution contexts to be updated
     */
    public synchronized void registerContexts(WorkerExecutionContext parentCtx, List<WorkerExecutionContext> ctxList) {
        // Since all ctx list have one parent, we can update the parent recursively at once.
        HashSet<String> updatedObjectSet = new HashSet<>();
        SerializableContext sParentCtx = populateContext(parentCtx, parentCtx.ip, false, true, updatedObjectSet, null);
        ctxList.forEach(ctx -> {
            SerializableContext sCtx = new SerializableContext(String.valueOf(ctx.hashCode()), ctx, this,
                                                               ctx.ip + 1, true, false, updatedObjectSet);
            sContexts.put(sCtx.ctxKey, sCtx);
            sCurrentCtxKeys.add(sCtx.ctxKey);
        });
        if (!ctxList.isEmpty() && ctxList.get(0).respCtx instanceof AsyncInvocableWorkerResponseContext) {
            sParentCtx.ip++;
        } else {
            sCurrentCtxKeys.remove(sParentCtx.ctxKey);
        }
        cleanCompletedContexts();
    }

    /**
     * Add or update serializable worker execution context data propagating through context hierarchy.
     *
     * @param ctx                   Worker Execution context to be updated
     * @param ip                    Instruction point
     * @param isCompletedCtxRemoved Flag to determine whether we have remove the completed context. This will be used
     *                              as a performance improvement since if it is true we do not need to check child
     *                              contexts
     * @param updateParent          Flag to determine whether need to update parent
     * @param updatedObjectSet      Set contains already updated context and refTypes during propagating through context
     *                              hierarchy
     * @param childCtx              Child context which needs to populate the parent context
     * @return Serializable context
     */
    SerializableContext populateContext(WorkerExecutionContext ctx, int ip, Boolean isCompletedCtxRemoved,
                                        boolean updateParent, HashSet<String> updatedObjectSet,
                                        SerializableContext childCtx) {
        String ctxKey = String.valueOf(ctx.hashCode());
        final SerializableContext exCtx = sContexts.get(ctxKey);
        SerializableContext updatedCtx = exCtx;
        if (updatedObjectSet != null && !updatedObjectSet.contains(ctxKey)) {
            updatedObjectSet.add(ctxKey);
            // We need to remove the already completed worker execution contexts from the serializable state.
            // ex. Here we have two functions as f2 will be called after f1.
            // f1();
            // f2()
            // Here when we update the f2 context hierarchy , contexts and data related to f1 required be cleaned.
            // This only be required to do at once for given update process of context hierarchy.
            if (!isCompletedCtxRemoved) {
                isCompletedCtxRemoved = removeCompletedContexts(exCtx, childCtx);
            }
            updatedCtx = new SerializableContext(ctxKey, ctx, this, ip, isCompletedCtxRemoved,
                                                 updateParent, updatedObjectSet);
            sContexts.put(ctxKey, updatedCtx);
            if (exCtx != null) {
                updatedCtx.children.addAll(exCtx.children);
            }
            sCurrentCtxKeys.add(ctxKey);
            if (updatedCtx.parent != null && !isAsync(ctx)) {
                sCurrentCtxKeys.remove(updatedCtx.parent);
            }
        }
        return updatedCtx;
    }

    /**
     * Provides worker execution contexts which will be reschedule to recover the state.
     *
     * @param programFile  Program file
     * @param deserializer Deserializer
     * @return List of worker execution contexts
     */
    public synchronized List<WorkerExecutionContext> getExecutionContexts(ProgramFile programFile,
                                                                          Deserializer deserializer) {
        return sCurrentCtxKeys
                .stream()
                .map(sCtxKey -> sContexts.get(sCtxKey).getWorkerExecutionContext(programFile, this, deserializer))
                .collect(Collectors.toList());
    }

    public SerializableResponseContext addRespContext(WorkerResponseContext responseContext,
                                                      HashSet<String> updatedObjectSet) {
        String respCtxKey = String.valueOf(responseContext.hashCode());
        SerializableResponseContext sRespCtx = sRespContexts.get(respCtxKey);
        if (!updatedObjectSet.contains(respCtxKey)) {
            updatedObjectSet.add(respCtxKey);
            sRespCtx = Serializer.sRspCtxFactory.getResponseContext(respCtxKey, responseContext);
            sRespContexts.put(sRespCtx.getRespCtxKey(), sRespCtx);
            sRespCtx.addTargetContexts(responseContext, this);
        }
        return sRespCtx;
    }

    public WorkerExecutionContext getExecutionContext(String contextKey, ProgramFile programFile,
                                                      Deserializer deserializer) {
        SerializableContext serializableContext = sContexts.get(contextKey);
        return serializableContext.getWorkerExecutionContext(programFile, this, deserializer);
    }

    public WorkerResponseContext getResponseContext(String respCtxKey, ProgramFile programFile,
                                                    CallableUnitInfo callableUnitInfo,
                                                    Deserializer deserializer) {
        WorkerResponseContext responseContext = deserializer.getRespContexts().get(respCtxKey);
        if (responseContext != null) {
            return responseContext;
        }
        SerializableResponseContext sRespContext = sRespContexts.get(respCtxKey);
        responseContext = sRespContext.getResponseContext(programFile, callableUnitInfo, this, deserializer);
        deserializer.getRespContexts().put(respCtxKey, responseContext);
        sRespContext.joinTargetContextInfo(responseContext, programFile, this, deserializer);
        return responseContext;
    }

    public String serialize() {
        return Serializer.getJsonSerializer().serialize(this);
    }

    ArrayList<Object> serializeRefFields(BRefType[] bRefFields, HashSet<String> updatedObjectSet) {
        if (bRefFields == null) {
            return null;
        }
        ArrayList<Object> refFields = new ArrayList<>(bRefFields.length);
        for (int i = 0; i < bRefFields.length; i++) {
            BRefType refType = bRefFields[i];
            refFields.add(i, serialize(refType, updatedObjectSet));
        }
        return refFields;
    }

    BRefType[] deserializeRefFields(List<Object> sRefFields, ProgramFile programFile, Deserializer deserializer) {
        if (sRefFields == null) {
            return null;
        }
        BRefType[] bRefFields = new BRefType[sRefFields.size()];
        for (int i = 0; i < sRefFields.size(); i++) {
            Object s = sRefFields.get(i);
            bRefFields[i] = (BRefType) deserialize(s, programFile, deserializer);
        }
        return bRefFields;
    }

    public Object serialize(Object o, HashSet<String> updatedObjectSet) {
        if (o == null || Serializer.isSerializable(o)) {
            return o;
        } else {
            if (o instanceof Serializable) {
                return addRefType((Serializable) o, updatedObjectSet);
            } else {
                return null;
            }
        }
    }

    public Object deserialize(Object o, ProgramFile programFile, Deserializer deserializer) {
        if (o instanceof SerializedKey) {
            SerializedKey key = (SerializedKey) o;
            BRefType bRefType = deserializer.getRefTypes().get(key.key);
            if (bRefType != null) {
                return bRefType;
            } else {
                SerializableRefType sRefType = sRefTypes.get(key.key);
                bRefType = sRefType.getBRefType(programFile, this, deserializer);
                deserializer.getRefTypes().put(key.key, bRefType);
                sRefType.setContexts(bRefType, programFile, this, deserializer);
                return bRefType;
            }
        } else {
            return o;
        }
    }

    public SerializableContext getSerializableContext(String ctxKey) {
        return sContexts.get(ctxKey);
    }

    private SerializedKey addRefType(Serializable serializable, HashSet<String> updatedObjectSet) {
        String refKey = String.valueOf(serializable.hashCode());
        if (!updatedObjectSet.contains(refKey)) {
            updatedObjectSet.add(refKey);
            SerializableRefType sRefType = serializable.serialize(this, updatedObjectSet);
            if (sRefType != null) {
                sRefTypes.put(refKey, sRefType);
                return new SerializedKey(refKey);
            }
            return null;
        } else {
            return new SerializedKey(refKey);
        }
    }

    private void cleanCompletedContexts() {
        sRespContexts.entrySet().removeIf(respCtx -> !(respCtx.getValue() instanceof SerializableAsyncResponse) &&
                !sContexts.containsKey(respCtx.getValue().getTargetCtxKey()));
        sContexts.entrySet().removeIf(sCtx -> sCtx.getValue().respCtxKey != null &&
                sRespContexts.get(sCtx.getValue().respCtxKey) == null);
    }

    private boolean removeCompletedContexts(SerializableContext exCtx, SerializableContext childCtx) {
        if (exCtx != null) {
            if (childCtx != null) {
                exCtx.children.remove(childCtx.ctxKey);
            }
            if (childCtx == null || !childCtx.type.equals(SerializableContext.Type.WORKER)) {
                return exCtx.children.removeIf(child -> {
                    boolean isRemovable = REMOVABLE_TYPES_INITIATION.contains(
                            sContexts.get(child).type);
                    if (isRemovable) {
                        SerializableContext context = sContexts.get(child);
                        cleanContextData(context.ctxKey, context);
                    }
                    return isRemovable;
                });
            }
        }
        return false;
    }

    private void cleanContextData(String ctxKey, SerializableContext ctx) {
        ctx.children.forEach(childCtxKey -> {
            SerializableContext childCtx = sContexts.get(childCtxKey);
            cleanContextData(childCtxKey, childCtx);
        });
        ctx.children.clear();
        removeRefTypes(ctx);
        sContexts.remove(ctxKey);
        sCurrentCtxKeys.remove(ctxKey);
    }

    private void removeRefTypes(SerializableContext ctx) {
        if (ctx.workerLocal != null && ctx.workerLocal.refFields != null) {
            ctx.workerLocal.refFields.stream()
                                     .filter(o -> o instanceof SerializedKey)
                                     .forEach(o -> sRefTypes.remove(((SerializedKey) o).key));
        }
        if (ctx.workerResult != null && ctx.workerResult.refFields != null) {
            ctx.workerResult.refFields.stream()
                                      .filter(o -> o instanceof SerializedKey)
                                      .forEach(o -> sRefTypes.remove(((SerializedKey) o).key));
        }
    }

    public boolean isAsync(WorkerExecutionContext ctx) {
        return ctx.respCtx instanceof AsyncInvocableWorkerResponseContext;
    }
}
