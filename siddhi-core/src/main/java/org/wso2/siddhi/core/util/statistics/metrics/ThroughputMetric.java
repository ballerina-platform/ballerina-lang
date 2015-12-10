package org.wso2.siddhi.core.util.statistics.metrics;

import com.codahale.metrics.Meter;
import org.wso2.siddhi.core.util.statistics.ThroughputTracker;

/**
 * Created by sajith on 11/20/15.
 */
public class ThroughputMetric implements ThroughputTracker {
    private final Meter eventMarker;

    public ThroughputMetric(String streamName){
        eventMarker = MetricRegistryHolder.getMetricRegistry().meter(streamName);
    }
    /**
     * This method is to notify receive of events to calculate the throughput
     */
    @Override
    public void eventIn() {
        eventMarker.mark();
    }

    /**
     * This method is to notify receive of events to calculate the throughput
     *
     * @param eventCount
     */
    @Override
    public void eventsIn(int eventCount) {
        eventMarker.mark(eventCount);
    }

    /**
     * @return Name of the memory usage tracker
     */
    @Override
    public String getName() {
        return null;
    }
}
