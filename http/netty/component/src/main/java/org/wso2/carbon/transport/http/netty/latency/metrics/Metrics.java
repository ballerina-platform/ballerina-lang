package org.wso2.carbon.transport.http.netty.latency.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.impl.MetricServiceImpl;
import org.wso2.carbon.metrics.impl.MetricsLevelConfigException;
import org.wso2.carbon.metrics.impl.MetricsLevelConfiguration;
import org.wso2.carbon.metrics.impl.util.ConsoleReporterBuilder;
import org.wso2.carbon.metrics.impl.util.DASReporterBuilder;
import org.wso2.carbon.metrics.impl.util.JmxReporterBuilder;
import org.wso2.carbon.metrics.manager.MetricManager;
import org.wso2.carbon.metrics.manager.MetricService;

/**
 * Created by nadeeshaan on 1/18/16.
 */
public final class Metrics {
    private static final Logger logger = LoggerFactory.getLogger(Metrics.class);

    private static MetricService metricService;

    private Metrics() {
    }

    public static synchronized void init(MetricReporter... metricReporters) {
        if (metricService != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Metric Service is already initialized");
            }
            return;
        }
        if (logger.isInfoEnabled()) {
            logger.info("Initializing Metrics Service");
        }
        MetricsEnvConfiguration metricsEnvConfiguration = new MetricsEnvConfiguration();
        MetricsLevelConfiguration metricsLevelConfiguration = new MetricsLevelConfiguration();
        try {
            metricsLevelConfiguration.loadFromSystemPropertyFile();
        } catch (MetricsLevelConfigException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Error loading metrics level configuration", e);
            }
        }
        MetricServiceImpl.Builder builder = new MetricServiceImpl.Builder().setEnabled(true)
                .setRootLevel(org.wso2.carbon.metrics.manager.Level.INFO);
        for (MetricReporter metricReporter : metricReporters) {
            switch (metricReporter) {
                case CONSOLE:
                    builder.addReporterBuilder(
                            new ConsoleReporterBuilder().setEnabled(true).configure(metricsEnvConfiguration));
                    break;
                case DAS:
                    builder.addReporterBuilder(
                            new DASReporterBuilder().setEnabled(true).configure(metricsEnvConfiguration));
                    break;
                case JMX:
                    builder.addReporterBuilder(
                            new JmxReporterBuilder().setEnabled(true).configure(metricsEnvConfiguration));
                    break;
                default:
                    break;

            }
        }
        metricService = builder.build(metricsLevelConfiguration);
        //ServiceReferenceHolder.getInstance().setMetricService(metricService);
        MetricManager.registerMXBean();
    }

    static synchronized void destroy() {
        if (metricService != null) {
            MetricManager.unregisterMXBean();
            metricService.disable();
            metricService = null;
        }
        //ServiceReferenceHolder.getInstance().setMetricService(null);
    }
}
