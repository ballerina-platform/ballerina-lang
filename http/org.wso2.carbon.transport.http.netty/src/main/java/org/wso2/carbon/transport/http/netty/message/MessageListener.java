package org.wso2.carbon.transport.http.netty.message;

import io.netty.handler.codec.http.HttpContent;

/**
 * Get notified when there is a state change in message
 */
public interface MessageListener {

    void onMessage(HttpContent httpContent);
}
