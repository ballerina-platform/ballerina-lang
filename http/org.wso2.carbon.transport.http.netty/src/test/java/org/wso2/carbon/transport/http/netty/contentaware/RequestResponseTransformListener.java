package org.wso2.carbon.transport.http.netty.contentaware;

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
import org.wso2.carbon.transport.http.netty.contract.ServerConnectorException;
import org.wso2.carbon.transport.http.netty.contractimpl.HTTPConnectorFactoryImpl;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.carbon.transport.http.netty.message.HTTPMessageUtil;
import org.wso2.carbon.transport.http.netty.util.TestUtil;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Transform message in request and response path
 */
public class RequestResponseTransformListener implements HTTPConnectorListener {

    private static final Logger logger = LoggerFactory.getLogger(RequestResponseTransformListener.class);

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private String responseValue;
    private String requestValue;
    private TransportsConfiguration configuration;

    public RequestResponseTransformListener(String responseValue, TransportsConfiguration configuration) {
        this.responseValue = responseValue;
        this.configuration = configuration;
    }

    @Override
    public void onMessage(HTTPCarbonMessage httpRequest) {
        executor.execute(() -> {
            try {
                int length = httpRequest.getFullMessageLength();
                List<ByteBuffer> byteBufferList = httpRequest.getFullMessageBody();

                ByteBuffer byteBuff = ByteBuffer.allocate(length);
                byteBufferList.forEach(buf -> byteBuff.put(buf));
                requestValue = new String(byteBuff.array());

                httpRequest.setProperty(Constants.HOST, TestUtil.TEST_HOST);
                httpRequest.setProperty(Constants.PORT, TestUtil.TEST_SERVER_PORT);

                if (responseValue != null) {
                    byte[] array = responseValue.getBytes("UTF-8");
                    ByteBuffer byteBuffer = ByteBuffer.allocate(array.length);
                    byteBuffer.put(array);
                    httpRequest.setHeader(Constants.HTTP_CONTENT_LENGTH, String.valueOf(array.length));
                    byteBuffer.flip();
                    httpRequest.addMessageBody(byteBuffer);
                    httpRequest.setEndOfMsgAdded(true);
                }

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
                httpRequest.setResponseListener(new HTTPConnectorListener() {
                    @Override
                    public void onMessage(HTTPCarbonMessage httpMessage) {
                        executor.execute(() -> {
                            int length = httpMessage.getFullMessageLength();
                            List<ByteBuffer> byteBufferList = httpMessage.getFullMessageBody();

                            ByteBuffer byteBuffer = ByteBuffer.allocate(length);
                            byteBufferList.forEach(buf -> byteBuffer.put(buf));
                            String responseValue = new String(byteBuffer.array()) + ":" + requestValue;
                            if (requestValue != null) {
                                byte[] array = new byte[0];
                                try {
                                    array = responseValue.getBytes("UTF-8");
                                } catch (UnsupportedEncodingException e) {

                                }
                                ByteBuffer byteBuff = ByteBuffer.allocate(array.length);
                                byteBuff.put(array);
                                httpMessage.setHeader(Constants.HTTP_CONTENT_LENGTH, String.valueOf(array.length));
                                byteBuff.flip();
                                httpMessage.addMessageBody(byteBuff);
                                httpMessage.setEndOfMsgAdded(true);
                                try {
                                    httpRequest.respond(httpMessage);
                                } catch (ServerConnectorException e) {
                                    logger.error("Error occurred during message notification: " + e.getMessage());
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }
                });
                clientConnector.send(httpRequest);
            } catch (Exception e) {
                logger.error("Error while reading stream", e);
            }
        });
    }

    @Override
    public void onError(Throwable throwable) {

    }
}
