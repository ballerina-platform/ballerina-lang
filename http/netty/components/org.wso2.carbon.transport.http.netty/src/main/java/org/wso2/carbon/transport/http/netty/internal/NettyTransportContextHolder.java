/*
 *  Copyright (c) 2015 WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
import org.wso2.carbon.messaging.Interceptor;
import org.wso2.carbon.transport.http.netty.internal.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.listener.CarbonNettyServerInitializer;

import java.util.HashMap;
import java.util.Map;

/**
 * DataHolder for the Netty transport.
 */
public class NettyTransportContextHolder {
    private static final Logger log = LoggerFactory.getLogger(NettyTransportContextHolder.class);

    private static NettyTransportContextHolder instance = new NettyTransportContextHolder();
    private Map<String, CarbonTransportInitializer> channelServerInitializers = new HashMap<>();
    private Map<String, CarbonTransportInitializer> channelClientInitializers = new HashMap<>();
    private BundleContext bundleContext;
    private CarbonMessageProcessor messageProcessor;
    private Interceptor interceptor;
    private Map<String, ListenerConfiguration> listenerConfigurations = new HashMap<>();

    public ListenerConfiguration getListenerConfiguration(String id) {
        return listenerConfigurations.get(id);
    }

    public void setListenerConfiguration(String id, ListenerConfiguration config) {
        listenerConfigurations.put(id, config);
    }

    private NettyTransportContextHolder() {

    }

    public static NettyTransportContextHolder getInstance() {
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

    public CarbonMessageProcessor getMessageProcessor() {
        return messageProcessor;
    }

    public void addMessageProcessor(CarbonMessageProcessor carbonMessageProcessor) {
        this.messageProcessor = carbonMessageProcessor;
    }

    public void removeMessageProcessor(CarbonMessageProcessor carbonMessageProcessor) {
        if (carbonMessageProcessor.getId().equals(messageProcessor.getId())) {
            messageProcessor = null;
        }
    }

    public void setInterceptor(Interceptor interceptor) {
        this.interceptor = interceptor;
    }

    public Interceptor getInterceptor() {
        return interceptor;
    }
}
