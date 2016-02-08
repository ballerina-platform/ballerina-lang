package org.wso2.carbon.transport.http.netty.latency.metrics;

import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.MetricManager;
import org.wso2.carbon.metrics.manager.Timer;

/**
 * Created by chamile on 2/3/16.
 */
public class TimerHandler {
    private final Timer responseLifeTimer;
    private final Timer requestLifeTimer;
    private final Timer requestBodyReadTimer;
    private final Timer requestHeaderReadTimer;
    private final Timer requestBodyWriteTimer;
    private final Timer responseHeaderReadTimer;
    private final Timer responseBodyReadTimer;
    private static volatile TimerHandler timerHandler;

    public TimerHandler() {
        //TODO rename metrics name

        // Initialize request metrics holder Timers
        //requestLifeTimer = MetricManager.timer(MetricManager.
        //      name("org.wso2.carbon.transport", "request.life.time"), Level.INFO);
        requestLifeTimer = MetricManager.timer(MetricManager.
                name(RequestMetricsHolder.class, "request.life.time"), Level.INFO);
        requestBodyReadTimer = MetricManager.timer(MetricManager.
                name(RequestMetricsHolder.class, "request.body.read.time"), Level.INFO);
        requestHeaderReadTimer = MetricManager.timer(MetricManager.
                name(RequestMetricsHolder.class, "request.header.read.time"), Level.INFO);
        requestBodyWriteTimer = MetricManager.timer(MetricManager.
                name(RequestMetricsHolder.class, "request.body.write.timer"), Level.INFO);
        //response metrics holder timers
        responseLifeTimer = MetricManager.timer(MetricManager.
                name(ResponseMetricsHolder.class, "response.life.time"), Level.INFO);
        responseHeaderReadTimer = MetricManager.timer(MetricManager.
                name(ResponseMetricsHolder.class, "response.header.read.time"), Level.INFO);
        responseBodyReadTimer = MetricManager.timer(MetricManager.
                name(ResponseMetricsHolder.class, "response.body.read.time"), Level.INFO);
    }

    public static TimerHandler getInstance() {
        if (timerHandler == null) {
            timerHandler = new TimerHandler();
        }
        return timerHandler;
    }

    //TODO send this to constructor,,, check other holders
    public void initTimer() {

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

    /*public Timer getConnectionTimer() {
        return connectionTimer;
    }*/
}
