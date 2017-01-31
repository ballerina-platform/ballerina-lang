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
import org.wso2.carbon.messaging.TransportListener;
import org.wso2.carbon.messaging.TransportSender;
import org.wso2.carbon.messaging.handler.HandlerExecutor;
import org.wso2.carbon.transport.http.netty.config.ListenerConfiguration;
import org.wso2.carbon.transport.http.netty.config.SenderConfiguration;
import org.wso2.carbon.transport.http.netty.config.TransportProperty;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.config.YAMLTransportConfigurationBuilder;
import org.wso2.carbon.transport.http.netty.listener.HTTPTransportListener;
import org.wso2.carbon.transport.http.netty.sender.HTTPSender;

import java.util.Collections;
import java.util.Set;

/**
 * OSGi BundleActivator of the Netty transport component.
 */
public class HTTPTransportActivator implements BundleActivator {

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        registerTransport(bundleContext);
        bundleContext.registerService(TransportSender.class, createClientBootstrapper(), null);
        HTTPTransportContextHolder.getInstance().setBundleContext(bundleContext);
        HandlerExecutor handlerExecutor = new HandlerExecutor();
        HTTPTransportContextHolder.getInstance().setHandlerExecutor(handlerExecutor);
    }

    /**
     * Parse the  netty-transports.xml config file & create the Netty transport instances.
     *
     * @return Netty transport instances
     */
    private void registerTransport(BundleContext bundleContext) {
        TransportsConfiguration trpConfig = YAMLTransportConfigurationBuilder.build();
        Set<ListenerConfiguration> listenerConfigurations = trpConfig.getListenerConfigurations();
        Set<TransportProperty> transportProperties = trpConfig.getTransportProperties();
        listenerConfigurations.forEach(listenerConfiguration -> {
            HTTPTransportContextHolder.getInstance()
                                      .setListenerConfiguration(listenerConfiguration.getId(), listenerConfiguration);
            HTTPTransportListener httpTransportListener =
                    new HTTPTransportListener(transportProperties, Collections.singleton(listenerConfiguration));
            bundleContext.registerService(TransportListener.class, httpTransportListener, null);
        });
    }

    /**
     * Parse the  netty-transports.xml config file & create the Netty transport instances.
     *
     * @return Netty transport instances
     */
    private HTTPSender createClientBootstrapper() {
        TransportsConfiguration trpConfig = YAMLTransportConfigurationBuilder.build();
        Set<SenderConfiguration> senderConfigurations = trpConfig.getSenderConfigurations();
        Set<TransportProperty> transportProperties = trpConfig.getTransportProperties();
        HTTPSender sender = new HTTPSender(senderConfigurations, transportProperties);
        return sender;
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {

    }
}
