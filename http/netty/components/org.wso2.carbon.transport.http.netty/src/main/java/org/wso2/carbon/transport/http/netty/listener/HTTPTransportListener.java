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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.TransportListener;
import org.wso2.carbon.messaging.TransportListenerManager;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.common.ssl.SSLConfig;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.Parameter;
import org.wso2.carbon.transport.http.netty.internal.HTTPTransportContextHolder;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A class that starts the HTTP Server Bootstrap in given port and capable of binding interfaces to Server Bootstrap.
 */
public class HTTPTransportListener extends TransportListener {
    private static final Logger log = LoggerFactory.getLogger(HTTPTransportListener.class);

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private int bossGroupSize;
    private int workerGroupSize;

    private ServerBootstrap bootstrap;
    private ListenerConfiguration defaultListenerConfig;
    private Map<Integer, ChannelFuture> channelFutureMap = new ConcurrentHashMap<>();
    //Map used for cache listener configurations
    private Map<String, ListenerConfiguration> listenerConfigurationMap = new HashMap<>();
    //Map used for  map listener configurations with host and post as key for used in channel initializer
    private Map<String, ListenerConfiguration> listenerConfigMapWithHostPort = new HashMap<>();

    private Map<String, SSLConfig> sslConfigMap = new ConcurrentHashMap<>();

    public HTTPTransportListener(int bossGroupSize, int workerGroupSize,
            Set<ListenerConfiguration> listenerConfigurationSet) {
        super("ServerBootStrapper");
        if (listenerConfigurationSet.isEmpty()) {
            log.error("Cannot find registered listener configurations  hence cannot start the transport listeners");
            return;
        }
        listenerConfigurationMap = listenerConfigurationSet.stream()
                .collect(Collectors.toMap(ListenerConfiguration::getId, config -> config));
        listenerConfigurationSet.forEach(config -> {
            String host = config.getHost();
            int port = config.getPort();
            String id = host + ":" + port;
            listenerConfigMapWithHostPort.put(id, config);
            if (host.equals(Constants.DEFAULT_ADDRESS) || host.equals(Constants.LOCALHOST) || host
                    .equals(Constants.LOOP_BACK_ADDRESS)) {
                id = Constants.LOCALHOST + ":" + port;
                listenerConfigMapWithHostPort.put(id, config);
                id = Constants.LOOP_BACK_ADDRESS + ":" + port;
                listenerConfigMapWithHostPort.put(id, config);
                id = Constants.DEFAULT_ADDRESS + ":" + port;
                listenerConfigMapWithHostPort.put(id, config);
            }
        });
        Iterator itr = listenerConfigurationSet.iterator();
        if (itr.hasNext()) {
            defaultListenerConfig = (ListenerConfiguration) itr.next();
        }
        this.bossGroupSize = bossGroupSize;
        this.workerGroupSize = workerGroupSize;
    }

    @Override
    public void start() {
        log.info("Starting  HTTP Transport Listener");
        startTransport();
    }

    //configure bootstrap and bind bootstrap for default configuration
    private void startTransport() {
        //Create Bootstrap Configuration from listener parameters
        ServerBootstrapConfiguration.createBootStrapConfiguration(defaultListenerConfig.getParameters());
        ServerBootstrapConfiguration serverBootstrapConfiguration = ServerBootstrapConfiguration.getInstance();
        //boss group is for accepting channels
        bossGroup = new NioEventLoopGroup(
                bossGroupSize != 0 ? bossGroupSize : Runtime.getRuntime().availableProcessors());
        //worker group is for processing IO
        workerGroup = new NioEventLoopGroup(
                workerGroupSize != 0 ? workerGroupSize : Runtime.getRuntime().availableProcessors() * 2);
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

        try {
            ChannelFuture future = bootstrap
                    .bind(new InetSocketAddress(defaultListenerConfig.getHost(), defaultListenerConfig.getPort()))
                    .sync();
            if (future.isSuccess()) {
                TransportListenerManager artifactDeployer = HTTPTransportContextHolder.getInstance().getManager();
                if (artifactDeployer != null) {
                    artifactDeployer.registerTransportListener(this);
                }
                if (defaultListenerConfig.getSslConfig() == null) {
                    log.info("HTTP Listener starting on port " + defaultListenerConfig.getPort());
                } else {
                    log.info("HTTPS Listener starting on port " + defaultListenerConfig.getPort());
                }
            } else {
                log.error("HTTP/S Listener cannot start on port " + defaultListenerConfig.getPort());
            }

        } catch (InterruptedException e) {
            log.error("HTTP/S Listener cannot start on port " + defaultListenerConfig.getPort(), e);
        }
    }

    //Channel Initializer is responsible for create channel pipeline
    private void addChannelInitializer() {
        CarbonHTTPServerInitializer handler = new CarbonHTTPServerInitializer(listenerConfigMapWithHostPort);
        handler.setSslConfig(defaultListenerConfig.getSslConfig());
        handler.setSslConfigMap(sslConfigMap);
        List<Parameter> parameters = defaultListenerConfig.getParameters();
        Map<String, String> paramMap = new HashMap<>();
        if (parameters != null && !parameters.isEmpty()) {
            paramMap = parameters.stream().collect(Collectors.toMap(Parameter::getName, Parameter::getValue));

        }
        handler.setup(paramMap);
        bootstrap.childHandler(handler);
    }

    @Override
    public void stop() {
        log.info("Stopping HTTP transport " + id + " on port " + defaultListenerConfig.getPort());
        shutdownEventLoops();
    }

    @Override
    public void beginMaintenance() {
        log.info("Putting HTTP transport " + id + " on port " + defaultListenerConfig.getPort()
                + " into maintenance mode");
        shutdownEventLoops();
    }

    @Override
    public void endMaintenance() {
        log.info("Ending maintenance mode for HTTP transport " + id + " running on port " + defaultListenerConfig
                .getPort());
        bossGroup = new NioEventLoopGroup(
                bossGroupSize != 0 ? bossGroupSize : Runtime.getRuntime().availableProcessors());
        workerGroup = new NioEventLoopGroup(
                workerGroupSize != 0 ? workerGroupSize : Runtime.getRuntime().availableProcessors());
        startTransport();
    }

    private void shutdownEventLoops() {
        try {
            bossGroup.shutdownGracefully().sync();
            workerGroup.shutdownGracefully().sync();

            log.info("HTTP transport " + id + " on port " + defaultListenerConfig.getPort() +
                    " stopped successfully");
        } catch (InterruptedException e) {
            log.error("HTTP transport " + id + " on port " + defaultListenerConfig.getPort() +
                    " could not be stopped successfully " + e.getMessage());
        }
    }

    @Override
    public void setMessageProcessor(CarbonMessageProcessor carbonMessageProcessor) {

    }

    @Override
    public boolean bind(String interfaceId) {
        try {
            ListenerConfiguration listenerConfiguration = listenerConfigurationMap.get(interfaceId);
            if (listenerConfiguration != null) {
                String id = listenerConfiguration.getHost() + ":" + listenerConfiguration.getPort();

                SSLConfig sslConfig = listenerConfiguration.getSslConfig();
                if (sslConfig != null) {
                    sslConfigMap.put(id, sslConfig);
                    if (listenerConfiguration.getHost().equals(Constants.DEFAULT_ADDRESS) || listenerConfiguration
                            .getHost().equals(Constants.LOCALHOST) || listenerConfiguration.getHost()
                            .equals(Constants.LOOP_BACK_ADDRESS)) {
                        id = Constants.LOCALHOST + ":" + listenerConfiguration.getPort();
                        sslConfigMap.put(id, sslConfig);
                        id = Constants.LOOP_BACK_ADDRESS + ":" + listenerConfiguration.getPort();
                        sslConfigMap.put(id, sslConfig);
                        id = Constants.DEFAULT_ADDRESS + ":" + listenerConfiguration.getPort();
                        sslConfigMap.put(id, sslConfig);
                    }

                }

                if (!interfaceId.equals(defaultListenerConfig.getId())) {
                    ChannelFuture future = bootstrap.bind(new InetSocketAddress(listenerConfiguration.getHost(),
                            listenerConfiguration.getPort())).sync();
                    if (future.isSuccess()) {
                        channelFutureMap.put(listenerConfiguration.getPort(), future);
                        if (listenerConfiguration.getSslConfig() == null) {
                            log.info("HTTP Interface " + interfaceId + " starting on host  " + listenerConfiguration
                                    .getHost() + " and port " + listenerConfiguration.getPort());
                        } else {
                            log.info("HTTPS Interface " + interfaceId + " starting on host  " + listenerConfiguration
                                    .getHost() + " and port " + listenerConfiguration.getPort());
                        }
                        return true;
                    } else {
                        log.error("Cannot bind port for host " + listenerConfiguration.getHost() + " port "
                                + listenerConfiguration.getPort());
                    }
                } else {
                    log.debug("Interface Id  equals to default interface hence using default interface");
                }
            } else {
                log.error("Cannot find defined Listener interface  for Listener id " + interfaceId);
            }

        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean unBind(String interfaceId) {
        ListenerConfiguration listenerConfiguration = listenerConfigurationMap.get(interfaceId);
        if (listenerConfiguration != null && !defaultListenerConfig.getId().equals(listenerConfiguration.getId())) {
            String id = listenerConfiguration.getHost() + ":" + listenerConfiguration.getPort();
            //Remove cached channels and close them.
            ChannelFuture future = channelFutureMap.remove(listenerConfiguration.getPort());
            if (future != null) {
                if (sslConfigMap.get(id) != null) {
                    sslConfigMap.remove(id);
                }
                future.channel().close();
                if (listenerConfiguration.getSslConfig() == null) {
                    log.info("HTTP Listener stopped on listening interface " + interfaceId + " attached to   host  "
                            + listenerConfiguration.getHost() + " and port " + listenerConfiguration.getPort());
                } else {
                    log.info("HTTPS Listener stopped on listening interface " + interfaceId + " attached to   host  "
                            + listenerConfiguration.getHost() + " and port " + listenerConfiguration.getPort());
                }
                return true;
            }
        }
        return false;
    }

}
