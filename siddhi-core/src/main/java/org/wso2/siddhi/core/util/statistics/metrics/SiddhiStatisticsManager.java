package org.wso2.siddhi.core.util.statistics.metrics;

import com.codahale.metrics.*;
import org.wso2.siddhi.core.util.statistics.StatisticsManager;
import org.wso2.siddhi.query.api.annotation.Element;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Each execution plan will have one metric registry and placed in execution plan context
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
            throw new UnsupportedOperationException("Only 'ConsoleReporter' and 'JmxReporter' is supported, Reporter type '" + reporter.getClass().getName() + "' is not supported");
        }
    }

    public void stopReporting() {
        if (reporter instanceof ScheduledReporter) {
            ((ScheduledReporter) reporter).stop();
        } else if (reporter instanceof JmxReporter) {
            ((JmxReporter) reporter).stop();
        } else {
            throw new UnsupportedOperationException("Only 'ConsoleReporter' and 'JmxReporter' is supported, Reporter type '" + reporter.getClass().getName() + "' is not supported");
        }
    }

    @Override
    public void cleanup() {

    }
}
