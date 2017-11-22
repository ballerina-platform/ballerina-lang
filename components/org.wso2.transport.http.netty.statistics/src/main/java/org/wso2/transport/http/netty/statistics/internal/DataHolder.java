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

import org.wso2.carbon.metrics.core.MetricManagementService;
import org.wso2.carbon.metrics.core.MetricService;

/**
 * DataHolder to hold OSGi services
 */
public class DataHolder {

    private static final DataHolder instance = new DataHolder();

    private MetricService metricService;

    private MetricManagementService metricManagementService;

    private DataHolder() {
    }

    /**
     * This returns the DataHolder instance.
     *
     * @return The DataHolder instance of this singleton class
     */
    public static DataHolder getInstance() {
        return instance;
    }

    /**
     * Returns the {@link MetricService}, which is set by the service component
     *
     * @return The {@link MetricService} instance
     */
    public MetricService getMetricService() {
        return metricService;
    }

    /**
     * Set the {@link MetricService} when the service component gets a reference to the {@link MetricService} instance.
     *
     * @param metricService The {@link MetricService} reference being passed through the service component
     */
    public void setMetricService(MetricService metricService) {
        this.metricService = metricService;
    }

    /**
     * Returns the {@link MetricManagementService}, which is set by the service component
     *
     * @return The {@link MetricManagementService} instance
     */
    public MetricManagementService getMetricManagementService() {
        return metricManagementService;
    }

    /**
     * Set the {@link MetricManagementService} when the service component gets a reference to the
     * {@link MetricManagementService} instance.
     *
     * @param metricManagementService The {@link MetricManagementService} reference being passed through the service
     *                                component
     */
    public void setMetricManagementService(MetricManagementService metricManagementService) {
        this.metricManagementService = metricManagementService;
    }
}
