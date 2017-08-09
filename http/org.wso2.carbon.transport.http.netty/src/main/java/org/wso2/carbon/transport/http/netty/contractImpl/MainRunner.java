package org.wso2.carbon.transport.http.netty.contractImpl;

import org.eclipse.equinox.internal.util.impl.tpt.threadpool.Executor;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.contract.HTTPClientConnector;
import org.wso2.carbon.transport.http.netty.contract.HTTPClientConnectorFuture;
import org.wso2.carbon.transport.http.netty.contract.HTTPConnectorFactory;
import org.wso2.carbon.transport.http.netty.contract.HTTPConnectorListener;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * MainRunner
 */
public class MainRunner implements Runnable {
    HTTPCarbonMessage request;
    HTTPConnectorFactory httpConnectorFactory;

    public MainRunner(HTTPCarbonMessage carbonMessage, HTTPConnectorFactory httpConnectorFactory) {
        this.request = carbonMessage;
        this.httpConnectorFactory = httpConnectorFactory;
    }

    @Override
    public void run() {
        System.out.println("Message received..");
        Map<String, String> props = new HashMap<>();
        HTTPClientConnector clientConnector = httpConnectorFactory.getHTTPClientConnector(props);
        try {
            request.setProperty(Constants.HOST, "localhost");
            request.setProperty(Constants.PORT, 8688);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            HTTPClientConnectorFuture clientConnectorFuture = clientConnector.send(request);
            clientConnectorFuture.setHTTPConnectorListener(new HTTPConnectorListener() {
                @Override
                public void onMessage(HTTPCarbonMessage httpMessage) {
                    Executors.newSingleThreadExecutor().execute(new Runnable() {
                        @Override
                        public void run() {
                            String requestContentType = httpMessage.getHeader("Content-Type");
                            httpMessage.getHeaders().clear();
                            httpMessage.setHeader(requestContentType, "Content-Type");
                            HTTPCarbonMessage httpCarbonMessage = (HTTPCarbonMessage) httpMessage;

                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            request.respond(httpCarbonMessage);
                            request.release();
                        }
                    });
                }

                @Override
                public void onError(Throwable throwable) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

//        String requestContentType = httpMessage.getHeader("Content-Type");
//        httpMessage.getHeaders().clear();
//        httpMessage.setHeader(requestContentType, "Content-Type");
//        HTTPCarbonMessage httpCarbonMessage = (HTTPCarbonMessage) httpMessage;
//
//        httpCarbonMessage.respond(httpCarbonMessage);

        System.out.println("Message received..");
    }
}
