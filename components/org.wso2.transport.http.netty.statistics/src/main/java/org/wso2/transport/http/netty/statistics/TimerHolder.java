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

package org.wso2.transport.http.netty.statistics;

import org.wso2.carbon.metrics.core.Level;
import org.wso2.carbon.metrics.core.MetricService;
import org.wso2.carbon.metrics.core.Timer;
import org.wso2.transport.http.netty.statistics.internal.DataHolder;

/**
 * Initialize all the timers.
 */
public class TimerHolder {

    private final MetricService metricService = DataHolder.getInstance().getMetricService();

    private final Timer sourceConnectionTimer = metricService.timer("gw.source.connection.timer", Level.INFO);
    private final Timer targetConnectionTimer = metricService.timer("gw.target.connection.timer", Level.INFO);
    private final Timer sourceRequestTimer = metricService.timer("gw.source.request.timer", Level.INFO);
    private final Timer targetRequestTimer = metricService.timer("gw.target.request.timer", Level.INFO);
    private final Timer targetResponseTimer = metricService.timer("gw.target.response.timer", Level.INFO);
    private final Timer sourceResponseTimer = metricService.timer("gw.source.response.timer", Level.INFO);

    private static volatile TimerHolder timerHolder = new TimerHolder();

    private TimerHolder() {

    }

    public static TimerHolder getInstance() {
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
