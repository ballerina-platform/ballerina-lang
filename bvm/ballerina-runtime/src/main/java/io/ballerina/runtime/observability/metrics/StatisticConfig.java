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

import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;

/**
 * Configures the distribution statistics.
 *
 * @since 0.980.0
 */
public class StatisticConfig {

    public static final StatisticConfig DEFAULT = StatisticConfig.builder()
            // Compute 33rd, 50th, 66th, 75th, 95th, 99th, and 99.9th percentiles.
            .percentiles(0.33, 0.5, 0.66, 0.75, 0.95, 0.99, 0.999).build();

    private StatisticConfig() {
    }

    /**
     * Percentiles to compute and publish.
     */
    private double[] percentiles;

    /**
     * Precision of percentiles.
     */
    private int percentilePrecision = 2;

    /**
     * Expire statistics after ten minutes.
     */
    private Duration timeWindow = Duration.ofMinutes(10);

    /**
     * The number of buckets used to implement the sliding time window.
     */
    private long buckets = 5;

    /**
     * Get percentiles.
     *
     * @return Percentiles to compute and publish.
     * @see Builder#percentiles(double...)
     */
    public double[] getPercentiles() {
        return percentiles != null ? Arrays.copyOf(percentiles, percentiles.length) : percentiles;
    }

    /**
     * Get the precision to used to maintain on the dynamic range histogram used to compute
     * percentile approximations.
     *
     * @return The digits of precision to maintain for percentile approximations.
     */
    public Integer getPercentilePrecision() {
        return percentilePrecision;
    }

    /**
     * Get the timeWindow duration.
     *
     * @return Duration to expire statistics.
     * @see Builder#expiry(Duration)
     */
    public Duration getTimeWindow() {
        return timeWindow;
    }

    /**
     * Get the number of buckets used to implement the sliding time window.
     *
     * @return The number of buckets
     */
    public long getBuckets() {
        return buckets;
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
         * Percentiles to compute and publish. Percentile is in the domain [0,1].
         * For example, the 95th percentile should be expressed as {@code 0.95}. The percentiles are computed locally,
         * and these percentile values cannot be aggregated with percentiles computed across other dimensions
         * (e.g. in a different instance). Default percentiles are 0.5, 0.75, 0.98, 0.99, and 0.999
         *
         * @param percentiles Percentiles to compute and publish.
         * @return This builder.
         */
        public Builder percentiles(double... percentiles) {
            percentiles = Objects.requireNonNull(percentiles, "percentiles array should not be null.");
            if (percentiles.length == 0) {
                throw new IllegalArgumentException("percentiles array should not be empty.");
            }
            for (double quantile : percentiles) {
                if (quantile < 0.0 || quantile > 1.0) {
                    throw new IllegalArgumentException("Quantile " + quantile + " invalid: " +
                            "Expected number between 0.0 and 1.0.");
                }
            }
            config.percentiles = percentiles;
            return this;
        }

        /**
         * Specify the number of digits of precision to maintain on the dynamic range histogram used to compute
         * percentile approximations.
         *
         * @param digitsOfPrecision The digits of precision to maintain for percentile approximations.
         * @return This builder.
         */
        public Builder percentilePrecision(int digitsOfPrecision) {
            if ((digitsOfPrecision < 0) || (digitsOfPrecision > 5)) {
                throw new IllegalArgumentException("precision cannot be " + digitsOfPrecision +
                        ". It must be a non-negative integer between 0 and 5");
            }
            config.percentilePrecision = digitsOfPrecision;
            return this;
        }

        /**
         * Duration to expire statistics. Statistics like max and percentile decay over time to give greater weight to
         * recent samples. Default value is 10 minutes.
         *
         * @param expiry The duration of samples used to compute statistics.
         * @return This builder.
         */
        public Builder expiry(Duration expiry) {
            if (expiry.getSeconds() <= 0) {
                throw new IllegalArgumentException("timeWindow cannot be " + expiry
                        + ". It must be greater than zero.");
            }
            config.timeWindow = expiry;
            return this;
        }

        /**
         * Set the number of buckets used to implement the sliding time window. If the time window is 10 minutes, and
         * buckets=5, buckets will be switched every 2 minutes. The value is a trade-off between resources
         * (memory and cpu for maintaining the bucket) and how smooth the time window is moved. Default value is 5.
         *
         * @param buckets number of buckets used to implement the sliding time window
         * @return This builder.
         */
        public Builder buckets(long buckets) {
            if (buckets <= 0) {
                throw new IllegalArgumentException("buckets cannot be " + buckets + ". It must be greater than zero.");
            }
            config.buckets = buckets;
            return this;
        }

        /**
         * @return A new immutable statistic configuration.
         */
        public StatisticConfig build() {
            return config;
        }
    }
}
