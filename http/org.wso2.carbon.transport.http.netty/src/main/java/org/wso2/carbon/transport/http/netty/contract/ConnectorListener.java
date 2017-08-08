package org.wso2.carbon.transport.http.netty.contract;

import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Allows to get notifications.
 */
public interface ConnectorListener {
    void onMessage(CarbonMessage httpMessage);
    void onError(Throwable throwable);
}
