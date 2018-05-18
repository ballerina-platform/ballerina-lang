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
 */

package org.wso2.transport.http.netty.contractimpl.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 * Abstract WebSocket frame handler for WebSocket server and client.
 */
public abstract class WebSocketInboundFrameHandler extends ChannelInboundHandlerAdapter {

    /**
     * Set channel promise for WebSocket connection close.
     *
     * @param promise {@link ChannelPromise} to indicate the receiving of close frame echo
     *                                      back from the remote endpoint.
     */
    public abstract void setClosePromise(ChannelPromise promise);

    /**
     * Retrieve the WebSocket connection associated with the frame handler.
     *
     * @return the WebSocket connection associated with the frame handler.
     */
    public abstract DefaultWebSocketConnection getWebSocketConnection();

    /**
     * Check whether a close frame is received without the relevant connection to this Frame handler sending a close
     * frame.
     *
     * @return true if a close frame is received without the relevant connection to this Frame handler sending a close
     * frame.
     */
    public abstract boolean isCloseFrameReceived();

    /**
     * Retrieve the {@link ChannelHandlerContext} of the {@link WebSocketInboundFrameHandler}.
     *
     * @return the {@link ChannelHandlerContext} of the {@link WebSocketInboundFrameHandler}.
     */
    public abstract ChannelHandlerContext getChannelHandlerContext();

}
