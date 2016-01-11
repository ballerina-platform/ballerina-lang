package org.wso2.siddhi.core.util.statistics;

public interface StatisticsTrackerFactory {

    LatencyTracker createLatencyTracker(String name);

    ThroughputTracker createThroughputTracker(String name);

    MemoryUsageTracker createMemoryUsageTracker();
}
