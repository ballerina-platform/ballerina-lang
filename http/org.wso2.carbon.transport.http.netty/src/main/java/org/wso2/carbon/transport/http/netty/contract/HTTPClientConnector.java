package org.wso2.carbon.transport.http.netty.contract;

import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

/**
 * Allows to send outbound messages.
 */
public interface HTTPClientConnector {
    /**
     * Creates the connection to the back-end.
     * @return the future that can be used to get future events of the connection.
     */
    HTTPClientConnectorFuture connect();

    /**
     * Send httpMessages to the back-end in asynchronous manner.
     * @return returns the status of the asynchronous send action.
     */
    HTTPClientConnectorFuture send(HTTPCarbonMessage httpCarbonMessage) throws Exception;

    /**
     * Close the connection related to this connector.
     * @return return the status of the close action.
     */
    boolean close();
}
