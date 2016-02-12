/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.transport.http.netty.statistics;

import org.wso2.carbon.metrics.manager.Timer;
import org.wso2.carbon.transport.http.netty.common.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Keep the response related latency data (Raw Data)
 */
public class ResponseMetricsStaticsHolder implements MetricsStaticsHolder {

    private String type = null;
    private Map<String, Long> responseMetricsStatics;

    // Carbon-Metrics parameters]
    private Timer responseLifeTimer = null;
    private Timer responseHeaderReadTimer = null;
    private Timer responseBodyReadTimer = null;

    // Carbon-Metrics timer contexts
    private Timer.Context resLifeContext = null;
    private Timer.Context resHeaderReadContext = null;
    private Timer.Context resBodyReadContext = null;

    public ResponseMetricsStaticsHolder(String type, TimerHolder timerHolder) {
        this.type = type;
        this.responseLifeTimer = timerHolder.getResponseLifeTimer();
        this.responseHeaderReadTimer = timerHolder.getResponseHeaderReadTimer();
        this.responseBodyReadTimer = timerHolder.getResponseBodyReadTimer();
    }

    @Override public boolean startTimer(String timer) {
        switch (timer) {
        case Constants.RESPONSE_LIFE_TIMER:
            resLifeContext = responseLifeTimer.start();
            break;
        case Constants.RESPONSE_HEADER_READ_TIMER:
            resHeaderReadContext = responseHeaderReadTimer.start();
            break;
        case Constants.RESPONSE_BODY_READ_TIMER:
            resBodyReadContext = responseBodyReadTimer.start();
            break;
        default:
            return false;
        }
        return true;
    }

    @Override public boolean stopTimer(String timer) {
        switch (timer) {
        case Constants.RESPONSE_LIFE_TIMER:
            setStatics(timer, resLifeContext.stop());
            break;
        case Constants.RESPONSE_HEADER_READ_TIMER:
            setStatics(timer, resHeaderReadContext.stop());
            break;
        case Constants.RESPONSE_BODY_READ_TIMER:
            setStatics(timer, resBodyReadContext.stop());
            break;

        default:
            return false;
        }
        return true;
    }

    @Override public void setStatics(String timer, Long duration) {
        if (responseMetricsStatics == null) {
            this.responseMetricsStatics = new HashMap<String, Long>();
        }
        responseMetricsStatics.put(timer, duration);

    }

    @Override public Map<String, Long> getStatics(String timer) {
        return responseMetricsStatics;
    }

    @Override public String getType() {
        return type;
    }
}
