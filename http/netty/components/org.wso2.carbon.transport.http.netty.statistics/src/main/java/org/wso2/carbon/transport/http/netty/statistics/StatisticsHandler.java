package org.wso2.carbon.transport.http.netty.statistics;

import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.EngagedLocation;
import org.wso2.carbon.messaging.MessagingHandler;
import org.wso2.carbon.transport.http.netty.common.Constants;

/**
 * Implementation of MessagingHandler
 */
public class StatisticsHandler implements MessagingHandler {

    private final TimerHolder timerHolder;

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
            if (cMessage.getProperty(Constants.CLIENT_CONNECTION_METRICS_HOLDER) != null) {

                serverConnectionMetricsHolder = new ConnectionMetricsStaticsHolder(Constants.TYPE_SOURCE_CONNECTION,
                        Constants.CONNECTION_TIMER, timerHolder);
                clientConnectionMetricsHolder = new ConnectionMetricsStaticsHolder(Constants.TYPE_CLIENT_CONNECTION,
                        Constants.CONNECTION_TIMER, timerHolder);
                serverRequestMetricsHolder = new RequestMetricsStaticsHolder(Constants.TYPE_SERVER_REQUEST,
                        timerHolder);
                clientRequestMetricsHolder = new RequestMetricsStaticsHolder(Constants.TYPE_CLIENT_REQUEST,
                        timerHolder);
                serverResponseMetricsHolder = new ResponseMetricsStaticsHolder(Constants.TYPE_SERVER_RESPONSE,
                        timerHolder);
                clientResponseMetricsHolder = new ResponseMetricsStaticsHolder(Constants.TYPE_CLIENT_RESPONSE,
                        timerHolder);

                cMessage.setProperty(Constants.CLIENT_RESPONSE_METRICS_HOLDER, clientResponseMetricsHolder);
                cMessage.setProperty(Constants.SERVER_RESPONSE_METRICS_HOLDER, serverResponseMetricsHolder);
                cMessage.setProperty(Constants.SERVER_REQUEST_METRICS_HOLDER, serverRequestMetricsHolder);
                cMessage.setProperty(Constants.CLIENT_REQUEST_METRICS_HOLDER, clientRequestMetricsHolder);
                cMessage.setProperty(Constants.SERVER_CONNECTION_METRICS_HOLDER, serverConnectionMetricsHolder);
                cMessage.setProperty(Constants.CLIENT_CONNECTION_METRICS_HOLDER, clientConnectionMetricsHolder);
            }

            switch (engagedLocation) {
            case CLIENT_CONNECTION_INITIATED:
                clientConnectionMetricsHolder = (ConnectionMetricsStaticsHolder) cMessage
                        .getProperty(Constants.CLIENT_CONNECTION_METRICS_HOLDER);
                clientConnectionMetricsHolder.startTimer(Constants.CONNECTION_TIMER);
                break;
            case CLIENT_CONNECTION_COMPLETED:
                clientConnectionMetricsHolder = (ConnectionMetricsStaticsHolder) cMessage
                        .getProperty(Constants.CLIENT_CONNECTION_METRICS_HOLDER);
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
                serverResponseMetricsHolder = (ConnectionMetricsStaticsHolder) cMessage
                        .getProperty(Constants.SERVER_RESPONSE_METRICS_HOLDER);
                serverResponseMetricsHolder.startTimer(Constants.RESPONSE_LIFE_TIMER);
                serverResponseMetricsHolder.startTimer(Constants.RESPONSE_HEADER_READ_TIMER);
                break;
            case SERVER_RESPONSE_READ_HEADERS_COMPLETED:
                serverResponseMetricsHolder = (ConnectionMetricsStaticsHolder) cMessage
                        .getProperty(Constants.SERVER_RESPONSE_METRICS_HOLDER);
                serverResponseMetricsHolder.stopTimer(Constants.RESPONSE_HEADER_READ_TIMER);
                serverResponseMetricsHolder.startTimer(Constants.RESPONSE_BODY_READ_TIMER);
                break;
            case SERVER_RESPONSE_READ_BODY_COMPLETED:
                serverResponseMetricsHolder = (ConnectionMetricsStaticsHolder) cMessage
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
