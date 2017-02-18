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

package org.ballerinalang.runtime;

import org.ballerinalang.bre.BLangExecutor;
import org.ballerinalang.bre.CallableUnitInfo;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.RuntimeEnvironment;
import org.ballerinalang.bre.StackFrame;
import org.ballerinalang.bre.StackVarLocation;
import org.ballerinalang.bre.nonblocking.BLangNonBlockingExecutor;
import org.ballerinalang.bre.nonblocking.ModeResolver;
import org.ballerinalang.bre.nonblocking.debugger.BLangExecutionDebugger;
import org.ballerinalang.model.Annotation;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.ParameterDef;
import org.ballerinalang.model.Resource;
import org.ballerinalang.model.Service;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.VariableDef;
import org.ballerinalang.model.expressions.Expression;
import org.ballerinalang.model.expressions.ResourceInvocationExpr;
import org.ballerinalang.model.expressions.VariableRefExpr;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.debugger.DebugManager;
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
                                org.ballerinalang.runtime.Constants.RESOURCE_ARGS);

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
        RuntimeEnvironment runtimeEnv = service.getBLangProgram().getRuntimeEnvironment();

        SymbolName resourceSymbolName = resource.getSymbolName();
        CallableUnitInfo resourceInfo = new CallableUnitInfo(resourceSymbolName.getName(),
                resourceSymbolName.getName(), resource.getNodeLocation());

        BValue[] cacheValues = new BValue[resource.getTempStackFrameSize()];

        StackFrame currentStackFrame = new StackFrame(argValues, new BValue[0], cacheValues, resourceInfo);
        balContext.getControlStack().pushFrame(currentStackFrame);
        if (ModeResolver.getInstance().isDebugEnabled()) {
            DebugManager debugManager = DebugManager.getInstance();
            // Only start the debugger if there is an active client (debug session).
            if (debugManager.isDebugSessionActive()) {
                BLangExecutionDebugger debugger = new BLangExecutionDebugger(runtimeEnv, balContext);
                debugManager.setDebugger(debugger);
                balContext.setExecutor(debugger);
                debugger.execute(new ResourceInvocationExpr(resource, exprs));
            } else {
                // repeated code to make sure debugger have no impact in none debug mode.
                if (ModeResolver.getInstance().isNonblockingEnabled()) {
                    BLangNonBlockingExecutor executor = new BLangNonBlockingExecutor(runtimeEnv, balContext);
                    balContext.setExecutor(executor);
                    executor.execute(new ResourceInvocationExpr(resource, exprs));
                } else {
                    BLangExecutor executor = new BLangExecutor(runtimeEnv, balContext);
                    new ResourceInvocationExpr(resource, exprs).executeMultiReturn(executor);
                }
            }
        } else if (ModeResolver.getInstance().isNonblockingEnabled()) {
            BLangNonBlockingExecutor executor = new BLangNonBlockingExecutor(runtimeEnv, balContext);
            balContext.setExecutor(executor);
            executor.execute(new ResourceInvocationExpr(resource, exprs));
        } else {
            BLangExecutor executor = new BLangExecutor(runtimeEnv, balContext);
            new ResourceInvocationExpr(resource, exprs).executeMultiReturn(executor);
            balContext.getControlStack().popFrame();
        }
    }
}
