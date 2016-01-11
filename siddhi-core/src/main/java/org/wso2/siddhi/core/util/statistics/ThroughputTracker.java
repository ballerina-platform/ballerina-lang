package org.wso2.siddhi.core.util.statistics;

/**
 * This interface will have the necessary methods to calculate the throughput.
 */
public interface ThroughputTracker {
    /**
     * This method is to notify receive of an event to calculate the throughput
     */
    void eventIn();

    /**
     * This method is to notify receive of multiple events to calculate the throughput
     * @param eventCount number of events passing through
     */
    void eventsIn(int eventCount);

    /**
     * @return Name of the memory usage tracker
     */
    String getName();

}
