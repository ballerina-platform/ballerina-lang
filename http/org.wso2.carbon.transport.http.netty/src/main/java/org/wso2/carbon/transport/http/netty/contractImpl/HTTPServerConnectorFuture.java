package org.wso2.carbon.transport.http.netty.contractImpl;

import org.wso2.carbon.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.carbon.transport.http.netty.contract.HTTPConnectorListener;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

/**
 * Server connector future implementation
 */
public class HTTPServerConnectorFuture implements ServerConnectorFuture {

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

    @Override
    public void setWSConnectorListener(HTTPConnectorListener connectorListener) {

    }

    @Override
    public void removeWSListener(HTTPConnectorListener connectorListener) {

    }

    @Override
    public void notifyWSListener(HTTPCarbonMessage httpMessage) {

    }
}
