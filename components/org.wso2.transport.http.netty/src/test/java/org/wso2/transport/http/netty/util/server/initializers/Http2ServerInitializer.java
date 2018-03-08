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

package org.wso2.transport.http.netty.util.server.initializers;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerUpgradeHandler;
import io.netty.handler.codec.http.HttpServerUpgradeHandler.UpgradeCodecFactory;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2ServerUpgradeCodec;
import io.netty.util.AsciiString;

/**
 * An initializer class for a Http2 Server.
 */
public abstract class Http2ServerInitializer extends ChannelInitializer<SocketChannel> {

    private final UpgradeCodecFactory upgradeCodecFactory = protocol -> {
        if (AsciiString.contentEquals(Http2CodecUtil.HTTP_UPGRADE_PROTOCOL_NAME, protocol)) {
            return new Http2ServerUpgradeCodec(
                    Http2FrameCodecBuilder.forServer().build(), getBusinessLogicHandler());
        } else {
            return null;
        }
    };

    @Override
    public void initChannel(SocketChannel ch) {
        configureClearText(ch);
    }

    /**
     * Configure the pipeline for a cleartext upgrade from HTTP to HTTP/2.0
     */
    private void configureClearText(SocketChannel ch) {
        final ChannelPipeline p = ch.pipeline();
        final HttpServerCodec sourceCodec = new HttpServerCodec();

        p.addLast(sourceCodec);
        p.addLast(new HttpServerUpgradeHandler(sourceCodec, upgradeCodecFactory, Integer.MAX_VALUE));
    }

    protected abstract ChannelHandler getBusinessLogicHandler();
}
