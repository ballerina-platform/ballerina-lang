package org.wso2.carbon.transport.http.netty.latency.metrics;

import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.MetricManager;
import org.wso2.carbon.metrics.manager.MetricService;
import org.wso2.carbon.metrics.manager.Timer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chamile on 2/9/16.
 */
public class ConnectionMetricsStaticsHolder implements MetricsStaticsHolder {

    private String type, timer = null;
    private Timer connectionTimer = null;
    private Timer.Context context = null;
    private MetricService metricService = null;
    private Map<String, Long> connectionStatics;
    private long duration;

    public ConnectionMetricsStaticsHolder(String type, String timer) {
        //get the type of holder
        this.type = type;
        this.timer = timer;
        //Initialize connection metrics holder Timers
        connectionTimer = MetricManager.timer(MetricManager.
                name(ConnectionMetricsHolder.class, "connection.timer"), Level.INFO);
    }

    @Override
    public boolean startTimer(String timer) {
        this.context = this.connectionTimer.start();
        return false;
    }

    @Override
    public boolean stopTimer(String timer) {
        if (this.context != null) {
            duration = this.context.stop();
            setStatics(timer, duration);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Map<String, Long> getStatics(String timer) {
        return null;
    }

    @Override
    public void setStatics(String timer, Long duration) {
        if (connectionStatics == null) {
            this.connectionStatics = new HashMap<String, Long>();
        }
        connectionStatics.put(timer, duration);
    }

    @Override
    public String getType() {
        return type;
    }
}
