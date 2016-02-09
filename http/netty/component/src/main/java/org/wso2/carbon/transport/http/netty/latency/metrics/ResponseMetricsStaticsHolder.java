package org.wso2.carbon.transport.http.netty.latency.metrics;

import org.wso2.carbon.metrics.manager.Timer;
import org.wso2.carbon.transport.http.netty.common.TransportConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chamile on 2/9/16.
 */
public class ResponseMetricsStaticsHolder implements MetricsStaticsHolder {

    private String type, timer = null;
    private Map<String, Long> responseMetricsStatics;

    // Carbon-Metrics parameters
    private Timer responseLifeTimer = null;
    private Timer responseHeaderReadTimer = null;
    private Timer responseBodyReadTimer = null;

    // Carbon-Metrics timer contexts
    private Timer.Context resLifeContext = null;
    private Timer.Context resHeaderReadContext = null;
    private Timer.Context resBodyReadContext = null;

    public ResponseMetricsStaticsHolder(String type, TimerHandler timerHandler) {
        this.type = type;
        this.responseLifeTimer = timerHandler.getResponseLifeTimer();
        this.responseHeaderReadTimer = timerHandler.getResponseHeaderReadTimer();
        this.responseBodyReadTimer = timerHandler.getResponseBodyReadTimer();
    }

    @Override
    public boolean startTimer(String timer) {
        switch (timer) {
            case TransportConstants.RESPONSE_LIFE_TIMER:
                resLifeContext = responseLifeTimer.start();
                break;
            case TransportConstants.RESPONSE_HEADER_READ_TIMER:
                resHeaderReadContext = responseHeaderReadTimer.start();
                break;
            case TransportConstants.RESPONSE_BODY_READ_TIMER:
                resBodyReadContext = responseBodyReadTimer.start();
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public boolean stopTimer(String timer) {
        switch (timer) {
            case TransportConstants.RESPONSE_LIFE_TIMER:
                setStatics(timer, resLifeContext.stop());
                break;
            case TransportConstants.RESPONSE_HEADER_READ_TIMER:
                setStatics(timer, resHeaderReadContext.stop());
                break;
            case TransportConstants.RESPONSE_BODY_READ_TIMER:
                setStatics(timer, resBodyReadContext.stop());
                break;

            default:
                return false;
        }
        return true;
    }

    @Override
    public void setStatics(String timer, Long duration) {
        if (responseMetricsStatics == null) {
            this.responseMetricsStatics = new HashMap<String, Long>();
        }
        responseMetricsStatics.put(timer, duration);

    }

    @Override
    public Map<String, Long> getStatics(String timer) {
        return responseMetricsStatics;
    }

    @Override
    public String getType() {
        return type;
    }
}
