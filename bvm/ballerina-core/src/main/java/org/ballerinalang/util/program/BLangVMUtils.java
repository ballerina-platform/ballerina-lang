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
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.BLangConstants;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.LocalVariableInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.WorkerInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfo;
import org.ballerinalang.util.codegen.attributes.CodeAttributeInfo;
import org.ballerinalang.util.codegen.attributes.LocalVariableAttributeInfo;

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
    
    public static BValue[] populateReturnData(WorkerExecutionContext ctx, CallableUnitInfo callableUnitInfo, 
            int[] retRegs) {
        WorkerData data = ctx.workerLocal;
        BType[] retTypes = callableUnitInfo.getRetParamTypes();
        BValue[] returnValues = new BValue[retTypes.length];
        for (int i = 0; i < returnValues.length; i++) {
            BType retType = retTypes[i];
            switch (retType.getTag()) {
            case TypeTags.INT_TAG:
                returnValues[i] = new BInteger(data.longRegs[retRegs[i]]);
                break;
            case TypeTags.FLOAT_TAG:
                returnValues[i] = new BFloat(data.doubleRegs[retRegs[i]]);
                break;
            case TypeTags.STRING_TAG:
                returnValues[i] = new BString(data.stringRegs[retRegs[i]]);
                break;
            case TypeTags.BOOLEAN_TAG:
                boolean boolValue = data.intRegs[retRegs[i]] == 1;
                returnValues[i] = new BBoolean(boolValue);
                break;
            case TypeTags.BLOB_TAG:
                returnValues[i] = new BBlob(data.byteRegs[retRegs[i]]);
                break;
            default:
                returnValues[i] = data.refRegs[retRegs[i]];
                break;
            }
        }
        return returnValues;
    }

    public static int[] createReturnRegValues(WorkerDataIndex paramWDI, WorkerDataIndex retWDI, BType[] retTypes) {
        int[] result = new int[retWDI.retRegs.length];
        System.arraycopy(retWDI.retRegs, 0, result, 0, result.length);
        for (int i = 0; i < result.length; i++) {
            BType retType = retTypes[i];
            switch (retType.getTag()) {
            case TypeTags.INT_TAG:
                result[i] += paramWDI.longRegCount;
                break;
            case TypeTags.FLOAT_TAG:
                result[i] += paramWDI.doubleRegCount;
                break;
            case TypeTags.STRING_TAG:
                result[i] += paramWDI.stringRegCount;
                break;
            case TypeTags.BOOLEAN_TAG:
                result[i] += paramWDI.intRegCount;
                break;
            case TypeTags.BLOB_TAG:
                result[i] += paramWDI.byteRegCount;
                break;
            default:
                result[i] += paramWDI.refRegCount;
                break;
            }
        }
        return result;
    }
    
    @SuppressWarnings("rawtypes")
    public static int[][] populateArgAndReturnData(WorkerExecutionContext ctx, 
            CallableUnitInfo callableUnitInfo, BValue[] args) {
        WorkerDataIndex wdi1 = callableUnitInfo.paramWorkerIndex;
        WorkerDataIndex wdi2 = callableUnitInfo.retWorkerIndex;
        WorkerData local = createWorkerData(wdi1, wdi2);
        BType[] types = callableUnitInfo.getParamTypes();
        int longParamCount = 0, doubleParamCount = 0, stringParamCount = 0, intParamCount = 0, 
                byteParamCount = 0, refParamCount = 0;
        for (int i = 0; i < types.length; i++) {
            switch (types[i].getTag()) {
                case TypeTags.INT_TAG:
                    local.longRegs[longParamCount] = ((BInteger) args[i]).intValue();
                    longParamCount++;
                    break;
                case TypeTags.FLOAT_TAG:
                    local.doubleRegs[doubleParamCount] = ((BFloat) args[i]).floatValue();
                    doubleParamCount++;
                    break;
                case TypeTags.STRING_TAG:
                    local.stringRegs[stringParamCount] = args[i].stringValue();
                    stringParamCount++;
                    break;
                case TypeTags.BOOLEAN_TAG:
                    local.intRegs[intParamCount] = ((BBoolean) args[i]).booleanValue() ? 1 : 0;
                    intParamCount++;
                    break;
                case TypeTags.BLOB_TAG:
                    local.byteRegs[byteParamCount] = ((BBlob) args[i]).blobValue();
                    byteParamCount++;
                    break;
                default:
                    local.refRegs[refParamCount] = (BRefType) args[i];
                    refParamCount++;
                    break;
            }
        }
        ctx.workerLocal = local;
        return new int[][] { wdi1.retRegs, BLangVMUtils.createReturnRegValues(
                wdi1, wdi2, callableUnitInfo.getRetParamTypes()) };
    }
    
    public static WorkerData createWorkerData(WorkerDataIndex wdi) {
        WorkerData wd = new WorkerData();
        wd.longRegs = new long[wdi.longRegCount];
        wd.doubleRegs = new double[wdi.doubleRegCount];
        wd.stringRegs = new String[wdi.stringRegCount];
        wd.intRegs = new int[wdi.intRegCount];
        wd.byteRegs = new byte[wdi.byteRegCount][];
        wd.refRegs = new BRefType[wdi.refRegCount];
        return wd;
    }
    
    private static WorkerData createWorkerData(WorkerDataIndex wdi1, WorkerDataIndex wdi2) {
        WorkerData wd = new WorkerData();
        wd.longRegs = new long[wdi1.longRegCount + wdi2.longRegCount];
        wd.doubleRegs = new double[wdi1.doubleRegCount + wdi2.doubleRegCount];
        wd.stringRegs = new String[wdi1.stringRegCount + wdi2.stringRegCount];
        wd.intRegs = new int[wdi1.intRegCount + wdi2.intRegCount];
        wd.byteRegs = new byte[wdi1.byteRegCount + wdi2.byteRegCount][];
        wd.refRegs = new BRefType[wdi1.refRegCount + wdi2.refRegCount];
        return wd;
    }
    
    public static void processUnresolvedAnnAttrValues(ProgramFile programFile) {
        programFile.getUnresolvedAnnAttrValues().forEach(entry -> {
            PackageInfo packageInfo = programFile.getPackageInfo(entry.getConstPkg());
            LocalVariableAttributeInfo localVariableAttributeInfo = (LocalVariableAttributeInfo) packageInfo
                    .getAttributeInfo(AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE);

            LocalVariableInfo localVariableInfo = localVariableAttributeInfo.getLocalVarialbeDetails(entry.getConstName());

            switch (localVariableInfo.getVariableType().getTag()) {
                case TypeTags.BOOLEAN_TAG:
                    entry.setBooleanValue(programFile.getGlobalMemoryBlock().getBooleanField(localVariableInfo
                            .getVariableIndex()) == 1 ? true : false);
                    break;
                case TypeTags.INT_TAG:
                    entry.setIntValue(programFile.getGlobalMemoryBlock().getIntField(localVariableInfo
                            .getVariableIndex()));
                    break;
                case TypeTags.FLOAT_TAG:
                    entry.setFloatValue(programFile.getGlobalMemoryBlock().getFloatField(localVariableInfo
                            .getVariableIndex()));
                    break;
                case TypeTags.STRING_TAG:
                    entry.setStringValue(programFile.getGlobalMemoryBlock().getStringField(localVariableInfo
                            .getVariableIndex()));
                    break;
            }
        });
    }
    
    public static BStruct createErrorStruct(Throwable e) {
        //TODO
        return null;
    }
    
}
