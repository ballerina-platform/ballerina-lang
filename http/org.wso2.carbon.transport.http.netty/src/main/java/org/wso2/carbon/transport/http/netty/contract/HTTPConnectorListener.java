package org.wso2.carbon.transport.http.netty.contract;

import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

/**
 * Allows to get notifications.
 */
public interface HTTPConnectorListener {
    void onMessage(HTTPCarbonMessage httpMessage);
    void onError(Throwable throwable);
}
