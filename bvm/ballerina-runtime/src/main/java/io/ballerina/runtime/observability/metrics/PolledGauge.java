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
import java.util.function.ToDoubleFunction;

/**
 * Gauge to report instantaneous values by using a callback function.
 */
public interface PolledGauge extends Metric {

    /**
     * Create new builder for {@link PolledGauge}.
     *
     * @param name          The name of the metric.
     * @param obj           State object used to compute a value.
     * @param valueFunction Function that produces an instantaneous gauge value from the state object.
     * @param <T>           The type of the state object from which the gauge value is extracted.
     * @return The builder for {@link PolledGauge}.
     */
    static <T> Builder<T> builder(String name, T obj, ToDoubleFunction<T> valueFunction) {
        return new Builder<>(name, obj, valueFunction);
    }

    /**
     * Builder for {@link PolledGauge}s.
     *
     * @param <T> The type of the state object from which the gauge value is extracted.
     */
    class Builder<T> implements Metric.Builder<Builder<T>, PolledGauge> {

        private final String name;
        // Expecting at least 10 tags
        private final Set<Tag> tags = new HashSet<>(10);
        private String description;
        private T obj;
        private ToDoubleFunction<T> valueFunction;

        private Builder(String name, T obj, ToDoubleFunction<T> valueFunction) {
            this.name = name;
            this.obj = obj;
            this.valueFunction = valueFunction;
        }

        @Override
        public Builder<T> description(String description) {
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

        @Override
        public PolledGauge register() {
            return register(DefaultMetricRegistry.getInstance());
        }

        @Override
        public PolledGauge register(MetricRegistry registry) {
            return registry.polledGauge(new MetricId(name, description, tags), obj, valueFunction);
        }

        @Override
        public PolledGauge build() {
            return DefaultMetricRegistry.getInstance().getMetricProvider().
                    newPolledGauge(new MetricId(name, description, tags), obj, valueFunction);
        }
    }

    /**
     * Default implementation for register polled gauge.
     *
     * @return The registered gauge instance.
     */
    default PolledGauge register() {
        return DefaultMetricRegistry.getInstance().register(this);
    }

    /**
     * Default implementation for register polled gauge.
     *
     */
    default void unregister() {
        DefaultMetricRegistry.getInstance().unregister(this);
    }
    /**
     * @return The value of the gauge.
     */
    double getValue();

}
