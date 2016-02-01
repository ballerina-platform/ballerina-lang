package org.wso2.siddhi.core.util.statistics.metrics;

import com.codahale.metrics.Reporter;
import org.wso2.siddhi.core.util.statistics.*;
import org.wso2.siddhi.query.api.annotation.Element;

import java.util.List;


public class SiddhiMetricsFactory implements StatisticsTrackerFactory {

    public LatencyTracker createLatencyTracker(String name, StatisticsManager statisticsManager) {
        return new SiddhiLatencyMetric(name, statisticsManager.getRegistry());
    }

    public ThroughputTracker createThroughputTracker(String name, StatisticsManager statisticsManager) {
        return new SiddhiThroughputMetric(name, statisticsManager.getRegistry());
    }

    public MemoryUsageTracker createMemoryUsageTracker(StatisticsManager statisticsManager){
        return new SiddhiMemoryUsageMetric(statisticsManager.getRegistry());
    }

    @Override
    public StatisticsManager createStatisticsManager(List<Element> elements) {
        return new SiddhiStatisticsManager(elements);
    }

}
