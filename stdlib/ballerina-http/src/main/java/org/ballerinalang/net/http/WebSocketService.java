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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket service for service dispatching.
 */
public class WebSocketService implements Service {

    private final Service service;
    private final String[] negotiableSubProtocols;
    private final int idleTimeoutInSeconds;
    private final int maxFrameSize;
    private final Map<String, Resource> resourceMap = new ConcurrentHashMap<>();
    private String basePath;
    private Resource upgradeResource;
    private static final int DEFAULT_MAX_FRAME_SIZE = 65536;

    public WebSocketService(Service service) {
        this.service = service;
        for (Resource resource : service.getResources()) {
            resourceMap.put(resource.getName(), resource);
        }

        Annotation configAnnotation =
                WebSocketUtil.getServiceConfigAnnotation(service, HttpConstants.PROTOCOL_PACKAGE_HTTP);
        Struct configAnnotationStruct;
        if (configAnnotation != null && (configAnnotationStruct = configAnnotation.getValue()) != null) {
            negotiableSubProtocols = findNegotiableSubProtocols(configAnnotationStruct);
            idleTimeoutInSeconds = findIdleTimeoutInSeconds(configAnnotationStruct);
            maxFrameSize = findMaxFrameSize(configAnnotationStruct);
        } else {
            negotiableSubProtocols = null;
            idleTimeoutInSeconds = 0;
            maxFrameSize = DEFAULT_MAX_FRAME_SIZE;
        }
        basePath = findFullWebSocketUpgradePath(this);
        upgradeResource = null;
    }

    public WebSocketService(String httpBasePath, Resource upgradeResource, Service service) {
        this(service);
        Annotation resourceConfigAnnotation = HttpResource.getResourceConfigAnnotation(upgradeResource);
        if (resourceConfigAnnotation == null) {
            throw new BallerinaException("Cannot find a resource config for resource " + upgradeResource.getName());
        }
        Struct webSocketConfig =
                resourceConfigAnnotation.getValue().getStructField(HttpConstants.ANN_CONFIG_ATTR_WEBSOCKET_UPGRADE);
        String upgradePath = webSocketConfig.getStringField(HttpConstants.ANN_WEBSOCKET_ATTR_UPGRADE_PATH);
        this.basePath = httpBasePath.concat(upgradePath);
        this.upgradeResource = upgradeResource;
    }

    @Override
    public String getName() {
        return service.getName();
    }

    @Override
    public String getPackage() {
        return service.getPackage();
    }

    @Override
    public String getEndpointName() {
        return service.getEndpointName();
    }

    @Override
    public List<Annotation> getAnnotationList(String pkgPath, String name) {
        return service.getAnnotationList(pkgPath, name);
    }

    @Override
    public Resource[] getResources() {
        return service.getResources();
    }

    @Override
    public ServiceInfo getServiceInfo() {
        return service.getServiceInfo();
    }

    @Override
    public String getPackageVersion() {
        return null;
    }

    public Resource getResourceByName(String resourceName) {
        return resourceMap.get(resourceName);
    }

    public String[] getNegotiableSubProtocols() {
        return negotiableSubProtocols;
    }

    public Resource getUpgradeResource() {
        return upgradeResource;
    }

    public int getIdleTimeoutInSeconds() {
        return idleTimeoutInSeconds;
    }

    public int getMaxFrameSize() {
        return maxFrameSize;
    }

    private String[] findNegotiableSubProtocols(Struct annAttrSubProtocols) {
        if (annAttrSubProtocols == null) {
            return null;
        }
        Value[] subProtocolsInAnnotation = annAttrSubProtocols.getArrayField(
                WebSocketConstants.ANNOTATION_ATTR_SUB_PROTOCOLS);

        if (subProtocolsInAnnotation == null) {
            return null;
        }

        String[] negotiableSubProtocols = new String[subProtocolsInAnnotation.length];
        for (int i = 0; i < subProtocolsInAnnotation.length; i++) {
            negotiableSubProtocols[i] = subProtocolsInAnnotation[i].getStringValue();
        }
        return negotiableSubProtocols;
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
     * @param service {@link WebSocketService} which the full path should be found.
     * @return the full path of the WebSocket upgrade.
     */
    private String findFullWebSocketUpgradePath(WebSocketService service) {
        // Find Base path for WebSocket
        Annotation configAnnotation = WebSocketUtil.getServiceConfigAnnotation(service,
                                                                               HttpConstants.PROTOCOL_PACKAGE_HTTP);
        String basePath = null;
        if (configAnnotation != null) {
            Struct annStruct = configAnnotation.getValue();
            String basePathVal = annStruct.getStringField(WebSocketConstants.ANNOTATION_ATTR_PATH);
            if (basePathVal != null && !basePathVal.trim().isEmpty()) {
                basePath = WebSocketUtil.refactorUri(basePathVal);
            }
        }

        if (basePath == null) {
            basePath = "/".concat(service.getName());
        }
        return basePath;
    }
}
