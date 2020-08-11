/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.transport.http.netty.contractimpl.listener.states.http2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Error;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.util.internal.logging.InternalLogLevel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.Http2OutboundRespListener;
import org.wso2.transport.http.netty.contractimpl.common.Util;
import org.wso2.transport.http.netty.contractimpl.common.states.Http2MessageStateContext;
import org.wso2.transport.http.netty.contractimpl.common.states.Http2StateUtil;
import org.wso2.transport.http.netty.contractimpl.listener.HttpServerChannelInitializer;
import org.wso2.transport.http.netty.contractimpl.listener.http2.Http2SourceHandler;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2DataEventListener;
import org.wso2.transport.http.netty.message.Http2DataFrame;
import org.wso2.transport.http.netty.message.Http2HeadersFrame;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.io.IOException;
import java.util.Calendar;

import static org.wso2.transport.http.netty.contract.Constants.ACCESS_LOG;
import static org.wso2.transport.http.netty.contract.Constants.ACCESS_LOG_FORMAT;
import static org.wso2.transport.http.netty.contract.Constants.HTTP_X_FORWARDED_FOR;
import static org.wso2.transport.http.netty.contract.Constants.IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_RESPONSE_BODY;
import static org.wso2.transport.http.netty.contract.Constants.REMOTE_CLIENT_CLOSED_WHILE_WRITING_OUTBOUND_RESPONSE_BODY;
import static org.wso2.transport.http.netty.contract.Constants.TO;
import static org.wso2.transport.http.netty.contractimpl.common.states.Http2StateUtil.validatePromisedStreamState;

/**
 * State between start and end of outbound response or push response entity body write.
 *
 * @since 6.0.241
 */
public class SendingEntityBody implements ListenerState {

    private static final Logger LOG = LoggerFactory.getLogger(SendingEntityBody.class);
    private static final InternalLogger ACCESS_LOGGER = InternalLoggerFactory.getInstance(ACCESS_LOG);

    private final Http2MessageStateContext http2MessageStateContext;
    private final ChannelHandlerContext ctx;
    private final HttpServerChannelInitializer serverChannelInitializer;
    private final Http2Connection conn;
    private final Http2ConnectionEncoder encoder;
    private final HttpResponseFuture outboundRespStatusFuture;
    private final HttpCarbonMessage inboundRequestMsg;
    private final Calendar inboundRequestArrivalTime;
    private final int originalStreamId;
    private final Http2OutboundRespListener http2OutboundRespListener;
    private HttpCarbonMessage outboundResponseMsg;

    private Long contentLength = 0L;
    private String remoteAddress;

    SendingEntityBody(Http2OutboundRespListener http2OutboundRespListener,
                      Http2MessageStateContext http2MessageStateContext) {
        this.http2OutboundRespListener = http2OutboundRespListener;
        this.http2MessageStateContext = http2MessageStateContext;
        this.ctx = http2OutboundRespListener.getChannelHandlerContext();
        this.serverChannelInitializer = http2OutboundRespListener.getServerChannelInitializer();
        this.conn = http2OutboundRespListener.getConnection();
        this.encoder = http2OutboundRespListener.getEncoder();
        this.inboundRequestMsg = http2OutboundRespListener.getInboundRequestMsg();
        this.outboundRespStatusFuture = inboundRequestMsg.getHttpOutboundRespStatusFuture();
        this.inboundRequestArrivalTime = http2OutboundRespListener.getInboundRequestArrivalTime();
        this.originalStreamId = http2OutboundRespListener.getOriginalStreamId();
        this.remoteAddress = http2OutboundRespListener.getRemoteAddress();
    }

    @Override
    public void readInboundRequestHeaders(ChannelHandlerContext ctx, Http2HeadersFrame headersFrame) {
        LOG.warn("readInboundRequestHeaders is not a dependant action of this state");
    }

    @Override
    public void readInboundRequestBody(Http2SourceHandler http2SourceHandler, Http2DataFrame dataFrame) throws
            Http2Exception {
        // In bidirectional streaming case, while sending the request data frames, server response data frames can
        // receive. In order to handle it. we need to change the states depending on the action.
        http2MessageStateContext.setListenerState(new ReceivingEntityBody(http2MessageStateContext));
        http2MessageStateContext.getListenerState().readInboundRequestBody(http2SourceHandler, dataFrame);
    }

    @Override
    public void writeOutboundResponseHeaders(Http2OutboundRespListener http2OutboundRespListener,
                                             HttpCarbonMessage outboundResponseMsg, HttpContent httpContent,
                                             int streamId) {
        LOG.warn("writeOutboundResponseHeaders is not a dependant action of this state");
    }

    @Override
    public void writeOutboundResponseBody(Http2OutboundRespListener http2OutboundRespListener,
                                          HttpCarbonMessage outboundResponseMsg, HttpContent httpContent,
                                          int streamId) throws Http2Exception {
        this.outboundResponseMsg = outboundResponseMsg;
        writeContent(http2OutboundRespListener, outboundResponseMsg, httpContent, streamId);
    }

    @Override
    public void writeOutboundPromise(Http2OutboundRespListener http2OutboundRespListener,
                                     Http2PushPromise pushPromise) throws Http2Exception {
        LOG.warn("writeOutboundPromise is not a dependant action of this state");
        throw new Http2Exception(Http2Error.PROTOCOL_ERROR,
                "WriteOutboundPromise is not a dependant action of SendingEntityBody state");
    }

    @Override
    public void handleStreamTimeout(ServerConnectorFuture serverConnectorFuture, ChannelHandlerContext ctx,
                                    Http2OutboundRespListener http2OutboundRespListener, int streamId) {
        try {
            serverConnectorFuture.notifyErrorListener(
                    new ServerConnectorException(IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_RESPONSE_BODY));
            LOG.error(IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_RESPONSE_BODY);
        } catch (ServerConnectorException e) {
            LOG.error("Error while notifying error state to server-connector listener");
        }
    }

    @Override
    public void handleAbruptChannelClosure(ServerConnectorFuture serverConnectorFuture, ChannelHandlerContext ctx,
                                           Http2OutboundRespListener http2OutboundRespListener, int streamId) {
        IOException connectionClose = new IOException(REMOTE_CLIENT_CLOSED_WHILE_WRITING_OUTBOUND_RESPONSE_BODY);
        outboundResponseMsg.setIoException(connectionClose);
        outboundRespStatusFuture.notifyHttpListener(connectionClose);

        LOG.error(REMOTE_CLIENT_CLOSED_WHILE_WRITING_OUTBOUND_RESPONSE_BODY);
    }

    private void writeContent(Http2OutboundRespListener http2OutboundRespListener,
                              HttpCarbonMessage outboundResponseMsg, HttpContent httpContent, int streamId)
            throws Http2Exception {
        if (httpContent instanceof LastHttpContent) {
            if (serverChannelInitializer.isHttpAccessLogEnabled()) {
                logAccessInfo(outboundResponseMsg, streamId);
            }

            final LastHttpContent lastContent = (httpContent == LastHttpContent.EMPTY_LAST_CONTENT) ?
                    new DefaultLastHttpContent() : (LastHttpContent) httpContent;
            HttpHeaders trailers = lastContent.trailingHeaders();
            trailers.add(outboundResponseMsg.getTrailerHeaders());
            boolean endStream = trailers.isEmpty();
            writeData(lastContent, streamId, endStream);
            if (!trailers.isEmpty()) {
                Http2Headers http2Trailers = HttpConversionUtil.toHttp2Headers(trailers, true);
                // Write trailing headers.
                Http2StateUtil.writeHttp2ResponseHeaders(ctx, encoder, outboundRespStatusFuture, streamId,
                                                         http2Trailers, true, http2OutboundRespListener);
            }
            http2OutboundRespListener.removeDefaultResponseWriter();
            http2MessageStateContext
                    .setListenerState(new ResponseCompleted(http2OutboundRespListener, http2MessageStateContext));
        } else {
            writeData(httpContent, streamId, false);
        }
    }

    private void writeData(HttpContent httpContent, int streamId, boolean endStream) throws Http2Exception {
        contentLength += httpContent.content().readableBytes();
        validatePromisedStreamState(originalStreamId, streamId, conn, inboundRequestMsg);
        final ByteBuf content = httpContent.content();
        for (Http2DataEventListener dataEventListener : http2OutboundRespListener.getHttp2ServerChannel()
                .getDataEventListeners()) {
            if (!dataEventListener.onDataWrite(ctx, streamId, content, endStream)) {
                break;
            }
        }
        ChannelFuture channelFuture = encoder.writeData(
                ctx, streamId, content, 0, endStream, ctx.newPromise());
        encoder.flowController().writePendingBytes();
        ctx.flush();
        if (endStream) {
            http2OutboundRespListener.getHttp2ServerChannel().getStreamIdRequestMap().remove(streamId);
            Util.checkForResponseWriteStatus(inboundRequestMsg, outboundRespStatusFuture, channelFuture);
        } else {
            Util.addResponseWriteFailureListener(outboundRespStatusFuture, channelFuture, http2OutboundRespListener);
        }
    }

    private void logAccessInfo(HttpCarbonMessage outboundResponseMsg, int streamId) {
        if (!ACCESS_LOGGER.isEnabled(InternalLogLevel.INFO)) {
            return;
        }
        if (originalStreamId != streamId) { // Skip access logs for server push messages
            LOG.debug("Access logging skipped for server push response");
            return;
        }
        HttpHeaders headers = inboundRequestMsg.getHeaders();
        if (headers.contains(HTTP_X_FORWARDED_FOR)) {
            String forwardedHops = headers.get(HTTP_X_FORWARDED_FOR);
            // If multiple IPs available, the first ip is the client
            int firstCommaIndex = forwardedHops.indexOf(',');
            remoteAddress = firstCommaIndex != -1 ? forwardedHops.substring(0, firstCommaIndex) : forwardedHops;
        }

        // Populate request parameters
        String userAgent = "-";
        if (headers.contains(HttpHeaderNames.USER_AGENT)) {
            userAgent = headers.get(HttpHeaderNames.USER_AGENT);
        }
        String referrer = "-";
        if (headers.contains(HttpHeaderNames.REFERER)) {
            referrer = headers.get(HttpHeaderNames.REFERER);
        }
        String method = inboundRequestMsg.getHttpMethod();
        String uri = (String) inboundRequestMsg.getProperty(TO);
        HttpMessage request = inboundRequestMsg.getNettyHttpRequest();
        String protocol;
        if (request != null) {
            protocol = request.protocolVersion().toString();
        } else {
            protocol = inboundRequestMsg.getHttpVersion();
        }

        // Populate response parameters
        int statusCode = Util.getHttpResponseStatus(outboundResponseMsg).code();

        ACCESS_LOGGER.log(InternalLogLevel.INFO, String.format(
                ACCESS_LOG_FORMAT, remoteAddress, inboundRequestArrivalTime, method, uri, protocol,
                statusCode, contentLength, referrer, userAgent));
    }
}
