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

    private final Timer clientConnectionTimer;
    private final Timer serverConnectionTimer;

    private final Timer clientRequestLifeTimer;
    private final Timer clientRequestHeaderTimer;
    private final Timer clientRequestBodyTimer;

    private final Timer serverRequestLifeTimer;
    private final Timer serverRequestHeaderTimer;
    private final Timer serverRequestBodyTimer;

    private final Timer serverResponseLifeTimer;
    private final Timer serverResponseHeaderTimer;
    private final Timer serverResponseBodyTimer;

    private final Timer clientResponseLifeTimer;
    private final Timer clientResponseHeaderTimer;
    private final Timer clientResponseBodyTimer;

    private static volatile TimerHolder timerHolder;

    private TimerHolder() {
        clientConnectionTimer = MetricManager.timer("gw.client.connection.timer", Level.INFO);
        serverConnectionTimer = MetricManager.timer("gw.server.connection.timer", Level.INFO);

        clientRequestLifeTimer = MetricManager.timer("gw.client.request.life.timer", Level.INFO);
        clientRequestHeaderTimer = MetricManager.timer("gw.client.request.header.timer", Level.INFO);
        clientRequestBodyTimer = MetricManager.timer("gw.client.request.body.timer", Level.INFO);

        serverRequestLifeTimer = MetricManager.timer("gw.server.request.life.timer", Level.INFO);
        serverRequestHeaderTimer = MetricManager.timer("gw.server.request.header.timer", Level.INFO);
        serverRequestBodyTimer = MetricManager.timer("gw.server.request.body.timer", Level.INFO);

        serverResponseLifeTimer = MetricManager.timer("gw.server.response.life.timer", Level.INFO);
        serverResponseHeaderTimer = MetricManager.timer("gw.server.response.header.timer", Level.INFO);
        serverResponseBodyTimer = MetricManager.timer("gw.server.response.body.timer", Level.INFO);

        clientResponseLifeTimer = MetricManager.timer("gw.client.response.life.timer", Level.INFO);
        clientResponseHeaderTimer = MetricManager.timer("gw.client.response.header.timer", Level.INFO);
        clientResponseBodyTimer = MetricManager.timer("gw.client.response.body.timer", Level.INFO);
    }

    public static TimerHolder getInstance() {
        if (timerHolder == null) {
            timerHolder = new TimerHolder();
        }
        return timerHolder;
    }

    public Timer getClientConnectionTimer() {
        return clientConnectionTimer;
    }

    public Timer getServerConnectionTimer() {
        return serverConnectionTimer;
    }

    public Timer getClientRequestLifeTimer() {
        return clientRequestLifeTimer;
    }

    public Timer getClientRequestHeaderTimer() {
        return clientRequestHeaderTimer;
    }

    public Timer getClientRequestBodyTimer() {
        return clientRequestBodyTimer;
    }

    public Timer getServerRequestLifeTimer() {
        return serverRequestLifeTimer;
    }

    public Timer getServerRequestHeaderTimer() {
        return serverRequestHeaderTimer;
    }

    public Timer getServerRequestBodyTimer() {
        return serverRequestBodyTimer;
    }

    public Timer getServerResponseLifeTimer() {
        return serverResponseLifeTimer;
    }

    public Timer getServerResponseHeaderTimer() {
        return serverResponseHeaderTimer;
    }

    public Timer getServerResponseBodyTimer() {
        return serverResponseBodyTimer;
    }

    public Timer getClientResponseLifeTimer() {
        return clientResponseLifeTimer;
    }

    public Timer getClientResponseHeaderTimer() {
        return clientResponseHeaderTimer;
    }

    public Timer getClientResponseBodyTimer() {
        return clientResponseBodyTimer;
    }
}
