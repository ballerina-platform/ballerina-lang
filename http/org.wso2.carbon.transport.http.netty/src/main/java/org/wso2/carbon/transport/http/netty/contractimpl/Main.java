package org.wso2.carbon.transport.http.netty.contractimpl;

import org.wso2.carbon.transport.http.netty.contract.ServerConnectorFuture;
import org.wso2.carbon.transport.http.netty.contract.HTTPConnectorListener;
import org.wso2.carbon.transport.http.netty.contract.ServerConnector;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Main Test
 */
public class Main {
    public static void main(String[] args) {

//        Executor executor = Executors.newSingleThreadExecutor();
//
//        HTTPConnectorFactoryImpl httpConnectorFactory = new HTTPConnectorFactoryImpl();
//
//        Map<String, String> props = new HashMap<>();
//        props.put(Constants.HOST, "localhost");
//        props.put(Constants.HTTP_PORT, "9009");
//
//        ServerConnector serverConnector = httpConnectorFactory.getServerConnector(props);
//        ServerConnectorFuture serverConnectorFuture = serverConnector.start();
//        serverConnectorFuture.setHTTPConnectorListener(new HTTPConnectorListener() {
//            @Override
//            public void onMessage(HTTPCarbonMessage httpMessage) {
//                executor.execute(new MainRunner(httpMessage, httpConnectorFactory));
//            }
//
//            @Override
//            public void onError(Throwable throwable) {
//                // Do something
//            }
//        });
//
//        try {
//            Thread.sleep(10000000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

}
