/**
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.transport.http.netty.listener;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.CarbonTransportServerInitializer;
import org.wso2.carbon.messaging.TransportListener;
import org.wso2.carbon.transport.http.netty.Constants;
import org.wso2.carbon.transport.http.netty.internal.NettyTransportDataHolder;
import org.wso2.carbon.transport.http.netty.internal.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.internal.config.Parameter;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class that starts the netty server bootstrap in given port.
 */
public class NettyListener extends TransportListener {
    private static final Logger log = LoggerFactory.getLogger(NettyListener.class);

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private String serverState = Constants.STATE_STOPPED;
    private ServerBootstrap bootstrap;
    private ListenerConfiguration nettyConfig;

    public NettyListener(ListenerConfiguration nettyConfig) {
        super(nettyConfig.getId());
        this.nettyConfig = nettyConfig;
        bossGroup = new NioEventLoopGroup(nettyConfig.getBossThreadPoolSize());
        workerGroup = new NioEventLoopGroup(nettyConfig.getWorkerThreadPoolSize());
    }

    public void start() {
        log.info("Starting Netty Http Transport Listener");
        startTransport();
    }

    private void startTransport() {
        bootstrap = new ServerBootstrap();
        bootstrap.option(ChannelOption.SO_BACKLOG, 100);
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class);
        addChannelInitializer();
        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15000);

        bootstrap.option(ChannelOption.SO_SNDBUF, 1048576);
        bootstrap.option(ChannelOption.SO_RCVBUF, 1048576);
        bootstrap.childOption(ChannelOption.SO_RCVBUF, 1048576);
        bootstrap.childOption(ChannelOption.SO_SNDBUF, 1048576);

        setupChannelInitializer();
        try {
            bootstrap.bind(new InetSocketAddress(nettyConfig.getHost(), nettyConfig.getPort())).sync();
            serverState = Constants.STATE_STARTED;
            log.info("Netty Listener starting on port " + nettyConfig.getPort());
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void setupChannelInitializer() {
        CarbonTransportServerInitializer channelInitializer = new CarbonNettyInitializer();

        List<Parameter> parameters = nettyConfig.getParameters();
        if (parameters != null && !parameters.isEmpty()) {
            Map<String, String> paramMap = new HashMap<>(parameters.size());
            for (Parameter parameter : parameters) {
                paramMap.put(parameter.getName(), parameter.getValue());
            }

            channelInitializer.setup(paramMap);
        }
        NettyTransportDataHolder.getInstance().setCarbonNettyInitializer(channelInitializer);
    }

    private void addChannelInitializer() {
        NettyServerInitializer handler = new NettyServerInitializer(id);
        handler.setSslConfig(nettyConfig.getSslConfig());
        bootstrap.childHandler(handler);
    }

    @Override
    public void stop() {
        serverState = Constants.STATE_TRANSITION;
        log.info("Stopping Netty transport " + id + " on port " + nettyConfig.getPort());
        shutdownEventLoops();
    }

    @Override
    public void beginMaintenance() {
        serverState = Constants.STATE_TRANSITION;
        log.info("Putting Netty transport " + id + " on port " + nettyConfig.getPort() + " into maintenance mode");
        shutdownEventLoops();
    }

    @Override
    public void endMaintenance() {
        serverState = Constants.STATE_TRANSITION;
        log.info("Ending maintenance mode for Netty transport " + id + " running on port " + nettyConfig.getPort());
        bossGroup = new NioEventLoopGroup(nettyConfig.getBossThreadPoolSize());
        workerGroup = new NioEventLoopGroup(nettyConfig.getWorkerThreadPoolSize());
        startTransport();
    }

    public String getState() {
        return serverState;
    }

    private void shutdownEventLoops() {
        Future<?> f = workerGroup.shutdownGracefully();
        f.addListener(new GenericFutureListener<Future<Object>>() {
            @Override
            public void operationComplete(Future<Object> future) throws Exception {
                Future f = bossGroup.shutdownGracefully();
                f.addListener(new GenericFutureListener<Future<Object>>() {
                    @Override
                    public void operationComplete(Future<Object> future) throws Exception {
                        log.info("Netty transport " + id + " on port " + nettyConfig.getPort() +
                                " stopped successfully");
                        serverState = Constants.STATE_STOPPED;
                    }
                });
            }
        });
    }

    @Override
    public void setEngine(CarbonMessageProcessor carbonMessageProcessor) {
        NettyTransportDataHolder.getInstance().setEngine(carbonMessageProcessor);
    }
}
