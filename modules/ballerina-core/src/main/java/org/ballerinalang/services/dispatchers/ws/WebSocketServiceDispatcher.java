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
import org.ballerinalang.util.codegen.AnnAttachmentInfo;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;


/**
 * Service Dispatcher for WebSocket Endpoint.
 *
 * @since 0.8.0
 */
public class WebSocketServiceDispatcher implements ServiceDispatcher {

    private static final Logger log = LoggerFactory.getLogger(WebSocketServicesRegistry.class);

    @Override
    public String getProtocol() {
        return Constants.PROTOCOL_WEBSOCKET;
    }

    @Override
    public String getProtocolPackage() {
        return Constants.PROTOCOL_PACKAGE_WEBSOCKET;
    public void serviceRegistered(ServiceInfo service) {
        WebSocketServicesRegistry.getInstance().registerService(service);
    }


    @Override
    public void serviceUnregistered(ServiceInfo service) {
        WebSocketServicesRegistry.getInstance().unregisterService(service);
    }

    protected String getListenerInterface(CarbonMessage cMsg) {
        String interfaceId = (String) cMsg.getProperty(org.wso2.carbon.messaging.Constants.LISTENER_INTERFACE_ID);
        if (interfaceId == null) {
            if (log.isDebugEnabled()) {
                log.debug("Interface id not found on the message, hence using the default interface");
            }
            interfaceId = org.ballerinalang.services.dispatchers.http.Constants.DEFAULT_INTERFACE;
        }

        return interfaceId;
    }

    private String findWebSocketUpgradePath(ServiceInfo service) {
        AnnAttachmentInfo websocketUpgradePathAnnotation = service.getAnnotationAttachmentInfo(
                Constants.WS_PACKAGE_PATH, Constants.ANNOTATION_NAME_WEBSOCKET_UPGRADE_PATH);
        AnnAttachmentInfo config = service.getAnnotationAttachmentInfo(Constants.HTTP_PACKAGE_PATH,
                Constants.ANNOTATION_NAME_CONFIGURATION);

        if (websocketUpgradePathAnnotation != null &&
                websocketUpgradePathAnnotation.getAttributeValue(Constants.VALUE_ATTRIBUTE) != null) {
            String value = websocketUpgradePathAnnotation.getAttributeValue(Constants
                    .VALUE_ATTRIBUTE).getStringValue();
            if (value != null && !value.trim().isEmpty()) {

                if (config == null ||
                        config.getAttributeValue(Constants.ANNOTATION_ATTRIBUTE_BASE_PATH) == null ||
                        config.getAttributeValue(Constants.ANNOTATION_ATTRIBUTE_BASE_PATH).getStringValue()
                                .trim().isEmpty()) {
                    throw new BallerinaException("Cannot define @WebSocketPathUpgrade without @BasePath");
                }
                String basePath = refactorUri(config.getAttributeValue(Constants
                        .ANNOTATION_ATTRIBUTE_BASE_PATH).getStringValue());
                String websocketUpgradePath = refactorUri(value);
                return refactorUri(basePath.concat(websocketUpgradePath));
            }
        }
        return null;
    }

    private boolean isWebSocketClientService(Service service) {
        AnnotationAttachment[] annotations = service.getAnnotations();
        for (AnnotationAttachment annotation: annotations) {
            if (annotation.getPkgName().equals(Constants.PROTOCOL_WEBSOCKET) &&
                    annotation.getName().equals(Constants.ANNOTATION_NAME_WEBSOCKET_CLIENT_SERVICE)) {
                return true;
            }
        }
        return false;
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
