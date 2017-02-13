/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.transport.http.netty.util.websocket.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import org.wso2.carbon.transport.http.netty.util.TestUtil;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * WebSocket client class for test
 */
public class WebSocketClient {

    private final String url = System.getProperty("url", String.format("ws://%s:%d/%s",
                                                        TestUtil.TEST_HOST, TestUtil.TEST_SERVER_PORT, "test"));
    private Channel channel = null;

    /**
     * @param host host of the WebSocket client.
     * @param port port of the WebSocket client.
     * @return true if the handshake is done properly.
     * @throws URISyntaxException throws if there is an error in the URI syntax.
     * @throws InterruptedException throws if the connecting the server is interrupted.
     */
    public boolean handhshake(String host, int port) throws InterruptedException, URISyntaxException {
        URI uri = new URI(url);
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new WebSocketClientInitializer());
        channel = b.connect(host, port).sync().channel();
        WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.
                newHandshaker(uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders());
        boolean isDone = handshaker.handshake(channel).sync().isSuccess();
        return isDone;
    }

    public boolean sendAndReceiveWebSocketFrames() {
        ChannelFuture channelFuture = channel.writeAndFlush(new TextWebSocketFrame("Test"));
        if (channelFuture.isDone()) {
            return true;
        } else {
            return false;
        }
    }
}
