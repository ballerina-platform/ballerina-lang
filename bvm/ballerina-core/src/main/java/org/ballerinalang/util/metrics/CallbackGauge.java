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
import java.util.function.ToDoubleFunction;

/**
 * Gauge to report instantaneous values by using a callback function.
 */
public interface CallbackGauge extends Metric {

    /**
     * Create new builder for {@link CallbackGauge}.
     *
     * @param name          The name of the metric.
     * @param obj           State object used to compute a value.
     * @param valueFunction Function that produces an instantaneous gauge value from the state object.
     * @param <T>           The type of the state object from which the gauge value is extracted.
     * @return The builder for {@link CallbackGauge}.
     */
    static <T> Builder<T> builder(String name, T obj, ToDoubleFunction<T> valueFunction) {
        return new Builder<>(name, obj, valueFunction);
    }

    /**
     * Builder for {@link CallbackGauge}s.
     *
     * @param <T> The type of the state object from which the gauge value is extracted.
     */
    class Builder<T> extends Metric.Builder<Builder<T>, CallbackGauge> {

        private final String name;
        private final List<Tag> tags = new ArrayList<>();
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
        public Builder<T> tags(String... keyValues) {
            Tags.tags(keyValues).forEach(this.tags::add);
            return this;
        }

        @Override
        public Builder<T> tags(Iterable<Tag> tags) {
            Tags.tags(tags).forEach(this.tags::add);
            return this;
        }

        @Override
        public Builder<T> tag(String key, String value) {
            Tags.tags(key, value).forEach(this.tags::add);
            return this;
        }

        @Override
        public Builder<T> tags(Map<String, String> tags) {
            Tags.tags(tags).forEach(this.tags::add);
            return this;
        }

        @Override
        public CallbackGauge register(MetricRegistry registry) {
            return registry.callbackGauge(new MetricId(name, description, tags), obj, valueFunction);
        }
    }

    /**
     * @return The value of the gauge.
     */
    double get();

}
