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

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.common.HttpRoute;
import org.wso2.transport.http.netty.common.ssl.SSLConfig;
import org.wso2.transport.http.netty.config.ChunkConfig;
import org.wso2.transport.http.netty.config.SenderConfiguration;
import org.wso2.transport.http.netty.contract.ClientConnectorException;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.listener.SourceHandler;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.ResponseHandle;
import org.wso2.transport.http.netty.sender.channel.TargetChannel;
import org.wso2.transport.http.netty.sender.channel.pool.ConnectionManager;
import org.wso2.transport.http.netty.sender.http2.Http2ClientChannel;
import org.wso2.transport.http.netty.sender.http2.Http2ConnectionManager;
import org.wso2.transport.http.netty.sender.http2.OutboundMsgHolder;

import java.util.NoSuchElementException;

/**
 * Implementation of the client connector.
 */
public class HttpClientConnectorImpl implements HttpClientConnector {

    private static final Logger log = LoggerFactory.getLogger(HttpClientConnector.class);

    private ConnectionManager connectionManager;
    private Http2ConnectionManager http2ConnectionManager;
    private SenderConfiguration senderConfiguration;
    private SSLConfig sslConfig;
    private int socketIdleTimeout;
    private boolean followRedirect;
    private String httpVersion;
    private ChunkConfig chunkConfig;
    private boolean keepAlive;

    public HttpClientConnectorImpl(ConnectionManager connectionManager, SenderConfiguration senderConfiguration) {
        this.connectionManager = connectionManager;
        this.http2ConnectionManager = connectionManager.getHttp2ConnectionManager();
        this.senderConfiguration = senderConfiguration;
        initTargetChannelProperties(senderConfiguration);
    }

    @Override
    public HttpResponseFuture connect() {
        return null;
    }

    @Override
    public HttpResponseFuture send(HTTPCarbonMessage httpOutboundRequest) {
        return sendMessage(httpOutboundRequest, false);
    }

    @Override
    public HttpResponseFuture submit(HTTPCarbonMessage httpOutboundRequest) {
        return sendMessage(httpOutboundRequest, true);
    }

    @Override
    public HttpResponseFuture getResponse(ResponseHandle responseHandle) {
        return responseHandle.getOutboundMsgHolder().getResponseFuture();
    }

    @Override
    public HttpResponseFuture getNextPushPromise(ResponseHandle responseHandle) {
        return responseHandle.getOutboundMsgHolder().getPushPromiseFuture();
    }

    @Override
    public HttpResponseFuture hasPushPromise(ResponseHandle responseHandle) {
        return responseHandle.getOutboundMsgHolder().getPromiseAvailabilityFuture();
    }

    @Override
    public HttpResponseFuture getPushResponse(ResponseHandle responseHandle, Http2PushPromise pushPromise) {
        return responseHandle.getOutboundMsgHolder().getPushResponseFuture(pushPromise);
    }

    @Override
    public boolean close() {
        return false;
    }

    private HttpResponseFuture sendMessage(HTTPCarbonMessage httpOutboundRequest, boolean returnResponseHandle) {
        HttpResponseFuture forwardResponseFuture;
        HttpResponseFuture returnResponseFuture;

        SourceHandler srcHandler = (SourceHandler) httpOutboundRequest.getProperty(Constants.SRC_HANDLER);
        if (srcHandler == null) {
            if (log.isDebugEnabled()) {
                log.debug(Constants.SRC_HANDLER + " property not found in the message."
                        + " Message is not originated from the HTTP Server connector");
            }
        }

        try {
            final HttpRoute route = getTargetRoute(httpOutboundRequest);
            Http2ClientChannel activeHttp2ClientChannel = http2ConnectionManager.borrowChannel(route);

            if (activeHttp2ClientChannel != null) {   // If channel available in http2 connection pool
                OutboundMsgHolder outboundMsgHolder =
                        new OutboundMsgHolder(httpOutboundRequest, activeHttp2ClientChannel);

                activeHttp2ClientChannel.getChannel().eventLoop().execute(() -> {
                    activeHttp2ClientChannel.getChannel().write(outboundMsgHolder);
                });
                if (returnResponseHandle) {
                    returnResponseFuture = outboundMsgHolder.getResponseHandleFuture();
                    returnResponseFuture.notifyResponseHandle(new ResponseHandle(outboundMsgHolder));
                } else {
                    returnResponseFuture = outboundMsgHolder.getResponseFuture();
                }
                return returnResponseFuture;
            }

            TargetChannel targetChannel = connectionManager.borrowTargetChannel(route, srcHandler, senderConfiguration);

            Http2ClientChannel freshHttp2ClientChannel = targetChannel.getHttp2ClientChannel();

            OutboundMsgHolder outboundMsgHolder = new OutboundMsgHolder(httpOutboundRequest, freshHttp2ClientChannel);
            forwardResponseFuture = outboundMsgHolder.getResponseFuture();
            if (returnResponseHandle) {
                returnResponseFuture = outboundMsgHolder.getResponseHandleFuture();
            } else {
                returnResponseFuture = forwardResponseFuture;
            }

            // Response for the upgrade request will arrive in stream 1, so use 1 as the stream id
            freshHttp2ClientChannel.putInFlightMessage(1, outboundMsgHolder);

            targetChannel.getChannelFuture().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (isValidateChannel(channelFuture)) {
                        returnResponseFuture.notifyResponseHandle(new ResponseHandle(outboundMsgHolder));
                        targetChannel.setChannel(channelFuture.channel());
                        targetChannel.configTargetHandler(httpOutboundRequest, forwardResponseFuture);
                        targetChannel.setEndPointTimeout(socketIdleTimeout, followRedirect);
                        targetChannel.setCorrelationIdForLogging();
                        targetChannel.setHttpVersion(httpVersion);
                        targetChannel.setChunkConfig(chunkConfig);
                        if (followRedirect) {
                            setChannelAttributes(channelFuture.channel(), httpOutboundRequest, forwardResponseFuture,
                                                 targetChannel);
                        }
                        if (!keepAlive && Float.valueOf(httpVersion) >= Constants.HTTP_1_1) {
                            httpOutboundRequest.setHeader(HttpHeaderNames.CONNECTION.toString(),
                                                          Constants.CONNECTION_CLOSE);
                        } else if (keepAlive && Float.valueOf(httpVersion) < Constants.HTTP_1_1) {
                            httpOutboundRequest.setHeader(HttpHeaderNames.CONNECTION.toString(),
                                                          Constants.CONNECTION_KEEP_ALIVE);
                        }
                        targetChannel.writeContent(httpOutboundRequest);
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
                    ClientConnectorException cause;

                    if (channelFuture.isDone() && channelFuture.isCancelled()) {
                        cause = new ClientConnectorException("Request Cancelled, " + route.toString(),
                                HttpResponseStatus.BAD_GATEWAY.code());
                    } else if (!channelFuture.isDone() && !channelFuture.isSuccess() &&
                            !channelFuture.isCancelled() && (channelFuture.cause() == null)) {
                        cause = new ClientConnectorException("Connection timeout, " + route.toString(),
                                HttpResponseStatus.BAD_GATEWAY.code());
                    } else {
                        cause = new ClientConnectorException("Connection refused, " + route.toString(),
                                HttpResponseStatus.BAD_GATEWAY.code());
                    }

                    if (channelFuture.cause() != null) {
                        cause.initCause(channelFuture.cause());
                    }

                    returnResponseFuture.notifyHttpListener(cause);
                }
            });
        } catch (Exception failedCause) {
            if (failedCause instanceof NoSuchElementException
                    && "Timeout waiting for idle object".equals(failedCause.getMessage())) {
                failedCause = new NoSuchElementException(Constants.MAXIMUM_WAIT_TIME_EXCEED);
            }
            HttpResponseFuture errorResponseFuture = new DefaultHttpResponseFuture();
            errorResponseFuture.notifyHttpListener(failedCause);
            return errorResponseFuture;
        }

        return returnResponseFuture;
    }

    private HttpRoute getTargetRoute(HTTPCarbonMessage httpCarbonMessage) {
        String host = fetchHost(httpCarbonMessage);
        int port = fetchPort(httpCarbonMessage);

        return new HttpRoute(host, port);
    }

    private int fetchPort(HTTPCarbonMessage httpCarbonMessage) {
        int port;
        Object intProperty = httpCarbonMessage.getProperty(Constants.HTTP_PORT);
        if (intProperty != null && intProperty instanceof Integer) {
            port = (int) intProperty;
        } else {
            port = sslConfig != null ? Constants.DEFAULT_HTTPS_PORT : Constants.DEFAULT_HTTP_PORT;
            httpCarbonMessage.setProperty(Constants.HTTP_PORT, port);
            log.debug("Cannot find property PORT of type integer, hence using " + port);
        }
        return port;
    }

    private String fetchHost(HTTPCarbonMessage httpCarbonMessage) {
        String host;
        Object hostProperty = httpCarbonMessage.getProperty(Constants.HTTP_HOST);
        if (hostProperty != null && hostProperty instanceof String) {
            host = (String) hostProperty;
        } else {
            host = Constants.LOCALHOST;
            httpCarbonMessage.setProperty(Constants.HTTP_HOST, Constants.LOCALHOST);
            log.debug("Cannot find property HOST of type string, hence using localhost as the host");
        }
        return host;
    }

    /**
     * Set following attributes to original channel when redirect is on.
     *
     * @param channel            Original channel
     * @param httpCarbonRequest  Http request
     * @param httpResponseFuture Response future
     * @param targetChannel      Target channel
     */
    private void setChannelAttributes(Channel channel, HTTPCarbonMessage httpCarbonRequest,
            HttpResponseFuture httpResponseFuture, TargetChannel targetChannel) {
        channel.attr(Constants.ORIGINAL_REQUEST).set(httpCarbonRequest);
        channel.attr(Constants.RESPONSE_FUTURE_OF_ORIGINAL_CHANNEL).set(httpResponseFuture);
        channel.attr(Constants.TARGET_CHANNEL_REFERENCE).set(targetChannel);
        channel.attr(Constants.ORIGINAL_CHANNEL_START_TIME).set(System.currentTimeMillis());
        channel.attr(Constants.ORIGINAL_CHANNEL_TIMEOUT).set(socketIdleTimeout);
    }

    private void initTargetChannelProperties(SenderConfiguration senderConfiguration) {
        this.httpVersion = senderConfiguration.getHttpVersion();
        this.chunkConfig = senderConfiguration.getChunkingConfig();
        this.followRedirect = senderConfiguration.isFollowRedirect();
        this.socketIdleTimeout = senderConfiguration.getSocketIdleTimeout(Constants.ENDPOINT_TIMEOUT);
        this.sslConfig = senderConfiguration.getSSLConfig();
        this.keepAlive = senderConfiguration.isKeepAlive();
    }
}
