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
package org.ballerinalang.util.metrics;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Metric identity is a unique combination of name and tags.
 */
public class MetricId {

    private final String name;
    private final String description;
    private final SortedSet<Tag> tags;
    private final int hashCode;

    public MetricId(String name, String description, List<Tag> tags) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name is required.");
        }
        this.name = name;
        if (tags != null) {
            this.tags = Collections.unmodifiableSortedSet(new TreeSet<>(tags));
        } else {
            this.tags = Collections.emptySortedSet();
        }
        this.description = description;
        // Compute hash here as this Id is immutable
        this.hashCode = Objects.hash(name, tags);
    }


    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MetricId metricId = (MetricId) o;
        return Objects.equals(name, metricId.name) &&
                Objects.equals(tags, metricId.tags);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MetricId{");
        sb.append("name='").append(name).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", tags=").append(tags);
        sb.append('}');
        return sb.toString();
    }
}
