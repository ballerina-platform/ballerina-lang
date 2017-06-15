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
package org.ballerinalang;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVM;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.BLangVMWorkers;
import org.ballerinalang.bre.bvm.ControlStackNew;
import org.ballerinalang.bre.bvm.DebuggerExecutor;
import org.ballerinalang.bre.nonblocking.ModeResolver;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.services.dispatchers.DispatcherRegistry;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.codegen.WorkerInfo;
import org.ballerinalang.util.debugger.DebugInfoHolder;
import org.ballerinalang.util.debugger.VMDebugManager;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;

/**
 * {@code BLangProgramRunner} runs main and service programs.
 *
 * @since 0.8.0
 */
public class BLangProgramRunner {

    public void startServices(ProgramFile programFile) {
        String[] servicePackageNameList = programFile.getServicePackageNameList();
        if (servicePackageNameList.length == 0) {
            throw new BallerinaException("no service found in '" + programFile.getProgramFilePath() + "'");
        }

        // This is required to invoke package/service init functions;
        Context bContext = new Context(programFile);
        bContext.initFunction = true;

        int serviceCount = 0;
        for (String packageName : servicePackageNameList) {
            PackageInfo packageInfo = programFile.getPackageInfo(packageName);

            // Invoke package init function
            BLangFunctions.invokeFunction(programFile, packageInfo, packageInfo.getInitFunctionInfo(), bContext);
            if (bContext.getError() != null) {
                String stackTraceStr = BLangVMErrors.getPrintableStackTrace(bContext.getError());
                throw new BLangRuntimeException("error: " + stackTraceStr);
            }

            for (ServiceInfo serviceInfo : packageInfo.getServiceInfoList()) {
                // Invoke service init function
                BLangFunctions.invokeFunction(programFile, packageInfo,
                        serviceInfo.getInitFunctionInfo(), bContext);
                if (bContext.getError() != null) {
                    String stackTraceStr = BLangVMErrors.getPrintableStackTrace(bContext.getError());
                    throw new BLangRuntimeException("error: " + stackTraceStr);
                }

                // Deploy service
                DispatcherRegistry.getInstance().getServiceDispatchers().forEach((protocol, dispatcher) ->
                        dispatcher.serviceRegistered(serviceInfo));
                serviceCount++;
            }
        }

        if (serviceCount == 0) {
            throw new BallerinaException("no service found in '" + programFile.getProgramFilePath() + "'");
        }

        if (ModeResolver.getInstance().isDebugEnabled()) {
            VMDebugManager debugManager = VMDebugManager.getInstance();
            // This will start the websocket server.
            debugManager.serviceInit();
            debugManager.setDebugEnagled(true);
        }
    }

    public void runMain(ProgramFile programFile, String[] args) {
        Context bContext = new Context(programFile);
        // Non blocking is not support in the main program flow..
        bContext.initFunction = true;

        ControlStackNew controlStackNew = bContext.getControlStackNew();
        String mainPkgName = programFile.getMainPackageName();

        PackageInfo mainPkgInfo = programFile.getPackageInfo(mainPkgName);
        if (mainPkgInfo == null) {
            throw new BallerinaException("cannot find main function in '" + programFile.getProgramFilePath() + "'");
        }

        // Invoke package init function
        FunctionInfo mainFuncInfo = getMainFunction(mainPkgInfo);
        BLangFunctions.invokeFunction(programFile, mainPkgInfo, mainPkgInfo.getInitFunctionInfo(), bContext);
        if (bContext.getError() != null) {
            String stackTraceStr = BLangVMErrors.getPrintableStackTrace(bContext.getError());
            throw new BLangRuntimeException("error: " + stackTraceStr);
        }

        // Prepare main function arguments
        BStringArray arrayArgs = new BStringArray();
        for (int i = 0; i < args.length; i++) {
            arrayArgs.add(i, args[i]);
        }

        WorkerInfo defaultWorkerInfo = mainFuncInfo.getDefaultWorkerInfo();

        // Execute workers
        org.ballerinalang.bre.bvm.StackFrame callerSF = new org.ballerinalang.bre.bvm.StackFrame(mainFuncInfo,
                defaultWorkerInfo, -1, new int[0]);
        callerSF.getRefRegs()[0] = arrayArgs;
        int[] argRegs = {0};
        BLangVMWorkers.invoke(programFile, mainFuncInfo, callerSF, argRegs);

        org.ballerinalang.bre.bvm.StackFrame stackFrame = new org.ballerinalang.bre.bvm.StackFrame(mainFuncInfo,
                defaultWorkerInfo, -1, new int[0]);
        stackFrame.getRefLocalVars()[0] = arrayArgs;
        controlStackNew.pushFrame(stackFrame);

        BLangVM bLangVM = new BLangVM(programFile);
        bContext.setStartIP(defaultWorkerInfo.getCodeAttributeInfo().getCodeAddrs());
        if (ModeResolver.getInstance().isDebugEnabled()) {
            bContext.setDebugInfoHolder(new DebugInfoHolder());
            DebuggerExecutor debuggerExecutor = new DebuggerExecutor(programFile, bContext);
            bContext.setDebugEnabled(true);
            VMDebugManager debugManager = VMDebugManager.getInstance();
            // This will start the websocket server.
            debugManager.mainInit(debuggerExecutor, bContext);
            debugManager.waitTillClientConnect();
            debugManager.holdON();
        } else {
            bLangVM.run(bContext);
        }

        if (bContext.getError() != null) {
            String stackTraceStr = BLangVMErrors.getPrintableStackTrace(bContext.getError());
            throw new BLangRuntimeException("error: " + stackTraceStr);
        }
    }

    private FunctionInfo getMainFunction(PackageInfo mainPkgInfo) {
        String errorMsg = "cannot find main function in '" +
                mainPkgInfo.getProgramFile().getProgramFilePath() + "'";

        FunctionInfo mainFuncInfo = mainPkgInfo.getFunctionInfo("main");
        if (mainFuncInfo == null) {
            throw new BallerinaException(errorMsg);
        }

        BType[] paramTypes = mainFuncInfo.getParamTypes();
        BType[] retParamTypes = mainFuncInfo.getRetParamTypes();
        BArrayType argsType = new BArrayType(BTypes.typeString);
        if (paramTypes.length != 1 || !paramTypes[0].equals(argsType) || retParamTypes.length != 0) {
            throw new BallerinaException(errorMsg);
        }

        return mainFuncInfo;
    }
}
