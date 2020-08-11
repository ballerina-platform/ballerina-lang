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

package org.wso2.transport.http.netty.contractimpl.sender.states;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contractimpl.common.states.SenderReqRespStateManager;
import org.wso2.transport.http.netty.contractimpl.sender.TargetHandler;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import static org.wso2.transport.http.netty.contractimpl.common.states.StateUtil.ILLEGAL_STATE_ERROR;

/**
 * State of handling request with expect-continue.
 */
public class EntityBodyReceived implements SenderState {

    private static final Logger LOG = LoggerFactory.getLogger(EntityBodyReceived.class);

    private final SenderReqRespStateManager senderReqRespStateManager;

    EntityBodyReceived(SenderReqRespStateManager senderReqRespStateManager) {
        this.senderReqRespStateManager =  senderReqRespStateManager;
    }

    @Override
    public void writeOutboundRequestHeaders(HttpCarbonMessage httpOutboundRequest) {
        LOG.warn("writeOutboundRequestHeaders {}", ILLEGAL_STATE_ERROR);
    }

    @Override
    public void writeOutboundRequestEntity(HttpCarbonMessage httpOutboundRequest, HttpContent httpContent) {
        // This method might get called when inbound response is received but outbound request is not yet completed.
        // In that instance, remote host doesn't require request content anymore, Therefore contents are released.
        httpContent.release();
    }

    @Override
    public void readInboundResponseHeaders(TargetHandler targetHandler, HttpResponse httpInboundResponse) {
        LOG.warn("readInboundResponseHeaders {}", ILLEGAL_STATE_ERROR);
    }

    @Override
    public void readInboundResponseEntityBody(ChannelHandlerContext ctx, HttpContent httpContent,
                                              HttpCarbonMessage inboundResponseMsg) {
        LOG.warn("readInboundResponseEntityBody {}", ILLEGAL_STATE_ERROR);
    }

    @Override
    public void handleAbruptChannelClosure(TargetHandler targetHandler, HttpResponseFuture httpResponseFuture) {
        LOG.debug("Closing the channel once response is received");
    }

    @Override
    public void handleIdleTimeoutConnectionClosure(TargetHandler targetHandler,
                                                   HttpResponseFuture httpResponseFuture, String channelID) {
        senderReqRespStateManager.nettyTargetChannel.pipeline().remove(Constants.IDLE_STATE_HANDLER);
        senderReqRespStateManager.nettyTargetChannel.close();
        LOG.debug("Closing the channel once response is received");
    }
}
