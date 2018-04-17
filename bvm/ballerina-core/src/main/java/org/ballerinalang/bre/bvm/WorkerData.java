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
package org.ballerinalang.bre.bvm;

import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.util.BLangConstants;
import org.ballerinalang.util.codegen.attributes.CodeAttributeInfo;
import org.ballerinalang.util.program.WorkerDataIndex;

import java.util.Arrays;

/**
 * This represents the local variables that are available to a worker. 
 * 
 * @since 0.965.0
 */
public class WorkerData {

    public long[] longRegs;
    
    public double[] doubleRegs;
    
    public String[] stringRegs;
    
    public int[] intRegs;
    
    public byte[][] byteRegs;
    
    public BRefType<?>[] refRegs;

    public WorkerData() {}
    
    public WorkerData(CodeAttributeInfo ci) {
        if (ci.maxLongRegs > 0) {
            this.longRegs = new long[ci.maxLongRegs];
        }
        if (ci.maxDoubleRegs > 0) {
            this.doubleRegs = new double[ci.maxDoubleRegs];
        }
        if (ci.maxStringRegs > 0) {
            this.stringRegs = new String[ci.maxStringRegs];
            Arrays.fill(this.stringRegs, BLangConstants.STRING_EMPTY_VALUE);
        }
        if (ci.maxIntRegs > 0) {
            this.intRegs = new int[ci.maxIntRegs];
        }
        if (ci.maxByteRegs > 0) {
            this.byteRegs = new byte[ci.maxByteRegs][];
        }
        if (ci.maxBValueRegs > 0) {
            this.refRegs = new BRefType[ci.maxBValueRegs];
        }
    }
    
    public WorkerData(WorkerDataIndex wdi) {
        if (wdi.longRegCount > 0) {
            this.longRegs = new long[wdi.longRegCount];
        }
        if (wdi.doubleRegCount > 0) {
            this.doubleRegs = new double[wdi.doubleRegCount];
        }
        if (wdi.stringRegCount > 0) {
            this.stringRegs = new String[wdi.stringRegCount];
            Arrays.fill(this.stringRegs, BLangConstants.STRING_EMPTY_VALUE);
        }
        if (wdi.intRegCount > 0) {
            this.intRegs = new int[wdi.intRegCount];
        }
        if (wdi.byteRegCount > 0) {
            this.byteRegs = new byte[wdi.byteRegCount][];
        }
        if (wdi.refRegCount > 0) {
            this.refRegs = new BRefType[wdi.refRegCount];
        }
    }
    
    public WorkerData(WorkerDataIndex wdi1, WorkerDataIndex wdi2) {
        int count = wdi1.longRegCount + wdi2.longRegCount;
        if (count > 0) {
            this.longRegs = new long[count];
        }
        count = wdi1.doubleRegCount + wdi2.doubleRegCount;
        if (count > 0) {
            this.doubleRegs = new double[count];
        }
        count = wdi1.stringRegCount + wdi2.stringRegCount;
        if (count > 0) {
            this.stringRegs = new String[count];
            Arrays.fill(this.stringRegs, BLangConstants.STRING_EMPTY_VALUE);
        }
        count = wdi1.intRegCount + wdi2.intRegCount;
        if (count > 0) {
            this.intRegs = new int[count];
        }
        count = wdi1.byteRegCount + wdi2.byteRegCount;
        if (count > 0) {
            this.byteRegs = new byte[count][];
        }
        count = wdi1.refRegCount + wdi2.refRegCount;
        if (count > 0) {
            this.refRegs = new BRefType[count];
        }
    }
        
}
