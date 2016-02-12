package org.wso2.carbon.transport.http.netty.statistics;

import org.wso2.carbon.metrics.manager.Timer;
import org.wso2.carbon.transport.http.netty.common.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds the request metrics parameters
 */
public class RequestMetricsStaticsHolder implements MetricsStaticsHolder {

    private String type;
    private Map<String, Long> requestMetricsStatics;

    // Carbon-Metrics Measuring parameters
    private Timer requestLifeTimer = null;
    private Timer requestHeaderReadTimer = null;
    private Timer requestBodyReadTimer = null;
    private Timer requestHeaderWriteTimer = null;
    private Timer requestBodyWriteTimer = null;

    // Set the Carbon-Metrics Timers
    private Timer.Context rLifeTimeContext;
    private Timer.Context rHeaderReadContext;
    private Timer.Context rBodyReadContext;
    private Timer.Context rHeaderWriteContext;
    private Timer.Context rBodyWriteContext;

    public RequestMetricsStaticsHolder(String type, TimerHolder timerHolder) {
        this.type = type;
        this.requestLifeTimer = timerHolder.getRequestLifeTimer();
        this.requestHeaderReadTimer = timerHolder.getRequestHeaderReadTimer();
        this.requestBodyReadTimer = timerHolder.getRequestBodyReadTimer();
        this.requestHeaderWriteTimer = timerHolder.getRequestBodyWriteTimer();
        this.requestBodyWriteTimer = timerHolder.getRequestBodyWriteTimer();
    }

    @Override public boolean startTimer(String timer) {
        switch (timer) {
        case Constants.REQUEST_LIFE_TIMER:
            rLifeTimeContext = requestLifeTimer.start();
            break;
        case Constants.REQUEST_HEADER_READ_TIMER:
            rHeaderReadContext = requestHeaderReadTimer.start();
            break;
        case Constants.REQUEST_BODY_READ_TIMER:
            rBodyReadContext = requestBodyReadTimer.start();
            break;
        case Constants.REQUEST_HEADER_WRITE_TIMER:
            rHeaderWriteContext = requestHeaderWriteTimer.start();
            break;
        case Constants.REQUEST_BODY_WRITE_TIMER:
            rBodyWriteContext = requestBodyWriteTimer.start();
            break;
        default:
            return false;
        }
        return true;
    }

    @Override public boolean stopTimer(String timer) {
        switch (timer) {
        case Constants.REQUEST_LIFE_TIMER:
            setStatics(timer, rLifeTimeContext.stop());
            break;
        case Constants.REQUEST_HEADER_READ_TIMER:
            setStatics(timer, rHeaderReadContext.stop());
            break;
        case Constants.REQUEST_BODY_READ_TIMER:
            setStatics(timer, rBodyReadContext.stop());
            break;
        case Constants.REQUEST_HEADER_WRITE_TIMER:
            setStatics(timer, rHeaderWriteContext.stop());
            break;
        case Constants.REQUEST_BODY_WRITE_TIMER:
            setStatics(timer, rBodyWriteContext.stop());
            break;
        default:
            return false;
        }
        return true;
    }

    @Override public void setStatics(String timer, Long duration) {
        if (requestMetricsStatics == null) {
            this.requestMetricsStatics = new HashMap<String, Long>();
        }
        requestMetricsStatics.put(timer, duration);
    }

    @Override public Map<String, Long> getStatics(String timer) {
        return requestMetricsStatics;
    }

    @Override public String getType() {
        return type;
    }
}
