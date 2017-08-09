package org.wso2.carbon.transport.http.netty.contractImpl;

import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.transport.http.netty.contract.HTTPClientConnector;
import org.wso2.carbon.transport.http.netty.contract.HTTPClientConnectorFuture;
import org.wso2.carbon.transport.http.netty.contract.HTTPConnectorFactory;
import org.wso2.carbon.transport.http.netty.contract.HTTPConnectorListener;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * MainRunner
 */
public class MainRunner implements Runnable {
    CarbonMessage httpMessage;
    HTTPConnectorFactory httpConnectorFactory;

    public MainRunner(CarbonMessage carbonMessage, HTTPConnectorFactory httpConnectorFactory) {
        this.httpMessage = carbonMessage;
        this.httpConnectorFactory = httpConnectorFactory;
    }

    @Override
    public void run() {
        System.out.println("Message received..");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Map<String, String> props = new HashMap<>();
        HTTPClientConnector clientConnector = httpConnectorFactory.getHTTPClientConnector(props);
        try {
            HTTPClientConnectorFuture clientConnectorFuture = clientConnector.send((HTTPCarbonMessage) httpMessage);
            clientConnectorFuture.setHTTPConnectorListener(new HTTPConnectorListener() {
                @Override
                public void onMessage(HTTPCarbonMessage httpMessage) {

                }

                @Override
                public void onError(Throwable throwable) {

                }
            });
        } catch (Exception e) {
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
