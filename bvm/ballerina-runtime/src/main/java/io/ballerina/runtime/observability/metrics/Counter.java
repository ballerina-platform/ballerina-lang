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
package io.ballerina.runtime.observability.metrics;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    class Builder implements Metric.Builder<Builder, Counter> {

        private final String name;
        // Expecting at least 10 tags
        private final Set<Tag> tags = new HashSet<>(10);
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
            Tags.tags(this.tags, keyValues);
            return this;
        }

        @Override
        public Builder tags(Iterable<Tag> tags) {
            Tags.tags(this.tags, tags);
            return this;
        }

        @Override
        public Builder tag(String key, String value) {
            Tags.tags(this.tags, key, value);
            return this;
        }

        @Override
        public Builder tags(Map<String, String> tags) {
            Tags.tags(this.tags, tags);
            return this;
        }

        public Counter build() {
            return DefaultMetricRegistry.getInstance().getMetricProvider().
                    newCounter(new MetricId(name, description, tags));
        }

        @Override
        public Counter register() {
            return register(DefaultMetricRegistry.getInstance());
        }

        @Override
        public Counter register(MetricRegistry registry) {
            return registry.counter(new MetricId(name, description, tags));
        }
    }

    /**
     * Register the Metric to the registry.
     */
    default Counter register() {
        return DefaultMetricRegistry.getInstance().register(this);
    }

    /**
     * Unregisters the metric to the registry.
     */
    default void unregister() {
        DefaultMetricRegistry.getInstance().unregister(this);
    }
    /**
     * Increment the counter by one.
     */
    default void increment() {
        increment(1L);
    }

    /**
     * Reset to the counter's current value to zero.
     *
     */
    void reset();

    /**
     * Increment the counter by {@code amount}.
     *
     * @param amount Amount to add to the counter.
     */
    void increment(long amount);

    /**
     * Returns the counter's current value.
     *
     * @return the counter's current value.
     */
    long getValue();

    /**
     * Returns the counter's current value and then resets.
     *
     * @return the counter's current value.
     */
    long getValueThenReset();

}
