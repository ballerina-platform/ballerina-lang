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

import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.LastHttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.SourceInteractiveState;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import static org.wso2.transport.http.netty.common.Constants.REMOTE_CLIENT_CLOSED_BEFORE_INITIATING_OUTBOUND_RESPONSE;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_CLIENT_CLOSED_WHILE_READING_INBOUND_REQUEST;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_SERVER_CLOSED_BEFORE_INITIATING_INBOUND_RESPONSE;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_SERVER_CLOSED_BEFORE_INITIATING_OUTBOUND_REQUEST;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_SERVER_CLOSED_WHILE_READING_INBOUND_RESPONSE;
import static org.wso2.transport.http.netty.common.Constants.REMOTE_SERVER_CLOSED_WHILE_WRITING_OUTBOUND_REQUEST;

/**
 * Handle all the errors related to target-handler.
 */
public class TargetErrorHandler {

    private static Logger log = LoggerFactory.getLogger(TargetErrorHandler.class);
    private HttpResponseFuture httpResponseFuture;
    private SourceInteractiveState state;

    protected void handleErrorCloseScenario(HTTPCarbonMessage inboundResponseMsg, HttpResponseFuture httpOutRespStatusFuture) {
        switch (state) {
            case CONNECTED:
                httpResponseFuture.notifyHttpListener(
                        new ServerConnectorException(REMOTE_SERVER_CLOSED_BEFORE_INITIATING_OUTBOUND_REQUEST));
                // Error is notified to server connector. Debug log is to make transport layer aware
                log.debug(REMOTE_SERVER_CLOSED_BEFORE_INITIATING_OUTBOUND_REQUEST);
                break;
            case SENDING_ENTITY_BODY:
                log.error(REMOTE_SERVER_CLOSED_WHILE_WRITING_OUTBOUND_REQUEST);
                break;
            case ENTITY_BODY_SENT:
                log.error(REMOTE_SERVER_CLOSED_BEFORE_INITIATING_INBOUND_RESPONSE);
                break;
            case RECEIVING_ENTITY_BODY:
                handleIncompleteInboundResponse(inboundResponseMsg,
                        REMOTE_SERVER_CLOSED_WHILE_READING_INBOUND_RESPONSE);
                break;
            case ENTITY_BODY_RECEIVED:
                break;
            default:
                log.error("Unexpected state detected ", state);
        }
    }

    private void handleIncompleteInboundResponse(HTTPCarbonMessage inboundResponseMsg, String errorMessage) {
        LastHttpContent lastHttpContent = new DefaultLastHttpContent();
        lastHttpContent.setDecoderResult(DecoderResult.failure(new DecoderException(errorMessage)));
        inboundResponseMsg.addHttpContent(lastHttpContent);
        log.warn(errorMessage);
    }

    public void setState(SourceInteractiveState state) {
        this.state = state;
    }

    public void setResponseFuture(HttpResponseFuture httpResponseFuture) {
        this.httpResponseFuture = httpResponseFuture;
    }
}


