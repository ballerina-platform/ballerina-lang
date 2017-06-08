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

import org.ballerinalang.bre.BLangExecutor;
import org.ballerinalang.bre.CallableUnitInfo;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.RuntimeEnvironment;
import org.ballerinalang.bre.StackFrame;
import org.ballerinalang.bre.StackVarLocation;
import org.ballerinalang.bre.bvm.BLangVM;
import org.ballerinalang.bre.bvm.ControlStackNew;
import org.ballerinalang.bre.bvm.DebuggerExecutor;
import org.ballerinalang.bre.nonblocking.ModeResolver;
import org.ballerinalang.bre.nonblocking.debugger.BLangExecutionDebugger;
import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.BallerinaFunction;
import org.ballerinalang.model.Service;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.Worker;
import org.ballerinalang.model.expressions.Expression;
import org.ballerinalang.model.expressions.VariableRefExpr;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.ballerinalang.services.dispatchers.DispatcherRegistry;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.codegen.WorkerInfo;
import org.ballerinalang.util.debugger.DebugInfoHolder;
import org.ballerinalang.util.debugger.DebugManager;
import org.ballerinalang.util.debugger.VMDebugManager;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;

import java.util.AbstractMap;

/**
 * {@code BLangProgramRunner} runs main and service programs.
 *
 * @since 0.8.0
 */
public class BLangProgramRunner {

    @Deprecated
    public void startServices(BLangProgram bLangProgram) {
        BLangPackage[] servicePackages = bLangProgram.getServicePackages();
        if (servicePackages.length == 0) {
            throw new RuntimeException("no service(s) found in '" + bLangProgram.getProgramFilePath() + "'");
        }

        int serviceCount = 0;
        for (BLangPackage servicePackage : servicePackages) {
            for (Service service : servicePackage.getServices()) {
                serviceCount++;
                service.setBLangProgram(bLangProgram);
                DispatcherRegistry.getInstance().getServiceDispatchers().forEach((protocol, dispatcher) ->
                        dispatcher.serviceRegistered(service));
            }
        }

        if (serviceCount == 0) {
            throw new RuntimeException("no service(s) found in '" + bLangProgram.getProgramFilePath() + "'");
        }
        if (ModeResolver.getInstance().isDebugEnabled()) {
            DebugManager debugManager = DebugManager.getInstance();
            // This will start the websocket server.
            debugManager.init();
        }

        // Create a runtime environment for this Ballerina application
        RuntimeEnvironment runtimeEnv = RuntimeEnvironment.get(bLangProgram);
        bLangProgram.setRuntimeEnvironment(runtimeEnv);
    }

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

            for (ServiceInfo serviceInfo : packageInfo.getServiceInfoList()) {
                // Invoke service init function
                BLangFunctions.invokeFunction(programFile, packageInfo,
                        serviceInfo.getInitFunctionInfo(), bContext);

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
        }
    }

    public void runMain(ProgramFile programFile, String[] args) {
        Context bContext = new Context(programFile);
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

        // Prepare main function arguments
        BStringArray arrayArgs = new BStringArray();
        for (int i = 0; i < args.length; i++) {
            arrayArgs.add(i, args[i]);
        }

        WorkerInfo defaultWorkerInfo = mainFuncInfo.getDefaultWorkerInfo();
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
    }

    @Deprecated
    public void runMain(BLangProgram bLangProgram, String[] args) {
        Context bContext = new Context();
        BallerinaFunction mainFunction = bLangProgram.getMainFunction();

        try {
            BValue[] argValues = new BValue[mainFunction.getStackFrameSize()];
            BValue[] cacheValues = new BValue[mainFunction.getTempStackFrameSize()];

            BArray<BString> arrayArgs = new BArray<>(BString.class);
            for (int i = 0; i < args.length; i++) {
                arrayArgs.add(i, new BString(args[i]));
            }

            argValues[0] = arrayArgs;

            CallableUnitInfo functionInfo = new CallableUnitInfo(mainFunction.getName(), mainFunction.getPackagePath(),
                    mainFunction.getNodeLocation());

            StackFrame stackFrame = new StackFrame(argValues, new BValue[0], cacheValues, functionInfo);
            bContext.getControlStack().pushFrame(stackFrame);

            // Invoke main function
            RuntimeEnvironment runtimeEnv = RuntimeEnvironment.get(bLangProgram);
            if (ModeResolver.getInstance().isDebugEnabled()) {
                stackFrame.variables.put(new SymbolName("args"), new AbstractMap.SimpleEntry<>(0, "Arg"));
                DebugManager debugManager = DebugManager.getInstance();
                // This will start the websocket server.
                debugManager.init();
                debugManager.waitTillClientConnect();
                BLangExecutionDebugger debugger = new BLangExecutionDebugger(runtimeEnv, bContext);
                debugManager.setDebugger(debugger);
//                debugger.setParentScope(mainFunction);
//                bContext.setExecutor(debugger);
//                debugger.continueExecution(mainFunction.getCallableUnitBody());
                debugManager.holdON();
            } else {
                BLangExecutor executor = new BLangExecutor(runtimeEnv, bContext);
                executor.setParentScope(mainFunction);

                if (mainFunction.getWorkers().length > 0) {
                    // TODO: Fix this properly.
                    Expression[] exprs = new Expression[args.length];
                    for (int i = 0; i < args.length; i++) {
                        VariableRefExpr variableRefExpr = new VariableRefExpr(mainFunction.getNodeLocation(), null,
                                new SymbolName("arg" + i));

                        variableRefExpr.setVariableDef(mainFunction.getParameterDefs()[i]);
                        StackVarLocation location = new StackVarLocation(i);
                        variableRefExpr.setMemoryLocation(location);
                        exprs[i] = variableRefExpr;
                    }
                    // Start the workers defined within the function
                    for (Worker worker : mainFunction.getWorkers()) {
                        executor.executeWorker(worker, exprs);
                    }
                }
                mainFunction.getCallableUnitBody().execute(executor);
                if (executor.isErrorThrown && executor.thrownError != null) {
                    String errorMsg = "uncaught error: " + executor.thrownError.getType().getName() + "{ msg : " +
                            executor.thrownError.getValue(0).stringValue() + "}";
                    throw new BallerinaException(errorMsg);
                }
            }
            bContext.getControlStack().popFrame();
        } catch (Throwable ex) {
            String errorMsg = ErrorHandlerUtils.getErrorMessage(ex);
            String stacktrace = ErrorHandlerUtils.getMainFuncStackTrace(bContext, ex);
            throw new BLangRuntimeException(errorMsg + "\n" + stacktrace);
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
