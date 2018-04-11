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

import org.ballerinalang.bre.bvm.CPU;
import org.ballerinalang.bre.bvm.CPU.HandleErrorException;
import org.ballerinalang.bre.bvm.WorkerData;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.model.types.BSingletonType;
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
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.codegen.WorkerInfo;
import org.ballerinalang.util.codegen.attributes.CodeAttributeInfo;
import org.ballerinalang.util.transactions.LocalTransactionInfo;

import java.io.PrintStream;

/**
 * Utilities related to the Ballerina VM.
 */
public class BLangVMUtils {
    
    private static final String SERVICE_INFO_KEY = "SERVICE_INFO";

    private static final String TRANSACTION_INFO_KEY = "TRANSACTION_INFO";

    private static final String GLOBAL_TRANSACTION_ENABLED = "GLOBAL_TRANSACTION_ENABLED";

    private static BType resolveToSuperType(BType bType) {
        if (bType.getTag() == TypeTags.SINGLETON_TAG) {
            return ((BSingletonType) bType).superSetType;
        }
        return bType;
    }

    public static void copyArgValues(WorkerData caller, WorkerData callee, int[] argRegs, BType[] paramTypes) {
        int longRegIndex = -1;
        int doubleRegIndex = -1;
        int stringRegIndex = -1;
        int booleanRegIndex = -1;
        int refRegIndex = -1;
        int blobRegIndex = -1;
        for (int i = 0; i < argRegs.length; i++) {
            BType paramType = resolveToSuperType(paramTypes[i]);
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

    public static void copyValuesForForkJoin(WorkerData caller, WorkerData callee, int[] argRegs) {
        int longLocalVals = argRegs[0];
        int doubleLocalVals = argRegs[1];
        int stringLocalVals = argRegs[2];
        int booleanLocalVals = argRegs[3];
        int blobLocalVals = argRegs[4];
        int refLocalVals = argRegs[5];

        for (int i = 0; i <= longLocalVals; i++) {
            callee.longRegs[i] = caller.longRegs[i];
        }

        for (int i = 0; i <= doubleLocalVals; i++) {
            callee.doubleRegs[i] = caller.doubleRegs[i];
        }

        for (int i = 0; i <= stringLocalVals; i++) {
            callee.stringRegs[i] = caller.stringRegs[i];
        }

        for (int i = 0; i <= booleanLocalVals; i++) {
            callee.intRegs[i] = caller.intRegs[i];
        }

        for (int i = 0; i <= refLocalVals; i++) {
            callee.refRegs[i] = caller.refRegs[i];
        }

        for (int i = 0; i <= blobLocalVals; i++) {
            callee.byteRegs[i] = caller.byteRegs[i];
        }
    }

    public static WorkerData createWorkerDataForLocal(WorkerInfo workerInfo, WorkerExecutionContext parentCtx,
            int[] argRegs, BType[] paramTypes) {
        WorkerData wd = createWorkerData(workerInfo);
        BLangVMUtils.copyArgValues(parentCtx.workerLocal, wd, argRegs, paramTypes);
        return wd;
    }

    static WorkerData createWorkerDataForLocal(WorkerInfo workerInfo, WorkerExecutionContext parentCtx,
                                               int[] argRegs) {
        WorkerData wd = createWorkerData(workerInfo);
        BLangVMUtils.copyValuesForForkJoin(parentCtx.workerLocal, wd, argRegs);
        return wd;
    }

    private static WorkerData createWorkerData(WorkerInfo workerInfo) {
        return new WorkerData(workerInfo.getCodeAttributeInfo());
    }

    @SuppressWarnings("rawtypes")
    public static void populateWorkerDataWithValues(WorkerData data, int[] regIndexes, BValue[] vals, BType[] types) {
        if (vals == null) {
            return;
        }
        for (int i = 0; i < vals.length; i++) {
            int callersRetRegIndex = regIndexes[i];
            BType retType = resolveToSuperType(types[i]);
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
                    data.stringRegs[callersRetRegIndex] = BLangConstants.STRING_EMPTY_VALUE;
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
    
    @SuppressWarnings("rawtypes")
    public static void populateWorkerResultWithValues(WorkerData result, BValue[] vals, BType[] types) {
        if (vals == null) {
            return;
        }
        int longRegCount = 0;
        int doubleRegCount = 0;
        int stringRegCount = 0;
        int intRegCount = 0;
        int refRegCount = 0;
        int byteRegCount = 0;
        for (int i = 0; i < vals.length; i++) {
            BType retType = resolveToSuperType(types[i]);
            switch (retType.getTag()) {
            case TypeTags.INT_TAG:
                if (vals[i] == null) {
                    result.longRegs[longRegCount++] = 0;
                    break;
                }
                result.longRegs[longRegCount++] = ((BInteger) vals[i]).intValue();
                break;
            case TypeTags.FLOAT_TAG:
                if (vals[i] == null) {
                    result.doubleRegs[doubleRegCount++] = 0;
                    break;
                }
                result.doubleRegs[doubleRegCount++] = ((BFloat) vals[i]).floatValue();
                break;
            case TypeTags.STRING_TAG:
                if (vals[i] == null) {
                    result.stringRegs[stringRegCount++] = BLangConstants.STRING_NULL_VALUE;
                    break;
                }
                result.stringRegs[stringRegCount++] = vals[i].stringValue();
                break;
            case TypeTags.BOOLEAN_TAG:
                if (vals[i] == null) {
                    result.intRegs[intRegCount++] = 0;
                    break;
                }
                result.intRegs[intRegCount++] = ((BBoolean) vals[i]).booleanValue() ? 1 : 0;
                break;
            case TypeTags.BLOB_TAG:
                if (vals[i] == null) {
                    result.byteRegs[byteRegCount++] = new byte[0];
                    break;
                }
                result.byteRegs[byteRegCount++] = ((BBlob) vals[i]).blobValue();
                break;
            default:
                result.refRegs[refRegCount++] = (BRefType) vals[i];
            }
        }
    }
    
    public static BValue[] populateReturnData(WorkerExecutionContext ctx, CallableUnitInfo callableUnitInfo, 
            int[] retRegs) {
        WorkerData data = ctx.workerLocal;
        BType[] retTypes = callableUnitInfo.getRetParamTypes();
        BValue[] returnValues = new BValue[retTypes.length];
        for (int i = 0; i < returnValues.length; i++) {
            BType retType = resolveToSuperType(retTypes[i]);
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
            BType retType = resolveToSuperType(retTypes[i]);
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
            switch (resolveToSuperType(types[i]).getTag()) {
                case TypeTags.INT_TAG:
                    if (args[i] instanceof BString) {
                        local.longRegs[longParamCount] = ((BString) args[i]).intValue();
                    } else {
                        local.longRegs[longParamCount] = ((BInteger) args[i]).intValue();
                    }
                    longParamCount++;
                    break;
                case TypeTags.FLOAT_TAG:
                    if (args[i] instanceof BString) {
                        local.doubleRegs[doubleParamCount] = ((BString) args[i]).floatValue();
                    } else {
                        local.doubleRegs[doubleParamCount] = ((BFloat) args[i]).floatValue();
                    }
                    doubleParamCount++;
                    break;
                case TypeTags.STRING_TAG:
                    local.stringRegs[stringParamCount] = args[i].stringValue();
                    stringParamCount++;
                    break;
                case TypeTags.BOOLEAN_TAG:
                    if (args[i] instanceof BString) {
                        local.intRegs[intParamCount] = ((BString) args[i]).value().toLowerCase().equals("true") ? 1 : 0;
                    } else {
                        local.intRegs[intParamCount] = ((BBoolean) args[i]).booleanValue() ? 1 : 0;
                    }
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
        return new WorkerData(wdi);
    }
    
    private static WorkerData createWorkerData(WorkerDataIndex wdi1, WorkerDataIndex wdi2) {
        return new WorkerData(wdi1, wdi2);
    }
    
    public static void mergeResultData(WorkerData sourceData, WorkerData targetData, BType[] types,
            int[] regIndexes) {
        int callersRetRegIndex;
        int longRegCount = 0;
        int doubleRegCount = 0;
        int stringRegCount = 0;
        int intRegCount = 0;
        int refRegCount = 0;
        int byteRegCount = 0;
        for (int i = 0; i < types.length; i++) {
            BType retType = resolveToSuperType(types[i]);
            callersRetRegIndex = regIndexes[i];
            switch (retType.getTag()) {
            case TypeTags.INT_TAG:
                targetData.longRegs[callersRetRegIndex] = sourceData.longRegs[longRegCount++];
                break;
            case TypeTags.FLOAT_TAG:
                targetData.doubleRegs[callersRetRegIndex] = sourceData.doubleRegs[doubleRegCount++];
                break;
            case TypeTags.STRING_TAG:
                targetData.stringRegs[callersRetRegIndex] = sourceData.stringRegs[stringRegCount++];
                break;
            case TypeTags.BOOLEAN_TAG:
                targetData.intRegs[callersRetRegIndex] = sourceData.intRegs[intRegCount++];
                break;
            case TypeTags.BLOB_TAG:
                targetData.byteRegs[callersRetRegIndex] = sourceData.byteRegs[byteRegCount++];
                break;
            default:
                targetData.refRegs[callersRetRegIndex] = sourceData.refRegs[refRegCount++];
                break;
            }
        }
    }
    
    public static void mergeInitWorkertData(WorkerData sourceData, WorkerData targetData, 
            CodeAttributeInfo initWorkerCAI) {
        for (int i = 0; i < initWorkerCAI.getMaxByteLocalVars(); i++) {
            targetData.byteRegs[i] = sourceData.byteRegs[i];
        }
        for (int i = 0; i < initWorkerCAI.getMaxDoubleLocalVars(); i++) {
            targetData.doubleRegs[i] = sourceData.doubleRegs[i];
        }
        for (int i = 0; i < initWorkerCAI.getMaxIntLocalVars(); i++) {
            targetData.intRegs[i] = sourceData.intRegs[i];
        }
        for (int i = 0; i < initWorkerCAI.getMaxLongLocalVars(); i++) {
            targetData.longRegs[i] = sourceData.longRegs[i];
        }
        for (int i = 0; i < initWorkerCAI.getMaxStringLocalVars(); i++) {
            targetData.stringRegs[i] = sourceData.stringRegs[i];
        }
        for (int i = 0; i < initWorkerCAI.getMaxRefLocalVars(); i++) {
            targetData.refRegs[i] = sourceData.refRegs[i];
        }
    }
    
    public static WorkerExecutionContext handleNativeInvocationError(WorkerExecutionContext parentCtx, BStruct error) {
        parentCtx.setError(error);
        try {
            CPU.handleError(parentCtx);
            return parentCtx;
        } catch (HandleErrorException e) {
            if (e.ctx != null && !e.ctx.isRootContext()) {
                return e.ctx;
            } else {
                return null;
            }
        }
    }
    
    public static void log(String msg) {
        PrintStream out = System.out;
        out.println(msg);
    }
    
    public static void setServiceInfo(WorkerExecutionContext ctx, ServiceInfo serviceInfo) {
        ctx.globalProps.put(SERVICE_INFO_KEY, serviceInfo);
    }
    
    public static ServiceInfo getServiceInfo(WorkerExecutionContext ctx) {
        return (ServiceInfo) ctx.globalProps.get(SERVICE_INFO_KEY);
    }

    public static void setTransactionInfo(WorkerExecutionContext ctx, LocalTransactionInfo localTransactionInfo) {
        ctx.globalProps.put(TRANSACTION_INFO_KEY, localTransactionInfo);
    }

    public static LocalTransactionInfo getTransactionInfo(WorkerExecutionContext ctx) {
        return (LocalTransactionInfo) ctx.globalProps.get(TRANSACTION_INFO_KEY);
    }

    public static void removeTransactionInfo(WorkerExecutionContext ctx) {
        ctx.globalProps.remove(TRANSACTION_INFO_KEY);
    }

    public static void setGlobalTransactionEnabledStatus(WorkerExecutionContext ctx,
            boolean isGlobalTransactionEnabled) {
        ctx.globalProps.put(GLOBAL_TRANSACTION_ENABLED, isGlobalTransactionEnabled);
    }

    public static boolean getGlobalTransactionenabled(WorkerExecutionContext ctx) {
        return (boolean) ctx.globalProps.get(GLOBAL_TRANSACTION_ENABLED);
    }
}
