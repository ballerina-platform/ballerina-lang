package org.wso2.carbon.transport.http.netty.latency.metrics;

import org.wso2.carbon.metrics.manager.Timer;
import org.wso2.carbon.transport.http.netty.common.TransportConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chamile on 2/9/16.
 */
public class RequestMetricsStaticsHolder implements MetricsStaticsHolder {

    private String type,timer;
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


    public RequestMetricsStaticsHolder(String type,TimerHandler timerHandler) {
        this.type = type;
        this.requestLifeTimer = timerHandler.getRequestLifeTimer();
        this.requestHeaderReadTimer = timerHandler.getRequestHeaderReadTimer();
        this.requestBodyReadTimer = timerHandler.getRequestBodyReadTimer();
        this.requestHeaderWriteTimer = timerHandler.getRequestBodyWriteTimer();
        this.requestBodyWriteTimer = timerHandler.getRequestBodyWriteTimer();
    }

    @Override
    public boolean startTimer(String timer) {
        switch (timer) {
            case TransportConstants.REQUEST_LIFE_TIMER:
                rLifeTimeContext = requestLifeTimer.start();
                break;
            case TransportConstants.REQUEST_HEADER_READ_TIMER:
                rHeaderReadContext = requestHeaderReadTimer.start();
                break;
            case TransportConstants.REQUEST_BODY_READ_TIMER:
                rBodyReadContext = requestBodyReadTimer.start();
                break;
            case TransportConstants.REQUEST_HEADER_WRITE_TIMER:
                rHeaderWriteContext = requestHeaderWriteTimer.start();
                break;
            case TransportConstants.REQUEST_BODY_WRITE_TIMER:
                rBodyWriteContext = requestBodyWriteTimer.start();
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public boolean stopTimer(String timer) {
        switch (timer) {
            case TransportConstants.REQUEST_LIFE_TIMER:
                setStatics(timer,rLifeTimeContext.stop());
                break;
            case TransportConstants.REQUEST_BODY_READ_TIMER:
                setStatics(timer,rBodyReadContext.stop());
                break;
            case TransportConstants.REQUEST_HEADER_READ_TIMER:
                setStatics(timer,rHeaderReadContext.stop());
                break;
            case TransportConstants.REQUEST_HEADER_WRITE_TIMER:
                setStatics(timer,rHeaderWriteContext.stop());
                break;
            case TransportConstants.REQUEST_BODY_WRITE_TIMER:
                setStatics(timer,rBodyWriteContext.stop());
                break;

            default:
                return false;
        }
        return true;
    }

    @Override
    public void setStatics(String timer, Long duration) {
        if (requestMetricsStatics == null) {
            this.requestMetricsStatics = new HashMap<String, Long>();
        }
        requestMetricsStatics.put(timer, duration);
    }

    @Override
    public Map<String, Long> getStatics(String timer) {
        return requestMetricsStatics;
    }

    @Override
    public String getType() {
        return type;
    }
}
