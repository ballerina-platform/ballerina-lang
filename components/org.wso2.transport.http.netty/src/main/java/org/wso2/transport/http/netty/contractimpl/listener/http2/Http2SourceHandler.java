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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpServerUpgradeHandler;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.util.internal.PlatformDependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.Http2OutboundRespListener;
import org.wso2.transport.http.netty.contractimpl.common.Util;
import org.wso2.transport.http.netty.contractimpl.common.Constants;
import org.wso2.transport.http.netty.contractimpl.listener.HttpServerChannelInitializer;
import org.wso2.transport.http.netty.contractimpl.listener.states.Http2MessageStateContext;
import org.wso2.transport.http.netty.contractimpl.listener.states.listener.http2.ReceivingHeaders;
import org.wso2.transport.http.netty.message.Http2DataFrame;
import org.wso2.transport.http.netty.message.Http2HeadersFrame;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpCarbonRequest;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;

import static org.wso2.transport.http.netty.contractimpl.listener.states.Http2StateUtil.notifyRequestListener;
import static org.wso2.transport.http.netty.contractimpl.listener.states.Http2StateUtil.setupCarbonRequest;

/**
 * {@code HTTP2SourceHandler} read the HTTP/2 binary frames sent from client through the channel.
 * <p>
 * This is also responsible for building the {@link HttpCarbonRequest} and forward to the listener
 * interested in request messages.
 */
public final class Http2SourceHandler extends ChannelInboundHandlerAdapter {

    // streamIdRequestMap contains mapping of http carbon messages vs stream id to support multiplexing
    private Map<Integer, HttpCarbonMessage> streamIdRequestMap = PlatformDependent.newConcurrentHashMap();
    private ChannelHandlerContext ctx;
    private ServerConnectorFuture serverConnectorFuture;
    private HttpServerChannelInitializer serverChannelInitializer;
    private Http2ConnectionEncoder encoder;
    private Http2Connection conn;
    private String interfaceId;
    private String serverName;
    private String remoteAddress;

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

            HttpCarbonRequest requestCarbonMessage = setupCarbonRequest(httpRequest, ctx, interfaceId);
            requestCarbonMessage.addHttpContent(new DefaultLastHttpContent(upgradedRequest.content()));
            notifyRequestListener(this, requestCarbonMessage, 1);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Http2HeadersFrame) {
            Http2HeadersFrame headersFrame = (Http2HeadersFrame) msg;
            Http2MessageStateContext http2MessageStateContext = new Http2MessageStateContext();
            http2MessageStateContext.setListenerState(new ReceivingHeaders(this, http2MessageStateContext));
            http2MessageStateContext.getListenerState().readInboundRequestHeaders(headersFrame);
        } else if (msg instanceof Http2DataFrame) {
            Http2DataFrame dataFrame = (Http2DataFrame) msg;
            int streamId = dataFrame.getStreamId();
            HttpCarbonMessage sourceReqCMsg = streamIdRequestMap.get(streamId);
            sourceReqCMsg.getHttp2MessageStateContext().getListenerState().readInboundRequestBody(dataFrame);
        } else {
            ctx.fireChannelRead(msg);
        }
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

    private void destroy() {
        streamIdRequestMap.clear();
    }

    public Map<Integer, HttpCarbonMessage> getStreamIdRequestMap() {
        return streamIdRequestMap;
    }

    public ChannelHandlerContext getChannelHandlerContext() {
        return ctx;
    }

    public ServerConnectorFuture getServerConnectorFuture() {
        return serverConnectorFuture;
    }

    public HttpServerChannelInitializer getServerChannelInitializer() {
        return serverChannelInitializer;
    }

    public Http2ConnectionEncoder getEncoder() {
        return encoder;
    }

    public Http2Connection getConnection() {
        return conn;
    }

    public String getInterfaceId() {
        return interfaceId;
    }

    public String getServerName() {
        return serverName;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }
}
