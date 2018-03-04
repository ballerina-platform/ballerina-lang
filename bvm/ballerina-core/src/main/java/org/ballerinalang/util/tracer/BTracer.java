/*
 * Copyright (c)  2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.util.tracer;

import org.ballerinalang.bre.Context;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * {@code BTracer} holds the current trace context of the program.
 *
 * @since 0.963.1
 */
public class BTracer {
    private static final String FUNCTION_INIT = "<init>";
    private static final String KEY_SPAN_KIND = "span.kind";
    private static final String SPAN_KIND_SERVER = "server";
    private static final String SPAN_KIND_CLIENT = "client";
    private static final Random RANDOM = new Random();
    private static TraceManagerWrapper manager = TraceManagerWrapper.getInstance();

    /**
     * {@link Map} of properties, which used to represent the span contexts of each tracer.
     */
    private Map<String, String> properties;
    /**
     * {@link Map} of tags, which will get injected to spans.
     */
    private Map<String, String> tags;
    /**
     * flag to represent whether this is a client (originate from a client connector) context or a server context.
     */
    private boolean isClientContext;
    /**
     * Name of the span.
     */
    private String spanName = "defaultSpan";
    /**
     * Name of the service.
     */
    private String serviceName = "ballerinaService";
    /**
     * Name of the resource.
     */
    private String resourceName = "ballerinaResource";
    /**
     * Indicates whether this context is traceable or not.
     */
    private boolean isTraceable = true;
    /**
     * Ballerina context.
     */
    private Context bContext = null;
    /**
     * If there's a parent, this should hold parent span context.
     */
    private Map<String, Object> parentSpanContext = null;
    /**
     * List of spans belongs to this tracer.
     */
    private List<Object> spans;

    private BTracer() {

    }

    public BTracer(Context bContext, boolean isClientContext) {
        this.properties = new HashMap<>();
        this.tags = new HashMap<>();
        this.bContext = bContext;
        this.isClientContext = isClientContext;
        this.tags.put(KEY_SPAN_KIND, isClientContext ? SPAN_KIND_CLIENT : SPAN_KIND_SERVER);
        this.isTraceable = !(isClientContext && bContext.getControlStack().getCurrentFrame()
                .getCallableUnitInfo().getName().endsWith(FUNCTION_INIT));
    }

    public void startSpan() {
        manager.startSpan(bContext);
    }

    public void finishSpan() {
        manager.finishSpan(this);
    }

    public void log(Map<String, Object> fields) {
        manager.log(this, fields);
    }

    public void logError(Map<String, Object> fields) {
        addTags(Collections.singletonMap("error", "true"));
        manager.log(this, fields);
    }

    public void addTags(Map<String, String> tags) {
        if (spans != null) {
            //span has started, there for add tags to the span
            manager.addTags(this, tags);
        } else {
            this.tags.putAll(tags);
        }
    }

    public String getSpanName() {
        return spanName;
    }

    public void setSpanName(String spanName) {
        this.spanName = spanName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
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

    public boolean isTraceable() {
        return isTraceable;
    }

    public boolean isClientContext() {
        return isClientContext;
    }

    public String getInvocationID() {
        return getProperty(TraceConstants.TRACE_PREFIX + TraceConstants.INVOCATION_ID);
    }

    public void setInvocationID(String invocationId) {
        addProperty(TraceConstants.TRACE_PREFIX + TraceConstants.INVOCATION_ID, invocationId);
    }

    public void generateInvocationID() {
        setInvocationID(String.valueOf(RANDOM.nextLong()));
    }

    public void setContext(Context bContext) {
        this.bContext = bContext;
    }

    public Map<String, Object> getParentSpanContext() {
        return parentSpanContext;
    }

    public void setParentSpanContext(Map<String, Object> parentSpanContext) {
        this.parentSpanContext = parentSpanContext;
    }

    public List<Object> getSpans() {
        return spans;
    }

    public void setSpans(List<Object> spans) {
        this.spans = spans;
    }
}
