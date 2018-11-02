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
 */

package org.ballerinalang.net.http;

import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.connector.api.Value;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket service for service dispatching.
 */
public class WebSocketService {

    private final Service service;
    private String[] negotiableSubProtocols = null;
    private int idleTimeoutInSeconds = 0;
    private final Map<String, Resource> resourceMap = new ConcurrentHashMap<>();
    private String basePath;
    private HttpResource upgradeResource;
    private static final int DEFAULT_MAX_FRAME_SIZE = 65536;
    private int maxFrameSize = DEFAULT_MAX_FRAME_SIZE;

    public WebSocketService() {
        service = null;
    }

    public WebSocketService(Service service) {
        this.service = service;
        for (Resource resource : service.getResources()) {
            resourceMap.put(resource.getName(), resource);
        }

        Annotation configAnnotation = WebSocketUtil.getServiceConfigAnnotation(service);

        Struct configAnnotationStruct = null;
        if (configAnnotation != null && (configAnnotationStruct = configAnnotation.getValue()) != null) {
            negotiableSubProtocols = findNegotiableSubProtocols(configAnnotationStruct);
            idleTimeoutInSeconds = findIdleTimeoutInSeconds(configAnnotationStruct);
            maxFrameSize = findMaxFrameSize(configAnnotationStruct);
        }
        if (WebSocketConstants.WEBSOCKET_ENDPOINT_NAME.equals(service.getEndpointName())) {
            basePath = findFullWebSocketUpgradePath(configAnnotationStruct);
        }

    }

    public WebSocketService(String httpBasePath, HttpResource upgradeResource, Service service) {
        this(service);
        Annotation resourceConfigAnnotation = HttpResource.getResourceConfigAnnotation(
                upgradeResource.getBalResource());
        if (resourceConfigAnnotation == null) {
            throw new BallerinaException("Cannot find a resource config for resource " + upgradeResource.getName());
        }
        Struct webSocketConfig =
                resourceConfigAnnotation.getValue().getStructField(HttpConstants.ANN_CONFIG_ATTR_WEBSOCKET_UPGRADE);
        String upgradePath = webSocketConfig.getStringField(HttpConstants.ANN_WEBSOCKET_ATTR_UPGRADE_PATH);
        this.basePath = httpBasePath.concat(upgradePath);
        this.upgradeResource = upgradeResource;
    }

    public String getName() {
        return service != null ? service.getName() : null;
    }

    public ServiceInfo getServiceInfo() {
        return service != null ? service.getServiceInfo() : null;
    }

    public Resource getResourceByName(String resourceName) {
        return resourceMap.get(resourceName);
    }

    public String[] getNegotiableSubProtocols() {
        return negotiableSubProtocols;
    }

    public HttpResource getUpgradeResource() {
        return upgradeResource;
    }

    public int getIdleTimeoutInSeconds() {
        return idleTimeoutInSeconds;
    }

    public int getMaxFrameSize() {
        return maxFrameSize;
    }

    private String[] findNegotiableSubProtocols(Struct annAttrSubProtocols) {
        Value[] subProtocolsInAnnotation = annAttrSubProtocols.getArrayField(
                WebSocketConstants.ANNOTATION_ATTR_SUB_PROTOCOLS);

        if (subProtocolsInAnnotation == null) {
            return new String[0];
        }

        String[] subProtoCols = new String[subProtocolsInAnnotation.length];
        for (int i = 0; i < subProtocolsInAnnotation.length; i++) {
            subProtoCols[i] = subProtocolsInAnnotation[i].getStringValue();
        }
        return subProtoCols;
    }

    private int findIdleTimeoutInSeconds(Struct annAttrIdleTimeout) {
        return (int) annAttrIdleTimeout.getIntField(WebSocketConstants.ANNOTATION_ATTR_IDLE_TIMEOUT);
    }

    private int findMaxFrameSize(Struct annotation) {
        int size = (int) annotation.getIntField(WebSocketConstants.ANNOTATION_ATTR_MAX_FRAME_SIZE);
        if (size <= 0) {
            size = DEFAULT_MAX_FRAME_SIZE;
        }
        return size;
    }

    public String getBasePath() {
        return basePath;
    }

    /**
     * Find the Full path for WebSocket upgrade.
     *
     * @return the full path of the WebSocket upgrade.
     */
    private String findFullWebSocketUpgradePath(Struct annStruct) {
        String path = null;
        if (annStruct != null) {
            String basePathVal = annStruct.getStringField(WebSocketConstants.ANNOTATION_ATTR_PATH);
            if (!basePathVal.trim().isEmpty()) {
                path = HttpUtil.sanitizeBasePath(basePathVal);
            }
        }
        if (path == null) {
            path = "/".concat(getName());
        }
        return path;
    }
}
