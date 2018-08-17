/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.listener.states.sender;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Util;
import org.wso2.transport.http.netty.config.ChunkConfig;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import static org.wso2.transport.http.netty.common.Constants.IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_REQUEST;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_SERVER_CLOSED_WHILE_WRITING_OUTBOUND_REQUEST;
import static org.wso2.transport.http.netty.common.Util.isLastHttpContent;
import static org.wso2.transport.http.netty.common.Util.setupChunkedRequest;

/**
 * State between start and end of outbound response entity body write
 */
public class SendingEntityBody implements SenderState {

    private static Logger log = LoggerFactory.getLogger(SendingEntityBody.class);

    @Override
    public void writeOutboundRequestHeaders(HttpCarbonMessage httpOutboundRequest,
                                            HttpContent httpContent) {

    }

    @Override
    public void writeOutboundRequestEntityBody(
            HttpCarbonMessage httpOutboundRequest, HttpContent httpContent) {



        if (isLastHttpContent(httpContent)) {
            if (!this.requestHeaderWritten) {
                // this means we need to send an empty payload
                // depending on the http verb
                if (Util.isEntityBodyAllowed(getHttpMethod(httpOutboundRequest))) {
                    if (chunkConfig == ChunkConfig.ALWAYS && checkChunkingCompatibility()) {
                        setupChunkedRequest(httpOutboundRequest);
                    } else {
                        contentLength += httpContent.content().readableBytes();
                        Util.setupContentLengthRequest(httpOutboundRequest, contentLength);
                    }
                }
                writeOutboundRequestHeaders(httpOutboundRequest);
            }

            writeOutboundRequestBody(httpContent);
        } else {
            if ((chunkConfig == ChunkConfig.ALWAYS || chunkConfig == ChunkConfig.AUTO) &&
                    checkChunkingCompatibility()) {
                if (!this.requestHeaderWritten) {
                    setupChunkedRequest(httpOutboundRequest);
                    writeOutboundRequestHeaders(httpOutboundRequest);
                }
                this.getChannel().writeAndFlush(httpContent);
            } else {
                this.contentList.add(httpContent);
                contentLength += httpContent.content().readableBytes();
            }
        }

    }

    @Override
    public void readInboundResponseHeaders() {

    }

    @Override
    public void readInboundResponseEntityBody() {

    }

    @Override
    public void handleAbruptChannelClosure(ServerConnectorFuture serverConnectorFuture) {
        // HttpResponseFuture will be notified asynchronously via Target channel.
        log.error(REMOTE_SERVER_CLOSED_WHILE_WRITING_OUTBOUND_REQUEST);

    }

    @Override
    public void handleIdleTimeoutConnectionClosure(ServerConnectorFuture serverConnectorFuture,
                                                            ChannelHandlerContext ctx,
                                                            IdleStateEvent evt) {
        // HttpResponseFuture will be notified asynchronously via Target channel.
        log.error("Error in HTTP client: {}", IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_REQUEST);
    }
}
