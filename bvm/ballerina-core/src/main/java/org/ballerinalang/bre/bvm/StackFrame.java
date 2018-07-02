/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.bre.bvm;

import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.WorkerInfo;
import org.ballerinalang.util.codegen.attributes.CodeAttributeInfo;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@code StackFrame} represents frame in a control stack.
 * Holds references to parameters, local variables and return values.
 *
 * @since 0.88
 */
public class StackFrame {
    long[] longRegs;
    double[] doubleRegs;
    String[] stringRegs;
    int[] intRegs;
    byte[][] byteRegs;
    BRefType[] refRegs;

    // Return address of the caller
    int retAddrs;

    // Caller's Register indexes to which the return values should be copied;
    int[] retRegIndexes;

    // Error thrown by current stack.
    BMap<String, BValue> errorThrown;

    CallableUnitInfo callableUnitInfo;
    PackageInfo packageInfo;
    WorkerInfo workerInfo;

    // To support old native function and action invocation
    public BValue[] returnValues;

    // To support worker return.
    final AtomicBoolean workerReturned = new AtomicBoolean();
    String returnedWorker = "";
    
    public StackFrame prevStackFrame;

    public StackFrame(PackageInfo packageInfo, int retAddrs, int[] retRegIndexes) {
        this.packageInfo = packageInfo;
        this.retAddrs = retAddrs;
        this.retRegIndexes = retRegIndexes;
    }

    public StackFrame(CallableUnitInfo callableUnitInfo, WorkerInfo workerInfo, int retAddrs, int[] retRegIndexes) {
        this.callableUnitInfo = callableUnitInfo;
        this.packageInfo = callableUnitInfo.getPackageInfo();
        this.workerInfo = workerInfo;
        CodeAttributeInfo codeAttribInfo = workerInfo.getCodeAttributeInfo();

        this.longRegs = new long[codeAttribInfo.getMaxLongRegs()];
        this.doubleRegs = new double[codeAttribInfo.getMaxDoubleRegs()];
        this.stringRegs = new String[codeAttribInfo.getMaxStringRegs()];
        this.intRegs = new int[codeAttribInfo.getMaxIntRegs()];
        this.byteRegs = new byte[codeAttribInfo.getMaxByteRegs()][];
        Arrays.fill(this.byteRegs, new byte[0]);
        this.refRegs = new BRefType[codeAttribInfo.getMaxRefRegs()];

        this.retAddrs = retAddrs;
        this.retRegIndexes = retRegIndexes;
    }

    public StackFrame(CallableUnitInfo callableUnitInfo, WorkerInfo workerInfo, int retAddrs, int[] retRegIndexes,
                      BValue[] returnValues) {
        this.callableUnitInfo = callableUnitInfo;
        this.packageInfo = callableUnitInfo.getPackageInfo();
        this.workerInfo = workerInfo;
        CodeAttributeInfo codeAttribInfo = workerInfo.getCodeAttributeInfo();

        this.longRegs = new long[codeAttribInfo.getMaxLongRegs()];
        this.doubleRegs = new double[codeAttribInfo.getMaxDoubleRegs()];
        this.stringRegs = new String[codeAttribInfo.getMaxStringRegs()];
        this.intRegs = new int[codeAttribInfo.getMaxIntRegs()];
        this.byteRegs = new byte[codeAttribInfo.getMaxByteRegs()][];
        Arrays.fill(this.byteRegs, new byte[0]);
        this.refRegs = new BRefType[codeAttribInfo.getMaxRefRegs()];

        this.retAddrs = retAddrs;
        this.retRegIndexes = retRegIndexes;
        this.returnValues = returnValues;
    }

    public long[] getLongRegs() {
        return longRegs;
    }

    public double[] getDoubleRegs() {
        return doubleRegs;
    }

    public String[] getStringRegs() {
        return stringRegs;
    }

    public int[] getIntRegs() {
        return intRegs;
    }

    public byte[][] getByteRegs() {
        return byteRegs;
    }

    public BRefType[] getRefRegs() {
        return refRegs;
    }

    public void setLongRegs(long[] longRegs) {
        this.longRegs = longRegs;
    }

    public void setDoubleRegs(double[] doubleRegs) {
        this.doubleRegs = doubleRegs;
    }

    public void setStringRegs(String[] stringRegs) {
        this.stringRegs = stringRegs;
    }

    public void setIntRegs(int[] intRegs) {
        this.intRegs = intRegs;
    }

    public void setByteRegs(byte[][] byteRegs) {
        this.byteRegs = byteRegs;
    }

    public void setRefRegs(BRefType[] refRegs) {
        this.refRegs = refRegs;
    }

    public int getRetAddrs() {
        return retAddrs;
    }

    public BMap<String, BValue> getErrorThrown() {
        return errorThrown;
    }

    public void setErrorThrown(BMap<String, BValue> errorThrown) {
        this.errorThrown = errorThrown;
    }

    public CallableUnitInfo getCallableUnitInfo() {
        return callableUnitInfo;
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public WorkerInfo getWorkerInfo() {
        return workerInfo;
    }

    public boolean tryReturn() {
        return this.workerReturned.compareAndSet(false, true);
    }

    public void markedAsReturned() {
        this.workerReturned.set(true);
    }
}
