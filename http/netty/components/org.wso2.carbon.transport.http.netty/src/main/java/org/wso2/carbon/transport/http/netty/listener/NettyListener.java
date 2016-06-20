/**
 * Copyright (c) 2015, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.transport.http.netty.listener;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.CarbonTransportInitializer;
import org.wso2.carbon.messaging.Constants;
import org.wso2.carbon.messaging.TransportListener;
import org.wso2.carbon.messaging.TransportListenerManager;
import org.wso2.carbon.transport.http.netty.common.Util;
import org.wso2.carbon.transport.http.netty.common.ssl.SSLConfig;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.Parameter;
import org.wso2.carbon.transport.http.netty.internal.NettyTransportContextHolder;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class that starts the netty server bootstrap in given port.
 */
public class NettyListener extends TransportListener {
    private static final Logger log = LoggerFactory.getLogger(NettyListener.class);

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ServerBootstrap bootstrap;
    private ListenerConfiguration nettyConfig;
    private Map<Integer, ChannelFuture> channelFutureMap = new ConcurrentHashMap<>();

    private Map<String, SSLConfig> sslConfigMap = new ConcurrentHashMap<>();

    public NettyListener(ListenerConfiguration nettyConfig) {
        super(nettyConfig.getId());
        this.nettyConfig = nettyConfig;
    }

    public void start() {
        log.info("Starting Netty Http Transport Listener");
        startTransport();
    }

    private void startTransport() {
        ServerBootstrapConfiguration.createBootStrapConfiguration(nettyConfig.getParameters());
        ServerBootstrapConfiguration serverBootstrapConfiguration = ServerBootstrapConfiguration.getInstance();
        bossGroup = new NioEventLoopGroup(nettyConfig.getBossThreadPoolSize());
        workerGroup = new NioEventLoopGroup(nettyConfig.getWorkerThreadPoolSize());
        log.debug("Netty Boss group size " + bossGroup);
        log.debug("Netty Worker group Size" + workerGroup);
        bootstrap = new ServerBootstrap();
        bootstrap.option(ChannelOption.SO_BACKLOG, serverBootstrapConfiguration.getSoBackLog());
        log.debug("Netty Server Socket BACKLOG " + serverBootstrapConfiguration.getSoBackLog());

        bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class);

        addChannelInitializer();
        bootstrap.childOption(ChannelOption.TCP_NODELAY, serverBootstrapConfiguration.isTcpNoDelay());
        log.debug("Netty Server Socket TCP_NODELAY " + serverBootstrapConfiguration.isTcpNoDelay());
        bootstrap.option(ChannelOption.SO_KEEPALIVE, serverBootstrapConfiguration.isKeepAlive());
        log.debug("Netty Server Socket SO_KEEPALIVE " + serverBootstrapConfiguration.isKeepAlive());
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, serverBootstrapConfiguration.getConnectTimeOut());
        log.debug(" Netty Server Socket CONNECT_TIMEOUT_MILLIS " + serverBootstrapConfiguration.getConnectTimeOut());

        bootstrap.option(ChannelOption.SO_SNDBUF, serverBootstrapConfiguration.getSendBufferSize());
        log.debug("Netty Server Socket SO_SNDBUF " + serverBootstrapConfiguration.getSendBufferSize());
        bootstrap.option(ChannelOption.SO_RCVBUF, serverBootstrapConfiguration.getReciveBufferSize());
        log.debug("Netty Server Socket SO_RCVBUF " + serverBootstrapConfiguration.getReciveBufferSize());
        bootstrap.childOption(ChannelOption.SO_RCVBUF, serverBootstrapConfiguration.getReciveBufferSize());
        log.debug("Netty Server Socket SO_RCVBUF " + serverBootstrapConfiguration.getReciveBufferSize());
        bootstrap.childOption(ChannelOption.SO_SNDBUF, serverBootstrapConfiguration.getSendBufferSize());
        log.debug("Netty Server Socket SO_SNDBUF " + serverBootstrapConfiguration.getSendBufferSize());

        setupChannelInitializer();
        try {
            ChannelFuture future = bootstrap.bind(new InetSocketAddress(nettyConfig.getHost(), nettyConfig.getPort()))
                    .sync();
            if (future.isSuccess()) {
                TransportListenerManager artifactDeployer = NettyTransportContextHolder.getInstance().getManager();
                if (artifactDeployer != null) {
                    artifactDeployer.registerTransportListener(id, this);
                }
                log.info("Netty Listener starting on port " + nettyConfig.getPort());
            } else {
                log.error("Netty Listener cannot start on port " + nettyConfig.getPort());
            }

        } catch (InterruptedException e) {
            log.error("Netty Listener cannot start on port " + nettyConfig.getPort(), e);
        }
    }

    private void setupChannelInitializer() {
        CarbonTransportInitializer channelInitializer = NettyTransportContextHolder.getInstance()
                .getServerChannelInitializer(nettyConfig.getId());
        if (channelInitializer == null) {
            // start with the default initializer
            channelInitializer = new CarbonNettyServerInitializer(nettyConfig);
        }

        List<Parameter> parameters = nettyConfig.getParameters();
        if (parameters != null && !parameters.isEmpty()) {
            Map<String, String> paramMap = new HashMap<>(parameters.size());
            for (Parameter parameter : parameters) {
                paramMap.put(parameter.getName(), parameter.getValue());
            }

            channelInitializer.setup(paramMap);
        }
        NettyTransportContextHolder.getInstance().addNettyChannelInitializer(nettyConfig.getId(), channelInitializer);
    }

    private void addChannelInitializer() {
        NettyServerInitializer handler = new NettyServerInitializer(id);
        handler.setSslConfig(nettyConfig.getSslConfig());
        handler.setSslConfigMap(sslConfigMap);
        bootstrap.childHandler(handler);
    }

    @Override
    public void stop() {
        log.info("Stopping Netty transport " + id + " on port " + nettyConfig.getPort());
        shutdownEventLoops();
    }

    @Override
    public void beginMaintenance() {
        log.info("Putting Netty transport " + id + " on port " + nettyConfig.getPort() + " into maintenance mode");
        shutdownEventLoops();
    }

    @Override
    public void endMaintenance() {
        log.info("Ending maintenance mode for Netty transport " + id + " running on port " + nettyConfig.getPort());
        bossGroup = new NioEventLoopGroup(nettyConfig.getBossThreadPoolSize());
        workerGroup = new NioEventLoopGroup(nettyConfig.getWorkerThreadPoolSize());
        startTransport();
    }

    private void shutdownEventLoops() {
        Future<?> f = bossGroup.shutdownGracefully();
        f.addListener(new GenericFutureListener<Future<Object>>() {
            @Override
            public void operationComplete(Future<Object> future) throws Exception {
                Future f = workerGroup.shutdownGracefully();
                f.addListener(new GenericFutureListener<Future<Object>>() {
                    @Override
                    public void operationComplete(Future<Object> future) throws Exception {
                        log.info("Netty transport " + id + " on port " + nettyConfig.getPort() +
                                " stopped successfully");
                    }
                });
            }
        });
    }

    @Override
    public void setMessageProcessor(CarbonMessageProcessor carbonMessageProcessor) {

    }

    @Override
    public boolean listen(String host, int port) {
        try {
            //TODO check for host verification
            ChannelFuture future = bootstrap.bind(new InetSocketAddress(host, port)).sync();
            if (future.isSuccess()) {
                channelFutureMap.put(port, future);
                log.info("Netty Listener starting on host  " + host + " and port " + port);
                return true;
            } else {
                log.error("Cannot bind port for host " + host + " port " + port);
            }

        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean listen(String host, int port, Map<String, String> map) {
        String id = host + ":" + port;
        if (map != null) {
            List<Parameter> parameters = new ArrayList<>();
            String certPass = map.get(Constants.CERTPASS);
            String keyStorePass = map.get(Constants.KEYSTOREPASS);
            String keyStoreFile = map.get(Constants.KEYSTOREFILE);
            String trustoreFile = map.get(Constants.TRUSTSTOREFILE);
            String trustorePass = map.get(Constants.TRUSTSTOREPASS);
            for (Map.Entry entry : map.entrySet()) {
                Parameter parameter = new Parameter();
                parameter.setName((String) entry.getKey());
                parameter.setValue((String) entry.getValue());
                parameters.add(parameter);
            }
            SSLConfig sslConfig = Util
                    .getSSLConfigForListener(certPass, keyStorePass, keyStoreFile, trustoreFile, trustorePass,
                            parameters);
            sslConfigMap.put(id, sslConfig);
            listen(host, port);
        }

        return false;
    }

    @Override
    public boolean stopListening(String host, int port) {
        String id = host + ":" + port;
        //TODO check for host verification
        ChannelFuture future = channelFutureMap.remove(port);
        if (future != null) {
            if (sslConfigMap.get(id) != null) {
                sslConfigMap.remove(id);
            }
            future.channel().close();
            log.info("Netty Listener stopped  listening on  host  " + host + " and port " + port);
            return true;
        }
        return false;
    }
}
