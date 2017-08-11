package org.wso2.carbon.transport.http.netty.contract;

import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

/**
 * Represents the future events and results of connectors.
 */
public interface HTTPClientConnectorFuture {
    /**
     * Set listener for the connector future.
     * @param connectorListener that receives events related to the connector.
     */
    void setHTTPConnectorListener(HTTPConnectorListener connectorListener);

    /**
     * Remove the listener set to the future.
     */
    void removeHTTPListener();

    /**
     * Notify the listeners when there is an event
     * @param httpMessage contains the data related to the event.
     */
    void notifyHTTPListener(HTTPCarbonMessage httpMessage);
}
