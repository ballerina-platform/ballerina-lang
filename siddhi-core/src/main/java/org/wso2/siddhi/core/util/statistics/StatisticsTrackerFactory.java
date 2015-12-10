package org.wso2.siddhi.core.util.statistics;

/**
 * Created by sajith on 12/7/15.
 */
public interface StatisticsTrackerFactory {

    LatencyTracker createLatencyTracker(String name);

    ThroughputTracker createThroughputTracker(String name);

    MemoryUsageTracker createMemoryUsageTracker();
}
