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
package org.ballerinalang.util.observability;

import org.ballerinalang.util.tracer.BSpan;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.ballerinalang.util.tracer.TraceConstants.KEY_SPAN;

/**
 * Context object used for observation purposes.
 */
public class ObserverContext {

    /**
     * Singleton {@link ObserverContext} to pass as the context for non observable contexts.
     */
    private static final ObserverContext emptyContext = new ObserverContext();

    /**
     * {@link Map} of properties, which is used to represent addition information required for observers.
     */
    private Map<String, Object> properties;

    /**
     * {@link Map} of tags, which is required to pass to observers.
     */
    private Map<String, String> tags;

    private String serviceName;

    private String resourceName;

    private String connectorName;

    private String actionName;

    private boolean server;

    private boolean started;

    private ObserverContext parent;

    public ObserverContext() {
        this.properties = new HashMap<>();
        this.tags = new HashMap<>();
    }

    public static ObserverContext emptyContext() {
        return emptyContext;
    }

    public void addProperty(String key, Object value) {
        properties.put(key, value);
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public void addTag(String key, String value) {
        tags.put(key, value);
    }

    public void addTags(Map<String, String> tags) {
        this.tags.putAll(tags);
    }

    public Map<String, String> getTags() {
        return Collections.unmodifiableMap(tags);
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

    public String getConnectorName() {
        return connectorName;
    }

    public void setConnectorName(String connectorName) {
        this.connectorName = Objects.requireNonNull(connectorName);
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = Objects.requireNonNull(actionName);
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

    public ObserverContext getParent() {
        return parent;
    }

    public void setParent(ObserverContext parent) {
        this.parent = parent;
    }

    public Map<String, String> getTraceProperties() {
        BSpan bSpan = (BSpan) getProperty(KEY_SPAN);
        if (bSpan != null) {
            return bSpan.getTraceContext();
        }
        return Collections.emptyMap();
    }
}
