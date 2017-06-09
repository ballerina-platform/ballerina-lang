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

package org.ballerinalang.services.dispatchers.ws;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.AnnotationAttachment;
import org.ballerinalang.model.Service;
import org.ballerinalang.services.dispatchers.http.Constants;
import org.ballerinalang.services.dispatchers.http.HTTPServiceDispatcher;
import org.ballerinalang.services.dispatchers.http.HTTPServicesRegistry;
import org.ballerinalang.services.dispatchers.uri.URIUtil;
import org.ballerinalang.util.codegen.AnnotationAttachmentInfo;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;


/**
 * Service Dispatcher for WebSocket Endpoint.
 */
public class WebSocketServiceDispatcher extends HTTPServiceDispatcher {

    @Override
    @Deprecated
    public Service findService(CarbonMessage cMsg, CarbonCallback callback, Context balContext) {
        String interfaceId = getInterface(cMsg);
        String serviceUri = (String) cMsg.getProperty(Constants.TO);
        serviceUri = refactorUri(serviceUri);
        if (serviceUri == null) {
            throw new BallerinaException("Internal error occurred during service dispatching");
        }

        String basePath = "";
        Service service = null;
        String[] basePathSegments = URIUtil.getPathSegments(serviceUri);
        for (String pathSegments: basePathSegments) {
            basePath = basePath + Constants.DEFAULT_BASE_PATH + pathSegments;
            service = HTTPServicesRegistry.getInstance().getService(interfaceId, basePath);
            if (service != null) {
                break;
            }
        }

        if (service == null) {
            throw new BallerinaException("no service found to handle the service request received to " + serviceUri);
        }
        String webSocketUpgradePath = findWebSocketUpgradePath(service);
        if (webSocketUpgradePath == null) {
            throw new BallerinaException("no service found to handle the service request received to " + serviceUri);
        }
        if (webSocketUpgradePath.equals(serviceUri)) {
            return service;
        }
        throw new BallerinaException("no service found to handle the service request received to " + serviceUri);
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

    @Override
    public ServiceInfo findService(CarbonMessage cMsg, CarbonCallback callback) {
        String interfaceId = getInterface(cMsg);
        String serviceUri = (String) cMsg.getProperty(Constants.TO);
        serviceUri = refactorUri(serviceUri);
        if (serviceUri == null) {
            throw new BallerinaException("No service found to dispatch");
        }
        String basePath = "";
        ServiceInfo service = null;
        String[] basePathSegments = URIUtil.getPathSegments(serviceUri);
        for (String pathSegments: basePathSegments) {
            basePath = basePath + Constants.DEFAULT_BASE_PATH + pathSegments;
            service = HTTPServicesRegistry.getInstance().getServiceInfo(interfaceId, basePath);
            if (service != null) {
                break;
            }
        }
        if (service == null) {
            return null;
        }
        String webSocketUpgradePath = findWebSocketUpgradePath(service);
        if (webSocketUpgradePath == null) {
            return null;
        }
        if (webSocketUpgradePath.equals(serviceUri)) {
            return service;
        }
        return null;
    }

    @Override
    public void serviceRegistered(ServiceInfo service) {
        // Nothing goes here since all the WebSocket services are registered as HTTP endpoints too.
        // TODO: Implement a separate ServicesRegistry for WebSocket
    }

    private String findWebSocketUpgradePath(ServiceInfo service) {
        AnnotationAttachmentInfo websocketUpgradePathAnnotation = service.getAnnotationAttachmentInfo(
                Constants.WS_PACKAGE_PATH, Constants.ANNOTATION_NAME_WEBSOCKET_UPGRADE_PATH);
        AnnotationAttachmentInfo basePathAnnotation = service.getAnnotationAttachmentInfo(Constants.HTTP_PACKAGE_PATH,
                Constants.ANNOTATION_NAME_BASE_PATH);

        if (websocketUpgradePathAnnotation != null &&
                websocketUpgradePathAnnotation.getAnnotationAttributeValue(Constants.VALUE_ATTRIBUTE) != null) {
            String value = websocketUpgradePathAnnotation.getAnnotationAttributeValue(Constants
                    .VALUE_ATTRIBUTE).getStringValue();
            if (value != null && !value.trim().isEmpty()) {

                if (basePathAnnotation == null ||
                        basePathAnnotation.getAnnotationAttributeValue(Constants.VALUE_ATTRIBUTE) == null ||
                        basePathAnnotation.getAnnotationAttributeValue(Constants.VALUE_ATTRIBUTE).getStringValue()
                                .trim().isEmpty()) {
                    throw new BallerinaException("Cannot define @WebSocketPathUpgrade without @BasePath");
                }
                String basePath = refactorUri(basePathAnnotation.getAnnotationAttributeValue(Constants
                        .VALUE_ATTRIBUTE).getStringValue());
                String websocketUpgradePath = refactorUri(value);
                return refactorUri(basePath.concat(websocketUpgradePath));
            }
        }
        return null;
    }

    @Deprecated
    private String findWebSocketUpgradePath(Service service) {
        AnnotationAttachment websocketUpgradePathAnnotation = null;
        AnnotationAttachment basePathAnnotation = null;
        AnnotationAttachment[] annotations = service.getAnnotations();
        for (AnnotationAttachment annotation: annotations) {
            if (annotation.getPkgName().equals(Constants.PROTOCOL_HTTP) &&
                    annotation.getName().equals(Constants.ANNOTATION_NAME_BASE_PATH)) {
                basePathAnnotation = annotation;
            } else if (annotation.getPkgName().equals(Constants.PROTOCOL_WEBSOCKET) &&
                    annotation.getName().equals(Constants.ANNOTATION_NAME_WEBSOCKET_UPGRADE_PATH)) {
                websocketUpgradePathAnnotation = annotation;
            }
        }
        if (websocketUpgradePathAnnotation != null && websocketUpgradePathAnnotation.getValue() != null &&
                !websocketUpgradePathAnnotation.getValue().trim().isEmpty()) {
            if (basePathAnnotation == null || basePathAnnotation.getValue() == null ||
                    basePathAnnotation.getValue().trim().isEmpty()) {
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
