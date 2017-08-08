package org.wso2.carbon.transport.http.netty.contractImpl;

import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

/**
 * MainRunner
 */
public class MainRunner implements Runnable {
    CarbonMessage httpMessage;

    public MainRunner(CarbonMessage carbonMessage) {
        this.httpMessage = carbonMessage;
    }

    @Override
    public void run() {
        System.out.println("Message received..");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String requestContentType = httpMessage.getHeader("Content-Type");
        httpMessage.getHeaders().clear();
        httpMessage.setHeader(requestContentType, "Content-Type");
        HTTPCarbonMessage httpCarbonMessage = (HTTPCarbonMessage) httpMessage;

        httpCarbonMessage.respond(httpCarbonMessage);

        System.out.println("Message received..");
        httpMessage.release();
    }
}
