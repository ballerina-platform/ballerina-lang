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

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangScheduler;
import org.ballerinalang.bre.bvm.BLangVM;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.CPU;
import org.ballerinalang.bre.bvm.ControlStack;
import org.ballerinalang.bre.bvm.StackFrame;
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
import org.ballerinalang.util.BLangConstants;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.LocalVariableInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.WorkerInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfo;
import org.ballerinalang.util.codegen.attributes.CodeAttributeInfo;
import org.ballerinalang.util.codegen.attributes.LocalVariableAttributeInfo;
import org.ballerinalang.util.exceptions.BLangRuntimeException;

import java.util.Arrays;
import java.util.Map;

/**
 * This class contains helper methods to invoke Ballerina functions.
 *
 * @since 0.8.0
 */
public class BLangFunctions {

    private BLangFunctions() {
    }

    /**
     * Invokes a Ballerina function defined in the given language model.
     *
     * @param bLangProgram parsed, analyzed and linked object model
     * @param functionName name of the function to be invoked
     * @return return values from the function
     */
    public static BValue[] invokeNew(ProgramFile bLangProgram, String functionName) {
        BValue[] args = {};
        //return invokeNew(bLangProgram, ".", functionName, args, new Context(bLangProgram));
        return null;
    }

    public static BValue[] invokeNew(ProgramFile bLangProgram, String packageName, String functionName) {
        BValue[] args = {};
        //return invokeNew(bLangProgram, packageName, functionName, args, new Context(bLangProgram));
        return null;
    }

    public static BValue[] invokeNew(ProgramFile bLangProgram, String functionName, BValue[] args, Context bContext) {
        return invokeNew(bLangProgram, ".", functionName, args, bContext);
    }

    /**
     * Invokes a Ballerina function defined in the given language model.
     *
     * @param bLangProgram parsed, analyzed and linked object model
     * @param functionName name of the function to be invoked
     * @param args         arguments for the function
     * @return return values from the function
     */
    public static BValue[] invokeNew(ProgramFile bLangProgram, String functionName, BValue[] args) {
        //return invokeNew(bLangProgram, ".", functionName, args, new Context(bLangProgram));
        return null;
    }

    public static BValue[] invokeNew(ProgramFile bLangProgram, String packageName, String functionName, BValue[] args) {
        //return invokeNew(bLangProgram, packageName, functionName, args, new Context(bLangProgram));
        return null;
    }

    public static BValue[] invokeNew(ProgramFile bLangProgram, String packageName, String functionName,
                                     BValue[] args, Context context) {
        PackageInfo packageInfo = bLangProgram.getPackageInfo(packageName);
        FunctionInfo functionInfo = packageInfo.getFunctionInfo(functionName);

        if (functionInfo == null) {
            throw new RuntimeException("Function '" + functionName + "' is not defined");
        }

        if (functionInfo.getParamTypes().length != args.length) {
            throw new RuntimeException("Size of input argument arrays is not equal to size of function parameters");
        }

        invokePackageInitFunction2(bLangProgram, packageInfo.getInitFunctionInfo(), context);
        return invokeFunction(bLangProgram, functionInfo, args, context);
    }

    public static BValue[] invokeFunction(ProgramFile bLangProgram, FunctionInfo functionInfo, BValue[] args,
                                          Context context) {

        PackageInfo packageInfo = functionInfo.getPackageInfo();
        ControlStack controlStack = context.getControlStack();
        // First Create the caller's stack frame. This frame contains zero local variables, but it contains enough
        // registers to hold function arguments as well as return values from the callee.
        StackFrame callerSF = new StackFrame(packageInfo, -1, new int[0]);
        controlStack.pushFrame(callerSF);
        // TODO Create registers to hold return values

        int longRegCount = 0;
        int doubleRegCount = 0;
        int stringRegCount = 0;
        int intRegCount = 0;
        int refRegCount = 0;
        int byteRegCount = 0;

        // Calculate registers to store return values
        BType[] retTypes = functionInfo.getRetParamTypes();
        int[] retRegs = new int[retTypes.length];
        for (int i = 0; i < retTypes.length; i++) {
            BType retType = retTypes[i];
            switch (retType.getTag()) {
                case TypeTags.INT_TAG:
                    retRegs[i] = longRegCount++;
                    break;
                case TypeTags.FLOAT_TAG:
                    retRegs[i] = doubleRegCount++;
                    break;
                case TypeTags.STRING_TAG:
                    retRegs[i] = stringRegCount++;
                    break;
                case TypeTags.BOOLEAN_TAG:
                    retRegs[i] = intRegCount++;
                    break;
                case TypeTags.BLOB_TAG:
                    retRegs[i] = byteRegCount++;
                    break;
                default:
                    retRegs[i] = refRegCount++;
                    break;
            }
        }

        callerSF.setLongRegs(new long[longRegCount]);
        callerSF.setDoubleRegs(new double[doubleRegCount]);
        callerSF.setStringRegs(new String[stringRegCount]);
        callerSF.setIntRegs(new int[intRegCount]);
        callerSF.setRefRegs(new BRefType[refRegCount]);
        callerSF.setByteRegs(new byte[byteRegCount][]);

        // Now create callee's stackframe
        WorkerInfo defaultWorkerInfo = functionInfo.getDefaultWorkerInfo();
        SynchronizedStackFrame calleeSF = new SynchronizedStackFrame(functionInfo, defaultWorkerInfo, -1, retRegs);
        controlStack.pushFrame(calleeSF);

        int longParamCount = 0;
        int doubleParamCount = 0;
        int stringParamCount = 0;
        int intParamCount = 0;
        int refParamCount = 0;
        int byteParamCount = 0;

        CodeAttributeInfo codeAttribInfo = defaultWorkerInfo.getCodeAttributeInfo();

        long[] longRegs = new long[codeAttribInfo.getMaxLongRegs()];
        double[] doubleRegs = new double[codeAttribInfo.getMaxDoubleRegs()];
        String[] stringRegs = new String[codeAttribInfo.getMaxStringRegs()];
        // Setting the zero values for strings
        Arrays.fill(stringRegs, BLangConstants.STRING_NULL_VALUE);

        int[] intRegs = new int[codeAttribInfo.getMaxIntRegs()];
        byte[][] byteRegs = new byte[codeAttribInfo.getMaxByteRegs()][];
        BRefType[] refRegs = new BRefType[codeAttribInfo.getMaxRefRegs()];

        for (int i = 0; i < functionInfo.getParamTypes().length; i++) {
            BType argType = functionInfo.getParamTypes()[i];
            switch (argType.getTag()) {
                case TypeTags.INT_TAG:
                    longRegs[longParamCount] = ((BInteger) args[i]).intValue();
                    longParamCount++;
                    break;
                case TypeTags.FLOAT_TAG:
                    doubleRegs[doubleParamCount] = ((BFloat) args[i]).floatValue();
                    doubleParamCount++;
                    break;
                case TypeTags.STRING_TAG:
                    stringRegs[stringParamCount] = args[i].stringValue();
                    stringParamCount++;
                    break;
                case TypeTags.BOOLEAN_TAG:
                    intRegs[intParamCount] = ((BBoolean) args[i]).booleanValue() ? 1 : 0;
                    intParamCount++;
                    break;
                case TypeTags.BLOB_TAG:
                    byteRegs[byteParamCount] = ((BBlob) args[i]).blobValue();
                    byteParamCount++;
                    break;
                default:
                    refRegs[refParamCount] = (BRefType) args[i];
                    refParamCount++;
                    break;
            }
        }

        calleeSF.setLongRegs(longRegs);
        calleeSF.setDoubleRegs(doubleRegs);
        calleeSF.setStringRegs(stringRegs);
        calleeSF.setIntRegs(intRegs);
        calleeSF.setByteRegs(byteRegs);
        calleeSF.setRefRegs(refRegs);
        
        BLangVM bLangVM = new BLangVM(bLangProgram);
        context.startTrackWorker();
        context.setStartIP(codeAttribInfo.getCodeAddrs());
        bLangVM.run(context);

        calleeSF.await();

        if (context.getError() != null) {
            String stackTraceStr = BLangVMErrors.getPrintableStackTrace(context.getError());
            throw new BLangRuntimeException("error: " + stackTraceStr);
        }

        longRegCount = 0;
        doubleRegCount = 0;
        stringRegCount = 0;
        intRegCount = 0;
        refRegCount = 0;
        byteRegCount = 0;
        BValue[] returnValues = new BValue[retTypes.length];
        for (int i = 0; i < returnValues.length; i++) {
            BType retType = retTypes[i];
            switch (retType.getTag()) {
                case TypeTags.INT_TAG:
                    returnValues[i] = new BInteger(callerSF.getLongRegs()[longRegCount++]);
                    break;
                case TypeTags.FLOAT_TAG:
                    returnValues[i] = new BFloat(callerSF.getDoubleRegs()[doubleRegCount++]);
                    break;
                case TypeTags.STRING_TAG:
                    returnValues[i] = new BString(callerSF.getStringRegs()[stringRegCount++]);
                    break;
                case TypeTags.BOOLEAN_TAG:
                    boolean boolValue = callerSF.getIntRegs()[intRegCount++] == 1;
                    returnValues[i] = new BBoolean(boolValue);
                    break;
                case TypeTags.BLOB_TAG:
                    returnValues[i] = new BBlob(callerSF.getByteRegs()[byteRegCount++]);
                    break;
                default:
                    returnValues[i] = callerSF.getRefRegs()[refRegCount++];
                    break;
            }
        }

        return returnValues;
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

    public static void invokeFunction(ProgramFile programFile, FunctionInfo functionInfo, 
            WorkerExecutionContext parentCtx) {
        invokeFunction(programFile, functionInfo, parentCtx, new int[0], new int[0], false);
    }
    
    public static BValue[] invokeFunction(ProgramFile programFile, CallableUnitInfo callableUnitInfo, BValue[] args) {
        WorkerExecutionContext parentCtx = new WorkerExecutionContext();
        int[] argRegs = populateArgData(parentCtx, callableUnitInfo, args);
        int[] retRegs = createReturnData(parentCtx, callableUnitInfo);
        invokeFunction(programFile, callableUnitInfo, parentCtx, argRegs, retRegs, true);
        return populateReturnData(parentCtx, callableUnitInfo);
    }
    
    private static BValue[] populateReturnData(WorkerExecutionContext ctx, CallableUnitInfo callableUnitInfo) {
        int longRegCount = 0;
        int doubleRegCount = 0;
        int stringRegCount = 0;
        int intRegCount = 0;
        int refRegCount = 0;
        int byteRegCount = 0;
        WorkerData data = ctx.workerResult;
        BType[] retTypes = callableUnitInfo.getRetParamTypes();
        BValue[] returnValues = new BValue[retTypes.length];
        for (int i = 0; i < returnValues.length; i++) {
            BType retType = retTypes[i];
            switch (retType.getTag()) {
            case TypeTags.INT_TAG:
                returnValues[i] = new BInteger(data.longRegs[longRegCount++]);
                break;
            case TypeTags.FLOAT_TAG:
                returnValues[i] = new BFloat(data.doubleRegs[doubleRegCount++]);
                break;
            case TypeTags.STRING_TAG:
                returnValues[i] = new BString(data.stringRegs[stringRegCount++]);
                break;
            case TypeTags.BOOLEAN_TAG:
                boolean boolValue = data.intRegs[intRegCount++] == 1;
                returnValues[i] = new BBoolean(boolValue);
                break;
            case TypeTags.BLOB_TAG:
                returnValues[i] = new BBlob(data.byteRegs[byteRegCount++]);
                break;
            default:
                returnValues[i] = data.refRegs[refRegCount++];
                break;
            }
        }
        return returnValues;
    }
    
    private static int[] createReturnData(WorkerExecutionContext ctx, CallableUnitInfo callableUnitInfo) {
        WorkerDataIndex wdi = callableUnitInfo.retWorkerIndex;
        ctx.workerResult = createWorkerData(wdi);
        return wdi.retRegs;
    }
    
    @SuppressWarnings("rawtypes")
    private static int[] populateArgData(WorkerExecutionContext ctx, 
            CallableUnitInfo callableUnitInfo, BValue[] args) {
        WorkerDataIndex wdi = callableUnitInfo.paramWorkerIndex;
        WorkerData local = createWorkerData(wdi);
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
        return wdi.retRegs;
    }
    
    public static WorkerExecutionContext invokeFunction(ProgramFile programFile, CallableUnitInfo callableUnitInfo, 
            WorkerExecutionContext parentCtx, int[] argRegs, int[] retRegs, boolean waitForResponse) {
        InvocableWorkerResponseContext respCtx = new InvocableWorkerResponseContext(
                callableUnitInfo.getRetParamTypes(), waitForResponse);
        respCtx.updateParentWorkerResultLocation(retRegs);
        WorkerDataIndex wdi = callableUnitInfo.retWorkerIndex;
        Map<String, Object> globalProps = parentCtx.globalProps;
        BLangScheduler.switchToWaitForResponse(parentCtx);
        WorkerExecutionContext runInCallerCtx = null;
        WorkerInfo[] workerInfos = listWorkerInfos(callableUnitInfo);
        for (int i = 0; i < workerInfos.length; i++) {
            if (i == 0) {
                runInCallerCtx = executeWorker(respCtx, parentCtx, argRegs, callableUnitInfo, 
                        workerInfos[i], wdi, globalProps, true);
            } else {
                executeWorker(respCtx, parentCtx, argRegs, callableUnitInfo, workerInfos[i], wdi, globalProps, false);
            }
        }
        if (waitForResponse) {
            if (runInCallerCtx != null) {
                CPU.exec(runInCallerCtx);
            }
            respCtx.waitForResponse();
        }
        return runInCallerCtx;
    }
    
    private static WorkerExecutionContext executeWorker(WorkerResponseContext respCtx, WorkerExecutionContext parentCtx, int[] argRegs,
            CallableUnitInfo callableUnitInfo, WorkerInfo workerInfo, WorkerDataIndex wdi, 
            Map<String, Object> globalProps, boolean runInCaller) {
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
    
    public static void invokeFunction2(ProgramFile programFile, FunctionInfo initFuncInfo, Context context) {
        WorkerInfo defaultWorker = initFuncInfo.getDefaultWorkerInfo();
        SynchronizedStackFrame stackFrame = new SynchronizedStackFrame(initFuncInfo, defaultWorker, -1, new int[0]);
        context.getControlStack().pushFrame(stackFrame);

        BLangVM bLangVM = new BLangVM(programFile);
        context.startTrackWorker();
        context.setStartIP(defaultWorker.getCodeAttributeInfo().getCodeAddrs());
        bLangVM.run(context);
        stackFrame.await();
        context.resetWorkerContextFlow();
    }

    public static void invokePackageInitFunction(ProgramFile programFile, FunctionInfo initFuncInfo, 
            WorkerExecutionContext context) {
        invokeFunction(programFile, initFuncInfo, context, new int[0], new int[0], true);
        if (context.getError() != null) {
            String stackTraceStr = BLangVMErrors.getPrintableStackTrace(context.getError());
            throw new BLangRuntimeException("error: " + stackTraceStr);
        }
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
    
    public static void invokePackageInitFunction2(ProgramFile programFile, FunctionInfo initFuncInfo, Context context) {
        invokeFunction2(programFile, initFuncInfo, context);
        if (context.getError() != null) {
            String stackTraceStr = BLangVMErrors.getPrintableStackTrace(context.getError());
            throw new BLangRuntimeException("error: " + stackTraceStr);
        }
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
