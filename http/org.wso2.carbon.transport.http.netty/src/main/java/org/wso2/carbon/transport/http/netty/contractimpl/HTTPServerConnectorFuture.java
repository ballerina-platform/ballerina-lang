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

import org.wso2.carbon.transport.http.netty.contract.HTTPConnectorListener;
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
public class HTTPServerConnectorFuture implements ServerConnectorFuture {

    private HTTPConnectorListener httpConnectorListener;
    private WebSocketConnectorListener wsConnectorListener;

    @Override
    public void setHTTPConnectorListener(HTTPConnectorListener httpConnectorListener) {
        this.httpConnectorListener = httpConnectorListener;
    }

    @Override
    public void notifyHTTPListener(HTTPCarbonMessage httpMessage) throws ServerConnectorException {
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
}
