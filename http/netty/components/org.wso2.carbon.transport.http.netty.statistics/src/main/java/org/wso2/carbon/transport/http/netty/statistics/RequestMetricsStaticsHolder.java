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

/**
 * Holds the request metrics parameters
 */
public class RequestMetricsStaticsHolder implements MetricsStaticsHolder {

    // Carbon-Metrics Measuring parameters
    private Timer lifeTimer = null;
    private Timer headerTimer = null;
    private Timer bodyTimer = null;

    // Set the Carbon-Metrics Timers
    private Timer.Context lifeTimerContext = null;
    private Timer.Context headerTimerContext = null;
    private Timer.Context bodyTimerContext = null;

    public RequestMetricsStaticsHolder(String type, TimerHolder timerHolder) {
        if (type.equals(MetricsConstants.TYPE_CLIENT_REQUEST)) {
            lifeTimer = timerHolder.getClientRequestLifeTimer();
            headerTimer = timerHolder.getClientRequestHeaderTimer();
            bodyTimer = timerHolder.getClientRequestBodyTimer();

        } else {
            lifeTimer = timerHolder.getServerRequestLifeTimer();
            headerTimer = timerHolder.getServerRequestHeaderTimer();
            bodyTimer = timerHolder.getServerRequestBodyTimer();
        }
    }

    @Override
    public boolean startTimer(String timer) {
        switch (timer) {
        case MetricsConstants.REQUEST_LIFE_TIMER:
            lifeTimerContext = lifeTimer.start();
            break;
        case MetricsConstants.REQUEST_HEADER_TIMER:
            headerTimerContext = headerTimer.start();
            break;
        case MetricsConstants.REQUEST_BODY_TIMER:
            bodyTimerContext = bodyTimer.start();
            break;

        default:
            return false;
        }
        return true;
    }

    @Override
    public boolean stopTimer(String timer) {
        switch (timer) {
        case MetricsConstants.REQUEST_LIFE_TIMER:
            lifeTimerContext.stop();
            break;
        case MetricsConstants.REQUEST_HEADER_TIMER:
            headerTimerContext.stop();
            break;
        case MetricsConstants.REQUEST_BODY_TIMER:
            bodyTimerContext.stop();
            break;

        default:
            return false;
        }
        return true;
    }
}
