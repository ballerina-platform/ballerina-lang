package org.wso2.siddhi.core.util.statistics.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.wso2.siddhi.core.util.statistics.LatencyTracker;

public class SiddhiLatencyMetric implements LatencyTracker {
    // Using thread local variables to keep the timer track the time of the same execution path by different threads.
    private ThreadLocal<Timer> execLatencyTimer;
    private ThreadLocal<Timer.Context> context;
    private String metricName;

    public SiddhiLatencyMetric(String name, final MetricRegistry metricRegistry){
        this.metricName = name + ".latency";
        execLatencyTimer = new ThreadLocal<Timer>(){
            protected Timer initialValue() {
                return metricRegistry.timer(metricName);
            }
        };
        context = new ThreadLocal<Timer.Context>(){
            protected Timer.Context initialValue() {
                return null;
            }
        };
    }

    /**
     * This is called when the processing of the event is started. This is
     * called at ProcessStreamReceiver#receive before the event is passed into
     * process chain
     */
    public void markIn() {
        if (context.get() != null){
            throw new IllegalStateException("MarkIn consecutively called without calling markOut in " + metricName);
        }
        context.set(execLatencyTimer.get().time());
    }

    /**
     * This is called to when the processing of an event is finished. This is called at
     * two places,
     * 1. OutputRateLimiter#sendToCallBacks - When the event is processed and by the full chain and emitted out
     * 2. ProcessStreamReceiver#receive - When event is not processed by full process chain(e.g. Filtered out by a filter)
     */
    @Override
    public void markOut() {
        if (context.get() != null){
            context.get().stop();
            context.set(null);
        }
    }

    /**
     * @return Name of the latency tracker
     */
    @Override
    public String getName() {
        return metricName;
    }
}
