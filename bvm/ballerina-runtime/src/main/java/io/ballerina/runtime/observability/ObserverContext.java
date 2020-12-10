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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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

    private String serviceName;

    private String resourceName;

    private String objectName;

    private String functionName;

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

    @Deprecated
    public void addMainTag(String key, String value) {
        // TODO: Remove this method once all the usages in the standard libraries had been updated.
        addTag(key, value);
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

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = Objects.requireNonNull(serviceName);
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = Objects.requireNonNull(resourceName);
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = Objects.requireNonNull(objectName);
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = Objects.requireNonNull(functionName);
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
}
