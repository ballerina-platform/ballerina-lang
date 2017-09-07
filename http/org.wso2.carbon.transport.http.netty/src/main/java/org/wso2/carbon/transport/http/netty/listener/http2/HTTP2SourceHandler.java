/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.transport.http.netty.listener.http2;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpServerUpgradeHandler;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.Http2ConnectionDecoder;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2ConnectionHandler;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Flags;
import io.netty.handler.codec.http2.Http2FrameListener;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.PlatformDependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.carbon.transport.http.netty.sender.channel.pool.ConnectionManager;

import java.net.InetSocketAddress;
import java.util.Map;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * Class {@code HTTP2SourceHandler} will read the Http2 binary frames sent from client through the channel
 * and build carbon messages and sent to message processor.
 */
public final class HTTP2SourceHandler extends Http2ConnectionHandler implements Http2FrameListener {

    private static final Logger log = LoggerFactory.getLogger(HTTP2SourceHandler.class);
    /**
     * streamIdRequestMap contains mapping of http carbon messages to streamid to support multiplexing capability of.
     * http2 tcp connections
     */
    private Map<Integer, HTTPCarbonMessage> streamIdRequestMap = PlatformDependent.newConcurrentHashMap();
    private ConnectionManager connectionManager;
    private ListenerConfiguration listenerConfiguration;
    private ChannelHandlerContext ctx;

    HTTP2SourceHandler(Http2ConnectionDecoder decoder, Http2ConnectionEncoder encoder,
                       Http2Settings initialSettings, ConnectionManager connectionManager, ListenerConfiguration
                               listenerConfiguration) {
        super(decoder, encoder, initialSettings);
        this.listenerConfiguration = listenerConfiguration;
        this.connectionManager = connectionManager;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        this.ctx = ctx;
    }

    /**
     * This method handles the cleartext HTTP upgrade event. If an upgrade occurred, sends a simple response via HTTP/2
     * on stream 1 (the stream specifically reserved for cleartext HTTP upgrade).
     *
     * @param ctx Channel context
     * @param evt Event
     * @throws Exception Throws when user event trigger has an error
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof HttpServerUpgradeHandler.UpgradeEvent) {
            // Write an HTTP/2 response to the upgrade request
            Http2Headers headers =
                    new DefaultHttp2Headers().status(OK.codeAsText())
                            .set(new AsciiString(Constants.UPGRADE_RESPONSE_HEADER), new AsciiString("true"));
            encoder().writeHeaders(ctx, 1, headers, 0, true, ctx.newPromise());
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public int onDataRead(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endOfStream)
            throws Http2Exception {

        HTTPCarbonMessage cMsg = streamIdRequestMap.get(streamId);
        if (cMsg != null) {
            cMsg.addHttpContent(new DefaultLastHttpContent(data.retain()));
            if (endOfStream) {
                cMsg.setEndOfMsgAdded(true);
                if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
                    HTTPTransportContextHolder.getInstance().getHandlerExecutor().executeAtSourceRequestSending(cMsg);
                }
            }
        }
        return data.readableBytes() + padding;
    }


    public void onHeadersRead(ChannelHandlerContext ctx, int streamId,
                              Http2Headers headers, int padding, boolean endOfStream) throws Http2Exception {

        HTTPCarbonMessage cMsg = publishToMessageProcessor(streamId, headers);
        if (endOfStream) {
            cMsg.setEndOfMsgAdded(true);
            if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
                HTTPTransportContextHolder.getInstance().getHandlerExecutor().executeAtSourceRequestSending(cMsg);
            }
        }
    }

    @Override
    public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int streamDependency,
                              short weight, boolean exclusive, int padding, boolean endOfStream) throws Http2Exception {
        onHeadersRead(ctx, streamId, headers, padding, endOfStream);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (ctx != null && ctx.channel().isActive()) {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // Stop the connector timer
        ctx.close();
        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
            HTTPTransportContextHolder.getInstance().getHandlerExecutor()
                    .executeAtSourceConnectionTermination(Integer.toString(ctx.hashCode()));
        }
//        connectionManager.notifyChannelInactive();
    }


    /**
     * Carbon Message is published to registered message processor and Message Processor should return transport.
     * thread immediately
     *
     * @param streamId Stream id of HTTP2 request received
     * @param headers  HTTP2 Headers
     * @return HTTPCarbonMessage
     */
    private HTTPCarbonMessage publishToMessageProcessor(int streamId, Http2Headers headers) {
        HTTPCarbonMessage cMsg = setupCarbonMessage(streamId, headers);
        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
            HTTPTransportContextHolder.getInstance().getHandlerExecutor().executeAtSourceRequestReceiving(cMsg);
        }

        boolean continueRequest = true;

        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {

            continueRequest = HTTPTransportContextHolder.getInstance().getHandlerExecutor()
                    .executeRequestContinuationValidator(cMsg, carbonMessage -> {
                        CarbonCallback responseCallback = (CarbonCallback) cMsg
                                .getProperty(org.wso2.carbon.messaging.Constants.CALL_BACK);
                        responseCallback.done(carbonMessage);
                    });

        }
        if (continueRequest) {
            CarbonMessageProcessor carbonMessageProcessor = HTTPTransportContextHolder.getInstance()
                    .getMessageProcessor(listenerConfiguration.getMessageProcessorId());
            if (carbonMessageProcessor != null) {
                try {
                    carbonMessageProcessor.receive(cMsg, new HTTP2ResponseCallback(ctx, streamId));
                } catch (Exception e) {
                    log.error("Error while submitting CarbonMessage to CarbonMessageProcessor", e);
                }
            } else {
                log.error("Cannot find registered MessageProcessor for forward the message");
            }
        }

        return cMsg;
    }

    /**
     * Setup carbon message for HTTP2 request.
     *
     * @param streamId Stream id of HTTP2 request received
     * @param headers  HTTP2 Headers
     * @return HTTPCarbonMessage
     */
    protected HTTPCarbonMessage setupCarbonMessage(int streamId, Http2Headers headers) {

        // Construct new HTTP carbon message and put into stream id request map
        HTTPCarbonMessage cMsg = new HTTPCarbonMessage();
        cMsg.setProperty(Constants.PORT, ((InetSocketAddress) ctx.channel().remoteAddress()).getPort());
        cMsg.setProperty(Constants.HOST, ((InetSocketAddress) ctx.channel().remoteAddress()).getHostName());
        cMsg.setProperty(Constants.SCHEME, listenerConfiguration.getScheme());
        cMsg.setProperty(Constants.HTTP_VERSION, Constants.HTTP2_VERSION);
        cMsg.setProperty(org.wso2.carbon.messaging.Constants.LISTENER_PORT,
                ((InetSocketAddress) ctx.channel().localAddress()).getPort());
        cMsg.setProperty(org.wso2.carbon.messaging.Constants.LISTENER_INTERFACE_ID, listenerConfiguration.getId());
        cMsg.setProperty(org.wso2.carbon.messaging.Constants.PROTOCOL, Constants.PROTOCOL_NAME);
        if (listenerConfiguration.getSslConfig() != null) {
            cMsg.setProperty(Constants.IS_SECURED_CONNECTION, true);
        } else {
            cMsg.setProperty(Constants.IS_SECURED_CONNECTION, false);
        }
        cMsg.setProperty(Constants.LOCAL_ADDRESS, ctx.channel().localAddress());
        cMsg.setProperty(Constants.LOCAL_NAME, ((InetSocketAddress) ctx.channel().localAddress()).getHostName());
        cMsg.setProperty(Constants.REMOTE_ADDRESS, ctx.channel().remoteAddress());
        cMsg.setProperty(Constants.REMOTE_HOST, ((InetSocketAddress) ctx.channel().remoteAddress()).getHostName());
        cMsg.setProperty(Constants.REMOTE_PORT, ((InetSocketAddress) ctx.channel().remoteAddress()).getPort());
        ChannelHandler handler = ctx.handler();
        cMsg.setProperty(Constants.CHANNEL_ID, ((HTTP2SourceHandler) handler).getListenerConfiguration().getId());
        cMsg.setProperty(Constants.STREAM_ID, streamId);
        if (headers.path() != null) {
            String path = headers.getAndRemove(Constants.HTTP2_PATH).toString();
            cMsg.setProperty(Constants.TO, path);
            cMsg.setProperty(Constants.REQUEST_URL, path);
        }
        if (headers.method() != null) {
            String method = headers.getAndRemove(Constants.HTTP2_METHOD).toString();
            cMsg.setProperty(Constants.HTTP_METHOD, method);
        }
        // Remove PseudoHeaderNames from headers
        headers.getAndRemove(Constants.HTTP2_AUTHORITY);
        headers.getAndRemove(Constants.HTTP2_SCHEME);

        // Copy Http2 headers to carbon message
        headers.forEach(k -> cMsg.setHeader(k.getKey().toString(), k.getValue().toString()));
        streamIdRequestMap.put(streamId, cMsg);
        return cMsg;
    }

    @Override
    public void onPriorityRead(ChannelHandlerContext ctx, int streamId, int streamDependency,
                               short weight, boolean exclusive) {
    }

    @Override
    public void onRstStreamRead(ChannelHandlerContext ctx, int streamId, long errorCode) {
    }

    @Override
    public void onSettingsAckRead(ChannelHandlerContext ctx) {
    }

    @Override
    public void onSettingsRead(ChannelHandlerContext ctx, Http2Settings settings) {
    }

    @Override
    public void onPingRead(ChannelHandlerContext ctx, ByteBuf data) {
    }

    @Override
    public void onPingAckRead(ChannelHandlerContext ctx, ByteBuf data) {
    }

    @Override
    public void onPushPromiseRead(ChannelHandlerContext ctx, int streamId, int promisedStreamId,
                                  Http2Headers headers, int padding) {
    }

    @Override
    public void onGoAwayRead(ChannelHandlerContext ctx, int lastStreamId, long errorCode, ByteBuf debugData) {
        if (log.isDebugEnabled()) {
            if (errorCode != 0 && debugData.isReadable()) {
                int contentLength = debugData.readableBytes();
                byte[] arr = new byte[contentLength];
                debugData.readBytes(arr);
                log.debug("Error occurred while closing the client connection " + new String(arr, 0, contentLength,
                        CharsetUtil.UTF_8));
            }
        }
    }

    @Override
    public void onWindowUpdateRead(ChannelHandlerContext ctx, int streamId, int windowSizeIncrement) {
    }

    @Override
    public void onUnknownFrame(ChannelHandlerContext ctx, byte frameType, int streamId,
                               Http2Flags flags, ByteBuf payload) {
    }

    public ListenerConfiguration getListenerConfiguration() {
        return listenerConfiguration;
    }

}

