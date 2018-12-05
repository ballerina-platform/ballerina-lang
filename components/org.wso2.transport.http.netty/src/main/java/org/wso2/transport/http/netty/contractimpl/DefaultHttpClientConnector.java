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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.base64.Base64;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.ClientConnectorException;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.config.ChunkConfig;
import org.wso2.transport.http.netty.contract.config.ForwardedExtensionConfig;
import org.wso2.transport.http.netty.contract.config.KeepAliveConfig;
import org.wso2.transport.http.netty.contract.config.SenderConfiguration;
import org.wso2.transport.http.netty.contractimpl.common.HttpRoute;
import org.wso2.transport.http.netty.contractimpl.common.ssl.SSLConfig;
import org.wso2.transport.http.netty.contractimpl.listener.SourceHandler;
import org.wso2.transport.http.netty.contractimpl.sender.ConnectionAvailabilityListener;
import org.wso2.transport.http.netty.contractimpl.sender.channel.TargetChannel;
import org.wso2.transport.http.netty.contractimpl.sender.channel.pool.ConnectionManager;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2ClientChannel;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2ConnectionManager;
import org.wso2.transport.http.netty.contractimpl.sender.http2.OutboundMsgHolder;
import org.wso2.transport.http.netty.contractimpl.sender.http2.TimeoutHandler;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.Http2Reset;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.ResponseHandle;

import java.util.NoSuchElementException;

import static org.wso2.transport.http.netty.contract.Constants.COLON;
import static org.wso2.transport.http.netty.contract.Constants.HTTP_SCHEME;

/**
 * Implementation of the client connector.
 */
public class DefaultHttpClientConnector implements HttpClientConnector {

    private static final Logger LOG = LoggerFactory.getLogger(HttpClientConnector.class);

    private ConnectionManager connectionManager;
    private Http2ConnectionManager http2ConnectionManager;
    private SenderConfiguration senderConfiguration;
    private SSLConfig sslConfig;
    private int socketIdleTimeout;
    private String httpVersion;
    private ChunkConfig chunkConfig;
    private KeepAliveConfig keepAliveConfig;
    private boolean isHttp2;
    private ForwardedExtensionConfig forwardedExtensionConfig;

    public DefaultHttpClientConnector(ConnectionManager connectionManager, SenderConfiguration senderConfiguration) {
        this.connectionManager = connectionManager;
        this.http2ConnectionManager = connectionManager.getHttp2ConnectionManager();
        this.senderConfiguration = senderConfiguration;
        initTargetChannelProperties(senderConfiguration);
        if (Float.valueOf(senderConfiguration.getHttpVersion()) == Constants.HTTP_2_0) {
            isHttp2 = true;
        }
    }

    @Override
    public HttpResponseFuture connect() {
        return null;
    }

    @Override
    public HttpResponseFuture getResponse(ResponseHandle responseHandle) {
        return responseHandle.getOutboundMsgHolder().getResponseFuture();
    }

    @Override
    public HttpResponseFuture getNextPushPromise(ResponseHandle responseHandle) {
        return responseHandle.getOutboundMsgHolder().getResponseFuture();
    }

    @Override
    public HttpResponseFuture hasPushPromise(ResponseHandle responseHandle) {
        return responseHandle.getOutboundMsgHolder().getResponseFuture();
    }

    @Override
    public void rejectPushResponse(Http2PushPromise pushPromise) {
        Http2Reset http2Reset = new Http2Reset(pushPromise.getPromisedStreamId());
        OutboundMsgHolder outboundMsgHolder = pushPromise.getOutboundMsgHolder();
        pushPromise.reject();
        outboundMsgHolder.getHttp2ClientChannel().getChannel().write(http2Reset);
    }

    @Override
    public HttpResponseFuture getPushResponse(Http2PushPromise pushPromise) {
        OutboundMsgHolder outboundMsgHolder = pushPromise.getOutboundMsgHolder();
        if (pushPromise.isRejected()) {
            outboundMsgHolder.getResponseFuture().
                    notifyPushResponse(pushPromise.getPromisedStreamId(),
                                       new ClientConnectorException("Cannot fetch a response for an rejected promise",
                                                                    HttpResponseStatus.BAD_REQUEST.code()));
        }
        return outboundMsgHolder.getResponseFuture();
    }

    @Override
    public boolean close() {
        return false;
    }

    @Override
    public HttpResponseFuture send(HttpCarbonMessage httpOutboundRequest) {
        OutboundMsgHolder outboundMsgHolder = new OutboundMsgHolder(httpOutboundRequest);
        return send(outboundMsgHolder, httpOutboundRequest);
    }

    public HttpResponseFuture send(OutboundMsgHolder outboundMsgHolder, HttpCarbonMessage httpOutboundRequest) {
        final HttpResponseFuture httpResponseFuture;

        SourceHandler srcHandler = (SourceHandler) httpOutboundRequest.getProperty(Constants.SRC_HANDLER);
        if (srcHandler == null && LOG.isDebugEnabled()) {
            LOG.debug(Constants.SRC_HANDLER + " property not found in the message."
                              + " Message is not originated from the HTTP Server connector");
        }

        try {
            /*
             * First try to get a channel from the http2 connection manager. If it is not available
             * get the channel from the http connection manager. Http2 connection manager never create new channels,
             * rather http connection manager create new connections and handover to the http2 connection manager
             * in case of the connection get upgraded to a HTTP/2 connection.
             */
            final HttpRoute route = getTargetRoute(httpOutboundRequest);
            if (isHttp2) {
                // See whether an already upgraded HTTP/2 connection is available
                Http2ClientChannel activeHttp2ClientChannel = http2ConnectionManager.borrowChannel(route);

                if (activeHttp2ClientChannel != null) {
                    outboundMsgHolder.setHttp2ClientChannel(activeHttp2ClientChannel);
                    activeHttp2ClientChannel.getChannel().eventLoop().execute(
                            () -> activeHttp2ClientChannel.getChannel().write(outboundMsgHolder));
                    httpResponseFuture = outboundMsgHolder.getResponseFuture();
                    httpResponseFuture.notifyResponseHandle(new ResponseHandle(outboundMsgHolder));
                    return httpResponseFuture;
                }
            }

            // Look for the connection from http connection manager
            TargetChannel targetChannel = connectionManager.borrowTargetChannel(route, srcHandler, senderConfiguration);
            Http2ClientChannel freshHttp2ClientChannel = targetChannel.getHttp2ClientChannel();
            outboundMsgHolder.setHttp2ClientChannel(freshHttp2ClientChannel);
            httpResponseFuture = outboundMsgHolder.getResponseFuture();

            targetChannel.getConnenctionReadyFuture().setListener(new ConnectionAvailabilityListener() {
                @Override
                public void onSuccess(String protocol, ChannelFuture channelFuture) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Created the connection to address: {}",
                                  route.toString() + " " + "Original Channel ID is : " + channelFuture.channel().id());
                    }
                    if (srcHandler != null) {
                        channelFuture.channel().deregister().addListener(future ->
                                srcHandler.getEventLoop().register(channelFuture.channel()).addListener(
                                        future1 -> startExecutingOutboundRequest(protocol, channelFuture)));
                    } else {
                        startExecutingOutboundRequest(protocol, channelFuture);
                    }
                }

                private void startExecutingOutboundRequest(String protocol, ChannelFuture channelFuture) {
                    if (protocol.equalsIgnoreCase(Constants.HTTP2_CLEARTEXT_PROTOCOL)
                            || protocol.equalsIgnoreCase(Constants.HTTP2_TLS_PROTOCOL)) {
                        prepareTargetChannelForHttp2();
                    } else {
                        // Response for the upgrade request will arrive in stream 1,
                        // so use 1 as the stream id.
                        prepareTargetChannelForHttp(channelFuture);
                        if (protocol.equalsIgnoreCase(Constants.HTTP_SCHEME) &&
                                senderConfiguration.getProxyServerConfiguration() != null) {
                            httpOutboundRequest.setProperty(Constants.IS_PROXY_ENABLED, true);
                        }
                        targetChannel.writeContent(httpOutboundRequest);
                    }
                }

                private void prepareTargetChannelForHttp2() {
                    freshHttp2ClientChannel.setSocketIdleTimeout(socketIdleTimeout);
                    connectionManager.getHttp2ConnectionManager().
                            addHttp2ClientChannel(route, freshHttp2ClientChannel);
                    freshHttp2ClientChannel.addDataEventListener(Constants.IDLE_STATE_HANDLER,
                            new TimeoutHandler(socketIdleTimeout, freshHttp2ClientChannel));

                    freshHttp2ClientChannel.getChannel().eventLoop().execute(
                            () -> freshHttp2ClientChannel.getChannel().write(outboundMsgHolder));
                    httpResponseFuture.notifyResponseHandle(new ResponseHandle(outboundMsgHolder));
                }

                private void prepareTargetChannelForHttp(ChannelFuture channelFuture) {
                    // Response for the upgrade request will arrive in stream 1,
                    // so use 1 as the stream id.
                    freshHttp2ClientChannel.putInFlightMessage(Http2CodecUtil.HTTP_UPGRADE_STREAM_ID,
                            outboundMsgHolder);
                    httpResponseFuture.notifyResponseHandle(new ResponseHandle(outboundMsgHolder));
                    targetChannel.setChannel(channelFuture.channel());
                    targetChannel.configTargetHandler(httpOutboundRequest, httpResponseFuture);
                    targetChannel.setEndPointTimeout(socketIdleTimeout);
                    targetChannel.setCorrelationIdForLogging();
                    targetChannel.setHttpVersion(httpVersion);
                    targetChannel.setChunkConfig(chunkConfig);
                    handleOutboundConnectionHeader(keepAliveConfig, httpOutboundRequest);
                    targetChannel
                            .setForwardedExtension(forwardedExtensionConfig, httpOutboundRequest);
                }

                @Override
                public void onFailure(ClientConnectorException cause) {
                    httpResponseFuture.notifyHttpListener(cause);
                }
            });
        } catch (NoSuchElementException failedCause) {
            if ("Timeout waiting for idle object".equals(failedCause.getMessage())) {
                failedCause = new NoSuchElementException(Constants.MAXIMUM_WAIT_TIME_EXCEED);
            }
            return notifyListenerAndGetErrorResponseFuture(failedCause);
        } catch (Exception failedCause) {
            return notifyListenerAndGetErrorResponseFuture(failedCause);
        }
        return httpResponseFuture;
    }

    private HttpResponseFuture notifyListenerAndGetErrorResponseFuture(Exception failedCause) {
        HttpResponseFuture errorResponseFuture = new DefaultHttpResponseFuture();
        errorResponseFuture.notifyHttpListener(failedCause);
        return errorResponseFuture;
    }

    private HttpRoute getTargetRoute(HttpCarbonMessage httpCarbonMessage) {
        String host = fetchHost(httpCarbonMessage);
        int port = fetchPort(httpCarbonMessage);

        return new HttpRoute(host, port);
    }

    private int fetchPort(HttpCarbonMessage httpCarbonMessage) {
        int port;
        Object intProperty = httpCarbonMessage.getProperty(Constants.HTTP_PORT);
        if (intProperty instanceof Integer) {
            port = (int) intProperty;
        } else {
            port = sslConfig != null ? Constants.DEFAULT_HTTPS_PORT : Constants.DEFAULT_HTTP_PORT;
            httpCarbonMessage.setProperty(Constants.HTTP_PORT, port);
            LOG.debug("Cannot find property PORT of type integer, hence using {}", port);
        }
        return port;
    }

    private String fetchHost(HttpCarbonMessage httpCarbonMessage) {
        String host;
        Object hostProperty = httpCarbonMessage.getProperty(Constants.HTTP_HOST);
        if (hostProperty instanceof String) {
            host = (String) hostProperty;
        } else {
            host = Constants.LOCALHOST;
            httpCarbonMessage.setProperty(Constants.HTTP_HOST, Constants.LOCALHOST);
            LOG.debug("Cannot find property HOST of type string, hence using localhost as the host");
        }
        return host;
    }

    private void initTargetChannelProperties(SenderConfiguration senderConfiguration) {
        this.httpVersion = senderConfiguration.getHttpVersion();
        this.chunkConfig = senderConfiguration.getChunkingConfig();
        this.socketIdleTimeout = senderConfiguration.getSocketIdleTimeout(Constants.ENDPOINT_TIMEOUT);
        this.sslConfig = senderConfiguration.getClientSSLConfig();
        this.keepAliveConfig = senderConfiguration.getKeepAliveConfig();
        this.forwardedExtensionConfig = senderConfiguration.getForwardedExtensionConfig();
    }

    private void handleOutboundConnectionHeader(KeepAliveConfig keepAliveConfig,
                                                        HttpCarbonMessage httpOutboundRequest) {
        switch (keepAliveConfig) {
            case AUTO:
                if (Float.valueOf(httpVersion) >= Constants.HTTP_1_1) {
                    httpOutboundRequest
                            .setHeader(HttpHeaderNames.CONNECTION.toString(), Constants.CONNECTION_KEEP_ALIVE);
                } else {
                    httpOutboundRequest.setHeader(HttpHeaderNames.CONNECTION.toString(), Constants.CONNECTION_CLOSE);
                }
                break;
            case ALWAYS:
                httpOutboundRequest.setHeader(HttpHeaderNames.CONNECTION.toString(), Constants.CONNECTION_KEEP_ALIVE);
                break;
            case NEVER:
                httpOutboundRequest.setHeader(HttpHeaderNames.CONNECTION.toString(), Constants.CONNECTION_CLOSE);
                break;
            default:
                //Do nothing
                break;
        }
        // Add proxy-authorization header if proxy is enabled and scheme is http
        if (senderConfiguration.getScheme().equals(HTTP_SCHEME) &&
                senderConfiguration.getProxyServerConfiguration() != null &&
                senderConfiguration.getProxyServerConfiguration().getProxyUsername() != null &&
                senderConfiguration.getProxyServerConfiguration().getProxyPassword() != null) {
            setProxyAuthorizationHeader(httpOutboundRequest);
        }
    }

    private void setProxyAuthorizationHeader(HttpCarbonMessage httpOutboundRequest) {
        ByteBuf authz = Unpooled.copiedBuffer(
                senderConfiguration.getProxyServerConfiguration().getProxyUsername() + COLON
                        + senderConfiguration.getProxyServerConfiguration().getProxyPassword(), CharsetUtil.UTF_8);
        ByteBuf authzBase64 = Base64.encode(authz, false);
        CharSequence authorization = new AsciiString("Basic " + authzBase64.toString(CharsetUtil.US_ASCII));
        httpOutboundRequest.setHeader(HttpHeaderNames.PROXY_AUTHORIZATION.toString(), authorization);
        authz.release();
        authzBase64.release();
    }
}
