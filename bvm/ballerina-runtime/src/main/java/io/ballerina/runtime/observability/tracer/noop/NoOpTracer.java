/*
 * Copyright (c) 2026, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime.observability.tracer.noop;

import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;

/**
 * A {@link Tracer} used with the {@link NoOpTracerProvider}.
 * <p>
 * Unlike OpenTelemetry's built-in no-op tracer (obtained via
 * {@code io.opentelemetry.api.trace.TracerProvider.noop()}), which always returns
 * {@code Span.getInvalid()} with all-zero trace and span identifiers, spans created by this
 * tracer carry real, randomly generated identifiers. This allows the trace id to still be used
 * as a request correlation id (e.g. in logs) even when no tracing backend is configured, while
 * remaining lightweight since nothing is sampled, recorded or exported.
 *
 * @since 2201.14.0
 */
public class NoOpTracer implements Tracer {

    @Override
    public SpanBuilder spanBuilder(String spanName) {
        return new NoOpSpanBuilder();
    }
}
