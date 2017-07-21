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

import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.WorkerInfo;
import org.ballerinalang.util.codegen.attributes.CodeAttributeInfo;

import java.util.Arrays;

/**
 * {@code StackFrame} represents frame in a control stack.
 * Holds references to parameters, local variables and return values.
 *
 * @since 0.88
 */
public class StackFrame {
    long[] longLocalVars;
    double[] doubleLocalVars;
    String[] stringLocalVars;
    // These are used for array indexes and boolean values;
    int[] intLocalVars;
    byte[][] byteLocalVars;
    BRefType[] refLocalVars;

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
    BStruct errorThrown;

    CallableUnitInfo callableUnitInfo;
    PackageInfo packageInfo;
    WorkerInfo workerInfo;

    // To Support old native function and action invocation
    // TODO Refactor this when native function and action invocations are improved.
    public BValue[] argValues;
    public BValue[] returnValues;

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

        this.longLocalVars = new long[codeAttribInfo.getMaxLongLocalVars()];
        this.doubleLocalVars = new double[codeAttribInfo.getMaxDoubleLocalVars()];

        this.stringLocalVars = new String[codeAttribInfo.getMaxStringLocalVars()];
        // Setting the zero values for strings
        Arrays.fill(stringLocalVars, "");

        this.intLocalVars = new int[codeAttribInfo.getMaxIntLocalVars()];
        this.byteLocalVars = new byte[codeAttribInfo.getMaxByteLocalVars()][];
        Arrays.fill(byteLocalVars, new byte[0]);

        this.refLocalVars = new BRefType[codeAttribInfo.getMaxRefLocalVars()];

        this.longRegs = new long[codeAttribInfo.getMaxLongRegs()];
        this.doubleRegs = new double[codeAttribInfo.getMaxDoubleRegs()];
        this.stringRegs = new String[codeAttribInfo.getMaxStringRegs()];
        this.intRegs = new int[codeAttribInfo.getMaxIntRegs()];
        this.byteRegs = new byte[codeAttribInfo.getMaxByteRegs()][];
        this.refRegs = new BRefType[codeAttribInfo.getMaxRefRegs()];

        this.retAddrs = retAddrs;
        this.retRegIndexes = retRegIndexes;
    }

    public CallableUnitInfo getCallableUnitInfo() {
        return callableUnitInfo;
    }

    public StackFrame(CallableUnitInfo callableUnitInfo, WorkerInfo workerInfo, int retAddrs, int[] retRegIndexes,
                      BValue[] returnValues) {
        this.callableUnitInfo = callableUnitInfo;
        this.packageInfo = callableUnitInfo.getPackageInfo();
        this.workerInfo = workerInfo;
        CodeAttributeInfo codeAttribInfo = workerInfo.getCodeAttributeInfo();

        this.longLocalVars = new long[codeAttribInfo.getMaxLongLocalVars()];
        this.doubleLocalVars = new double[codeAttribInfo.getMaxDoubleLocalVars()];

        this.stringLocalVars = new String[codeAttribInfo.getMaxStringLocalVars()];
        // Setting the zero values for strings
        Arrays.fill(stringLocalVars, "");

        this.intLocalVars = new int[codeAttribInfo.getMaxIntLocalVars()];
        this.byteLocalVars = new byte[codeAttribInfo.getMaxByteLocalVars()][];
        Arrays.fill(byteLocalVars, new byte[0]);
        this.refLocalVars = new BRefType[codeAttribInfo.getMaxRefLocalVars()];

        this.longRegs = new long[codeAttribInfo.getMaxLongRegs()];
        this.doubleRegs = new double[codeAttribInfo.getMaxDoubleRegs()];
        this.stringRegs = new String[codeAttribInfo.getMaxStringRegs()];
        this.intRegs = new int[codeAttribInfo.getMaxIntRegs()];
        this.byteRegs = new byte[codeAttribInfo.getMaxByteRegs()][];
        this.refRegs = new BRefType[codeAttribInfo.getMaxRefRegs()];

        this.retAddrs = retAddrs;
        this.retRegIndexes = retRegIndexes;
        this.returnValues = returnValues;
    }

    public long[] getLongLocalVars() {
        return longLocalVars;
    }

    public double[] getDoubleLocalVars() {
        return doubleLocalVars;
    }

    public String[] getStringLocalVars() {
        return stringLocalVars;
    }

    public int[] getIntLocalVars() {
        return intLocalVars;
    }

    public byte[][] getByteLocalVars() {
        return byteLocalVars;
    }

    public BRefType[] getRefLocalVars() {
        return refLocalVars;
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

    public void setLongLocalVars(long[] longLocalVars) {
        this.longLocalVars = longLocalVars;
    }

    public void setDoubleLocalVars(double[] doubleLocalVars) {
        this.doubleLocalVars = doubleLocalVars;
    }

    public void setStringLocalVars(String[] stringLocalVars) {
        this.stringLocalVars = stringLocalVars;
    }

    public void setIntLocalVars(int[] intLocalVars) {
        this.intLocalVars = intLocalVars;
    }

    public void setByteLocalVars(byte[][] byteLocalVars) {
        this.byteLocalVars = byteLocalVars;
    }

    public void setRefLocalVars(BRefType[] refLocalVars) {
        this.refLocalVars = refLocalVars;
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

    public BStruct getErrorThrown() {
        return errorThrown;
    }

    public void setErrorThrown(BStruct errorThrown) {
        this.errorThrown = errorThrown;
    }
}
