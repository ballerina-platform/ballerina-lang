/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerina.testobserve.metrics.extension.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for holding all the metrics types.
 */
public class Metrics {
    List<MockCounter> counters;
    List<MockGauge> gauges;
    List<MockPolledGauge> polledGauges;

    public Metrics() {
        counters = new ArrayList<>();
        gauges = new ArrayList<>();
        polledGauges = new ArrayList<>();
    }

    public List<MockCounter> getCounters() {
        return counters;
    }

    public void addCounter(MockCounter counter) {
        this.counters.add(counter);
    }

    public void addAllCounters(List<MockCounter> counters) {
        this.counters.addAll(counters);
    }

    public List<MockGauge> getGauges() {
        return gauges;
    }

    public void addGauge(MockGauge gauge) {
        this.gauges.add(gauge);
    }

    public void addAllGauges(List<MockGauge> gauges) {
        this.gauges.addAll(gauges);
    }

    public List<MockPolledGauge> getPolledGauges() {
        return polledGauges;
    }

    public void addPolledGauge(MockPolledGauge polledGauge) {
        this.polledGauges.add(polledGauge);
    }

    public void addAllPolledGauges(List<MockPolledGauge> polledGauges) {
        this.polledGauges.addAll(polledGauges);
    }
}
