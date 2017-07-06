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

package org.wso2.carbon.transport.http.netty.sender.websocket;

import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.ClientConnector;
import org.wso2.carbon.messaging.Headers;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.carbon.transport.http.netty.listener.WebSocketSourceHandler;

import java.net.URISyntaxException;
import java.util.Map;
import javax.net.ssl.SSLException;
import javax.websocket.Session;

/**
 * WebSocket Client connector which connects to a remote WebSocket Endpoint.This store the WebSocket Clients
 * against a unique ID given by the user. So make sure to add a unique ID the carbon message in order to use
 * this properly.
 */
public class WebSocketClientConnector implements ClientConnector {

    @Override
    public Object init(CarbonMessage carbonMessage, CarbonCallback carbonCallback, Map<String, Object> map)
            throws ClientConnectorException {
        // Return the relevant session of the client.
        return handleHandshake(carbonMessage);
    }

    @Override
    public boolean send(CarbonMessage msg, CarbonCallback callback) throws ClientConnectorException {
        // This method is not used since the Session is used in WebSocket scenarios.
        throw new ClientConnectorException("Method not supported for WebSocket. Use session to send messages.");
    }

    @Override
    public boolean send(CarbonMessage msg, CarbonCallback callback, Map<String, String> parameters)
            throws ClientConnectorException {
        // This method is not used since the Session is used in WebSocket scenarios.
        throw new ClientConnectorException("Method not supported for WebSocket. Use session to send messages.");
    }

    @Override
    public String getProtocol() {
        return Constants.WEBSOCKET_PROTOCOL;
    }

    @Override
    public void setMessageProcessor(CarbonMessageProcessor messageProcessor) {
        HTTPTransportContextHolder.getInstance().setMessageProcessor(messageProcessor);
    }

    private Session handleHandshake(CarbonMessage carbonMessage) throws ClientConnectorException {
        String url = (String) carbonMessage.getProperty(Constants.TO);
        WebSocketSourceHandler sourceHandler =
                (WebSocketSourceHandler) carbonMessage.getProperty(Constants.SRC_HANDLER);
        String subprotocols = (String) carbonMessage.getProperty(Constants.WEBSOCKET_SUBPROTOCOLS);
        Boolean allowExtensions = (Boolean) carbonMessage.getProperty(Constants.WEBSOCKET_ALLOW_EXTENSIONS);
        if (allowExtensions == null) {
            allowExtensions = true;
        }
        Headers headers = carbonMessage.getHeaders();
        WebSocketClient webSocketClient = new WebSocketClient(url, subprotocols, allowExtensions,
                                                              headers, sourceHandler);
        try {
            webSocketClient.handshake();
            return webSocketClient.getSession();
        } catch (InterruptedException e) {
            throw new ClientConnectorException("Handshake interrupted", e);
        } catch (URISyntaxException e) {
            throw new ClientConnectorException("SSL Exception occurred during handshake", e);
        } catch (SSLException e) {
            throw new ClientConnectorException("URI Syntax exception occurred during handshake", e);
        }
    }
}
