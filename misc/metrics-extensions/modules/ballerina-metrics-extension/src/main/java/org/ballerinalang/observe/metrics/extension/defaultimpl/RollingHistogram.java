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
package org.ballerinalang.observe.metrics.extension.defaultimpl;

import io.ballerina.runtime.observability.metrics.PercentileValue;
import io.ballerina.runtime.observability.metrics.Snapshot;
import io.ballerina.runtime.observability.metrics.StatisticConfig;
import org.HdrHistogram.DoubleHistogram;
import org.HdrHistogram.DoubleRecorder;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * Using {@link DoubleHistogram} to maintain samples in a ring buffer to decay older samples and give greater weight
 * to recent samples. This implementation allows to get summary statistics for a rolling window over the last X minutes.
 */
public class RollingHistogram {

    /**
     * Clock for measuring time.
     */
    private final Clock clock;

    /**
     * Statistic configurations used for this {@link RollingHistogram}.
     */
    private final StatisticConfig statisticConfig;

    /**
     * Recorder in a ring buffer.
     */
    private final DoubleRecorder[] ringBuffer;

    /**
     * Current bucket index.
     */
    private int currentBucket;

    /**
     * Last time the {@link RollingHistogram} was rotated.
     */
    private volatile long lastRotateTimestampMillis;

    /**
     * Duration between rotations.
     */
    private final long durationBetweenRotatesMillis;

    /**
     * This interval histogram is reused when taking the next interval histogram.
     */
    private final DoubleHistogram intervalHistogram;

    /**
     * A histogram to keep accumulated stats.
     */
    private DoubleHistogram accumulatedHistogram;

    /**
     * Global atomic field updater to update volatile {@code rotating} integer.
     */
    private static final AtomicIntegerFieldUpdater<RollingHistogram> rotatingUpdater =
            AtomicIntegerFieldUpdater.newUpdater(RollingHistogram.class, "rotating");

    /**
     * A flag to check whether {@link RollingHistogram} is being rotated.
     */
    private volatile int rotating; // 0 - not rotating, 1 - rotating

    /**
     * A flag to check whether accumulatedHistogram is stale or not.
     */
    private volatile boolean accumulatedHistogramStale;

    public RollingHistogram(Clock clock, StatisticConfig statisticConfig) {
        this.clock = clock;
        this.statisticConfig = statisticConfig;
        int ageBuckets = (int) statisticConfig.getBuckets();
        ringBuffer = new DoubleRecorder[ageBuckets];
        for (int i = 0; i < ageBuckets; i++) {
            ringBuffer[i] = new DoubleRecorder(statisticConfig.getPercentilePrecision());
        }
        this.currentBucket = 0;
        this.lastRotateTimestampMillis = clock.getCurrentTime();
        this.durationBetweenRotatesMillis = statisticConfig.getTimeWindow().toMillis() / ageBuckets;
        intervalHistogram = new DoubleHistogram(statisticConfig.getPercentilePrecision());
        accumulatedHistogram = new DoubleHistogram(statisticConfig.getPercentilePrecision());
    }

    public void record(double value) {
        rotate();
        try {
            for (DoubleRecorder recorder : ringBuffer) {
                recorder.recordValue(value);
            }
        } finally {
            accumulatedHistogramStale = true;
        }
    }

    private void rotate() {
        long timeSinceLastRotateMillis = clock.getCurrentTime() - lastRotateTimestampMillis;
        if (timeSinceLastRotateMillis < durationBetweenRotatesMillis) {
            // Need to wait more for next rotation.
            return;
        }
        if (!rotatingUpdater.compareAndSet(this, 0, 1)) {
            // Being rotated by other thread already.
            return;
        }

        try {
            int iterations = 0;
            synchronized (this) {
                do {
                    currentHistogram().reset();
                    if (++currentBucket >= ringBuffer.length) {
                        currentBucket = 0;
                    }
                    timeSinceLastRotateMillis -= durationBetweenRotatesMillis;
                    lastRotateTimestampMillis += durationBetweenRotatesMillis;
                } while (timeSinceLastRotateMillis >= durationBetweenRotatesMillis && ++iterations < ringBuffer.length);

                if (iterations >= ringBuffer.length) {
                    // All buckets have been reset, therefore update lastRotateTimestampMillis
                    // to the most recent time window
                    lastRotateTimestampMillis += durationBetweenRotatesMillis *
                            (timeSinceLastRotateMillis / durationBetweenRotatesMillis);
                }

                accumulatedHistogram = new DoubleHistogram(statisticConfig.getPercentilePrecision());
                //TODO: Use accumulatedHistogram.reset(); and make accumulatedHistogram as final
                //Refer: https://github.com/HdrHistogram/HdrHistogram/issues/143
                accumulatedHistogramStale = true;
            }
        } finally {
            rotating = 0;
        }
    }

    public StatisticConfig getStatisticConfig() {
        return statisticConfig;
    }

    public Snapshot getSnapshot() {
        rotate();
        synchronized (this) {
            accumulateIfStale();
            PercentileValue[] percentileValues = null;
            final double[] monitoredPercentiles = statisticConfig.getPercentiles();
            if (monitoredPercentiles != null) {
                percentileValues = new PercentileValue[monitoredPercentiles.length];
                for (int i = 0; i < monitoredPercentiles.length; i++) {
                    final double p = monitoredPercentiles[i];
                    percentileValues[i] = new PercentileValue(p, accumulatedHistogram.getValueAtPercentile(p * 100));
                }
            }
            return new Snapshot(statisticConfig.getTimeWindow(),
                    accumulatedHistogram.getMinValue(),
                    accumulatedHistogram.getMean(),
                    accumulatedHistogram.getStdDeviation(),
                    accumulatedHistogram.getMaxValue(),
                    percentileValues);
        }
    }

    private void accumulateIfStale() {
        if (accumulatedHistogramStale) {
            accumulate();
            accumulatedHistogramStale = false;
        }
    }

    private void accumulate() {
        currentHistogram().getIntervalHistogramInto(intervalHistogram);
        accumulatedHistogram.add(intervalHistogram);
    }

    private DoubleRecorder currentHistogram() {
        return ringBuffer[currentBucket];
    }
}
