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

import java.util.HashMap;
import java.util.Map;

/**
 * Holds the Connection related Latency Metrics
 */
public class ConnectionMetricsStaticsHolder implements MetricsStaticsHolder {

    private String type = null;
    private String timer = null;
    private Timer connectionTimer = null;
    private Timer.Context context = null;
    private Map<String, Long> connectionStatics;

    public ConnectionMetricsStaticsHolder(String type, String timer, TimerHolder timerHolder) {
        this.type = type;
        this.timer = timer;
        this.connectionTimer = timerHolder.getConnectionTimer();
    }

    @Override public boolean startTimer(String timer) {
        this.context = this.connectionTimer.start();
        return false;
    }

    @Override public boolean stopTimer(String timer) {
        if (this.context != null) {
            setStatics(timer, this.context.stop());
            return true;
        } else {
            return false;
        }
    }

    @Override public Map<String, Long> getStatics(String timer) {
        return null;
    }

    @Override public void setStatics(String timer, Long duration) {
        if (connectionStatics == null) {
            this.connectionStatics = new HashMap<String, Long>();
        }
        connectionStatics.put(timer, duration);
    }

    @Override public String getType() {
        return type;
    }
}
