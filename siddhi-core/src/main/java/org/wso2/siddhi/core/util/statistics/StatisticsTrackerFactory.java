package org.wso2.siddhi.core.util.statistics;

public interface StatisticsTrackerFactory {

    LatencyTracker createLatencyTracker(String name, StatisticsManager statisticsManager);

    ThroughputTracker createThroughputTracker(String name, StatisticsManager statisticsManager);

    MemoryUsageTracker createMemoryUsageTracker(StatisticsManager statisticsManager);

    StatisticsManager createStatisticsManager();

}
