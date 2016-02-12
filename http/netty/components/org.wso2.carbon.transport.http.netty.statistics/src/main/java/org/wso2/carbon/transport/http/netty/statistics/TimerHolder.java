package org.wso2.carbon.transport.http.netty.statistics;

import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.MetricManager;
import org.wso2.carbon.metrics.manager.Timer;

/**
 * Initialize all the timers
 */
public class TimerHolder {

    private final Timer connectionTimer;
    private final Timer responseLifeTimer;
    private final Timer requestLifeTimer;
    private final Timer requestBodyReadTimer;
    private final Timer requestHeaderReadTimer;
    private final Timer requestBodyWriteTimer;
    private final Timer responseHeaderReadTimer;
    private final Timer responseBodyReadTimer;
    private static volatile TimerHolder timerHandler;

    public TimerHolder() {
        // Initialize request metrics holder Timers
        //requestLifeTimer = MetricManager.timer(MetricManager.
        //      name("org.wso2.carbon.transport", "request.life.time"), Level.INFO);
        connectionTimer = MetricManager.timer(MetricManager.
                name(ConnectionMetricsStaticsHolder.class, "connection.timer"), Level.INFO);
        requestLifeTimer = MetricManager.timer(MetricManager.
                name(RequestMetricsStaticsHolder.class, "request.life.time"), Level.INFO);
        requestBodyReadTimer = MetricManager.timer(MetricManager.
                name(RequestMetricsStaticsHolder.class, "request.body.read.time"), Level.INFO);
        requestHeaderReadTimer = MetricManager.timer(MetricManager.
                name(RequestMetricsStaticsHolder.class, "request.header.read.time"), Level.INFO);
        requestBodyWriteTimer = MetricManager.timer(MetricManager.
                name(RequestMetricsStaticsHolder.class, "request.body.write.timer"), Level.INFO);
        //response metrics holder timers
        responseLifeTimer = MetricManager.timer(MetricManager.
                name(ResponseMetricsStaticsHolder.class, "response.life.time"), Level.INFO);
        responseHeaderReadTimer = MetricManager.timer(MetricManager.
                name(ResponseMetricsStaticsHolder.class, "response.header.read.time"), Level.INFO);
        responseBodyReadTimer = MetricManager.timer(MetricManager.
                name(ResponseMetricsStaticsHolder.class, "response.body.read.time"), Level.INFO);
    }

    public static TimerHolder getInstance() {
        if (timerHandler == null) {
            timerHandler = new TimerHolder();
        }
        return timerHandler;
    }

    //TODO send this to constructor,,, check other holders
    public void initTimer() {

    }

    public Timer getConnectionTimer() {
        return connectionTimer;
    }

    public Timer getRequestLifeTimer() {
        return requestLifeTimer;
    }

    public Timer getRequestBodyReadTimer() {
        return requestBodyReadTimer;
    }

    public Timer getRequestHeaderReadTimer() {
        return requestHeaderReadTimer;
    }

    public Timer getRequestBodyWriteTimer() {
        return requestBodyWriteTimer;
    }

    public Timer getResponseLifeTimer() {
        return responseLifeTimer;
    }

    public Timer getResponseHeaderReadTimer() {
        return responseHeaderReadTimer;
    }

    public Timer getResponseBodyReadTimer() {
        return responseBodyReadTimer;
    }

}
