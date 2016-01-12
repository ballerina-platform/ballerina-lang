package org.wso2.siddhi.core.util.statistics;

import org.wso2.siddhi.query.api.annotation.Element;

import java.util.List;

public interface StatisticsTrackerFactory {

    LatencyTracker createLatencyTracker(String name, StatisticsManager statisticsManager);

    ThroughputTracker createThroughputTracker(String name, StatisticsManager statisticsManager);

    MemoryUsageTracker createMemoryUsageTracker(StatisticsManager statisticsManager);

    StatisticsManager createStatisticsManager(List<Element> elements);

}
