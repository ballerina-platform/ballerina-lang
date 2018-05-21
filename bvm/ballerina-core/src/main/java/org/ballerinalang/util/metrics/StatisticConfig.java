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

import org.ballerinalang.config.ConfigRegistry;

import java.io.PrintStream;
import java.time.Duration;
import java.util.Arrays;

import static org.ballerinalang.util.observability.ObservabilityConstants.CONFIG_TABLE_METRICS;

/**
 * Configures the distribution statistics.
 */
public class StatisticConfig {

    public static final StatisticConfig DEFAULT;

    private static final String METRICS_STATISTIC_CONFIGS = CONFIG_TABLE_METRICS + ".statistic";
    private static final String METRICS_STATISTIC_PERCENTILES = METRICS_STATISTIC_CONFIGS + ".percentiles";
    private static final String METRICS_STATISTIC_EXPIRY = METRICS_STATISTIC_CONFIGS + ".expiry";

    static {
        final PrintStream consoleErr = System.err;
        // Get defaults from config registry
        ConfigRegistry configRegistry = ConfigRegistry.getInstance();
        double[] percentiles = null;
        Duration expiry = null;
        try {
            String configPercentiles = configRegistry.getAsString(METRICS_STATISTIC_PERCENTILES);
            if (configPercentiles != null) {
                percentiles = Arrays.stream(configPercentiles.split(","))
                        .map(s -> s.trim())
                        .mapToDouble(Double::parseDouble)
                        .toArray();
            }
        } catch (RuntimeException e) {
            consoleErr.println("ballerina: error parsing percentiles for Metrics statistic configuration");
        }
        try {
            String configExpiry = configRegistry.getAsString(METRICS_STATISTIC_EXPIRY);
            if (configExpiry != null) {
                expiry = Duration.parse(configExpiry);
            }
        } catch (RuntimeException e) {
            consoleErr.println("ballerina: error parsing expiry for Metrics statistic configuration");
        }
        StatisticConfig.Builder builder = builder();
        if (percentiles != null) {
            builder.percentiles(percentiles);
        } else {
            builder.percentiles(new double[]{0.5, 0.75, 0.98, 0.99, 0.999});
        }
        if (expiry != null) {
            builder.expiry(expiry);
        } else {
            builder.expiry(Duration.ofMinutes(1));
        }
        DEFAULT = builder.build();
    }

    private StatisticConfig() {
    }

    /**
     * Compute 50th, 75th, 98th, 99th and 99.9th percentiles by default.
     */
    private double[] percentiles = new double[]{0.5, 0.75, 0.98, 0.99, 0.999};

    /**
     * Expire statistics after two minutes.
     */
    private Duration expiry = Duration.ofMinutes(2);

    /**
     * Get percentiles.
     *
     * @return Percentiles to compute and publish.
     * @see Builder#percentiles(double...)
     */
    public double[] getPercentiles() {
        return percentiles;
    }

    /**
     * Get the expiry duration.
     *
     * @return Duration to expire statistics.
     * @see Builder#expiry(Duration)
     */
    public Duration getExpiry() {
        return expiry;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link StatisticConfig}.
     */
    public static class Builder {

        private StatisticConfig config = new StatisticConfig();

        /**
         * Percentiles to compute and publish from a summary metric. The percentiles are computed locally, and
         * these percentile values cannot be aggregated with percentiles computed across other dimensions
         * (e.g. in a different instance).
         *
         * @param percentiles Percentiles to compute and publish.
         * @return This builder.
         */
        public Builder percentiles(double... percentiles) {
            config.percentiles = percentiles;
            return this;
        }

        /**
         * Duration to expire statistics. Statistics like max and percentile decay over time to give greater weight to
         * recent samples.
         *
         * @param expiry The duration of samples used to compute statistics.
         * @return This builder.
         */
        public Builder expiry(Duration expiry) {
            config.expiry = expiry;
            return this;
        }

        /**
         * @return A new immutable statistic configuration.
         */
        public StatisticConfig build() {
            if (config.percentiles == null) {
                config.percentiles = DEFAULT.getPercentiles();
            }
            if (config.expiry == null) {
                config.expiry = DEFAULT.getExpiry();
            }
            return config;
        }
    }
}
