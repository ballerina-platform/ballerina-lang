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

package org.wso2.transport.http.netty.listener.states.listener;

import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contractimpl.HttpOutboundRespListener;
import org.wso2.transport.http.netty.internal.HandlerExecutor;
import org.wso2.transport.http.netty.internal.HttpTransportContextHolder;
import org.wso2.transport.http.netty.listener.SourceHandler;
import org.wso2.transport.http.netty.listener.states.StateContext;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static org.wso2.transport.http.netty.common.Constants.HTTP_HEAD_METHOD;
import static org.wso2.transport.http.netty.common.Constants.IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_RESPONSE_BODY;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_CLIENT_CLOSED_WHILE_WRITING_OUTBOUND_RESPONSE_BODY;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_CLIENT_TO_HOST_CONNECTION_CLOSED;
import static org.wso2.transport.http.netty.common.Util.createFullHttpResponse;
import static org.wso2.transport.http.netty.common.Util.setupContentLengthRequest;

/**
 * State between start and end of outbound response entity body write
 */
public class SendingEntityBody implements ListenerState {

    private static Logger log = LoggerFactory.getLogger(SendingEntityBody.class);
    private final HandlerExecutor handlerExecutor;
    private final HttpResponseFuture outboundRespStatusFuture;
    private final StateContext stateContext;
    private boolean headersWritten;
    private long contentLength = 0;
    private boolean headRequest;
    private List<HttpContent> contentList = new ArrayList<>();
    private HttpCarbonMessage inboundRequestMsg;
    private ChannelHandlerContext sourceContext;
    private SourceHandler sourceHandler;

    SendingEntityBody(StateContext stateContext, HttpResponseFuture outboundRespStatusFuture,
                      boolean headersWritten) {
        this.stateContext = stateContext;
        this.outboundRespStatusFuture = outboundRespStatusFuture;
        this.headersWritten = headersWritten;
        this.handlerExecutor = HttpTransportContextHolder.getInstance().getHandlerExecutor();
    }

    @Override
    public void readInboundRequestHeaders(HttpCarbonMessage inboundRequestMsg, HttpRequest inboundRequestHeaders) {
        // Not a dependant action of this state.
    }

    @Override
    public void readInboundRequestEntityBody(Object inboundRequestEntityBody) throws ServerConnectorException {
        // Not a dependant action of this state.
    }

    @Override
    public void writeOutboundResponseHeaders(HttpCarbonMessage outboundResponseMsg, HttpContent httpContent) {
        // Not a dependant action of this state.
    }

    @Override
    public void writeOutboundResponseEntityBody(HttpOutboundRespListener outboundRespListener,
                                                HttpCarbonMessage outboundResponseMsg, HttpContent httpContent) {
        headRequest = outboundRespListener.getRequestDataHolder().getHttpMethod().equalsIgnoreCase(HTTP_HEAD_METHOD);
        inboundRequestMsg = outboundRespListener.getInboundRequestMsg();
        sourceContext = outboundRespListener.getSourceContext();
        sourceHandler = outboundRespListener.getSourceHandler();

        ChannelFuture outboundChannelFuture;
        if (httpContent instanceof LastHttpContent) {
            if (headersWritten) {
                outboundChannelFuture = checkHeadRequestAndWriteOutboundResponseBody(httpContent);
            } else {
                contentLength += httpContent.content().readableBytes();
                setupContentLengthRequest(outboundResponseMsg, contentLength);
                outboundChannelFuture = writeOutboundResponseHeaderAndBody(outboundRespListener, outboundResponseMsg,
                                                                           (LastHttpContent) httpContent);
            }

            if (!outboundRespListener.isKeepAlive()) {
                outboundChannelFuture.addListener(ChannelFutureListener.CLOSE);
            }  else {
                triggerPipeliningLogic(outboundResponseMsg);
            }

            if (handlerExecutor != null) {
                handlerExecutor.executeAtSourceResponseSending(outboundResponseMsg);
            }
        } else {
            if (headersWritten) {
                if (headRequest) {
                    httpContent.release();
                    return;
                }
                outboundRespListener.getSourceContext().writeAndFlush(httpContent);
            } else {
                this.contentList.add(httpContent);
                contentLength += httpContent.content().readableBytes();
            }
        }
    }

    @Override
    public void handleAbruptChannelClosure(ServerConnectorFuture serverConnectorFuture) {
        // OutboundResponseStatusFuture will be notified asynchronously via OutboundResponseListener.
        log.error(REMOTE_CLIENT_CLOSED_WHILE_WRITING_OUTBOUND_RESPONSE_BODY);
    }

    @Override
    public ChannelFuture handleIdleTimeoutConnectionClosure(ServerConnectorFuture serverConnectorFuture,
                                                            ChannelHandlerContext ctx) {
        // OutboundResponseStatusFuture will be notified asynchronously via OutboundResponseListener.
        log.error(IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_RESPONSE_BODY);
        return null;
    }

    private ChannelFuture checkHeadRequestAndWriteOutboundResponseBody(HttpContent httpContent) {
        ChannelFuture outboundChannelFuture;
        if (headRequest) {
            httpContent.release();
            outboundChannelFuture = writeOutboundResponseBody(new DefaultLastHttpContent());
        } else {
            outboundChannelFuture = writeOutboundResponseBody(httpContent);
        }
        return outboundChannelFuture;
    }

    private ChannelFuture writeOutboundResponseHeaderAndBody(HttpOutboundRespListener outboundRespListener,
                                                             HttpCarbonMessage outboundResponseMsg,
                                                             LastHttpContent lastHttpContent) {
        CompositeByteBuf allContent = Unpooled.compositeBuffer();
        for (HttpContent cachedHttpContent : contentList) {
            allContent.addComponent(true, cachedHttpContent.content());
        }
        allContent.addComponent(true, lastHttpContent.content());

        if (headRequest) {
            allContent.release();
            allContent = Unpooled.compositeBuffer();
            allContent.addComponent(true, new DefaultLastHttpContent().content());
        }

        HttpResponse fullOutboundResponse = createFullHttpResponse(outboundResponseMsg,
                                                                   outboundRespListener.getRequestDataHolder()
                                                                           .getHttpVersion(),
                                                                   outboundRespListener.getServerName(),
                                                                   outboundRespListener.isKeepAlive(), allContent);

        ChannelFuture outboundChannelFuture = sourceContext.writeAndFlush(fullOutboundResponse);
        checkForResponseWriteStatus(inboundRequestMsg, outboundRespStatusFuture, outboundChannelFuture);
        return outboundChannelFuture;
    }

    private ChannelFuture writeOutboundResponseBody(HttpContent lastHttpContent) {
        ChannelFuture outboundChannelFuture = sourceContext.writeAndFlush(lastHttpContent);
        checkForResponseWriteStatus(inboundRequestMsg, outboundRespStatusFuture, outboundChannelFuture);
        return outboundChannelFuture;
    }

    private void checkForResponseWriteStatus(HttpCarbonMessage inboundRequestMsg,
                                             HttpResponseFuture outboundRespStatusFuture, ChannelFuture channelFuture) {
        channelFuture.addListener(writeOperationPromise -> {
            Throwable throwable = writeOperationPromise.cause();
            if (throwable != null) {
                if (throwable instanceof ClosedChannelException) {
                    throwable = new IOException(REMOTE_CLIENT_TO_HOST_CONNECTION_CLOSED);
                }
                outboundRespStatusFuture.notifyHttpListener(throwable);
            } else {
                outboundRespStatusFuture.notifyHttpListener(inboundRequestMsg);
            }
            stateContext.setListenerState(new ResponseCompleted(sourceHandler, stateContext, inboundRequestMsg));
            resetOutboundListenerState();
        });
    }

    /**
     * Increment the next expected sequence number and trigger the pipelining logic.
     *
     * @param outboundResponseMsg Represent the outbound response
     */
    private void triggerPipeliningLogic(HttpCarbonMessage outboundResponseMsg) {
        String httpVersion = (String) inboundRequestMsg.getProperty(Constants.HTTP_VERSION);
        if (outboundResponseMsg.isPipeliningNeeded() && Constants.HTTP_1_1_VERSION.equalsIgnoreCase
                (httpVersion)) {
            Queue responseQueue;
            synchronized (sourceContext.channel().attr(Constants.RESPONSE_QUEUE).get()) {
                responseQueue = sourceContext.channel().attr(Constants.RESPONSE_QUEUE).get();
                Integer nextSequenceNumber = sourceContext.channel().attr(Constants.NEXT_SEQUENCE_NUMBER).get();
                //IMPORTANT:Next sequence number should never be incremented for interim 100 continue response
                //because the body of the request is yet to come. Only when the actual response is sent out, this
                //next sequence number should be updated.
                nextSequenceNumber++;
                sourceContext.channel().attr(Constants.NEXT_SEQUENCE_NUMBER).set(nextSequenceNumber);
            }
            if (!responseQueue.isEmpty()) {
                //Notify ballerina to send the response which is next in queue. This is needed because,
                // if the other responses got ready before the nextSequenceNumber gets updated then the
                // ballerina respond() won't start serializing the responses in queue. This is to trigger
                // that process again.
                outboundResponseMsg.getPipelineListener().onLastHttpContentSent(sourceContext);
            }
        }
    }

    private void resetOutboundListenerState() {
        contentList.clear();
        contentLength = 0;
        headersWritten = false;
    }
}
