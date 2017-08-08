package org.wso2.carbon.transport.http.netty.contract;

import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

/**
 * Allows to set listeners.
 */
public interface ServerConnectorFuture {
    void setHTTPConnectorListener(HTTPConnectorListener connectorListener);
    void removeHTTPListener(HTTPConnectorListener connectorListener);
    void notifyHTTPListener(HTTPCarbonMessage httpMessage);

    void setWSConnectorListener(HTTPConnectorListener connectorListener);
    void removeWSListener(HTTPConnectorListener connectorListener);
    void notifyWSListener(HTTPCarbonMessage httpMessage);
}
