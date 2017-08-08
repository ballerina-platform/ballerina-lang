package org.wso2.carbon.transport.http.netty.contractImpl;

import org.wso2.carbon.transport.http.netty.contract.ConnectorListener;
import org.wso2.carbon.transport.http.netty.contract.ConnectorFuture;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

/**
 * ServerConnector observable the get returned upon creating
 * a new server connector
 */
public class ServerConnectorObseravable implements ConnectorFuture {

    ConnectorListener observable;

    @Override
    public void setConnectorListener(ConnectorListener connectorListener) {
        this.observable = connectorListener;
    }

    @Override
    public void removeListener(ConnectorListener connectorListener) {
        if (this.observable == connectorListener) {
            this.observable = null;
        }
    }

    @Override
    public void notifyListener(HTTPCarbonMessage httpMessage) {
        this.observable.onMessage(httpMessage);
    }
}
