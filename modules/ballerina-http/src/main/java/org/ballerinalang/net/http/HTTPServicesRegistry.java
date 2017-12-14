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

import org.ballerinalang.connector.api.AnnAttrValue;
import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.net.uri.DispatcherUtil;
import org.ballerinalang.net.uri.URITemplateException;
import org.ballerinalang.net.ws.WebSocketServicesRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.config.ListenerConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * This services registry holds all the services of HTTP + WebSocket.
 * This is a singleton class where all HTTP + WebSocket Dispatchers can access.
 *
 * @since 0.8
 */
public class HTTPServicesRegistry {

    private static final Logger logger = LoggerFactory.getLogger(HTTPServicesRegistry.class);

    // Outer Map key=interface, Inner Map key=basePath
    private final Map<String, Map<String, HttpService>> servicesInfoMap = new ConcurrentHashMap<>();
    private CopyOnWriteArrayList<String> sortedServiceURIs = new CopyOnWriteArrayList<>();
    private final WebSocketServicesRegistry webSocketServicesRegistry;

    public HTTPServicesRegistry(WebSocketServicesRegistry webSocketServicesRegistry) {
        this.webSocketServicesRegistry = webSocketServicesRegistry;
    }

    /**
     * Get ServiceInfo isntance for given interface and base path.
     *
     * @param interfaceId interface id of the service.
     * @param basepath    basepath of the service.
     * @return the {@link HttpService} instance if exist else null
     */
    public HttpService getServiceInfo(String interfaceId, String basepath) {
        return servicesInfoMap.get(interfaceId).get(basepath);
    }

    /**
     * Get ServiceInfo map for given interfaceId.
     *
     * @param interfaceId interfaceId interface id of the services.
     * @return the serviceInfo map if exists else null.
     */
    public Map<String, HttpService> getServicesInfoByInterface(String interfaceId) {
        return servicesInfoMap.get(interfaceId);
    }

    /**
     * Register a service into the map.
     *
     * @param service requested serviceInfo to be registered.
     */
    public void registerService(HttpService service) {
        Annotation annotation = service.getBalService()
                .getAnnotation(Constants.HTTP_PACKAGE_PATH, Constants.ANN_NAME_CONFIG);

        String basePath = discoverBasePathFrom(service, annotation);
        service.setBasePath(basePath);
        Set<ListenerConfiguration> listenerConfigurationSet = HttpUtil.getDefaultOrDynamicListenerConfig(annotation);

        for (ListenerConfiguration listenerConfiguration : listenerConfigurationSet) {
            String entryListenerInterface = listenerConfiguration.getHost() + ":" + listenerConfiguration.getPort();
            Map<String, HttpService> servicesOnInterface = servicesInfoMap
                    .computeIfAbsent(entryListenerInterface, k -> new HashMap<>());

            HttpConnectionManager.getInstance().createHttpServerConnector(listenerConfiguration);
            // Assumption : this is always sequential, no two simultaneous calls can get here
            if (servicesOnInterface.containsKey(basePath)) {
                throw new BallerinaConnectorException(
                        "service with base path :" + basePath + " already exists in listener : "
                                + entryListenerInterface);
            }
            servicesOnInterface.put(basePath, service);

            // If WebSocket upgrade path is available, then register the name of the WebSocket service.
            if (annotation != null) {
                AnnAttrValue webSocketAttr = annotation.getAnnAttrValue(Constants.ANN_CONFIG_ATTR_WEBSOCKET);
                if (webSocketAttr != null) {
                    Annotation webSocketAnn = webSocketAttr.getAnnotation();
                    registerWebSocketUpgradePath(webSocketAnn, basePath, entryListenerInterface);
                }
            }
        }
        logger.info("Service deployed : " + service.getName() + " with context " + basePath);
        postProcessService(service);
    }

    private String discoverBasePathFrom(HttpService service, Annotation annotation) {
        String basePath = service.getName();
        if (annotation == null) {
            //service name cannot start with / hence concat
            return Constants.DEFAULT_BASE_PATH.concat(basePath);
        }
        AnnAttrValue annotationValue = annotation.getAnnAttrValue(Constants.ANN_CONFIG_ATTR_BASE_PATH);
        if (annotationValue == null || annotationValue.getStringValue() == null) {
            return Constants.DEFAULT_BASE_PATH.concat(basePath);
        }
        if (!annotationValue.getStringValue().trim().isEmpty()) {
            basePath = annotationValue.getStringValue();
        } else {
            basePath = Constants.DEFAULT_BASE_PATH;
        }
        return sanitizeBasePath(basePath);
    }

    private String sanitizeBasePath(String basePath) {
        basePath = basePath.trim();
        if (!basePath.startsWith(Constants.DEFAULT_BASE_PATH)) {
            basePath = Constants.DEFAULT_BASE_PATH.concat(basePath);
        }
        if (basePath.endsWith(Constants.DEFAULT_BASE_PATH) && basePath.length() != 1) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }
        return basePath;
    }

    private void registerWebSocketUpgradePath(Annotation webSocketAnn, String basePath, String serviceInterface) {
        String upgradePath = sanitizeBasePath(
                webSocketAnn.getAnnAttrValue(Constants.ANN_WEBSOCKET_ATTR_UPGRADE_PATH).getStringValue());
        String serviceName =
                webSocketAnn.getAnnAttrValue(Constants.ANN_WEBSOCKET_ATTR_SERVICE_NAME).getStringValue().trim();
        String uri = basePath.concat(upgradePath);
        webSocketServicesRegistry.addUpgradableServiceByName(serviceInterface, uri, serviceName);
    }

    private void postProcessService(HttpService httpService) {
        CorsPopulator.populateServiceCors(httpService);
        List<HttpResource> resources = new ArrayList<>();
        for (Resource resource : httpService.getBalerinaService().getResources()) {
            HttpResource httpResource = buildHttpResource(resource);
            validateResourceSignature(httpResource);
            try {
                httpService.getUriTemplate().parse(httpResource.getPath(), httpResource,
                                                   new HttpResourceElementFactory());
            } catch (URITemplateException e) {
                throw new BallerinaConnectorException(e.getMessage());
            }
            CorsPopulator.processResourceCors(httpResource, httpService);
            resources.add(httpResource);
        }
        httpService.setResources(resources);
        httpService.setAllAllowMethods(DispatcherUtil.getAllResourceMethods(httpService));
        //basePath will get cached after registering service
        sortedServiceURIs.add(httpService.getBasePath());
        sortedServiceURIs.sort((basePath1, basePath2) -> basePath2.length() - basePath1.length());
    }

    private HttpResource buildHttpResource(Resource resource) {
        HttpResource httpResource = new HttpResource(resource);
        Annotation rConfigAnnotation = resource.getAnnotation(Constants.HTTP_PACKAGE_PATH,
                                                              Constants.ANN_NAME_RESOURCE_CONFIG);
        if (rConfigAnnotation == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("resourceConfig not specified in the Resource, using default sub path");
            }
            httpResource.setPath(resource.getName());
            return httpResource;
        }
        String subPath;
        AnnAttrValue pathAttrVal = rConfigAnnotation.getAnnAttrValue(Constants.ANN_RESOURCE_ATTR_PATH);
        if (pathAttrVal == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Path not specified in the Resource, using default sub path");
            }
            subPath = resource.getName();
        } else {
            subPath = pathAttrVal.getStringValue().trim();
        }
        if (subPath.isEmpty()) {
            subPath = Constants.DEFAULT_BASE_PATH;
        }
        httpResource.setPath(subPath);

        AnnAttrValue methodsAttrVal = rConfigAnnotation.getAnnAttrValue(Constants.ANN_RESOURCE_ATTR_METHODS);
        if (methodsAttrVal != null) {
            httpResource.setMethods(DispatcherUtil.getValueList(methodsAttrVal, null));
        }
        AnnAttrValue consumesAttrVal = rConfigAnnotation.getAnnAttrValue(Constants.ANN_RESOURCE_ATTR_CONSUMES);
        if (consumesAttrVal != null) {
            httpResource.setConsumes(DispatcherUtil.getValueList(consumesAttrVal, null));
        }
        AnnAttrValue producesAttrVal = rConfigAnnotation.getAnnAttrValue(Constants.ANN_RESOURCE_ATTR_PRODUCES);
        if (producesAttrVal != null) {
            httpResource.setProduces(DispatcherUtil.getValueList(producesAttrVal, null));
        }
        if (httpResource.getProduces() != null) {
            List<String> subAttributeValues = httpResource.getProduces().stream()
                    .map(mediaType -> mediaType.trim()
                            .substring(0, mediaType.indexOf("/")))
                    .distinct().collect(Collectors.toList());
            httpResource.setProducesSubTypes(subAttributeValues);
        }
        return httpResource;
    }

    private void validateResourceSignature(HttpResource resource) {
        List<ParamDetail> paramDetails = resource.getParamDetails();

        if (paramDetails.size() < 2) {
            throw new BallerinaConnectorException("resource signature parameter count should be more than two");
        }

        //Validate request parameter
        ParamDetail reqParamDetail = paramDetails.get(0);
        if (reqParamDetail == null) {
            throw new BallerinaConnectorException("request parameter cannot be null");
        }
        if (reqParamDetail.getVarType().getPackagePath() == null
                || !reqParamDetail.getVarType().getPackagePath().equals(Constants.PROTOCOL_PACKAGE_HTTP)
                || !reqParamDetail.getVarType().getName().equals(Constants.REQUEST)) {
            throw new BallerinaConnectorException("request parameter should be of type - "
                                                          + Constants.PROTOCOL_PACKAGE_HTTP + ":" + Constants.REQUEST);
        }

        //validate response parameter
        ParamDetail respParamDetail = paramDetails.get(1);
        if (respParamDetail == null) {
            throw new BallerinaConnectorException("response parameter cannot be null");
        }
        if (respParamDetail.getVarType().getPackagePath() == null
                || !respParamDetail.getVarType().getPackagePath().equals(Constants.PROTOCOL_PACKAGE_HTTP)
                || !respParamDetail.getVarType().getName().equals(Constants.RESPONSE)) {
            throw new BallerinaConnectorException("response parameter should be of type - "
                                                          + Constants.PROTOCOL_PACKAGE_HTTP + ":" + Constants.REQUEST);
        }

        //validate rest of the parameters
        for (int i = 2; i < paramDetails.size(); i++) {
            ParamDetail paramDetail = paramDetails.get(i);
            if (!paramDetail.getVarType().getName().equals(Constants.TYPE_STRING)) {
                throw new BallerinaConnectorException("incompatible resource signature parameter type");
            }
        }
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
        if (services.containsKey(Constants.DEFAULT_BASE_PATH)) {
            return Constants.DEFAULT_BASE_PATH;
        }
        return null;
    }
}
