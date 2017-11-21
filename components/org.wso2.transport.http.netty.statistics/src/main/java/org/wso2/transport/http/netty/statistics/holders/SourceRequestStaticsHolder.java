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

package org.wso2.transport.http.netty.statistics.holders;

import org.wso2.carbon.metrics.core.Timer;
import org.wso2.transport.http.netty.statistics.TimerHolder;

/**
 * Holder for source request timer.
 */
public class SourceRequestStaticsHolder implements MetricsStaticsHolder {

    // Carbon-Metrics Measuring parameters
    private Timer timer = null;

    // Set the Carbon-Metrics Timers
    private Timer.Context timerContext = null;

    public SourceRequestStaticsHolder(TimerHolder timerHolder) {
            timer = timerHolder.getSourceRequestTimer();
    }

    @Override
    public void startTimer() {
        timerContext = this.timer.start();
    }

    @Override
    public void stopTimer() {
        timerContext.stop();
    }
}
