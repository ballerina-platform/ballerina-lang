package org.wso2.carbon.transport.http.netty.message;

import io.netty.handler.codec.http.HttpContent;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Represents future contents of the message
 */
public class MessageFuture {

    private MessageListener messageListener;
    private HTTPCarbonMessage httpCarbonMessage;
    private ConcurrentLinkedQueue<HttpContent> pendingPayload;

     public MessageFuture(HTTPCarbonMessage httpCarbonMessage) {
        this.httpCarbonMessage = httpCarbonMessage;
        this.pendingPayload = new ConcurrentLinkedQueue<>();
//        while (!httpCarbonMessage.isEmpty()) {
//            HttpContent httpContent = httpCarbonMessage.getHttpContent();
//            pendingPayload.add(httpContent);
//        }
    }

    public synchronized void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
        while (!httpCarbonMessage.isEmpty()) {
            HttpContent httpContent = httpCarbonMessage.getHttpContent();
            notifyMessageListener(httpContent);
        }
        while (!pendingPayload.isEmpty()) {
            notifyMessageListener(pendingPayload.poll());
        }
    }

    public synchronized void removeMessageListener() {
        this.messageListener = null;
    }

    public synchronized void notifyMessageListener(HttpContent httpContent) {
        if (this.messageListener != null) {
            this.messageListener.onMessage(httpContent);
        } else {
            pendingPayload.add(httpContent);
        }
    }

    public synchronized HttpContent sync() {
        return this.httpCarbonMessage.getBlockingEntityCollector().getHttpContent();
    }
}
