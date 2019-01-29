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
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.connector.api.Struct;
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
    private int maxFrameSize = WebSocketConstants.DEFAULT_MAX_FRAME_SIZE;

    public WebSocketService() {
        service = null;
    }

    public WebSocketService(Service service) {
        this.service = service;
        for (Resource resource : service.getResources()) {
            resourceMap.put(resource.getName(), resource);
        }

        ParamDetail param = service.getResources()[0].getParamDetails().get(0);
        Annotation configAnnotation = WebSocketUtil.getServiceConfigAnnotation(service);

        if (param != null && WebSocketConstants.WEBSOCKET_CALLER_NAME.equals(param.getVarType().toString())) {
            Struct configAnnotationStruct = null;
            if (configAnnotation != null && (configAnnotationStruct = configAnnotation.getValue()) != null) {
                negotiableSubProtocols = WebSocketUtil.findNegotiableSubProtocols(configAnnotationStruct);
                idleTimeoutInSeconds = WebSocketUtil.findIdleTimeoutInSeconds(configAnnotationStruct);
                maxFrameSize = WebSocketUtil.findMaxFrameSize(configAnnotationStruct);
            }

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
        if (service != null) {
            String name = service.getName();
            return !name.startsWith(HttpConstants.DOLLAR) ? name : "";
        }
        return null;
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
