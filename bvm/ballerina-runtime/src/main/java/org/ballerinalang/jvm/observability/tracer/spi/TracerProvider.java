/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.ballerinalang.jvm.observability.tracer.spi;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.propagation.ContextPropagators;
import org.ballerinalang.jvm.observability.InvalidConfigurationException;

/**
 * This returns a tracer for a service
 * for the Ballerina Observability Runtime.
 */
public interface TracerProvider {

    /**
     * Returns a unique name for the Tracers provided.
     *
     * @return The name of the provider
     */
    String getName();

    /**
     * This will be called when initializing {@link TracerProvider}.
     *
     * @throws InvalidConfigurationException if the configurations are invalid.
     */
    void init() throws InvalidConfigurationException;

    /**
     * Get a tracer implementing an OpenTracer.
     *
     * @param serviceName the name of the service for which the tracer should be generated
     * @return the Tracer
     */
    Tracer getTracer(String serviceName);

    ContextPropagators getPropagators();
}
