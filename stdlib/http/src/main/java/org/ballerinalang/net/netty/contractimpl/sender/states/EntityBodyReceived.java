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
import org.ballerinalang.net.netty.contract.Constants;
import org.ballerinalang.net.netty.contract.HttpResponseFuture;
import org.ballerinalang.net.netty.contractimpl.common.states.SenderReqRespStateManager;
import org.ballerinalang.net.netty.contractimpl.common.states.StateUtil;
import org.ballerinalang.net.netty.contractimpl.sender.TargetHandler;
import org.ballerinalang.net.netty.message.HttpCarbonMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * State of handling request with expect-continue.
 */
public class EntityBodyReceived implements SenderState {

    private static final Logger LOG = LoggerFactory.getLogger(EntityBodyReceived.class);

    private final org.ballerinalang.net.netty.contractimpl.common.states.SenderReqRespStateManager
            senderReqRespStateManager;

    EntityBodyReceived(SenderReqRespStateManager senderReqRespStateManager) {
        this.senderReqRespStateManager =  senderReqRespStateManager;
    }

    @Override
    public void writeOutboundRequestHeaders(org.ballerinalang.net.netty.message.HttpCarbonMessage httpOutboundRequest) {
        LOG.warn("writeOutboundRequestHeaders {}", StateUtil.ILLEGAL_STATE_ERROR);
    }

    @Override
    public void writeOutboundRequestEntity(org.ballerinalang.net.netty.message.HttpCarbonMessage httpOutboundRequest, HttpContent httpContent) {
        // This method might get called when inbound response is received but outbound request is not yet completed.
        // In that instance, remote host doesn't require request content anymore, Therefore contents are released.
        httpContent.release();
    }

    @Override
    public void readInboundResponseHeaders(org.ballerinalang.net.netty.contractimpl.sender.TargetHandler targetHandler, HttpResponse httpInboundResponse) {
        LOG.warn("readInboundResponseHeaders {}", StateUtil.ILLEGAL_STATE_ERROR);
    }

    @Override
    public void readInboundResponseEntityBody(ChannelHandlerContext ctx, HttpContent httpContent,
                                              HttpCarbonMessage inboundResponseMsg) {
        LOG.warn("readInboundResponseEntityBody {}", StateUtil.ILLEGAL_STATE_ERROR);
    }

    @Override
    public void handleAbruptChannelClosure(org.ballerinalang.net.netty.contractimpl.sender.TargetHandler targetHandler, org.ballerinalang.net.netty.contract.HttpResponseFuture httpResponseFuture) {
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
