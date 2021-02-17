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
package io.ballerina.runtime.observability;

import io.ballerina.runtime.observability.metrics.Tag;
import io.ballerina.runtime.observability.tracer.BSpan;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_LISTENER_NAME;

/**
 * Context object used for observation purposes.
 */
public class ObserverContext {

    /**
     * {@link Map} of properties, which is used to represent additional information required for observers.
     */
    private final Map<String, Object> properties;

    /**
     * {@link Map} of values (with tag as map's key and tag value as map's value),
     * which is required to pass to observers.
     */
    private final Map<String, Tag> tags;

    /**
     * {@link Map} of custom Tags, which are relevant to metrics  .
     */
    public Map<String, Tag> customMetricTags;

    private BSpan span;

    private String entrypointFunctionModule;

    private String entrypointFunctionPosition;

    private String serviceName;

    private String operationName;

    private boolean server;

    private boolean started;

    private boolean finished;

    private ObserverContext parent;

    private boolean isSystemSpan;

    public ObserverContext() {
        this.properties = new HashMap<>();
        this.tags = new HashMap<>();
    }

    public void addProperty(String key, Object value) {
        properties.put(key, value);
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public void addTag(String key, String value) {
        String sanitizedValue = value != null ? value : "";
        Tag tag = Tag.of(key, sanitizedValue);
        tags.put(key, tag);
    }

    public Tag getTag(String key) {
        return tags.get(key);
    }

    public Set<Tag> getAllTags() {
        return new HashSet<>(tags.values());
    }

    public BSpan getSpan() {
        return span;
    }

    public void setSpan(BSpan span) {
        this.span = span;
    }

    public String getEntrypointFunctionModule() {
        return entrypointFunctionModule;
    }

    public void setEntrypointFunctionModule(String entrypointFunctionModule) {
        this.entrypointFunctionModule = entrypointFunctionModule;
    }

    public String getEntrypointFunctionPosition() {
        return entrypointFunctionPosition;
    }

    public void setEntrypointFunctionPosition(String entrypointFunctionPosition) {
        this.entrypointFunctionPosition = entrypointFunctionPosition;
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

    public boolean isServer() {
        return server;
    }

    void setServer() {
        this.server = true;
    }

    public boolean isStarted() {
        return started;
    }

    void setStarted() {
        this.started = true;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished() {
        this.finished = true;
    }

    public ObserverContext getParent() {
        return parent;
    }

    public void setParent(ObserverContext parent) {
        this.parent = parent;
    }

    public boolean isSystemSpan() {
        return isSystemSpan;
    }

    public void setSystemSpan(boolean userSpan) {
        isSystemSpan = userSpan;
    }

    @Deprecated
    public void setObjectName(String objectName) {
        // TODO: Remove once connector usages are removed (Connectors should directly add connector tag instead)
        addTag(TAG_KEY_LISTENER_NAME, objectName);
    }
}
