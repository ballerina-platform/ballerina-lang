package org.wso2.carbon.transport.http.netty.statistics.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.messaging.MessagingHandler;
import org.wso2.carbon.transport.http.netty.statistics.MetricReporter;
import org.wso2.carbon.transport.http.netty.statistics.Metrics;
import org.wso2.carbon.transport.http.netty.statistics.StatisticsHandler;
import org.wso2.carbon.transport.http.netty.statistics.TimerHolder;

/**
 * Created by chamile on 2/11/16.
 */
public class NettyTransportStatActivator implements BundleActivator {

    @Override public void start(BundleContext bundleContext) throws Exception {
        Metrics.init(MetricReporter.JMX);
        bundleContext.registerService(MessagingHandler.class, new StatisticsHandler(TimerHolder.getInstance()), null);
    }

    @Override public void stop(BundleContext bundleContext) throws Exception {

    }
}
