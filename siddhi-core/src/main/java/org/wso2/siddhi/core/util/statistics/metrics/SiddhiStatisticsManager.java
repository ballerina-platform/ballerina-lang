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

package org.wso2.siddhi.core.util.statistics.metrics;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Reporter;
import com.codahale.metrics.ScheduledReporter;
import org.wso2.siddhi.core.util.statistics.StatisticsManager;
import org.wso2.siddhi.query.api.annotation.Element;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Each siddhi app will have one metric registry and placed in siddhi app context
 */
public class SiddhiStatisticsManager implements StatisticsManager {
    private MetricRegistry metricRegistry = new MetricRegistry();
    private Reporter reporter;
    private String reporterName = "console";
    private int interval = 60;

    public SiddhiStatisticsManager(List<Element> elements) {
        for (Element element : elements) {
            if ("reporter".equals(element.getKey())) {
                reporterName = element.getValue();
            } else if ("interval".equals(element.getKey())) {
                interval = Integer.parseInt(element.getValue());
            }
        }
    }

    public MetricRegistry getRegistry() {
        return metricRegistry;
    }

    public void startReporting() {
        if (reporterName.equalsIgnoreCase("console")) {
            reporter = ConsoleReporter.forRegistry(metricRegistry)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .build();
            ((ConsoleReporter) reporter).start(interval, TimeUnit.SECONDS);
        } else if (reporterName.equalsIgnoreCase("jmx")) {
            reporter = JmxReporter.forRegistry(metricRegistry)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .build();
            ((JmxReporter) reporter).start();
        } else {
            throw new UnsupportedOperationException("Only 'ConsoleReporter' and 'JmxReporter' is supported, Reporter " +
                    "type '" + reporter.getClass().getName() + "' is not supported");
        }
    }

    public void stopReporting() {
        if (reporter instanceof ScheduledReporter) {
            ((ScheduledReporter) reporter).stop();
        } else if (reporter instanceof JmxReporter) {
            ((JmxReporter) reporter).stop();
        } else {
            throw new UnsupportedOperationException("Only 'ConsoleReporter' and 'JmxReporter' is supported, Reporter " +
                    "type '" + reporter.getClass().getName() + "' is not supported");
        }
    }

    @Override
    public void cleanup() {

    }
}
