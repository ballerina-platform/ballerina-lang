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

package org.wso2.carbon.transport.http.netty.contractimpl;

import io.netty.channel.ChannelFuture;
import org.wso2.carbon.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.carbon.transport.http.netty.contract.LifeCycleEventListener;
import org.wso2.carbon.transport.http.netty.contract.ServerConnector;
import org.wso2.carbon.transport.http.netty.contract.ServerConnectorException;
import org.wso2.carbon.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketBinaryMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketCloseMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketConnectorListener;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketControlMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketInitMessage;
import org.wso2.carbon.transport.http.netty.contract.websocket.WebSocketTextMessage;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

/**
 * Server connector future implementation
 */
public class HttpWsServerConnectorFuture implements ServerConnectorFuture {

    private HttpConnectorListener httpConnectorListener;
    private WebSocketConnectorListener wsConnectorListener;
    private LifeCycleEventListener lifeCycleEventListener;
    private ChannelFuture channelFuture;
    private String connectorHost;
    private int connectorPort = -1;

    public HttpWsServerConnectorFuture() {
    }

    public HttpWsServerConnectorFuture(ChannelFuture channelFuture) {
        this.channelFuture = channelFuture;
    }

    @Override
    public void setHttpConnectorListener(HttpConnectorListener httpConnectorListener) {
        this.httpConnectorListener = httpConnectorListener;
    }

    @Override
    public void notifyHttpListener(HTTPCarbonMessage httpMessage) throws ServerConnectorException {
        if (httpConnectorListener == null) {
            throw new ServerConnectorException("HTTP connector listener is not set");
        }
        httpConnectorListener.onMessage(httpMessage);
    }

    @Override
    public void setWSConnectorListener(WebSocketConnectorListener wsConnectorListener) {
        this.wsConnectorListener = wsConnectorListener;
    }

    @Override
    public void notifyWSListener(WebSocketInitMessage initMessage) throws ServerConnectorException {
        if (wsConnectorListener == null) {
            throw new ServerConnectorException("WebSocket connector listener is not set");
        }
        wsConnectorListener.onMessage(initMessage);
    }

    @Override
    public void notifyWSListener(WebSocketTextMessage textMessage) throws ServerConnectorException {
        if (wsConnectorListener == null) {
            throw new ServerConnectorException("WebSocket connector listener is not set");
        }
        wsConnectorListener.onMessage(textMessage);
    }

    @Override
    public void notifyWSListener(WebSocketBinaryMessage binaryMessage) throws ServerConnectorException {
        if (wsConnectorListener == null) {
            throw new ServerConnectorException("WebSocket connector listener is not set");
        }
        wsConnectorListener.onMessage(binaryMessage);
    }

    @Override
    public void notifyWSListener(WebSocketControlMessage controlMessage) throws ServerConnectorException {
        if (wsConnectorListener == null) {
            throw new ServerConnectorException("WebSocket connector listener is not set");
        }
        wsConnectorListener.onMessage(controlMessage);
    }

    @Override
    public void notifyWSListener(WebSocketCloseMessage closeMessage) throws ServerConnectorException {
        if (wsConnectorListener == null) {
            throw new ServerConnectorException("WebSocket connector listener is not set");
        }
        wsConnectorListener.onMessage(closeMessage);
    }

    @Override
    public void notifyWSListener(Throwable throwable) throws ServerConnectorException {
        if (wsConnectorListener == null) {
            throw new ServerConnectorException("WebSocket connector listener is not set");
        }
        wsConnectorListener.onError(throwable);
    }

    @Override
    public void notifyWSIdleTimeout(WebSocketControlMessage controlMessage) throws ServerConnectorException {
        if (wsConnectorListener == null) {
            throw new ServerConnectorException("WebSocket connector listener is not set");
        }
        wsConnectorListener.onIdleTimeout(controlMessage);
    }

    @Override
    public void sync() throws InterruptedException {
        channelFuture.sync();
    }

    @Override
    public void notifyErrorListener(Throwable cause) throws ServerConnectorException {
        if (httpConnectorListener == null) {
            throw new ServerConnectorException("HTTP connector listener is not set", new Exception(cause));
        }
        httpConnectorListener.onError(cause);
    }

    @Override
    public void setLifeCycleEventListener(LifeCycleEventListener lifeCycleEventListener) {
        this.lifeCycleEventListener = lifeCycleEventListener;
        if (connectorHost != null && connectorPort >= 0) {
            notifyLifeCycleEventListener(connectorHost, connectorPort);
            connectorHost = null;
            connectorPort = -1;
        }
    }

    @Override
    public void notifyLifeCycleEventListener(String host, int port) {
        if (lifeCycleEventListener == null) {
            this.connectorHost = host;
            this.connectorPort = port;
        } else {
            lifeCycleEventListener.onOpen(host, port);
        }
    }

    @Override
    public void notifyLifeCycleEventListener(ServerConnector serverConnector) throws ServerConnectorException {
        if (lifeCycleEventListener == null) {
            throw new ServerConnectorException("Connector life cycle listener is not set");
        } else {
            lifeCycleEventListener.onClose(serverConnector);
        }
    }
}
