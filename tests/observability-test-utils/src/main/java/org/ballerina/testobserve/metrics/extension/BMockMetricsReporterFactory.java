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
package org.ballerina.testobserve.metrics.extension;

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.observability.metrics.spi.MetricReporterFactory;

import static org.ballerina.testobserve.Constants.OBSERVE_MODULE;

/**
 * Mock metrics reporter.
 *
 * This does not do anything and is only created as a reporter type is required by Ballerina Observability configs.
 * This is mainly used in test cases. The metrics are read by writing an API which exposes the metrics from the
 * Metrics Registry.
 */
public class BMockMetricsReporterFactory implements MetricReporterFactory {
    private static final BObject BOBJECT_INSTANCE =
            ValueCreator.createObjectValue(OBSERVE_MODULE, "MockMetricReporter");

    @Override
    public String getName() {
        return "mock";
    }

    @Override
    public BObject getReporterBObject() {
        return BOBJECT_INSTANCE;
    }
}
