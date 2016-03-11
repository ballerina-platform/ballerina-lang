/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.transport.http.netty.statistics.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.messaging.handler.MessagingHandler;
import org.wso2.carbon.transport.http.netty.config.TransportProperty;
import org.wso2.carbon.transport.http.netty.config.YAMLTransportConfigurationBuilder;
import org.wso2.carbon.transport.http.netty.statistics.MetricReporter;
import org.wso2.carbon.transport.http.netty.statistics.Metrics;
import org.wso2.carbon.transport.http.netty.statistics.StatisticsHandler;
import org.wso2.carbon.transport.http.netty.statistics.TimerHolder;

import java.util.Set;

/**
 * OSGi BundleActivator of the Netty transport component.
 */
public class NettyTransportStatActivator implements BundleActivator {

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        if (getMetricsStatus()) {
            Metrics.init(MetricReporter.JMX);
            bundleContext
                    .registerService(MessagingHandler.class, new StatisticsHandler(TimerHolder.getInstance()), null);
        }
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {

    }

    private boolean getMetricsStatus() {
        boolean statStatus = false;

        Set<TransportProperty> transportProperties = YAMLTransportConfigurationBuilder.build().getTransportProperties();
        for (TransportProperty property : transportProperties) {
            if (property.getName().equalsIgnoreCase("latency.metrics.enabled")) {
                statStatus = (Boolean) property.getValue();
            }
        }

        return statStatus;
    }
}
