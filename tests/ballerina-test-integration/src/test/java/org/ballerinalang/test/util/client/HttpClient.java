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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.util.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * A simple Netty based HTTP client to be used in test cases.
 */
public class HttpClient {

    private static final Logger log = LoggerFactory.getLogger(HttpClient.class);

    private Channel connectedChannel;
    private final String host;
    private final int port;
    private final ResponseHandler responseHandler;
    private CountDownLatch waitForConnectionClosureLatch;

    public HttpClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.responseHandler = new ResponseHandler();

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap clientBootStrap = new Bootstrap();
            clientBootStrap.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(host, port))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new HttpClientCodec());
                            ch.pipeline().addLast(new HttpObjectAggregator(1024 * 512));
                            ch.pipeline().addLast(responseHandler);
                        }
                    });
            connectedChannel = clientBootStrap.connect().sync().channel();
        } catch (Exception e) {
            log.error("Error while initializing the client", e);
        }
    }

    public FullHttpResponse sendChunkRequest(FullHttpRequest httpRequest) {
        httpRequest.headers().set(HttpHeaderNames.TRANSFER_ENCODING, Constants.CHUNKED);
        return send(httpRequest);
    }

    public FullHttpResponse sendRequest(FullHttpRequest httpRequest) {
        httpRequest.headers().set(HttpHeaderNames.CONTENT_LENGTH, httpRequest.content().readableBytes());
        return send(httpRequest);
    }

    private FullHttpResponse send(FullHttpRequest httpRequest) {
        CountDownLatch latch = new CountDownLatch(1);
        this.waitForConnectionClosureLatch = new CountDownLatch(1);
        this.responseHandler.setLatch(latch);
        this.responseHandler.setWaitForConnectionClosureLatch(this.waitForConnectionClosureLatch);

        httpRequest.headers().set(HttpHeaderNames.HOST, host + ":" + port);
        this.connectedChannel.writeAndFlush(httpRequest);
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.warn("Interrupted before receiving the response");
        }
        return this.responseHandler.getHttpFullResponse();
    }

    public List<FullHttpResponse> sendExpectContinueRequest(DefaultHttpRequest httpRequest,
                                                            DefaultLastHttpContent httpContent) {
        CountDownLatch latch = new CountDownLatch(1);
        this.waitForConnectionClosureLatch = new CountDownLatch(1);
        this.responseHandler.setLatch(latch);
        this.responseHandler.setWaitForConnectionClosureLatch(this.waitForConnectionClosureLatch);

        httpRequest.headers().set(HttpHeaderNames.HOST, host + ":" + port);
        httpRequest.headers().set(HttpHeaderNames.EXPECT, HttpHeaderValues.CONTINUE);
        this.connectedChannel.writeAndFlush(httpRequest);

        try {
            latch.await();
        } catch (InterruptedException e) {
            log.warn("Interrupted before receiving the response.");
        }

        FullHttpResponse response100Continue = this.responseHandler.getHttpFullResponse();

        if (response100Continue.status().equals(HttpResponseStatus.CONTINUE)) {
            latch = new CountDownLatch(1);
            this.responseHandler.setLatch(latch);
            this.connectedChannel.writeAndFlush(httpContent);
            try {
                latch.await();
            } catch (InterruptedException e) {
                log.warn("Interrupted before receiving the response.");
            }
        }

        return responseHandler.getHttpFullResponses();
    }

    public LinkedList<FullHttpResponse> sendTwoInPipeline(FullHttpRequest httpRequest) {
        CountDownLatch latch = new CountDownLatch(2);
        this.waitForConnectionClosureLatch = new CountDownLatch(2);
        this.responseHandler.setLatch(latch);
        this.responseHandler.setWaitForConnectionClosureLatch(this.waitForConnectionClosureLatch);

        httpRequest.headers().set(HttpHeaderNames.HOST, host + ":" + port);
        this.connectedChannel.writeAndFlush(httpRequest.copy());

        this.connectedChannel.writeAndFlush(httpRequest);
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.warn("Interrupted before receiving the response");
        }
        return this.responseHandler.getHttpFullResponses();
    }

    public boolean waitForChannelClose() {
        try {
            if (!this.waitForConnectionClosureLatch.await(5, TimeUnit.SECONDS)) {
                return false;
            }
        } catch (InterruptedException e) {
            log.warn("Interrupted before receiving the response");
            return false;
        }
        return true;
    }
}
