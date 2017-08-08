package org.wso2.carbon.transport.http.netty.contractImpl;

import org.wso2.carbon.transport.http.netty.contract.ConnectorFuture;
import org.wso2.carbon.transport.http.netty.contract.ConnectorListener;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

/**
 * Server connector future implementation
 */
public class HTTPConnectorFuture implements ConnectorFuture {

    private ConnectorListener connectorListener;

    @Override
    public void setConnectorListener(ConnectorListener connectorListener) {
        this.connectorListener = connectorListener;
    }

    @Override
    public void removeListener(ConnectorListener connectorListener) {
        this.connectorListener = null;
    }

    @Override
    public void notifyListener(HTTPCarbonMessage httpMessage) {
        connectorListener.onMessage(httpMessage);
    }
}
