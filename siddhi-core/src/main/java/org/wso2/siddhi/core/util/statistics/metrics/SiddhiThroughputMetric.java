package org.wso2.siddhi.core.util.statistics.metrics;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import org.wso2.siddhi.core.util.statistics.ThroughputTracker;

public class SiddhiThroughputMetric implements ThroughputTracker {
    private Meter eventMeter = null;
    private String name;

    public SiddhiThroughputMetric(String name, MetricRegistry metricRegistry){
        this.name = name + ".throughput";
        eventMeter = metricRegistry.meter(this.name);
    }

    /**
     * This method is to notify receive of events to calculate the throughput
     */
    @Override
    public void eventIn() {
        eventMeter.mark();
    }

    /**
     * This method is to notify receive of events to calculate the throughput
     *
     * @param eventCount events passed through
     */
    @Override
    public void eventsIn(int eventCount) {
        eventMeter.mark(eventCount);
    }

    /**
     * @return Name of the memory usage tracker
     */
    @Override
    public String getName() {
        return name;
    }

}
