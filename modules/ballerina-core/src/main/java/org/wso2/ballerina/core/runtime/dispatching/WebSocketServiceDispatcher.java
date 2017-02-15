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

package org.wso2.ballerina.core.runtime.dispatching;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.Service;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Service Dispatcher for WebSocket Endpoint.
 *
 * @since 0.8.0
 */
public class WebSocketServiceDispatcher implements ServiceDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketServiceDispatcher.class);
    private Map<String, Service> services = new HashMap<>();

    @Override
    public Service findService(CarbonMessage cMsg, CarbonCallback callback, Context balContext) {
        String interfaceId = (String) cMsg.getProperty(org.wso2.carbon.messaging.Constants.LISTENER_INTERFACE_ID);
        if (interfaceId == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Interface id not found on the message, hence using the default interface");
            }
        }

        String serviceUri = (String) cMsg.getProperty(org.wso2.carbon.messaging.Constants.TO);
        if (serviceUri == null) {
            throw new BallerinaException("No service found to dispatch");
        }

        serviceUri = refactorUri(serviceUri);

        Service service = services.get(serviceUri);

        if (service == null) {
            throw new BallerinaException("No service found to handle message for " + serviceUri);
        }

        return service;
    }

    @Override
    public String getProtocol() {
        return Constants.PROTOCOL_WEBSOCKET;
    }

    @Override
    public void serviceRegistered(Service service) {
        String websocketUri = findWebSocketPath(service);
        if (websocketUri != null) {
            services.put(websocketUri, service);
        }
    }

    @Override
    public void serviceUnregistered(Service service) {
        String websocketUri = findWebSocketPath(service);
        if (websocketUri != null) {
            services.remove(websocketUri);
        }
    }

    private String findWebSocketPath(Service service) {
        Annotation websocketUpgradePathAnnotation = null;
        Annotation basePathAnnotation = null;
        Annotation[] annotations = service.getAnnotations();
        for (Annotation annotation: annotations) {
            if (annotation.getName().equals(Constants.ANNOTATION_NAME_BASE_PATH)) {
                basePathAnnotation = annotation;
            } else if (annotation.getName().equals(Constants.ANNOTATION_NAME_WEBSOCKET_UPGRADE_PATH)) {
                websocketUpgradePathAnnotation = annotation;
            }
        }
        if (websocketUpgradePathAnnotation != null && websocketUpgradePathAnnotation.getValue() != null) {
            if (basePathAnnotation == null) {
                throw new BallerinaException("Cannot define @WebSocketPathUpgrade without @BasePath");
            }

            String basePath = refactorUri(basePathAnnotation.getValue());
            String websocketUpgradePath = refactorUri(websocketUpgradePathAnnotation.getValue());

            return basePath.concat(websocketUpgradePath);
        }

        return null;
    }

    private String refactorUri(String uri) {
        if (uri.startsWith("\"")) {
            uri = uri.substring(1, uri.length() - 1);
        }

        if (!uri.startsWith("/")) {
            uri = "/".concat(uri);
        }

        if (uri.endsWith("/")) {
            uri = uri.substring(0, uri.length() - 1);
        }
        return uri;
    }
}
