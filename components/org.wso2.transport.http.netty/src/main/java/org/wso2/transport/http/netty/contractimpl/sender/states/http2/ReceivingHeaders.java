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
package org.wso2.transport.http.netty.contractimpl.sender.states.http2;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.HttpConversionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contractimpl.common.states.Http2MessageStateContext;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2ClientChannel;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2TargetHandler;
import org.wso2.transport.http.netty.contractimpl.sender.http2.OutboundMsgHolder;
import org.wso2.transport.http.netty.message.DefaultListener;
import org.wso2.transport.http.netty.message.Http2DataFrame;
import org.wso2.transport.http.netty.message.Http2HeadersFrame;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;
import org.wso2.transport.http.netty.message.PooledDataStreamerFactory;

/**
 * State between start and end of inbound response headers read.
 */
public class ReceivingHeaders implements SenderState {

    private static final Logger LOG = LoggerFactory.getLogger(ReceivingHeaders.class);

    private final Http2TargetHandler http2TargetHandler;
    private final Http2ClientChannel http2ClientChannel;

    public ReceivingHeaders(Http2TargetHandler http2TargetHandler) {
        this.http2TargetHandler = http2TargetHandler;
        this.http2ClientChannel = http2TargetHandler.getHttp2ClientChannel();
    }

    @Override
    public void writeOutboundRequestHeaders(ChannelHandlerContext ctx, HttpContent httpContent) {
        LOG.warn("writeOutboundRequestHeaders is not a dependant action of this state");
    }

    @Override
    public void writeOutboundRequestBody(ChannelHandlerContext ctx, HttpContent httpContent) {
        LOG.warn("writeOutboundRequestBody is not a dependant action of this state");
    }

    @Override
    public void readInboundResponseHeaders(ChannelHandlerContext ctx, Http2HeadersFrame http2HeadersFrame,
                                           OutboundMsgHolder outboundMsgHolder, boolean isServerPush,
                                           Http2MessageStateContext http2MessageStateContext) {
        onHeadersRead(ctx, http2HeadersFrame, outboundMsgHolder, isServerPush, http2MessageStateContext);
    }

    @Override
    public void readInboundResponseBody(ChannelHandlerContext ctx, Http2DataFrame http2DataFrame,
                                        OutboundMsgHolder outboundMsgHolder, boolean isServerPush,
                                        Http2MessageStateContext http2MessageStateContext) {
        LOG.warn("readInboundResponseBody is not a dependant action of this state");
    }

    @Override
    public void readInboundPromise(Http2PushPromise http2PushPromise, OutboundMsgHolder outboundMsgHolder) {
        LOG.warn("readInboundPromise is not a dependant action of this state");
    }

    private void onHeadersRead(ChannelHandlerContext ctx, Http2HeadersFrame http2HeadersFrame,
                               OutboundMsgHolder outboundMsgHolder, boolean isServerPush,
                               Http2MessageStateContext http2MessageStateContext) {
        int streamId = http2HeadersFrame.getStreamId();
        Http2Headers http2Headers = http2HeadersFrame.getHeaders();
        boolean endOfStream = http2HeadersFrame.isEndOfStream();

        if (isServerPush) {
            if (endOfStream) {
                // Retrieve response message.
                HttpCarbonResponse responseMessage = outboundMsgHolder.getPushResponse(streamId);
                if (responseMessage != null) {
                    onTrailersRead(streamId, http2Headers, outboundMsgHolder, responseMessage);
                } else if (http2Headers.contains(Constants.HTTP2_METHOD)) {
                    // if the header frame is an initial header frame and also it has endOfStream
                    responseMessage = setupResponseCarbonMessage(ctx, streamId, http2Headers, outboundMsgHolder);
                    responseMessage.addHttpContent(new DefaultLastHttpContent());
                    outboundMsgHolder.addPushResponse(streamId, responseMessage);
                }
                http2ClientChannel.removePromisedMessage(streamId);
                http2MessageStateContext.setSenderState(new EntityBodyReceived(http2TargetHandler));
            } else {
                // Create response carbon message.
                HttpCarbonResponse responseMessage = setupResponseCarbonMessage(ctx, streamId,
                        http2Headers, outboundMsgHolder);
                outboundMsgHolder.addPushResponse(streamId, responseMessage);
                http2MessageStateContext.setSenderState(new ReceivingEntityBody(http2TargetHandler));
            }
        } else {
            if (endOfStream) {
                // Retrieve response message.
                HttpCarbonResponse responseMessage = outboundMsgHolder.getResponse();
                if (responseMessage != null) {
                    onTrailersRead(streamId, http2Headers, outboundMsgHolder, responseMessage);
                } else if (http2Headers.contains(Constants.HTTP2_METHOD)) {
                    // if the header frame is an initial header frame and also it has endOfStream
                    responseMessage = setupResponseCarbonMessage(ctx, streamId, http2Headers, outboundMsgHolder);
                    responseMessage.addHttpContent(new DefaultLastHttpContent());
                    outboundMsgHolder.setResponse(responseMessage);
                }
                http2ClientChannel.removeInFlightMessage(streamId);
                http2MessageStateContext.setSenderState(new EntityBodyReceived(http2TargetHandler));
            } else {
                // Create response carbon message.
                HttpCarbonResponse responseMessage = setupResponseCarbonMessage(ctx, streamId,
                        http2Headers, outboundMsgHolder);
                outboundMsgHolder.setResponse(responseMessage);
                http2MessageStateContext.setSenderState(new ReceivingEntityBody(http2TargetHandler));
            }
        }
    }

    private void onTrailersRead(int streamId, Http2Headers headers, OutboundMsgHolder outboundMsgHolder,
                                HttpCarbonMessage responseMessage) {
        HttpVersion version = new HttpVersion(Constants.HTTP_VERSION_2_0, true);
        LastHttpContent lastHttpContent = new DefaultLastHttpContent();
        HttpHeaders trailers = lastHttpContent.trailingHeaders();

        try {
            HttpConversionUtil.addHttp2ToHttpHeaders(streamId, headers, trailers, version, true, false);
        } catch (Http2Exception e) {
            outboundMsgHolder.getResponseFuture().
                    notifyHttpListener(new Exception("Error while setting http headers", e));
        }
        responseMessage.addHttpContent(lastHttpContent);
    }

    private HttpCarbonResponse setupResponseCarbonMessage(ChannelHandlerContext ctx, int streamId,
                                                          Http2Headers http2Headers,
                                                          OutboundMsgHolder outboundMsgHolder) {
        // Create HTTP Response
        CharSequence status = http2Headers.status();
        HttpResponseStatus responseStatus;
        try {
            responseStatus = HttpConversionUtil.parseStatus(status);
        } catch (Http2Exception e) {
            responseStatus = HttpResponseStatus.BAD_GATEWAY;
        }
        HttpVersion version = new HttpVersion(Constants.HTTP_VERSION_2_0, true);
        HttpResponse httpResponse = new DefaultHttpResponse(version, responseStatus);

        // Set headers
        try {
            HttpConversionUtil.addHttp2ToHttpHeaders(
                    streamId, http2Headers, httpResponse.headers(), version, false, false);
        } catch (Http2Exception e) {
            outboundMsgHolder.getResponseFuture().
                    notifyHttpListener(new Exception("Error while setting http headers", e));
        }
        // Create HTTP Carbon Response
        HttpCarbonResponse responseCarbonMsg = new HttpCarbonResponse(httpResponse, new DefaultListener(ctx));

        // Setting properties of the HTTP Carbon Response
        responseCarbonMsg.setProperty(Constants.POOLED_BYTE_BUFFER_FACTORY, new PooledDataStreamerFactory(ctx.alloc()));
        responseCarbonMsg.setProperty(Constants.DIRECTION, Constants.DIRECTION_RESPONSE);
        responseCarbonMsg.setProperty(Constants.HTTP_STATUS_CODE, httpResponse.status().code());

        /* copy required properties for service chaining from incoming carbon message to the response carbon message
        copy shared worker pool */
        responseCarbonMsg.setProperty(Constants.EXECUTOR_WORKER_POOL,
                outboundMsgHolder.getRequest().getProperty(Constants.EXECUTOR_WORKER_POOL));
        return responseCarbonMsg;
    }
}
