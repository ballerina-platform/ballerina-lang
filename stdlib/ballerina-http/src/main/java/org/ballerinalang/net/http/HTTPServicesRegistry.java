/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.ballerinalang.net.http;

import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.connector.api.Value;
import org.ballerinalang.logging.BLogManager;
import org.ballerinalang.net.uri.DispatcherUtil;
import org.ballerinalang.net.uri.URITemplateException;
import org.ballerinalang.util.codegen.ProgramFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This services registry holds all the services of HTTP + WebSocket. This is a singleton class where all HTTP +
 * WebSocket Dispatchers can access.
 *
 * @since 0.8
 */
public class HTTPServicesRegistry {

    private static final Logger logger = LoggerFactory.getLogger(HTTPServicesRegistry.class);

    // Outer Map key=basePath
    private final Map<String, HttpService> servicesInfoMap = new ConcurrentHashMap<>();
    private CopyOnWriteArrayList<String> sortedServiceURIs = new CopyOnWriteArrayList<>();
    private final WebSocketServicesRegistry webSocketServicesRegistry;

    public HTTPServicesRegistry(WebSocketServicesRegistry webSocketServicesRegistry) {
        this.webSocketServicesRegistry = webSocketServicesRegistry;
    }

    /**
     * Get ServiceInfo isntance for given interface and base path.
     *
     * @param basepath basepath of the service.
     * @return the {@link HttpService} instance if exist else null
     */
    public HttpService getServiceInfo(String basepath) {
        return servicesInfoMap.get(basepath);
    }

    /**
     * Get ServiceInfo map for given interfaceId.
     *
     * @return the serviceInfo map if exists else null.
     */
    public Map<String, HttpService> getServicesInfoByInterface() {
        return servicesInfoMap;
    }

    /**
     * Register a service into the map.
     *
     * @param service requested serviceInfo to be registered.
     */
    public void registerService(HttpService service) {
        String accessLogConfig = HttpConnectionManager.getInstance().getHttpAccessLoggerConfig();
        if (accessLogConfig != null) {
            try {
                ((BLogManager) BLogManager.getLogManager()).setHttpAccessLogHandler(accessLogConfig);
            } catch (IOException e) {
                throw new BallerinaConnectorException("Invalid file path: " + accessLogConfig, e);
            }
        }

        Annotation annotation = HttpUtil.getServiceConfigAnnotation(service.getBalService(),
                                                                    HttpConstants.HTTP_PACKAGE_PATH);

        Struct serviceConfig = annotation.getValue();
        String basePath = discoverBasePathFrom(service, annotation);
        //TODO check with new method
//        HttpUtil.populateKeepAliveAndCompressionStatus(service, annotation);

        basePath = urlDecode(basePath);
        service.setBasePath(basePath);
        // TODO: Add websocket services to the service registry when service creation get available.
        servicesInfoMap.put(service.getBasePath(), service);
        logger.info("Service deployed : " + service.getName() + " with context " + basePath);
        postProcessService(service, serviceConfig);
        // If WebSocket upgrade path is available, then register the name of the WebSocket service.

        Struct websocketConfig = serviceConfig.getStructField(WebSocketConstants.WEBSOCKET_UPGRADE_CONFIG);
        if (websocketConfig != null) {
            registerWebSocketUpgradePath(WebSocketUtil.getProgramFile(service.getBalerinaService().getResources()[0]),
                                         websocketConfig, basePath);
        }

    }

    public List<String> populateListFromValueArray(Value[] valueArray) {
        List<String> list = new ArrayList<>();
        if (valueArray != null) {
            for (Value method : valueArray) {
                list.add(method.getStringValue());
            }
        }
        return list;
    }

    private String discoverBasePathFrom(HttpService service, Annotation annotation) {
        String basePath = service.getName();
        if (annotation == null) {
            //service name cannot start with / hence concat
            return HttpConstants.DEFAULT_BASE_PATH.concat(basePath);
        }

        Struct annStruct = annotation.getValue();
        String annotationValue = annStruct.getStringField(HttpConstants.ANN_CONFIG_ATTR_BASE_PATH);
        if (annotationValue == null) {
            return HttpConstants.DEFAULT_BASE_PATH.concat(basePath);
        }
        if (!annotationValue.trim().isEmpty()) {
            basePath = annotationValue;
        } else {
            basePath = HttpConstants.DEFAULT_BASE_PATH;
        }
        return sanitizeBasePath(basePath);
    }

    private String sanitizeBasePath(String basePath) {
        basePath = basePath.trim();
        if (!basePath.startsWith(HttpConstants.DEFAULT_BASE_PATH)) {
            basePath = HttpConstants.DEFAULT_BASE_PATH.concat(basePath);
        }
        if (basePath.endsWith(HttpConstants.DEFAULT_BASE_PATH) && basePath.length() != 1) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }
        return basePath;
    }

    private void registerWebSocketUpgradePath(ProgramFile programFile, Struct websocketConfig, String basePath) {
        String upgradePath = sanitizeBasePath(
                websocketConfig.getStringField(HttpConstants.ANN_WEBSOCKET_ATTR_UPGRADE_PATH));
        Value serviceType = websocketConfig.getTypeField(WebSocketConstants.WEBSOCKET_UPGRADE_SERVICE_CONFIG);
        String uri = basePath.concat(upgradePath);
        webSocketServicesRegistry.addUpgradableServiceByName(
                new WebSocketService(BLangConnectorSPIUtil.getServiceFromType(programFile, serviceType)), uri);
    }

    private void postProcessService(HttpService httpService, Struct serviceConfig) {
        populateServiceCors(httpService, serviceConfig);
        List<HttpResource> resources = new ArrayList<>();
        for (Resource resource : httpService.getBalerinaService().getResources()) {
            HttpResource httpResource = HttpResource.buildHttpResource(resource, httpService);
            httpResource.prepareAndValidateSignatureParams();
            try {
                httpService.getUriTemplate().parse(httpResource.getPath(), httpResource,
                                                   new HttpResourceElementFactory());
            } catch (URITemplateException | UnsupportedEncodingException e) {
                throw new BallerinaConnectorException(e.getMessage());
            }
            resources.add(httpResource);
        }
        httpService.setResources(resources);
        httpService.setAllAllowMethods(DispatcherUtil.getAllResourceMethods(httpService));
        //basePath will get cached after registering service
        sortedServiceURIs.add(httpService.getBasePath());
        sortedServiceURIs.sort((basePath1, basePath2) -> basePath2.length() - basePath1.length());
    }

    private String urlDecode(String basePath) {
        try {
            basePath = URLDecoder.decode(basePath, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new BallerinaConnectorException(e.getMessage());
        }
        return basePath;
    }

    public String findTheMostSpecificBasePath(String requestURIPath, Map<String, HttpService> services) {
        for (Object key : sortedServiceURIs) {
            if (!requestURIPath.toLowerCase().contains(key.toString().toLowerCase())) {
                continue;
            }
            if (requestURIPath.length() <= key.toString().length()) {
                return key.toString();
            }
            if (requestURIPath.charAt(key.toString().length()) == '/') {
                return key.toString();
            }
        }
        if (services.containsKey(HttpConstants.DEFAULT_BASE_PATH)) {
            return HttpConstants.DEFAULT_BASE_PATH;
        }
        return null;
    }

    private void populateServiceCors(HttpService service, Struct serviceConfig) {
        CorsHeaders corsHeaders = populateAndGetCorsHeaders(serviceConfig);
        service.setCorsHeaders(corsHeaders);
        if (!corsHeaders.isAvailable()) {
            return;
        }
        if (corsHeaders.getAllowOrigins() == null) {
            corsHeaders.setAllowOrigins(Stream.of("*").collect(Collectors.toList()));
        }
        if (corsHeaders.getAllowMethods() == null) {
            corsHeaders.setAllowMethods(Stream.of("*").collect(Collectors.toList()));
        }
    }

    private CorsHeaders populateAndGetCorsHeaders(Struct serviceConfig) {
        CorsHeaders corsHeaders = new CorsHeaders();
        if (serviceConfig == null) {
            return corsHeaders;
        }
        if (serviceConfig.getArrayField(HttpConstants.ALLOW_ORIGIN) != null) {
            Value[] allowedMethods = serviceConfig.getArrayField(HttpConstants.ALLOW_ORIGIN);
            corsHeaders.setAllowOrigins(populateListFromValueArray(allowedMethods));
        }

        corsHeaders.setAllowCredentials(serviceConfig.getBooleanField("allowCredentials") ? 1 : 0);


        if (serviceConfig.getArrayField(HttpConstants.ALLOW_METHODS) != null) {
            Value[] allowedMethods = serviceConfig.getArrayField(HttpConstants.ALLOW_METHODS);
            corsHeaders.setAllowMethods(populateListFromValueArray(allowedMethods));
        }

        if (serviceConfig.getArrayField(HttpConstants.ALLOW_HEADERS) != null) {
            Value[] allowedMethods = serviceConfig.getArrayField(HttpConstants.ALLOW_HEADERS);
            corsHeaders.setAllowHeaders(populateListFromValueArray(allowedMethods));
        }

        long maxAge = serviceConfig.getIntField("maxAge");
        corsHeaders.setMaxAge(maxAge > 0 ? maxAge : -1);


        if (serviceConfig.getArrayField(HttpConstants.EXPOSE_HEADERS) != null) {
            Value[] allowedMethods = serviceConfig.getArrayField(HttpConstants.EXPOSE_HEADERS);
            corsHeaders.setExposeHeaders(populateListFromValueArray(allowedMethods));
        }

        return corsHeaders;
    }
}
