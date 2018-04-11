/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.bre;

import org.ballerinalang.bre.bvm.WorkerData;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.exceptions.ArgumentOutOfRangeException;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.debugger.DebugContext;
import org.ballerinalang.util.exceptions.BLangNullReferenceException;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangVMUtils;
import org.ballerinalang.util.transactions.LocalTransactionInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents a {@link Context} implementation for native calls.
 */
public class NativeCallContext implements Context {

    private WorkerExecutionContext parentCtx;
    
    private CallableUnitInfo callableUnitInfo;

    private WorkerData workerLocal;

    private BValue[] returnValues;

    public Map<String, Object> localProps;

    public NativeCallContext(WorkerExecutionContext parentCtx, CallableUnitInfo callableUnitInfo,
            WorkerData workerLocal) {
        this.parentCtx = parentCtx;
        this.callableUnitInfo = callableUnitInfo;
        this.workerLocal = workerLocal;
    }

    @Override
    public WorkerExecutionContext getParentWorkerExecutionContext() {
        return parentCtx;
    }
    
    @Override
    public CallableUnitInfo getCallableUnitInfo() {
        return callableUnitInfo;
    }

    public WorkerData getLocalWorkerData() {
        return workerLocal;
    }

    @Override
    public DebugContext getDebugContext() {
        // TODO
        return null;
    }

    @Override
    public void setDebugContext(DebugContext debugContext) {
        // TODO

    }

    @Override
    public Object getProperty(String key) {
        return this.parentCtx.globalProps.get(key);
    }

    @Override
    public Map<String, Object> getProperties() {
        return this.parentCtx.globalProps;
    }

    @Override
    public void setProperty(String key, Object value) {
        this.parentCtx.globalProps.put(key, value);
    }

    @Override
    public ServiceInfo getServiceInfo() {
        return BLangVMUtils.getServiceInfo(this.parentCtx);
    }

    @Override
    public void setServiceInfo(ServiceInfo serviceInfo) {
        BLangVMUtils.setServiceInfo(this.parentCtx, serviceInfo);
    }

    @Override
    public boolean isInTransaction() {
        return this.parentCtx.isInTransaction();
    }

    @Override
    public BStruct getError() {
        return this.parentCtx.getError();
    }

    @Override
    public void setError(BStruct error) {
        this.parentCtx.setError(error);
    }

    @Override
    public ProgramFile getProgramFile() {
        return this.parentCtx.programFile;
    }

    @Override
    public long getIntArgument(int index) {
        if (index < 0) {
            throw new ArgumentOutOfRangeException(index);
        }

        return workerLocal.longRegs[index];
    }

    @Override
    public String getStringArgument(int index) {
        if (index < 0) {
            throw new ArgumentOutOfRangeException(index);
        }

        String str = workerLocal.stringRegs[index];
        if (str == null) {
            throw new BLangNullReferenceException();
        }

        return str;
    }

    @Override
    public String getNullableStringArgument(int index) {
        if (index < 0) {
            throw new ArgumentOutOfRangeException(index);
        }

        return workerLocal.stringRegs[index];
    }

    @Override
    public double getFloatArgument(int index) {
        if (index < 0) {
            throw new ArgumentOutOfRangeException(index);
        }

        return workerLocal.doubleRegs[index];
    }

    @Override
    public boolean getBooleanArgument(int index) {
        if (index < 0) {
            throw new ArgumentOutOfRangeException(index);
        }

        return (workerLocal.intRegs[index] == 1);
    }

    @Override
    public byte[] getBlobArgument(int index) {
        if (index < 0) {
            throw new ArgumentOutOfRangeException(index);
        }

        byte[] result = workerLocal.byteRegs[index];
        if (result == null) {
            throw new BallerinaException("argument " + index + " is null");
        }

        return result;
    }

    @Override
    public BValue getRefArgument(int index) {
        if (index < 0) {
            throw new ArgumentOutOfRangeException(index);
        }

        BValue result = workerLocal.refRegs[index];
        if (result == null) {
            throw new BallerinaException("argument " + index + " is null");
        }

        return result;
    }

    @Override
    public BValue getNullableRefArgument(int index) {
        if (index < 0) {
            throw new ArgumentOutOfRangeException(index);
        }

        return workerLocal.refRegs[index];
    }

    @Override
    public BValue[] getReturnValues() {
        return this.returnValues;
    }

    @Override
    public void setReturnValues(BValue... values) {
        this.returnValues = values;
    }

    public LocalTransactionInfo getLocalTransactionInfo() {
        return this.parentCtx.getLocalTransactionInfo();
    }

    @Override
    public void setLocalProperty(String key, Object val) {
        if (localProps == null) {
            localProps = new HashMap<>();
        }
        localProps.put(key, val);
    }

    @Override
    public Object getLocalProperty(String key) {
        if (localProps != null) {
            return localProps.get(key);
        }
        return null;
    }

}
