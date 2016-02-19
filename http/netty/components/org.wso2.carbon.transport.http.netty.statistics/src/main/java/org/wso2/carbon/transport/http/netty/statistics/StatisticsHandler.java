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
import org.wso2.carbon.messaging.Constants;
import org.wso2.carbon.messaging.EngagedLocation;
import org.wso2.carbon.messaging.MessagingHandler;

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
    public boolean invoke(CarbonMessage cMessage, EngagedLocation engagedLocation) {

        MetricsStaticsHolder serverConnectionMetricsHolder;
        MetricsStaticsHolder clientConnectionMetricsHolder;
        MetricsStaticsHolder serverRequestMetricsHolder;
        MetricsStaticsHolder clientRequestMetricsHolder;
        MetricsStaticsHolder serverResponseMetricsHolder;
        MetricsStaticsHolder clientResponseMetricsHolder;

        if (cMessage == null) {
            return false;
        }
        if (cMessage.getProperty(Constants.CONNECTION_ID) != null) {

            switch (engagedLocation) {
            case CLIENT_CONNECTION_INITIATED:
                clientConnectionMetricsHolder = new ConnectionMetricsStaticsHolder(
                        MetricsConstants.TYPE_CLIENT_CONNECTION, timerHolder);

                clientConnectionMetricsHolder.startTimer(MetricsConstants.CONNECTION_TIMER);
                messageCorrelation
                        .put(cMessage.getProperty(Constants.CONNECTION_ID).toString(), clientConnectionMetricsHolder);
                return true;
            case CLIENT_CONNECTION_COMPLETED:
                clientConnectionMetricsHolder = messageCorrelation
                        .remove(cMessage.getProperty(Constants.CONNECTION_ID).toString());
                clientConnectionMetricsHolder.stopTimer(MetricsConstants.CONNECTION_TIMER);
                return true;
            case SERVER_CONNECTION_INITIATED:
                serverConnectionMetricsHolder = new ConnectionMetricsStaticsHolder(
                        MetricsConstants.TYPE_SERVER_CONNECTION, timerHolder);
                serverConnectionMetricsHolder.startTimer(MetricsConstants.CONNECTION_TIMER);
                messageCorrelation
                        .put(cMessage.getProperty(Constants.CONNECTION_ID).toString(), serverConnectionMetricsHolder);
                return true;
            case SERVER_CONNECTION_COMPLETED:
                serverConnectionMetricsHolder = messageCorrelation
                        .remove(cMessage.getProperty(Constants.CONNECTION_ID).toString());
                serverConnectionMetricsHolder.stopTimer(MetricsConstants.CONNECTION_TIMER);
                return true;
            default:
                return false;
            }

        }

        if (cMessage.getProperty(org.wso2.carbon.messaging.Constants.DIRECTION) == null) {

            if (cMessage.getProperty(MetricsConstants.CLIENT_REQUEST_METRICS_HOLDER) == null) {
                clientRequestMetricsHolder = new RequestMetricsStaticsHolder(MetricsConstants.TYPE_CLIENT_REQUEST,
                        timerHolder);
                serverRequestMetricsHolder = new RequestMetricsStaticsHolder(MetricsConstants.TYPE_SERVER_REQUEST,
                        timerHolder);

                cMessage.setProperty(MetricsConstants.CLIENT_REQUEST_METRICS_HOLDER, clientRequestMetricsHolder);
                cMessage.setProperty(MetricsConstants.SERVER_REQUEST_METRICS_HOLDER, serverRequestMetricsHolder);

            }
            switch (engagedLocation) {
            case CLIENT_REQUEST_INITIATED:
                clientRequestMetricsHolder = (RequestMetricsStaticsHolder) cMessage
                        .getProperty(MetricsConstants.CLIENT_REQUEST_METRICS_HOLDER);
                clientRequestMetricsHolder.startTimer(MetricsConstants.REQUEST_LIFE_TIMER);
                clientRequestMetricsHolder.startTimer(MetricsConstants.REQUEST_HEADER_TIMER);
                break;
            case CLIENT_REQUEST_HEADERS_COMPLETED:
                clientRequestMetricsHolder = (RequestMetricsStaticsHolder) cMessage
                        .getProperty(MetricsConstants.CLIENT_REQUEST_METRICS_HOLDER);
                clientRequestMetricsHolder.stopTimer(MetricsConstants.REQUEST_HEADER_TIMER);
                clientRequestMetricsHolder.startTimer(MetricsConstants.REQUEST_BODY_TIMER);
                break;
            case CLIENT_REQUEST_BODY_COMPLETED:
                clientRequestMetricsHolder = (RequestMetricsStaticsHolder) cMessage
                        .getProperty(MetricsConstants.CLIENT_REQUEST_METRICS_HOLDER);
                clientRequestMetricsHolder.stopTimer(MetricsConstants.REQUEST_BODY_TIMER);
                clientRequestMetricsHolder.stopTimer(MetricsConstants.REQUEST_LIFE_TIMER);
                break;

            case SERVER_REQUEST_INITIATED:
                serverRequestMetricsHolder = (RequestMetricsStaticsHolder) cMessage
                        .getProperty(MetricsConstants.SERVER_REQUEST_METRICS_HOLDER);
                serverRequestMetricsHolder.startTimer(MetricsConstants.REQUEST_LIFE_TIMER);
                serverRequestMetricsHolder.startTimer(MetricsConstants.REQUEST_HEADER_TIMER);
                break;
            case SERVER_REQUEST_HEADERS_COMPLETED:
                serverRequestMetricsHolder = (RequestMetricsStaticsHolder) cMessage
                        .getProperty(MetricsConstants.SERVER_REQUEST_METRICS_HOLDER);
                serverRequestMetricsHolder.stopTimer(MetricsConstants.REQUEST_HEADER_TIMER);
                serverRequestMetricsHolder.startTimer(MetricsConstants.REQUEST_BODY_TIMER);
                break;

            case SERVER_REQUEST_BODY_COMPLETED:
                serverRequestMetricsHolder = (RequestMetricsStaticsHolder) cMessage
                        .getProperty(MetricsConstants.SERVER_REQUEST_METRICS_HOLDER);
                serverRequestMetricsHolder.stopTimer(MetricsConstants.REQUEST_BODY_TIMER);
                serverRequestMetricsHolder.stopTimer(MetricsConstants.REQUEST_LIFE_TIMER);

                break;
            default:
                return false;
            }

            return true;
        } else {
            if (cMessage.getProperty(MetricsConstants.SERVER_RESPONSE_METRICS_HOLDER) == null) {
                serverResponseMetricsHolder = new ResponseMetricsStaticsHolder(MetricsConstants.TYPE_SERVER_RESPONSE,
                        timerHolder);
                clientResponseMetricsHolder = new ResponseMetricsStaticsHolder(MetricsConstants.TYPE_CLIENT_RESPONSE,
                        timerHolder);

                cMessage.setProperty(MetricsConstants.CLIENT_RESPONSE_METRICS_HOLDER, clientResponseMetricsHolder);
                cMessage.setProperty(MetricsConstants.SERVER_RESPONSE_METRICS_HOLDER, serverResponseMetricsHolder);
            }
            switch (engagedLocation) {

            case SERVER_RESPONSE_INITIATED:
                serverResponseMetricsHolder = (ResponseMetricsStaticsHolder) cMessage
                        .getProperty(MetricsConstants.SERVER_RESPONSE_METRICS_HOLDER);
                serverResponseMetricsHolder.startTimer(MetricsConstants.RESPONSE_LIFE_TIMER);
                serverResponseMetricsHolder.startTimer(MetricsConstants.RESPONSE_HEADER_TIMER);
                break;
            case SERVER_RESPONSE_HEADERS_COMPLETED:
                serverResponseMetricsHolder = (ResponseMetricsStaticsHolder) cMessage
                        .getProperty(MetricsConstants.SERVER_RESPONSE_METRICS_HOLDER);
                serverResponseMetricsHolder.stopTimer(MetricsConstants.RESPONSE_HEADER_TIMER);
                serverResponseMetricsHolder.startTimer(MetricsConstants.RESPONSE_BODY_TIMER);
                break;
            case SERVER_RESPONSE_BODY_COMPLETED:
                serverResponseMetricsHolder = (ResponseMetricsStaticsHolder) cMessage
                        .getProperty(MetricsConstants.SERVER_RESPONSE_METRICS_HOLDER);
                serverResponseMetricsHolder.stopTimer(MetricsConstants.RESPONSE_BODY_TIMER);
                serverResponseMetricsHolder.stopTimer(MetricsConstants.RESPONSE_LIFE_TIMER);
                break;
            case CLIENT_RESPONSE_INITIATED:
                clientResponseMetricsHolder = (ResponseMetricsStaticsHolder) cMessage
                        .getProperty(MetricsConstants.CLIENT_RESPONSE_METRICS_HOLDER);
                clientResponseMetricsHolder.startTimer(MetricsConstants.RESPONSE_LIFE_TIMER);
                clientResponseMetricsHolder.startTimer(MetricsConstants.RESPONSE_HEADER_TIMER);
                break;
            case CLIENT_RESPONSE_HEADERS_COMPLETED:
                clientResponseMetricsHolder = (ResponseMetricsStaticsHolder) cMessage
                        .getProperty(MetricsConstants.CLIENT_RESPONSE_METRICS_HOLDER);
                clientResponseMetricsHolder.stopTimer(MetricsConstants.RESPONSE_HEADER_TIMER);
                clientResponseMetricsHolder.startTimer(MetricsConstants.RESPONSE_BODY_TIMER);
                break;
            case CLIENT_RESPONSE_BODY_COMPLETED:
                clientResponseMetricsHolder = (ResponseMetricsStaticsHolder) cMessage
                        .getProperty(MetricsConstants.CLIENT_RESPONSE_METRICS_HOLDER);
                clientResponseMetricsHolder.stopTimer(MetricsConstants.RESPONSE_LIFE_TIMER);
                clientResponseMetricsHolder.stopTimer(MetricsConstants.RESPONSE_BODY_TIMER);
                break;

            default:
                return false;
            }
            return true;
        }

    }

    @Override
    public String handlerName() {
        return "StatisticsHandler";
    }

}
