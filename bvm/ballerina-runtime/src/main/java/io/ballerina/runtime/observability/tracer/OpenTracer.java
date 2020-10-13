/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime.observability.tracer;

import io.opentracing.Tracer;

/**
 * This represents the Java SPI interface that OpenTracerManager will be using
 * to obtain the {@link Tracer} implementation.
 */
public interface OpenTracer {

    /**
     * Initializes the {@link Tracer} implementation with configurations.
     *
     * @throws InvalidConfigurationException if the configurations are invalid.
     */
    void init() throws InvalidConfigurationException;

    /**
     * Returns the specific tracer implementation of the analytics engine based
     * on the configuration provided.
     *
     * @param serviceName name of the service of the trace
     * @return Specific {@link Tracer} instance
     * if the configuration or tracer name is invalid.
     */
    Tracer getTracer(String serviceName);

    /**
     * Returns the name of the tracer. This will be used when loading the tracer by name.
     *
     * @return tracer name.
     */
    String getName();
}
