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
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link RollingHistogram}.
 *
 * @since 0.980.0
 */
public class RollingHistogramTest {

    @Test
    public void testRollingHistogram() {
        final AtomicInteger currentTime = new AtomicInteger(0);
        Clock clock = mock(Clock.class);
        when(clock.getCurrentTime()).then(invocationOnMock -> currentTime.get());
        Assert.assertEquals(clock.getCurrentTime(), 0);

        double[] percentiles = new double[]{0.0, 0.5, 0.75, 0.9, 0.98, 0.99, 0.999, 1.0};
        RollingHistogram histogram = new RollingHistogram(clock, StatisticConfig.builder()
                .percentiles(percentiles)
                .expiry(Duration.ofSeconds(4)).buckets(4).build());

        histogram.record(10);
        histogram.record(20);
        Snapshot snapshot = histogram.getSnapshot();
        Assert.assertEquals(percentileValue(snapshot, 0.0), 10.0, 1.0);
        Assert.assertEquals(percentileValue(snapshot, 1.0), 20.0, 1.0);
        Assert.assertEquals(snapshot.getMin(), 10.0, 1.0);
        Assert.assertEquals(snapshot.getMax(), 20.0, 1.0);

        currentTime.addAndGet(900);
        Assert.assertEquals(clock.getCurrentTime(), 900);

        histogram.record(30);
        histogram.record(40);

        snapshot = histogram.getSnapshot();
        Assert.assertEquals(percentileValue(snapshot, 0.0), 10.0, 1.0);
        Assert.assertEquals(percentileValue(snapshot, 1.0), 40.0, 1.0);
        Assert.assertEquals(snapshot.getMin(), 10.0, 1.0);
        Assert.assertEquals(snapshot.getMax(), 40.0, 1.0);

        currentTime.addAndGet(99);
        Assert.assertEquals(clock.getCurrentTime(), 999);

        histogram.record(9);
        histogram.record(60);

        snapshot = histogram.getSnapshot();
        Assert.assertEquals(percentileValue(snapshot, 0.0), 9.0, 1.0);
        Assert.assertEquals(percentileValue(snapshot, 1.0), 60.0, 1.0);
        Assert.assertEquals(snapshot.getMin(), 9.0, 1.0);
        Assert.assertEquals(snapshot.getMax(), 60.0, 1.0);

        currentTime.addAndGet(1);
        Assert.assertEquals(clock.getCurrentTime(), 1000);

        histogram.record(12);
        histogram.record(70);
        snapshot = histogram.getSnapshot();
        Assert.assertEquals(percentileValue(snapshot, 0.0), 9.0, 1.0);
        Assert.assertEquals(percentileValue(snapshot, 1.0), 70.0, 1.0);
        Assert.assertEquals(snapshot.getMin(), 9.0, 1.0);
        Assert.assertEquals(snapshot.getMax(), 70.0, 1.0);

        currentTime.addAndGet(1001);
        Assert.assertEquals(clock.getCurrentTime(), 2001);

        histogram.record(13);
        histogram.record(80);

        snapshot = histogram.getSnapshot();
        Assert.assertEquals(snapshot.getMin(), 9.0, 1.0);
        Assert.assertEquals(snapshot.getMax(), 80.0, 1.0);
        Assert.assertEquals(percentileValue(snapshot, 0.0), 9.0, 1.0);
        Assert.assertEquals(percentileValue(snapshot, 1.0), 80.0, 1.0);

        currentTime.addAndGet(1000);
        Assert.assertEquals(clock.getCurrentTime(), 3001);

        snapshot = histogram.getSnapshot();
        Assert.assertEquals(percentileValue(snapshot, 0.0), 9.0, 1.0);
        Assert.assertEquals(percentileValue(snapshot, 1.0), 80.0, 1.0);
        Assert.assertEquals(snapshot.getMin(), 9.0, 1.0);
        Assert.assertEquals(snapshot.getMax(), 80.0, 1.0);

        currentTime.addAndGet(999);
        Assert.assertEquals(clock.getCurrentTime(), 4000);

        snapshot = histogram.getSnapshot();
        Assert.assertEquals(percentileValue(snapshot, 0.0), 12.0, 1.0);
        Assert.assertEquals(percentileValue(snapshot, 1.0), 80.0, 1.0);
        Assert.assertEquals(snapshot.getMin(), 12.0, 1.0);
        Assert.assertEquals(snapshot.getMax(), 80.0, 1.0);

        histogram.record(1);
        histogram.record(200);

        snapshot = histogram.getSnapshot();
        Assert.assertEquals(percentileValue(snapshot, 0.0), 1.0, 1.0);
        Assert.assertEquals(percentileValue(snapshot, 1.0), 200.0, 1.0);
        Assert.assertEquals(snapshot.getMin(), 1.0, 1.0);
        Assert.assertEquals(snapshot.getMax(), 200.0, 1.0);

        currentTime.addAndGet(10000);
        Assert.assertEquals(clock.getCurrentTime(), 14000);

        snapshot = histogram.getSnapshot();
        Assert.assertEquals(percentileValue(snapshot, 0.0), 0.0, 0.0);
        Assert.assertEquals(percentileValue(snapshot, 1.0), 0.0, 0.0);
        Assert.assertEquals(snapshot.getMin(), 0.0, 1.0);
        Assert.assertEquals(snapshot.getMax(), 0.0, 1.0);

        histogram.record(3);

        currentTime.addAndGet(3999);
        Assert.assertEquals(clock.getCurrentTime(), 17999);

        snapshot = histogram.getSnapshot();
        Assert.assertEquals(percentileValue(snapshot, 0.0), 3.0, 1.0);
        Assert.assertEquals(percentileValue(snapshot, 1.0), 3.0, 1.0);
        Assert.assertEquals(snapshot.getMin(), 3.0, 1.0);
        Assert.assertEquals(snapshot.getMax(), 3.0, 1.0);

        currentTime.addAndGet(1);
        Assert.assertEquals(clock.getCurrentTime(), 18000);

        snapshot = histogram.getSnapshot();
        Assert.assertEquals(percentileValue(snapshot, 0.0), 0.0, 0.0);
        Assert.assertEquals(percentileValue(snapshot, 1.0), 0.0, 0.0);
        Assert.assertEquals(snapshot.getMin(), 0.0, 1.0);
        Assert.assertEquals(snapshot.getMax(), 0.0, 1.0);
    }

    private double percentileValue(Snapshot snapshot, double p) {
        for (PercentileValue percentileValue : snapshot.getPercentileValues()) {
            if (percentileValue.getPercentile() == p) {
                return percentileValue.getValue();
            }
        }
        return Double.NaN;
    }

    @Test
    public void testPercentiles() {
        double[] percentiles = new double[]{0.5, 0.9, 0.95};
        RollingHistogram histogram = new RollingHistogram(Clock.DEFAULT, StatisticConfig.builder()
                .percentiles(percentiles)
                .percentilePrecision(5)
                .expiry(Duration.ofDays(1)).buckets(3).build());

        for (int i = 1; i <= 10; i++) {
            histogram.record(i * 1_000.0);
        }

        Snapshot snapshot = histogram.getSnapshot();
        PercentileValue[] percentileValues = snapshot.getPercentileValues();
        double[] expected = new double[]{5e3D, 9e3D, 10e3D};
        for (int i = 0; i < percentiles.length; i++) {
            Assert.assertEquals(percentileValues[i].getPercentile(), percentiles[i]);
            Assert.assertEquals(percentileValues[i].getValue(), expected[i], 0.5);
        }
    }

    @Test
    public void percentilesWithNoSamples() {
        double[] percentiles = new double[]{0.5, 0.9, 0.95};
        RollingHistogram histogram = new RollingHistogram(Clock.DEFAULT, StatisticConfig.builder()
                .percentiles(percentiles)
                .expiry(Duration.ofDays(1)).buckets(3).build());
        Snapshot snapshot = histogram.getSnapshot();
        PercentileValue[] percentileValues = snapshot.getPercentileValues();
        for (int i = 0; i < percentiles.length; i++) {
            Assert.assertEquals(percentileValues[i].getPercentile(), percentiles[i]);
            Assert.assertEquals(percentileValues[i].getValue(), 0.0);
        }
    }

}
