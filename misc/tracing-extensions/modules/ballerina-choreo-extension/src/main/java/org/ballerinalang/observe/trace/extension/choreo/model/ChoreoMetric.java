/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.observe.trace.extension.choreo.model;

import java.util.Map;

/**
 * Represents a Metric published to Choreo.
 */
public class ChoreoMetric {
    private long timestamp;
    private String name;
    private double value;
    private Map<String, String> tags;

    public ChoreoMetric(long timestamp, String name, double value, Map<String, String> tags) {
        this.timestamp = timestamp;
        this.name = name;
        this.value = value;
        this.tags = tags;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }

    public Map<String, String> getTags() {
        return tags;
    }
}
