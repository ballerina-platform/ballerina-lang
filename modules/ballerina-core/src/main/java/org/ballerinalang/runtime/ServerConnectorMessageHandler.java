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

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVM;
import org.ballerinalang.bre.bvm.BLangVMWorkers;
import org.ballerinalang.bre.bvm.ControlStackNew;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.natives.connectors.BallerinaConnectorManager;
import org.ballerinalang.runtime.interceptors.BLangVMInterceptors;
import org.ballerinalang.runtime.interceptors.ServiceInterceptorCallback;
import org.ballerinalang.runtime.model.BLangRuntimeRegistry;
import org.ballerinalang.runtime.model.ServerConnector;
import org.ballerinalang.runtime.model.ServiceInterceptor;
import org.ballerinalang.services.DefaultServerConnectorErrorHandler;
import org.ballerinalang.services.dispatchers.DispatcherRegistry;
import org.ballerinalang.services.dispatchers.ResourceDispatcher;
import org.ballerinalang.services.dispatchers.ServiceDispatcher;
import org.ballerinalang.util.codegen.CodeAttributeInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ResourceInfo;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.codegen.WorkerInfo;
import org.ballerinalang.util.debugger.DebugInfoHolder;
import org.ballerinalang.util.debugger.VMDebugManager;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.ServerConnectorErrorHandler;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * {@code ServerConnectorMessageHandler} is responsible for bridging Ballerina Program and External Server Connector.
 *
 * @since 0.8.0
 */
public class ServerConnectorMessageHandler {

    private static final Logger breLog = LoggerFactory.getLogger(ServerConnectorMessageHandler.class);

    public static void handleInbound(CarbonMessage cMsg, CarbonCallback callback) {

        String protocol = (String) cMsg.getProperty(org.wso2.carbon.messaging.Constants.PROTOCOL);
        if (protocol == null) {
            throw new BallerinaException("protocol not defined in the incoming request");
        }

        // Find the Service Dispatcher
        ServiceDispatcher dispatcher = DispatcherRegistry.getInstance().getServiceDispatcher(protocol);
        if (dispatcher == null) {
            throw new BallerinaException("no service dispatcher available to handle protocol: " + protocol);
        }

        try {
            // Find the Service
            ServiceInfo service = dispatcher.findService(cMsg, callback);
            if (service == null) {
                throw new BallerinaException("no Service found to handle the service request");
                // Finer details of the errors are thrown from the dispatcher itself, Ideally we shouldn't get here.
            }

            // Find the Resource Dispatcher
            ResourceDispatcher resourceDispatcher = DispatcherRegistry.getInstance().getResourceDispatcher(protocol);
            if (resourceDispatcher == null) {
                throw new BallerinaException("no resource dispatcher available to handle protocol: " + protocol);
            }

            // Find the Resource
            ResourceInfo resource = resourceDispatcher.findResource(service, cMsg, callback);
            invokeResource(cMsg, callback, protocol, resource, service);
        } catch (Throwable throwable) {
            handleError(cMsg, callback, throwable);
        }
    }

    /**
     * Resource invocation logic.
     *
     * @param cMsg         incoming carbonMessage
     * @param callback     carbonCallback
     * @param protocol     protocol of the resource
     * @param resourceInfo resource that has been invoked
     * @param serviceInfo  service that has been invoked
     */
    public static void invokeResource(CarbonMessage cMsg, CarbonCallback callback, String protocol,
                                      ResourceInfo resourceInfo, ServiceInfo serviceInfo) {
        // engage Service interceptors.
        CarbonCallback resourceCallback = callback;
        CarbonMessage resourceMessage = cMsg;
        if (BLangRuntimeRegistry.getInstance().isInterceptionEnabled(protocol)) {
            ServerConnector serverConnector = BLangRuntimeRegistry.getInstance().getServerConnector(protocol);
            resourceCallback = new ServiceInterceptorCallback(callback, protocol);
            List<ServiceInterceptor> serviceInterceptorList = serverConnector.getServiceInterceptorList();
            BMessage message = new BMessage(cMsg);
            // Invoke request interceptor serviceInterceptorList.
            for (ServiceInterceptor interceptor : serviceInterceptorList) {
                if (interceptor.getRequestFunction() == null) {
                    continue;
                }
                ServiceInterceptor.Result result = BLangVMInterceptors.invokeResourceInterceptor(interceptor,
                        interceptor.getRequestFunction(), message);
                if (result.getMessageIntercepted() == null) {
                    // Can't Intercept null message further. Let it handle at server connector level.
                    breLog.error("error in service interception, return message null in " +
                            (".".equals(interceptor.getPackageInfo().getPkgPath()) ? "" :
                                    interceptor.getPackageInfo().getPkgPath() + ":") +
                            ServiceInterceptor.REQUEST_INTERCEPTOR_NAME);
                    callback.done(null);
                    return;
                }
                message = result.getMessageIntercepted();
                if (!result.isInvokeNext()) {
                    callback.done(message.value());
                    return;
                }
                resourceMessage = message.value();
            }
        }
        // Invoke VM.
        PackageInfo packageInfo = serviceInfo.getPackageInfo();
        ProgramFile programFile = packageInfo.getProgramFile();

        Context context = new Context(programFile);
        context.setServiceInfo(serviceInfo);
        context.setCarbonMessage(resourceMessage);
        context.setBalCallback(new DefaultBalCallback(resourceCallback));
        ControlStackNew controlStackNew = context.getControlStackNew();

        // Now create callee's stack-frame
        WorkerInfo defaultWorkerInfo = resourceInfo.getDefaultWorkerInfo();
        org.ballerinalang.bre.bvm.StackFrame calleeSF =
                new org.ballerinalang.bre.bvm.StackFrame(resourceInfo, defaultWorkerInfo, -1, new int[0]);
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
        String[] paramNameArray = resourceInfo.getParamNames();
        BType[] bTypes = resourceInfo.getParamTypes();
        if (resourceMessage.getProperty(org.ballerinalang.runtime.Constants.RESOURCE_ARGS) != null) {
            Map<String, String> resourceArgumentValues =
                    (Map<String, String>) resourceMessage.getProperty(org.ballerinalang.runtime.Constants
                            .RESOURCE_ARGS);

            for (int i = 0; i < paramNameArray.length; i++) {
                BType btype = bTypes[i];
                String value = resourceArgumentValues.get(paramNameArray[i]);

                // Set default values
                if (value == null || "".equals(value)) {
                    if (btype == BTypes.typeString) {
                        stringLocalVars[stringParamCount++] = "";
                    }
                    continue;
                }

                if (btype == BTypes.typeString) {
                    stringLocalVars[stringParamCount++] = value;
                } else if (btype == BTypes.typeBoolean) {
                    if ("true".equalsIgnoreCase(value)) {
                        intLocalVars[intParamCount++] = 1;
                    } else if ("false".equalsIgnoreCase(value)) {
                        intLocalVars[intParamCount++] = 0;
                    } else {
                        throw new BallerinaException("Unsupported parameter type for parameter " + value);
                    }
                } else if (btype == BTypes.typeFloat) {
                    doubleLocalVars[doubleParamCount++] = new Double(value);
                } else if (btype == BTypes.typeInt) {
                    longLocalVars[longParamCount++] = Long.parseLong(value);
                } else {
                    throw new BallerinaException("Unsupported parameter type for parameter " + value);
                }
            }
        }

        // It is given that first parameter of the resource is carbon message.
        refLocalVars[0] = new BMessage(resourceMessage);
        calleeSF.setLongLocalVars(longLocalVars);
        calleeSF.setDoubleLocalVars(doubleLocalVars);
        calleeSF.setStringLocalVars(stringLocalVars);
        calleeSF.setIntLocalVars(intLocalVars);
        calleeSF.setRefLocalVars(refLocalVars);

        // Execute workers
        // Pass the incoming message variable into the worker invocations
        // Fix #2623
        org.ballerinalang.bre.bvm.StackFrame callerSF = new org.ballerinalang.bre.bvm.StackFrame(resourceInfo,
                defaultWorkerInfo, -1, new int[0]);
        callerSF.getRefRegs()[0] = refLocalVars[0];
        int[] retRegs = {0};
        BLangVMWorkers.invoke(packageInfo.getProgramFile(), resourceInfo, callerSF, retRegs);

        BLangVM bLangVM = new BLangVM(packageInfo.getProgramFile());
        if (VMDebugManager.getInstance().isDebugEnagled()) {
            VMDebugManager debugManager = VMDebugManager.getInstance();
            context.setDebugInfoHolder(new DebugInfoHolder());
            context.getDebugInfoHolder().setCurrentCommand(DebugInfoHolder.DebugCommand.RESUME);
            context.setDebugEnabled(true);
            debugManager.setDebuggerContext("main", context); //todo fix
        }
        bLangVM.run(context);
    }

    public static void handleOutbound(CarbonMessage cMsg, CarbonCallback callback) {
        callback.done(cMsg);
    }

    public static void handleError(CarbonMessage cMsg, CarbonCallback callback, Throwable throwable) {
        String errorMsg = throwable.getMessage();

        // bre log should contain bre stack trace, not the ballerina stack trace
        breLog.error("error: " + errorMsg, throwable);
        Object protocol = cMsg.getProperty("PROTOCOL");
        Optional<ServerConnectorErrorHandler> optionalErrorHandler =
                BallerinaConnectorManager.getInstance().getServerConnectorErrorHandler((String) protocol);

        try {
            optionalErrorHandler
                    .orElseGet(DefaultServerConnectorErrorHandler::getInstance)
                    .handleError(new BallerinaException(errorMsg, throwable.getCause()), cMsg, callback);
        } catch (Exception e) {
            breLog.error("Cannot handle error using the error handler for: " + protocol, e);
        }

    }

}
