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
import org.wso2.siddhi.tcp.transport.utils.EventComposite;
import org.wso2.siddhi.tcp.transport.handlers.EventEncoder;

import java.util.ArrayList;

public class TcpNettyClient {
    private static final Logger log = Logger.getLogger(TcpNettyClient.class);

    /**
     * @param args
     */
    public static void main(String[] args) {

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(
                                    new EventEncoder()
                            );
                        }
                    });


            // Start the connection attempt.
            Channel ch = b.connect("localhost", 8080).sync().channel();
            ChannelFuture cf;
            for (int i = 0; i < 1000000; i++) {
                ArrayList<Event> arrayList = new ArrayList<Event>(100);
                for (int j = 0; j < 50; j++) {
                    arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"WSO2", i, 10}));
                    arrayList.add(new Event(System.currentTimeMillis(), new Object[]{"IBM", i, 10}));
                }
                EventComposite EventComposite = new EventComposite("test","StockStream", arrayList.toArray(new Event[10]));
                cf = ch.write(EventComposite);
                ch.flush();
                cf.await();

//                if (i * 10 % 10000 == 0) {
//                    Thread.sleep(10000);
                    log.info("Done Sending " + i * 10 + " events..");
//                }
            }

            ch.close().sync();
        } catch (InterruptedException e) {
            log.error("Error sending messages " + e.getMessage(), e);
        } finally {
            group.shutdownGracefully();
        }

    }


}



