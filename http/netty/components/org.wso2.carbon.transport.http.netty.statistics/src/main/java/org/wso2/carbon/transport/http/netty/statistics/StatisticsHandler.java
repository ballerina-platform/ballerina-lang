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

import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.MessagingHandler;
import org.wso2.carbon.messaging.State;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of MessagingHandler
 */
public class StatisticsHandler implements MessagingHandler {

    private final TimerHolder timerHolder;
    private Map<String, MetricsStaticsHolder> messageCorrelation = new ConcurrentHashMap<>();

    public StatisticsHandler(TimerHolder timerHolder) {
        // Initialize the timerHolder
        this.timerHolder = timerHolder;

    }

    @Override
    public boolean sourceConnection(String key, State state) {
        if (state == State.INITIATED) {
            MetricsStaticsHolder sourceConnectionMetricsHolder = new ConnectionMetricsStaticsHolder(
                    MetricsConstants.TYPE_SOURCE, timerHolder);
            sourceConnectionMetricsHolder.startTimer();
            messageCorrelation.put(key, sourceConnectionMetricsHolder);
        } else {
            messageCorrelation.remove(key).stopTimer();
        }
        return true;

    }

    @Override
    public boolean sourceRequest(CarbonMessage carbonMessage, State state) {
        RequestMetricsStaticsHolder requestMetricsStaticsHolder;
        if (state == State.INITIATED) {
            requestMetricsStaticsHolder = new RequestMetricsStaticsHolder(MetricsConstants.TYPE_SOURCE, timerHolder);
            requestMetricsStaticsHolder.startTimer();
            carbonMessage.setProperty(MetricsConstants.SOURCE_REQUEST_METRICS_HOLDER, requestMetricsStaticsHolder);
        } else {
            requestMetricsStaticsHolder = (RequestMetricsStaticsHolder) carbonMessage
                    .getProperty(MetricsConstants.SOURCE_REQUEST_METRICS_HOLDER);
            requestMetricsStaticsHolder.stopTimer();
        }
        return true;
    }

    @Override
    public boolean sourceResponse(CarbonMessage carbonMessage, State state) {
        ResponseMetricsStaticsHolder responseMetricsStaticsHolder;
        if (state == State.INITIATED) {
            responseMetricsStaticsHolder = new ResponseMetricsStaticsHolder(MetricsConstants.TYPE_SOURCE, timerHolder);
            responseMetricsStaticsHolder.startTimer();
            carbonMessage.setProperty(MetricsConstants.SOURCE_RESPONSE_METRICS_HOLDER, responseMetricsStaticsHolder);
        } else {
            responseMetricsStaticsHolder = (ResponseMetricsStaticsHolder) carbonMessage
                    .getProperty(MetricsConstants.SOURCE_RESPONSE_METRICS_HOLDER);
            responseMetricsStaticsHolder.stopTimer();
        }
        return true;
    }

    @Override
    public boolean targetConnection(String key, State state) {
        if (state == State.INITIATED) {
            MetricsStaticsHolder targetConnectionMetricsHolder = new ConnectionMetricsStaticsHolder(
                    MetricsConstants.TYPE_TARGET, timerHolder);
            targetConnectionMetricsHolder.startTimer();
            messageCorrelation.put(key, targetConnectionMetricsHolder);
        } else {
            messageCorrelation.remove(key).stopTimer();
        }
        return true;
    }

    @Override
    public boolean targetRequest(CarbonMessage carbonMessage, State state) {
        RequestMetricsStaticsHolder requestMetricsStaticsHolder;
        if (state == State.INITIATED) {
            requestMetricsStaticsHolder = new RequestMetricsStaticsHolder(MetricsConstants.TYPE_TARGET, timerHolder);
            requestMetricsStaticsHolder.startTimer();
            carbonMessage.setProperty(MetricsConstants.TARGET_REQUEST_METRICS_HOLDER, requestMetricsStaticsHolder);
        } else {
            requestMetricsStaticsHolder = (RequestMetricsStaticsHolder) carbonMessage
                    .getProperty(MetricsConstants.TARGET_REQUEST_METRICS_HOLDER);
            requestMetricsStaticsHolder.stopTimer();
        }
        return true;
    }

    @Override
    public boolean targetResponse(CarbonMessage carbonMessage, State state) {
        ResponseMetricsStaticsHolder responseMetricsStaticsHolder;
        if (state == State.INITIATED) {
            responseMetricsStaticsHolder = new ResponseMetricsStaticsHolder(MetricsConstants.TYPE_TARGET, timerHolder);
            responseMetricsStaticsHolder.startTimer();
            carbonMessage.setProperty(MetricsConstants.TARGET_RESPONSE_METRICS_HOLDER, responseMetricsStaticsHolder);
        } else {
            responseMetricsStaticsHolder = (ResponseMetricsStaticsHolder) carbonMessage
                    .getProperty(MetricsConstants.TARGET_RESPONSE_METRICS_HOLDER);
            responseMetricsStaticsHolder.stopTimer();
        }
        return true;
    }

    @Override
    public String handlerName() {
        return "StatisticsHandler";
    }

}
