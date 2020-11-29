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
package io.ballerina.runtime.observability.tracer.spi;

import io.ballerina.runtime.api.values.BObject;
import io.opentracing.Tracer;

/**
 * This represents the Java SPI interface that OpenTracerManager will be used
 * to obtain the {@link Tracer} implementation.
 */
public interface TracerProviderFactory {

    /**
     * Returns the name of the Tracer Provider produced by this factory.
     *
     * @return The name of the provider
     */
    String getName();

    /**
     * Returns the TracerProvider Ballerina Object.
     *
     * @return Specific {@link Tracer} instance
     */
    BObject getProviderBObject();

    /**
     * Returns an implementation of {@link TracerProvider} interface.
     *
     * @return the tracer provider
     */
    TracerProvider getProvider();
}
