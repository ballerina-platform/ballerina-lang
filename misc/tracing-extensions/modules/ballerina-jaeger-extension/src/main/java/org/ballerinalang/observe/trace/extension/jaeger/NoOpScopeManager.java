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

package org.ballerinalang.observe.trace.extension.jaeger;

import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;
import io.opentracing.SpanContext;

import java.util.Collections;
import java.util.Map;

/**
 * NoOp implementation of Scope Manager.
 */
public class NoOpScopeManager implements ScopeManager {

    private NoOpScopeManager() {
    }

    static final NoOpScopeManager INSTANCE = new NoOpScopeManager();

    @Override
    public Scope activate(Span span, boolean finishSpanOnClose) {
        return NoOpScope.INSTANCE;
    }

    @Override
    public Scope active() {
        return null;
    }

    static class NoOpScope implements Scope {

        private NoOpScope() {
        }

        static final NoOpScope INSTANCE = new NoOpScope();

        @Override
        public void close() {

        }

        @Override
        public Span span() {
            return NoOpSpan.INSTANCE;
        }
    }

    static class NoOpSpan implements Span {

        private NoOpSpan() {
        }

        static final NoOpSpan INSTANCE = new NoOpSpan();

        @Override
        public SpanContext context() {
            return NoOpSpanContext.INSTANCE;
        }

        @Override
        public void finish() {
        }

        @Override
        public void finish(long finishMicros) {
        }

        @Override
        public NoOpSpan setTag(String key, String value) {
            return this;
        }

        @Override
        public NoOpSpan setTag(String key, boolean value) {
            return this;
        }

        @Override
        public NoOpSpan setTag(String key, Number value) {
            return this;
        }

        @Override
        public NoOpSpan log(Map<String, ?> fields) {
            return this;
        }

        @Override
        public NoOpSpan log(long timestampMicroseconds, Map<String, ?> fields) {
            return this;
        }

        @Override
        public NoOpSpan log(String event) {
            return this;
        }

        @Override
        public NoOpSpan log(long timestampMicroseconds, String event) {
            return this;
        }

        @Override
        public NoOpSpan setBaggageItem(String key, String value) {
            return this;
        }

        @Override
        public String getBaggageItem(String key) {
            return null;
        }

        @Override
        public NoOpSpan setOperationName(String operationName) {
            return this;
        }

        @Override
        public String toString() {
            return NoOpSpan.class.getSimpleName();
        }
    }

    static class NoOpSpanContext implements SpanContext {

        private NoOpSpanContext() {
        }

        static final NoOpSpanContext INSTANCE = new NoOpSpanContext();

        @Override
        public Iterable<Map.Entry<String, String>> baggageItems() {
            return Collections.emptyList();
        }
    }
}
