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

package io.ballerina.runtime.observability.tracer;

import io.ballerina.runtime.observability.ObserverContext;
import io.opentracing.Span;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.ballerina.runtime.observability.tracer.TraceConstants.DEFAULT_OPERATION_NAME;
import static io.ballerina.runtime.observability.tracer.TraceConstants.DEFAULT_SERVICE_NAME;
import static io.ballerina.runtime.observability.tracer.TraceConstants.KEY_SPAN;
import static io.ballerina.runtime.observability.tracer.TraceConstants.TAG_KEY_STR_ERROR;
import static io.ballerina.runtime.observability.tracer.TraceConstants.TAG_STR_TRUE;

/**
 * {@code BSpan} holds the trace of the current context.
 *
 * @since 0.964.1
 */
public class BSpan {

    private static final TraceManager manager = TraceManager.getInstance();

    /**
     * {@link Map} of properties, which used to represent
     * the span contexts of each tracer.
     */
    private final Map<String, String> properties;
    /**
     * {@link Map} of tags, which will get injected to span.
     */
    private final Map<String, String> tags;
    /**
     * Name of the service.
     */
    private String serviceName = DEFAULT_SERVICE_NAME;
    /**
     * Name of the operation.
     */
    private String operationName = DEFAULT_OPERATION_NAME;
    /**
     * Active Ballerina {@link ObserverContext}.
     */
    private final ObserverContext observerContext;
    /**
     * Open tracer specific span.
     */
    private Span span;

    public BSpan(ObserverContext observerContext, boolean isClientContext) {
        this.properties = new HashMap<>();
        this.tags = new HashMap<>();
        this.observerContext = observerContext;
        this.tags.put(TraceConstants.TAG_KEY_SPAN_KIND, isClientContext
                ? TraceConstants.TAG_SPAN_KIND_CLIENT
                : TraceConstants.TAG_SPAN_KIND_SERVER);
    }

    public void startSpan() {
        manager.startSpan(getParentBSpan(), this);
    }

    public void finishSpan() {
        manager.finishSpan(this);
    }

    public void log(Map<String, Object> fields) {
        manager.log(this, fields);
    }

    public void logError(Map<String, Object> fields) {
        addTags(Collections.singletonMap(TAG_KEY_STR_ERROR, TAG_STR_TRUE));
        manager.log(this, fields);

    }

    public void addTags(Map<String, String> tags) {
        if (span != null) {
            //span has started, therefore add tags to the span.
            manager.addTags(this, tags);
        } else {
            //otherwise keep the tags in a map, and add it once
            //the span get created.
            this.tags.putAll(tags);
        }

    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void addProperty(String key, String value) {
        if (properties != null) {
            properties.put(key, value);
        }
    }

    public String getProperty(String key) {
        if (properties != null) {
            return properties.get(key);
        }
        return null;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public Span getSpan() {
        return span;
    }

    public void setSpan(Span span) {
        this.span = span;
    }

    public Map<String, String> getTraceContext() {
        return manager.extractTraceContext(span, serviceName);
    }

    private BSpan getParentBSpan() {
        if (observerContext.getParent() != null) {
            return (BSpan) observerContext.getParent().getProperty(KEY_SPAN);
        }
        return null;
    }
}
