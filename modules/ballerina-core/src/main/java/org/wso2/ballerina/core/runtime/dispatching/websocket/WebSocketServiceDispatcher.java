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

package org.wso2.ballerina.core.runtime.dispatching.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.runtime.dispatching.http.Constants;
import org.wso2.ballerina.core.runtime.dispatching.http.HTTPServiceDispatcher;
import org.wso2.ballerina.core.runtime.dispatching.http.HTTPServicesRegistry;
import org.wso2.ballerina.core.runtime.dispatching.uri.URIUtil;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;


/**
 * Service Dispatcher for WebSocket Endpoint.
 *
 * @since 0.8.0
 */
public class WebSocketServiceDispatcher extends HTTPServiceDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketServiceDispatcher.class);

    @Override
    public Service findService(CarbonMessage cMsg, CarbonCallback callback, Context balContext) {
        String interfaceId = getInterface(cMsg);

        String serviceUri = (String) cMsg.getProperty(Constants.TO);
        serviceUri = refactorUri(serviceUri);
        if (serviceUri == null) {
            throw new BallerinaException("No service found to dispatch");
        }
        String basePath = URIUtil.getFirstPathSegment(serviceUri);
        Service service = HTTPServicesRegistry.getInstance().
                getService(interfaceId, Constants.DEFAULT_BASE_PATH + basePath);

        if (service == null) {
            throw new BallerinaException("No service found to handle message for " + serviceUri);
        }


        String webSocketUpgradePath = findWebSocketUpgradePath(service);
        if (webSocketUpgradePath == null) {
            throw new BallerinaException("No service found to handle message for " + serviceUri);
        }

        if (webSocketUpgradePath.equals(serviceUri)) {
            return service;
        }

        throw new BallerinaException("No service found to handle message for " + serviceUri);
    }

    @Override
    public String getProtocol() {
        return Constants.PROTOCOL_WEBSOCKET;
    }


    @Override
    public void serviceRegistered(Service service) {
        if (findWebSocketUpgradePath(service) != null) {
            super.serviceRegistered(service);
        }
    }

    private String findWebSocketUpgradePath(Service service) {
        Annotation websocketUpgradePathAnnotation = null;
        Annotation basePathAnnotation = null;
        Annotation[] annotations = service.getAnnotations();
        for (Annotation annotation: annotations) {
            if (annotation.getName().equals(Constants.PROTOCOL_HTTP + ":" + Constants.ANNOTATION_NAME_BASE_PATH)) {
                basePathAnnotation = annotation;
            } else if (annotation.getName().equals(
                    Constants.PROTOCOL_WEBSOCKET + ":" + Constants.ANNOTATION_NAME_WEBSOCKET_UPGRADE_PATH)) {
                websocketUpgradePathAnnotation = annotation;
            }
        }
        if (websocketUpgradePathAnnotation != null && websocketUpgradePathAnnotation.getValue() != null) {
            if (basePathAnnotation == null) {
                throw new BallerinaException("Cannot define @WebSocketPathUpgrade without @BasePath");
            }

            String basePath = refactorUri(basePathAnnotation.getValue());
            String websocketUpgradePath = refactorUri(websocketUpgradePathAnnotation.getValue());

            return refactorUri(basePath.concat(websocketUpgradePath));
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
