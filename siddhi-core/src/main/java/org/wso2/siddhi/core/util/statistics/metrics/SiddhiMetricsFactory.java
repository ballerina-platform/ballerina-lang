package org.wso2.siddhi.core.util.statistics.metrics;

import org.wso2.siddhi.core.util.statistics.LatencyTracker;
import org.wso2.siddhi.core.util.statistics.MemoryUsageTracker;
import org.wso2.siddhi.core.util.statistics.StatisticsTrackerFactory;
import org.wso2.siddhi.core.util.statistics.ThroughputTracker;


public class SiddhiMetricsFactory implements StatisticsTrackerFactory{

    public LatencyTracker createLatencyTracker(String name){
        return new SiddhiLatencyMetric(name);
    }

    public ThroughputTracker createThroughputTracker(String name){
        return new SiddhiThroughputMetric(name);
    }

    public MemoryUsageTracker createMemoryUsageTracker(){
        return new SiddhiMemoryUsageMetric();
    }
}
