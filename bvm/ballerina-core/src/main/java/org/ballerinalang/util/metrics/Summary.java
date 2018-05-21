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

import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Track the sample distribution of events.
 */
public interface Summary extends Metric {

    /**
     * Create new builder for {@link Summary}.
     *
     * @param name The name of the metric.
     * @return The builder for {@link Summary}.
     */
    static Builder builder(String name) {
        return new Builder(name);
    }

    /**
     * Builder for {@link Summary}s.
     */
    class Builder implements Metric.Builder<Builder, Summary> {

        private final String name;
        // Expecting at least 10 tags
        private final Set<Tag> tags = new HashSet<>(10);
        private String description;

        private StatisticConfig.Builder statisticConfigBuilder = StatisticConfig.builder();

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

        /**
         * @param percentiles Percentiles to compute and publish. Percentile is in the domain [0,1].
         *                    For example, the 95th percentile should be expressed as {@code 0.95}.
         * @return This builder instance.
         * @see StatisticConfig.Builder#percentiles(double...)
         */
        public Builder percentiles(double... percentiles) {
            statisticConfigBuilder.percentiles(percentiles);
            return this;
        }

        /**
         * @param expiry The duration of samples used to compute statistics.
         * @return This builder instance.
         * @see StatisticConfig.Builder#expiry(Duration)
         */
        public Builder expiry(Duration expiry) {
            statisticConfigBuilder.expiry(expiry);
            return this;
        }

        @Override
        public Summary register() {
            return register(DefaultMetricRegistry.getInstance());
        }

        @Override
        public Summary register(MetricRegistry registry) {
            return registry.summary(new MetricId(name, description, tags), statisticConfigBuilder.build());
        }
    }

    /**
     * Updates the statistics kept by the summary with the specified amount.
     *
     * @param amount Amount for an event being measured.
     */
    void record(long amount);

    /**
     * Returns the number of times that record has been called since this summary was created.
     *
     * @return The number of values recorded.
     */
    long getCount();

    /**
     * Returns the total amount of all recorded events.
     *
     * @return The sum of values recorded.
     */
    long getSum();

    /**
     * Returns a snapshot of the values.
     *
     * @return A snapshot of all distribution statistics at a point in time.
     */
    Snapshot getSnapshot();
}
