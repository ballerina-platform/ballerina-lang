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

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.kernel.transports.CarbonTransport;
import org.wso2.carbon.messaging.TransportSender;
import org.wso2.carbon.messaging.handler.HandlerExecutor;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.SenderConfiguration;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.config.YAMLTransportConfigurationBuilder;
import org.wso2.carbon.transport.http.netty.listener.NettyListener;
import org.wso2.carbon.transport.http.netty.sender.NettySender;

import java.util.HashSet;
import java.util.Set;

/**
 * OSGi BundleActivator of the Netty transport component.
 */
public class NettyTransportActivator implements BundleActivator {

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        for (NettyListener listener : createNettyListeners()) {
            bundleContext.registerService(CarbonTransport.class, listener, null);
        }
        for (NettySender sender : createNettySenders()) {
            bundleContext.registerService(TransportSender.class, sender, null);
        }
        NettyTransportContextHolder.getInstance().setBundleContext(bundleContext);
        HandlerExecutor handlerExecutor = new HandlerExecutor();
        NettyTransportContextHolder.getInstance().setHandlerExecutor(handlerExecutor);
    }

    /**
     * Parse the  netty-transports.xml config file & create the Netty transport instances.
     *
     * @return Netty transport instances
     */
    private Set<NettyListener> createNettyListeners() {
        Set<NettyListener> listeners = new HashSet<>();
        TransportsConfiguration trpConfig = YAMLTransportConfigurationBuilder.build();
        Set<ListenerConfiguration> listenerConfigurations = trpConfig.getListenerConfigurations();
        for (ListenerConfiguration listenerConfiguration : listenerConfigurations) {
            NettyTransportContextHolder.getInstance()
                    .setListenerConfiguration(listenerConfiguration.getId(), listenerConfiguration);
            listeners.add(new NettyListener(listenerConfiguration));
        }
        return listeners;
    }

    /**
     * Parse the  netty-transports.xml config file & create the Netty transport instances.
     *
     * @return Netty transport instances
     */
    private Set<NettySender> createNettySenders() {
        Set<NettySender> senders = new HashSet<>();
        Set<SenderConfiguration> senderConfigurations = YAMLTransportConfigurationBuilder.build()
                .getSenderConfigurations();
        for (SenderConfiguration senderConfiguration : senderConfigurations) {
            senders.add(new NettySender(senderConfiguration));
        }
        return senders;
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {

    }
}
