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
import org.ballerinalang.bre.bvm.BLangVMErrors;
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
import org.ballerinalang.util.codegen.AnnAttachmentInfo;
import org.ballerinalang.util.codegen.AnnAttributeValue;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.exceptions.RuntimeErrors;
import org.ballerinalang.util.program.BLangFunctions;
import org.wso2.carbon.messaging.ServerConnector;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.message.HTTPMessageUtil;

import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        ctx.disableNonBlocking = true;
        String serviceName = getStringArgument(ctx, 0);

        ServiceInfo matchingService = null;
        for (ProgramFile programFile : TesterinaRegistry.getInstance().getProgramFiles()) {
            // 1) First, we get the Service for the given serviceName from the original ProgramFile
            matchingService = programFile.getEntryPackage().getServiceInfo(serviceName);
            if (matchingService != null) {
                startService(programFile, matchingService);
                break;
            }
        }

        // 3) fail if no matching service for the given 'serviceName' argument is found.
        if (matchingService == null) {
            String listOfServices = TesterinaRegistry.getInstance().getProgramFiles().stream()
                    .map(ProgramFile::getEntryPackage).map(PackageInfo::getServiceInfoEntries).flatMap(Arrays::stream)
                    .map(serviceInfo -> serviceInfo.getName()).collect(Collectors.joining(", "));
            throw new BallerinaException(MSG_PREFIX + "No service with the name " + serviceName + " found. "
                    + "Did you mean to start one of these services? " + listOfServices);
        }

        // 6) return the service url
        BString str = new BString(getServiceURL(matchingService));
        return getBValues(str);
    }

    private void startService(ProgramFile programFile, ServiceInfo matchingService) {
        BallerinaConnectorManager.getInstance().initialize(new MessageProcessor());

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
        bContext.disableNonBlocking = true;

        // Invoke package init function
        BLangFunctions.invokePackageInitFunction(programFile, servicesPackage.getInitFunctionInfo(), bContext);

        int serviceCount = 0;
            // Invoke service init function
            bContext.setServiceInfo(matchingService);
            BLangFunctions.invokeFunction(programFile, matchingService.getInitFunctionInfo(), bContext);
            if (bContext.getError() != null) {
                String stackTraceStr = BLangVMErrors.getPrintableStackTrace(bContext.getError());
                throw new BLangRuntimeException("error: " + stackTraceStr);
            }

            if (!DispatcherRegistry.getInstance().protocolPkgExist(matchingService.getProtocolPkgPath())) {
                throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.INVALID_SERVICE_PROTOCOL,
                        matchingService.getProtocolPkgPath());
            }
            // Deploy service
            DispatcherRegistry.getInstance().getServiceDispatcherFromPkg(matchingService.getProtocolPkgPath())
                    .serviceRegistered(matchingService);
            serviceCount++;

        if (serviceCount == 0) {
            throw new BallerinaException("no services found in '" + programFile.getProgramFilePath() + "'");
        }

        try {
            List<ServerConnector> startedConnectors = BallerinaConnectorManager.getInstance()
                    .startPendingConnectors();
            startedConnectors.forEach(serverConnector -> outStream.println("ballerina: started server connector " +
                    serverConnector));

            // Starting up HTTP Server connectors
            List<org.wso2.carbon.transport.http.netty.contract.ServerConnector> startedHTTPConnectors =
                    BallerinaConnectorManager.getInstance().startPendingHTTPConnectors();
            startedHTTPConnectors.forEach(serverConnector -> outStream.println("ballerina: started server connector " +
                    serverConnector));
        } catch (ServerConnectorException e) {
            throw new RuntimeException("error starting server connectors: " + e.getMessage(), e);
        }
    }

    private String getServiceURL(ServiceInfo service) {
        try {
            AnnAttachmentInfo annotationInfo = service.getAnnotationAttachmentInfo(Constants
                    .HTTP_PACKAGE_PATH, Constants.ANN_NAME_CONFIG);

            String basePath = discoverBasePathFrom(service, annotationInfo);
            Set<ListenerConfiguration> listenerConfigurationSet = getListenerConfig(annotationInfo);

            if (listenerConfigurationSet.isEmpty()) {
                throw new BallerinaException(MSG_PREFIX + "Cannot find relevant transport listener configuration");
            }

            //There can be multiple urls with the new transport implementation, so for the time being,
            // just use the first listener configuration to generate the url
            ListenerConfiguration config = listenerConfigurationSet.iterator().next();
            String host = config.getHost().equals(DEFAULT_HOSTNAME) ? LOCALHOST : config.getHost();
            int port = config.getPort();
            String scheme = config.getScheme();
            return new URL(scheme, host, port, basePath).toExternalForm();
        } catch (MalformedURLException e) {
            throw new BallerinaException(MSG_PREFIX + "Error while constructing service url for " + service.getName());
        }

    }

    /**
     * This method is used to get listener configuration.
     * It will return dynamic listener configuration if available,
     * otherwise it will return default listener configurations.
     *
     * TODO use methods from ballerina once available.
     * @param annotationInfo annotation info object.
     * @return  listenerProp map.
     */
    private Set<ListenerConfiguration> getListenerConfig(AnnAttachmentInfo annotationInfo) {
        Map<String, Map<String, String>> listenerProp = buildListerProperties(annotationInfo);

        Set<ListenerConfiguration> listenerConfigurationSet;
        if (listenerProp == null || listenerProp.isEmpty()) {
            listenerConfigurationSet =
                    BallerinaConnectorManager.getInstance().getDefaultListenerConfiugrationSet();
        } else {
            listenerConfigurationSet = getListenerConfigurationsFrom(listenerProp);
        }
        return listenerConfigurationSet;
    }

    /**
     * Method to build map of listener property maps given the service annotation attachment.
     * This will first look for the port property and if present then it will get other properties,
     * and create the property map.
     *
     * TODO use methods from ballerina once available.
     * @param configInfo            In which listener configurations are specified.
     * @return listenerConfMap      With required properties
     */
    private Map<String, Map<String, String>> buildListerProperties(AnnAttachmentInfo configInfo) {
        if (configInfo == null) {
            return null;
        }
        //key - listenerId, value - listener config property map
        Map<String, Map<String, String>> listenerConfMap = new HashMap<>();

        AnnAttributeValue hostAttrVal = configInfo.getAttributeValue
                (Constants.ANN_CONFIG_ATTR_HOST);
        AnnAttributeValue portAttrVal = configInfo.getAttributeValue
                (Constants.ANN_CONFIG_ATTR_PORT);
        AnnAttributeValue httpsPortAttrVal = configInfo.getAttributeValue
                (Constants.ANN_CONFIG_ATTR_HTTPS_PORT);
        AnnAttributeValue keyStoreFileAttrVal = configInfo.getAttributeValue
                (Constants.ANN_CONFIG_ATTR_KEY_STORE_FILE);
        AnnAttributeValue keyStorePassAttrVal = configInfo.getAttributeValue
                (Constants.ANN_CONFIG_ATTR_KEY_STORE_PASS);
        AnnAttributeValue certPassAttrVal = configInfo.getAttributeValue
                (Constants.ANN_CONFIG_ATTR_CERT_PASS);

        if (portAttrVal != null && portAttrVal.getIntValue() > 0) {
            Map<String, String> httpPropMap = new HashMap<>();
            httpPropMap.put(Constants.ANN_CONFIG_ATTR_PORT, Long.toString(portAttrVal.getIntValue()));
            httpPropMap.put(Constants.ANN_CONFIG_ATTR_SCHEME, Constants.PROTOCOL_HTTP);
            if (hostAttrVal != null && hostAttrVal.getStringValue() != null) {
                httpPropMap.put(Constants.ANN_CONFIG_ATTR_HOST, hostAttrVal.getStringValue());
            } else {
                httpPropMap.put(Constants.ANN_CONFIG_ATTR_HOST, Constants.HTTP_DEFAULT_HOST);
            }
            listenerConfMap.put(buildInterfaceName(httpPropMap), httpPropMap);
        }

        if (httpsPortAttrVal != null && httpsPortAttrVal.getIntValue() > 0) {
            Map<String, String> httpsPropMap = new HashMap<>();
            httpsPropMap.put(Constants.ANN_CONFIG_ATTR_PORT, Long.toString(httpsPortAttrVal.getIntValue()));
            httpsPropMap.put(Constants.ANN_CONFIG_ATTR_SCHEME, Constants.PROTOCOL_HTTPS);
            if (hostAttrVal != null && hostAttrVal.getStringValue() != null) {
                httpsPropMap.put(Constants.ANN_CONFIG_ATTR_HOST, hostAttrVal.getStringValue());
            } else {
                httpsPropMap.put(Constants.ANN_CONFIG_ATTR_HOST, Constants.HTTP_DEFAULT_HOST);
            }
            if (keyStoreFileAttrVal == null || keyStoreFileAttrVal.getStringValue() == null) {
                //TODO get from language pack, and add location
                throw new BallerinaException("Keystore location must be provided for protocol https");
            }
            if (keyStorePassAttrVal == null || keyStorePassAttrVal.getStringValue() == null) {
                //TODO get from language pack, and add location
                throw new BallerinaException("Keystore password value must be provided for protocol https");
            }
            if (certPassAttrVal == null || certPassAttrVal.getStringValue() == null) {
                //TODO get from language pack, and add location
                throw new BallerinaException("Certificate password value must be provided for protocol https");
            }
            httpsPropMap.put(Constants.ANN_CONFIG_ATTR_KEY_STORE_FILE, keyStoreFileAttrVal.getStringValue());
            httpsPropMap.put(Constants.ANN_CONFIG_ATTR_KEY_STORE_PASS, keyStorePassAttrVal.getStringValue());
            httpsPropMap.put(Constants.ANN_CONFIG_ATTR_CERT_PASS, certPassAttrVal.getStringValue());
            listenerConfMap.put(buildInterfaceName(httpsPropMap), httpsPropMap);
        }
        return listenerConfMap;
    }

    //TODO use methods from ballerina once available.
    private String discoverBasePathFrom(ServiceInfo service, AnnAttachmentInfo annotationInfo) {
        String basePath = service.getName();
        if (annotationInfo != null) {
            AnnAttributeValue annAttributeValue = annotationInfo.getAttributeValue
                    (Constants.ANN_CONFIG_ATTR_BASE_PATH);
            if (annAttributeValue != null && annAttributeValue.getStringValue() != null) {
                if (annAttributeValue.getStringValue().trim().isEmpty()) {
                    basePath = Constants.DEFAULT_BASE_PATH;
                } else {
                    basePath = annAttributeValue.getStringValue();
                }
            }
        }
        if (!basePath.startsWith(Constants.DEFAULT_BASE_PATH)) {
            basePath = Constants.DEFAULT_BASE_PATH.concat(basePath);
        }
        return basePath;
    }

    /**
     * Build interface name using schema and port.
     *
     * TODO use methods from ballerina once available.
     * @param propMap which has schema and port
     * @return interfaceName
     */
    private String buildInterfaceName(Map<String, String> propMap) {
        StringBuilder iName = new StringBuilder();
        iName.append(propMap.get(Constants.ANN_CONFIG_ATTR_SCHEME));
        iName.append("_");
        iName.append(propMap.get(Constants.ANN_CONFIG_ATTR_HOST));
        iName.append("_");
        iName.append(propMap.get(Constants.ANN_CONFIG_ATTR_PORT));
        return iName.toString();
    }

    //TODO use methods from ballerina once available.
    private Set<ListenerConfiguration> getListenerConfigurationsFrom(Map<String, Map<String, String>> listenerProp) {
        Set<ListenerConfiguration> listenerConfigurationSet = new HashSet<>();
        for (Map.Entry<String, Map<String, String>> entry : listenerProp.entrySet()) {
            Map<String, String> propMap = entry.getValue();
            String entryListenerInterface = getListenerInterface(propMap);
            ListenerConfiguration listenerConfiguration = HTTPMessageUtil
                    .buildListenerConfig(entryListenerInterface, propMap);
            listenerConfigurationSet.add(listenerConfiguration);
        }
        return listenerConfigurationSet;
    }

    //TODO use methods from ballerina once available.
    private String getListenerInterface(Map<String, String> parameters) {
        String host = parameters.get("host") != null ? parameters.get("host") : "0.0.0.0";
        int port = Integer.parseInt(parameters.get("port"));
        return host + ":" + port;
    }

}
