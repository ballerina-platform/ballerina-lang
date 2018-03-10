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

package org.ballerinalang.util.tracer;

import org.ballerinalang.bre.Context;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.ballerinalang.util.tracer.TraceConstant.DEFAULT_RESOURCE;
import static org.ballerinalang.util.tracer.TraceConstant.DEFAULT_SERVICE;
import static org.ballerinalang.util.tracer.TraceConstant.INVOCATION_ID;
import static org.ballerinalang.util.tracer.TraceConstant.STR_ERROR;
import static org.ballerinalang.util.tracer.TraceConstant.STR_TRUE;
import static org.ballerinalang.util.tracer.TraceConstant.TRACE_PREFIX;

/**
 * {@code BTracer} holds the trace of the current context.
 *
 * @since 0.963.1
 */
public class BTracer {

    private static final TraceManagerWrapper manager = TraceManagerWrapper.getInstance();

    /**
     * {@link Map} of properties, which used to represent
     * the span contexts of each tracer.
     */
    private Map<String, String> properties;
    /**
     * {@link Map} of tags, which will get injected to spans.
     */
    private Map<String, String> tags;
    /**
     * flag to represent whether this is a client (originate
     * from a client connector) context or a server context.
     */
    private boolean isClientContext;
    /**
     * Name of the service.
     */
    private String serviceName = DEFAULT_SERVICE;
    /**
     * Name of the resource.
     */
    private String resourceName = DEFAULT_RESOURCE;
    /**
     * Indicates whether this context is traceable or not.
     */
    private boolean traceable = false;
    /**
     * Active Ballerina context.
     */
    private Context bContext = null;
    /**
     * Map of spans belongs to each open tracer.
     */
    private Map<String, ?> spans;

    private BTracer() {
        this.properties = new HashMap<>();
        this.tags = new HashMap<>();
    }

    public BTracer(Context bContext, boolean isClientContext) {
        this();
        this.bContext = bContext;
        this.isClientContext = isClientContext;
        this.tags.put(TraceConstant.KEY_SPAN_KIND, isClientContext
                ? TraceConstant.SPAN_KIND_CLIENT
                : TraceConstant.SPAN_KIND_SERVER);
        this.traceable = !(isClientContext &&
                bContext.getControlStack().getCurrentFrame().getCallableUnitInfo()
                        .getName().endsWith(TraceConstant.FUNCTION_INIT));
    }

    public void startSpan() {
        if (traceable) {
            manager.startSpan(bContext);
        }
    }

    public void finishSpan() {
        if (traceable) {
            manager.finishSpan(this);
            spans.clear();
            properties.clear();
            tags.clear();
            spans = null;
            properties = null;
            tags = null;
            traceable = false;
        }
    }

    public void log(Map<String, Object> fields) {
        if (traceable) {
            manager.log(this, fields);
        }
    }

    public void logError(Map<String, Object> fields) {
        if (traceable) {
            addTags(Collections.singletonMap(STR_ERROR, STR_TRUE));
            manager.log(this, fields);
        }
    }

    public void addTags(Map<String, String> tags) {
        if (traceable) {
            if (spans != null) {
                //span has started, there for add tags to the span.
                manager.addTags(this, tags);
            } else {
                //otherwise keep the tags in a map, and add it once
                //the span get created.
                this.tags.putAll(tags);
            }
        }
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

    public boolean isClientContext() {
        return isClientContext;
    }

    public String getInvocationID() {
        return getProperty(TRACE_PREFIX + INVOCATION_ID);
    }

    public void setInvocationID(String invocationId) {
        addProperty(TRACE_PREFIX + INVOCATION_ID, invocationId);
    }

    public void setContext(Context bContext) {
        this.bContext = bContext;
    }

    public Map getSpans() {
        return spans;
    }

    public void setSpans(Map<String, ?> spans) {
        this.spans = spans;
    }

    public void generateInvocationID() {
        setInvocationID(String.valueOf(ThreadLocalRandom.current().nextLong()));
    }
}
