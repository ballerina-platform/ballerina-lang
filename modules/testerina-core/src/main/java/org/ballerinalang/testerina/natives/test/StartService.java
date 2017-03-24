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
package org.ballerinalang.testerina.natives.test;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.AnnotationAttachment;
import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.Service;
import org.ballerinalang.model.builder.BLangExecutionFlowBuilder;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.natives.connectors.BallerinaConnectorManager;
import org.ballerinalang.services.dispatchers.DispatcherRegistry;
import org.ballerinalang.services.dispatchers.http.Constants;
import org.ballerinalang.testerina.core.TesterinaRegistry;
import org.ballerinalang.testerina.core.TesterinaUtils;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.carbon.messaging.ServerConnector;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.listener.HTTPServerConnector;

import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Native function ballerina.lang.test:startServer.
 *
 * @since 0.8.0
 */
@BallerinaFunction(packageName = "ballerina.test",
        functionName = "startService", args = {
        @Argument(name = "serviceName", type = TypeEnum.STRING) }, returnType = {
        @ReturnType(type = TypeEnum.STRING) }, isPublic = true)
@BallerinaAnnotation(annotationName = "Description",
                     attributes = { @Attribute(name = "value",
                                               value = "Starts the service specified in the 'serviceName' argument") })
@BallerinaAnnotation(annotationName = "Param",
                     attributes = { @Attribute(name = "serviceName",
                                               value = "Name of the service to start") })
public class StartService extends AbstractNativeFunction {

    private static final String MSG_PREFIX = "test:startService: ";
    private static PrintStream outStream = System.err;

    static String getFileName(Path sourceFilePath) {
        Path fileNamePath = sourceFilePath.getFileName();
        return (fileNamePath != null) ? fileNamePath.toString() : sourceFilePath.toString();
    }

    /**
     *
     * Starts the service specified in the 'serviceName' argument.
     *
     * The TesterinaRegistry has the original BallerinaFile with all the services.
     * It also contain the corresponding actual BallerinaFile object without any services. (the derivativeBFile)
     * An Application named 'default' is registered for the derivativeBFile.
     *
     */
    @Override
    public BValue[] execute(Context ctx) {
        String serviceName = getArgument(ctx, 0).stringValue();

        Optional<Service> matchingService = Optional.empty();
        for (BLangProgram bLangProgram : TesterinaRegistry.getInstance().getBLangPrograms()) {
            // 1) First, we get the Service for the given serviceName from the original BLangProgram
            matchingService = Arrays.stream(bLangProgram.getServicePackages()).map(BLangPackage::getServices)
                    .flatMap(Arrays::stream).filter(s -> s.getName().equals(serviceName)).findAny();
            matchingService.ifPresent(service -> startService(bLangProgram, service));
        }

        // 3) fail if no matching service for the given 'serviceName' argument is found.
        if (!matchingService.isPresent()) {
            String listOfServices = TesterinaRegistry.getInstance().getBLangPrograms().stream()
                    .map(BLangProgram::getServicePackages).flatMap(Arrays::stream).map(BLangPackage::getServices)
                    .flatMap(Arrays::stream).map(service -> service.getSymbolName().getName())
                    .collect(Collectors.joining(", "));
            throw new BallerinaException(MSG_PREFIX + "No service with the name " + serviceName + " found. "
                    + "Did you mean to start one of these services? " + listOfServices);
        }

        // 6) return the service url
        BString str = new BString(getServiceURL(matchingService.get()));
        return getBValues(str);
    }

    private void startService(BLangProgram bLangProgram, Service matchingService) {
        BLangExecutionFlowBuilder flowBuilder = new BLangExecutionFlowBuilder();
        matchingService.setBLangProgram(bLangProgram);
        DispatcherRegistry.getInstance().getServiceDispatchers().values()
                .forEach(dispatcher -> dispatcher.serviceRegistered(matchingService));
        // Build Flow for Non-Blocking execution.
        matchingService.accept(flowBuilder);

        try {
            List<ServerConnector> startedConnectors = BallerinaConnectorManager.getInstance().startPendingConnectors();
            clearPendingConnectors();
            startedConnectors.forEach(
                    serverConnector -> outStream.println("ballerina: started server connector " + serverConnector));
        } catch (ServerConnectorException e) {
            throw new RuntimeException("error starting server connectors: " + e.getMessage(), e);
        }

    }

    /**
     * Temporary fix until we have a ballerina release with this fix
     * https://github.com/ballerinalang/ballerina/pull/1962
     *
     */
    private void clearPendingConnectors() {
        try {
            List startupDelayedServerConnectors = TesterinaUtils
                    .getField(BallerinaConnectorManager.getInstance(), "startupDelayedServerConnectors", List.class);
            startupDelayedServerConnectors.clear();
        } catch (NoSuchFieldException e) {
            //ignore
        }
    }

    private String getServiceURL(Service service) {
        try {
            String listenerInterface = Constants.DEFAULT_INTERFACE;
            String basePath = service.getSymbolName().getName();
            for (AnnotationAttachment annotation : service.getAnnotations()) {
                if (annotation.getName().equals(Constants.PROTOCOL_HTTP + ":" + Constants.ANNOTATION_NAME_BASE_PATH)) {
                    basePath = annotation.getValue();
                    break;
                }
            }
            if (basePath.startsWith("\"")) {
                basePath = basePath.substring(1, basePath.length() - 1);
            }

            if (!basePath.startsWith("/")) {
                basePath = "/".concat(basePath);
            }

            ServerConnector serverConnector =
                    BallerinaConnectorManager.getInstance().getServerConnector(listenerInterface);
            if (serverConnector instanceof HTTPServerConnector) {
                ListenerConfiguration config = ((HTTPServerConnector) serverConnector).getListenerConfiguration();
                String host = config.getHost();
                int port = config.getPort();
                String scheme = config.getScheme();
                return new URL(scheme, host, port, basePath).toExternalForm();
            } else {
                throw new BallerinaException(MSG_PREFIX + "Cannot handle non-http protocols.");
            }
        } catch (MalformedURLException e) {
            throw new BallerinaException(MSG_PREFIX + "Error while constructing service url for " + service.getName());
        }

    }


}
