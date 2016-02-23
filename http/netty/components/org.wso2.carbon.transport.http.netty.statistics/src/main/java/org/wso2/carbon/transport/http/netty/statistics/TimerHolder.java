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

import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.MetricManager;
import org.wso2.carbon.metrics.manager.Timer;

/**
 * Initialize all the timers
 */
public class TimerHolder {

    private final Timer sourceConnectionTimer;
    private final Timer targetConnectionTimer;
    private final Timer sourceRequestTimer;
    private final Timer targetRequestTimer;
    private final Timer targetResponseTimer;
    private final Timer sourceResponseTimer;

    private static volatile TimerHolder timerHolder;

    private TimerHolder() {
        sourceConnectionTimer = MetricManager.timer("gw.source.connection.timer", Level.INFO);
        targetConnectionTimer = MetricManager.timer("gw.target.connection.timer", Level.INFO);
        sourceRequestTimer = MetricManager.timer("gw.source.request.timer", Level.INFO);
        targetRequestTimer = MetricManager.timer("gw.target.request.timer", Level.INFO);
        targetResponseTimer = MetricManager.timer("gw.target.response.timer", Level.INFO);
        sourceResponseTimer = MetricManager.timer("gw.source.response.timer", Level.INFO);
    }

    public static TimerHolder getInstance() {
        if (timerHolder == null) {
            timerHolder = new TimerHolder();
        }
        return timerHolder;
    }

    public Timer getSourceConnectionTimer() {
        return sourceConnectionTimer;
    }

    public Timer getTargetConnectionTimer() {
        return targetConnectionTimer;
    }

    public Timer getSourceRequestTimer() {
        return sourceRequestTimer;
    }

    public Timer getTargetRequestTimer() {
        return targetRequestTimer;
    }

    public Timer getTargetResponseTimer() {
        return targetResponseTimer;
    }

    public Timer getSourceResponseTimer() {
        return sourceResponseTimer;
    }
}
