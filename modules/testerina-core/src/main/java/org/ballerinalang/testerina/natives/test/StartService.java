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
import org.ballerinalang.services.MessageProcessor;
import org.ballerinalang.services.dispatchers.DispatcherRegistry;
import org.ballerinalang.services.dispatchers.http.Constants;
import org.ballerinalang.testerina.core.TesterinaRegistry;
import org.ballerinalang.testerina.core.TesterinaUtils;
import org.ballerinalang.util.codegen.AnnotationAttachmentInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;
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
    private static final String DEFAULT_HOSTNAME = "0.0.0.0";
    private static final String LOCALHOST = "localhost";
    
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
        ctx.initFunction = true;
        String serviceName = getStringArgument(ctx, 0);

        Optional<ServiceInfo> matchingService = Optional.empty();
        for (ProgramFile programFile : TesterinaRegistry.getInstance().getProgramFiles()) {
            // 1) First, we get the Service for the given serviceName from the original ProgramFile
            matchingService = Arrays.stream(programFile.getServicePackageNameList())
                    .map(sName -> programFile.getPackageInfo(sName).getServiceInfoList())
                    .flatMap(Arrays::stream)
                    .filter(serviceInfo -> serviceInfo.getName().equals(serviceName))
                    .findAny();
            matchingService.ifPresent(serviceInfo -> startService(programFile, serviceInfo));
        }

        // 3) fail if no matching service for the given 'serviceName' argument is found.
        if (!matchingService.isPresent()) {
            String listOfServices = TesterinaRegistry.getInstance().getProgramFiles().stream()
                    .map(ProgramFile::getServicePackageNameList).flatMap(Arrays::stream)
                    .collect(Collectors.joining(", "));
            throw new BallerinaException(MSG_PREFIX + "No service with the name " + serviceName + " found. "
                    + "Did you mean to start one of these services? " + listOfServices);
        }

        // 6) return the service url
        BString str = new BString(getServiceURL(matchingService.get()));
        return getBValues(str);
    }

    private void startService(ProgramFile programFile, ServiceInfo matchingService) {
        BallerinaConnectorManager.getInstance().initialize(new MessageProcessor());

        Context bContext = new Context(programFile);
        bContext.initFunction = true;

        PackageInfo packageInfo = matchingService.getPackageInfo();
        
        BLangFunctions.invokeFunction(programFile, packageInfo, packageInfo.getInitFunctionInfo(), bContext);
        BLangFunctions.invokeFunction(programFile, packageInfo, matchingService.getInitFunctionInfo(), bContext);
        DispatcherRegistry.getInstance().getServiceDispatchers().forEach((protocol, dispatcher) ->
                                                                                 dispatcher.serviceRegistered(
                                                                                         matchingService));

        try {
            List<ServerConnector> startedConnectors = BallerinaConnectorManager.getInstance()
                    .startPendingConnectors();
            clearPendingConnectors();
            startedConnectors.forEach(serverConnector -> outStream.println("ballerina: started server connector " 
                    + serverConnector));
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

    private String getServiceURL(ServiceInfo service) {
        try {
            String listenerInterface = Constants.DEFAULT_INTERFACE;
            String basePath = service.getName();

            AnnotationAttachmentInfo annotation = service.getAnnotationAttachmentInfo(Constants.HTTP_PACKAGE_PATH,
                                                                                      Constants
                                                                                              .ANNOTATION_NAME_BASE_PATH);
            if (annotation != null) {
                basePath = annotation.getAnnotationAttributeValue(Constants.VALUE_ATTRIBUTE).getStringValue();
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
                String host = config.getHost().equals(DEFAULT_HOSTNAME) ? LOCALHOST : config.getHost();
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
