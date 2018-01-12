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
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.impl.ServerConnectorRegistry;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.Constants;
import org.ballerinalang.net.http.HttpConnectionManager;
import org.ballerinalang.testerina.core.TesterinaRegistry;
import org.ballerinalang.util.codegen.AnnAttachmentInfo;
import org.ballerinalang.util.codegen.AnnAttributeValue;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;
import org.wso2.transport.http.netty.config.ChunkConfig;
import org.wso2.transport.http.netty.config.ListenerConfiguration;
import org.wso2.transport.http.netty.config.Parameter;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Native function ballerina.lang.test:startServer.
 *
 * @since 0.8.0
 */
@BallerinaFunction(packageName = "ballerina.test",
                   functionName = "startService", args = {
        @Argument(name = "serviceName", type = TypeKind.STRING)}, returnType = {
        @ReturnType(type = TypeKind.STRING)}, isPublic = true)
@BallerinaAnnotation(annotationName = "Description",
                     attributes = { @Attribute(name = "value",
                                               value = "Starts the service specified in the 'serviceName' argument") })
@BallerinaAnnotation(annotationName = "Param",
                     attributes = { @Attribute(name = "serviceName",
                                               value = "Name of the service to start") })
public class StartService extends AbstractNativeFunction {

    private static final String MSG_PREFIX = "test:startService: ";
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
        ServerConnectorRegistry serverConnectorRegistry = new ServerConnectorRegistry();
        serverConnectorRegistry.initServerConnectors();

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

        // Deploy service
        serverConnectorRegistry.registerService(matchingService);

        serviceCount++;

        if (serviceCount == 0) {
            throw new BallerinaException("no services found in '" + programFile.getProgramFilePath() + "'");
        }

        serverConnectorRegistry.deploymentComplete();
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
        if (annotationInfo == null) {
            return HttpConnectionManager.getInstance().getDefaultListenerConfiugrationSet();
        }
    
        //key - listenerId, value - listener config property map
        Set<ListenerConfiguration> listenerConfSet = new HashSet<>();
    
        extractBasicConfig(annotationInfo, listenerConfSet);
        extractHttpsConfig(annotationInfo, listenerConfSet);
    
        if (listenerConfSet.isEmpty()) {
            listenerConfSet = HttpConnectionManager.getInstance().getDefaultListenerConfiugrationSet();
        }
    
        return listenerConfSet;
    }
    
    private void extractBasicConfig(AnnAttachmentInfo configInfo, Set<ListenerConfiguration> listenerConfSet) {
        AnnAttributeValue hostAttrVal = configInfo.getAttributeValue(Constants.ANN_CONFIG_ATTR_HOST);
        AnnAttributeValue portAttrVal = configInfo.getAttributeValue(Constants.ANN_CONFIG_ATTR_PORT);
        AnnAttributeValue keepAliveAttrVal = configInfo.getAttributeValue(Constants.ANN_CONFIG_ATTR_KEEP_ALIVE);
        AnnAttributeValue transferEncoding = configInfo.getAttributeValue(Constants.ANN_CONFIG_ATTR_TRANSFER_ENCODING);
        AnnAttributeValue chunking = configInfo.getAttributeValue(Constants.ANN_CONFIG_ATTR_CHUNKING);
        
        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        if (portAttrVal != null && portAttrVal.getIntValue() > 0) {
            listenerConfiguration.setPort(Math.toIntExact(portAttrVal.getIntValue()));
            
            listenerConfiguration.setScheme(Constants.PROTOCOL_HTTP);
            if (hostAttrVal != null && hostAttrVal.getStringValue() != null) {
                listenerConfiguration.setHost(hostAttrVal.getStringValue());
            } else {
                listenerConfiguration.setHost(Constants.HTTP_DEFAULT_HOST);
            }
            
            if (keepAliveAttrVal != null) {
                listenerConfiguration.setKeepAlive(keepAliveAttrVal.getBooleanValue());
            } else {
                listenerConfiguration.setKeepAlive(Boolean.TRUE);
            }
            
            // For the moment we don't have to pass it down to transport as we only support
            // chunking. Once we start supporting gzip, deflate, etc, we need to parse down the config.
            if (transferEncoding != null && !Constants.ANN_CONFIG_ATTR_CHUNKING
                    .equalsIgnoreCase(transferEncoding.getStringValue())) {
                throw new BallerinaConnectorException("Unsupported configuration found for Transfer-Encoding : "
                                                      + transferEncoding.getStringValue());
            }
            
            if (chunking != null) {
                ChunkConfig chunkConfig = getChunkConfig(chunking.getStringValue());
                listenerConfiguration.setChunkConfig(chunkConfig);
            } else {
                listenerConfiguration.setChunkConfig(ChunkConfig.AUTO);
            }
            
            listenerConfiguration
                    .setId(getListenerInterface(listenerConfiguration.getHost(), listenerConfiguration.getPort()));
            listenerConfSet.add(listenerConfiguration);
        }
    }
    
    public ChunkConfig getChunkConfig(String chunking) {
        ChunkConfig chunkConfig;
        if (Constants.CHUNKING_AUTO.equalsIgnoreCase(chunking)) {
            chunkConfig = ChunkConfig.AUTO;
        } else if (Constants.CHUNKING_ALWAYS.equalsIgnoreCase(chunking)) {
            chunkConfig = ChunkConfig.ALWAYS;
        } else if (Constants.CHUNKING_NEVER.equalsIgnoreCase(chunking)) {
            chunkConfig = ChunkConfig.NEVER;
        } else {
            throw new BallerinaConnectorException("Invalid configuration found for Transfer-Encoding : " + chunking);
        }
        return chunkConfig;
    }
    
    private void extractHttpsConfig(AnnAttachmentInfo configInfo, Set<ListenerConfiguration> listenerConfSet) {
        // Retrieve secure port from either http of ws configuration annotation.
        AnnAttributeValue httpsPortAttrVal;
        if (configInfo.getAttributeValue(Constants.ANN_CONFIG_ATTR_HTTPS_PORT) == null) {
            httpsPortAttrVal =
                    configInfo.getAttributeValue(org.ballerinalang.net.ws.Constants.ANN_CONFIG_ATTR_WSS_PORT);
        } else {
            httpsPortAttrVal = configInfo.getAttributeValue(Constants.ANN_CONFIG_ATTR_HTTPS_PORT);
        }
        
        AnnAttributeValue keyStoreFileAttrVal = configInfo.getAttributeValue(Constants.ANN_CONFIG_ATTR_KEY_STORE_FILE);
        AnnAttributeValue keyStorePasswordAttrVal =
                                                configInfo.getAttributeValue(Constants.ANN_CONFIG_ATTR_KEY_STORE_PASS);
        AnnAttributeValue certPasswordAttrVal = configInfo.getAttributeValue(Constants.ANN_CONFIG_ATTR_CERT_PASS);
        AnnAttributeValue trustStoreFileAttrVal =
                                            configInfo.getAttributeValue(Constants.ANN_CONFIG_ATTR_TRUST_STORE_FILE);
        AnnAttributeValue trustStorePasswordAttrVal =
                                            configInfo.getAttributeValue(Constants.ANN_CONFIG_ATTR_TRUST_STORE_PASS);
        AnnAttributeValue sslVerifyClientAttrVal =
                                            configInfo.getAttributeValue(Constants.ANN_CONFIG_ATTR_SSL_VERIFY_CLIENT);
        AnnAttributeValue sslEnabledProtocolsAttrVal = configInfo
                .getAttributeValue(Constants.ANN_CONFIG_ATTR_SSL_ENABLED_PROTOCOLS);
        AnnAttributeValue ciphersAttrVal = configInfo.getAttributeValue(Constants.ANN_CONFIG_ATTR_CIPHERS);
        AnnAttributeValue sslProtocolAttrVal = configInfo.getAttributeValue(Constants.ANN_CONFIG_ATTR_SSL_PROTOCOL);
        AnnAttributeValue hostAttrVal = configInfo.getAttributeValue(Constants.ANN_CONFIG_ATTR_HOST);
        
        ListenerConfiguration listenerConfiguration = new ListenerConfiguration();
        if (httpsPortAttrVal != null && httpsPortAttrVal.getIntValue() > 0) {
            listenerConfiguration.setPort(Math.toIntExact(httpsPortAttrVal.getIntValue()));
            listenerConfiguration.setScheme(Constants.PROTOCOL_HTTPS);
            
            if (hostAttrVal != null && hostAttrVal.getStringValue() != null) {
                listenerConfiguration.setHost(hostAttrVal.getStringValue());
            } else {
                listenerConfiguration.setHost(Constants.HTTP_DEFAULT_HOST);
            }
            
            if (keyStoreFileAttrVal == null || keyStoreFileAttrVal.getStringValue() == null) {
                //TODO get from language pack, and add location
                throw new BallerinaConnectorException("Keystore location must be provided for secure connection");
            }
            if (keyStorePasswordAttrVal == null || keyStorePasswordAttrVal.getStringValue() == null) {
                //TODO get from language pack, and add location
                throw new BallerinaConnectorException("Keystore password value must be provided for secure connection");
            }
            if (certPasswordAttrVal == null || certPasswordAttrVal.getStringValue() == null) {
                //TODO get from language pack, and add location
                throw new BallerinaConnectorException(
                        "Certificate password value must be provided for secure connection");
            }
            if ((trustStoreFileAttrVal == null || trustStoreFileAttrVal.getStringValue() == null)
                && sslVerifyClientAttrVal != null) {
                //TODO get from language pack, and add location
                throw new BallerinaException("Truststore location must be provided to enable Mutual SSL");
            }
            if ((trustStorePasswordAttrVal == null || trustStorePasswordAttrVal.getStringValue() == null)
                && sslVerifyClientAttrVal != null) {
                //TODO get from language pack, and add location
                throw new BallerinaException("Truststore password value must be provided to enable Mutual SSL");
            }
            
            listenerConfiguration.setTLSStoreType(Constants.PKCS_STORE_TYPE);
            listenerConfiguration.setKeyStoreFile(keyStoreFileAttrVal.getStringValue());
            listenerConfiguration.setKeyStorePass(keyStorePasswordAttrVal.getStringValue());
            listenerConfiguration.setCertPass(certPasswordAttrVal.getStringValue());
            
            if (sslVerifyClientAttrVal != null) {
                listenerConfiguration.setVerifyClient(sslVerifyClientAttrVal.getStringValue());
            }
            if (trustStoreFileAttrVal != null) {
                listenerConfiguration.setTrustStoreFile(trustStoreFileAttrVal.getStringValue());
            }
            if (trustStorePasswordAttrVal != null) {
                listenerConfiguration.setTrustStorePass(trustStorePasswordAttrVal.getStringValue());
            }
            
            List<Parameter> serverParams = new ArrayList<>();
            Parameter serverCiphers;
            if (sslEnabledProtocolsAttrVal != null && sslEnabledProtocolsAttrVal.getStringValue() != null) {
                serverCiphers = new Parameter(Constants.ANN_CONFIG_ATTR_SSL_ENABLED_PROTOCOLS,
                        sslEnabledProtocolsAttrVal.getStringValue());
                serverParams.add(serverCiphers);
            }
            
            if (ciphersAttrVal != null && ciphersAttrVal.getStringValue() != null) {
                serverCiphers = new Parameter(Constants.ANN_CONFIG_ATTR_CIPHERS, ciphersAttrVal.getStringValue());
                serverParams.add(serverCiphers);
            }
            
            if (!serverParams.isEmpty()) {
                listenerConfiguration.setParameters(serverParams);
            }
            
            if (sslProtocolAttrVal != null) {
                listenerConfiguration.setSSLProtocol(sslProtocolAttrVal.getStringValue());
            }
            
            listenerConfiguration
                    .setId(getListenerInterface(listenerConfiguration.getHost(), listenerConfiguration.getPort()));
            listenerConfSet.add(listenerConfiguration);
        }
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

    //TODO use methods from ballerina once available.
    private String getListenerInterface(String host, int port) {
        host = host != null ? host : "0.0.0.0";
        return host + ":" + port;
    }

}
