package org.wso2.carbon.transport.http.netty.contractImpl;

import org.wso2.carbon.transport.http.netty.contract.HTTPConnectorListener;
import org.wso2.carbon.transport.http.netty.contract.ConnectorFuture;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

/**
 * ServerConnector observable the get returned upon creating
 * a new server connector
 */
public class ServerConnectorObseravable implements ConnectorFuture {

    HTTPConnectorListener observable;

    @Override
    public void setHTTPConnectorListener(HTTPConnectorListener connectorListener) {
        this.observable = connectorListener;
    }

    @Override
    public void removeHTTPListener(HTTPConnectorListener connectorListener) {
        if (this.observable == connectorListener) {
            this.observable = null;
        }
    }

    @Override
    public void notifyHTTPListener(HTTPCarbonMessage httpMessage) {
        this.observable.onMessage(httpMessage);
    }
}
