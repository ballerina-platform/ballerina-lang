package org.wso2.siddhi.core.util.statistics;

import com.codahale.metrics.*;

/**
 * Each execution plan will have one StatisticsManager in execution plan context
 */
public interface StatisticsManager {

    public void startReporting();

    public void stopReporting();

    public MetricRegistry getRegistry();

    public  void cleanup();
}
