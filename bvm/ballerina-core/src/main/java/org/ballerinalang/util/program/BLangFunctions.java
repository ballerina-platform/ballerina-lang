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
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.bre.bvm.CallbackedInvocableWorkerResponseContext;
import org.ballerinalang.bre.bvm.InvocableWorkerResponseContext;
import org.ballerinalang.bre.bvm.WorkerData;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.bre.bvm.WorkerResponseContext;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.WorkerInfo;
import org.ballerinalang.util.exceptions.BLangRuntimeException;

import java.util.Map;

/**
 * This class contains helper methods to invoke Ballerina functions.
 *
 * @since 0.8.0
 */
public class BLangFunctions {

    private BLangFunctions() { }

    public static BValue[] invokeNew(ProgramFile bLangProgram, String packageName, String functionName, 
            BValue[] args) {
        return invokeNew(bLangProgram, packageName, functionName, args, new WorkerExecutionContext(bLangProgram));
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
    
    public static void invokeCallable(CallableUnitInfo callableUnitInfo, WorkerExecutionContext parentCtx) {
        invokeCallable(callableUnitInfo, parentCtx, new int[0], new int[0], false);
    }
    
    public static BValue[] invokeCallable(ProgramFile bLangProgram, CallableUnitInfo callableUnitInfo, 
            BValue[] args) {
        return invokeCallable(callableUnitInfo, new WorkerExecutionContext(bLangProgram), args);
    }
    
    public static BValue[] invokeCallable(CallableUnitInfo callableUnitInfo, WorkerExecutionContext parentCtx, 
            BValue[] args) {
        int[][] regs = BLangVMUtils.populateArgAndReturnData(parentCtx, callableUnitInfo, args);
        invokeCallable(callableUnitInfo, parentCtx, regs[0], regs[1], true);
        return BLangVMUtils.populateReturnData(parentCtx, callableUnitInfo, regs[1]);
    }
    
    public static void invokeCallable(CallableUnitInfo callableUnitInfo, WorkerExecutionContext parentCtx, 
            BValue[] args, CallableUnitCallback responseCallback) {
        int[][] regs = BLangVMUtils.populateArgAndReturnData(parentCtx, callableUnitInfo, args);
        invokeCallable(callableUnitInfo, parentCtx, regs[0], regs[1], responseCallback);
    }
    
    /**
     * This method does not short circuit the execution of the first worker to execute in the 
     * same calling thread, but rather executes all the workers in their own separate threads.
     * This is specifically useful in executing service resources, where the calling transport
     * threads shouldn't be blocked, but rather the worker threads should be used.
     */
    public static void invokeCallable(CallableUnitInfo callableUnitInfo, 
            WorkerExecutionContext parentCtx, int[] argRegs, int[] retRegs, 
            CallableUnitCallback responseCallback) {
        WorkerInfo[] workerInfos = listWorkerInfos(callableUnitInfo);
        InvocableWorkerResponseContext respCtx = new CallbackedInvocableWorkerResponseContext(
                callableUnitInfo.getRetParamTypes(), workerInfos.length, false, responseCallback);
        respCtx.updateTargetContextInfo(parentCtx, retRegs);
        WorkerDataIndex wdi = callableUnitInfo.retWorkerIndex;
        Map<String, Object> globalProps = parentCtx.globalProps;
        BLangScheduler.switchToWaitForResponse(parentCtx);
        for (int i = 0; i < workerInfos.length; i++) {
            executeWorker(respCtx, parentCtx, argRegs, callableUnitInfo, workerInfos[i], wdi, globalProps, false);
        }
    }
    
    public static WorkerExecutionContext invokeCallable(CallableUnitInfo callableUnitInfo, 
            WorkerExecutionContext parentCtx, int[] argRegs, int[] retRegs, boolean waitForResponse) {
        WorkerInfo[] workerInfos = listWorkerInfos(callableUnitInfo);
        InvocableWorkerResponseContext respCtx = new InvocableWorkerResponseContext(
                callableUnitInfo.getRetParamTypes(), 
                workerInfos.length, waitForResponse);
        respCtx.updateTargetContextInfo(parentCtx, retRegs);
        WorkerDataIndex wdi = callableUnitInfo.retWorkerIndex;
        Map<String, Object> globalProps = parentCtx.globalProps;
        BLangScheduler.switchToWaitForResponse(parentCtx);
        for (int i = 1; i < workerInfos.length; i++) {
            executeWorker(respCtx, parentCtx, argRegs, callableUnitInfo, workerInfos[i], wdi, globalProps, false);
        }
        WorkerExecutionContext runInCallerCtx = executeWorker(respCtx, parentCtx, argRegs, callableUnitInfo, 
                workerInfos[0], wdi, globalProps, true);
        if (waitForResponse) {
            CPU.exec(runInCallerCtx);
            respCtx.waitForResponse();

            // An error in the context at this point means an unhandled runtime error has propagated
            // all the way up to the entry point. Hence throw a {@link BLangRuntimeException} and
            // terminate the execution.
            BStruct error = runInCallerCtx.getError();
            if (error != null) {
                throw new BLangRuntimeException("error: " + BLangVMErrors.getPrintableStackTrace(error));
            }
        }
        return runInCallerCtx;
    }
    
    private static WorkerExecutionContext executeWorker(WorkerResponseContext respCtx, 
            WorkerExecutionContext parentCtx, int[] argRegs, CallableUnitInfo callableUnitInfo, 
            WorkerInfo workerInfo, WorkerDataIndex wdi, Map<String, Object> globalProps, boolean runInCaller) {
        WorkerData workerLocal = BLangVMUtils.createWorkerDataForLocal(workerInfo, parentCtx, argRegs,
                callableUnitInfo.getParamTypes());
        WorkerData workerResult = BLangVMUtils.createWorkerData(wdi);
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
        BLangVMUtils.processUnresolvedAnnAttrValues(programFile);
        programFile.setUnresolvedAnnAttrValues(null);
    }
}
