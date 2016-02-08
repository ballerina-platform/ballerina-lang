

/*
+ * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
+ *
+ * Licensed under the Apache License, Version 2.0 (the "License");
+ * you may not use this file except in compliance with the License.
+ * You may obtain a copy of the License at
+ *
+ * http://www.apache.org/licenses/LICENSE-2.0
+ *
+ * Unless required by applicable law or agreed to in writing, software
+ * distributed under the License is distributed on an "AS IS" BASIS,
+ * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
+ * See the License for the specific language governing permissions and limitations under the License.
 */

package org.wso2.carbon.transport.http.netty.latency.metrics;

import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.MetricManager;
import org.wso2.carbon.metrics.manager.MetricService;
import org.wso2.carbon.metrics.manager.Timer;

/**
 * * Holds the Connection related Latency Metrics
 */
public class ConnectionMetricsHolder {

    private String type = null;
    // Carbon-Metrics Measuring parameters
    private Timer connectionTimer = null;
    private Timer.Context context = null;

    private MetricService metricService = null;

    public ConnectionMetricsHolder(String type, TimerHandler timerHandler) {
        this.type = type;
        // Initialize connection metrics holder Timers
        connectionTimer = MetricManager.timer(MetricManager.
                name(ConnectionMetricsHolder.class, "connection.timer"), Level.INFO);
    }

    public Timer getConnectionTimer() {
        return connectionTimer;
    }

    public Timer.Context getContext() {
        return context;
    }

    public boolean startTimer() {
        this.context = this.connectionTimer.start();
        return true;
    }

    public boolean stopTimer() {
        if (this.context != null) {
            this.context.stop();
            return true;
        } else {
            return false;
        }
    }

    public void setConnectionTimer(Timer connectionTimer) {
        this.connectionTimer = connectionTimer;
    }
}
