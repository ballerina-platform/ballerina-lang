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

import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;
import org.wso2.carbon.connector.framework.WebSocketSessionManager;
import org.wso2.carbon.messaging.BinaryCarbonMessage;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.ControlCarbonMessage;
import org.wso2.carbon.messaging.StatusCarbonMessage;
import org.wso2.carbon.messaging.TextCarbonMessage;

import javax.websocket.Session;

/**
 * Resource Dispatcher for WebSocket Endpoint
 *
 * @since 0.8.0
 */
public class WebSocketResourceDispatcher implements ResourceDispatcher {

    private Resource onOpenMessage = null;
    private Resource onTextMessage = null;
    private Resource onBinaryMessage = null;
    private Resource onPongMessage = null;
    private Resource onCloseMessage = null;

    @Override
    public Resource findResource(Service service, CarbonMessage cMsg, CarbonCallback callback, Context balContext)
            throws BallerinaException {

        try {
            mapResources(service);
            if (cMsg instanceof TextCarbonMessage) {
                return onTextMessage;
            } else if (cMsg instanceof BinaryCarbonMessage) {
                return onBinaryMessage;
            } else if (cMsg instanceof ControlCarbonMessage) {
                return onPongMessage;
            } else if (cMsg instanceof StatusCarbonMessage) {
                StatusCarbonMessage statusMessage = (StatusCarbonMessage) cMsg;

                if (statusMessage.getStatus().equals(org.wso2.carbon.messaging.Constants.STATUS_CLOSE)) {
                    String uri =
                            (String) statusMessage.getProperty(org.wso2.carbon.transport.
                                                                       http.netty.common.Constants.TO);
                    String sessionId =
                            (String) statusMessage.getProperty(org.wso2.carbon.transport.
                                                                       http.netty.common.Constants.CHANNEL_ID);
                    WebSocketSessionManager.getInstance().removeSession(uri, sessionId);
                    return onCloseMessage;
                } else if (statusMessage.getStatus().equals(org.wso2.carbon.messaging.Constants.STATUS_OPEN)) {
                    String connection = (String) cMsg.getProperty(
                            org.wso2.carbon.transport.http.netty.common.Constants.CONNECTION);
                    String upgrade = (String) cMsg.getProperty(
                            org.wso2.carbon.transport.http.netty.common.Constants.UPGRADE);

                    /* If the connection is WebSocket upgrade, this block will be executed */
                    if (connection != null && upgrade != null &&
                            connection.equalsIgnoreCase(org.wso2.carbon.transport.http.netty.common.Constants.UPGRADE)
                            && upgrade.equalsIgnoreCase(
                                    org.wso2.carbon.transport.http.netty.common.Constants.WEBSOCKET_UPGRADE)) {
                        WebSocketSessionManager webSocketSessionManager = WebSocketSessionManager.getInstance();
                        Session session = (Session) cMsg.getProperty(org.wso2.carbon.transport.http.netty.
                                                                             common.Constants.WEBSOCKET_SESSION);
                        String uri = (String) cMsg.getProperty(org.wso2.carbon.transport.
                                                                       http.netty.common.Constants.TO);
                        webSocketSessionManager.add(uri, session);

                        return onOpenMessage;
                    }
                }
            }
        } catch (Throwable e) {
            throw new BallerinaException(e.getMessage(), balContext);
        }
        throw new BallerinaException("No matching Resource found for dispatching.");
    }

    @Override
    public String getProtocol() {
        return Constants.PROTOCOL_WEBSOCKET;
    }

    private void mapResources(Service service) {
        for (Resource resource: service.getResources()) {
            if (resource.getAnnotation(Constants.ANNOTATION_NAME_ON_OPEN) != null) {
                onOpenMessage = resource;
            } else if (resource.getAnnotation(Constants.ANNOTATION_NAME_ON_TEXT_MESSAGE) != null) {
                onTextMessage = resource;
            } else if (resource.getAnnotation(Constants.ANNOTATION_NAME_ON_BINARY_MESSAGE) != null) {
                onBinaryMessage = resource;
            } else if (resource.getAnnotation(Constants.ANNOTATION_NAME_ON_PONG_MESSAGE) != null) {
                onPongMessage = resource;
            } else if (resource.getAnnotation(Constants.ANNOTATION_NAME_ON_CLOSE) != null) {
                onCloseMessage = resource;
            }
        }
    }
}
