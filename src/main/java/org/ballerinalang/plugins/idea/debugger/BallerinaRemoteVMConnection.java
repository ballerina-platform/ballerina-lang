/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.plugins.idea.debugger;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.concurrency.AsyncPromise;
import org.jetbrains.debugger.Vm;
import org.jetbrains.debugger.connection.RemoteVmConnection;
import org.jetbrains.io.NettyKt;
import org.jetbrains.io.NettyUtil;
import org.jetbrains.io.webSocket.WebSocketProtocolHandler;

import java.net.InetSocketAddress;

public class BallerinaRemoteVMConnection extends RemoteVmConnection {

    @NotNull
    @Override
    public Bootstrap createBootstrap(@NotNull InetSocketAddress address, @NotNull AsyncPromise<Vm> vmResult) {

//        return NettyUtil.nioClientBootstrap().remoteAddress(address).handler(
        //                //                new ChannelInitializer() {
        //                //                    @Override
        //                //                    protected void initChannel(@NotNull Channel channel) throws Exception {
        //                //                        vmResult.setResult(new BallerinaVM(getDebugEventListener(),
        // channel));
        //                //                    }
        //                //                }
        //
        //                new WebSocketProtocolHandler() {
        //
        //                    @Override
        //                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //                        Channel channel = ctx.channel();
        //                        vmResult.setResult(new BallerinaVM(getDebugEventListener(), channel));
        //                    }
        //
        //                    @Override
        //                    protected void textFrameReceived(Channel channel, TextWebSocketFrame textWebSocketFrame) {
        //                        vmResult.setResult(new BallerinaVM(getDebugEventListener(), channel));
        //                    }
        //                }
        //
        //
        //        );
        return NettyKt.oioClientBootstrap().handler(


                //                new WebSocketProtocolHandler() {
                //                    @Override
                //                    protected void textFrameReceived(Channel channel, TextWebSocketFrame
                // textWebSocketFrame) {
                //                        vmResult.setResult(new BallerinaVM(getDebugEventListener(), channel));
                //                    }
                //                }


                new ChannelInitializer() {
                    @Override
                    protected void initChannel(@NotNull Channel channel) throws Exception {
                        vmResult.setResult(new BallerinaVM(getDebugEventListener(), channel));
                    }
                }
        );
    }

    @NotNull
    @Override
    protected String connectedAddressToPresentation(@NotNull InetSocketAddress address, @NotNull Vm vm) {
        return address.toString();
    }
}
