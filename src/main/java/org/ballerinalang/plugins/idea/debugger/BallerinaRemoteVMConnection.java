///*
// *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
// *
// *  Licensed under the Apache License, Version 2.0 (the "License");
// *  you may not use this file except in compliance with the License.
// *  You may obtain a copy of the License at
// *
// *  http://www.apache.org/licenses/LICENSE-2.0
// *
// *  Unless required by applicable law or agreed to in writing, software
// *  distributed under the License is distributed on an "AS IS" BASIS,
// *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *  See the License for the specific language governing permissions and
// *  limitations under the License.
// */
//
//package org.ballerinalang.plugins.idea.debugger;
//
//import com.intellij.util.io.NettyKt;
//import io.netty.bootstrap.Bootstrap;
//import io.netty.channel.Channel;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.ChannelInitializer;
//import io.netty.handler.codec.http.FullHttpRequest;
//import io.netty.handler.codec.http.FullHttpResponse;
//import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
//import io.netty.handler.codec.http.websocketx.WebSocketFrameDecoder;
//import io.netty.handler.codec.http.websocketx.WebSocketFrameEncoder;
//import io.netty.handler.codec.http.websocketx.WebSocketVersion;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.concurrency.AsyncPromise;
//import org.jetbrains.debugger.Vm;
//import org.jetbrains.debugger.connection.RemoteVmConnection;
//import org.jetbrains.io.webSocket.WebSocketProtocolHandshakeHandler;
//
//import java.net.InetSocketAddress;
//import java.net.URI;
//
//public class BallerinaRemoteVMConnection extends RemoteVmConnection {
//
//    @NotNull
//    @Override
//    public Bootstrap createBootstrap(@NotNull InetSocketAddress address, @NotNull AsyncPromise<Vm> vmResult) {
//
//        //        WebSocketClientHandshaker handshaker = new WebSocketClientHandshaker(URI.create("127.0.0.1"),
//        //                WebSocketVersion.V13, null, null, 65500) {
//        //            @Override
//        //            protected FullHttpRequest newHandshakeRequest() {
//        //                return null;
//        //            }
//        //
//        //            @Override
//        //            protected void verify(FullHttpResponse fullHttpResponse) {
//        //
//        //            }
//        //
//        //            @Override
//        //            protected WebSocketFrameDecoder newWebsocketDecoder() {
//        //                return null;
//        //            }
//        //
//        //            @Override
//        //            protected WebSocketFrameEncoder newWebSocketEncoder() {
//        //                return null;
//        //            }
//        //        };
//        //
//        //
//        //        return NettyKt.oioClientBootstrap().handler(
//        //
//        //                new WebSocketProtocolHandshakeHandler(handshaker) {
//        //                    @Override
//        //                    protected void completed() {
//        //                        super.completed();
//        //                    }
//        //
//        //                    @Override
//        //                    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
//        //                        super.channelRegistered(ctx);
//        //                    }
//        //
//        //                    @Override
//        //                    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
//        //                        super.channelUnregistered(ctx);
//        //                    }
//        //
//        //                    @Override
//        //                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        //                        super.channelActive(ctx);
//        //                    }
//        //
//        //                    @Override
//        //                    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//        //                        super.channelInactive(ctx);
//        //                    }
//        //
//        //                    @Override
//        //                    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        //                        super.channelReadComplete(ctx);
//        //                    }
//        //
//        //                    @Override
//        //                    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//        //                        super.userEventTriggered(ctx, evt);
//        //                    }
//        //
//        //                    @Override
//        //                    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
//        //                        super.channelWritabilityChanged(ctx);
//        //                    }
//        //
//        //                    @Override
//        //                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        //                        super.exceptionCaught(ctx, cause);
//        //                    }
//        //                }
//
//        return NettyKt.oioClientBootstrap().handler(
//                new ChannelInitializer() {
//                    @Override
//                    protected void initChannel(@NotNull Channel channel) throws Exception {
//                        vmResult.setResult(new BallerinaVM(getDebugEventListener(), channel));
//                    }
//                }
//        );
//    }
//
//    @NotNull
//    @Override
//    protected String connectedAddressToPresentation(@NotNull InetSocketAddress address, @NotNull Vm vm) {
//        return address.toString();
//    }
//}
