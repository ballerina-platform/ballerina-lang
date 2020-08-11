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
package org.wso2.transport.http.netty.internal;

import io.netty.channel.EventLoopGroup;
import org.osgi.framework.BundleContext;
import org.wso2.transport.http.netty.contract.config.ListenerConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * DataHolder for the HTTP transport.
 */
public class HttpTransportContextHolder {

    private static HttpTransportContextHolder instance = new HttpTransportContextHolder();
    private BundleContext bundleContext;
    private HandlerExecutor handlerExecutor;
    private Map<String, ListenerConfiguration> listenerConfigurations = new HashMap<>();
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    public EventLoopGroup getBossGroup() {
        return bossGroup;
    }

    public void setBossGroup(EventLoopGroup bossGroup) {
        this.bossGroup = bossGroup;
    }

    public EventLoopGroup getWorkerGroup() {
        return workerGroup;
    }

    public void setWorkerGroup(EventLoopGroup workerGroup) {
        this.workerGroup = workerGroup;
    }

    public ListenerConfiguration getListenerConfiguration(String id) {
        return listenerConfigurations.get(id);
    }

    public void setListenerConfiguration(String id, ListenerConfiguration config) {
        listenerConfigurations.put(id, config);
    }

    private HttpTransportContextHolder() {

    }

    public static HttpTransportContextHolder getInstance() {
        return instance;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public BundleContext getBundleContext() {
        return this.bundleContext;
    }

    public void setHandlerExecutor(HandlerExecutor handlerExecutor) {
        this.handlerExecutor = handlerExecutor;
    }

    public HandlerExecutor getHandlerExecutor() {
        return handlerExecutor;
    }
}
