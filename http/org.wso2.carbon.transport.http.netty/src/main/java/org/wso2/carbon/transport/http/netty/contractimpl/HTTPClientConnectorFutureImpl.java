package org.wso2.carbon.transport.http.netty.contractimpl;

import org.wso2.carbon.transport.http.netty.contract.HTTPClientConnectorFuture;
import org.wso2.carbon.transport.http.netty.contract.HTTPConnectorListener;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

/**
 * HTTPClientConnectorFutureImpl
 */
public class HTTPClientConnectorFutureImpl implements HTTPClientConnectorFuture {

    private HTTPConnectorListener httpConnectorListener;

    @Override
    public void setHTTPConnectorListener(HTTPConnectorListener connectorListener) {
        this.httpConnectorListener = connectorListener;
    }

    @Override
    public void removeHTTPListener(HTTPConnectorListener connectorListener) {
        this.httpConnectorListener = null;
    }

    @Override
    public void notifyHTTPListener(HTTPCarbonMessage httpMessage) {
        httpConnectorListener.onMessage(httpMessage);
    }
}
