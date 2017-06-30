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
    public String getProtocol() {
        return Constants.PROTOCOL_WEBSOCKET;
    }

    @Override
    public String getProtocolPackage() {
        return Constants.PROTOCOL_PACKAGE_WEBSOCKET;
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

    private String findWebSocketUpgradePath(ServiceInfo service) {
        AnnotationAttachmentInfo websocketUpgradePathAnnotation = service.getAnnotationAttachmentInfo(
                Constants.WS_PACKAGE_PATH, Constants.ANNOTATION_NAME_WEBSOCKET_UPGRADE_PATH);
        AnnotationAttachmentInfo config = service.getAnnotationAttachmentInfo(Constants.HTTP_PACKAGE_PATH,
                Constants.ANNOTATION_NAME_CONFIG);

        if (websocketUpgradePathAnnotation != null &&
                websocketUpgradePathAnnotation.getAnnotationAttributeValue(Constants.VALUE_ATTRIBUTE) != null) {
            String value = websocketUpgradePathAnnotation.getAnnotationAttributeValue(Constants
                    .VALUE_ATTRIBUTE).getStringValue();
            if (value != null && !value.trim().isEmpty()) {

                if (config == null ||
                        config.getAnnotationAttributeValue(Constants.ANNOTATION_ATTRIBUTE_BASE_PATH) == null ||
                        config.getAnnotationAttributeValue(Constants.ANNOTATION_ATTRIBUTE_BASE_PATH).getStringValue()
                                .trim().isEmpty()) {
                    throw new BallerinaException("Cannot define @WebSocketPathUpgrade without @BasePath");
                }
                String basePath = refactorUri(config.getAnnotationAttributeValue(Constants
                        .ANNOTATION_ATTRIBUTE_BASE_PATH).getStringValue());
                String websocketUpgradePath = refactorUri(value);
                return refactorUri(basePath.concat(websocketUpgradePath));
            }
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
