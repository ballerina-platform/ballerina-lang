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
import org.wso2.carbon.transport.http.netty.common.CarbonTransportServerInitializer;
import org.wso2.carbon.transport.http.netty.listener.CarbonNettyServerInitializer;

import java.util.HashMap;
import java.util.Map;

/**
 * DataHolder for the Netty transport.
 */
public class NettyTransportDataHolder {
    private static final Logger log = LoggerFactory.getLogger(NettyTransportDataHolder.class);

    private static NettyTransportDataHolder instance = new NettyTransportDataHolder();
    private Map<String, CarbonNettyServerInitializer> channelInitializers = new HashMap<>();
    private BundleContext bundleContext;
    private CarbonMessageProcessor engine;
    private CarbonTransportServerInitializer carbonNettyInitializer;

    public CarbonTransportServerInitializer getCarbonNettyInitializer() {
        return carbonNettyInitializer;
    }

    public void setCarbonNettyInitializer(CarbonTransportServerInitializer carbonNettyInitializer) {
        this.carbonNettyInitializer = carbonNettyInitializer;
    }

    private NettyTransportDataHolder() {

    }

    public static NettyTransportDataHolder getInstance() {
        return instance;
    }

    public synchronized void addNettyChannelInitializer(String key, CarbonNettyServerInitializer initializer) {
        if (channelInitializers.get(key) == null) {
            this.channelInitializers.put(key, initializer);
        } else {
            log.error("Netty transport listener " + key + " already registered");
        }
    }

    public CarbonNettyServerInitializer getChannelInitializer(String key) {
        return channelInitializers.get(key);
    }


    public void removeNettyChannelInitializer(String key) {
        channelInitializers.remove(key);
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
}
