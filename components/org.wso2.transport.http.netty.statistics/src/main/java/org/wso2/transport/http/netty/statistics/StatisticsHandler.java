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

import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.handler.MessagingHandler;
import org.wso2.transport.http.netty.statistics.holders.MetricsStaticsHolder;
import org.wso2.transport.http.netty.statistics.holders.SourceConnectionStaticsHolder;
import org.wso2.transport.http.netty.statistics.holders.SourceRequestStaticsHolder;
import org.wso2.transport.http.netty.statistics.holders.SourceResponseStaticsHolder;
import org.wso2.transport.http.netty.statistics.holders.TargetConnectionStaticsHolder;
import org.wso2.transport.http.netty.statistics.holders.TargetRequestStaticsHolder;
import org.wso2.transport.http.netty.statistics.holders.TargetResponseStaticsHolder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of MessagingHandler.
 */
public class StatisticsHandler implements MessagingHandler {

    private final TimerHolder timerHolder;
    private Map<String, MetricsStaticsHolder> messageCorrelation = new ConcurrentHashMap<>();

    public StatisticsHandler(TimerHolder timerHolder) {
        // Initialize the timerHolder
        this.timerHolder = timerHolder;

    }

    @Override
    public boolean validateRequestContinuation(CarbonMessage carbonMessage, CarbonCallback carbonCallback) {
        return true;
    }

    @Override
    public void invokeAtSourceConnectionInitiation(String key) {
        MetricsStaticsHolder sourceConnectionMetricsHolder = new SourceConnectionStaticsHolder(timerHolder);
        sourceConnectionMetricsHolder.startTimer();
        messageCorrelation.put(key, sourceConnectionMetricsHolder);
    }

    @Override
    public void invokeAtSourceConnectionTermination(String key) {
        messageCorrelation.remove(key).stopTimer();
    }

    @Override
    public void invokeAtSourceRequestReceiving(CarbonMessage carbonMessage) {
        SourceRequestStaticsHolder requestMetricsStaticsHolder = new SourceRequestStaticsHolder(timerHolder);
        requestMetricsStaticsHolder.startTimer();
        carbonMessage.setProperty(MetricsConstants.SOURCE_REQUEST_METRICS_HOLDER, requestMetricsStaticsHolder);
    }

    @Override
    public void invokeAtSourceRequestSending(CarbonMessage carbonMessage) {
        SourceRequestStaticsHolder requestMetricsStaticsHolder = (SourceRequestStaticsHolder) carbonMessage
                .getProperty(MetricsConstants.SOURCE_REQUEST_METRICS_HOLDER);
        if (requestMetricsStaticsHolder != null) {
            requestMetricsStaticsHolder.stopTimer();
        }

    }

    @Override
    public void invokeAtTargetRequestReceiving(CarbonMessage carbonMessage) {
        TargetRequestStaticsHolder requestMetricsStaticsHolder = new TargetRequestStaticsHolder(timerHolder);
        requestMetricsStaticsHolder.startTimer();
        carbonMessage.setProperty(MetricsConstants.TARGET_REQUEST_METRICS_HOLDER, requestMetricsStaticsHolder);
    }

    @Override
    public void invokeAtTargetRequestSending(CarbonMessage carbonMessage) {
        TargetRequestStaticsHolder requestMetricsStaticsHolder = (TargetRequestStaticsHolder) carbonMessage
                .getProperty(MetricsConstants.TARGET_REQUEST_METRICS_HOLDER);
        if (requestMetricsStaticsHolder != null) {
            requestMetricsStaticsHolder.stopTimer();
        }
    }

    @Override
    public void invokeAtTargetResponseReceiving(CarbonMessage carbonMessage) {
        TargetResponseStaticsHolder responseMetricsStaticsHolder = new TargetResponseStaticsHolder(timerHolder);
        responseMetricsStaticsHolder.startTimer();
        carbonMessage.setProperty(MetricsConstants.TARGET_RESPONSE_METRICS_HOLDER, responseMetricsStaticsHolder);
    }

    @Override
    public void invokeAtTargetResponseSending(CarbonMessage carbonMessage) {
        TargetResponseStaticsHolder responseMetricsStaticsHolder = (TargetResponseStaticsHolder) carbonMessage
                .getProperty(MetricsConstants.TARGET_RESPONSE_METRICS_HOLDER);
        if (responseMetricsStaticsHolder != null) {
            responseMetricsStaticsHolder.stopTimer();
        }

    }

    @Override
    public void invokeAtSourceResponseReceiving(CarbonMessage carbonMessage) {
        SourceResponseStaticsHolder responseMetricsStaticsHolder = new SourceResponseStaticsHolder(timerHolder);
        responseMetricsStaticsHolder.startTimer();
        carbonMessage.setProperty(MetricsConstants.SOURCE_RESPONSE_METRICS_HOLDER, responseMetricsStaticsHolder);
    }

    @Override
    public void invokeAtSourceResponseSending(CarbonMessage carbonMessage) {
        SourceResponseStaticsHolder responseMetricsStaticsHolder = (SourceResponseStaticsHolder) carbonMessage
                .getProperty(MetricsConstants.SOURCE_RESPONSE_METRICS_HOLDER);
        if (responseMetricsStaticsHolder != null) {
            responseMetricsStaticsHolder.stopTimer();
        }
    }

    @Override
    public void invokeAtTargetConnectionInitiation(String key) {
        MetricsStaticsHolder targetConnectionMetricsHolder = new TargetConnectionStaticsHolder(timerHolder);
        targetConnectionMetricsHolder.startTimer();
        messageCorrelation.put(key, targetConnectionMetricsHolder);
    }

    @Override
    public void invokeAtTargetConnectionTermination(String key) {
        messageCorrelation.remove(key).stopTimer();
    }

    @Override
    public String handlerName() {
        return "StatisticsHandler";
    }

}
