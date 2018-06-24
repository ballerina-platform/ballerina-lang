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

import static org.wso2.transport.http.netty.common.Constants.IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_INBOUND_REQUEST;
import static org.wso2.transport.http.netty.common.Constants.IDLE_TIMEOUT_TRIGGERED_BEFORE_INITIATING_OUTBOUND_RESPONSE;
import static org.wso2.transport.http.netty.common.Constants.IDLE_TIMEOUT_TRIGGERED_WHILE_READING_INBOUND_REQUEST;
import static org.wso2.transport.http.netty.common.Constants.IDLE_TIMEOUT_TRIGGERED_WHILE_WRITING_OUTBOUND_RESPONSE;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_CLIENT_CLOSED_BEFORE_INITIATING_INBOUND_REQUEST;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_CLIENT_CLOSED_BEFORE_INITIATING_OUTBOUND_RESPONSE;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_CLIENT_CLOSED_WHILE_READING_INBOUND_REQUEST;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_CLIENT_CLOSED_WHILE_WRITING_OUTBOUND_RESPONSE;

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
    }

    void handleErrorCloseScenario(HTTPCarbonMessage inboundRequestMsg, HttpResponseFuture httpOutRespStatusFuture) {
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

    void handleIdleErrorScenario(HTTPCarbonMessage inboundRequestMsg, HttpResponseFuture httpOutRespStatusFuture) {
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
                    handleIncompleteInboundRequest(IDLE_TIMEOUT_TRIGGERED_WHILE_READING_INBOUND_REQUEST);
                    break;
                case ENTITY_BODY_RECEIVED:
                    // This means we have received the complete inbound request. But nothing happened
                    // after that.
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
    }

    private void handleIncompleteInboundRequest(String errorMessage) {
        LastHttpContent lastHttpContent = new DefaultLastHttpContent();
        lastHttpContent.setDecoderResult(DecoderResult.failure(new DecoderException(errorMessage)));
        this.inboundRequestMsg.addHttpContent(lastHttpContent);
        log.warn(errorMessage);
    }

    public void exceptionCaught(Throwable cause) {
        log.warn("Exception occurred :" + cause.getMessage());
    }

    public void setState(SourceInteractiveState state) {
        this.state = state;
    }
}
