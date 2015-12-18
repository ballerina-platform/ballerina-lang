/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.transport.http.netty.internal;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.CarbonTransportInitializer;
import org.wso2.carbon.messaging.TransportListener;
import org.wso2.carbon.transport.http.netty.listener.CarbonNettyServerInitializer;
import org.wso2.carbon.transport.http.netty.sender.CarbonNettyClientInitializer;

import java.util.HashMap;
import java.util.Map;

/**
 * DataHolder for the Netty transport.
 */
public class NettyTransportDataHolder {
    private static final Logger log = LoggerFactory.getLogger(NettyTransportDataHolder.class);

    private static NettyTransportDataHolder instance = new NettyTransportDataHolder();
    private Map<String, CarbonTransportInitializer> channelServerInitializers = new HashMap<>();
    private Map<String, CarbonTransportInitializer> channelClientInitializers = new HashMap<>();
    private Map<String, TransportListener> transportListeners = new HashMap<>();
    private BundleContext bundleContext;
    private CarbonMessageProcessor engine;

    private NettyTransportDataHolder() {

    }

    public static NettyTransportDataHolder getInstance() {
        return instance;
    }

    public synchronized void addNettyChannelInitializer(String key, CarbonTransportInitializer initializer) {
        if (initializer.isServerInitializer()) {
            if (channelServerInitializers.get(key) == null) {
                this.channelServerInitializers.put(key, initializer);
            } else {
                if (channelServerInitializers.get(key) instanceof CarbonNettyServerInitializer) {
                    channelServerInitializers.remove(key);
                    this.channelServerInitializers.put(key, initializer);
                }
            }
        } else {
            if (channelClientInitializers.get(key) == null) {
                this.channelClientInitializers.put(key, initializer);
            } else {
                if (channelClientInitializers.get(key) instanceof CarbonNettyClientInitializer) {
                    channelClientInitializers.remove(key);
                    this.channelClientInitializers.put(key, initializer);
                }
            }
        }
    }


    public CarbonTransportInitializer getServerChannelInitializer(String key) {
        return channelServerInitializers.get(key);
    }

    public CarbonTransportInitializer getClientChannelInitializer(String key) {
        return channelClientInitializers.get(key);
    }


    public void removeNettyChannelInitializer(String key) {
        channelServerInitializers.remove(key);
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public BundleContext getBundleContext() {
        return this.bundleContext;
    }

    public CarbonMessageProcessor getEngine() {
        return engine;
    }

    public void setEngine(CarbonMessageProcessor engine) {
        this.engine = engine;
    }


    public void addMessageProcessor(CarbonMessageProcessor carbonMessageProcessor) {
        if (!transportListeners.isEmpty()) {
            transportListeners.forEach((k, v) -> v.setEngine(carbonMessageProcessor));
        }
    }

    public void removeMessageProcessor(CarbonMessageProcessor carbonMessageProcessor) {
        if (carbonMessageProcessor.getId().equals(engine.getId())) {
            engine = null;
        }
    }

    public void addTransportListener(TransportListener transportListener) {
        if (engine != null) {
            transportListener.setEngine(engine);
        }
        transportListeners.put(transportListener.getId(), transportListener);
    }
}
