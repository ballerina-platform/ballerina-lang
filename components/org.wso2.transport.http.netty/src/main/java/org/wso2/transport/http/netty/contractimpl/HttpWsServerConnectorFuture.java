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

package org.wso2.transport.http.netty.contractimpl;

import io.netty.channel.ChannelFuture;
import io.netty.channel.group.ChannelGroup;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.contract.PortBindingEventListener;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.websocket.DefaultWebSocketConnectorFuture;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

/**
 * Server connector future implementation.
 */
public class HttpWsServerConnectorFuture extends DefaultWebSocketConnectorFuture implements ServerConnectorFuture {

    private static final String HTTP_CONNECTOR_LISTENER_IS_NOT_SET = "HTTP connector listener is not set";
    private HttpConnectorListener httpConnectorListener;
    private PortBindingEventListener portBindingEventListener;

    private ChannelFuture nettyBindFuture;
    private ChannelGroup allChannels;

    private String openingServerConnectorId;
    private boolean isOpeningSCHttps;
    private String closingServerConnectorId;
    private boolean isClosingSCHttps;
    private Throwable connectorInitException;

    public HttpWsServerConnectorFuture() {
    }

    public HttpWsServerConnectorFuture(ChannelFuture nettyBindFuture) {
        this.nettyBindFuture = nettyBindFuture;
    }

    public HttpWsServerConnectorFuture(ChannelFuture nettyBindFuture, ChannelGroup allChannels) {
        this.nettyBindFuture = nettyBindFuture;
        this.allChannels = allChannels;
    }

    @Override
    public void setHttpConnectorListener(HttpConnectorListener httpConnectorListener) {
        this.httpConnectorListener = httpConnectorListener;
    }

    @Override
    public void notifyHttpListener(HttpCarbonMessage httpMessage) throws ServerConnectorException {
        if (httpConnectorListener == null) {
            throw new ServerConnectorException(HTTP_CONNECTOR_LISTENER_IS_NOT_SET);
        }
        httpConnectorListener.onMessage(httpMessage);
    }

    @Override
    public void notifyHttpListener(HttpCarbonMessage httpMessage, Http2PushPromise pushPromise)
            throws ServerConnectorException {
        if (httpConnectorListener == null) {
            throw new ServerConnectorException(HTTP_CONNECTOR_LISTENER_IS_NOT_SET);
        }
        httpConnectorListener.onPushResponse(pushPromise.getPromisedStreamId(), httpMessage);
    }

    @Override
    public void notifyHttpListener(Http2PushPromise pushPromise) throws ServerConnectorException {
        if (httpConnectorListener == null) {
            throw new ServerConnectorException(HTTP_CONNECTOR_LISTENER_IS_NOT_SET);
        }
        httpConnectorListener.onPushPromise(pushPromise);
    }

    @Override
    public void sync() throws InterruptedException {
        ChannelFuture bindFuture = nettyBindFuture.sync();
        if (this.allChannels != null && bindFuture.channel() != null) {
            this.allChannels.add(bindFuture.channel());
        }
    }

    @Override
    public void notifyErrorListener(Throwable cause) throws ServerConnectorException {
        if (httpConnectorListener == null) {
            throw new ServerConnectorException(HTTP_CONNECTOR_LISTENER_IS_NOT_SET, new Exception(cause));
        }
        httpConnectorListener.onError(cause);
    }

    @Override
    public void setPortBindingEventListener(PortBindingEventListener portBindingEventListener) {
        this.portBindingEventListener = portBindingEventListener;
        if (openingServerConnectorId != null) {
            notifyPortBindingEvent(openingServerConnectorId, isOpeningSCHttps);
            openingServerConnectorId = null;
            isOpeningSCHttps = false;
        }
        if (closingServerConnectorId != null) {
            notifyPortUnbindingEvent(closingServerConnectorId, isClosingSCHttps);
            closingServerConnectorId = null;
            isClosingSCHttps = false;
        }
        if (connectorInitException != null) {
            notifyPortBindingError(connectorInitException);
            connectorInitException = null;
        }
    }

    @Override
    public void notifyPortBindingEvent(String serverConnectorId, boolean isHttps) {
        if (portBindingEventListener == null) {
            this.openingServerConnectorId = serverConnectorId;
            this.isOpeningSCHttps = isHttps;
        } else {
            portBindingEventListener.onOpen(serverConnectorId, isHttps);
        }
    }

    @Override
    public void notifyPortUnbindingEvent(String serverConnectorId, boolean isHttps) {
        if (portBindingEventListener == null) {
            this.closingServerConnectorId = serverConnectorId;
            this.isClosingSCHttps = isHttps;
        } else {
            portBindingEventListener.onClose(serverConnectorId, isHttps);
        }
    }

    @Override
    public void notifyPortBindingError(Throwable throwable) {
        if (portBindingEventListener == null) {
            this.connectorInitException = throwable;
        } else {
            portBindingEventListener.onError(throwable);
        }
    }
}
