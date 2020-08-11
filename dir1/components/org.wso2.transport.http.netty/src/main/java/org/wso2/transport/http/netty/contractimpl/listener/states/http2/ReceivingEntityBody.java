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
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http2.Http2Exception;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.Http2OutboundRespListener;
import org.wso2.transport.http.netty.contractimpl.common.states.Http2MessageStateContext;
import org.wso2.transport.http.netty.contractimpl.common.states.Http2StateUtil;
import org.wso2.transport.http.netty.contractimpl.listener.http2.Http2SourceHandler;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2DataEventListener;
import org.wso2.transport.http.netty.message.Http2DataFrame;
import org.wso2.transport.http.netty.message.Http2HeadersFrame;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import static io.netty.handler.codec.http.HttpResponseStatus.REQUEST_TIMEOUT;
import static org.wso2.transport.http.netty.contract.Constants.REMOTE_CLIENT_CLOSED_WHILE_READING_INBOUND_REQUEST_BODY;
import static org.wso2.transport.http.netty.contractimpl.common.states.Http2StateUtil.writeHttp2Promise;
import static org.wso2.transport.http.netty.contractimpl.common.states.StateUtil.handleIncompleteInboundMessage;

/**
 * State between start and end of inbound request payload read.
 *
 * @since 6.0.241
 */
public class ReceivingEntityBody implements ListenerState {

    private static final Logger LOG = LoggerFactory.getLogger(ReceivingEntityBody.class);

    private final Http2MessageStateContext http2MessageStateContext;

    ReceivingEntityBody(Http2MessageStateContext http2MessageStateContext) {
        this.http2MessageStateContext = http2MessageStateContext;
    }

    @Override
    public void readInboundRequestHeaders(ChannelHandlerContext ctx, Http2HeadersFrame headersFrame) {
        LOG.warn("readInboundRequestHeaders is not a dependant action of this state");
    }

    @Override
    public void readInboundRequestBody(Http2SourceHandler http2SourceHandler, Http2DataFrame dataFrame) {
        int streamId = dataFrame.getStreamId();
        ByteBuf data = dataFrame.getData();
        HttpCarbonMessage sourceReqCMsg = http2SourceHandler.getStreamIdRequestMap().get(streamId)
                .getInboundMsg();
        if (sourceReqCMsg != null) {
            for (Http2DataEventListener listener : http2SourceHandler.getHttp2ServerChannel().getDataEventListeners()) {
                listener.onDataRead(http2SourceHandler.getChannelHandlerContext(), streamId, data,
                                    dataFrame.isEndOfStream());
            }
            if (dataFrame.isEndOfStream()) {
                sourceReqCMsg.addHttpContent(new DefaultLastHttpContent(data));
                sourceReqCMsg.setLastHttpContentArrived();
                http2MessageStateContext.setListenerState(new EntityBodyReceived(http2MessageStateContext));
            } else {
                sourceReqCMsg.addHttpContent(new DefaultHttpContent(data));
            }
        } else {
            LOG.warn("Inconsistent state detected : data has been received before headers");
        }
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
        // When receiving entity body, if payload is not consumed by the server, this method is invoked if server is
        // going to send the response back.
        // This conditional check is needed because, either to write response headers or response body, response
        // writer calls this method. So we need to check whether headers sent to change the state.
        if (http2MessageStateContext.isHeadersSent()) {
            // response header already sent. move the state to SendingEntityBody.
            http2MessageStateContext.setListenerState(
                    new SendingEntityBody(http2OutboundRespListener, http2MessageStateContext));
            http2MessageStateContext.getListenerState()
                    .writeOutboundResponseBody(http2OutboundRespListener, outboundResponseMsg, httpContent, streamId);
        } else {
            Http2StateUtil.beginResponseWrite(http2MessageStateContext, http2OutboundRespListener,
                                              outboundResponseMsg, httpContent, streamId);
        }
    }

    @Override
    public void writeOutboundPromise(Http2OutboundRespListener http2OutboundRespListener,
                                     Http2PushPromise pushPromise) throws Http2Exception {
        writeHttp2Promise(pushPromise, http2OutboundRespListener.getChannelHandlerContext(),
                http2OutboundRespListener.getConnection(), http2OutboundRespListener.getEncoder(),
                http2OutboundRespListener.getInboundRequestMsg(),
                http2OutboundRespListener.getInboundRequestMsg().getHttpOutboundRespStatusFuture(),
                http2OutboundRespListener.getOriginalStreamId());
    }

    @Override
    public void handleStreamTimeout(ServerConnectorFuture serverConnectorFuture, ChannelHandlerContext ctx,
                                    Http2OutboundRespListener http2OutboundRespListener, int streamId) {
        Http2StateUtil.sendRequestTimeoutResponse(ctx, http2OutboundRespListener, streamId, REQUEST_TIMEOUT,
                                                  Unpooled.EMPTY_BUFFER, true, false);
    }

    @Override
    public void handleAbruptChannelClosure(ServerConnectorFuture serverConnectorFuture, ChannelHandlerContext ctx,
                                           Http2OutboundRespListener http2OutboundRespListener, int streamId) {
        handleIncompleteInboundMessage(http2OutboundRespListener.getInboundRequestMsg(),
                                       REMOTE_CLIENT_CLOSED_WHILE_READING_INBOUND_REQUEST_BODY);
    }
}
