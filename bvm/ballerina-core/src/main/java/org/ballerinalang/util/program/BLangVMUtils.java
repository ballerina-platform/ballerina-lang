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
package org.ballerinalang.util.program;

import org.ballerinalang.bre.bvm.WorkerData;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.BLangConstants;
import org.ballerinalang.util.codegen.WorkerInfo;
import org.ballerinalang.util.codegen.attributes.CodeAttributeInfo;

/**
 * Utilities related to the Ballerina VM.
 */
public class BLangVMUtils {

    public static void copyArgValues(WorkerData caller, WorkerData callee, int[] argRegs, BType[] paramTypes) {
        int longRegIndex = -1;
        int doubleRegIndex = -1;
        int stringRegIndex = -1;
        int booleanRegIndex = -1;
        int refRegIndex = -1;
        int blobRegIndex = -1;
        for (int i = 0; i < argRegs.length; i++) {
            BType paramType = paramTypes[i];
            int argReg = argRegs[i];
            switch (paramType.getTag()) {
            case TypeTags.INT_TAG:
                callee.longRegs[++longRegIndex] = caller.longRegs[argReg];
                break;
            case TypeTags.FLOAT_TAG:
                callee.doubleRegs[++doubleRegIndex] = caller.doubleRegs[argReg];
                break;
            case TypeTags.STRING_TAG:
                callee.stringRegs[++stringRegIndex] = caller.stringRegs[argReg];
                break;
            case TypeTags.BOOLEAN_TAG:
                callee.intRegs[++booleanRegIndex] = caller.intRegs[argReg];
                break;
            case TypeTags.BLOB_TAG:
                callee.byteRegs[++blobRegIndex] = caller.byteRegs[argReg];
                break;
            default:
                callee.refRegs[++refRegIndex] = caller.refRegs[argReg];
            }
        }
    }

    public static WorkerData createWorkerDataForLocal(WorkerInfo workerInfo, WorkerExecutionContext parentCtx,
            int[] argRegs, BType[] paramTypes) {
        WorkerData wd = new WorkerData();
        CodeAttributeInfo ci = workerInfo.getCodeAttributeInfo();
        wd.longRegs = new long[ci.getMaxLongRegs()];
        wd.doubleRegs = new double[ci.getMaxDoubleRegs()];
        wd.stringRegs = new String[ci.getMaxStringRegs()];
        wd.intRegs = new int[ci.getMaxIntRegs()];
        wd.byteRegs = new byte[ci.getMaxByteRegs()][];
        wd.refRegs = new BRefType[ci.getMaxRefRegs()];
        BLangVMUtils.copyArgValues(parentCtx.workerLocal, wd, argRegs, paramTypes);
        return wd;
    }

    @SuppressWarnings("rawtypes")
    public static void populateWorkerDataWithValues(WorkerData data, int[] regIndexes, BValue[] vals, BType[] types) {
        for (int i = 0; i < vals.length; i++) {
            int callersRetRegIndex = regIndexes[i];
            BType retType = types[i];
            switch (retType.getTag()) {
            case TypeTags.INT_TAG:
                if (vals[i] == null) {
                    data.longRegs[callersRetRegIndex] = 0;
                    break;
                }
                data.longRegs[callersRetRegIndex] = ((BInteger) vals[i]).intValue();
                break;
            case TypeTags.FLOAT_TAG:
                if (vals[i] == null) {
                    data.doubleRegs[callersRetRegIndex] = 0;
                    break;
                }
                data.doubleRegs[callersRetRegIndex] = ((BFloat) vals[i]).floatValue();
                break;
            case TypeTags.STRING_TAG:
                if (vals[i] == null) {
                    data.stringRegs[callersRetRegIndex] = BLangConstants.STRING_NULL_VALUE;
                    break;
                }
                data.stringRegs[callersRetRegIndex] = vals[i].stringValue();
                break;
            case TypeTags.BOOLEAN_TAG:
                if (vals[i] == null) {
                    data.intRegs[callersRetRegIndex] = 0;
                    break;
                }
                data.intRegs[callersRetRegIndex] = ((BBoolean) vals[i]).booleanValue() ? 1 : 0;
                break;
            case TypeTags.BLOB_TAG:
                if (vals[i] == null) {
                    data.byteRegs[callersRetRegIndex] = new byte[0];
                    break;
                }
                data.byteRegs[callersRetRegIndex] = ((BBlob) vals[i]).blobValue();
                break;
            default:
                data.refRegs[callersRetRegIndex] = (BRefType) vals[i];
            }
        }
    }

}
