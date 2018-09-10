/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.persistence.serializable;

import org.ballerinalang.bre.bvm.AsyncInvocableWorkerResponseContext;
import org.ballerinalang.bre.bvm.ForkJoinWorkerResponseContext;
import org.ballerinalang.bre.bvm.WorkerData;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.bre.bvm.WorkerResponseContext;
import org.ballerinalang.bre.bvm.WorkerState;
import org.ballerinalang.model.util.serializer.JsonSerializer;
import org.ballerinalang.persistence.Deserializer;
import org.ballerinalang.persistence.Serializer;
import org.ballerinalang.runtime.Constants;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.Instruction;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ResourceInfo;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.codegen.WorkerInfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.ballerinalang.util.program.BLangVMUtils.SERVICE_INFO_KEY;

/**
 * This class represents a serializable Ballerina execution context.
 *
 * @since 0.981.1
 */
public class SerializableContext {

    public String ctxKey;

    public String parent;

    public WorkerState state = WorkerState.CREATED;

    public String respCtxKey;

    public int ip;

    public int[] retRegIndexes;

    public boolean runInCaller;

    public boolean interruptible;

    public String enclosingServiceName;

    public String callableUnitName;

    public String callableUnitPkgPath;

    public String workerName;

    public Type type = Type.PARENT;

    public HashMap<String, Object> globalProps = new HashMap<>();

    private HashMap<String, Object> localProps = new HashMap<>();

    public SerializableWorkerData workerLocal;

    public SerializableWorkerData workerResult;

    public HashSet<String> children = new HashSet<>();

    public SerializableContext(String ctxKey, WorkerExecutionContext ctx, SerializableState state, int ip,
                        boolean isCompletedCtxRemoved, boolean updateParent, boolean updateIfExist) {
        this.ctxKey = ctxKey;
        this.interruptible = ctx.interruptible;
        if (ctx.workerInfo != null) {
            workerName = ctx.workerInfo.getWorkerName();
            if (ctx.respCtx instanceof AsyncInvocableWorkerResponseContext) {
                type = Type.ASYNC;
            } else if (workerName.equals(Constants.DEFAULT)) {
                type = Type.DEFAULT;
            } else {
                type = Type.WORKER;
            }
        }
        populateData(ctx, ip, state, isCompletedCtxRemoved, updateParent, updateIfExist);
    }

    void populateData(WorkerExecutionContext ctx, int ip, SerializableState state,
                      boolean isCompletedCtxRemoved, boolean updateParent, boolean updateIfExist) {
        populateParentContexts(ctx, state, isCompletedCtxRemoved, updateParent, updateIfExist);
        populateData(ctx, ip, state);
    }

    private void populateData(WorkerExecutionContext ctx, int ip, SerializableState state) {
        this.ip = ip;
        populateProps(state.globalProps, ctx.globalProps, state);
        populateProps(localProps, ctx.localProps, state);
        retRegIndexes = ctx.retRegIndexes;
        runInCaller = ctx.runInCaller;
        if (ctx.callableUnitInfo != null) {
            if (ctx.callableUnitInfo instanceof ResourceInfo) {
                enclosingServiceName = ((ResourceInfo) ctx.callableUnitInfo).getServiceInfo().getName();
            }
            if (ctx.callableUnitInfo.attachedToType != null) {
                callableUnitName = ctx.callableUnitInfo.attachedToType.getName() + "." + ctx.callableUnitInfo.getName();
            } else {
                callableUnitName = ctx.callableUnitInfo.getName();
            }
            callableUnitPkgPath = ctx.callableUnitInfo.getPkgPath();
        }
        if (ctx.respCtx != null) {
            respCtxKey = state.addRespContext(ctx.respCtx).getRespCtxKey();
        }
        if (ctx.workerLocal != null) {
            workerLocal = new SerializableWorkerData(ctx.workerLocal, state);
        }
        if (ctx.workerResult != null) {
            workerResult = new SerializableWorkerData(ctx.workerResult, state);
        }
    }

    private void populateParentContexts(WorkerExecutionContext ctx, SerializableState state,
                                        boolean isCompletedCtxRemoved, boolean updateParent, boolean updateIfExist) {
        if (ctx.parent != null) {
            SerializableContext sParentCtx = state.getSerializableContext(String.valueOf(ctx.parent.hashCode()));
            if (sParentCtx == null || (updateParent && !type.equals(Type.ASYNC))) {
                sParentCtx = state.populateContext(ctx.parent, ctx.parent.ip, isCompletedCtxRemoved, updateParent,
                                                   updateIfExist, ctxKey);
            }
            this.parent = sParentCtx.ctxKey;
            sParentCtx.children.add(this.ctxKey);
        }
    }

    public static SerializableContext deserialize(String jsonString) {
        JsonSerializer serializer = Serializer.getJsonSerializer();
        return serializer.deserialize(jsonString, SerializableContext.class);
    }

    WorkerExecutionContext getWorkerExecutionContext(ProgramFile programFile, SerializableState state,
                                                     Deserializer deserializer) {
        WorkerExecutionContext workerExecutionContext = deserializer.getContexts().get(ctxKey);
        if (workerExecutionContext != null) {
            return workerExecutionContext;
        }
        CallableUnitInfo callableUnitInfo = null;
        WorkerData workerLocalData = null;
        WorkerData workerResultData = null;
        PackageInfo packageInfo = null;
        Map<String, Object> tempGlobalProps = prepareProps(state.globalProps, state, programFile, deserializer);
        if (callableUnitPkgPath != null) {
            packageInfo = programFile.getPackageInfo(callableUnitPkgPath);
            if (enclosingServiceName != null) {
                ServiceInfo serviceInfo = packageInfo.getServiceInfo(enclosingServiceName);
                tempGlobalProps.put(SERVICE_INFO_KEY, serviceInfo);
                callableUnitInfo = serviceInfo.getResourceInfo(callableUnitName);
            } else {
                callableUnitInfo = packageInfo.getFunctionInfo(callableUnitName);
            }
        }
        if (workerLocal != null) {
            workerLocalData = workerLocal.getWorkerData(programFile, state, deserializer);
        }
        if (workerResult != null) {
            workerResultData = workerResult.getWorkerData(programFile, state, deserializer);
        }
        if (parent == null || callableUnitInfo == null) {
            // this is the root context
            workerExecutionContext = new WorkerExecutionContext(programFile);
            workerExecutionContext.workerLocal = workerLocalData;
            workerExecutionContext.workerResult = workerResultData;
        } else {
            WorkerExecutionContext parentCtx = state.getExecutionContext(parent, programFile, deserializer);
            WorkerResponseContext respCtx = null;
            if (respCtxKey != null) {
                respCtx = state.getResponseContext(this.respCtxKey, programFile, callableUnitInfo, deserializer);

            }
            WorkerInfo workerInfo = getWorkerInfo(respCtx, parentCtx, packageInfo, callableUnitInfo);
            workerExecutionContext = new WorkerExecutionContext(parentCtx, respCtx, callableUnitInfo,
                                                                workerInfo, workerLocalData, workerResultData,
                                                                retRegIndexes, runInCaller);
        }
        workerExecutionContext.globalProps = tempGlobalProps;
        workerExecutionContext.localProps = prepareProps(localProps, state, programFile, deserializer);
        workerExecutionContext.ip = ip;
        workerExecutionContext.interruptible = interruptible;
        deserializer.getContexts().put(ctxKey, workerExecutionContext);
        return workerExecutionContext;
    }

    private Map<String, Object> prepareProps(HashMap<String, Object> properties, SerializableState state,
                                             ProgramFile programFile, Deserializer deserializer) {
        Map<String, Object> props = new HashMap<>();
        if (properties != null) {
            properties.forEach((s, o) -> {
                Object deserialize = state.deserialize(o, programFile, deserializer);
                props.put(s, deserialize);
            });
        }
        return props;
    }

    private void populateProps(HashMap<String, Object> properties, Map<String, Object> props,
                               SerializableState state) {
        if (props != null) {
            props.forEach((s, o) -> properties.put(s, state.serialize(o)));
        }
    }

    private WorkerInfo getWorkerInfo(WorkerResponseContext respCtx, WorkerExecutionContext parentCtx,
                                     PackageInfo packageInfo, CallableUnitInfo callableUnitInfo) {
        if (respCtx instanceof ForkJoinWorkerResponseContext) {
            return ((Instruction.InstructionFORKJOIN) packageInfo.getInstructions()[parentCtx.ip - 1])
                    .forkJoinCPEntry.getForkjoinInfo().getWorkerInfo(workerName);
        } else if (Constants.DEFAULT.equals(workerName)) {
            return callableUnitInfo.getDefaultWorkerInfo();
        } else {
            return callableUnitInfo.getWorkerInfo(workerName);
        }
    }

    /**
     * Execution Type of the @{@link SerializableContext}.
     */
    public enum Type {
        DEFAULT,
        ASYNC,
        WORKER,
        PARENT
    }
}
