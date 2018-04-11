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

import org.ballerinalang.bre.bvm.WorkerState;

import java.util.HashMap;
import java.util.Map;

public class SerializableExecutionContext {
    //    todo: RefTypes in WorkerData and BStruct error support should be added
    public SerializableExecutionContext parent;

    public WorkerState state = WorkerState.CREATED;

    //    public Map<String, Object> globalProps;

    public Map<String, Object> localProps = new HashMap<>();

    public int ip;

    //    public ProgramFile programFile;

    //    public ConstantPoolEntry[] constPool; //written only from callableUnitInfo

    //    public Instruction[] code; //written only from callableUnitInfo

    public SerializableWorkerData workerLocal;

    public SerializableWorkerData workerResult;

    public int[] retRegIndexes;

    //    public CallableUnitInfo callableUnitInfo;

    //    public WorkerInfo workerInfo;

    //    public WorkerResponseContext respCtx;

    public boolean runInCaller;

    //    private BStruct error;

    //    private DebugContext debugContext;

    public SerializableExecutionContext getParent() {
        return parent;
    }

    public void setParent(SerializableExecutionContext parent) {
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