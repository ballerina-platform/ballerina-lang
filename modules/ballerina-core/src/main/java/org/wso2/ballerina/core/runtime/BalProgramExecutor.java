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
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.NodeLocation;
import org.wso2.ballerina.core.model.ParameterDef;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.VariableDef;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.ResourceInvocationExpr;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.types.BTypes;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BMessage;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.Map;

/**
 * {@code BalProgramExecutor} is responsible for executing a BallerinaProgram.
 *
 * @since 0.8.0
 */
public class BalProgramExecutor {


    public static void execute(CarbonMessage cMsg, CarbonCallback callback, Resource resource, Service service,
                               Context balContext) {

        balContext.setServiceInfo(
                new CallableUnitInfo(service.getName(), service.getPackagePath(), service.getNodeLocation()));

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

        SymbolName resourceSymbolName = resource.getSymbolName();
        CallableUnitInfo resourceInfo = new CallableUnitInfo(resourceSymbolName.getName(),
                resourceSymbolName.getName(), resource.getNodeLocation());

        StackFrame currentStackFrame = new StackFrame(argValues, new BValue[0], resourceInfo);
        balContext.getControlStack().pushFrame(currentStackFrame);
        if (ModeResolver.getInstance().isNonblockingEnabled()) {
            if (ModeResolver.getInstance().isDebugEnabled()) {
                DebugManager debugManager = DebugManager.getInstance();
                // Only start the debugger if there is an active client (debug session).
                if (debugManager.isDebugSessionActive()) {
                    BLangExecutionDebugger debugger = new BLangExecutionDebugger(runtimeEnv, balContext);
                    debugManager.setDebugger(debugger);
                    balContext.setExecutor(debugger);
                    debugger.execute(new ResourceInvocationExpr(resource, exprs));
                } else {
                    // we do the normal flow
                    BLangNonBlockingExecutor executor = new BLangNonBlockingExecutor(runtimeEnv, balContext);
                    balContext.setExecutor(executor);
                    executor.execute(new ResourceInvocationExpr(resource, exprs));
                }
            } else {
                BLangNonBlockingExecutor executor = new BLangNonBlockingExecutor(runtimeEnv, balContext);
                balContext.setExecutor(executor);
                executor.execute(new ResourceInvocationExpr(resource, exprs));
            }
        } else {
            BLangExecutor executor = new BLangExecutor(runtimeEnv, balContext);
            new ResourceInvocationExpr(resource, exprs).executeMultiReturn(executor);
        }
//        balContext.getControlStack().popFrame();
    }

}
