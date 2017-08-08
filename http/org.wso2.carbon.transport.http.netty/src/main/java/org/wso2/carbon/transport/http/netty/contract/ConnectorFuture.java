package org.wso2.carbon.transport.http.netty.contract;

import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

/**
 * Allows to set listeners.
 */
public interface ConnectorFuture {
    void setConnectorListener(ConnectorListener connectorListener);
    void removeListener(ConnectorListener connectorListener);
    void notifyListener(HTTPCarbonMessage httpMessage);
}
