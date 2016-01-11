package org.wso2.siddhi.core.util.statistics.metrics;

import com.codahale.metrics.*;

import java.util.concurrent.TimeUnit;

/**
 * Each execution plan will have one metric registry and placed in execution plan context
 */
public class MetricManager {
    private MetricRegistry metricRegistry = new MetricRegistry();
    private Class<?> reporterType;
    private Reporter reporter;

    public MetricManager(Class<?> reporterType) {
        this.reporterType = reporterType;
    }

    public MetricRegistry getRegistry() {
        return metricRegistry;
    }

    public void startReporting() {
        if (reporterType == ConsoleReporter.class) {
            reporter = ConsoleReporter.forRegistry(metricRegistry)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .build();
            ((ConsoleReporter) reporter).start(60, TimeUnit.SECONDS);
        } else if (reporterType == JmxReporter.class) {
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
}
