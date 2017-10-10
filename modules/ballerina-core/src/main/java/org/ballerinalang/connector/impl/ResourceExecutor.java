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
package org.ballerinalang.connector.impl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVM;
import org.ballerinalang.bre.bvm.ControlStackNew;
import org.ballerinalang.bre.bvm.StackFrame;
import org.ballerinalang.connector.api.ConnectorFuture;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ResourceInfo;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.codegen.WorkerInfo;
import org.ballerinalang.util.codegen.attributes.CodeAttributeInfo;
import org.ballerinalang.util.debugger.DebugInfoHolder;
import org.ballerinalang.util.debugger.VMDebugManager;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.Map;

/**
 * {@code ResourceExecutor} This provides the implementation to execute resources within Ballerina.
 *
 * @since 0.94
 */
public class ResourceExecutor {

    /**
     * This method will execute the resource, given required details.
     * And it will use the future instance to notify interested parties about the
     * outcome of the execution.
     *
     * @param resource to be executed.
     * @param connectorFuture to notify.
     * @param bValues for parameters.
     */
    public static void execute(Resource resource, ConnectorFuture connectorFuture, BValue... bValues) {
        //TODO
    }

    public static void execute(Resource resource, BServerConnectorFuture connectorFuture,
                               Map<String, Object> properties, BValue... bValues) {
// engage Service interceptors.
//        if (BLangRuntimeRegistry.getInstance().isInterceptionEnabled(protocol)) {
//            org.ballerinalang.runtime.model.ServerConnector serverConnector =
// BLangRuntimeRegistry.getInstance().getServerConnector(protocol);
//            resourceCallback = new ServiceInterceptorCallback(callback, protocol);
//            List<ServiceInterceptor> serviceInterceptorList = serverConnector.getServiceInterceptorList();
//            BMessage message = new BMessage(cMsg);
//            // Invoke request interceptor serviceInterceptorList.
//            for (ServiceInterceptor interceptor : serviceInterceptorList) {
//                if (interceptor.getRequestFunction() == null) {
//                    continue;
//                }
//                ServiceInterceptor.Result result = BLangVMInterceptors.invokeResourceInterceptor(interceptor,
//                        interceptor.getRequestFunction(), message);
//                if (result.getMessageIntercepted() == null) {
//                    // Can't Intercept null message further. Let it handle at server connector level.
//                    breLog.error("error in service interception, return message null in " +
//                            (".".equals(interceptor.getPackageInfo().getPkgPath()) ? "" :
//                                    interceptor.getPackageInfo().getPkgPath() + ":") +
//                            ServiceInterceptor.REQUEST_INTERCEPTOR_NAME);
//                    callback.done(null);
//                    return;
//                }
//                message = result.getMessageIntercepted();
//                if (!result.isInvokeNext()) {
//                    callback.done(message.value());
//                    return;
//                }
//                resourceMessage = message.value();
//            }
//        }

        if (resource == null) {
            //todo
        }
        ResourceInfo resourceInfo = ((BResource) resource).getResourceInfo();
        ServiceInfo serviceInfo = resourceInfo.getServiceInfo();
        // Invoke VM.
        PackageInfo packageInfo = serviceInfo.getPackageInfo();
        ProgramFile programFile = packageInfo.getProgramFile();

        Context context = new Context(programFile);
        context.setServiceInfo(serviceInfo);
        context.setConnectorFuture(connectorFuture);

        //TODO remove this with a proper way
        if (properties != null) {
            properties.forEach((k, v) -> context.setProperty(k, v));
        }

        ControlStackNew controlStackNew = context.getControlStackNew();

        // Now create callee's stack-frame
        WorkerInfo defaultWorkerInfo = resourceInfo.getDefaultWorkerInfo();
        StackFrame calleeSF = new StackFrame(resourceInfo, defaultWorkerInfo, -1, new int[0]);
        controlStackNew.pushFrame(calleeSF);

        CodeAttributeInfo codeAttribInfo = defaultWorkerInfo.getCodeAttributeInfo();
        context.setStartIP(codeAttribInfo.getCodeAddrs());

        String[] stringLocalVars = new String[codeAttribInfo.getMaxStringLocalVars()];
        int[] intLocalVars = new int[codeAttribInfo.getMaxIntLocalVars()];
        long[] longLocalVars = new long[codeAttribInfo.getMaxLongLocalVars()];
        double[] doubleLocalVars = new double[codeAttribInfo.getMaxDoubleLocalVars()];
        BRefType[] refLocalVars = new BRefType[codeAttribInfo.getMaxRefLocalVars()];

        int stringParamCount = 0;
        int intParamCount = 0;
        int doubleParamCount = 0;
        int longParamCount = 0;
        int refParamCount = 0;
        BType[] bTypes = resourceInfo.getParamTypes();

        if (bValues != null) {
            for (int i = 0; i < bValues.length; i++) {
                BType btype = bTypes[i];
                BValue value = bValues[i];

                // Set default values
                if (value == null || "".equals(value)) {
                    if (btype == BTypes.typeString) {
                        stringLocalVars[stringParamCount++] = "";
                    }
                    continue;
                }

                if (btype == BTypes.typeString) {
                    stringLocalVars[stringParamCount++] = value.stringValue();
                } else if (btype == BTypes.typeBoolean) {
                    if ("true".equalsIgnoreCase(value.stringValue())) {
                        intLocalVars[intParamCount++] = 1;
                    } else if ("false".equalsIgnoreCase(value.stringValue())) {
                        intLocalVars[intParamCount++] = 0;
                    } else {
                        throw new BallerinaException("Unsupported parameter type for parameter " + value);
                    }
                } else if (btype == BTypes.typeFloat) {
                    doubleLocalVars[doubleParamCount++] = new Double(((BFloat) value).floatValue());
                } else if (btype == BTypes.typeInt) {
                    longLocalVars[longParamCount++] = ((BInteger) value).intValue();
                } else if (value instanceof BStruct) {
                    refLocalVars[refParamCount++] = (BRefType) value;
                } else {
                    //TODO Can't throw, need to handle with future
                    throw new BallerinaException("Unsupported parameter type for parameter " + value);
                }
            }
        }

        // It is given that first parameter of the resource is carbon message.
        calleeSF.setLongLocalVars(longLocalVars);
        calleeSF.setDoubleLocalVars(doubleLocalVars);
        calleeSF.setStringLocalVars(stringLocalVars);
        calleeSF.setIntLocalVars(intLocalVars);
        calleeSF.setRefLocalVars(refLocalVars);

        // Execute workers
        // Pass the incoming message variable into the worker invocations
        // Fix #2623
        StackFrame callerSF = new StackFrame(resourceInfo, defaultWorkerInfo, -1, new int[0]);
        callerSF.setRefRegs(new BRefType[1]);
        callerSF.getRefRegs()[0] = refLocalVars[0];

        BLangVM bLangVM = new BLangVM(packageInfo.getProgramFile());
        if (VMDebugManager.getInstance().isDebugEnabled() && VMDebugManager.getInstance().isDebugSessionActive()) {
            VMDebugManager debugManager = VMDebugManager.getInstance();
            context.setAndInitDebugInfoHolder(new DebugInfoHolder());
            context.getDebugInfoHolder().setCurrentCommand(DebugInfoHolder.DebugCommand.RESUME);
            context.setDebugEnabled(true);
            debugManager.setDebuggerContext(context);
        }
        bLangVM.run(context);
        connectorFuture.notifySuccess();
    }
}
