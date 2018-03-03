/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.common.HttpRoute;
import org.wso2.transport.http.netty.config.SenderConfiguration;
import org.wso2.transport.http.netty.contract.ClientConnectorException;
import org.wso2.transport.http.netty.contract.Http2ClientConnector;
import org.wso2.transport.http.netty.contract.HttpPushPromiseAvailabilityFuture;
import org.wso2.transport.http.netty.contract.HttpPushPromiseFuture;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.HttpResponseHandleFuture;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.ResponseHandle;
import org.wso2.transport.http.netty.sender.http2.ConnectionManager;
import org.wso2.transport.http.netty.sender.http2.OutboundMsgHolder;
import org.wso2.transport.http.netty.sender.http2.TargetChannel;

/**
 * {@code DefaultHttp2ClientConnector} is the implementation of the {@code Http2ClientConnector}.
 */
public class DefaultHttp2ClientConnector implements Http2ClientConnector {

    private static final Logger log = LoggerFactory.getLogger(Http2ClientConnector.class);
    private ConnectionManager connectionManager;

    public DefaultHttp2ClientConnector(SenderConfiguration senderConfiguration) {
        connectionManager = new ConnectionManager(senderConfiguration);
    }

    @Override
    public HttpResponseFuture connect() {
        return null;
    }

    @Override
    public HttpResponseFuture send(HTTPCarbonMessage httpOutboundRequest) {

        HttpResponseFuture httpResponseFuture = null;
        try {
            HttpRoute route = getTargetRoute(httpOutboundRequest);
            TargetChannel targetChannel = connectionManager.borrowChannel(route);
            OutboundMsgHolder outboundMsgHolder =
                    new OutboundMsgHolder(httpOutboundRequest, targetChannel);
            httpResponseFuture = outboundMsgHolder.getResponseFuture();
            targetChannel.getChannelFuture().
                    addListener(new ConnectionAvailabilityListener(outboundMsgHolder, route, false));
        } catch (Exception failedCause) {
            if (httpResponseFuture == null) {
                httpResponseFuture = new DefaultHttpResponseFuture();
            }
            httpResponseFuture.notifyHttpListener(failedCause);
        }
        return httpResponseFuture;
    }

    @Override
    public boolean close() {
        return false;
    }

    @Override
    public HttpResponseHandleFuture executeAsync(HTTPCarbonMessage httpOutboundRequest) {

        HttpResponseHandleFuture responseHandleFuture = null;
        try {
            HttpRoute route = getTargetRoute(httpOutboundRequest);
            TargetChannel targetChannel = connectionManager.borrowChannel(route);
            OutboundMsgHolder outboundMsgHolder = new OutboundMsgHolder(httpOutboundRequest, targetChannel);
            responseHandleFuture = outboundMsgHolder.getResponseHandleFuture();
            targetChannel.getChannelFuture().
                    addListener(new ConnectionAvailabilityListener(outboundMsgHolder, route, true));
        } catch (Exception failedCause) {
            if (responseHandleFuture == null) {
                responseHandleFuture = new DefaultHttpResponseHandleFuture();
            }
            responseHandleFuture.notifyHttpListener(failedCause);
        }
        return responseHandleFuture;
    }

    @Override
    public HttpResponseFuture getResponse(ResponseHandle responseHandle) {
        return responseHandle.getOutboundMsgHolder().getResponseFuture();
    }

    @Override
    public HttpPushPromiseFuture getNextPushPromise(ResponseHandle responseHandle) {
        return responseHandle.getOutboundMsgHolder().getPushPromiseFuture();
    }

    @Override
    public HttpPushPromiseAvailabilityFuture hasPushPromise(ResponseHandle responseHandle) {
        return responseHandle.getOutboundMsgHolder().getPushPromiseAvailabilityFuture();
    }

    @Override
    public HttpResponseFuture getPushResponse(ResponseHandle responseHandle, Http2PushPromise pushPromise) {
        return responseHandle.getOutboundMsgHolder().getPushResponseFuture(pushPromise);
    }

    /**
     * Get the {@code HttpRoute} where message need to be sent
     *
     * @param message request HTTPCarbonMessage
     * @return HTTP route where message need to be sent
     */
    private HttpRoute getTargetRoute(HTTPCarbonMessage message) {

        // Fetch host
        String host = Constants.LOCALHOST;
        Object hostProperty = message.getProperty(Constants.HTTP_HOST);
        if (hostProperty != null && hostProperty instanceof String) {
            host = (String) hostProperty;
        } else {
            message.setProperty(Constants.HTTP_HOST, Constants.LOCALHOST);
            log.debug("Cannot find property HOST of type string, hence using localhost as the host");
        }

        // Fetch Port
        int port = Constants.DEFAULT_HTTP_PORT;
        Object intProperty = message.getProperty(Constants.HTTP_PORT);
        if (intProperty != null && intProperty instanceof Integer) {
            port = (int) intProperty;
        } else {
            message.setProperty(Constants.HTTP_PORT, Constants.DEFAULT_HTTP_PORT);
            log.debug("Cannot find property PORT of type integer, hence using " + port);
        }

        return new HttpRoute(host, port);
    }

    /**
     * Listener which wait until connection to be established before sending the message
     */
    static class ConnectionAvailabilityListener implements ChannelFutureListener {

        OutboundMsgHolder outboundMsgHolder;
        HttpRoute route;
        boolean async;

        public ConnectionAvailabilityListener(OutboundMsgHolder outboundMsgHolder, HttpRoute route, boolean async) {
            this.route = route;
            this.outboundMsgHolder = outboundMsgHolder;
            this.async = async;
        }

        public void operationComplete(ChannelFuture channelFuture) throws Exception {
            if (isValidChannel(channelFuture)) {
                channelFuture.channel().write(outboundMsgHolder);
                if (async) {
                    outboundMsgHolder.getResponseHandleFuture().
                            notifyHttpListener(new ResponseHandle(outboundMsgHolder));
                }
            } else {
                ClientConnectorException cause = new ClientConnectorException(
                        "Connection error, " + route.toString(), HttpResponseStatus.BAD_GATEWAY.code());
                if (channelFuture.cause() != null) {
                    cause.initCause(channelFuture.cause());
                }
                if (async) {
                    outboundMsgHolder.getResponseHandleFuture().notifyHttpListener(cause);
                } else {
                    outboundMsgHolder.getResponseFuture().notifyHttpListener(cause);
                }
            }
        }

        private boolean isValidChannel(ChannelFuture channelFuture) throws Exception {
            if (channelFuture.isDone() && channelFuture.isSuccess()) {
                if (log.isDebugEnabled()) {
                    log.debug("Created the connection to address: {}, Original Channel ID is : {}", route.toString(),
                              channelFuture.channel().id());
                }
                return true;
            }
            return false;
        }
    }
}


