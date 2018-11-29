/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.bre.vm;

import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.Instruction;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.attributes.CodeAttributeInfo;
import org.ballerinalang.util.codegen.cpentries.ConstantPoolEntry;

import java.util.HashMap;
import java.util.Map;

/**
 * This represents the local variables that are available to a worker. 
 * 
 * @since 0.985.0
 */
public class StackFrame {

    public long[] longRegs;

    public double[] doubleRegs;

    public String[] stringRegs;

    public int[] intRegs;

    public BRefType<?>[] refRegs;

    public CallableUnitInfo callableUnitInfo;

    public Map<String, Object> localProps = new HashMap<>();

    // Cached value
    ConstantPoolEntry[] constPool;

    // Cached value
    public Instruction[] code;

    // Instruction pointer
    public int ip;

    // Return registry index
    int retReg;

    public StackFrame() {}

    public StackFrame(CallableUnitInfo callableUnitInfo, int retTypeTag) {
        int maxRetRegSize = 1; // Since we have single value return, this is 1
        switch (retTypeTag) {
            case TypeTags.INT_TAG:
                this.longRegs = new long[maxRetRegSize];
                break;
            case TypeTags.BYTE_TAG:
                this.intRegs = new int[maxRetRegSize];
                break;
            case TypeTags.FLOAT_TAG:
                this.doubleRegs = new double[maxRetRegSize];
                break;
            case TypeTags.STRING_TAG:
                this.stringRegs = new String[maxRetRegSize];
                break;
            case TypeTags.BOOLEAN_TAG:
                this.intRegs = new int[maxRetRegSize];
                break;
            default:
                this.refRegs = new BRefType[maxRetRegSize];
                break;
        }
        this.callableUnitInfo = callableUnitInfo;
    }

    public StackFrame(PackageInfo packageInfo, CallableUnitInfo callableUnitInfo, CodeAttributeInfo ci, int retReg) {
        if (ci.maxLongRegs > 0) {
            this.longRegs = new long[ci.maxLongRegs];
        }
        if (ci.maxDoubleRegs > 0) {
            this.doubleRegs = new double[ci.maxDoubleRegs];
        }
        if (ci.maxStringRegs > 0) {
            this.stringRegs = new String[ci.maxStringRegs];
        }
        if (ci.maxIntRegs > 0) {
            this.intRegs = new int[ci.maxIntRegs];
        }
        if (ci.maxBValueRegs > 0) {
            this.refRegs = new BRefType[ci.maxBValueRegs];
        }
        this.ip = ci.getCodeAddrs();
        this.callableUnitInfo = callableUnitInfo;
        this.constPool = packageInfo.getConstPoolEntries();
        this.code = packageInfo.getInstructions();
        this.retReg = retReg;
    }
        
}
