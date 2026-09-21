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

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * A {@link SpanBuilder} used by {@link NoOpTracer} that produces spans carrying randomly
 * generated, valid trace and span identifiers, so that they remain usable (e.g. as log
 * correlation ids) even though nothing is recorded, sampled or exported. A child span reuses
 * the trace id of its parent, when one is present, so that all spans within a single request
 * share the same trace id.
 *
 * @since 2201.14.0
 */
final class NoOpSpanBuilder implements SpanBuilder {

    private static final long INVALID_ID = 0L;

    private Context parentContext;
    private boolean noParent = false;

    @Override
    public SpanBuilder setParent(Context context) {
        this.parentContext = context;
        this.noParent = false;
        return this;
    }

    @Override
    public SpanBuilder setNoParent() {
        this.parentContext = null;
        this.noParent = true;
        return this;
    }

    @Override
    public SpanBuilder addLink(SpanContext spanContext) {
        return this;
    }

    @Override
    public SpanBuilder addLink(SpanContext spanContext, Attributes attributes) {
        return this;
    }

    @Override
    public SpanBuilder setAttribute(String key, String value) {
        return this;
    }

    @Override
    public SpanBuilder setAttribute(String key, long value) {
        return this;
    }

    @Override
    public SpanBuilder setAttribute(String key, double value) {
        return this;
    }

    @Override
    public SpanBuilder setAttribute(String key, boolean value) {
        return this;
    }

    @Override
    public <T> SpanBuilder setAttribute(AttributeKey<T> key, T value) {
        return this;
    }

    @Override
    public SpanBuilder setSpanKind(SpanKind spanKind) {
        return this;
    }

    @Override
    public SpanBuilder setStartTimestamp(long startTimestamp, TimeUnit unit) {
        return this;
    }

    @Override
    public Span startSpan() {
        SpanContext parentSpanContext = noParent ? SpanContext.getInvalid()
                : Span.fromContext(parentContext != null ? parentContext : Context.current()).getSpanContext();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String traceId = parentSpanContext.isValid() ? parentSpanContext.getTraceId() : generateTraceId(random);
        String spanId = generateSpanId(random);
        TraceFlags traceFlags = parentSpanContext.isValid() ? parentSpanContext.getTraceFlags()
                : TraceFlags.getSampled();
        return Span.wrap(SpanContext.create(traceId, spanId, traceFlags, TraceState.getDefault()));
    }

    private static String generateTraceId(ThreadLocalRandom random) {
        long idHi = random.nextLong();
        long idLo;
        do {
            idLo = random.nextLong();
        } while (idLo == INVALID_ID);
        return TraceId.fromLongs(idHi, idLo);
    }

    private static String generateSpanId(ThreadLocalRandom random) {
        long id;
        do {
            id = random.nextLong();
        } while (id == INVALID_ID);
        return SpanId.fromLong(id);
    }
}
