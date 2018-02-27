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

import java.util.HashMap;
import java.util.Map;

/**
 * {@code TraceContext} holds the current trace context of the program.
 *
 * @since 0.963.1
 */
public class TraceContext {
    private Map<String, String> properties;
    private Map<String, String> tags;

    public TraceContext() {
        properties = new HashMap<>();
        tags = new HashMap<>();
    }

    private TraceContext(TraceContext t) {
        properties = new HashMap<>(t.getProperties());
        tags = new HashMap<>(t.getTags());
    }

    public void addProperty(String key, String value) {
        if (properties != null) {
            properties.put(key, value);
        }
    }

    public void addTag(String key, String value) {
        if (tags != null) {
            tags.put(key, value);
        }
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public TraceContext copy() {
        return new TraceContext(this);
    }
}
