package org.wso2.carbon.transport.http.netty.contractImpl;

import org.wso2.carbon.transport.http.netty.contract.ConnectorFuture;
import org.wso2.carbon.transport.http.netty.contract.HTTPConnectorListener;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

/**
 * Server connector future implementation
 */
public class HTTPConnectorFuture implements ConnectorFuture {

    private HTTPConnectorListener connectorListener;

    @Override
    public void setHTTPConnectorListener(HTTPConnectorListener connectorListener) {
        this.connectorListener = connectorListener;
    }

    @Override
    public void removeHTTPListener(HTTPConnectorListener connectorListener) {
        this.connectorListener = null;
    }

    @Override
    public void notifyHTTPListener(HTTPCarbonMessage httpMessage) {
        connectorListener.onMessage(httpMessage);
    }
}
