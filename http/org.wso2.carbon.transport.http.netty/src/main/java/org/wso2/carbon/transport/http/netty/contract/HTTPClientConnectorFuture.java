package org.wso2.carbon.transport.http.netty.contract;

import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

/**
 * HTTPClientConnectorFuture
 */
public interface HTTPClientConnectorFuture {
    void setHTTPConnectorListener(HTTPConnectorListener connectorListener);
    void removeHTTPListener(HTTPConnectorListener connectorListener);
    void notifyHTTPListener(HTTPCarbonMessage httpMessage);
}
