package org.ballerina.testobserve.metrics.extension.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for holding all the metrics types;
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
