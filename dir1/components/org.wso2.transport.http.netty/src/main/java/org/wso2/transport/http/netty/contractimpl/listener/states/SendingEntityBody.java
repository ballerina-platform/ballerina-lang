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

package org.wso2.transport.http.netty.contractimpl.listener.states;

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
import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.contract.exceptions.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.HttpOutboundRespListener;
import org.wso2.transport.http.netty.contractimpl.listener.SourceHandler;
import org.wso2.transport.http.netty.internal.HandlerExecutor;
import org.wso2.transport.http.netty.internal.HttpTransportContextHolder;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static org.wso2.transport.http.netty.contract.Constants.HTTP_HEAD_METHOD;
import static org.wso2.transport.http.netty.contract.Constants.IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_RESPONSE_BODY;
import static org.wso2.transport.http.netty.contract.Constants.REMOTE_CLIENT_CLOSED_WHILE_WRITING_OUTBOUND_RESPONSE_BODY;
import static org.wso2.transport.http.netty.contract.Constants.REMOTE_CLIENT_TO_HOST_CONNECTION_CLOSED;
import static org.wso2.transport.http.netty.contractimpl.common.Util.createFullHttpResponse;
import static org.wso2.transport.http.netty.contractimpl.common.Util.setupContentLengthRequest;
import static org.wso2.transport.http.netty.contractimpl.common.states.StateUtil.ILLEGAL_STATE_ERROR;

/**
 * State between start and end of outbound response entity body write.
 */
public class SendingEntityBody implements ListenerState {

    private static final Logger LOG = LoggerFactory.getLogger(SendingEntityBody.class);

    private final HandlerExecutor handlerExecutor;
    private final HttpResponseFuture outboundRespStatusFuture;
    private final ListenerReqRespStateManager listenerReqRespStateManager;
    private boolean headersWritten;
    private long contentLength = 0;
    private boolean headRequest;
    private List<HttpContent> contentList = new ArrayList<>();
    private HttpCarbonMessage inboundRequestMsg;
    private HttpCarbonMessage outboundResponseMsg;
    private ChannelHandlerContext sourceContext;
    private SourceHandler sourceHandler;

    SendingEntityBody(ListenerReqRespStateManager listenerReqRespStateManager,
                      HttpResponseFuture outboundRespStatusFuture, boolean headersWritten) {
        this.listenerReqRespStateManager = listenerReqRespStateManager;
        this.outboundRespStatusFuture = outboundRespStatusFuture;
        this.headersWritten = headersWritten;
        this.handlerExecutor = HttpTransportContextHolder.getInstance().getHandlerExecutor();
    }

    @Override
    public void readInboundRequestHeaders(HttpCarbonMessage inboundRequestMsg, HttpRequest inboundRequestHeaders) {
        LOG.warn("readInboundRequestHeaders {}", ILLEGAL_STATE_ERROR);
    }

    @Override
    public void readInboundRequestBody(Object inboundRequestEntityBody) throws ServerConnectorException {
        LOG.warn("readInboundRequestBody {}", ILLEGAL_STATE_ERROR);
    }

    @Override
    public void writeOutboundResponseHeaders(HttpCarbonMessage outboundResponseMsg, HttpContent httpContent) {
        LOG.warn("writeOutboundResponseHeaders {}", ILLEGAL_STATE_ERROR);
    }

    @Override
    public void writeOutboundResponseBody(HttpOutboundRespListener outboundRespListener,
                                          HttpCarbonMessage outboundResponseMsg, HttpContent httpContent) {

        headRequest = outboundRespListener.getRequestDataHolder().getHttpMethod().equalsIgnoreCase(HTTP_HEAD_METHOD);
        inboundRequestMsg = outboundRespListener.getInboundRequestMsg();
        sourceContext = outboundRespListener.getSourceContext();
        sourceHandler = outboundRespListener.getSourceHandler();
        this.outboundResponseMsg = outboundResponseMsg;

        ChannelFuture outboundChannelFuture;
        if (httpContent instanceof LastHttpContent) {
            if (headersWritten) {
                final LastHttpContent lastContent = (httpContent == LastHttpContent.EMPTY_LAST_CONTENT) ?
                        new DefaultLastHttpContent() : (LastHttpContent) httpContent;
                lastContent.trailingHeaders().add(outboundResponseMsg.getTrailerHeaders());
                outboundChannelFuture = checkHeadRequestAndWriteOutboundResponseBody(lastContent);
            } else {
                contentLength += httpContent.content().readableBytes();
                setupContentLengthRequest(outboundResponseMsg, contentLength);
                outboundChannelFuture = writeOutboundResponseHeaderAndBody(outboundRespListener, outboundResponseMsg,
                                                                           (LastHttpContent) httpContent);
            }

            if (!outboundRespListener.isKeepAlive()) {
                outboundChannelFuture.addListener(ChannelFutureListener.CLOSE);
            } else {
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
        IOException connectionClose = new IOException(REMOTE_CLIENT_CLOSED_WHILE_WRITING_OUTBOUND_RESPONSE_BODY);
        outboundResponseMsg.setIoException(connectionClose);
        outboundRespStatusFuture.notifyHttpListener(connectionClose);

        LOG.error(REMOTE_CLIENT_CLOSED_WHILE_WRITING_OUTBOUND_RESPONSE_BODY);
    }

    @Override
    public ChannelFuture handleIdleTimeoutConnectionClosure(ServerConnectorFuture serverConnectorFuture,
                                                            ChannelHandlerContext ctx) {
        IOException connectionClose = new IOException(IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_RESPONSE_BODY);
        outboundResponseMsg.setIoException(connectionClose);
        outboundRespStatusFuture.notifyHttpListener(connectionClose);

        LOG.error(IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_RESPONSE_BODY);
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
            listenerReqRespStateManager.state
                    = new ResponseCompleted(listenerReqRespStateManager, sourceHandler, inboundRequestMsg);
            resetOutboundListenerState();
        });
    }

    /**
     * Increment the next expected sequence number and trigger the pipelining logic.
     *
     * @param outboundResponseMsg Represent the outbound response
     */
    private void triggerPipeliningLogic(HttpCarbonMessage outboundResponseMsg) {
        String httpVersion = inboundRequestMsg.getHttpVersion();
        if (outboundResponseMsg.isPipeliningEnabled() && Constants.HTTP_1_1_VERSION.equalsIgnoreCase
                (httpVersion)) {
            Queue responseQueue;
            synchronized (sourceContext.channel().attr(Constants.RESPONSE_QUEUE).get()) {
                responseQueue = sourceContext.channel().attr(Constants.RESPONSE_QUEUE).get();
                Long nextSequenceNumber = sourceContext.channel().attr(Constants.NEXT_SEQUENCE_NUMBER).get();
                //IMPORTANT:Next sequence number should never be incremented for interim 100 continue response
                //because the body of the request is yet to come. Only when the actual response is sent out, this
                //next sequence number should be updated.
                nextSequenceNumber++;
                sourceContext.channel().attr(Constants.NEXT_SEQUENCE_NUMBER).set(nextSequenceNumber);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Current sequence id of the response : {}", outboundResponseMsg.getSequenceId());
                    LOG.debug("Updated next sequence id to : {}", nextSequenceNumber);
                }
            }
            if (!responseQueue.isEmpty()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Pipelining logic is triggered from transport");
                }
                //Notify ballerina to send the response which is next in queue. This is needed because,
                //if the other responses got ready before the nextSequenceNumber gets updated then the
                //ballerina respond() won't start serializing the responses in queue. This is to trigger
                //that process again.
                if (outboundResponseMsg.getPipeliningFuture() != null) {
                    EventExecutorGroup pipeliningExecutor = sourceContext.channel().attr(Constants.PIPELINING_EXECUTOR)
                            .get();
                    //IMPORTANT:Pipelining logic should never be executed in an I/O thread as it might lead to I/O
                    //thread blocking scenarios in outbound trottling. Here, the pipelining logic runs in a thread that
                    //belongs to the pipelining thread pool.
                    pipeliningExecutor.execute(() -> outboundResponseMsg.getPipeliningFuture().
                            notifyPipeliningListener(sourceContext));

                }
            }
        }
    }

    private void resetOutboundListenerState() {
        contentList.clear();
        contentLength = 0;
        headersWritten = false;
    }
}
