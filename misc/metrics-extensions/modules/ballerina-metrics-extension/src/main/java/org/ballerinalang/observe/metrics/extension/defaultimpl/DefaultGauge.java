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

import io.ballerina.runtime.observability.metrics.AbstractMetric;
import io.ballerina.runtime.observability.metrics.Gauge;
import io.ballerina.runtime.observability.metrics.MetricId;
import io.ballerina.runtime.observability.metrics.Snapshot;
import io.ballerina.runtime.observability.metrics.StatisticConfig;

import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

/**
 * An implementation of {@link Gauge}.
 *
 * @since 0.980.0
 */
public class DefaultGauge extends AbstractMetric implements Gauge {

    private static final RollingHistogram[] EMPTY_ROLLING_HISTOGRAMS = new RollingHistogram[0];
    private static final Snapshot[] EMPTY_SNAPSHOTS = new Snapshot[0];

    private final LongAdder count = new LongAdder();
    private final DoubleAdder sum = new DoubleAdder();
    private final DoubleAdder value = new DoubleAdder();
    private final RollingHistogram[] rollingHistograms;

    private DefaultGauge(MetricId id, Clock clock, StatisticConfig... statisticConfigs) {
        super(id);
        if (statisticConfigs != null) {
            rollingHistograms = new RollingHistogram[statisticConfigs.length];
            for (int i = 0; i < statisticConfigs.length; i++) {
                rollingHistograms[i] = new RollingHistogram(clock, statisticConfigs[i]);
            }
        } else {
            rollingHistograms = EMPTY_ROLLING_HISTOGRAMS;
        }
    }

    DefaultGauge(MetricId id, StatisticConfig... statisticConfigs) {
        this(id, Clock.DEFAULT, statisticConfigs);
    }


    private void updateHistogram(double value) {
        count.increment();
        sum.add(value);
        for (RollingHistogram rollingHistogram : rollingHistograms) {
            rollingHistogram.record(value);
        }
    }

    @Override
    public void increment(double amount) {
        synchronized (this) {
            value.add(amount);
            updateHistogram(value.sum());
        }
    }

    @Override
    public void decrement(double amount) {
        synchronized (this) {
            value.add(-amount);
            updateHistogram(value.sum());
        }
    }

    @Override
    public void setValue(double value) {
        synchronized (this) {
            this.value.reset();
            this.value.add(value);
            updateHistogram(value);
        }
    }

    @Override
    public double getValue() {
        synchronized (this) {
            return value.sum();
        }
    }

    @Override
    public long getCount() {
        return count.sum();
    }

    @Override
    public double getSum() {
        return sum.sum();
    }

    @Override
    public Snapshot[] getSnapshots() {
        if (rollingHistograms.length == 0) {
            return EMPTY_SNAPSHOTS;
        }
        Snapshot[] snapshots = new Snapshot[rollingHistograms.length];
        for (int i = 0; i < rollingHistograms.length; i++) {
            snapshots[i] = rollingHistograms[i].getSnapshot();
        }
        return snapshots;
    }

    @Override
    public StatisticConfig[] getStatisticsConfig() {
        StatisticConfig[] configs = new StatisticConfig[this.rollingHistograms.length];
        int index = 0;
        for (RollingHistogram rollingHistogram : this.rollingHistograms) {
            configs[index] = rollingHistogram.getStatisticConfig();
            index++;
        }
        return configs;
    }
}
