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
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.util.internal.logging.InternalLogLevel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.common.Util;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.listener.HttpServerChannelInitializer;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.Http2PushPromise;

import java.util.Calendar;
import java.util.Locale;

/**
 * {@code Http2OutboundRespListener} is responsible for listening for outbound response messages
 * and delivering them to the client.
 */
public class Http2OutboundRespListener implements HttpConnectorListener {

    private static final Logger log = LoggerFactory.getLogger(Http2OutboundRespListener.class);
    private static final InternalLogger accessLogger = InternalLoggerFactory.getInstance(Constants.ACCESS_LOG);

    private HTTPCarbonMessage inboundRequestMsg;
    private ChannelHandlerContext ctx;
    private Http2ConnectionEncoder encoder;
    private int originalStreamId;   // stream id of the request received from the client
    private Http2Connection conn;
    private String serverName;
    private HttpResponseFuture outboundRespStatusFuture;
    private HttpServerChannelInitializer serverChannelInitializer;
    private Calendar inboundRequestArrivalTime;
    private String remoteAddress = "-";

    public Http2OutboundRespListener(HttpServerChannelInitializer serverChannelInitializer,
                                     HTTPCarbonMessage inboundRequestMsg, ChannelHandlerContext ctx,
                                     Http2Connection conn, Http2ConnectionEncoder encoder, int streamId,
                                     String serverName, String remoteAddress) {
        this.serverChannelInitializer = serverChannelInitializer;
        this.inboundRequestMsg = inboundRequestMsg;
        this.ctx = ctx;
        this.conn = conn;
        this.encoder = encoder;
        this.originalStreamId = streamId;
        this.serverName = serverName;
        if (remoteAddress != null) {
            this.remoteAddress = remoteAddress;
        }
        this.outboundRespStatusFuture = inboundRequestMsg.getHttpOutboundRespStatusFuture();
        inboundRequestArrivalTime = Calendar.getInstance();
    }

    @Override
    public void onMessage(HTTPCarbonMessage outboundResponseMsg) {
        writeMessage(outboundResponseMsg, originalStreamId);
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("Couldn't send the outbound response", throwable);
    }

    public void onPushPromise(Http2PushPromise pushPromise) {
        ctx.channel().eventLoop().execute(() -> {
            try {
                int promisedStreamId = getNextStreamId();
                // Update streamIds
                pushPromise.setPromisedStreamId(promisedStreamId);
                pushPromise.setStreamId(originalStreamId);
                // Construct http request
                HttpRequest httpRequest = pushPromise.getHttpRequest();
                httpRequest.headers().add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), Constants.HTTP_SCHEME);
                // A push promise is a server initiated request, hence it should contain request headers
                Http2Headers http2Headers =
                        HttpConversionUtil.toHttp2Headers(httpRequest, true);
                // Write the push promise to the wire
                ChannelFuture channelFuture = encoder.writePushPromise(
                        ctx, originalStreamId, promisedStreamId, http2Headers, 0, ctx.newPromise());
                encoder.flowController().writePendingBytes();
                ctx.flush();
                Util.checkForResponseWriteStatus(inboundRequestMsg, outboundRespStatusFuture, channelFuture);
            } catch (Exception ex) {
                String errorMsg = "Failed to send push promise : " + ex.getMessage().toLowerCase(Locale.ENGLISH);
                log.error(errorMsg, ex);
                inboundRequestMsg.getHttpOutboundRespStatusFuture().notifyHttpListener(ex);
            }
        });
    }

    @Override
    public void onPushResponse(int promiseId, HTTPCarbonMessage outboundResponseMsg) {
        if (isValidStreamId(promiseId)) {
            writeMessage(outboundResponseMsg, promiseId);
        } else {
            inboundRequestMsg.getHttpOutboundRespStatusFuture().notifyHttpListener(
                    new ServerConnectorException("Promise is already rejected or stream is no longer valid"));
        }
    }

    private void writeMessage(HTTPCarbonMessage outboundResponseMsg, int streamId) {
        ResponseWriter writer = new ResponseWriter(streamId);
        ctx.channel().eventLoop().execute(() -> outboundResponseMsg.getHttpContentAsync().setMessageListener(
                httpContent -> ctx.channel().eventLoop().execute(() -> {
                    try {
                        writer.writeOutboundResponse(outboundResponseMsg, httpContent);
                    } catch (Http2Exception ex) {
                        String errorMsg = "Failed to send the outbound response : " +
                                          ex.getMessage().toLowerCase(Locale.ENGLISH);
                        log.error(errorMsg, ex);
                        inboundRequestMsg.getHttpOutboundRespStatusFuture().notifyHttpListener(ex);
                    }
                })));
    }

    private class ResponseWriter {

        private boolean isHeaderWritten = false;
        private int streamId;
        private Long contentLength = 0L;

        public ResponseWriter(int streamId) {
            this.streamId = streamId;
        }

        private void writeOutboundResponse(HTTPCarbonMessage outboundResponseMsg, HttpContent httpContent)
                throws Http2Exception {
            if (!isHeaderWritten) {
                writeHeaders(outboundResponseMsg);
            }
            if (Util.isLastHttpContent(httpContent)) {
                writeData(httpContent, true);
                if (serverChannelInitializer.isHttpAccessLogEnabled()) {
                    logAccessInfo(outboundResponseMsg);
                }
            } else {
                writeData(httpContent, false);
            }
        }

        private void writeHeaders(HTTPCarbonMessage outboundResponseMsg) throws Http2Exception {
            outboundResponseMsg.getHeaders().
                    add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), Constants.HTTP_SCHEME);
            HttpMessage httpMessage =
                    Util.createHttpResponse(outboundResponseMsg, Constants.HTTP2_VERSION, serverName, true);
            // Construct Http2 headers
            Http2Headers http2Headers = HttpConversionUtil.toHttp2Headers(httpMessage, true);

            isHeaderWritten = true;
            ChannelFuture channelFuture =
                    encoder.writeHeaders(ctx, streamId, http2Headers, 0, false, ctx.newPromise());
            encoder.flowController().writePendingBytes();
            ctx.flush();
            Util.addResponseWriteFailureListener(outboundRespStatusFuture, channelFuture);
        }

        private void writeData(HttpContent httpContent, boolean endStream) throws Http2Exception {
            contentLength += httpContent.content().readableBytes();
            ChannelFuture channelFuture = encoder.writeData(
                    ctx, streamId, httpContent.content().retain(), 0, endStream, ctx.newPromise());
            encoder.flowController().writePendingBytes();
            ctx.flush();
            if (endStream) {
                Util.checkForResponseWriteStatus(inboundRequestMsg, outboundRespStatusFuture, channelFuture);
            } else {
                Util.addResponseWriteFailureListener(outboundRespStatusFuture, channelFuture);
            }
        }

        private void logAccessInfo(HTTPCarbonMessage outboundResponseMsg) {

            if (!accessLogger.isEnabled(InternalLogLevel.INFO)) {
                return;
            }

            HttpHeaders headers = inboundRequestMsg.getHeaders();
            if (headers.contains(Constants.HTTP_X_FORWARDED_FOR)) {
                // can contain multiple IPs for proxy chains. the first ip is the client.
                String proxyChain = headers.get(Constants.HTTP_X_FORWARDED_FOR);
                int firstComma = proxyChain.indexOf(',');
                if (firstComma != -1) {
                    remoteAddress = proxyChain.substring(0, proxyChain.indexOf(','));
                } else {
                    remoteAddress = proxyChain;
                }
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
            String method = (String) inboundRequestMsg.getProperty(Constants.HTTP_METHOD);
            String uri = (String) inboundRequestMsg.getProperty(Constants.TO);
            String protocol = (String) inboundRequestMsg.getProperty(Constants.HTTP_VERSION);

            // Populate response parameters
            int statusCode = Util.getHttpResponseStatus(outboundResponseMsg).code();

            accessLogger.log(InternalLogLevel.INFO, String.format(
                    Constants.ACCESS_LOG_FORMAT, remoteAddress, inboundRequestArrivalTime, method, uri, protocol,
                    statusCode, contentLength, referrer, userAgent));
        }
    }

    private synchronized int getNextStreamId() {
        return conn.local().incrementAndGetNextStreamId();
    }

    private boolean isValidStreamId(int streamId) {
        return conn.stream(streamId) != null;
    }

}

