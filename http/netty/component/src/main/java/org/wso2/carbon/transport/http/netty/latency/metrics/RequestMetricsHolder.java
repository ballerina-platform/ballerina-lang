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
+ */

package org.wso2.carbon.transport.http.netty.latency.metrics;

import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.MetricManager;
import org.wso2.carbon.metrics.manager.Timer;
import org.wso2.carbon.transport.http.netty.common.TransportConstants;

/**
 * Holds the request metrics parameters
 */
public class RequestMetricsHolder {

    private String type;

    // Carbon-Metrics Measuring parameters
    private Timer requestLifeTimer = null;
    private Timer requestBodyReadTimer = null;
    private Timer requestHeaderReadTimer = null;

    private Timer requestHeaderWriteTimer = null;
    private Timer requestBodyWriteTimer = null;

    // Set the Carbon-Metrics Timers
    private Timer.Context rLifeTimeContext;
    private Timer.Context rBodyReadContext;
    private Timer.Context rHeaderReadContext;

    private Timer.Context rHeaderWriteContext;
    private Timer.Context rBodyWriteContext;

    public RequestMetricsHolder(String type) {
        //TODO avoid creating same objects
        this.type = type;
        requestLifeTimer = MetricManager.timer(MetricManager.
                name(RequestMetricsHolder.class, "request.life.time"), Level.INFO);
        //TODO rename metrics name
        requestBodyReadTimer = MetricManager.timer(MetricManager.
                name(RequestMetricsHolder.class, "request.body.read.time"), Level.INFO);
        requestHeaderReadTimer = MetricManager.timer(MetricManager.
                name(RequestMetricsHolder.class, "request.header.read.time"), Level.INFO);
        requestBodyWriteTimer = MetricManager.timer(MetricManager.
                name(RequestMetricsHolder.class, "request.body.write.timer"), Level.INFO);
        /*
        requestLifeTimer = MetricManager.timer(Level.INFO, MetricManager.
                name(RequestMetricsHolder.class, "request.life.time"));
        requestBodyReadTimer = MetricManager.timer(Level.INFO, MetricManager.
                name(RequestMetricsHolder.class, "request.body.read.time"));
        requestHeaderReadTimer = MetricManager.timer(Level.INFO, MetricManager.
                name(RequestMetricsHolder.class, "request.header.read.time"));*/
    }

    public String getType() {
        return type;
    }

    public Timer getRequestLifeTimer() {
        return requestLifeTimer;
    }

    public Timer getRequestBodyReadTimer() {
        return requestBodyReadTimer;
    }

    public Timer getRequestHeaderReadTimer() {
        return requestHeaderReadTimer;
    }

    public boolean startTimer(String timer) {
        switch (timer) {
            case TransportConstants.REQUEST_LIFE_TIMER:
                rLifeTimeContext = requestLifeTimer.start();
                break;
            case TransportConstants.REQUEST_BODY_READ_TIMER:
                rBodyReadContext = requestBodyReadTimer.start();
                break;
            case TransportConstants.REQUEST_HEADER_READ_TIMER:
                rHeaderReadContext = requestHeaderReadTimer.start();
                break;
            case TransportConstants.REQUEST_BODY_WRITE_TIMER:
                rBodyWriteContext = requestBodyWriteTimer.start();
                break;
            default:
                return false;
        }
        return true;
    }

    public boolean stopTimer(String timer) {
        switch (timer) {
            case TransportConstants.REQUEST_LIFE_TIMER:
                rLifeTimeContext.stop();
                break;
            case TransportConstants.REQUEST_BODY_READ_TIMER:
                rBodyReadContext.stop();
                break;
            case TransportConstants.REQUEST_HEADER_READ_TIMER:
                rHeaderReadContext.stop();
                break;
            default:
                return false;
        }
        return true;
    }

    public Timer.Context getrLifeTimeContext() {
        return rLifeTimeContext;
    }

    public Timer.Context getrBodyReadContext() {
        return rBodyReadContext;
    }

    public Timer.Context getrHeaderReadContext() {
        return rHeaderReadContext;
    }
}
