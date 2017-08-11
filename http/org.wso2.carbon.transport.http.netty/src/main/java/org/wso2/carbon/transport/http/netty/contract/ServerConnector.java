package org.wso2.carbon.transport.http.netty.contract;

/**
 * Inlet of inbound messages.
 */
public interface ServerConnector {
    /**
     * Start the server connector which actually opens the port.
     * @return events related to the server connector.
     */
    ServerConnectorFuture start();

    /**
     * Stop the server connector which actually closes the port.
     * @return
     */
    boolean stop();
}
