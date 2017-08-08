package org.wso2.carbon.transport.http.netty.contract;

/**
 * Allows to send outbound messages
 */
public interface HTTPClientConnector {
    HTTPClientConnectorFuture connect();
    HTTPClientConnectorFuture send();
    boolean stop();
}
