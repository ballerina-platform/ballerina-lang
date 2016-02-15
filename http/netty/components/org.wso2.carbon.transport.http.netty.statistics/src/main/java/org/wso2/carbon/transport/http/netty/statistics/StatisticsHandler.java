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

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of MessagingHandler
 */
public class StatisticsHandler implements MessagingHandler {

    private final TimerHolder timerHolder;
    private Map<String, ConnectionMetricsStaticsHolder> messageCorelation;
    private String key;

    public StatisticsHandler(TimerHolder timerHolder) {

        // Initialize the timerHolder
        this.timerHolder = new TimerHolder();

    }

    @Override public boolean invoke(CarbonMessage cMessage, EngagedLocation engagedLocation) {

        MetricsStaticsHolder serverConnectionMetricsHolder;
        MetricsStaticsHolder clientConnectionMetricsHolder;
        MetricsStaticsHolder serverRequestMetricsHolder;
        MetricsStaticsHolder clientRequestMetricsHolder;
        MetricsStaticsHolder serverResponseMetricsHolder;
        MetricsStaticsHolder clientResponseMetricsHolder;

        if (cMessage != null) {
            if (cMessage.getProperty(Constants.CONNECTION_ID) != null) {
                if (cMessage.getProperty(Constants.CLIENT_CONNECTION_METRICS_HOLDER) == null
                        && engagedLocation == EngagedLocation.CLIENT_CONNECTION_INITIATED) {

                    clientConnectionMetricsHolder = new ConnectionMetricsStaticsHolder(Constants.TYPE_CLIENT_CONNECTION,
                            Constants.CONNECTION_TIMER, timerHolder);

                    cMessage.setProperty(Constants.CLIENT_CONNECTION_METRICS_HOLDER, clientConnectionMetricsHolder);

                    if (messageCorelation == null) {
                        this.messageCorelation = new HashMap<String, ConnectionMetricsStaticsHolder>();
                    }
                    key = cMessage.getProperty(Constants.CONNECTION_ID).toString();
                    messageCorelation.put(key, (ConnectionMetricsStaticsHolder) cMessage
                            .getProperty(Constants.CLIENT_CONNECTION_METRICS_HOLDER));
                }
            } else {
                if (cMessage.getProperty(Constants.CLIENT_REQUEST_METRICS_HOLDER) == null) {

                    serverRequestMetricsHolder = new RequestMetricsStaticsHolder(Constants.TYPE_SERVER_REQUEST,
                            timerHolder);
                    clientRequestMetricsHolder = new RequestMetricsStaticsHolder(Constants.TYPE_CLIENT_REQUEST,
                            timerHolder);
                    serverResponseMetricsHolder = new ResponseMetricsStaticsHolder(Constants.TYPE_SERVER_RESPONSE,
                            timerHolder);
                    clientResponseMetricsHolder = new ResponseMetricsStaticsHolder(Constants.TYPE_CLIENT_RESPONSE,
                            timerHolder);
                    serverConnectionMetricsHolder = new ConnectionMetricsStaticsHolder(Constants.TYPE_SOURCE_CONNECTION,
                            Constants.CONNECTION_TIMER, timerHolder);

                    cMessage.setProperty(Constants.SERVER_CONNECTION_METRICS_HOLDER, serverConnectionMetricsHolder);
                    cMessage.setProperty(Constants.CLIENT_RESPONSE_METRICS_HOLDER, clientResponseMetricsHolder);
                    cMessage.setProperty(Constants.SERVER_RESPONSE_METRICS_HOLDER, serverResponseMetricsHolder);
                    cMessage.setProperty(Constants.SERVER_REQUEST_METRICS_HOLDER, serverRequestMetricsHolder);
                    cMessage.setProperty(Constants.CLIENT_REQUEST_METRICS_HOLDER, clientRequestMetricsHolder);

                }
            }

            switch (engagedLocation) {
            case CLIENT_CONNECTION_INITIATED:
                clientConnectionMetricsHolder = (ConnectionMetricsStaticsHolder) cMessage
                        .getProperty(Constants.CLIENT_CONNECTION_METRICS_HOLDER);
                clientConnectionMetricsHolder.startTimer(Constants.CONNECTION_TIMER);
                break;
            case CLIENT_CONNECTION_COMPLETED:
                clientConnectionMetricsHolder = messageCorelation.get(this.key);
                clientConnectionMetricsHolder.stopTimer(Constants.CONNECTION_TIMER);
                break;
            case CLIENT_REQEST_READ_INITIATED:
                clientRequestMetricsHolder = (RequestMetricsStaticsHolder) cMessage
                        .getProperty(Constants.CLIENT_REQUEST_METRICS_HOLDER);
                clientRequestMetricsHolder.startTimer(Constants.REQUEST_LIFE_TIMER);
                clientRequestMetricsHolder.startTimer(Constants.REQUEST_HEADER_READ_TIMER);
                break;
            case CLIENT_REQUEST_READ_HEADERS_COMPLETED:
                clientRequestMetricsHolder = (RequestMetricsStaticsHolder) cMessage
                        .getProperty(Constants.CLIENT_REQUEST_METRICS_HOLDER);
                clientRequestMetricsHolder.stopTimer(Constants.REQUEST_HEADER_READ_TIMER);
                clientRequestMetricsHolder.startTimer(Constants.REQUEST_BODY_READ_TIMER);
                break;
            case CLIENT_REQUEST_READ_BODY_COMPLETED:
                clientRequestMetricsHolder = (RequestMetricsStaticsHolder) cMessage
                        .getProperty(Constants.CLIENT_REQUEST_METRICS_HOLDER);
                clientRequestMetricsHolder.stopTimer(Constants.REQUEST_BODY_READ_TIMER);
                clientRequestMetricsHolder.stopTimer(Constants.REQUEST_LIFE_TIMER);
                break;
            case SERVER_CONNECTION_INITIATED:
                serverConnectionMetricsHolder = (ConnectionMetricsStaticsHolder) cMessage
                        .getProperty(Constants.SERVER_CONNECTION_METRICS_HOLDER);
                serverConnectionMetricsHolder.startTimer(Constants.CONNECTION_TIMER);
                break;
            case SERVER_CONNECTION_COMPLETED:

                serverConnectionMetricsHolder = (ConnectionMetricsStaticsHolder) cMessage
                        .getProperty(Constants.SERVER_CONNECTION_METRICS_HOLDER);
                serverConnectionMetricsHolder.stopTimer(Constants.CONNECTION_TIMER);
                break;
            case SERVER_RESPONSE_READ_INITIATED:
                serverResponseMetricsHolder = (ResponseMetricsStaticsHolder) cMessage
                        .getProperty(Constants.SERVER_RESPONSE_METRICS_HOLDER);
                serverResponseMetricsHolder.startTimer(Constants.RESPONSE_LIFE_TIMER);
                serverResponseMetricsHolder.startTimer(Constants.RESPONSE_HEADER_READ_TIMER);
                break;
            case SERVER_RESPONSE_READ_HEADERS_COMPLETED:
                serverResponseMetricsHolder = (ResponseMetricsStaticsHolder) cMessage
                        .getProperty(Constants.SERVER_RESPONSE_METRICS_HOLDER);
                serverResponseMetricsHolder.stopTimer(Constants.RESPONSE_HEADER_READ_TIMER);
                serverResponseMetricsHolder.startTimer(Constants.RESPONSE_BODY_READ_TIMER);
                break;
            case SERVER_RESPONSE_READ_BODY_COMPLETED:
                serverResponseMetricsHolder = (ResponseMetricsStaticsHolder) cMessage
                        .getProperty(Constants.SERVER_RESPONSE_METRICS_HOLDER);
                serverResponseMetricsHolder.stopTimer(Constants.RESPONSE_BODY_READ_TIMER);
                serverResponseMetricsHolder.stopTimer(Constants.RESPONSE_LIFE_TIMER);
                break;
            case SERVER_REQUEST_WRITE_INITIATED:
                serverRequestMetricsHolder = (RequestMetricsStaticsHolder) cMessage
                        .getProperty(Constants.SERVER_REQUEST_METRICS_HOLDER);
                serverRequestMetricsHolder.startTimer(Constants.REQUEST_HEADER_WRITE_TIMER);
                break;
            case SERVER_REQUEST_WRITE_HEADERS_COMPLETED:
                serverRequestMetricsHolder = (RequestMetricsStaticsHolder) cMessage
                        .getProperty(Constants.SERVER_REQUEST_METRICS_HOLDER);
                serverRequestMetricsHolder.stopTimer(Constants.REQUEST_HEADER_WRITE_TIMER);
                serverRequestMetricsHolder.startTimer(Constants.REQUEST_BODY_WRITE_TIMER);
                break;

            case SERVER_REQUEST_WIRTE_BODY_COMPLETED:
                serverRequestMetricsHolder = (RequestMetricsStaticsHolder) cMessage
                        .getProperty(Constants.SERVER_REQUEST_METRICS_HOLDER);
                serverRequestMetricsHolder.stopTimer(Constants.REQUEST_BODY_WRITE_TIMER);
                break;
            default:
                return false;
            }
            return true;
        }
        return false;
    }

    @Override public String handlerName() {
        return "StatisticsHandler";
    }

}
