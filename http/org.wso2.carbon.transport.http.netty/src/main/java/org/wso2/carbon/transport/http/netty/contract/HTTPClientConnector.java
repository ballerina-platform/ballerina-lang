package org.wso2.carbon.transport.http.netty.contract;

import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

/**
 * Allows to send outbound messages
 */
public interface HTTPClientConnector {
    HTTPClientConnectorFuture connect();
    HTTPClientConnectorFuture send(HTTPCarbonMessage httpCarbonMessage) throws Exception;
    boolean stop();
}
