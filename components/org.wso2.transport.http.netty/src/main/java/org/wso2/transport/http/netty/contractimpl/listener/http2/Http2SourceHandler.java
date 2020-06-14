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
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2RemoteFlowController;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.common.states.Http2MessageStateContext;
import org.wso2.transport.http.netty.contractimpl.listener.HttpServerChannelInitializer;
import org.wso2.transport.http.netty.contractimpl.listener.states.http2.EntityBodyReceived;
import org.wso2.transport.http.netty.contractimpl.listener.states.http2.ReceivingHeaders;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2DataEventListener;
import org.wso2.transport.http.netty.message.Http2DataFrame;
import org.wso2.transport.http.netty.message.Http2HeadersFrame;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpCarbonRequest;
import org.wso2.transport.http.netty.message.ServerRemoteFlowControlListener;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.wso2.transport.http.netty.contract.Constants.ENDPOINT_TIMEOUT;
import static org.wso2.transport.http.netty.contract.Constants.STREAM_ID_ONE;
import static org.wso2.transport.http.netty.contractimpl.common.states.Http2StateUtil.notifyRequestListener;
import static org.wso2.transport.http.netty.contractimpl.common.states.Http2StateUtil.setupCarbonRequest;

/**
 * {@code HTTP2SourceHandler} read the HTTP/2 binary frames sent from client through the channel.
 * <p>
 * This is also responsible for building the {@link HttpCarbonRequest} and forward to the listener
 * interested in request messages.
 */
public final class Http2SourceHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(Http2SourceHandler.class);

    private Http2ServerChannel http2ServerChannel = new Http2ServerChannel();
    private ChannelHandlerContext ctx;
    private ServerConnectorFuture serverConnectorFuture;
    private HttpServerChannelInitializer serverChannelInitializer;
    private Http2ConnectionEncoder encoder;
    private Http2Connection conn;
    private String interfaceId;
    private String serverName;
    private String remoteHost;
    private Map<String, GenericObjectPool> targetChannelPool; //Keeps only h1 target channels
    private ServerRemoteFlowControlListener serverRemoteFlowControlListener;
    private SocketAddress remoteAddress;

    Http2SourceHandler(HttpServerChannelInitializer serverChannelInitializer, Http2ConnectionEncoder encoder,
                       String interfaceId, Http2Connection conn, ServerConnectorFuture serverConnectorFuture,
                       String serverName) {
        this.serverChannelInitializer = serverChannelInitializer;
        this.encoder = encoder;
        this.interfaceId = interfaceId;
        this.serverConnectorFuture = serverConnectorFuture;
        this.conn = conn;
        this.serverName = serverName;
        this.targetChannelPool = new ConcurrentHashMap<>();
        setRemoteFlowController();
        setDataEventListeners();
    }

    private void setDataEventListeners() {
        long serverTimeout = serverChannelInitializer.getSocketIdleTimeout() <= 0 ? ENDPOINT_TIMEOUT :
                serverChannelInitializer.getSocketIdleTimeout();
        http2ServerChannel.addDataEventListener(Constants.IDLE_STATE_HANDLER,
                                                new Http2ServerTimeoutHandler(serverTimeout, http2ServerChannel,
                                                                              serverConnectorFuture));
    }

    private void setRemoteFlowController() {
        Http2RemoteFlowController remoteFlowController = this.conn.remote().flowController();
        serverRemoteFlowControlListener = new ServerRemoteFlowControlListener(remoteFlowController);
        remoteFlowController.listener(serverRemoteFlowControlListener);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        this.ctx = ctx;
        // Populate remote address
        this.remoteAddress = ctx.channel().remoteAddress();
        if (this.remoteAddress instanceof InetSocketAddress) {
            remoteHost = ((InetSocketAddress) this.remoteAddress).getAddress().toString();
            if (remoteHost.startsWith("/")) {
                remoteHost = remoteHost.substring(1);
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

            HttpCarbonRequest requestCarbonMessage = setupCarbonRequest(httpRequest, this, STREAM_ID_ONE);
            requestCarbonMessage.addHttpContent(new DefaultLastHttpContent(upgradedRequest.content()));
            requestCarbonMessage.setLastHttpContentArrived();
            InboundMessageHolder inboundMsgHolder = new InboundMessageHolder(requestCarbonMessage);
            if (requestCarbonMessage.getHttp2MessageStateContext() == null) {
                Http2MessageStateContext http2MessageStateContext = new Http2MessageStateContext();
                http2MessageStateContext.setListenerState(new EntityBodyReceived(http2MessageStateContext));
                requestCarbonMessage.setHttp2MessageStateContext(http2MessageStateContext);
            }
            http2ServerChannel.getStreamIdRequestMap().put(STREAM_ID_ONE, inboundMsgHolder);
            http2ServerChannel.getDataEventListeners()
                    .forEach(dataEventListener -> dataEventListener.onStreamInit(ctx, STREAM_ID_ONE));
            notifyRequestListener(this, inboundMsgHolder, STREAM_ID_ONE);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Http2Exception {
        if (msg instanceof Http2HeadersFrame) {
            Http2HeadersFrame headersFrame = (Http2HeadersFrame) msg;
            Http2MessageStateContext http2MessageStateContext = new Http2MessageStateContext();
            http2MessageStateContext.setListenerState(new ReceivingHeaders(this, http2MessageStateContext));
            http2MessageStateContext.getListenerState().readInboundRequestHeaders(ctx, headersFrame);
        } else if (msg instanceof Http2DataFrame) {
            Http2DataFrame dataFrame = (Http2DataFrame) msg;
            int streamId = dataFrame.getStreamId();
            HttpCarbonMessage sourceReqCMsg = http2ServerChannel.getInboundMessage(streamId)
                    .getInboundMsg();
            // CarbonMessage can be already removed from the map once the LastHttpContent is added because of receiving
            // a data frame when the outbound response is started to send. So, the data frames received after that
            // should be released.
            if (sourceReqCMsg == null) {
                dataFrame.getData().release();
            } else {
                sourceReqCMsg.getHttp2MessageStateContext().getListenerState().readInboundRequestBody(this, dataFrame);
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Channel inactive event received in HTTP2SourceHandler");
        }
        destroy();
        closeTargetChannels();
        ctx.fireChannelInactive();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        destroy();
        ctx.fireChannelUnregistered();
    }

    private void destroy() {
        //Handle channel close for all the streams in the connection.
        LOG.debug("Inbound request map size {}", http2ServerChannel.getStreamIdRequestMap().size());
        http2ServerChannel.getStreamIdRequestMap().forEach((streamId, inboundMessageHolder) -> {
            HttpCarbonMessage inboundMsg = inboundMessageHolder.getInboundMsg();
            LOG.debug("Listener state {}", inboundMsg.getHttp2MessageStateContext().getListenerState());
            inboundMsg.getHttp2MessageStateContext().getListenerState()
                    .handleAbruptChannelClosure(serverConnectorFuture, getChannelHandlerContext(),
                                                inboundMessageHolder.getHttp2OutboundRespListener(), streamId);
            inboundMessageHolder.getHttp2OutboundRespListener().removeDefaultResponseWriter();
            inboundMessageHolder.getHttp2OutboundRespListener().removeBackPressureListener();
        });
        http2ServerChannel.getDataEventListeners().forEach(Http2DataEventListener::destroy);
        http2ServerChannel.destroy();
    }

    private void closeTargetChannels() {
        targetChannelPool.forEach((hostPortKey, genericObjectPool) -> {
            try {
                targetChannelPool.remove(hostPortKey).close();
            } catch (Exception e) {
                LOG.error("Couldn't close target channel socket connections", e);
            }
        });
    }

    public Map<Integer, InboundMessageHolder> getStreamIdRequestMap() {
        return http2ServerChannel.getStreamIdRequestMap();
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

    public String getRemoteHost() {
        return remoteHost;
    }
    public Map<String, GenericObjectPool> getTargetChannelPool() {
        return targetChannelPool;
    }

    public ChannelHandlerContext getInboundChannelContext() {
        return ctx;
    }

    public ServerRemoteFlowControlListener getServerRemoteFlowControlListener() {
        return serverRemoteFlowControlListener;
    }

    public Http2ServerChannel getHttp2ServerChannel() {
        return http2ServerChannel;
    }

    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }
}
