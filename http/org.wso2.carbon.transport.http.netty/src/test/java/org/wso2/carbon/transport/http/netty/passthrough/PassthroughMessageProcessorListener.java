package org.wso2.carbon.transport.http.netty.passthrough;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.config.SenderConfiguration;
import org.wso2.carbon.transport.http.netty.config.TransportProperty;
import org.wso2.carbon.transport.http.netty.config.TransportsConfiguration;
import org.wso2.carbon.transport.http.netty.contract.HTTPClientConnector;
import org.wso2.carbon.transport.http.netty.contract.HTTPClientConnectorFuture;
import org.wso2.carbon.transport.http.netty.contract.HTTPConnectorFactory;
import org.wso2.carbon.transport.http.netty.contract.HTTPConnectorListener;
import org.wso2.carbon.transport.http.netty.contractImpl.HTTPConnectorFactoryImpl;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.carbon.transport.http.netty.message.HTTPMessageUtil;
import org.wso2.carbon.transport.http.netty.util.TestUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * A Message Processor class to be used for test pass through scenarios
 */
public class PassthroughMessageProcessorListener implements HTTPConnectorListener {

    private static final Logger logger = LoggerFactory.getLogger(PassthroughMessageProcessorListener.class);
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private TransportsConfiguration configuration;
    private HTTPClientConnector clientConnector;

    public PassthroughMessageProcessorListener(TransportsConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void onMessage(HTTPCarbonMessage httpRequestMessage) {
        executor.execute(() -> {
            httpRequestMessage.setProperty(Constants.HOST, TestUtil.TEST_HOST);
            httpRequestMessage.setProperty(Constants.PORT, TestUtil.TEST_SERVER_PORT);
            try {
                Map<String, Object> transportProperties = new HashMap<>();
                Set<TransportProperty> transportPropertiesSet = configuration.getTransportProperties();
                if (transportPropertiesSet != null && !transportPropertiesSet.isEmpty()) {
                    transportProperties = transportPropertiesSet.stream().collect(
                            Collectors.toMap(TransportProperty::getName, TransportProperty::getValue));

                }

                SenderConfiguration senderConfiguration = HTTPMessageUtil.getSenderConfiguration(configuration);

                HTTPConnectorFactory httpConnectorFactory = new HTTPConnectorFactoryImpl();
                clientConnector =
                        httpConnectorFactory.getHTTPClientConnector(transportProperties, senderConfiguration);

                httpRequestMessage.setResponseListener(new HTTPConnectorListener() {
                    @Override
                    public void onMessage(HTTPCarbonMessage httpResponse) {
                        executor.execute(() -> httpRequestMessage.respond(httpResponse));
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }
                });
                clientConnector.send(httpRequestMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onError(Throwable throwable) {

    }
}
