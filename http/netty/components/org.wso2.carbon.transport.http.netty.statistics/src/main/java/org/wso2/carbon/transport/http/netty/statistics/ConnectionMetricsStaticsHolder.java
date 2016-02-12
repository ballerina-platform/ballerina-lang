package org.wso2.carbon.transport.http.netty.statistics;

import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.MetricManager;
/*import org.wso2.carbon.metrics.manager.MetricService;*/
import org.wso2.carbon.metrics.manager.Timer;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds the Connection related Latency Metrics
 */
public class ConnectionMetricsStaticsHolder implements MetricsStaticsHolder {

    private String type = null;
    private String timer = null;
    private Timer connectionTimer = null;
    private Timer.Context context = null;
    //private MetricService metricService = null;
    private Map<String, Long> connectionStatics;
    private long duration;

    public ConnectionMetricsStaticsHolder(String type, String timer, TimerHolder timerHolder) {
        this.type = type;
        this.timer = timer;
        this.connectionTimer = timerHolder.getRequestLifeTimer();
    }

    @Override public boolean startTimer(String timer) {
        this.context = this.connectionTimer.start();
        return false;
    }

    @Override public boolean stopTimer(String timer) {
        if (this.context != null) {
            setStatics(timer, this.context.stop());
            return true;
        } else {
            return false;
        }
    }

    @Override public Map<String, Long> getStatics(String timer) {
        return null;
    }

    @Override public void setStatics(String timer, Long duration) {
        if (connectionStatics == null) {
            this.connectionStatics = new HashMap<String, Long>();
        }
        connectionStatics.put(timer, duration);
    }

    @Override public String getType() {
        return type;
    }
}
