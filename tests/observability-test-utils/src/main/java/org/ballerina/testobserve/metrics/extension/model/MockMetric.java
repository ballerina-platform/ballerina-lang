/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerina.testobserve.metrics.extension.model;

import io.ballerina.runtime.observability.metrics.MetricId;

/**
 * Abstract class containing the common data of a metric.
 */
public abstract class MockMetric {
    private MetricId id;

    public MetricId getId() {
        return id;
    }

    public void setId(MetricId id) {
        this.id = id;
    }
}
