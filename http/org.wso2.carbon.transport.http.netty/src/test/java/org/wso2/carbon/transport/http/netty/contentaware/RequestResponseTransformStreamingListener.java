package org.wso2.carbon.transport.http.netty.contentaware;

import org.apache.commons.io.IOUtils;
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
import org.wso2.carbon.transport.http.netty.contractimpl.HTTPConnectorFactoryImpl;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.carbon.transport.http.netty.message.HTTPMessageUtil;
import org.wso2.carbon.transport.http.netty.util.TestUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Streaming processor which reads from same and write to same message
 */
public class RequestResponseTransformStreamingListener implements HTTPConnectorListener {

    private static final Logger logger = LoggerFactory.getLogger(RequestResponseTransformStreamingListener.class);
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private TransportsConfiguration configuration;

    public RequestResponseTransformStreamingListener(TransportsConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void onMessage(HTTPCarbonMessage httpRequestMessage) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream inputStream = httpRequestMessage.getInputStream();
                    OutputStream outputStream = httpRequestMessage.getOutputStream();
                    byte[] bytes = IOUtils.toByteArray(inputStream);
                    outputStream.write(bytes);
                    outputStream.flush();
                    httpRequestMessage.setEndOfMsgAdded(true);
                    httpRequestMessage.setProperty(Constants.HOST, TestUtil.TEST_HOST);
                    httpRequestMessage.setProperty(Constants.PORT, TestUtil.TEST_SERVER_PORT);

                    Map<String, Object> transportProperties = new HashMap<>();
                    Set<TransportProperty> transportPropertiesSet = configuration.getTransportProperties();
                    if (transportPropertiesSet != null && !transportPropertiesSet.isEmpty()) {
                        transportProperties = transportPropertiesSet.stream().collect(
                                Collectors.toMap(TransportProperty::getName, TransportProperty::getValue));

                    }

                    SenderConfiguration senderConfiguration = HTTPMessageUtil.getSenderConfiguration(configuration);

                    HTTPConnectorFactory httpConnectorFactory = new HTTPConnectorFactoryImpl();
                    HTTPClientConnector clientConnector =
                            httpConnectorFactory.getHTTPClientConnector(transportProperties, senderConfiguration);
                    HTTPClientConnectorFuture httpClientConnectorFuture = clientConnector.send(httpRequestMessage);
                    httpClientConnectorFuture.setHTTPConnectorListener(new HTTPConnectorListener() {
                        @Override
                        public void onMessage(HTTPCarbonMessage httpResponse) {
                            InputStream inputS = httpResponse.getInputStream();
                            OutputStream outputS = httpResponse.getOutputStream();
                            try {
                                byte[] bytes = IOUtils.toByteArray(inputS);
                                outputS.write(bytes);
                                outputS.flush();
                                httpResponse.setEndOfMsgAdded(true);
                            } catch (IOException e) {
                                throw new RuntimeException("Cannot read Input Stream from Response", e);
                            }
                            httpRequestMessage.respond(httpResponse);
                        }

                        @Override
                        public void onError(Throwable throwable) {

                        }
                    });
                } catch (Exception e) {
                    logger.error("Error while reading stream", e);
                }
            }
        });
    }

    @Override
    public void onError(Throwable throwable) {

    }
}
