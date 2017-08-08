package org.wso2.carbon.transport.http.netty.contract;

import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Represents an inbound http request.
 */
public class HTTPMessage extends CarbonMessage {
    ConnectorListener connectorListener;

    public void setConnectorFuture(ConnectorListener connectorListener) {
        this.connectorListener = connectorListener;
    }
}
