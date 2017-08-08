package org.wso2.carbon.transport.http.netty.contractImpl;

import org.wso2.carbon.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.carbon.transport.http.netty.contract.HTTPConnectorListener;
import org.wso2.carbon.transport.http.netty.contract.ServerConnector;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.transport.http.netty.common.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Main Test
 */
public class Main {
    public static void main(String[] args) {

        HTTPConnectorFactoryImpl httpConnectorFactory = new HTTPConnectorFactoryImpl();

        Map<String, String> props = new HashMap<>();
        props.put(Constants.HOST, "localhost");
        props.put(Constants.HTTP_PORT, "9009");

        ServerConnector serverConnector = httpConnectorFactory.getServerConnector(props);
        ServerConnectorFuture serverConnectorFuture = serverConnector.start();
        serverConnectorFuture.setHTTPConnectorListener(new HTTPConnectorListener() {
            @Override
            public void onMessage(CarbonMessage httpMessage) {
                System.out.println("Message received..");
                httpMessage.release();
            }

            @Override
            public void onError(Throwable throwable) {
                // Do something
            }
        });

//        HTTPConnectorFactory httpConnectorFactory = new HTTPConnectorFactory() {
//            @Override
//            public ServerConnectorFuture getServerConnector(Properties connectorConfig) {
//                return null;
//            }
//
//            @Override
//            public HTTPConnectorListener getHTTPClientConnector(Properties connectorConfig) {
//                return null;
//            }
//        };
//
//        ServerConnectorFuture observable = httpConnectorFactory.getServerConnector();
//
//        observable.setHTTPConnectorListener((httpMessage, callback) -> {
//            // you get your message
//        });
//
//        HTTPConnectorListener observer = httpConnectorFactory.getHTTPClientConnector();
//        observer.onMessage();
//
//        observable.notifyHTTPListener();

//        XMLReader xmlReader = new XMLReaderFactory.createXMLReader("sss");
    }
}
