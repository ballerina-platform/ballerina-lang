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

package org.wso2.transport.http.netty.listener;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.common.SourceInteractiveState;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.atomic.AtomicInteger;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static org.wso2.transport.http.netty.common.Constants.IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_INBOUND_REQUEST;
import static org.wso2.transport.http.netty.common.Constants.IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_OUTBOUND_RESPONSE;
import static org.wso2.transport.http.netty.common.Constants.IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_RESPONSE;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_CLIENT_CLOSED_BEFORE_INITIATING_INBOUND_REQUEST;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_CLIENT_CLOSED_BEFORE_INITIATING_OUTBOUND_RESPONSE;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_CLIENT_CLOSED_WHILE_READING_INBOUND_REQUEST;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_CLIENT_CLOSED_WHILE_WRITING_OUTBOUND_RESPONSE;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_CLIENT_TO_HOST_CONNECTION_CLOSED;
import static org.wso2.transport.http.netty.common.SourceInteractiveState.ENTITY_BODY_SENT;
import static org.wso2.transport.http.netty.common.SourceInteractiveState.SENDING_ENTITY_BODY;

/**
 * Handle all the errors related to source-handler.
 */
public class SourceErrorHandler {

    private static Logger log = LoggerFactory.getLogger(SourceErrorHandler.class);

    private HTTPCarbonMessage inboundRequestMsg;
    private final ServerConnectorFuture serverConnectorFuture;
    private SourceInteractiveState state;
    private String serverName;

    SourceErrorHandler(ServerConnectorFuture serverConnectorFuture, String serverName) {
        this.serverConnectorFuture = serverConnectorFuture;
        this.serverName = serverName;
    }

    void handleErrorCloseScenario(HTTPCarbonMessage inboundRequestMsg) {
        this.inboundRequestMsg = inboundRequestMsg;
        try {
            switch (state) {
                case CONNECTED:
                    serverConnectorFuture.notifyErrorListener(
                            new ServerConnectorException(REMOTE_CLIENT_CLOSED_BEFORE_INITIATING_INBOUND_REQUEST));
                    // Error is notified to server connector. Debug log is to make transport layer aware
                    log.debug(REMOTE_CLIENT_CLOSED_BEFORE_INITIATING_INBOUND_REQUEST);
                    break;
                case RECEIVING_ENTITY_BODY:
                    handleIncompleteInboundRequest(REMOTE_CLIENT_CLOSED_WHILE_READING_INBOUND_REQUEST);
                    break;
                case ENTITY_BODY_RECEIVED:
                    serverConnectorFuture.notifyErrorListener(
                            new ServerConnectorException(REMOTE_CLIENT_CLOSED_BEFORE_INITIATING_OUTBOUND_RESPONSE));
                    break;
                case SENDING_ENTITY_BODY:
                    // OutboundResponseStatusFuture will be notified asynchronously via OutboundResponseListener.
                    log.error(REMOTE_CLIENT_CLOSED_WHILE_WRITING_OUTBOUND_RESPONSE);
                    break;
                case ENTITY_BODY_SENT:
                    break;
                default:
                    log.error("Unexpected state detected ", state);
            }
        } catch (ServerConnectorException e) {
            log.error("Error while notifying error state to server-connector listener");
        }
    }

    ChannelFuture handleIdleErrorScenario(HTTPCarbonMessage inboundRequestMsg, ChannelHandlerContext ctx,
                                          IdleStateEvent evt) {
        this.inboundRequestMsg = inboundRequestMsg;
        try {
            switch (state) {
                case CONNECTED:
                    serverConnectorFuture.notifyErrorListener(
                            new ServerConnectorException(IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_INBOUND_REQUEST));
                    // Error is notified to server connector. Debug log is to make transport layer aware
                    log.debug(IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_INBOUND_REQUEST);
                    break;
                case RECEIVING_ENTITY_BODY:
                    //408 Request Timeout is sent if the idle timeout triggered when reading request
                    return sendRequestTimeoutResponse(ctx, HttpResponseStatus.REQUEST_TIMEOUT, Unpooled.EMPTY_BUFFER,
                                                      0);
                case ENTITY_BODY_RECEIVED:
                    // This means we have received the complete inbound request. But nothing happened
                    // after that.
                    if (evt.state() != IdleState.READER_IDLE) {
                        //500 Internal Server error is sent if the idle timeout is reached
                        String responseValue = "Server time out";
                        return sendRequestTimeoutResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                                          copiedBuffer(responseValue, CharsetUtil.UTF_8),
                                                          responseValue.length());
                    }
                    serverConnectorFuture.notifyErrorListener(
                            new ServerConnectorException(IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_OUTBOUND_RESPONSE));
                    break;
                case SENDING_ENTITY_BODY:
                    // OutboundResponseStatusFuture will be notified asynchronously via OutboundResponseListener.
                    log.error(IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_RESPONSE);
                    break;
                case ENTITY_BODY_SENT:
                    break;
                default:
                    log.error("Unexpected state detected ", state);
            }
        } catch (ServerConnectorException e) {
            log.error("Error while notifying error state to server-connector listener");
        }
        return null;
    }

    void handleIncompleteInboundRequest(String errorMessage) {
        LastHttpContent lastHttpContent = new DefaultLastHttpContent();
        lastHttpContent.setDecoderResult(DecoderResult.failure(new DecoderException(errorMessage)));
        this.inboundRequestMsg.addHttpContent(lastHttpContent);
        log.warn(errorMessage);
    }

    public void exceptionCaught(Throwable cause) {
        log.warn("Exception occurred in SourceHandler :" + cause.getMessage());
    }

    public void setState(SourceInteractiveState state) {
        this.state = state;
    }

    public void checkForResponseWriteStatus(HTTPCarbonMessage inboundRequestMsg,
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
                this.setState(ENTITY_BODY_SENT);
            }
        });
    }

    public void addResponseWriteFailureListener(HttpResponseFuture outboundRespStatusFuture,
                                                ChannelFuture channelFuture, AtomicInteger writeCounter) {
        channelFuture.addListener(writeOperationPromise -> {
            Throwable throwable = writeOperationPromise.cause();
            if (throwable != null && writeCounter.get() == 1) {
                if (throwable instanceof ClosedChannelException) {
                    throwable = new IOException(REMOTE_CLIENT_CLOSED_BEFORE_INITIATING_OUTBOUND_RESPONSE);
                }
                outboundRespStatusFuture.notifyHttpListener(throwable);
            }
            writeCounter.decrementAndGet();
        });
    }

    SourceInteractiveState getState() {
        return state;
    }

    private ChannelFuture sendRequestTimeoutResponse(ChannelHandlerContext ctx, HttpResponseStatus status,
                                                     ByteBuf content, int length) {
        state = SENDING_ENTITY_BODY;
        HttpResponse outboundResponse;
        if (inboundRequestMsg != null) {
            float httpVersion = Float.parseFloat((String) inboundRequestMsg.getProperty(Constants.HTTP_VERSION));
            if (httpVersion == Constants.HTTP_1_0) {
                outboundResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, status, content);
            } else {
                outboundResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
            }
        } else {
            outboundResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
        }
        outboundResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, length);
        outboundResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, Constants.TEXT_PLAIN);
        outboundResponse.headers().set(HttpHeaderNames.CONNECTION.toString(), Constants.CONNECTION_CLOSE);
        outboundResponse.headers().set(HttpHeaderNames.SERVER.toString(), serverName);
        return ctx.channel().writeAndFlush(outboundResponse);
    }
}
