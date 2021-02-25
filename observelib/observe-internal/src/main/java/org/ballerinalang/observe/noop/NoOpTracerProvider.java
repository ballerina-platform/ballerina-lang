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
package org.ballerinalang.observe.noop;

import io.ballerina.runtime.observability.tracer.spi.TracerProvider;
import io.opentracing.Tracer;
import io.opentracing.noop.NoopTracerFactory;

/**
 * Implementation of No-Op {@link TracerProvider}.
 */
public class NoOpTracerProvider implements TracerProvider {
    private Tracer instance;

    @Override
    public String getName() {
        return "noop";
    }

    @Override
    public void init() {
        instance = NoopTracerFactory.create();
    }

    @Override
    public Tracer getTracer(String serviceName) {
        return instance;
    }
}
