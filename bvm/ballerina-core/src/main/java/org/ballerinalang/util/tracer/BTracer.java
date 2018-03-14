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

import org.ballerinalang.bre.bvm.WorkerExecutionContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.ballerinalang.util.tracer.TraceConstants.DEFAULT_ACTION_NAME;
import static org.ballerinalang.util.tracer.TraceConstants.DEFAULT_CONNECTOR_NAME;
import static org.ballerinalang.util.tracer.TraceConstants.INVOCATION_ID;
import static org.ballerinalang.util.tracer.TraceConstants.TAG_KEY_STR_ERROR;
import static org.ballerinalang.util.tracer.TraceConstants.TAG_STR_TRUE;
import static org.ballerinalang.util.tracer.TraceConstants.TRACE_PREFIX;

/**
 * {@code BTracer} holds the trace of the current context.
 *
 * @since 0.964.1
 */
public class BTracer implements Tracer {

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
     * Name of the service.
     */
    private String connectorName = DEFAULT_CONNECTOR_NAME;
    /**
     * Name of the resource.
     */
    private String actionName = DEFAULT_ACTION_NAME;
    /**
     * Active Ballerina {@link WorkerExecutionContext}.
     */
    private WorkerExecutionContext executionContext = null;
    /**
     * Map of spans belongs to each open tracer.
     */
    private Map<String, ?> spans;
    /**
     * Indicate whether this is a root tracer.
     */
    private boolean isRoot = false;

    private BTracer() {

    }

    public BTracer(WorkerExecutionContext executionContext, boolean isClientContext) {
        this.properties = new HashMap<>();
        this.tags = new HashMap<>();
        this.executionContext = executionContext;
        this.isRoot = !isClientContext;
        this.tags.put(TraceConstants.TAG_KEY_SPAN_KIND, isClientContext
                ? TraceConstants.TAG_SPAN_KIND_CLIENT
                : TraceConstants.TAG_SPAN_KIND_SERVER);
    }

    @Override
    public void startSpan() {
        manager.startSpan(executionContext);
    }

    @Override
    public void finishSpan() {
        manager.finishSpan(this);
    }

    @Override
    public void log(Map<String, Object> fields) {
        manager.log(this, fields);
    }

    @Override
    public void logError(Map<String, Object> fields) {
        addTags(Collections.singletonMap(TAG_KEY_STR_ERROR, TAG_STR_TRUE));
        manager.log(this, fields);

    }

    @Override
    public void addTags(Map<String, String> tags) {
        if (spans != null) {
            //span has started, there for add tags to the span.
            manager.addTags(this, tags);
        } else {
            //otherwise keep the tags in a map, and add it once
            //the span get created.
            this.tags.putAll(tags);
        }

    }

    @Override
    public String getConnectorName() {
        return connectorName;
    }

    @Override
    public void setConnectorName(String connectorName) {
        this.connectorName = connectorName;
    }

    @Override
    public String getActionName() {
        return actionName;
    }

    @Override
    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public void addProperty(String key, String value) {
        if (properties != null) {
            properties.put(key, value);
        }
    }

    @Override
    public String getProperty(String key) {
        if (properties != null) {
            return properties.get(key);
        }
        return null;
    }

    @Override
    public Map<String, String> getTags() {
        return tags;
    }

    @Override
    public String getInvocationID() {
        return getProperty(TRACE_PREFIX + INVOCATION_ID);
    }

    @Override
    public void setInvocationID(String invocationId) {
        addProperty(TRACE_PREFIX + INVOCATION_ID, invocationId);
    }

    @Override
    public void setExecutionContext(WorkerExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    @Override
    public Map getSpans() {
        return spans;
    }

    @Override
    public void setSpans(Map<String, ?> spans) {
        this.spans = spans;
    }

    @Override
    public void generateInvocationID() {
        setInvocationID(String.valueOf(ThreadLocalRandom.current().nextLong()));
    }

    @Override
    public boolean isRoot() {
        return isRoot;
    }
}
