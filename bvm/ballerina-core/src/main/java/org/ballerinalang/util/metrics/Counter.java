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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A monotonically increasing counter metric. Use {@link Gauge} Gauge to track a value that goes up and down.
 */
public interface Counter extends Metric {

    /**
     * Create new builder for {@link Counter}.
     *
     * @param name The name of the metric.
     * @return The builder for {@link Counter}.
     */
    static Builder builder(String name) {
        return new Builder(name);
    }

    /**
     * Builder for {@link Counter}s.
     */
    class Builder extends Metric.Builder<Builder, Counter> {

        private final String name;
        private final List<Tag> tags = new ArrayList<>();
        private String description;

        private Builder(String name) {
            this.name = name;
        }

        @Override
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        @Override
        public Builder tags(String... keyValues) {
            this.tags.addAll(Tags.tags(keyValues));
            return this;
        }

        @Override
        public Builder tags(Iterable<Tag> tags) {
            this.tags.addAll(Tags.tags(tags));
            return this;
        }

        @Override
        public Builder tag(String key, String value) {
            this.tags.addAll(Tags.tags(key, value));
            return this;
        }

        @Override
        public Builder tags(Map<String, String> tags) {
            this.tags.addAll(Tags.tags(tags));
            return this;
        }

        @Override
        public Counter register(MetricRegistry registry) {
            return registry.counter(new MetricId(name, description, tags));
        }
    }

    /**
     * Increment the counter by one.
     */
    default void increment() {
        increment(1D);
    }

    /**
     * Increment the counter by {@code amount}.
     *
     * @param amount Amount to add to the counter.
     */
    void increment(double amount);

    /**
     * @return The cumulative count since this counter was created.
     */
    double count();

}
