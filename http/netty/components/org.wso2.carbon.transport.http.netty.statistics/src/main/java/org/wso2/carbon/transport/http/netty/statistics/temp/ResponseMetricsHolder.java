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
+ *//*


package org.wso2.carbon.transport.http.netty.statistics;

import org.wso2.carbon.metrics.manager.Timer;
import org.wso2.carbon.transport.http.netty.common.Constants;

*/
/**
 * + * Keep the response related latency data (Raw Data)
 *//*

public class ResponseMetricsHolder {
    private String type = null;

    // Carbon-Metrics parameters
    private Timer responseLifeTimer = null;
    private Timer responseHeaderReadTimer = null;
    private Timer responseBodyReadTimer = null;

    // Carbon-Metrics timer contexts
    private Timer.Context resLifeContext = null;
    private Timer.Context resHeaderReadContext = null;
    private Timer.Context resBodyReadContext = null;

    public ResponseMetricsHolder(String type, TimerHolder timerHandler) {
        this.type = type;
        responseLifeTimer = timerHandler.getResponseLifeTimer();
        responseHeaderReadTimer = timerHandler.getResponseHeaderReadTimer();
        responseBodyReadTimer = timerHandler.getResponseBodyReadTimer();
    }

    public Timer getResponseLifeTime() {
        return responseLifeTimer;
    }

    public Timer getResponseHeaderReadTime() {
        return responseHeaderReadTimer;
    }

    public Timer getResponseBodyReadTime() {
        return responseBodyReadTimer;
    }

    public String getType() {
        return type;
    }

    public boolean startTimer(String timer) {
        switch (timer) {
        case Constants.RESPONSE_LIFE_TIMER:
            resLifeContext = responseLifeTimer.start();
            break;
        case Constants.RESPONSE_BODY_READ_TIMER:
            resBodyReadContext = responseBodyReadTimer.start();
            break;
        case Constants.RESPONSE_HEADER_READ_TIMER:
            resHeaderReadContext = responseHeaderReadTimer.start();
            break;
        default:
            return false;
        }
        return true;
    }

    public boolean stopTimer(String timer) {
        switch (timer) {
        case Constants.RESPONSE_LIFE_TIMER:
            resLifeContext.stop();
            break;
        case Constants.RESPONSE_BODY_READ_TIMER:
            resBodyReadContext.stop();
            break;
        case Constants.RESPONSE_HEADER_READ_TIMER:
            resHeaderReadContext.stop();
            break;
        default:
            return false;
        }
        return true;
    }

    public Timer.Context getResLifeContext() {
        return resLifeContext;
    }

    public Timer.Context getResHeaderReadContext() {
        return resHeaderReadContext;
    }

    public Timer.Context getResBodyReadContext() {
        return resBodyReadContext;
    }
}
*/
