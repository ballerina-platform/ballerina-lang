/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.FullHttpMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.config.ChunkConfig;
import org.wso2.transport.http.netty.contract.ServerConnectorFuture;

/**
 * A Class responsible for handle  incoming message through netty inbound pipeline.
 */
public class AggregatorSourceHandler extends SourceHandler {
    private static Logger log = LoggerFactory.getLogger(AggregatorSourceHandler.class);

    public AggregatorSourceHandler(ServerConnectorFuture serverConnectorFuture,
            String interfaceId, ChunkConfig chunkConfig) {
        super(serverConnectorFuture, interfaceId, chunkConfig);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpMessage) {
            FullHttpMessage fullHttpMessage = (FullHttpMessage) msg;
            sourceReqCmsg = setupCarbonMessage(fullHttpMessage, ctx);
            notifyRequestListener(sourceReqCmsg, ctx);
            ByteBuf content = ((FullHttpMessage) msg).content();
            sourceReqCmsg.addHttpContent(new DefaultLastHttpContent(content));
            if (handlerExecutor != null) {
                handlerExecutor.executeAtSourceRequestSending(sourceReqCmsg);
            }
        } else {
            log.warn("Unexpected netty content detected : " + msg.toString());
        }
    }
}
