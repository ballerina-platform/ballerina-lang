package org.wso2.siddhi.core.util.statistics.metrics;

import org.wso2.siddhi.core.util.statistics.LatencyTracker;
import org.wso2.siddhi.core.util.statistics.MemoryUsageTracker;
import org.wso2.siddhi.core.util.statistics.StatisticsTrackerFactory;
import org.wso2.siddhi.core.util.statistics.ThroughputTracker;

/**
 * Created by sajith on 11/23/15.
 */
public class MetricsFactory implements StatisticsTrackerFactory{

    public LatencyTracker createLatencyTracker(String name, MetricRegistryHolder registryHolder){
        return new LatencyMetric(name, registryHolder);
    }

    public ThroughputTracker createThroughputTracker(String name, MetricRegistryHolder registryHolder){
        return new ThroughputMetric(name, registryHolder);
    }

    public MemoryUsageTracker createMemoryUsageTracker(MetricRegistryHolder registryHolder){
        return new MemoryUsageMetric(registryHolder);
    }
}
