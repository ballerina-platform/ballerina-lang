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

package org.ballerinalang.net.ws;

import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.ConnectorFuture;
import org.ballerinalang.connector.api.ConnectorFutureListener;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.net.http.HttpService;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketInitMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketTextMessage;
import org.wso2.carbon.transport.http.netty.message.HTTPMessageUtil;

import java.net.ProtocolException;
import javax.websocket.Session;

/**
 * Ballerina Connector listener for WebSocket.
 *
 * @since 0.93
 */
public class BallerinaWebSocketConnectorListener implements WebSocketConnectorListener {

    @Override
    public void onMessage(WebSocketInitMessage webSocketInitMessage) {
        try {
            Session session = webSocketInitMessage.handshake();
            CarbonMessage carbonMessage = HTTPMessageUtil.convertWebSocketInitMessage(webSocketInitMessage);
            HttpService service = WebSocketDispatcher.findService(carbonMessage, webSocketInitMessage);
            WebSocketConnectionManager.getInstance().addServerSession(service, session, carbonMessage);
            Resource resource = WebSocketDispatcher.getResource(service, Constants.ANNOTATION_NAME_ON_OPEN);
            submitResource(resource, carbonMessage);
        } catch (ProtocolException e) {
            throw new BallerinaConnectorException("Error occurred during WebSocket handshake", e);
        }
    }

    @Override
    public void onMessage(WebSocketTextMessage webSocketTextMessage) {
        CarbonMessage carbonMessage = HTTPMessageUtil.convertWebSocketTextMessage(webSocketTextMessage);
        HttpService service = WebSocketDispatcher.findService(carbonMessage, webSocketTextMessage);
        Resource resource = WebSocketDispatcher.getResource(service, Constants.ANNOTATION_NAME_ON_TEXT_MESSAGE);
        submitResource(resource, carbonMessage);
    }

    @Override
    public void onMessage(WebSocketBinaryMessage webSocketBinaryMessage) {
        throw new BallerinaConnectorException("Binary messages are not supported!");
    }

    @Override
    public void onMessage(WebSocketControlMessage webSocketControlMessage) {
        throw new BallerinaConnectorException("Pong messages are not supported!");
    }

    @Override
    public void onMessage(WebSocketCloseMessage webSocketCloseMessage) {
        CarbonMessage carbonMessage = HTTPMessageUtil.convertWebSocketCloseMessage(webSocketCloseMessage);
        Session serverSession = webSocketCloseMessage.getChannelSession();
        WebSocketConnectionManager.getInstance().removeSessionFromAll(serverSession);
        HttpService service = WebSocketDispatcher.findService(carbonMessage, webSocketCloseMessage);
        Resource resource = WebSocketDispatcher.getResource(service, Constants.ANNOTATION_NAME_ON_CLOSE);
        submitResource(resource, carbonMessage);
    }

    @Override
    public void onError(Throwable throwable) {
        throw new BallerinaConnectorException("Unexpected error occurred in WebSocket transport", throwable);
    }

    private void submitResource(Resource resource, CarbonMessage carbonMessage) {
        ConnectorFuture connectorFuture = Executor.submit(resource, carbonMessage);
        ConnectorFutureListener futureListener = new WebSocketConnectorFutureListener();
        connectorFuture.setConnectorFutureListener(futureListener);
    }

}
