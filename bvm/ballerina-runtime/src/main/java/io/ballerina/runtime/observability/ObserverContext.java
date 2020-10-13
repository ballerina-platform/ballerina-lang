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

import java.util.Collections;
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
     *
     * {@link Map} is used here to stop {@link Set} and @{link Tag} objects being instantiated
     * every-time tags are taken from the observer context to generate metrics.
     *
     * These tags are updated before the a service resource function is hit in the runtime.
     * After that point only additional tags should be used.
     */
    private final Map<String, Tag> mainTags;

    /**
     * This is similar to the mainTags.
     * However, this map contains all the tags added after a service resource function is hit in the runtime.
     */
    private final Map<String, Tag> additionalTags;

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
        this.mainTags = new HashMap<>();
        this.additionalTags = new HashMap<>();
    }

    public void addProperty(String key, Object value) {
        properties.put(key, value);
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    /**
     * Add a main tag.
     * This method should only be invoked before a service resource function is hit in the runtime.
     *
     * @param key The tag key
     * @param value The tag value
     */
    public void addMainTag(String key, String value) {
        if (isStarted()) {
            throw new IllegalStateException("main tags cannot be added after the observation had been started");
        }
        addTag(mainTags, key, value);
    }

    /**
     * Add an additional tag.
     * This method should only be invoked after a service resource function is hit in the runtime.
     *
     * @param key The tag key
     * @param value The tag value
     */
    public void addTag(String key, String value) {
        addTag(additionalTags, key, value);
    }

    private void addTag(Map<String, Tag> tagsValueMap, String key, String value) {
        String sanitizedValue = value != null ? value : "";
        Tag tag = Tag.of(key, sanitizedValue);
        tagsValueMap.put(key, tag);
    }

    public Tag getTag(String key) {
        Tag tag = mainTags.get(key);
        if (tag == null) {
            tag = additionalTags.get(key);
        }
        return tag;
    }

    public Set<Tag> getMainTags() {
        Set<Tag> tagSet = new HashSet<>(mainTags.size());
        tagSet.addAll(mainTags.values());
        return Collections.unmodifiableSet(tagSet);
    }

    public Set<Tag> getAllTags() {
        Set<Tag> allTags = new HashSet<>(mainTags.size() + additionalTags.size());
        allTags.addAll(mainTags.values());
        allTags.addAll(additionalTags.values());
        return Collections.unmodifiableSet(allTags);
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
