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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.ballerinalang.bre.bvm.CallableWorkerResponseContext;
import org.ballerinalang.bre.bvm.WorkerData;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.bre.bvm.WorkerState;
import org.ballerinalang.persistence.Deserializer;
import org.ballerinalang.runtime.Constants;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ResourceInfo;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.codegen.WorkerInfo;

import java.util.HashMap;
import java.util.Map;

import static org.ballerinalang.util.program.BLangVMUtils.SERVICE_INFO_KEY;

/**
 * This class represents a serializable Ballerina execution context.
 *
 * @since 0.976.0
 */
public class SerializableContext {

    private String contextKey;

    public String parent;

    private String respContextKey;

    public WorkerState state = WorkerState.CREATED;

    private HashMap<String, Object> localProps = new HashMap<>();

    public int ip;

    public SerializableWorkerData workerLocal;

    private SerializableWorkerData workerResult;

    public int[] retRegIndexes;

    private boolean runInCaller;

    public boolean interruptible;

    private String enclosingServiceName;

    private String callableUnitName;

    private String callableUnitPkgPath;

    public String workerName;

    public static SerializableContext deserialize(String jsonString) {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(jsonString, SerializableContext.class);
    }

    public SerializableContext(String contextKey, WorkerExecutionContext ctx, SerializableState state, int ip) {
        this.contextKey = contextKey;
        this.ip = ip;
        populateProps(state.globalProps, ctx.globalProps, state);
        populateProps(localProps, ctx.localProps, state);
        retRegIndexes = ctx.retRegIndexes;
        runInCaller = ctx.runInCaller;
        interruptible = ctx.interruptible;
        if (ctx.callableUnitInfo != null) {
            if (ctx.callableUnitInfo instanceof ResourceInfo) {
                enclosingServiceName = ((ResourceInfo) ctx.callableUnitInfo).getServiceInfo().getName();
            }
            callableUnitName = ctx.callableUnitInfo.getName();
            callableUnitPkgPath = ctx.callableUnitInfo.getPkgPath();
        }
        if (ctx.workerInfo != null) {
            workerName = ctx.workerInfo.getWorkerName();
        }
        if (ctx.respCtx != null) {
            if (ctx.respCtx instanceof CallableWorkerResponseContext) {
                CallableWorkerResponseContext callableRespCtx =
                        (CallableWorkerResponseContext) ctx.respCtx;
                respContextKey = state.addRespContext(callableRespCtx);
            }
        }
        if (ctx.workerLocal != null) {
            workerLocal = new SerializableWorkerData(ctx.workerLocal, state);
        }
        if (ctx.workerResult != null) {
            workerResult = new SerializableWorkerData(ctx.workerResult, state);
        }
        if (ctx.parent != null) {
            parent = state.addContext(ctx.parent, ctx.parent.ip);
        }
    }

    public WorkerExecutionContext getWorkerExecutionContext(ProgramFile programFile, SerializableState state,
                                                            Deserializer deserializer) {

        if (deserializer.getTempContexts().containsKey(contextKey)) {
            return deserializer.getTempContexts().get(contextKey);
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

        WorkerExecutionContext workerExecutionContext;
        if (parent == null) {
            // this is the root context
            workerExecutionContext = new WorkerExecutionContext(programFile);
            workerExecutionContext.workerLocal = workerLocalData;
            workerExecutionContext.workerResult = workerResultData;
        } else {
            WorkerExecutionContext parentCtx = state.getContext(parent, programFile, deserializer);
            CallableWorkerResponseContext respCtx =
                    state.getResponseContext(respContextKey, programFile, callableUnitInfo, deserializer);
            workerExecutionContext = new WorkerExecutionContext(
                    parentCtx, respCtx, callableUnitInfo, workerInfo, workerLocalData, workerResultData, retRegIndexes,
                    runInCaller);
        }
        workerExecutionContext.globalProps = tempGlobalProps;
        workerExecutionContext.localProps = prepareProps(localProps, state, programFile, deserializer);
        workerExecutionContext.ip = ip;
        workerExecutionContext.interruptible = interruptible;
        deserializer.getTempContexts().put(contextKey, workerExecutionContext);
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

    public String serialize() {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(this);
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public WorkerState getState() {
        return state;
    }
}
