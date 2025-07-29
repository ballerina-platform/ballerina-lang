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

package org.ballerina.testobserve.extension;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import org.ballerinalang.jvm.observability.tracer.spi.TracerProvider;

import java.io.PrintStream;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracer provider factory which returns mock tracer provider instance.
 */
public class BMockTracerProvider implements TracerProvider {
    private static final PrintStream out = System.out;
    private static final Map<String, InMemorySpanExporter> exporterMap = new ConcurrentHashMap<>();

    @Override
    public String getName() {
        return "mock";
    }

    @Override
    public void init() {    // Do Nothing
    }

    public static Map<String, InMemorySpanExporter> getExporterMap() {
        return Collections.unmodifiableMap(exporterMap);
    }

    @Override
    public Tracer getTracer(String serviceName) {
        InMemorySpanExporter inMemorySpanExporter = InMemorySpanExporter.create();
        exporterMap.put(serviceName, inMemorySpanExporter);
        out.println("Initialized Mock Tracer for " + serviceName);
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(SimpleSpanProcessor.create(inMemorySpanExporter))
                .build();
        return tracerProvider.get(serviceName);
    }

    @Override
    public ContextPropagators getPropagators() {
        return ContextPropagators.noop();
    }
}
