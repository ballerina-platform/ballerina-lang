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

package org.ballerinalang.net.netty.contractimpl.sender.states;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import org.ballerinalang.net.netty.contract.Constants;
import org.ballerinalang.net.netty.contract.HttpResponseFuture;
import org.ballerinalang.net.netty.contractimpl.common.Util;
import org.ballerinalang.net.netty.contractimpl.common.states.SenderReqRespStateManager;
import org.ballerinalang.net.netty.contractimpl.sender.TargetHandler;
import org.ballerinalang.net.netty.message.HttpCarbonMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * State between start and end of inbound response entity body read.
 */
public class ReceivingEntityBody implements SenderState {

    private static final Logger LOG = LoggerFactory.getLogger(ReceivingEntityBody.class);

    private final org.ballerinalang.net.netty.contractimpl.common.states.SenderReqRespStateManager
            senderReqRespStateManager;
    private final org.ballerinalang.net.netty.contractimpl.sender.TargetHandler targetHandler;

    ReceivingEntityBody(SenderReqRespStateManager senderReqRespStateManager, org.ballerinalang.net.netty.contractimpl.sender.TargetHandler targetHandler) {
        this.senderReqRespStateManager = senderReqRespStateManager;
        this.targetHandler = targetHandler;
    }

    @Override
    public void writeOutboundRequestHeaders(org.ballerinalang.net.netty.message.HttpCarbonMessage httpOutboundRequest) {
        LOG.warn("writeOutboundRequestHeaders {}", org.ballerinalang.net.netty.contractimpl.common.states.StateUtil.ILLEGAL_STATE_ERROR);
    }

    @Override
    public void writeOutboundRequestEntity(org.ballerinalang.net.netty.message.HttpCarbonMessage httpOutboundRequest, HttpContent httpContent) {
        LOG.warn("writeOutboundRequestEntity {}", org.ballerinalang.net.netty.contractimpl.common.states.StateUtil.ILLEGAL_STATE_ERROR);
    }

    @Override
    public void readInboundResponseHeaders(org.ballerinalang.net.netty.contractimpl.sender.TargetHandler targetHandler, HttpResponse httpInboundResponse) {
        LOG.warn("readInboundResponseHeaders {}", org.ballerinalang.net.netty.contractimpl.common.states.StateUtil.ILLEGAL_STATE_ERROR);
    }

    @Override
    public void readInboundResponseEntityBody(ChannelHandlerContext ctx, HttpContent httpContent,
                                              HttpCarbonMessage inboundResponseMsg) throws Exception {

        if (httpContent instanceof LastHttpContent) {
            org.ballerinalang.net.netty.contractimpl.common.states.StateUtil.setInboundTrailersToNewMessage(((LastHttpContent) httpContent).trailingHeaders(),
                                                                                                            inboundResponseMsg);
            inboundResponseMsg.addHttpContent(httpContent);
            inboundResponseMsg.setLastHttpContentArrived();
            targetHandler.resetInboundMsg();
            Util.safelyRemoveHandlers(targetHandler.getTargetChannel().getChannel().pipeline(), Constants.IDLE_STATE_HANDLER);
            senderReqRespStateManager.state = new EntityBodyReceived(senderReqRespStateManager);

            if (!Util.isKeepAlive(targetHandler.getKeepAliveConfig(), targetHandler.getOutboundRequestMsg())) {
                targetHandler.closeChannel(ctx);
            }
            targetHandler.getConnectionManager().returnChannel(targetHandler.getTargetChannel());
        } else {
            inboundResponseMsg.addHttpContent(httpContent);
        }
    }

    @Override
    public void handleAbruptChannelClosure(org.ballerinalang.net.netty.contractimpl.sender.TargetHandler targetHandler, org.ballerinalang.net.netty.contract.HttpResponseFuture httpResponseFuture) {
        org.ballerinalang.net.netty.contractimpl.common.states.StateUtil.handleIncompleteInboundMessage(targetHandler.getInboundResponseMsg(),
                                                                                                        Constants.REMOTE_SERVER_CLOSED_WHILE_READING_INBOUND_RESPONSE_BODY);
    }

    @Override
    public void handleIdleTimeoutConnectionClosure(TargetHandler targetHandler,
                                                   HttpResponseFuture httpResponseFuture, String channelID) {
        senderReqRespStateManager.nettyTargetChannel.pipeline().remove(Constants.IDLE_STATE_HANDLER);
        senderReqRespStateManager.nettyTargetChannel.close();
        org.ballerinalang.net.netty.contractimpl.common.states.StateUtil.handleIncompleteInboundMessage(targetHandler.getInboundResponseMsg(),
                                                                                                        Constants.IDLE_TIMEOUT_TRIGGERED_WHILE_READING_INBOUND_RESPONSE_BODY);
    }
}
