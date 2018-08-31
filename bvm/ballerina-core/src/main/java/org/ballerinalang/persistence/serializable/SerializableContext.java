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

import org.ballerinalang.bre.bvm.CallableWorkerResponseContext;
import org.ballerinalang.bre.bvm.WorkerData;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.bre.bvm.WorkerState;
import org.ballerinalang.model.util.serializer.JsonSerializer;
import org.ballerinalang.persistence.Deserializer;
import org.ballerinalang.persistence.Serializer;
import org.ballerinalang.runtime.Constants;
import org.ballerinalang.util.codegen.CallableUnitInfo;
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

    String ctxKey;

    public String parent;

    public String respContextKey;

    public WorkerState state = WorkerState.CREATED;

    public int ip;

    public int[] retRegIndexes;

    public boolean runInCaller;

    public boolean interruptible;

    public String enclosingServiceName;

    public String callableUnitName;

    public String callableUnitPkgPath;

    public String workerName;

    public Type type = Type.WORKER;

    public HashMap<String, Object> globalProps = new HashMap<>();

    private HashMap<String, Object> localProps = new HashMap<>();

    public SerializableWorkerData workerLocal;

    public SerializableWorkerData workerResult;

    public HashSet<String> children = new HashSet<>();


    public static SerializableContext deserialize(String jsonString) {
        JsonSerializer serializer = Serializer.getJsonSerializer();
        return serializer.deserialize(jsonString, SerializableContext.class);
    }

    private SerializableContext(String ctxKey, WorkerExecutionContext ctx, int ip, SerializableState state,
                                boolean updateIfExist) {
        this.ctxKey = ctxKey;
        this.interruptible = ctx.interruptible;
        if (ctx.workerInfo != null) {
            workerName = ctx.workerInfo.getWorkerName();
            if (workerName.equals(Constants.DEFAULT)) {
                type = Type.DEFAULT;
            }
        } else {
            type = Type.PARENT;
        }
        populateData(ctx, ip, state, updateIfExist);
    }

    SerializableContext(String ctxKey, WorkerExecutionContext ctx, int ip, SerializableState state,
                        SerializableContext sParentCtx, boolean updateIfExist) {
        this(ctxKey, ctx, ip, state, updateIfExist);
        this.parent = sParentCtx.ctxKey;
        sParentCtx.children.add(this.ctxKey);
    }

    SerializableContext(String ctxKey, WorkerExecutionContext ctx, SerializableState state, int ip,
                        boolean isCompletedCtxRemoved, boolean updateIfExist) {
        this(ctxKey, ctx, ip, state, updateIfExist);
        populateData(ctx, ip, state, updateIfExist);
        SerializableContext sParentCtx = populateParentContexts(ctx, state, isCompletedCtxRemoved, updateIfExist);
        if (sParentCtx != null) {
            this.parent = sParentCtx.ctxKey;
            sParentCtx.children.add(this.ctxKey);
        }
    }

    void populateData(WorkerExecutionContext ctx, SerializableState state, int ip,
                      boolean isCompletedCtxRemoved) {
        populateData(ctx, ip, state, true);
        populateParentContexts(ctx, state, isCompletedCtxRemoved, true);
    }

    private void populateData(WorkerExecutionContext ctx, int ip, SerializableState state, boolean updateIfExist) {
        this.ip = ip;
        populateProps(state.globalProps, ctx.globalProps, state);
        populateProps(localProps, ctx.localProps, state);
        retRegIndexes = ctx.retRegIndexes;
        runInCaller = ctx.runInCaller;
        if (ctx.callableUnitInfo != null) {
            if (ctx.callableUnitInfo instanceof ResourceInfo) {
                enclosingServiceName = ((ResourceInfo) ctx.callableUnitInfo).getServiceInfo().getName();
            }
            callableUnitName = ctx.callableUnitInfo.getName();
            callableUnitPkgPath = ctx.callableUnitInfo.getPkgPath();
        }
        if (ctx.respCtx != null) {
            if (ctx.respCtx instanceof CallableWorkerResponseContext) {
                CallableWorkerResponseContext callableRespCtx = (CallableWorkerResponseContext) ctx.respCtx;
                respContextKey = state.addRespContext(callableRespCtx, updateIfExist).respCtxKey;
            }
        }
        if (ctx.workerLocal != null) {
            workerLocal = new SerializableWorkerData(ctx.workerLocal, state);
        }
        if (ctx.workerResult != null) {
            workerResult = new SerializableWorkerData(ctx.workerResult, state);
        }
    }

    private SerializableContext populateParentContexts(WorkerExecutionContext ctx, SerializableState state,
                                                       boolean isCompletedCtxRemoved, boolean updateIfExist) {
        if (ctx.parent != null) {
            return state.populateContext(ctx.parent, ctx.parent.ip, isCompletedCtxRemoved, updateIfExist, ctxKey);
        }
        return null;
    }

    WorkerExecutionContext getWorkerExecutionContext(ProgramFile programFile, SerializableState state,
                                                     Deserializer deserializer) {
        WorkerExecutionContext workerExecutionContext = deserializer.getContexts().get(ctxKey);
        if (workerExecutionContext != null) {
            return workerExecutionContext;
        }
        CallableUnitInfo callableUnitInfo = null;
        WorkerInfo workerInfo = null;
        WorkerData workerLocalData = null;
        WorkerData workerResultData = null;

        Map<String, Object> tempGlobalProps = prepareProps(state.globalProps, state, programFile, deserializer);
        if (workerLocal != null) {
            workerLocalData = workerLocal.getWorkerData(programFile, state, deserializer);
        }
        if (workerResult != null) {
            workerResultData = workerResult.getWorkerData(programFile, state, deserializer);
        }
        if (callableUnitPkgPath != null) {
            PackageInfo packageInfo = programFile.getPackageInfo(callableUnitPkgPath);
            if (enclosingServiceName != null) {
                ServiceInfo serviceInfo = packageInfo.getServiceInfo(enclosingServiceName);
                tempGlobalProps.put(SERVICE_INFO_KEY, serviceInfo);
                callableUnitInfo = serviceInfo.getResourceInfo(callableUnitName);
                if (callableUnitInfo != null) {
                    if (Constants.DEFAULT.equals(workerName)) {
                        workerInfo = callableUnitInfo.getDefaultWorkerInfo();
                    } else {
                        workerInfo = callableUnitInfo.getWorkerInfo(workerName);
                    }
                }
            } else {
                callableUnitInfo = packageInfo.getFunctionInfo(callableUnitName);
                if (callableUnitInfo != null) {
                    if (Constants.DEFAULT.equals(workerName)) {
                        workerInfo = callableUnitInfo.getDefaultWorkerInfo();
                    } else {
                        workerInfo = callableUnitInfo.getWorkerInfo(workerName);
                    }
                }
            }
        }
        if (parent == null || callableUnitInfo == null) {
            // this is the root context
            workerExecutionContext = new WorkerExecutionContext(programFile);
            workerExecutionContext.workerLocal = workerLocalData;
            workerExecutionContext.workerResult = workerResultData;
        } else {
            WorkerExecutionContext parentCtx = state.getExecutionContext(parent, programFile, deserializer);
            CallableWorkerResponseContext respCtx = state.getResponseContext(respContextKey, programFile,
                                                                             callableUnitInfo, deserializer);
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
