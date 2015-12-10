package org.wso2.siddhi.core.util.statistics.metrics;

import com.codahale.metrics.Meter;
import org.wso2.siddhi.core.util.statistics.ThroughputTracker;

/**
 * Created by sajith on 11/20/15.
 */
public class ThroughputMetric implements ThroughputTracker {
    private Meter eventMeter = null;
    private String name;

    public ThroughputMetric(String name){
        this.name = name + ".throughput";
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
     * @param eventCount
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


    public void init(MetricRegistryHolder registryHolder){
        eventMeter = registryHolder.getRegistry().meter(this.name);
    }
}
