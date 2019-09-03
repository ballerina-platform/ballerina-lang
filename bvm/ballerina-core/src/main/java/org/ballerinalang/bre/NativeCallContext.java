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

import org.ballerinalang.bre.bvm.StackFrame;
import org.ballerinalang.bre.bvm.Strand;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.exceptions.BLangNullReferenceException;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangVMUtils;
import org.ballerinalang.util.transactions.TransactionLocalContext;

import java.util.Map;

/**
 * This class represents a {@link Context} implementation for native calls.
 */
public class NativeCallContext implements Context {

    private Strand strand;
    
    private CallableUnitInfo callableUnitInfo;

    private StackFrame sf;

    private BValue returnValue;

    public NativeCallContext(Strand strand, CallableUnitInfo callableUnitInfo,
                             StackFrame sf) {
        this.strand = strand;
        this.callableUnitInfo = callableUnitInfo;
        this.sf = sf;
    }

    @Override
    public Strand getStrand() {
        return strand;
    }

    @Override
    public CallableUnitInfo getCallableUnitInfo() {
        return callableUnitInfo;
    }

    @Override
    public StackFrame getDataFrame() {
        return sf;
    }

    @Override
    public Object getProperty(String key) {
        return this.strand.globalProps.get(key);
    }

    @Override
    public Map<String, Object> getProperties() {
        return this.strand.globalProps;
    }

    @Override
    public void setProperty(String key, Object value) {
        this.strand.globalProps.put(key, value);
    }

    @Override
    public ServiceInfo getServiceInfo() {
        return BLangVMUtils.getServiceInfo(this.strand);
    }

    @Override
    public void setServiceInfo(ServiceInfo serviceInfo) {
        BLangVMUtils.setServiceInfo(this.strand, serviceInfo);
    }

    @Override
    public boolean isInTransaction() {
        return this.strand.isInTransaction();
    }

    @Override
    public BError getError() {
        return this.strand.getError();
    }

    @Override
    public void setError(BError error) {
        this.strand.setError(error);
    }

    @Override
    public ProgramFile getProgramFile() {
        return this.strand.programFile;
    }

    @Override
    public long getIntArgument(int index) {
        return sf.longRegs[index];
    }

    @Override
    public String getStringArgument(int index) {
        String str = sf.stringRegs[index];
        if (str == null) {
            throw new BLangNullReferenceException();
        }

        return str;
    }

    @Override
    public String getNullableStringArgument(int index) {
        return sf.stringRegs[index];
    }

    @Override
    public double getFloatArgument(int index) {
        return sf.doubleRegs[index];
    }

    @Override
    public boolean getBooleanArgument(int index) {
        return (sf.intRegs[index] == 1);
    }

    @Override
    public BValue getRefArgument(int index) {
        BValue result = sf.refRegs[index];
        if (result == null) {
            throw new BallerinaException("argument " + index + " is null");
        }

        return result;
    }

    @Override
    public BValue getNullableRefArgument(int index) {
        return sf.refRegs[index];
    }

    @Override
    public void setReturnValues(BValue... value) {
        if (value != null && value.length > 0) {
            this.returnValue = value[0];
        }
    }

    @Override
    public BValue getReturnValue() {
        return this.returnValue;
    }

    public TransactionLocalContext getLocalTransactionInfo() {
        return this.strand.getLocalTransactionContext();
    }

}
