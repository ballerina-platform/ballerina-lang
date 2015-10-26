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

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.listener.CarbonNettyServerInitializer;

import java.util.Map;

/**
 * Declarative service component for the Netty transport. This handles registration & unregistration of relevant
 * OSGi services.
 */
@Component(
        name = "org.wso2.carbon.transport.http.netty.internal.NettyTransportServiceComponent",
        immediate = true
)
@SuppressWarnings("unused")
public class NettyTransportServiceComponent {

    private static final Logger log = LoggerFactory.getLogger(NettyTransportServiceComponent.class);

    private static final String CHANNEL_ID_KEY = "channel.id";

    private NettyTransportDataHolder dataHolder = NettyTransportDataHolder.getInstance();

    @Reference(
            name = "netty-channel.initializer",
            service = CarbonNettyServerInitializer.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeNettyChannelInitializer"
    )
    protected void addNettyChannelInitializer(CarbonNettyServerInitializer initializer, Map<String, ?> properties) {
        try {
            String channelId = (String) properties.get(CHANNEL_ID_KEY);
            if (channelId != null) {
                dataHolder.addNettyChannelInitializer(channelId, initializer);
            } else {
                throw new IllegalArgumentException(CHANNEL_ID_KEY + " not specified for ChannelInitializer " +
                        initializer);
            }
        } catch (Throwable e) {
            log.error("Cannot add CarbonNettyServerInitializer", e);
        }
    }

    @SuppressWarnings("unused")
    protected void removeNettyChannelInitializer(CarbonNettyServerInitializer initializer, Map<String, ?> properties) {
        String channelId = (String) properties.get(CHANNEL_ID_KEY);
        dataHolder.removeNettyChannelInitializer(channelId);
    }
}
