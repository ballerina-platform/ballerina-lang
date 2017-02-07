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
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.HttpConversionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.messaging.MessageDataSource;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.internal.HTTPTransportContextHolder;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

import java.nio.ByteBuffer;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * {@code HTTP2ResponseCallback} is the class implements {@link CarbonCallback} interface to process http2 message
 * responses coming from message processor
 */
public class HTTP2ResponseCallback implements CarbonCallback {

    private ChannelHandlerContext ctx;
    // Stream id of the channel of initial request
    private int streamId;
    private static final Logger logger = LoggerFactory.getLogger(HTTP2ResponseCallback.class);

    /**
     * Construct a new {@link HTTP2ResponseCallback} to process HTTP2 responses
     *
     * @param channelHandlerContext Channel context
     * @param streamId              Stream Id
     */
    public HTTP2ResponseCallback(ChannelHandlerContext channelHandlerContext, int streamId) {

        this.ctx = channelHandlerContext;
        this.streamId = streamId;
    }

    public void done(CarbonMessage cMsg) {

        handleResponsesWithoutContentLength(cMsg);
        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
            HTTPTransportContextHolder.getInstance().getHandlerExecutor().executeAtSourceResponseReceiving(cMsg);
        }
        Http2Headers http2Headers = new DefaultHttp2Headers().status(OK.codeAsText());
        cMsg.getHeaders().getAll().forEach(k -> http2Headers.set(k.getName().toLowerCase(), k.getValue()));
        http2Headers.set(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text(), Integer.toString(streamId));

        if (ctx.handler() instanceof HTTP2SourceHandler) {
            HTTP2SourceHandler http2SourceHandler = (HTTP2SourceHandler) ctx.handler();
            http2SourceHandler.encoder().writeHeaders(ctx, streamId, http2Headers, 0, false,
                    ctx.newPromise());

            // Full HTTP2 Request response
            if (cMsg instanceof HTTPCarbonMessage) {
                HTTPCarbonMessage nettyCMsg = (HTTPCarbonMessage) cMsg;
                while (true) {
                    if (nettyCMsg.isEndOfMsgAdded() && nettyCMsg.isEmpty()) {
                        http2SourceHandler.encoder().writeData(ctx, streamId, LastHttpContent
                                .EMPTY_LAST_CONTENT.content().retain(), 0, true, ctx
                                .newPromise());
                        break;
                    }
                    HttpContent httpContent = nettyCMsg.getHttpContent();
                    if (httpContent instanceof LastHttpContent) {
                        http2SourceHandler.encoder().writeData(ctx, streamId, httpContent.content()
                                .retain(), 0, true, ctx.newPromise());
                        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
                            HTTPTransportContextHolder.getInstance().getHandlerExecutor()
                                    .executeAtSourceResponseSending(cMsg);
                        }
                        break;
                    }
                    http2SourceHandler.encoder().writeData(ctx, streamId, httpContent.content(), 0, false,
                            ctx.newPromise());
                }

                // HTTP2 Header Request response
            } else if (cMsg instanceof DefaultCarbonMessage) {
                DefaultCarbonMessage defaultCMsg = (DefaultCarbonMessage) cMsg;
                while (true) {
                    ByteBuffer byteBuffer = defaultCMsg.getMessageBody();
                    ByteBuf bbuf = Unpooled.wrappedBuffer(byteBuffer);
                    http2SourceHandler.encoder().writeData(ctx, streamId, bbuf.retain(), 0, false, ctx
                            .newPromise());
                    if (defaultCMsg.isEndOfMsgAdded() && defaultCMsg.isEmpty()) {
                        http2SourceHandler.encoder().writeData(ctx, streamId, LastHttpContent
                                .EMPTY_LAST_CONTENT.content().retain(), 0, true, ctx.newPromise());
                        if (HTTPTransportContextHolder.getInstance().getHandlerExecutor() != null) {
                            HTTPTransportContextHolder.getInstance().getHandlerExecutor().
                                    executeAtSourceResponseSending(cMsg);
                        }
                        break;
                    }
                }
            }
            try {
                http2SourceHandler.flush(ctx);
            } catch (Http2Exception e) {
                logger.error("Error occurred while sending response to client", e.getMessage());
            }
        }
    }

    /**
     * Handles the response without content length and set or remove headers based on carbon message
     *
     * @param cMsg Carbon Message
     */
    private void handleResponsesWithoutContentLength(CarbonMessage cMsg) {
        if (cMsg.isAlreadyRead()) {
            MessageDataSource messageDataSource = cMsg.getMessageDataSource();
            if (messageDataSource != null) {
                messageDataSource.serializeData();
                cMsg.setEndOfMsgAdded(true);
                cMsg.getHeaders().remove(Constants.HTTP_CONTENT_LENGTH);
            } else {
                logger.error("Message is already built but cannot find the MessageDataSource");
            }
        }
        if (cMsg.getHeader(Constants.HTTP_TRANSFER_ENCODING) == null
                && cMsg.getHeader(Constants.HTTP_CONTENT_LENGTH) == null) {
            cMsg.setHeader(Constants.HTTP_CONTENT_LENGTH, String.valueOf(cMsg.getFullMessageLength()));

        }
    }
}
