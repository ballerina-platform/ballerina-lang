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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * {@code TraceContext} holds the current trace context of the program.
 *
 * @since 0.963.1
 */
public class TraceContext {
    private static final String FUNCTION_INIT = "<init>";
    private static final String KEY_SPAN_KIND = "span.kind";
    private static final String SPAN_KIND_SERVER = "server";
    private static final String SPAN_KIND_CLIENT = "client";
    private static final String DEFAULT_SERVICE_NAME = "default_service";
    private static final String DEFAULT_RESOURCE_NAME = "default_resource";
    private static final Random RANDOM = new Random();

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
     * ID that get generated when a span get created.
     */
    private String spanID;
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

    private TraceContext() {
        this.properties = new HashMap<>();
        this.tags = new HashMap<>();
    }

    public TraceContext(boolean isClientContext) {
        this();
        this.isClientContext = isClientContext;
        addTag(KEY_SPAN_KIND, isClientContext ? SPAN_KIND_CLIENT : SPAN_KIND_SERVER);
    }

    public TraceContext(Context bContext, boolean isClientContext) {
        this(isClientContext);
        this.isTraceable = !(isClientContext && bContext.getControlStack().getCurrentFrame()
                .getCallableUnitInfo().getName().endsWith(FUNCTION_INIT));
    }

    public TraceContext(String serviceName, String resourceName, String spanName, boolean isClientContext) {
        properties = new HashMap<>();
        tags = new HashMap<>();
        this.spanName = spanName;
        this.isClientContext = isClientContext;
        this.serviceName = (serviceName == null || serviceName.isEmpty()) ? DEFAULT_SERVICE_NAME : serviceName;
        this.resourceName = (resourceName == null || resourceName.isEmpty()) ? DEFAULT_RESOURCE_NAME : resourceName;
        addTag(KEY_SPAN_KIND, isClientContext ? SPAN_KIND_CLIENT : SPAN_KIND_SERVER);
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

    public void addTag(String key, String value) {
        if (tags != null) {
            tags.put(key, value);
        }
    }

    public String getTag(String key) {
        if (tags != null) {
            return tags.get(key);
        }
        return null;
    }

    public String getSpanID() {
        return spanID;
    }

    public void setSpanID(String spanID) {
        this.spanID = spanID;
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
}
