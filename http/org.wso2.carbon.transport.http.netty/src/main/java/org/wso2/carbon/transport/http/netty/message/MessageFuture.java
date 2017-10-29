package org.wso2.carbon.transport.http.netty.message;

import io.netty.handler.codec.http.HttpContent;

/**
 * Represents future contents of the message
 */
public class MessageFuture {

    private MessageListener messageListener;
    private HTTPCarbonMessage httpCarbonMessage;

    public MessageFuture(HTTPCarbonMessage httpCarbonMessage) {
        this.httpCarbonMessage = httpCarbonMessage;
    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
        while (!httpCarbonMessage.getBlockingEntityCollector().isEmpty()) {
            notifyMessageListener(httpCarbonMessage.getBlockingEntityCollector().getHttpContent());
        }
    }

    public void removeMessageListener() {
        this.messageListener = null;
    }

    public void notifyMessageListener(HttpContent httpContent) {
        if (this.messageListener != null) {
            this.messageListener.onMessage(httpContent);
        }
    }

    public HttpContent sync() {
        return this.httpCarbonMessage.getBlockingEntityCollector().getHttpContent();
    }
}
