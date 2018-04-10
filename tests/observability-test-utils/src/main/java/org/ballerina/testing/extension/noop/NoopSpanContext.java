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

import io.opentracing.SpanContext;

import java.util.Collections;
import java.util.Map;

/**
 * A no-op implementation of a SpanContext.
 */
public interface NoopSpanContext extends SpanContext {
}

final class NoopSpanContextImpl implements NoopSpanContext {
    static final NoopSpanContextImpl INSTANCE = new NoopSpanContextImpl();

    @Override
    public Iterable<Map.Entry<String, String>> baggageItems() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return NoopSpanContext.class.getSimpleName();
    }
}

