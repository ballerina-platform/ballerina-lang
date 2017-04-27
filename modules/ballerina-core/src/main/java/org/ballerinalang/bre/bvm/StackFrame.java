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
import org.ballerinalang.util.codegen.CodeAttributeInfo;
import org.ballerinalang.util.codegen.FunctionInfo;

/**
 * @since 0.87
 */
public class StackFrame {
    // These are used for array indexes and boolean values;
    int[] intLocalVars;
    long[] longLocalVars;
    double[] doubleLocalVars;
    String[] stringLocalVars;
    BRefType[] bValueLocalVars;

    int[] intRegs;
    long[] longRegs;
    double[] doubleRegs;
    String[] stringRegs;
    BRefType[] bValueRegs;

    // Return address of the caller
    int retAddrs;

    // Caller's Register indexes to which the return values should be copied;
    int[] retRegIndexes;

    FunctionInfo functionInfo;

    public StackFrame(FunctionInfo functionInfo, int retAddrs, int[] retRegIndexes) {
        this.functionInfo = functionInfo;
        CodeAttributeInfo codeAttribInfo = functionInfo.getCodeAttributeInfo();

        this.longLocalVars = new long[codeAttribInfo.getMaxLongLocalVars()];
        this.doubleLocalVars = new double[codeAttribInfo.getMaxDoubleLocalVars()];
        this.intLocalVars = new int[codeAttribInfo.getMaxIntLocalVars()];
        this.stringLocalVars = new String[codeAttribInfo.getMaxStringLocalVars()];
        this.bValueLocalVars = new BRefType[codeAttribInfo.getMaxBValueLocalVars()];

        this.longRegs = new long[codeAttribInfo.getMaxLongRegs()];
        this.doubleRegs = new double[codeAttribInfo.getMaxDoubleRegs()];
        this.intRegs = new int[codeAttribInfo.getMaxIntRegs()];
        this.stringRegs = new String[codeAttribInfo.getMaxStringRegs()];
        this.bValueRegs = new BRefType[codeAttribInfo.getMaxBValueRegs()];

        this.retAddrs = retAddrs;
        this.retRegIndexes = retRegIndexes;
    }
}
