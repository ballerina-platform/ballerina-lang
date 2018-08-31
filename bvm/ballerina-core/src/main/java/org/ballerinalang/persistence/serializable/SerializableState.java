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

import org.ballerinalang.bre.bvm.CallableWorkerResponseContext;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.model.util.serializer.JsonSerializer;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.persistence.Deserializer;
import org.ballerinalang.persistence.Serializer;
import org.ballerinalang.persistence.serializable.reftypes.Serializable;
import org.ballerinalang.persistence.serializable.reftypes.SerializableRefType;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.ProgramFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class represents a serializable state. This holds the required functionality to persist the context.
 *
 * @since 0.981.1
 */
public class SerializableState {

    private String id;

    private Set<String> sCurrentCtxKeys = new HashSet<>();

    private Map<String, SerializableContext> sContexts = new HashMap<>();

    private Map<String, SerializableRespContext> sRespContexts = new HashMap<>();

    private Map<String, SerializableRefType> sRefTypes = new HashMap<>();

    public HashMap<String, Object> globalProps = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public static SerializableState deserialize(String json) {
        JsonSerializer jsonSerializer = Serializer.getJsonSerializer();
        return jsonSerializer.deserialize(json, SerializableState.class);

    }
    public SerializableState(String id, List<WorkerExecutionContext> ctxList) {
        this.id = id;
        ctxList.forEach(ctx -> populateContext(ctx, ctx.ip, true, false, null));
    }

    public SerializableState(String id, WorkerExecutionContext executionContext) {
        this.id = id;
        populateContext(executionContext, executionContext.ip, true, false, null);
    }

    public synchronized String checkPoint(WorkerExecutionContext context, int ip) {
        populateContext(context, ip, false, true, null);
        cleanResponseContexts();
        return serialize();
    }

    public synchronized void registerContexts(WorkerExecutionContext parentCtx, List<WorkerExecutionContext> ctxList) {
        // Since all context list have one parent, we can update the parent recursively at once.
        SerializableContext sParentCtx = populateContext(parentCtx, parentCtx.ip, false, true, null);
        sCurrentCtxKeys.remove(sParentCtx.ctxKey);
        ctxList.forEach(ctx -> {
            SerializableContext sCtx = new SerializableContext(String.valueOf(ctx.hashCode()), ctx,
                                                               ctx.ip + 1, this, sParentCtx, true);
            sContexts.put(sCtx.ctxKey, sCtx);
            sCurrentCtxKeys.add(sCtx.ctxKey);
        });
        cleanResponseContexts();
    }

    public String serialize() {
        return Serializer.getJsonSerializer().serialize(this);
    }

    public synchronized List<WorkerExecutionContext> getExecutionContexts(ProgramFile programFile,
                                                                          Deserializer deserializer) {
        return sCurrentCtxKeys
                .stream()
                .map(sCtxKey -> sContexts.get(sCtxKey).getWorkerExecutionContext(programFile, this, deserializer))
                .collect(Collectors.toList());
    }

    SerializableContext populateContext(WorkerExecutionContext ctx, int ip, boolean isCompletedCtxRemoved,
                                        boolean updateIfExist, String childCtxKey) {
        String ctxKey = String.valueOf(ctx.hashCode());
        SerializableContext sCtx = sContexts.get(ctxKey);
        if (sCtx == null) {
            sCtx = new SerializableContext(ctxKey, ctx, this, ip, isCompletedCtxRemoved, updateIfExist);
            sContexts.put(sCtx.ctxKey, sCtx);
            sCurrentCtxKeys.add(sCtx.ctxKey);
            if (sCtx.parent != null) {
                sCurrentCtxKeys.remove(sCtx.parent);
            }
        } else if (updateIfExist) {
            if (childCtxKey != null && !isCompletedCtxRemoved) {
                Optional<String> sDefaultContext
                        = sCtx.children.stream()
                                       .filter(childCtx -> !childCtx.equals(childCtxKey) &&
                                               sContexts.get(childCtx).type.equals(SerializableContext.Type.DEFAULT))
                                       .findAny();
                if (isCompletedCtxRemoved = sDefaultContext.isPresent()) {
                    String sDefaultCtxKey = sDefaultContext.get();
                    removeChildContext(sCtx, sContexts.get(sDefaultCtxKey));
                }
            }
            sCtx.populateData(ctx, this, ip, isCompletedCtxRemoved);
        }
        return sCtx;
    }

    SerializableRespContext addRespContext(CallableWorkerResponseContext responseContext,
                                           boolean updateTargetCtxIfExist) {
        String respCtxKey = String.valueOf(responseContext.hashCode());
        SerializableRespContext sRespCtx = sRespContexts.get(respCtxKey);
        if (sRespCtx == null) {
            sRespCtx = new SerializableRespContext(respCtxKey, responseContext, this, updateTargetCtxIfExist);

            sRespContexts.put(sRespCtx.respCtxKey, sRespCtx);
        }
        return sRespCtx;
    }

    WorkerExecutionContext getExecutionContext(String contextKey, ProgramFile programFile,
                                               Deserializer deserializer) {

        SerializableContext serializableContext = sContexts.get(contextKey);
        return serializableContext.getWorkerExecutionContext(programFile, this, deserializer);
    }

    CallableWorkerResponseContext getResponseContext(String respCtxKey, ProgramFile programFile,
                                                     CallableUnitInfo callableUnitInfo,
                                                     Deserializer deserializer) {
        CallableWorkerResponseContext responseContext = deserializer.getRespContexts().get(respCtxKey);
        if (responseContext != null) {
            return responseContext;
        }
        SerializableRespContext sRespContext = sRespContexts.get(respCtxKey);
        responseContext = sRespContext.getResponseContext(programFile, callableUnitInfo,
                                                          this, deserializer);
        deserializer.getRespContexts().put(respCtxKey, responseContext);
        return responseContext;
    }

    ArrayList<Object> serializeRefFields(BRefType[] bRefFields) {
        if (bRefFields == null) {
            return null;
        }
        ArrayList<Object> refFields = new ArrayList<>(bRefFields.length);
        for (int i = 0; i < bRefFields.length; i++) {
            BRefType refType = bRefFields[i];
            refFields.add(i, serialize(refType));
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

    public Object serialize(Object o) {
        if (o == null || Serializer.isSerializable(o)) {
            return o;
        } else {
            if (o instanceof Serializable) {
                return addRefType((Serializable) o);
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
                return bRefType;
            }
        } else {
            return o;
        }
    }

    public SerializableContext getContext(String ctxKey) {
        return sContexts.get(ctxKey);
    }

    private SerializedKey addRefType(Serializable serializable) {
        String refKey = String.valueOf(serializable.hashCode());
        if (sRefTypes.containsKey(refKey)) {
            return new SerializedKey(refKey);
        }
        SerializableRefType sRefType = serializable.serialize(this);
        if (sRefType != null) {
            sRefTypes.put(refKey, sRefType);
            return new SerializedKey(refKey);
        }
        return null;
    }

    private void cleanResponseContexts() {
        sRespContexts.entrySet().removeIf(e -> !sContexts.containsKey(e.getValue().targetContextKey));
    }

    private void removeChildContext(SerializableContext parentCtx, SerializableContext childCtx) {
        cleanContextData(childCtx.ctxKey, childCtx);
        parentCtx.children.remove(childCtx.ctxKey);
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
}
