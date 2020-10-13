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

/**
 * Snapshot of all distribution statistics at a point in time.
 *
 * @since 0.980.0
 */
public class Snapshot {
    private static final PercentileValue[] EMPTY_PERCENTILE_VALUES = new PercentileValue[0];

    private final Duration timeWindow;
    private final double min;
    private final double mean;
    private final double stdDev;
    private final double max;
    private final PercentileValue[] percentileValues;

    public Snapshot(Duration timeWindow, double min, double mean, double stdDev, double max,
                    PercentileValue[] percentileValues) {
        this.timeWindow = timeWindow;
        this.min = min;
        this.mean = mean;
        this.stdDev = stdDev;
        this.max = max;
        this.percentileValues = percentileValues;
    }

    /**
     * Returns the timewindow configuration for this snapshot.
     *
     * @return The timeWindow used to calculate statistics in this {@link Snapshot}.
     */
    public Duration getTimeWindow() {
        return timeWindow;
    }

    /**
     * Returns the minimum value encountered within the time window in this snapshot.
     *
     * @return The minimum value of a single event.
     */
    public double getMin() {
        return min;
    }

    /**
     * Returns the mean value for the time window in this snapshot.
     *
     * @return The distribution average for recorded events.
     */
    public double getMean() {
        return mean;
    }

    /**
     * @return The distribution standard deviation for recorded events.
     */
    public double getStdDev() {
        return stdDev;
    }

    /**
     * @return The maximum value of a single event.
     */
    public double getMax() {
        return max;
    }

    /**
     * Return an array of {@link PercentileValue percentile values}. These value are non-aggregable across dimensions.
     *
     * @return An array of percentile values.
     */
    public PercentileValue[] getPercentileValues() {
        return percentileValues != null ? Arrays.copyOf(percentileValues, percentileValues.length) :
                EMPTY_PERCENTILE_VALUES;
    }

}
