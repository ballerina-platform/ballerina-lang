/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.siddhi.core.util.statistics.metrics;

import org.ballerinalang.siddhi.core.util.statistics.BufferedEventsTracker;
import org.ballerinalang.siddhi.core.util.statistics.LatencyTracker;
import org.ballerinalang.siddhi.core.util.statistics.MemoryUsageTracker;
import org.ballerinalang.siddhi.core.util.statistics.StatisticsManager;
import org.ballerinalang.siddhi.core.util.statistics.StatisticsTrackerFactory;
import org.ballerinalang.siddhi.core.util.statistics.ThroughputTracker;
import org.ballerinalang.siddhi.query.api.annotation.Element;

import java.util.List;

/**
 * Factory class to create Trackers and Managers.
 */
public class SiddhiMetricsFactory implements StatisticsTrackerFactory {

    public LatencyTracker createLatencyTracker(String name, StatisticsManager statisticsManager) {
        return new SiddhiLatencyMetric(name, ((SiddhiStatisticsManager) statisticsManager).getRegistry());
    }

    public ThroughputTracker createThroughputTracker(String name, StatisticsManager statisticsManager) {
        return new SiddhiThroughputMetric(name, ((SiddhiStatisticsManager) statisticsManager).getRegistry());
    }

    public BufferedEventsTracker createBufferSizeTracker(StatisticsManager statisticsManager) {
        return new SiddhiBufferedEventsMetric(((SiddhiStatisticsManager) statisticsManager).getRegistry());
    }

    public MemoryUsageTracker createMemoryUsageTracker(StatisticsManager statisticsManager) {
        return new SiddhiMemoryUsageMetric(((SiddhiStatisticsManager) statisticsManager).getRegistry());
    }

    @Override
    public StatisticsManager createStatisticsManager(String prefix, String siddhiAppName, List<Element> elements) {
        return new SiddhiStatisticsManager(elements);
    }

}
