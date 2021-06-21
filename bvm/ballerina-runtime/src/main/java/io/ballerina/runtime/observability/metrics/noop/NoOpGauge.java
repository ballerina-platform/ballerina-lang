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
package io.ballerina.runtime.observability.metrics.noop;

import io.ballerina.runtime.observability.metrics.AbstractMetric;
import io.ballerina.runtime.observability.metrics.Gauge;
import io.ballerina.runtime.observability.metrics.MetricId;
import io.ballerina.runtime.observability.metrics.Snapshot;
import io.ballerina.runtime.observability.metrics.StatisticConfig;

/**
 * Implementation of No-Op {@link Gauge}.
 */
public class NoOpGauge extends AbstractMetric implements Gauge {

    public NoOpGauge(MetricId id) {
        super(id);
    }


    @Override
    public void increment(double amount) {
        // Do nothing
    }

    @Override
    public void decrement(double amount) {
        // Do nothing
    }

    @Override
    public void setValue(double value) {
        // Do nothing
    }

    @Override
    public double getValue() {
        return 0;
    }

    @Override
    public long getCount() {
        return 0;
    }

    @Override
    public double getSum() {
        return 0;
    }

    @Override
    public Snapshot[] getSnapshots() {
        return new Snapshot[0];
    }

    @Override
    public StatisticConfig[] getStatisticsConfig() {
        return new StatisticConfig[0];
    }
}
