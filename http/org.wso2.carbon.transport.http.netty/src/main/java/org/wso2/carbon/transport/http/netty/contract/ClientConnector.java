package org.wso2.carbon.transport.http.netty.contract;

/**
 * Allows to send outbound messages
 */
public interface ClientConnector {
    ConnectorFuture connect();
    ConnectorFuture send();
    boolean stop();
}
