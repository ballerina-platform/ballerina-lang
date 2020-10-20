/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.ballerina.runtime.observability.metrics.spi;

import io.ballerina.runtime.observability.tracer.InvalidConfigurationException;

/**
 * Metrics Reporter interface that needs to be implemented by external metric reporters from Ballerina.
 *
 * @since 0.980.0
 */
public interface MetricReporter {

    /**
     * Initializes the {@link MetricReporter} implementation with configurations.
     *
     * @throws InvalidConfigurationException if the configurations are invalid.
     */
    void init() throws InvalidConfigurationException;

    /**
     * Returns the name of the tracer. This will be used when loading the tracer by name.
     *
     * @return tracer name.
     */
    String getName();

}
