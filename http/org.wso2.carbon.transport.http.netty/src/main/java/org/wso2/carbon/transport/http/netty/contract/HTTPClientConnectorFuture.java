package org.wso2.carbon.transport.http.netty.contract;

import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

/**
 * Represents the future events and results of connectors.
 */
public interface HTTPClientConnectorFuture {
    void setHTTPConnectorListener(HTTPConnectorListener connectorListener);
    void removeHTTPListener(HTTPConnectorListener connectorListener);
    void notifyHTTPListener(HTTPCarbonMessage httpMessage);
}
