package org.wso2.siddhi.core.util.statistics.metrics;

import org.wso2.siddhi.core.util.statistics.LatencyTracker;
import org.wso2.siddhi.core.util.statistics.MemoryUsageTracker;
import org.wso2.siddhi.core.util.statistics.StatisticsTrackerFactory;
import org.wso2.siddhi.core.util.statistics.ThroughputTracker;

/**
 * Created by sajith on 11/23/15.
 */
public class MetricsFactory implements StatisticsTrackerFactory{

    public LatencyTracker createLatencyTracker(String name){
        return new LatencyMetric(name);
    }

    public ThroughputTracker createThroughputTracker(String name){
        return new ThroughputMetric(name);
    }

    public MemoryUsageTracker createMemoryUsageTracker(){
        return new MemoryUsageMetric();
    }
}
