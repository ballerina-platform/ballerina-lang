/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.transport.http.netty.contractimpl.sender.states.http2;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contractimpl.common.states.Http2MessageStateContext;
import org.wso2.transport.http.netty.contractimpl.sender.http2.Http2TargetHandler;

/**
 * State of successfully read response
 */
public class EntityBodyReceived implements SenderState {

    private static final Logger LOG = LoggerFactory.getLogger(EntityBodyReceived.class);

    @Override
    public void writeOutboundRequestHeaders(Http2TargetHandler.Http2RequestWriter requestWriter,
                                            ChannelHandlerContext ctx, HttpContent httpContent) {
        LOG.warn("writeOutboundRequestHeaders is not a dependant action of this state");
    }

    @Override
    public void writeOutboundRequestEntity(Http2TargetHandler.Http2RequestWriter requestWriter,
                                           ChannelHandlerContext ctx, HttpContent httpContent) {
        LOG.warn("writeOutboundRequestEntity is not a dependant action of this state");
    }

    @Override
    public void readInboundResponseHeaders(Http2TargetHandler targetHandler, ChannelHandlerContext ctx, Object msg,
                                           Http2MessageStateContext http2MessageStateContext) {
        LOG.warn("readInboundResponseHeaders is not a dependant action of this state");
    }

    @Override
    public void readInboundResponseEntityBody(Http2TargetHandler targetHandler, ChannelHandlerContext ctx, Object msg,
                                              Http2MessageStateContext http2MessageStateContext) {

        LOG.warn("readInboundResponseEntityBody is not a dependant action of this state");
    }
}
