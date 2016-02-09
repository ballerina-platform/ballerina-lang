package org.wso2.carbon.transport.http.netty.latency.metrics;

import org.wso2.carbon.transport.http.netty.NettyCarbonMessage;
import org.wso2.carbon.transport.http.netty.common.TransportConstants;

/**
 * Created by chamile on 2/9/16.
 */
public class StaticsHandler {

    private String metricsStatus, timerStatus;
    private int start = 0;
    private int stop = 1;

    private MetricsStaticsHolder serverConnectionMetricsHolder;
    private MetricsStaticsHolder clientConnectionMetricsHolder;
    private MetricsStaticsHolder serverRequestMetricsHolder;
    private MetricsStaticsHolder clientRequestMetricsHolder;
    private MetricsStaticsHolder serverResponseMetricsHolder;
    private MetricsStaticsHolder clientResponseMetricsHolder;


    public StaticsHandler(TimerHandler timerHandler) {

        // Initialize the metric holders
        this.serverConnectionMetricsHolder = new ConnectionMetricsStaticsHolder(
                TransportConstants.TYPE_SOURCE_CONNECTION, TransportConstants.CONNECTION_TIMER);
        this.clientConnectionMetricsHolder = new ConnectionMetricsStaticsHolder(
                TransportConstants.TYPE_CLIENT_CONNECTION, TransportConstants.CONNECTION_TIMER);
        this.serverRequestMetricsHolder = new RequestMetricsStaticsHolder(
                TransportConstants.TYPE_SERVER_REQUEST, timerHandler);
        this.clientRequestMetricsHolder = new RequestMetricsStaticsHolder(
                TransportConstants.TYPE_CLIENT_REQUEST, timerHandler);
        this.serverResponseMetricsHolder = new ResponseMetricsStaticsHolder(
                TransportConstants.TYPE_SERVER_RESPONSE, timerHandler);
        this.clientResponseMetricsHolder = new ResponseMetricsStaticsHolder(
                TransportConstants.TYPE_CLIENT_RESPONSE, timerHandler);
    }

    public boolean invoke(NettyCarbonMessage carbonMessage) {
        if (carbonMessage != null) {
            if (carbonMessage.getProperty(metricsStatus) == null) {
                carbonMessage.setProperty(metricsStatus, TransportConstants.REQUEST_LIFE_TIMER);
                carbonMessage.setProperty(timerStatus, start);
                serverConnectionMetricsHolder.startTimer(TransportConstants.CONNECTION_TIMER);
            } else {
                String status = carbonMessage.getProperty(metricsStatus).toString();

                switch (status) {
                    case TransportConstants.REQUEST_LIFE_TIMER:
                        if ((Boolean) carbonMessage.getProperty(timerStatus)) {
                            serverRequestMetricsHolder.startTimer(TransportConstants.REQUEST_LIFE_TIMER);
                            carbonMessage.setProperty(metricsStatus, TransportConstants.REQUEST_HEADER_READ_TIMER);
                            carbonMessage.setProperty(timerStatus, start);
                        } else {
                            serverRequestMetricsHolder.stopTimer(TransportConstants.REQUEST_HEADER_READ_TIMER);
                            carbonMessage.setProperty(metricsStatus, TransportConstants.CONNECTION_TIMER);
                            carbonMessage.setProperty(timerStatus, stop);
                        }
                        break;
                    case TransportConstants.REQUEST_HEADER_READ_TIMER:
                        if ((Boolean) carbonMessage.getProperty(timerStatus)) {
                            serverRequestMetricsHolder.startTimer(TransportConstants.REQUEST_HEADER_READ_TIMER);
                            carbonMessage.setProperty(timerStatus, stop);
                        } else {
                            serverRequestMetricsHolder.stopTimer(TransportConstants.REQUEST_HEADER_READ_TIMER);
                            carbonMessage.setProperty(metricsStatus, TransportConstants.REQUEST_BODY_READ_TIMER);
                            carbonMessage.setProperty(timerStatus, start);
                        }
                        break;
                    case TransportConstants.REQUEST_BODY_READ_TIMER:
                        if ((Boolean) carbonMessage.getProperty(timerStatus)) {
                            serverRequestMetricsHolder.startTimer(TransportConstants.REQUEST_BODY_READ_TIMER);
                            carbonMessage.setProperty(timerStatus, stop);
                        } else {
                            serverRequestMetricsHolder.stopTimer(TransportConstants.REQUEST_BODY_READ_TIMER);
                            carbonMessage.setProperty(metricsStatus, TransportConstants.REQUEST_LIFE_TIMER);
                            carbonMessage.setProperty(timerStatus, stop);
                        }
                        break;
                    case TransportConstants.CONNECTION_TIMER:
                        if ((Boolean) carbonMessage.getProperty(timerStatus)) {
                            serverRequestMetricsHolder.startTimer(TransportConstants.REQUEST_BODY_READ_TIMER);
                            carbonMessage.setProperty(metricsStatus, TransportConstants.REQUEST_LIFE_TIMER);
                        }else {
                            serverRequestMetricsHolder.stopTimer(TransportConstants.CONNECTION_TIMER);
                            carbonMessage.setProperty(metricsStatus, TransportConstants.CONNECTION_TIMER);
                            carbonMessage.setProperty(timerStatus, start);
                        }
                        break;
                    case TransportConstants.REQUEST_LIFE_TIMER:
                        serverRequestMetricsHolder.startTimer(TransportConstants.REQUEST_LIFE_TIMER);
                        carbonMessage.setProperty(metricsStatus, TransportConstants.REQUEST_LIFE_TIMER);
                        break;
                    case TransportConstants.REQUEST_LIFE_TIMER:
                        serverRequestMetricsHolder.startTimer(TransportConstants.REQUEST_LIFE_TIMER);
                        carbonMessage.setProperty(metricsStatus, TransportConstants.REQUEST_LIFE_TIMER);
                        break;

                }
            }


        }

        serverConnectionMetricsHolder.startTimer(TransportConstants.CONNECTION_TIMER);
        return false;
    }

}
