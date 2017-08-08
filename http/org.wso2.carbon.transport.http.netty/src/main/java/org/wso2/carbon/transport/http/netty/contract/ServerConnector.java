package org.wso2.carbon.transport.http.netty.contract;

/**
 * Inlet of inbound messages
 */
public interface ServerConnector {
    ServerConnectorFuture start();
    boolean stop();
}
