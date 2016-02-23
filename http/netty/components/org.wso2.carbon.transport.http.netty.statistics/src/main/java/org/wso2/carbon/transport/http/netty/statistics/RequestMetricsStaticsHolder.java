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
    private Timer timer = null;

    // Set the Carbon-Metrics Timers
    private Timer.Context timerContext = null;

    public RequestMetricsStaticsHolder(String type, TimerHolder timerHolder) {
        if (type.equals(MetricsConstants.TYPE_SOURCE)) {
            timer = timerHolder.getSourceRequestTimer();

        } else {
            timer = timerHolder.getTargetRequestTimer();
        }
    }

    @Override
    public boolean startTimer() {
        timerContext = this.timer.start();
        return true;
    }

    @Override
    public boolean stopTimer() {
        timerContext.stop();
        return true;
    }
}
