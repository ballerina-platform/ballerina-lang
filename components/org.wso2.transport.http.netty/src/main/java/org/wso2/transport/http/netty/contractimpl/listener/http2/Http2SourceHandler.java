/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.transport.http.netty.contractimpl.listener.http2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpServerUpgradeHandler;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.util.internal.PlatformDependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.Http2OutboundRespListener;
import org.wso2.transport.http.netty.contractimpl.common.Util;
import org.wso2.transport.http.netty.contractimpl.listener.HttpServerChannelInitializer;
import org.wso2.transport.http.netty.message.DefaultListener;
import org.wso2.transport.http.netty.message.Http2DataFrame;
import org.wso2.transport.http.netty.message.Http2HeadersFrame;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpCarbonRequest;
import org.wso2.transport.http.netty.message.PooledDataStreamerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;

/**
 * {@code HTTP2SourceHandler} read the HTTP/2 binary frames sent from client through the channel.
 * <p>
 * This is also responsible for building the {@link HttpCarbonRequest} and forward to the listener
 * interested in request messages.
 */
public final class Http2SourceHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(Http2SourceHandler.class);

    // streamIdRequestMap contains mapping of http carbon messages vs stream id to support multiplexing
    private Map<Integer, HttpCarbonMessage> streamIdRequestMap = PlatformDependent.newConcurrentHashMap();
    private ChannelHandlerContext ctx;
    private String interfaceId;
    private ServerConnectorFuture serverConnectorFuture;
    private Http2Connection conn;
    private String serverName;
    private HttpServerChannelInitializer serverChannelInitializer;
    private String remoteAddress;
    private Http2ConnectionEncoder encoder;

    Http2SourceHandler(HttpServerChannelInitializer serverChannelInitializer, Http2ConnectionEncoder encoder,
                       String interfaceId, Http2Connection conn, ServerConnectorFuture serverConnectorFuture,
                       String serverName) {
        this.serverChannelInitializer = serverChannelInitializer;
        this.encoder = encoder;
        this.interfaceId = interfaceId;
        this.serverConnectorFuture = serverConnectorFuture;
        this.conn = conn;
        this.serverName = serverName;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        this.ctx = ctx;
        // Populate remote address
        SocketAddress address = ctx.channel().remoteAddress();
        if (address instanceof InetSocketAddress) {
            remoteAddress = ((InetSocketAddress) address).getAddress().toString();
            if (remoteAddress.startsWith("/")) {
                remoteAddress = remoteAddress.substring(1);
            }
        }
    }

    /**
     * Handles the cleartext HTTP upgrade event.
     * <p>
     * If an upgrade occurred, message needs to be dispatched to
     * the correct service/resource and response should be delivered over stream 1
     * (the stream specifically reserved for cleartext HTTP upgrade).
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof HttpServerUpgradeHandler.UpgradeEvent) {
            FullHttpRequest upgradedRequest = ((HttpServerUpgradeHandler.UpgradeEvent) evt).upgradeRequest();

            // Construct new HTTP Request
            HttpRequest httpRequest = new DefaultHttpRequest(
                    new HttpVersion(Constants.HTTP_VERSION_2_0, true), upgradedRequest.method(),
                    upgradedRequest.uri(), upgradedRequest.headers());

            HttpCarbonRequest requestCarbonMessage = setupCarbonRequest(httpRequest);
            requestCarbonMessage.addHttpContent(new DefaultLastHttpContent(upgradedRequest.content()));
            notifyRequestListener(requestCarbonMessage, 1);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof Http2HeadersFrame) {
            Http2HeadersFrame headersFrame = (Http2HeadersFrame) msg;
            int streamId = headersFrame.getStreamId();

            if (headersFrame.isEndOfStream()) {
                // Retrieve HTTP request and add last http content with trailer headers.
                HttpCarbonMessage sourceReqCMsg = streamIdRequestMap.get(streamId);
                if (sourceReqCMsg != null) {
                    readTrailerHeaders(streamId, headersFrame.getHeaders(), sourceReqCMsg);
                    streamIdRequestMap.remove(streamId);
                } else if (headersFrame.getHeaders().contains(Constants.HTTP2_METHOD)) {
                    // if the header frame is an initial header frame and also it has endOfStream
                    sourceReqCMsg = setupHttp2CarbonMsg(headersFrame.getHeaders(), streamId);
                    // Add empty last http content if no data frames available in the http request
                    sourceReqCMsg.addHttpContent(new DefaultLastHttpContent());
                    notifyRequestListener(sourceReqCMsg, streamId);
                }
            } else {
                // Construct new HTTP Request
                HttpCarbonMessage sourceReqCMsg = setupHttp2CarbonMsg(headersFrame.getHeaders(), streamId);
                streamIdRequestMap.put(streamId, sourceReqCMsg);   // storing to add HttpContent later
                notifyRequestListener(sourceReqCMsg, streamId);
            }

        } else if (msg instanceof Http2DataFrame) {
            Http2DataFrame dataFrame = (Http2DataFrame) msg;
            int streamId = dataFrame.getStreamId();
            ByteBuf data = dataFrame.getData();
            HttpCarbonMessage sourceReqCMsg = streamIdRequestMap.get(streamId);
            if (sourceReqCMsg != null) {
                if (dataFrame.isEndOfStream()) {
                    sourceReqCMsg.addHttpContent(new DefaultLastHttpContent(data));
                    streamIdRequestMap.remove(streamId);
                } else {
                    sourceReqCMsg.addHttpContent(new DefaultHttpContent(data));
                }
            } else {
                LOG.warn("Inconsistent state detected : data has received before headers");
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void readTrailerHeaders(int streamId, Http2Headers headers, HttpCarbonMessage responseMessage)
            throws Http2Exception {

        HttpVersion version = new HttpVersion(Constants.HTTP_VERSION_2_0, true);
        LastHttpContent lastHttpContent = new DefaultLastHttpContent();
        HttpHeaders trailers = lastHttpContent.trailingHeaders();

        HttpConversionUtil.addHttp2ToHttpHeaders(
                streamId, headers, trailers, version, true, false);
        responseMessage.addHttpContent(lastHttpContent);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        destroy();
        ctx.fireChannelInactive();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        destroy();
        ctx.fireChannelUnregistered();
    }

    /**
     * Notifies the registered listeners which listen for the incoming carbon messages.
     *
     * @param httpRequestMsg the http request message
     * @param streamId       the id of the stream
     */
    private void notifyRequestListener(HttpCarbonMessage httpRequestMsg, int streamId) {
        if (serverConnectorFuture != null) {
            try {
                ServerConnectorFuture outboundRespFuture = httpRequestMsg.getHttpResponseFuture();
                outboundRespFuture.setHttpConnectorListener(new Http2OutboundRespListener(
                        serverChannelInitializer, httpRequestMsg, ctx, conn, encoder, streamId, serverName,
                        remoteAddress));
                serverConnectorFuture.notifyHttpListener(httpRequestMsg);
            } catch (Exception e) {
                LOG.error("Error while notifying listeners", e);
            }
        } else {
            LOG.error("Cannot find registered listener to forward the message");
        }
    }

    /**
     * Creates a carbon message for HTTP/2 request.
     *
     * @param http2Headers the Http2 headers
     * @return a HttpCarbonMessage
     */
    private HttpCarbonMessage setupHttp2CarbonMsg(Http2Headers http2Headers, int streamId) throws Http2Exception {
        return setupCarbonRequest(Util.createHttpRequestFromHttp2Headers(http2Headers, streamId));
    }

    /**
     * Creates a {@code HttpCarbonRequest} from HttpRequest.
     *
     * @param httpRequest the HTTPRequest message
     * @return the CarbonRequest Message created from given HttpRequest
     */
    private HttpCarbonRequest setupCarbonRequest(HttpRequest httpRequest) {
        HttpCarbonRequest sourceReqCMsg = new HttpCarbonRequest(httpRequest, new DefaultListener(ctx));
        sourceReqCMsg.setProperty(Constants.POOLED_BYTE_BUFFER_FACTORY, new PooledDataStreamerFactory(ctx.alloc()));
        sourceReqCMsg.setProperty(Constants.CHNL_HNDLR_CTX, this.ctx);
        HttpVersion protocolVersion = httpRequest.protocolVersion();
        sourceReqCMsg.setProperty(Constants.HTTP_VERSION,
                protocolVersion.majorVersion() + "." + protocolVersion.minorVersion());
        sourceReqCMsg.setProperty(Constants.HTTP_METHOD, httpRequest.method().name());

        InetSocketAddress localAddress = null;
        //This check was added because in case of netty embedded channel, this could be of type 'EmbeddedSocketAddress'.
        if (ctx.channel().localAddress() instanceof InetSocketAddress) {
            localAddress = (InetSocketAddress) ctx.channel().localAddress();
        }
        sourceReqCMsg.setProperty(Constants.LOCAL_ADDRESS, localAddress);
        sourceReqCMsg.setProperty(Constants.LISTENER_PORT, localAddress != null ? localAddress.getPort() : null);
        sourceReqCMsg.setProperty(Constants.LISTENER_INTERFACE_ID, interfaceId);
        sourceReqCMsg.setProperty(Constants.PROTOCOL, Constants.HTTP_SCHEME);
        String uri = httpRequest.uri();
        sourceReqCMsg.setProperty(Constants.REQUEST_URL, uri);
        sourceReqCMsg.setProperty(Constants.TO, uri);
        return sourceReqCMsg;
    }

    private void destroy() {
        streamIdRequestMap.clear();
    }

}
