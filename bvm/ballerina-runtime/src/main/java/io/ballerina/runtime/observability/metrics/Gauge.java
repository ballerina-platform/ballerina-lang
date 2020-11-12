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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Gauge is used to track a value that goes up and down. Gauge can also report instantaneous values.
 * A gauge can be summarized to track the sample distribution of events.
 */
public interface Gauge extends Metric {

    /**
     * Create new builder for {@link Gauge}.
     *
     * @param name The name of the metric.
     * @return The builder for {@link Gauge}.
     */
    static Builder builder(String name) {
        return new Builder(name);
    }

    /**
     * Builder for {@link Gauge}s.
     */
    class Builder implements Metric.Builder<Builder, Gauge> {

        private final String name;
        // Expecting at least 10 tags
        private final Set<Tag> tags = new HashSet<>(10);
        private String description;
        private final List<StatisticConfig> statisticConfigs = new ArrayList<>();

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
         * Summarize gauge values using given {@link StatisticConfig statistic configuration}.
         *
         * @param statisticConfig The {@link StatisticConfig} used for summarization.
         * @return The builder with added statistic configuration for summarization.
         */
        public Builder summarize(StatisticConfig statisticConfig) {
            statisticConfigs.add(statisticConfig);
            return this;
        }

        @Override
        public Gauge register() {
            return register(DefaultMetricRegistry.getInstance());
        }

        @Override
        public Gauge register(MetricRegistry registry) {
            return registry.gauge(new MetricId(name, description, tags), statisticConfigs.toArray(
                    new StatisticConfig[statisticConfigs.size()]));
        }

        @Override
        public Gauge build() {
            return DefaultMetricRegistry.getInstance().getMetricProvider().
                    newGauge(new MetricId(name, description, tags), statisticConfigs.toArray(
                            new StatisticConfig[statisticConfigs.size()]));
        }
    }

    /**
     * Registers the gauge instance to the metrics registry.
     *
     * @return The registered Gauge instance.
     */
    default Gauge register() {
        return DefaultMetricRegistry.getInstance().register(this);
    }


    /**
     * Unregisters the metric to the registry.
     *
     */
    default void unregister() {
        DefaultMetricRegistry.getInstance().unregister(this);
    }

    /**
     * Increment the gauge by one.
     */
    default void increment() {
        increment(1D);
    }

    /**
     * Increment the gauge by {@code amount}.
     *
     * @param amount Amount to add to the gauge.
     */
    void increment(double amount);

    /**
     * Decrement the gauge by one.
     */
    default void decrement() {
        decrement(1D);
    }

    /**
     * Decrement the gauge by {@code amount}.
     *
     * @param amount Amount to subtract from the gauge.
     */
    void decrement(double amount);

    /**
     * Set the gauge to the given value. This will overwrite existing value.
     *
     * @param value The value to set to the gauge.
     */
    void setValue(double value);

    /**
     * @return The value of the gauge.
     */
    double getValue();

    /**
     * Returns the number of times that setValue has been called since this gauge was created.
     *
     * @return The number of times setValue has been called.
     */
    long getCount();

    /**
     * Returns the total amount of all values.
     *
     * @return The sum of values.
     */
    double getSum();

    /**
     * Returns snapshots of the values if the gauge was configured to summarize.
     *
     * @return Snapshots of all distribution statistics at a point in time.
     */
    Snapshot[] getSnapshots();

    /**
     * Returns statistics configs added for summarization.
     *
     * @return Array of statistics configurations.
     */
    StatisticConfig[] getStatisticsConfig();

}
