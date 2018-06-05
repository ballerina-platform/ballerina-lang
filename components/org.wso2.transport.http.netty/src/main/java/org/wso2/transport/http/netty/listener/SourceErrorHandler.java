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

import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.SourceInteractiveState;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import static org.wso2.transport.http.netty.common.Constants.IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_OUTBOUND_RESPONSE;
import static org.wso2.transport.http.netty.common.Constants.IDLE_TIMEOUT_TRIGGERED_BEFORE_READING_INBOUND_REQUEST;
import static org.wso2.transport.http.netty.common.Constants.IDLE_TIMEOUT_TRIGGERED_WHILE_READING_INBOUND_REQUEST;
import static org.wso2.transport.http.netty.common.Constants.IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_RESPONSE;
import static org.wso2.transport.http.netty.common.Constants.IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUT_RESPONSE_HEADERS;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_CLIENT_ABRUPTLY_CLOSE_BEFORE_INITIATING_RESPONSE;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_CLIENT_ABRUPTLY_CLOSE_CONNECTION_WITHOUT_COMPLETING_REQUEST;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_CLIENT_ABRUPTLY_CLOSE_RESPONSE_CONNECTION;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_CLIENT_CLOSE_CONNECTION_BEFORE_INITIATING_REQUEST;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_CLIENT_CLOSE_WHILE_WRITING_OUTBOUND_RESPONSE_HEADERS;
import static org.wso2.transport.http.netty.common.SourceInteractiveState.CONNECTED;

/**
 * Handle all the errors related to source-handler.
 */
public class SourceErrorHandler {

    private static Logger log = LoggerFactory.getLogger(SourceErrorHandler.class);

    private HTTPCarbonMessage inboundRequestMsg;
    private final ServerConnectorFuture serverConnectorFuture;
    private SourceInteractiveState state;

    public SourceErrorHandler(ServerConnectorFuture serverConnectorFuture) {
        this.serverConnectorFuture = serverConnectorFuture;
        this.state = CONNECTED;
    }

    void handleErrorCloseScenario(HTTPCarbonMessage inboundRequestMsg, HttpResponseFuture httpOutboundRespFuture) {
        this.inboundRequestMsg = inboundRequestMsg;
        try {
            switch (state) {
                case CONNECTED:
                    serverConnectorFuture.notifyErrorListener(
                            new ServerConnectorException(REMOTE_CLIENT_CLOSE_CONNECTION_BEFORE_INITIATING_REQUEST));
                    break;
                case RECEIVING_ENTITY_BODY:
                    handleIncompleteInboundRequest(REMOTE_CLIENT_ABRUPTLY_CLOSE_CONNECTION_WITHOUT_COMPLETING_REQUEST);
                    break;
                case ENTITY_BODY_RECEIVED:
                    serverConnectorFuture.notifyErrorListener(
                            new ServerConnectorException(REMOTE_CLIENT_ABRUPTLY_CLOSE_BEFORE_INITIATING_RESPONSE));
                    httpOutboundRespFuture.notifyHttpListener(
                            new ServerConnectorException(REMOTE_CLIENT_ABRUPTLY_CLOSE_BEFORE_INITIATING_RESPONSE));
                    break;
                case SENDING_HEADERS:
                    log.error(REMOTE_CLIENT_CLOSE_WHILE_WRITING_OUTBOUND_RESPONSE_HEADERS);
                    httpOutboundRespFuture.notifyHttpListener(
                            new ServerConnectorException(REMOTE_CLIENT_CLOSE_WHILE_WRITING_OUTBOUND_RESPONSE_HEADERS));
                    break;
                case SENDING_ENTITY_BODY:
                    log.error(REMOTE_CLIENT_ABRUPTLY_CLOSE_RESPONSE_CONNECTION);
                    httpOutboundRespFuture.notifyHttpListener(
                            new ServerConnectorException(REMOTE_CLIENT_ABRUPTLY_CLOSE_RESPONSE_CONNECTION));
                    break;
                default:
                    log.error("Unexpected state detected ", state);
            }
        } catch (ServerConnectorException e) {
            log.error("Error while notifying error state to server-connector listener");
        }
    }

    void handleIdleErrorScenario(HTTPCarbonMessage inboundRequestMsg, HttpResponseFuture httpOutboundRespFuture) {
        this.inboundRequestMsg = inboundRequestMsg;
        try {
            switch (state) {
                case CONNECTED:
                    serverConnectorFuture.notifyErrorListener(
                            new ServerConnectorException(IDLE_TIMEOUT_TRIGGERED_BEFORE_READING_INBOUND_REQUEST));
                    break;
                case RECEIVING_ENTITY_BODY:
                    handleIncompleteInboundRequest(IDLE_TIMEOUT_TRIGGERED_WHILE_READING_INBOUND_REQUEST);
                    break;
                case ENTITY_BODY_RECEIVED:
                    // This means we have received the complete inbound request. But nothing happened
                    // after that.
                    serverConnectorFuture.notifyErrorListener(
                            new ServerConnectorException(IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_OUTBOUND_RESPONSE));

                    // This is to stop application layer writing response after
                    // idle timeout is triggered.
                    httpOutboundRespFuture.notifyHttpListener(
                            new ServerConnectorException(IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_OUTBOUND_RESPONSE));
                    break;
                case SENDING_HEADERS:
                    log.error(IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUT_RESPONSE_HEADERS);
                    httpOutboundRespFuture.notifyHttpListener(
                            new ServerConnectorException(IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUT_RESPONSE_HEADERS));
                    break;
                case SENDING_ENTITY_BODY:
                    log.error(IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_RESPONSE);
                    httpOutboundRespFuture.notifyHttpListener(
                            new ServerConnectorException(IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_RESPONSE));
                    break;
                default:
                    log.error("Unexpected state detected ", state);
            }
        } catch (ServerConnectorException e) {
            log.error("Error while notifying error state to server-connector listener");
        }
    }

    private void handleIncompleteInboundRequest(String errorMessage) {
        LastHttpContent lastHttpContent = new DefaultLastHttpContent();
        lastHttpContent.setDecoderResult(DecoderResult.failure(new DecoderException(errorMessage)));
        this.inboundRequestMsg.addHttpContent(lastHttpContent);
        log.warn(errorMessage);
    }

    public void exceptionCaught(Throwable cause) {
        log.warn("Exception occurred :" + cause);
    }

    public void setState(SourceInteractiveState state) {
        this.state = state;
    }
}
