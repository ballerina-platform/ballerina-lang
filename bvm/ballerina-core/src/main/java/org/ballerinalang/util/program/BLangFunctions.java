/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.bre.bvm.BLangScheduler;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.CPU;
import org.ballerinalang.bre.bvm.InvocableWorkerResponseContext;
import org.ballerinalang.bre.bvm.WorkerData;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.bre.bvm.WorkerResponseContext;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.LocalVariableInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.WorkerInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfo;
import org.ballerinalang.util.codegen.attributes.LocalVariableAttributeInfo;
import org.ballerinalang.util.exceptions.BLangRuntimeException;

import java.util.Map;

/**
 * This class contains helper methods to invoke Ballerina functions.
 *
 * @since 0.8.0
 */
public class BLangFunctions {

    private BLangFunctions() { }

    public static BValue[] invokeNew(ProgramFile bLangProgram, String packageName, String functionName, BValue[] args) {
        return invokeNew(bLangProgram, packageName, functionName, args, new WorkerExecutionContext());
    }

    public static BValue[] invokeNew(ProgramFile bLangProgram, String packageName, String functionName,
                                     BValue[] args, WorkerExecutionContext parentCtx) {
        PackageInfo packageInfo = bLangProgram.getPackageInfo(packageName);
        FunctionInfo functionInfo = packageInfo.getFunctionInfo(functionName);

        if (functionInfo == null) {
            throw new RuntimeException("Function '" + functionName + "' is not defined");
        }

        if (functionInfo.getParamTypes().length != args.length) {
            throw new RuntimeException("Size of input argument arrays is not equal to size of function parameters");
        }

        invokePackageInitFunction(packageInfo.getInitFunctionInfo(), parentCtx);
        return invokeCallable(functionInfo, parentCtx, args);
    }

    private static WorkerData createWorkerData(WorkerDataIndex wdi) {
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

    public static void invokeCallable(CallableUnitInfo callableUnitInfo, WorkerExecutionContext parentCtx) {
        invokeCallable(callableUnitInfo, parentCtx, new int[0], new int[0], false);
    }
    
    public static BValue[] invokeCallable(CallableUnitInfo callableUnitInfo, BValue[] args) {
        return invokeCallable(callableUnitInfo, new WorkerExecutionContext(), args);
    }
    
    public static BValue[] invokeCallable(CallableUnitInfo callableUnitInfo, WorkerExecutionContext parentCtx, 
            BValue[] args) {
        int[][] regs = populateArgAndReturnData(parentCtx, callableUnitInfo, args);
        invokeCallable(callableUnitInfo, parentCtx, regs[0], regs[1], true);
        return populateReturnData(parentCtx, callableUnitInfo, regs[1]);
    }
    
    private static BValue[] populateReturnData(WorkerExecutionContext ctx, CallableUnitInfo callableUnitInfo, 
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
    
    @SuppressWarnings("rawtypes")
    private static int[][] populateArgAndReturnData(WorkerExecutionContext ctx, 
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
        return new int[][] { wdi1.retRegs, createReturnRegValues(wdi1, wdi2, callableUnitInfo.getRetParamTypes()) };
    }
    
    private static int[] createReturnRegValues(WorkerDataIndex paramWDI, WorkerDataIndex retWDI, BType[] retTypes) {
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
    
    public static WorkerExecutionContext invokeCallable(CallableUnitInfo callableUnitInfo, 
            WorkerExecutionContext parentCtx, int[] argRegs, int[] retRegs, boolean waitForResponse) {
        InvocableWorkerResponseContext respCtx = new InvocableWorkerResponseContext(
                callableUnitInfo.getRetParamTypes(), waitForResponse);
        respCtx.updateTargetContextInfo(parentCtx, retRegs);
        WorkerDataIndex wdi = callableUnitInfo.retWorkerIndex;
        Map<String, Object> globalProps = parentCtx.globalProps;
        BLangScheduler.switchToWaitForResponse(parentCtx);
        WorkerInfo[] workerInfos = listWorkerInfos(callableUnitInfo);
        for (int i = 1; i < workerInfos.length; i++) {
            executeWorker(respCtx, parentCtx, argRegs, callableUnitInfo, workerInfos[i], wdi, globalProps, false);
        }
        WorkerExecutionContext runInCallerCtx = executeWorker(respCtx, parentCtx, argRegs, callableUnitInfo, 
                workerInfos[0], wdi, globalProps, true);
        if (waitForResponse) {
            CPU.exec(runInCallerCtx);
            respCtx.waitForResponse();
        }
        return runInCallerCtx;
    }
    
    private static WorkerExecutionContext executeWorker(WorkerResponseContext respCtx, 
            WorkerExecutionContext parentCtx, int[] argRegs, CallableUnitInfo callableUnitInfo, 
            WorkerInfo workerInfo, WorkerDataIndex wdi, Map<String, Object> globalProps, boolean runInCaller) {
        WorkerData workerLocal = BLangVMUtils.createWorkerDataForLocal(workerInfo, parentCtx, argRegs,
                callableUnitInfo.getParamTypes());
        WorkerData workerResult = createWorkerData(wdi);
        WorkerExecutionContext ctx = new WorkerExecutionContext(parentCtx, respCtx, callableUnitInfo, workerInfo,
                workerLocal, workerResult, wdi.retRegs, globalProps, runInCaller);
        return BLangScheduler.schedule(ctx);
    }
    
    private static WorkerInfo[] listWorkerInfos(CallableUnitInfo callableUnitInfo) {
        WorkerInfo[] result = callableUnitInfo.getWorkerInfoEntries();
        if (result.length == 0) {
            result = new WorkerInfo[] { callableUnitInfo.getDefaultWorkerInfo() };
        }
        return result;
    }
    
    public static void invokePackageInitFunction(FunctionInfo initFuncInfo, WorkerExecutionContext context) {
        invokeCallable(initFuncInfo, context, new int[0], new int[0], true);
        if (context.getError() != null) {
            String stackTraceStr = BLangVMErrors.getPrintableStackTrace(context.getError());
            throw new BLangRuntimeException("error: " + stackTraceStr);
        }
        ProgramFile programFile = initFuncInfo.getPackageInfo().getProgramFile();
        if (programFile.getUnresolvedAnnAttrValues() == null) {
            return;
        }
        programFile.getUnresolvedAnnAttrValues().forEach(a -> {
            PackageInfo packageInfo = programFile.getPackageInfo(a.getConstPkg());
            LocalVariableAttributeInfo localVariableAttributeInfo = (LocalVariableAttributeInfo) packageInfo
                    .getAttributeInfo(AttributeInfo.Kind.LOCAL_VARIABLES_ATTRIBUTE);

            LocalVariableInfo localVariableInfo = localVariableAttributeInfo.getLocalVarialbeDetails(a.getConstName());

            switch (localVariableInfo.getVariableType().getTag()) {
                case TypeTags.BOOLEAN_TAG:
                    a.setBooleanValue(programFile.getGlobalMemoryBlock().getBooleanField(localVariableInfo
                            .getVariableIndex()) == 1 ? true : false);
                    break;
                case TypeTags.INT_TAG:
                    a.setIntValue(programFile.getGlobalMemoryBlock().getIntField(localVariableInfo
                            .getVariableIndex()));
                    break;
                case TypeTags.FLOAT_TAG:
                    a.setFloatValue(programFile.getGlobalMemoryBlock().getFloatField(localVariableInfo
                            .getVariableIndex()));
                    break;
                case TypeTags.STRING_TAG:
                    a.setStringValue(programFile.getGlobalMemoryBlock().getStringField(localVariableInfo
                            .getVariableIndex()));
                    break;
            }
        });
        programFile.setUnresolvedAnnAttrValues(null);
    }
    
}
