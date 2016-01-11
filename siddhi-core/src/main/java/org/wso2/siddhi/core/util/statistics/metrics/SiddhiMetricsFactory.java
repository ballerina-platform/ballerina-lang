package org.wso2.siddhi.core.util.statistics.metrics;

import com.codahale.metrics.Reporter;
import org.wso2.siddhi.core.util.statistics.*;


public class SiddhiMetricsFactory implements StatisticsTrackerFactory {
    Class<? extends Reporter > reporterType;

    public SiddhiMetricsFactory(Class<? extends Reporter > reporterType) {
        this.reporterType = reporterType;
    }

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
    public StatisticsManager createStatisticsManager() {
        return new SiddhiStatisticsManager(reporterType);
    }

}
