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
package org.ballerinalang.bre.bvm.persistency;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.ballerinalang.bre.bvm.WorkerData;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.bre.bvm.WorkerState;
import org.ballerinalang.bre.bvm.persistency.reftypes.LocalPropKey;
import org.ballerinalang.persistence.StateStore;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ResourceInfo;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.codegen.WorkerInfo;

import java.util.HashMap;
import java.util.Map;

public class SerializableContext {
    // TODO: BStruct error support should be added
    public String parent;

    public WorkerState state = WorkerState.CREATED;

    public Map<String, Object> globalProps = new HashMap<>();

    public Map<String, Object> localProps = new HashMap<>();

    public int ip;

    public SerializableWorkerData workerLocal;

    public SerializableWorkerData workerResult;

    public int[] retRegIndexes;

    public boolean runInCaller;

    public String enclosingServiceName;

    public String callableUnitName;

    public String callableUnitPkgPath;

    public String workerName;

    public static SerializableContext deserialize(String jsonString) {
        Gson gson = new GsonBuilder().create();
        SerializableContext serializableContext = gson.fromJson(jsonString, SerializableContext.class);
        return serializableContext;
    }

    private void populateGlobalProps(Map<String, Object> props) {
        if (props == null) {
            return;
        }
        for (String key : props.keySet()) {
            Object v = props.get(key);
            if (PersistenceUtils.isSerializable(v)) {
                globalProps.put(key, v);
            }
        }
    }

    private void populateLocalProps(Map<String, Object> props) {
        if (props == null) {
            return;
        }
        for (String key : props.keySet()) {
            Object v = props.get(key);
            if (PersistenceUtils.isSerializable(v)) {
                localProps.put(key, v);
            } else {
                LocalPropKey localPropKey = new LocalPropKey(key, v.getClass().getName());
                localProps.put(key, localPropKey);
            }
        }
    }

    private Map<String, Object> prepareLocalProps(
            SerializableState state, WorkerExecutionContext context) {
        Map<String, Object> props = new HashMap<>();
        if (localProps != null) {
            for (String key : localProps.keySet()) {
                Object v = localProps.get(key);
                if (v instanceof LocalPropKey) {
                    StateStore.getDataMapper().
                            mapLocalProp(state.getSerializationId(), (LocalPropKey) v, context);
                } else {
                    props.put(key, v);
                }
            }
        }
        return props;
    }

    public SerializableContext(WorkerExecutionContext ctx, SerializableState state) {
        ip = ctx.ip;
        populateGlobalProps(ctx.globalProps);
        populateLocalProps(ctx.localProps);
        retRegIndexes = ctx.retRegIndexes;
        runInCaller = ctx.runInCaller;

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
        if (ctx.workerLocal != null) {
            workerLocal = new SerializableWorkerData(ctx.workerLocal, state);
        }
        if (ctx.workerResult != null) {
            workerResult = new SerializableWorkerData(ctx.workerResult, state);
        }
        if (ctx.parent != null) {
            parent = state.addContext(ctx.parent);
        }
    }

    public WorkerExecutionContext getWorkerExecutionContext(ProgramFile programFile, SerializableState state) {

        CallableUnitInfo callableUnitInfo = null;
        WorkerInfo workerInfo = null;
        WorkerData workerLocalData = null;
        WorkerData workerResultData = null;

        if (workerLocal != null) {
            workerLocalData = workerLocal.getWorkerData(programFile, state);
        }
        if (workerResult != null) {
            workerResultData = workerResult.getWorkerData(programFile, state);
        }

        if (callableUnitPkgPath != null) {
            PackageInfo packageInfo = programFile.getPackageInfo(callableUnitPkgPath);
            if (enclosingServiceName != null) {
                ServiceInfo serviceInfo = packageInfo.getServiceInfo(enclosingServiceName);
                callableUnitInfo = serviceInfo.getResourceInfo(callableUnitName);
                if ("default".equals(workerName)) {
                    workerInfo = callableUnitInfo.getDefaultWorkerInfo();
                } else {
                    workerInfo = callableUnitInfo.getWorkerInfo(workerName);
                }
            } else {
                callableUnitInfo = packageInfo.getFunctionInfo(callableUnitName);
                if ("default".equals(workerName)) {
                    workerInfo = callableUnitInfo.getDefaultWorkerInfo();
                } else {
                    workerInfo = callableUnitInfo.getWorkerInfo(workerName);
                }
            }
        }

        WorkerExecutionContext workerExecutionContext = null;
        if (parent == null) {
            // this is the root context
            workerExecutionContext = new WorkerExecutionContext(programFile);
            workerExecutionContext.workerLocal = workerLocalData;
            workerExecutionContext.workerResult = workerResultData;
        } else {
            WorkerExecutionContext parentCtx = state.getContext(parent, programFile);
            workerExecutionContext = new WorkerExecutionContext(
                    parentCtx, null, callableUnitInfo, workerInfo, workerLocalData, workerResultData, retRegIndexes, runInCaller);
        }
        workerExecutionContext.globalProps = globalProps;
        workerExecutionContext.localProps = prepareLocalProps(state, workerExecutionContext);
        return workerExecutionContext;
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

    public void setState(WorkerState state) {
        this.state = state;
    }

    public Map<String, Object> getLocalProps() {
        return localProps;
    }

    public void setLocalProps(Map<String, Object> localProps) {
        this.localProps = localProps;
    }

    public int getIp() {
        return ip;
    }

    public void setIp(int ip) {
        this.ip = ip;
    }

    public SerializableWorkerData getWorkerLocal() {
        return workerLocal;
    }

    public void setWorkerLocal(SerializableWorkerData workerLocal) {
        this.workerLocal = workerLocal;
    }

    public SerializableWorkerData getWorkerResult() {
        return workerResult;
    }

    public void setWorkerResult(SerializableWorkerData workerResult) {
        this.workerResult = workerResult;
    }

    public int[] getRetRegIndexes() {
        return retRegIndexes;
    }

    public void setRetRegIndexes(int[] retRegIndexes) {
        this.retRegIndexes = retRegIndexes;
    }

    public boolean isRunInCaller() {
        return runInCaller;
    }

    public void setRunInCaller(boolean runInCaller) {
        this.runInCaller = runInCaller;
    }
}