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
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.sender.http2.Http2ConnectionManager;
import org.wso2.transport.http.netty.sender.http2.OutboundMsgHolder;
import org.wso2.transport.http.netty.sender.http2.TargetChannel;

/**
 * Implementation of the client connector.
 */
public class Http2ClientConnectorImpl implements Http2ClientConnector {

    private static final Logger log = LoggerFactory.getLogger(Http2ClientConnector.class);
    private SenderConfiguration senderConfiguration;

    public Http2ClientConnectorImpl(SenderConfiguration senderConfiguration) {
        this.senderConfiguration = senderConfiguration;
    }

    @Override
    public HttpResponseFuture connect() {
        return null;
    }

    @Override
    public HttpResponseFuture send(HTTPCarbonMessage httpOutboundRequest) {

        HttpResponseFuture httpResponseFuture = new DefaultHttpResponseFuture();

        OutboundMsgHolder outboundMsgHolder =
                new OutboundMsgHolder(httpOutboundRequest, httpResponseFuture);

        try {
            HttpRoute route = getTargetRoute(httpOutboundRequest);
            TargetChannel targetChannel =
                    Http2ConnectionManager.getInstance().borrowChannel(route, senderConfiguration);
            targetChannel.getChannelFuture().addListener(
                    new ConnectionAvailabilityListener(targetChannel, outboundMsgHolder, route));
        } catch (Exception failedCause) {
            httpResponseFuture.notifyHttpListener(failedCause);
        }

        return httpResponseFuture;
    }

    static class ConnectionAvailabilityListener implements ChannelFutureListener {

        TargetChannel targetChannel;
        OutboundMsgHolder outboundMsgHolder;
        HttpRoute route;

        public ConnectionAvailabilityListener(TargetChannel targetChannel,
                                              OutboundMsgHolder outboundMsgHolder, HttpRoute route) {
            this.targetChannel = targetChannel;
            this.route = route;
            this.outboundMsgHolder = outboundMsgHolder;
        }

        public void operationComplete(ChannelFuture channelFuture) throws Exception {
            if (isValidateChannel(channelFuture)) {
                targetChannel.getClientHandler().writeRequest(outboundMsgHolder);
            } else {
                notifyErrorState(channelFuture);
            }
        }

        private boolean isValidateChannel(ChannelFuture channelFuture) throws Exception {
            if (channelFuture.isDone() && channelFuture.isSuccess()) {
                if (log.isDebugEnabled()) {
                    log.debug("Created the connection to address: {}",
                              route.toString() + " " + "Original Channel ID is : " + channelFuture.channel()
                                      .id());
                }
                return true;
            }
            return false;
        }

        private void notifyErrorState(ChannelFuture channelFuture) {
            ClientConnectorException cause =
                    new ClientConnectorException(
                            "Connection error, " + route.toString(), HttpResponseStatus.BAD_GATEWAY.code());
            if (channelFuture.cause() != null) {
                cause.initCause(channelFuture.cause());
            }

            outboundMsgHolder.getResponseFuture().notifyHttpListener(cause);
        }
    }

    @Override
    public boolean close() {
        return false;
    }


    private HttpRoute getTargetRoute(HTTPCarbonMessage httpCarbonMessage) {
        String host = fetchHost(httpCarbonMessage);
        int port = fetchPort(httpCarbonMessage);

        return new HttpRoute(host, port);
    }

    private int fetchPort(HTTPCarbonMessage httpCarbonMessage) {
        int port;
        Object intProperty = httpCarbonMessage.getProperty(Constants.PORT);
        if (intProperty != null && intProperty instanceof Integer) {
            port = (int) intProperty;
        } else {
            port = Constants.DEFAULT_HTTP_PORT;
            httpCarbonMessage.setProperty(Constants.PORT, port);
            log.debug("Cannot find property PORT of type integer, hence using " + port);
        }
        return port;
    }

    private String fetchHost(HTTPCarbonMessage httpCarbonMessage) {
        String host;
        Object hostProperty = httpCarbonMessage.getProperty(Constants.HOST);
        if (hostProperty != null && hostProperty instanceof String) {
            host = (String) hostProperty;
        } else {
            host = Constants.LOCALHOST;
            httpCarbonMessage.setProperty(Constants.HOST, Constants.LOCALHOST);
            log.debug("Cannot find property HOST of type string, hence using localhost as the host");
        }
        return host;
    }
}
