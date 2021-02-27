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

import java.util.Map;

/**
 * Main interface for metrics.
 */
public interface Metric {

    /**
     * @return The ID of the metric, which is a unique combination of name and tags.
     */
    MetricId getId();

    /**
     * Registers the metric with Metric registry.
     *
     * @return The registered metric instance.
     */
    Metric register();

    /**
     * Unregisters the metric with Metric registry.
     */
    void unregister();

    /**
     * Builder for metrics.
     *
     * @param <B> Builder for a given metric class.
     * @param <M> Metric class.
     */
    interface Builder<B extends Builder<B, M>, M extends Metric> {

        /**
         * Set the description of the metric.
         *
         * @param description The description of the metric.
         * @return This builder instance.
         */
        B description(String description);

        /**
         * Add tags to this metric.
         *
         * @param keyValues Must be an even number of arguments representing key/value pairs of tags.
         * @return The builder with added tags.
         */
        B tags(String... keyValues);

        /**
         * Add tags to this metric.
         *
         * @param tags Tags to add to the metrics
         * @return The builder with added tags.
         */
        B tags(Iterable<Tag> tags);

        /**
         * Add tags to this metric.
         *
         * @param key   The tag key.
         * @param value The tag value.
         * @return The builder with a single added tag.
         */
        B tag(String key, String value);

        /**
         * Add tags to this metric.
         *
         * @param tags A map of key value pairs to be used as tags
         * @return The builder with added tags.
         */
        B tags(Map<String, String> tags);

        /**
         * Create and register the metric with the default registry.
         *
         * @return Registered metric
         */
        M register();

        /**
         * Create and register the metric with the given registry.
         *
         * @param registry {@link MetricRegistry} to be used
         * @return Registered metric
         */
        M register(MetricRegistry registry);

        /**
         * Build the metric without registering.
         *
         * @return Metric instance.
         */
        M build();
    }

}
