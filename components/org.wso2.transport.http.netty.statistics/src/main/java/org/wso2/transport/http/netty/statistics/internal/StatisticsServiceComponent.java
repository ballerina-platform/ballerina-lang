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

package org.wso2.transport.http.netty.statistics.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.messaging.handler.MessagingHandler;
import org.wso2.carbon.metrics.core.MetricManagementService;
import org.wso2.carbon.metrics.core.MetricService;
import org.wso2.transport.http.netty.contract.config.ConfigurationBuilder;
import org.wso2.transport.http.netty.contract.config.TransportProperty;
import org.wso2.transport.http.netty.statistics.StatisticsHandler;
import org.wso2.transport.http.netty.statistics.TimerHolder;

import java.util.Set;

/**
 * Service component to refer metrics services
 */
@Component(
        name = "org.wso2.carbon.transport.http.netty.statistics.internal.StatisticsServiceComponent",
        immediate = true
)
public class StatisticsServiceComponent {
    /**
     * This bind method will be called when {@link MetricService} is registered.
     *
     * @param metricService The {@link MetricService} instance registered as an OSGi service
     */
    @Reference(
            name = "carbon.metrics.service",
            service = MetricService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetMetricService"
    )
    protected void setMetricService(MetricService metricService) {
        DataHolder.getInstance().setMetricService(metricService);
    }

    /**
     * This is the unbind method which gets called at the un-registration of {@link MetricService}
     *
     * @param metricService The {@link MetricService} instance registered as an OSGi service
     */
    protected void unsetMetricService(MetricService metricService) {
        DataHolder.getInstance().setMetricService(null);
    }

    /**
     * This bind method will be called when {@link MetricManagementService} is registered.
     *
     * @param metricManagementService The {@link MetricManagementService} instance registered as an OSGi service
     */
    @Reference(
            name = "carbon.metrics.management.service",
            service = MetricManagementService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetMetricManagementService"
    )
    protected void setMetricManagementService(MetricManagementService metricManagementService) {
        DataHolder.getInstance().setMetricManagementService(metricManagementService);
    }

    /**
     * This is the unbind method which gets called at the un-registration of {@link MetricManagementService}
     *
     * @param metricManagementService The {@link MetricManagementService} instance registered as an OSGi service
     */
    protected void unsetMetricManagementService(MetricManagementService metricManagementService) {
        DataHolder.getInstance().setMetricManagementService(null);
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        if (getMetricsStatus()) {
            bundleContext
                    .registerService(MessagingHandler.class, new StatisticsHandler(TimerHolder.getInstance()), null);
        }
    }

    @Deactivate
    public void deactivate(BundleContext bundleContext) {

    }

    private boolean getMetricsStatus() {
        boolean statStatus = false;

        Set<TransportProperty> transportProperties =
                ConfigurationBuilder.getInstance().getConfiguration().getTransportProperties();
        for (TransportProperty property : transportProperties) {
            if (property.getName().equalsIgnoreCase("latency.metrics.enabled")) {
                statStatus = (Boolean) property.getValue();
            }
        }

        return statStatus;
    }

}
