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
 *
 */

package org.ballerinalang.nativeimpl.observe.tracing;

import io.opentracing.Span;

import java.util.UUID;

/**
 * Class that holds an instance of opentracing span.
 */
public class BSpan {

    private String invocationId;

    private String spanId;

    private Span span;

    private BSpan parentSpan;

    public BSpan(String invocationId, Span span, BSpan parentSpan) {
        this.invocationId = invocationId;
        this.spanId = UUID.randomUUID().toString();
        this.span = span;
        this.parentSpan = parentSpan;
    }

    public Span getSpan() {
        return span;
    }

    public void setSpan(Span span) {
        this.span = span;
    }

    public BSpan getParentSpan() {
        return parentSpan;
    }

    public String getSpanId() {
        return spanId;
    }

    public String getInvocationId() {
        return invocationId;
    }

}
