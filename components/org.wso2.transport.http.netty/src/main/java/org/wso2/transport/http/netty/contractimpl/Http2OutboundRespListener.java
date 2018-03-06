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

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.HttpConversionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.common.Util;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.Http2PushPromise;

import java.util.Locale;

/**
 * {@code Http2OutboundRespListener} is responsible for listening for
 * outbound response messages and delivering them to the client
 */
public class Http2OutboundRespListener implements HttpConnectorListener {

    private static final Logger log = LoggerFactory.getLogger(Http2OutboundRespListener.class);

    private HTTPCarbonMessage inboundRequestMsg;
    private ChannelHandlerContext ctx;
    private Http2ConnectionEncoder encoder;
    private int originalStreamId;
    private Http2Connection conn;
    private String serverName;

    public Http2OutboundRespListener(HTTPCarbonMessage inboundRequestMsg, ChannelHandlerContext ctx,
                                     Http2Connection conn, Http2ConnectionEncoder encoder, int streamId,
                                     String serverName) {
        this.inboundRequestMsg = inboundRequestMsg;
        this.ctx = ctx;
        this.conn = conn;
        this.encoder = encoder;
        this.originalStreamId = streamId;
        this.serverName = serverName;
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
                encoder.writePushPromise(
                        ctx, originalStreamId, promisedStreamId, pushPromise.getHeaders(), 0, ctx.newPromise());
                encoder.flowController().writePendingBytes();
                ctx.flush();
            } catch (Exception ex) {
                String errorMsg = "Failed to send push promise : " + ex.getMessage().toLowerCase(Locale.ENGLISH);
                log.error(errorMsg, ex);
                inboundRequestMsg.getHttpOutboundRespStatusFuture().notifyHttpListener(ex);
            }
        });
    }

    @Override
    public void onPushResponse(int promiseId, HTTPCarbonMessage outboundResponseMsg) {
        writeMessage(outboundResponseMsg, promiseId);
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

        public ResponseWriter(int streamId) {
            this.streamId = streamId;
        }

        public void writeOutboundResponse(HTTPCarbonMessage outboundResponseMsg, HttpContent httpContent)
                throws Http2Exception {

            if (!isHeaderWritten) {
                writeHeaders(outboundResponseMsg);
            }

            if (Util.isLastHttpContent(httpContent)) {
                writeData(httpContent, true);
            } else {
                writeData(httpContent, false);
            }
        }

        private void writeHeaders(HTTPCarbonMessage outboundResponseMsg) throws Http2Exception {
            outboundResponseMsg.getHeaders().
                    add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), Constants.HTTP_SCHEME);
            HttpMessage httpMessage;

            httpMessage =
                    Util.createHttpResponse(outboundResponseMsg, Constants.HTTP2_VERSION, serverName, true);

            // Construct Http2 headers
            Http2Headers http2Headers = HttpConversionUtil.toHttp2Headers(httpMessage, true);

            isHeaderWritten = true;
            encoder.writeHeaders(ctx, streamId, http2Headers, 0, false, ctx.newPromise());
            encoder.flowController().writePendingBytes();
            ctx.flush();
        }

        private void writeData(HttpContent httpContent, boolean endStream) throws Http2Exception {
            encoder.writeData(ctx, streamId, httpContent.content().retain(), 0, endStream, ctx.newPromise());
            encoder.flowController().writePendingBytes();
            ctx.flush();
        }
    }

    private synchronized int getNextStreamId() {
        return conn.local().incrementAndGetNextStreamId();
    }

}
