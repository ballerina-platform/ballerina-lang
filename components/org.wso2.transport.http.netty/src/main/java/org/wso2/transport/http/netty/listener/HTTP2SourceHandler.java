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
package org.wso2.transport.http.netty.listener;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpServerUpgradeHandler;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http2.Http2ConnectionDecoder;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2ConnectionHandler;
import io.netty.handler.codec.http2.Http2EventAdapter;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.util.internal.PlatformDependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.Http2OutboundRespListener;
import org.wso2.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.transport.http.netty.message.EmptyLastHttpContent;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HttpCarbonRequest;
import org.wso2.transport.http.netty.message.PooledDataStreamerFactory;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * {@code HTTP2SourceHandler} read the Http2 binary frames sent from client through the channel
 * and build carbon messages deliver to the listener.
 */
public final class HTTP2SourceHandler extends Http2ConnectionHandler {

    private static final Logger log = LoggerFactory.getLogger(HTTP2SourceHandler.class);

    /* streamIdRequestMap contains mapping of http carbon messages vs stream id to support multiplexing */
    private Map<Integer, HTTPCarbonMessage> streamIdRequestMap = PlatformDependent.newConcurrentHashMap();
    private ChannelHandlerContext ctx;
    private Http2FrameListener http2FrameListener;
    private String interfaceId;
    private ServerConnectorFuture serverConnectorFuture;

    public HTTP2SourceHandler(Http2ConnectionDecoder decoder, Http2ConnectionEncoder encoder,
                              Http2Settings initialSettings,
                              String interfaceId, ServerConnectorFuture serverConnectorFuture) {
        super(decoder, encoder, initialSettings);
        http2FrameListener = new Http2FrameListener();
        this.interfaceId = interfaceId;
        this.serverConnectorFuture = serverConnectorFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        this.ctx = ctx;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (ctx != null && ctx.channel().isActive()) {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.close();
        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
            HTTPTransportContextHolder.getInstance().getHandlerExecutor()
                    .executeAtSourceConnectionTermination(Integer.toString(ctx.hashCode()));
        }
    }

    /**
     * Handles the cleartext HTTP upgrade event. If an upgrade occurred, message needs to be dispatched to
     * the correct service/resource and response should be delivered over stream 1
     * (the stream specifically reserved for cleartext HTTP upgrade).
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof HttpServerUpgradeHandler.UpgradeEvent) {
            FullHttpRequest upgradedRequest = ((HttpServerUpgradeHandler.UpgradeEvent) evt).upgradeRequest();
            HttpCarbonRequest requestCarbonMessage = setupCarbonRequest(upgradedRequest);
            notifyRequestListener(requestCarbonMessage, 1);
        }
        super.userEventTriggered(ctx, evt);
    }

    /**
     * Get the listener which listen to the HTTP/2 frames
     *
     * @return the {@code Http2FrameListener}
     */
    public Http2FrameListener getHttp2FrameListener() {
        return http2FrameListener;
    }

    /* Notify the registered listeners which listen for the incoming carbon messages */
    private void notifyRequestListener(HTTPCarbonMessage httpRequestMsg, int streamId) {

        if (serverConnectorFuture != null) {
            try {
                ServerConnectorFuture outboundRespFuture = httpRequestMsg.getHttpResponseFuture();
                outboundRespFuture
                        .setHttpConnectorListener(
                                new Http2OutboundRespListener(httpRequestMsg, ctx, encoder(), streamId));
                serverConnectorFuture.notifyHttpListener(httpRequestMsg);
            } catch (Exception e) {
                log.error("Error while notifying listeners", e);
            }
        } else {
            log.error("Cannot find registered listener to forward the message");
        }
    }

    /* listener which listen to the HTTP/2 frames */
    private class Http2FrameListener extends Http2EventAdapter {

        @Override
        public void onHeadersRead(ChannelHandlerContext ctx, int streamId,
                                  Http2Headers headers, int padding, boolean endOfStream) throws Http2Exception {

            HTTPCarbonMessage sourceReqCMsg = setupHttp2CarbonMsg(headers);

            if (endOfStream) {  // Add empty last http content if no data frames available in the http request
                sourceReqCMsg.addHttpContent(new EmptyLastHttpContent());
            } else {
                streamIdRequestMap.put(streamId, sourceReqCMsg);   // storing to add HttpContent later
            }
            notifyRequestListener(sourceReqCMsg, streamId);
        }

        @Override
        public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int streamDependency,
                                  short weight, boolean exclusive, int padding, boolean endOfStream)
                throws Http2Exception {
            onHeadersRead(ctx, streamId, headers, padding, endOfStream);
        }

        @Override
        public int onDataRead(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endOfStream)
                throws Http2Exception {

            HTTPCarbonMessage sourceReqCMsg = streamIdRequestMap.get(streamId);
            if (sourceReqCMsg != null) {
                if (endOfStream) {
                    sourceReqCMsg.addHttpContent(new DefaultLastHttpContent(data.retain()));
                    streamIdRequestMap.remove(streamId);
                } else {
                    sourceReqCMsg.addHttpContent(new DefaultHttpContent(data.retain()));
                }
            } else {
                log.warn("Inconsistent state detected : data has received before headers");
            }
            return data.readableBytes() + padding;
        }

        /* Setup carbon message for HTTP2 request */
        private HTTPCarbonMessage setupHttp2CarbonMsg(Http2Headers headers) {

            String method = Constants.HTTP_GET_METHOD;
            if (headers.method() != null) {
                method = headers.getAndRemove(Constants.HTTP2_METHOD).toString();
            }
            String path = "";
            if (headers.path() != null) {
                path = headers.getAndRemove(Constants.HTTP2_PATH).toString();
            }

            // Construct new HTTP Carbon Request
            HttpRequest httpRequest = new DefaultHttpRequest(
                    new HttpVersion(Constants.HTTP_VERSION_2_0, true), HttpMethod.valueOf(method), path);

            HttpCarbonRequest sourceReqCMsg = setupCarbonRequest(httpRequest);

            // Remove PseudoHeaderNames from headers
            headers.getAndRemove(Constants.HTTP2_AUTHORITY);
            headers.getAndRemove(Constants.HTTP2_SCHEME);

            // Copy Http2 headers to carbon message
            headers.forEach(header ->
                                    sourceReqCMsg.setHeader(header.getKey().toString(), header.getValue().toString()));
            return sourceReqCMsg;
        }
    }

    /* Setup CarbonRequest from HttpRequest */
    private HttpCarbonRequest setupCarbonRequest(HttpRequest httpRequest) {

        HttpCarbonRequest sourceReqCMsg = new HttpCarbonRequest(httpRequest);
        if (httpRequest instanceof FullHttpRequest) {
            sourceReqCMsg.addHttpContent((FullHttpRequest) httpRequest);
        }
        sourceReqCMsg.setProperty(Constants.POOLED_BYTE_BUFFER_FACTORY, new PooledDataStreamerFactory(ctx.alloc()));
        sourceReqCMsg.setProperty(Constants.CHNL_HNDLR_CTX, this.ctx);
        sourceReqCMsg.setProperty(Constants.SRC_HANDLER, this);
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
}

