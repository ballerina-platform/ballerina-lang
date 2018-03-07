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
import org.ballerinalang.bre.bvm.ControlStack;
import org.ballerinalang.bre.bvm.StackFrame;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.codegen.WorkerInfo;
import org.ballerinalang.util.debugger.DebugContext;
import org.ballerinalang.util.debugger.Debugger;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;

/**
 * This class contains utilities to execute Ballerina main and service programs.
 *
 * @since 0.8.0
 */
public class BLangProgramRunner {

    public static void runService(ProgramFile programFile) {
        if (!programFile.isServiceEPAvailable()) {
            throw new BallerinaException("no services found in '" + programFile.getProgramFilePath() + "'");
        }

        // Get the service package
        PackageInfo servicesPackage = programFile.getEntryPackage();
        if (servicesPackage == null) {
            throw new BallerinaException("no services found in '" + programFile.getProgramFilePath() + "'");
        }

        // This is required to invoke package/service init functions;
        Context bContext = new Context(programFile);

        Debugger debugger = new Debugger(programFile);
        initDebugger(bContext, debugger);

        // Invoke package init function
        BLangFunctions.invokePackageInitFunction(programFile, servicesPackage.getInitFunctionInfo(), bContext);

        deployTransactionCoordinatorServices(programFile, bContext);

        ServiceInfo[] declaredServices = servicesPackage.getServiceInfoEntries();
        int serviceCount = 0;
        for (ServiceInfo serviceInfo : declaredServices) {
            deployService(programFile, bContext, serviceInfo);
            serviceCount++;
        }

        if (serviceCount == 0) {
            throw new BallerinaException("no services found in '" + programFile.getProgramFilePath() + "'");
        }
    }

    private static void deployTransactionCoordinatorServices(ProgramFile programFile, Context bContext) {
        PackageInfo coordinatorPkgInfo = programFile.getPackageInfo("ballerina.transactions.coordinator");
        ServiceInfo[] coordinatorServices;
        if (coordinatorPkgInfo != null) {
            coordinatorPkgInfo.setProgramFile(programFile);
            coordinatorServices = coordinatorPkgInfo.getServiceInfoEntries();
            if (coordinatorServices != null) {
                for (ServiceInfo coordinatorService : coordinatorServices) {
                    deployService(programFile, bContext, coordinatorService);
                }
            }
        }
    }

    private static void deployService(ProgramFile programFile, Context bContext, ServiceInfo serviceInfo) {
        // Invoke service init function
        //TODO check this to pass a Service
        bContext.setServiceInfo(serviceInfo);
        BLangFunctions.invokeFunction(programFile, serviceInfo.getInitFunctionInfo(), bContext);
        if (bContext.getError() != null) {
            String stackTraceStr = BLangVMErrors.getPrintableStackTrace(bContext.getError());
            throw new BLangRuntimeException("error in deploying service: " + stackTraceStr);
        }

        // Deploy service
        programFile.getServerConnectorRegistry().registerService(serviceInfo);
    }

    public static void runMain(ProgramFile programFile, String[] args) {
        if (!programFile.isMainEPAvailable()) {
            throw new BallerinaException("main function not found in  '" + programFile.getProgramFilePath() + "'");
        }

        // Get the main entry package
        PackageInfo mainPkgInfo = programFile.getEntryPackage();
        if (mainPkgInfo == null) {
            throw new BallerinaException("main function not found in  '" + programFile.getProgramFilePath() + "'");
        }

        // Non blocking is not supported in the main program flow..
        Context bContext = new Context(programFile);

        Debugger debugger = new Debugger(programFile);
        initDebugger(bContext, debugger);

        // Invoke package init function
        FunctionInfo mainFuncInfo = getMainFunction(mainPkgInfo);
        BLangFunctions.invokePackageInitFunction(programFile, mainPkgInfo.getInitFunctionInfo(), bContext);

        // Prepare main function arguments
        BStringArray arrayArgs = new BStringArray();
        for (int i = 0; i < args.length; i++) {
            arrayArgs.add(i, args[i]);
        }

        WorkerInfo defaultWorkerInfo = mainFuncInfo.getDefaultWorkerInfo();

        // Execute workers
        StackFrame callerSF = new StackFrame(mainPkgInfo, -1, new int[0]);
        callerSF.setRefRegs(new BRefType[1]);
        callerSF.getRefRegs()[0] = arrayArgs;

        StackFrame stackFrame = new StackFrame(mainFuncInfo, defaultWorkerInfo, -1, new int[0]);
        stackFrame.getRefRegs()[0] = arrayArgs;
        ControlStack controlStack = bContext.getControlStack();
        controlStack.pushFrame(stackFrame);
        bContext.startTrackWorker();
        bContext.setStartIP(defaultWorkerInfo.getCodeAttributeInfo().getCodeAddrs());

        BLangVM bLangVM = new BLangVM(programFile);
        bLangVM.run(bContext);
        bContext.await();
        if (debugger.isDebugEnabled()) {
            debugger.notifyExit();
        }
        if (bContext.getError() != null) {
            String stackTraceStr = BLangVMErrors.getPrintableStackTrace(bContext.getError());
            throw new BLangRuntimeException("error: " + stackTraceStr);
        }
    }

    private static void initDebugger(Context bContext, Debugger debugger) {
        bContext.getProgramFile().setDebugger(debugger);
        if (debugger.isDebugEnabled()) {
            DebugContext debugContext = new DebugContext();
            bContext.setDebugContext(debugContext);
            debugger.init();
            debugger.addDebugContextAndWait(debugContext);
        }
    }

    public static FunctionInfo getMainFunction(PackageInfo mainPkgInfo) {
        String errorMsg = "main function not found in  '" +
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
