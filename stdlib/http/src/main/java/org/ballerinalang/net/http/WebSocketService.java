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

import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.http.exception.WebSocketException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket service for service dispatching.
 */
public class WebSocketService {

    private final ObjectValue service;
    private String[] negotiableSubProtocols = null;
    private int idleTimeoutInSeconds = 0;
    private final Map<String, AttachedFunction> resourceMap = new ConcurrentHashMap<>();
    private String basePath;
    private HttpResource upgradeResource;
    private int maxFrameSize = WebSocketConstants.DEFAULT_MAX_FRAME_SIZE;
    private Scheduler scheduler;

    public WebSocketService(Scheduler scheduler) {
        this.scheduler = scheduler;
        service = null;
    }

    @SuppressWarnings("unchecked")
    public WebSocketService(ObjectValue service, Scheduler scheduler) {
        this.scheduler = scheduler;
        this.service = service;
        for (AttachedFunction resource : service.getType().getAttachedFunctions()) {
            resourceMap.put(resource.getName(), resource);
        }

        BType paramType = service.getType().getAttachedFunctions()[0].getParameterType()[0];
        MapValue configAnnotation = WebSocketUtil.getServiceConfigAnnotation(service);

        if (paramType != null && WebSocketConstants.WEBSOCKET_CALLER_NAME.equals(paramType.toString())) {
            MapValue configAnnotationMap;
            if ((configAnnotationMap = configAnnotation) != null) {
                negotiableSubProtocols = WebSocketUtil.findNegotiableSubProtocols(configAnnotationMap);
                idleTimeoutInSeconds = WebSocketUtil.findIdleTimeoutInSeconds(configAnnotationMap);
                maxFrameSize = WebSocketUtil.findMaxFrameSize(configAnnotationMap);
            }

            basePath = findFullWebSocketUpgradePath(configAnnotationMap);
        }

    }

    public WebSocketService(String httpBasePath, HttpResource upgradeResource, ObjectValue service,
                            Scheduler scheduler) {
        this(service, scheduler);
        MapValue resourceConfigAnnotation = HttpResource.getResourceConfigAnnotation(
                upgradeResource.getBalResource());
        if (resourceConfigAnnotation == null) {
            throw new WebSocketException("Cannot find a resource config for resource " + upgradeResource.getName());
        }
        MapValue webSocketConfig =
                resourceConfigAnnotation.getMapValue(HttpConstants.ANN_CONFIG_ATTR_WEBSOCKET_UPGRADE);
        String upgradePath = webSocketConfig.getStringValue(HttpConstants.ANN_WEBSOCKET_ATTR_UPGRADE_PATH);
        this.basePath = httpBasePath.concat(upgradePath);
        this.upgradeResource = upgradeResource;
    }

    public String getName() {
        if (service != null) {
            // With JBallerina this is the way to get the key
            String name = HttpUtil.getServiceName(service);
            return !name.startsWith(HttpConstants.DOLLAR) ? name : "";
        }
        return null;
    }

    public AttachedFunction getResourceByName(String resourceName) {
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
    private String findFullWebSocketUpgradePath(MapValue annotation) {
        String path = null;
        if (annotation != null) {
            String basePathVal = annotation.getStringValue(WebSocketConstants.ANNOTATION_ATTR_PATH);
            if (!basePathVal.trim().isEmpty()) {
                path = HttpUtil.sanitizeBasePath(basePathVal);
            }
        }
        if (path == null) {
            path = "/".concat(getName());
        }
        return path;
    }

    public ObjectValue getBalService() {
        //TODO check service = null scenarios
        return service;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }
}
