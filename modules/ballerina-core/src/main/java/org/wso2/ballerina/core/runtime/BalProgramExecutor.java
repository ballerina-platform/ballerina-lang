/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.core.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.interpreter.BLangExecutor;
import org.wso2.ballerina.core.interpreter.CallableUnitInfo;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.RuntimeEnvironment;
import org.wso2.ballerina.core.interpreter.StackFrame;
import org.wso2.ballerina.core.interpreter.StackVarLocation;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.NodeLocation;
import org.wso2.ballerina.core.model.ParameterDef;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.VariableDef;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.ResourceInvocationExpr;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.types.BTypes;
import org.wso2.ballerina.core.model.values.BArray;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BMessage;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;

import org.wso2.ballerina.core.runtime.errors.handler.ErrorHandlerUtils;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.Map;

import static org.wso2.ballerina.core.runtime.Constants.SYSTEM_PROP_BAL_ARGS;

/**
 * {@code BalProgramExecutor} is responsible for executing a BallerinaProgram.
 *
 * @since 0.8.0
 */
public class BalProgramExecutor {

    private static final Logger log = LoggerFactory.getLogger(BalProgramExecutor.class);

    public static void execute(CarbonMessage cMsg, CarbonCallback callback, Resource resource, Service service,
                               Context balContext) {

        SymbolName symbolName = service.getSymbolName();
        balContext.setServiceInfo(
                new CallableUnitInfo(symbolName.getName(), symbolName.getPkgPath(), service.getNodeLocation()));

        balContext.setBalCallback(new DefaultBalCallback(callback));
        Expression[] exprs = new Expression[resource.getParameterDefs().length];

        BValue[] argValues = new BValue[resource.getParameterDefs().length];

        int locationCounter = 0;
        for (ParameterDef parameter : resource.getParameterDefs()) {
            NodeLocation nodeLocation = parameter.getNodeLocation();
            String parameterName = parameter.getName();
            VariableRefExpr variableRefExpr = new VariableRefExpr(nodeLocation, parameterName);
            StackVarLocation location = new StackVarLocation(locationCounter);
            VariableDef variableDef = new VariableDef(nodeLocation, parameter.getType(), new SymbolName(parameterName));
            variableRefExpr.setVariableDef(variableDef);
            variableRefExpr.setMemoryLocation(location);
            variableRefExpr.setType(parameter.getType());
            exprs[locationCounter] = variableRefExpr;

            // Set message as the first argument
            if (locationCounter == 0) {
                argValues[locationCounter] = new BMessage(cMsg);
            } else {
                Map<String, String> resourceArgsMap =
                        (Map<String, String>) cMsg.getProperty(
                                org.wso2.ballerina.core.runtime.Constants.RESOURCE_ARGS);

                for (Annotation annotation : parameter.getAnnotations()) {
                    if (resourceArgsMap.get(annotation.getValue()) != null) {
                        // ToDo Only String and Int param types are supported.
                        if (parameter.getType() == BTypes.typeString) {
                            argValues[locationCounter] = new BString(resourceArgsMap.get(annotation.getValue()));
                        } else if (parameter.getType() == BTypes.typeInt) {
                            argValues[locationCounter] = new BInteger(Integer.parseInt(
                                    resourceArgsMap.get(annotation.getValue())));
                        }
                    }
                }
            }
            locationCounter++;
        }

        // Create the interpreter and Execute
        RuntimeEnvironment runtimeEnv = resource.getApplication().getRuntimeEnv();
        BLangExecutor executor = new BLangExecutor(runtimeEnv, balContext);

        SymbolName resourceSymbolName = resource.getSymbolName();
        CallableUnitInfo resourceInfo = new CallableUnitInfo(resourceSymbolName.getName(),
                resourceSymbolName.getName(), resource.getNodeLocation());

        StackFrame currentStackFrame = new StackFrame(argValues, new BValue[0], resourceInfo);
        balContext.getControlStack().pushFrame(currentStackFrame);
        new ResourceInvocationExpr(resource, exprs).executeMultiReturn(executor);
        balContext.getControlStack().popFrame();
    }

    /**
     * Execute a BallerinaFunction main function.
     *
     * @param balFile Ballerina main function to be executed in given Ballerina File.
     */
    public static void execute(BallerinaFile balFile) {

        Context bContext = new Context();
        try {
            SymbolName argsName;
            BallerinaFunction mainFun = (BallerinaFunction) balFile.getMainFunction();
            if (mainFun != null) {

                // TODO Refactor this logic ASAP
                ParameterDef[] parameterDefs = mainFun.getParameterDefs();
                argsName = parameterDefs[0].getSymbolName();

//                if (parameters.length == 1 && parameters[0].getType() == BTypes.getArrayType(BTypes.
//                        typeString.toString())) {
//                } else {
//                    throw new BallerinaException("Main function does not comply with standard main function in" +
//                            " ballerina");
//                }
                NodeLocation mainFuncLocation = mainFun.getNodeLocation();

                // Read from command line arguments
                String balArgs = System.getProperty(SYSTEM_PROP_BAL_ARGS);
                String[] arguments;

                if (balArgs == null || balArgs.trim().length() == 0) {
                    arguments = new String[0];
                } else {
                    arguments = balArgs.split(";");
                }

                Expression[] exprs = new Expression[1];
                VariableRefExpr variableRefExpr = new VariableRefExpr(mainFuncLocation, argsName);
                StackVarLocation location = new StackVarLocation(0);
                variableRefExpr.setMemoryLocation(location);
                variableRefExpr.setType(BTypes.typeString);
                exprs[0] = variableRefExpr;

                BArray<BString> arrayArgs = new BArray<>(BString.class);
                for (int i = 0; i < arguments.length; i++) {
                    arrayArgs.add(i, new BString(arguments[i]));
                }
                BValue[] argValues = {arrayArgs};

                // 3) Create a function invocation expression
                FunctionInvocationExpr funcIExpr = new FunctionInvocationExpr(mainFuncLocation,
                        mainFun.getName(), null, mainFun.getPackagePath(), exprs);
                funcIExpr.setCallableUnit(mainFun);

                SymbolName functionSymbolName = funcIExpr.getCallableUnit().getSymbolName();
                CallableUnitInfo functionInfo = new CallableUnitInfo(functionSymbolName.getName(),
                        functionSymbolName.getPkgPath(), mainFuncLocation);

                StackFrame currentStackFrame = new StackFrame(argValues, new BValue[0], functionInfo);
                bContext.getControlStack().pushFrame(currentStackFrame);
                RuntimeEnvironment runtimeEnv = RuntimeEnvironment.get(balFile);
                BLangExecutor executor = new BLangExecutor(runtimeEnv, bContext);
                funcIExpr.executeMultiReturn(executor);

                bContext.getControlStack().popFrame();
            }
        } catch (Throwable ex) {
            String errorMsg = ErrorHandlerUtils.getErrorMessage(ex);
            String stacktrace = ErrorHandlerUtils.getMainFuncStackTrace(bContext, ex);
            log.error(errorMsg + "\n" + stacktrace);
        }
    }
}
