package org.wso2.carbon.transport.http.netty.contract;

import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

/**
 * Allows to get notifications of connectors.
 */
public interface HTTPConnectorListener {
    /**
     * Each event triggered by connector ends up here.
     * @param httpMessage
     */
    void onMessage(HTTPCarbonMessage httpMessage);

    /**
     * Each error event triggered by connector ends up here.
     * @param throwable
     */
    void onError(Throwable throwable);
}
