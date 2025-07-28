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
package org.ballerinalang.jvm.observability.tracer.noop;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.propagation.ContextPropagators;
import org.ballerinalang.jvm.observability.tracer.spi.TracerProvider;

/**
 * Implementation of No-Op {@link TracerProvider}.
 */
public class NoOpTracerProvider implements TracerProvider {
    public static final String NAME = "noop";

    private Tracer instance;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void init() {
        instance = io.opentelemetry.api.trace.TracerProvider.noop().get("");
    }

    @Override
    public Tracer getTracer(String serviceName) {
        return instance;
    }

    @Override
    public ContextPropagators getPropagators() {
        return ContextPropagators.noop();
    }
}
