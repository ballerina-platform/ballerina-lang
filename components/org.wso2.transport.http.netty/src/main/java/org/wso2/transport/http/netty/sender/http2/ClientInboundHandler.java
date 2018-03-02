/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.transport.http.netty.sender.http2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http2.Http2EventAdapter;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.codec.http2.HttpConversionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.message.EmptyLastHttpContent;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.Http2Response;
import org.wso2.transport.http.netty.message.HttpCarbonResponse;
import org.wso2.transport.http.netty.message.PooledDataStreamerFactory;

/**
 * {@code ClientInboundHandler} listen to HTTP/2 Events received from the HTTP/2 backend service
 * and construct response messages.
 * <p>
 * And also this is responsible for notifying the HTTP Response Listener as well.
 */
public class ClientInboundHandler extends Http2EventAdapter {

    private static final Logger log = LoggerFactory.getLogger(ClientInboundHandler.class);

    private TargetChannel targetChannel;

    @Override
    public int onDataRead(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding,
                          boolean endOfStream) throws Http2Exception {

        log.debug("Http2FrameListenAdapter.onDataRead()");

        OutboundMsgHolder outboundMsgHolder = targetChannel.getInFlightMessage(streamId);
        boolean isServerPush = false;
        if (outboundMsgHolder == null) {
            outboundMsgHolder = targetChannel.getPromisedMessage(streamId);
            if (outboundMsgHolder != null) {
                isServerPush = true;
            } else {
                log.warn("Data Frame received over invalid stream");
                return 0;
            }
        }
        Http2Response http2Response = outboundMsgHolder.getResponse();
        if (http2Response == null) {
            log.warn("Data Frame received before receiving headers");
            return 0;
        }
        if (isServerPush) {
            HTTPCarbonMessage responseMessage = http2Response.getPushResponse(streamId);
            if (endOfStream) {
                responseMessage.addHttpContent(new DefaultLastHttpContent(data.retain()));
                targetChannel.removePromisedMessage(streamId);
            } else {
                responseMessage.addHttpContent(new DefaultHttpContent(data.retain()));
            }
        } else {
            HTTPCarbonMessage responseMessage = http2Response.getResponse();
            if (endOfStream) {
                responseMessage.addHttpContent(new DefaultLastHttpContent(data.retain()));
                targetChannel.removeInFlightMessage(streamId);
            } else {
                responseMessage.addHttpContent(new DefaultHttpContent(data.retain()));
            }
        }
        return data.readableBytes() + padding;
    }

    @Override
    public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers,
                              int streamDependency, short weight, boolean exclusive, int padding,
                              boolean endStream) throws Http2Exception {
        log.debug("Http2FrameListenAdapter.onHeadersRead()");
        this.onHeadersRead(ctx, streamId, headers, padding, endStream);
    }

    @Override
    public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers,
                              int padding, boolean endStream) throws Http2Exception {

        log.debug("Http2FrameListenAdapter.onHeadersRead()");

        OutboundMsgHolder outboundMsgHolder = targetChannel.getInFlightMessage(streamId);
        boolean isServerPush = false;
        if (outboundMsgHolder == null) {
            outboundMsgHolder = targetChannel.getPromisedMessage(streamId);
            if (outboundMsgHolder != null) {
                isServerPush = true;
            } else {
                log.warn("Header Frame received over invalid stream");
                return;
            }
        }

        // Create response carbon message
        HttpCarbonResponse responseMessage = setupResponseCarbonMessage(ctx, streamId, headers, outboundMsgHolder);

        Http2Response http2Response = outboundMsgHolder.getResponse();
        if (isServerPush) {
            http2Response.addPushResponse(streamId, responseMessage);
            if (endStream) {
                responseMessage.addHttpContent(new EmptyLastHttpContent());
                targetChannel.removePromisedMessage(streamId);
            }
        } else {
            if (http2Response == null) {
                http2Response = new Http2Response();
                outboundMsgHolder.setHttp2Response(http2Response);
            }
            http2Response.setResponse(responseMessage);
            if (endStream) {
                responseMessage.addHttpContent(new EmptyLastHttpContent());
                targetChannel.removeInFlightMessage(streamId);
            }

            if (!outboundMsgHolder.isResponseListenerNotified()) {
                // Notify the response listener
                outboundMsgHolder.setResponseListenerNotified(true);
                outboundMsgHolder.getResponseFuture().notifyHttpListener(http2Response);
            }
        }
    }

    @Override
    public void onSettingsRead(ChannelHandlerContext ctx, Http2Settings settings)
            throws Http2Exception {
        log.debug("Http2FrameListenAdapter.onSettingRead()");
        ctx.fireChannelRead(settings);
        super.onSettingsRead(ctx, settings);
    }

    @Override
    public void onPushPromiseRead(ChannelHandlerContext ctx, int streamId, int promisedStreamId,
                                  Http2Headers headers, int padding) throws Http2Exception {
        log.debug("Http2FrameListenAdapter.onPushPromiseRead()");
        OutboundMsgHolder outboundMsgHolder = targetChannel.getInFlightMessage(streamId);
        if (outboundMsgHolder == null) {
            log.warn("Push promised received over invalid stream");
            return;
        }

        Http2Response http2Response = outboundMsgHolder.getResponse();
        if (http2Response == null) {
            http2Response = new Http2Response();
            outboundMsgHolder.setHttp2Response(http2Response);
        }
        http2Response.addPromise(new Http2PushPromise(streamId, promisedStreamId, headers));
        targetChannel.putPromisedMessage(promisedStreamId, outboundMsgHolder);
        if (!outboundMsgHolder.isResponseListenerNotified()) {
            // Notify the response listener
            outboundMsgHolder.setResponseListenerNotified(true);
            outboundMsgHolder.getResponseFuture().notifyHttpListener(http2Response);
        }
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
        HttpCarbonResponse responseCarbonMsg = new HttpCarbonResponse(httpResponse);

        // Setting properties of the HTTP Carbon Response
        responseCarbonMsg.setProperty(Constants.POOLED_BYTE_BUFFER_FACTORY, new PooledDataStreamerFactory(ctx.alloc()));
        responseCarbonMsg.setProperty(org.wso2.carbon.messaging.Constants.DIRECTION,
                                      org.wso2.carbon.messaging.Constants.DIRECTION_RESPONSE);
        responseCarbonMsg.setProperty(Constants.HTTP_STATUS_CODE, httpResponse.status().code());

        //copy required properties for service chaining from incoming carbon message to the response carbon message
        //copy shared worker pool
        responseCarbonMsg.setProperty(Constants.EXECUTOR_WORKER_POOL,
                                      outboundMsgHolder.getRequest().getProperty(Constants.EXECUTOR_WORKER_POOL));

        return responseCarbonMsg;
    }

    /**
     * Set the {@code TargetChannel} associated with the ClientInboundHandler
     *
     * @param targetChannel associated TargetChannel
     */
    public void setTargetChannel(TargetChannel targetChannel) {
        this.targetChannel = targetChannel;
    }

}
