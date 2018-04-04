/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerina.testing.extension.noop;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;

import java.util.Collections;
import java.util.Map;

/**
 * A no-op implementation of a SpanBuilder.
 */
public interface NoopSpanBuilder extends Tracer.SpanBuilder, NoopSpanContext {
    NoopSpanBuilder INSTANCE = new NoopSpanBuilderImpl();
}

final class NoopSpanBuilderImpl implements NoopSpanBuilder {

    @Override
    public Tracer.SpanBuilder addReference(String refType, SpanContext referenced) {
        return this;
    }

    @Override
    public Tracer.SpanBuilder asChildOf(SpanContext parent) {
        return this;
    }

    @Override
    public Tracer.SpanBuilder ignoreActiveSpan() {
        return this;
    }

    @Override
    public Tracer.SpanBuilder asChildOf(Span parent) {
        return this;
    }

    @Override
    public Tracer.SpanBuilder withTag(String key, String value) {
        return this;
    }

    @Override
    public Tracer.SpanBuilder withTag(String key, boolean value) {
        return this;
    }

    @Override
    public Tracer.SpanBuilder withTag(String key, Number value) {
        return this;
    }

    @Override
    public Tracer.SpanBuilder withStartTimestamp(long microseconds) {
        return this;
    }

    @Override
    public Scope startActive(boolean finishOnClose) {
        return NoopScopeManager.NoopScope.INSTANCE;
    }

    @Override
    public Span start() {
        return startManual();
    }

    @Override
    public Span startManual() {
        return NoopSpanImpl.INSTANCE;
    }

    @Override
    public Iterable<Map.Entry<String, String>> baggageItems() {
        return Collections.<String, String>emptyMap().entrySet();
    }

    @Override
    public String toString() {
        return NoopSpanBuilder.class.getSimpleName();
    }
}
