/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.transport.http.netty.listener;

import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

/**
 * Handle all the errors related to source-handler.
 */
public class SourceHandlerErrorHandler {

    private static Logger log = LoggerFactory.getLogger(SourceHandlerErrorHandler.class);

    private enum InboundState {
        CONNECTED,
        HEADERS_RECEIVED,
        RECEIVING_ENTITY_BODY, ENTITY_BODY_RECEIVED
    }

    private HTTPCarbonMessage inboundRequestMsg;
    private final ServerConnectorFuture serverConnectorFuture;
    private final HttpResponseFuture httpOutboundRespFuture;
    private InboundState state;

    public SourceHandlerErrorHandler(HTTPCarbonMessage inboundRequestMsg, ServerConnectorFuture serverConnectorFuture,
            HttpResponseFuture httpOutboundRespFuture) {
        this.inboundRequestMsg = inboundRequestMsg;
        this.serverConnectorFuture = serverConnectorFuture;
        this.httpOutboundRespFuture = httpOutboundRespFuture;
        this.state = InboundState.CONNECTED;
    }

    void handleErrorCloseScenario() {
        if (state == InboundState.RECEIVING_ENTITY_BODY) {
            handleIncompleteInboundRequest(Constants.REMOTE_CLIENT_ABRUPTLY_CLOSE_CONNECTION);
        }
    }

    void handleIdleErrorScenario() {
        try {
            switch (state) {
            case CONNECTED:
                serverConnectorFuture.notifyErrorListener(
                        new ServerConnectorException(Constants.IDLE_TIMEOUT_TRIGGERED_BEFORE_READING_INBOUND_RESPONSE));
                break;
            case RECEIVING_ENTITY_BODY:
                handleIncompleteInboundRequest(Constants.IDLE_TIMEOUT_TRIGGERED_WHILE_READING_INBOUND_REQUEST);
                break;
            case ENTITY_BODY_RECEIVED:
                // This means we have received the complete inbound request. But nothing happened
                // after that.
                serverConnectorFuture.notifyErrorListener(new ServerConnectorException(
                        Constants.IDLE_TIMEOUT_TRIGGERED_BEFORE_WRITING_OUTBOUND_RESPONSE));

                // This is to stop application layer writing response after
                // idle timeout is triggered.
                httpOutboundRespFuture.notifyHttpListener(new ServerConnectorException(
                        Constants.IDLE_TIMEOUT_TRIGGERED_BEFORE_WRITING_OUTBOUND_RESPONSE));
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
        inboundRequestMsg.addHttpContent(lastHttpContent);
        log.warn(errorMessage);
    }

    public void exceptionCaught(Throwable cause) {
        if (state != InboundState.RECEIVING_ENTITY_BODY) {
            handleIncompleteInboundRequest(Constants.EXCEPTION_CAUGHT_WHILE_READING_REQUEST);
        }
        try {
            serverConnectorFuture.notifyErrorListener(cause);
        } catch (ServerConnectorException e) {
            log.error("Couldn't notify error state to application layer");
        }
    }

    public void setStateConnected() {
        state = InboundState.CONNECTED;
    }

    public void setStateHeaderReceived() {
        state = InboundState.HEADERS_RECEIVED;
    }

    public void setStateReceivingEntityBody() {
        state = InboundState.RECEIVING_ENTITY_BODY;
    }

    public void setStateRequestReceived() {
        state = InboundState.ENTITY_BODY_RECEIVED;
    }
}
