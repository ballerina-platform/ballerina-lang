/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.siddhi.tcp.transport;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.log4j.Logger;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.tcp.transport.handlers.EventEncoder;
import org.wso2.siddhi.tcp.transport.utils.EventComposite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class TcpNettyClient {
    private static final Logger log = Logger.getLogger(TcpNettyClient.class);

    private EventLoopGroup group;
    private Bootstrap bootstrap;
    private Channel channel;
    private String sessionId;
    private String hostAndPort;


    public TcpNettyClient() {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(
                                new EventEncoder()
                        );
                    }
                });
    }

    public void connect(String host, int port) {
        // Start the connection attempt.
        try {
            hostAndPort = host + ":" + port;
            channel = bootstrap.connect(host, port).sync().channel();
            sessionId = UUID.randomUUID() + "-" + hostAndPort;
        } catch (InterruptedException e) {
            log.error("Error connecting to '" + hostAndPort + "', " + e.getMessage(), e);
        }
    }

    public ChannelFuture send(String streamId, Event[] events) {
        EventComposite EventComposite = new EventComposite(sessionId, streamId, events);
        ChannelFuture cf = channel.writeAndFlush(EventComposite);
        cf.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    log.error("Error sending events to '" + hostAndPort + "' on stream '" + streamId +
                            "', " + future.cause() + ", dropping events " + Arrays.deepToString(events), future.cause());
                }
            }
        });
        return cf;
    }

    public void disconnect() {
        if (channel != null && channel.isOpen()) {
            try {
                channel.close();
                channel.closeFuture().sync();
            } catch (InterruptedException e) {
                log.error("Error closing connection to '" + hostAndPort + "' from client '" + sessionId +
                        "', " + e);
            }
            channel.disconnect();
            log.info("Disconnecting client to '" + hostAndPort + "' with sessionId:" + sessionId);
        }
    }

    public void shutdown() {
        disconnect();
        if (group != null) {
            group.shutdownGracefully();
        }
        log.info("Stopping client to '" + hostAndPort + "' with sessionId:" + sessionId);
        hostAndPort = null;
        sessionId = null;
    }


    public static void main(String[] args) {
        TcpNettyClient tcpNettyClient = new TcpNettyClient();
        tcpNettyClient.connect("localhost", 8080);
        for (int i = 0; i < 10000; i++) {
            ArrayList<Event> arrayList = new ArrayList<Event>(100);
            for (int j = 0; j < 5; j++) {
                arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"WSO2", i, 10}));
                arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"IBM", i, 10}));
            }
            tcpNettyClient.send("StockStream", arrayList.toArray(new Event[10]));
        }
        tcpNettyClient.disconnect();
        tcpNettyClient.shutdown();
    }

}



