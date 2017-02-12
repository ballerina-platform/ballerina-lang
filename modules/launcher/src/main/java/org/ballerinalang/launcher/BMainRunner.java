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
package org.ballerinalang.launcher;

import org.wso2.ballerina.core.debugger.DebugManager;
import org.wso2.ballerina.core.interpreter.BLangExecutor;
import org.wso2.ballerina.core.interpreter.CallableUnitInfo;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.RuntimeEnvironment;
import org.wso2.ballerina.core.interpreter.StackFrame;
import org.wso2.ballerina.core.interpreter.StackVarLocation;
import org.wso2.ballerina.core.interpreter.nonblocking.BLangNonBlockingExecutor;
import org.wso2.ballerina.core.interpreter.nonblocking.ModeResolver;
import org.wso2.ballerina.core.interpreter.nonblocking.debugger.BLangExecutionDebugger;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.NodeLocation;
import org.wso2.ballerina.core.model.ParameterDef;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.builder.BLangExecutionFlowBuilder;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.nodes.StartNode;
import org.wso2.ballerina.core.model.types.BTypes;
import org.wso2.ballerina.core.model.values.BArray;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.connectors.BallerinaConnectorManager;
import org.wso2.ballerina.core.runtime.MessageProcessor;
import org.wso2.ballerina.core.runtime.errors.handler.ErrorHandlerUtils;

import java.nio.file.Path;
import java.util.List;

/**
 * Executes the main function of a Ballerina program
 *
 * @since 0.8.0
 */
class BMainRunner {

    static void runMain(Path sourceFilePath, List<String> args) {
        BallerinaFile bFile = LauncherUtils.buildLangModel(sourceFilePath);

        // Load Client Connectors
        BallerinaConnectorManager.getInstance().initializeClientConnectors(new MessageProcessor());

        // Check whether there is a main function
        BallerinaFunction function = (BallerinaFunction) bFile.getMainFunction();
        if (function == null) {
            String pkgString = (bFile.getPackagePath() != null) ? "in package " + bFile.getPackagePath() : "";
            pkgString = (pkgString.equals("")) ? "in file '" + LauncherUtils.getFileName(sourceFilePath) + "'" : "";
            String errorMsg = "ballerina: main method not found " + pkgString + "";
            throw LauncherUtils.createLauncherException(errorMsg);
        }

        execute(bFile, args);
        Runtime.getRuntime().exit(0);
    }

    private static void execute(BallerinaFile balFile, List<String> args) {
        Context bContext = new Context();
        try {
            SymbolName argsName;
            BallerinaFunction mainFun = (BallerinaFunction) balFile.getMainFunction();
            NodeLocation mainFuncLocation = mainFun.getNodeLocation();
            ParameterDef[] parameterDefs = mainFun.getParameterDefs();
            argsName = parameterDefs[0].getSymbolName();

            Expression[] exprs = new Expression[1];
            VariableRefExpr variableRefExpr = new VariableRefExpr(mainFuncLocation, argsName);
            variableRefExpr.setVariableDef(parameterDefs[0]);
            StackVarLocation location = new StackVarLocation(0);
            variableRefExpr.setMemoryLocation(location);
            variableRefExpr.setType(BTypes.typeString);
            exprs[0] = variableRefExpr;

            BArray<BString> arrayArgs = new BArray<>(BString.class);
            for (int i = 0; i < args.size(); i++) {
                arrayArgs.add(i, new BString(args.get(i)));
            }

            BValue[] argValues = {arrayArgs};

            // 3) Create a function invocation expression
            FunctionInvocationExpr funcIExpr = new FunctionInvocationExpr(mainFuncLocation,
                    mainFun.getName(), null, mainFun.getPackagePath(), exprs);
            funcIExpr.setOffset(1);
            funcIExpr.setCallableUnit(mainFun);
            BLangExecutionFlowBuilder flowBuilder = new BLangExecutionFlowBuilder();
            funcIExpr.setParent(new StartNode(StartNode.Originator.MAIN_FUNCTION));
            flowBuilder.visit(funcIExpr);

            CallableUnitInfo functionInfo = new CallableUnitInfo(funcIExpr.getName(),
                    funcIExpr.getPackagePath(), mainFuncLocation);

            BValue[] tempValues = new BValue[flowBuilder.getCurrentTempStackSize()];

            StackFrame currentStackFrame = new StackFrame(argValues, new BValue[0], tempValues, functionInfo);
            bContext.getControlStack().pushFrame(currentStackFrame);

            RuntimeEnvironment runtimeEnv = RuntimeEnvironment.get(balFile);
            if (ModeResolver.getInstance().isNonblockingEnabled()) {
                if (ModeResolver.getInstance().isDebugEnabled()) {
                    DebugManager debugManager = DebugManager.getInstance();
                    // This will start the websocket server.
                    debugManager.init();
                    debugManager.waitTillClientConnect();
                    BLangExecutionDebugger debugger = new BLangExecutionDebugger(runtimeEnv, bContext);
                    debugManager.setDebugger(debugger);
                    bContext.setExecutor(debugger);
                    debugger.execute(funcIExpr);
                    debugManager.holdON();
                } else {
                    BLangNonBlockingExecutor executor = new BLangNonBlockingExecutor(runtimeEnv, bContext);
                    bContext.setExecutor(executor);
                    executor.execute(funcIExpr);
                }
            } else {
                BLangExecutor executor = new BLangExecutor(runtimeEnv, bContext);
                funcIExpr.executeMultiReturn(executor);
            }
            bContext.getControlStack().popFrame();
        } catch (Throwable ex) {
            String errorMsg = ErrorHandlerUtils.getErrorMessage(ex);
            String stacktrace = ErrorHandlerUtils.getMainFuncStackTrace(bContext, ex);
            throw LauncherUtils.createLauncherException(errorMsg + "\n" + stacktrace);
        }
    }
}
