package org.wso2.siddhi.core.util.statistics;

import com.codahale.metrics.*;

/**
 * Each execution plan will have one StatisticsManager in execution plan context
 */
public interface StatisticsManager {

    void startReporting();

    void stopReporting();

    MetricRegistry getRegistry();

    void cleanup();
}
