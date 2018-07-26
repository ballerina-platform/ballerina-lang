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

package org.wso2.transport.http.netty.sender;

import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.SourceInteractiveState;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.exception.EndpointTimeOutException;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;

import static org.wso2.transport.http.netty.common.Constants.CLIENT_TO_REMOTE_HOST_CONNECTION_CLOSED;
import static org.wso2.transport.http.netty.common.Constants.IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_INBOUND_RESPONSE;
import static org.wso2.transport.http.netty.common.Constants.IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_OUTBOUND_REQUEST;
import static org.wso2.transport.http.netty.common.Constants.IDLE_TIMEOUT_TRIGGERED_WHILE_READING_INBOUND_RESPONSE;
import static org.wso2.transport.http.netty.common.Constants.IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_REQUEST;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_SERVER_CLOSED_BEFORE_INITIATING_INBOUND_RESPONSE;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_SERVER_CLOSED_BEFORE_INITIATING_OUTBOUND_REQUEST;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_SERVER_CLOSED_WHILE_READING_INBOUND_RESPONSE;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_SERVER_CLOSED_WHILE_WRITING_OUTBOUND_REQUEST;
import static org.wso2.transport.http.netty.common.SourceInteractiveState.CONNECTED;
import static org.wso2.transport.http.netty.common.SourceInteractiveState.ENTITY_BODY_SENT;

/**
 * Handle all the errors related to target-handler.
 */
public class TargetErrorHandler {

    private static Logger log = LoggerFactory.getLogger(TargetErrorHandler.class);
    private HttpResponseFuture httpResponseFuture;
    private SourceInteractiveState state;
    private static final String CLIENT_ERROR = "Error in HTTP client: ";

    public TargetErrorHandler() {
        this.state = CONNECTED;
    }

    protected void handleErrorCloseScenario(HttpCarbonMessage inboundResponseMsg) {
        switch (state) {
            case CONNECTED:
                httpResponseFuture.notifyHttpListener(
                        new ServerConnectorException(REMOTE_SERVER_CLOSED_BEFORE_INITIATING_OUTBOUND_REQUEST));
                log.debug(REMOTE_SERVER_CLOSED_BEFORE_INITIATING_OUTBOUND_REQUEST);
                break;
            case SENDING_ENTITY_BODY:
                // HttpResponseFuture will be notified asynchronously via Target channel.
                log.error(REMOTE_SERVER_CLOSED_WHILE_WRITING_OUTBOUND_REQUEST);
                break;
            case ENTITY_BODY_SENT:
                httpResponseFuture.notifyHttpListener(
                        new ServerConnectorException(REMOTE_SERVER_CLOSED_BEFORE_INITIATING_INBOUND_RESPONSE));
                log.error(REMOTE_SERVER_CLOSED_BEFORE_INITIATING_INBOUND_RESPONSE);
                break;
            case RECEIVING_ENTITY_BODY:
                handleIncompleteInboundResponse(inboundResponseMsg,
                        REMOTE_SERVER_CLOSED_WHILE_READING_INBOUND_RESPONSE);
                break;
            case ENTITY_BODY_RECEIVED:
                break;
            default:
                log.error("{} Unexpected state detected {}", CLIENT_ERROR, state);
        }
    }

    protected void handleErrorIdleScenarios(HttpCarbonMessage inboundResponseMsg, String channelID) {
        String logStr = "{} {}";
        switch (state) {
            case CONNECTED:
                httpResponseFuture.notifyHttpListener(new EndpointTimeOutException(channelID,
                        IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_OUTBOUND_REQUEST,
                        HttpResponseStatus.GATEWAY_TIMEOUT.code()));
                // Error is notified to server connector. Debug log is to make transport layer aware
                log.debug(logStr, CLIENT_ERROR, IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_OUTBOUND_REQUEST);
                break;
            case SENDING_ENTITY_BODY:
                // HttpResponseFuture will be notified asynchronously via Target channel.
                log.error(logStr, CLIENT_ERROR, IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_REQUEST);
                break;
            case ENTITY_BODY_SENT:
                httpResponseFuture.notifyHttpListener(new EndpointTimeOutException(channelID,
                        IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_INBOUND_RESPONSE,
                        HttpResponseStatus.GATEWAY_TIMEOUT.code()));
                log.error(logStr, CLIENT_ERROR, IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_INBOUND_RESPONSE);
                break;
            case RECEIVING_ENTITY_BODY:
                handleIncompleteInboundResponse(inboundResponseMsg,
                        IDLE_TIMEOUT_TRIGGERED_WHILE_READING_INBOUND_RESPONSE);
                break;
            case ENTITY_BODY_RECEIVED:
                break;
            default:
                log.error("{} Unexpected state detected {}", CLIENT_ERROR, state);
        }
    }

    private void handleIncompleteInboundResponse(HttpCarbonMessage inboundResponseMsg, String errorMessage) {
        LastHttpContent lastHttpContent = new DefaultLastHttpContent();
        lastHttpContent.setDecoderResult(DecoderResult.failure(new DecoderException(errorMessage)));
        inboundResponseMsg.addHttpContent(lastHttpContent);
        log.warn(errorMessage);
    }

    public void exceptionCaught(Throwable cause) {
        log.warn("Exception occurred in TargetHandler : {}", cause.getMessage());
    }

    public void setState(SourceInteractiveState state) {
        this.state = state;
    }

    public void setResponseFuture(HttpResponseFuture httpResponseFuture) {
        this.httpResponseFuture = httpResponseFuture;
    }

    public void checkForRequestWriteStatus(ChannelFuture outboundRequestChannelFuture) {
        outboundRequestChannelFuture.addListener(writeOperationPromise -> {
            if (writeOperationPromise.cause() != null) {
                notifyResponseFutureListener(writeOperationPromise);
            } else {
                this.setState(ENTITY_BODY_SENT);
            }
        });
    }

    public void notifyIfHeaderFailure(ChannelFuture outboundRequestChannelFuture) {
        outboundRequestChannelFuture.addListener(writeOperationPromise -> {
            if (writeOperationPromise.cause() != null) {
                notifyResponseFutureListener(writeOperationPromise);
            }
        });
    }

    private void notifyResponseFutureListener(Future<? super Void> writeOperationPromise) {
        Throwable throwable = writeOperationPromise.cause();
        if (throwable instanceof ClosedChannelException) {
            throwable = new IOException(CLIENT_TO_REMOTE_HOST_CONNECTION_CLOSED);
        }
        httpResponseFuture.notifyHttpListener(throwable);
    }
}
