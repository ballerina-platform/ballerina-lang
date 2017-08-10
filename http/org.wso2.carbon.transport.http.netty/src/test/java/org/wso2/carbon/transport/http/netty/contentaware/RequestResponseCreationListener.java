package org.wso2.carbon.transport.http.netty.contentaware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.MessageUtil;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;
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

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * A Message Processor which creates Request and Response
 */
public class RequestResponseCreationListener implements HTTPConnectorListener {
    private Logger logger = LoggerFactory.getLogger(RequestResponseCreationListener.class);

    private String responseValue;
    private TransportsConfiguration configuration;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public RequestResponseCreationListener(String responseValue, TransportsConfiguration configuration) {
        this.responseValue = responseValue;
        this.configuration = configuration;
    }

    private class ClientConnectorListener implements HTTPConnectorListener {
        String requestValue = "test";
        HTTPCarbonMessage request;

        public ClientConnectorListener(String requestValue, HTTPCarbonMessage request) {
            this.requestValue = requestValue;
            this.request = request;
        }

        @Override
        public void onMessage(HTTPCarbonMessage httpResponse) {
            int length = httpResponse.getFullMessageLength();
            List<ByteBuffer> byteBufferList = httpResponse.getFullMessageBody();

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
                byteBuff.flip();
                CarbonMessage carbonMessage = MessageUtil.cloneCarbonMessageWithOutData(httpResponse);
                if (carbonMessage.getHeader(Constants.HTTP_TRANSFER_ENCODING) == null) {
                    carbonMessage.setHeader(Constants.HTTP_CONTENT_LENGTH, String.valueOf(array.length));
                }
                carbonMessage.addMessageBody(byteBuff);
                carbonMessage.setEndOfMsgAdded(true);

                HTTPCarbonMessage httpCarbonMessage = HTTPMessageUtil.convertCarbonMessage(carbonMessage);
                request.respond(httpCarbonMessage);
            }
        }

        @Override
        public void onError(Throwable throwable) {

        }
    }

    @Override
    public void onMessage(HTTPCarbonMessage httpRequest) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    int length = httpRequest.getFullMessageLength();
                    List<ByteBuffer> byteBufferList = httpRequest.getFullMessageBody();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(length);
                    byteBufferList.forEach(buf -> byteBuffer.put(buf));
                    String requestValue = new String(byteBuffer.array());
                    byte[] arry = responseValue.getBytes("UTF-8");

                    CarbonMessage newMsg = MessageUtil.cloneCarbonMessageWithOutData(httpRequest);
                    if (newMsg.getHeader(Constants.HTTP_TRANSFER_ENCODING) == null) {
                        newMsg.setHeader(Constants.HTTP_CONTENT_LENGTH, String.valueOf(arry.length));
                    }
                    ByteBuffer byteBuffer1 = ByteBuffer.allocate(arry.length);
                    byteBuffer1.put(arry);
                    byteBuffer1.flip();
                    newMsg.addMessageBody(byteBuffer1);
                    newMsg.setEndOfMsgAdded(true);
                    newMsg.setProperty(Constants.HOST, TestUtil.TEST_HOST);
                    newMsg.setProperty(Constants.PORT, TestUtil.TEST_SERVER_PORT);

                    Map<String, Object> transportProperties = new HashMap<>();
                    Set<TransportProperty> transportPropertiesSet = configuration.getTransportProperties();
                    if (transportPropertiesSet != null && !transportPropertiesSet.isEmpty()) {
                        transportProperties = transportPropertiesSet.stream().collect(
                                Collectors.toMap(TransportProperty::getName, TransportProperty::getValue));

                    }

                    SenderConfiguration senderConfiguration = getSenderConfiguration(configuration);

                    HTTPConnectorFactory httpConnectorFactory = new HTTPConnectorFactoryImpl();
                    HTTPClientConnector clientConnector =
                            httpConnectorFactory.getHTTPClientConnector(transportProperties, senderConfiguration);

                    HTTPCarbonMessage httpCarbonMessage = HTTPMessageUtil.convertCarbonMessage(newMsg);
                    HTTPClientConnectorFuture clientConnectorFuture = clientConnector.send(httpCarbonMessage);
                    clientConnectorFuture
                            .setHTTPConnectorListener(new ClientConnectorListener(requestValue, httpRequest));
                } catch (UnsupportedEncodingException e) {
                    logger.error("Encoding is not supported", e);
                } catch (ClientConnectorException e) {
                    logger.error("MessageProcessor is not supported ", e);
                } catch (Exception e) {
                    logger.error("Failed to send the message to the back-end", e);
                } finally {
                }
            }

        });

    }

    @Override
    public void onError(Throwable throwable) {

    }

    private SenderConfiguration getSenderConfiguration(TransportsConfiguration transportsConfiguration) {
        Map<String, SenderConfiguration> senderConfigurations =
                transportsConfiguration.getSenderConfigurations().stream().collect(Collectors
                        .toMap(senderConf ->
                                senderConf.getScheme().toLowerCase(Locale.getDefault()), config -> config));
        return senderConfigurations.get("http");
    }


    //    private class EngineCallBack implements CarbonCallback {
    //
    //        String requestValue = "test";
    //        CarbonCallback requestCallBack;
    //
    //        EngineCallBack(String requestValue, CarbonCallback carbonCallback) {
    //            this.requestValue = requestValue;
    //            requestCallBack = carbonCallback;
    //        }
    //
    //        @Override
    //        public void done(CarbonMessage carbonMessage) {
    //            int length = carbonMessage.getFullMessageLength();
    //            List<ByteBuffer> byteBufferList = carbonMessage.getFullMessageBody();
    //
    //            ByteBuffer byteBuffer = ByteBuffer.allocate(length);
    //            byteBufferList.forEach(buf -> byteBuffer.put(buf));
    //            String responseValue = new String(byteBuffer.array()) + ":" + requestValue;
    //            if (requestValue != null) {
    //                byte[] array = new byte[0];
    //                try {
    //                    array = responseValue.getBytes("UTF-8");
    //                } catch (UnsupportedEncodingException e) {
    //
    //                }
    //                ByteBuffer byteBuff = ByteBuffer.allocate(array.length);
    //                byteBuff.put(array);
    //                byteBuff.flip();
    //                carbonMessage = MessageUtil.cloneCarbonMessageWithOutData(carbonMessage);
    //                if (carbonMessage.getHeader(Constants.HTTP_TRANSFER_ENCODING) == null) {
    //                    carbonMessage.setHeader(Constants.HTTP_CONTENT_LENGTH, String.valueOf(array.length));
    //                }
    //
    //                carbonMessage.addMessageBody(byteBuff);
    //                carbonMessage.setEndOfMsgAdded(true);
    //                requestCallBack.done(carbonMessage);
    //            }
    //
    //        }
    //    }


    //    @Override
    //    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {
    //        executor.execute(new Runnable() {
    //            @Override
    //            public void run() {
    //                try {
    //                    if (carbonMessage.getProperty(org.wso2.carbon.messaging.Constants.DIRECTION) != null
    //                            && carbonMessage.getProperty(org.wso2.carbon.messaging.Constants.DIRECTION)
    //                            .equals(org.wso2.carbon.messaging.Constants.DIRECTION_RESPONSE)) {
    //
    //                        carbonCallback.done(carbonMessage);
    //                    } else {
    //                        int length = carbonMessage.getFullMessageLength();
    //                        List<ByteBuffer> byteBufferList = carbonMessage.getFullMessageBody();
    //                        ByteBuffer byteBuffer = ByteBuffer.allocate(length);
    //                        byteBufferList.forEach(buf -> byteBuffer.put(buf));
    //                        String requestValue = new String(byteBuffer.array());
    //                        byte[] arry = responseValue.getBytes("UTF-8");
    //                        CarbonMessage newMsg = MessageUtil.cloneCarbonMessageWithOutData(carbonMessage);
    //                        if (newMsg.getHeader(Constants.HTTP_TRANSFER_ENCODING) == null) {
    //                            newMsg.setHeader(Constants.HTTP_CONTENT_LENGTH, String.valueOf(arry.length));
    //                        }
    //                        ByteBuffer byteBuffer1 = ByteBuffer.allocate(arry.length);
    //                        byteBuffer1.put(arry);
    //                        byteBuffer1.flip();
    //                        newMsg.addMessageBody(byteBuffer1);
    //                        newMsg.setEndOfMsgAdded(true);
    //                        newMsg.setProperty(Constants.HOST, TestUtil.TEST_HOST);
    //                        newMsg.setProperty(Constants.PORT, TestUtil.TEST_SERVER_PORT);
    //                        RequestResponseCreationListener.EngineCallBack engineCallBack =
    //                                new RequestResponseCreationListener.EngineCallBack(requestValue, carbonCallback);
    //                        clientConnector.send(newMsg, engineCallBack);
    //                    }
    //                } catch (UnsupportedEncodingException e) {
    //                    logger.error("Encoding is not supported", e);
    //                } catch (ClientConnectorException e) {
    //                    logger.error("MessageProcessor is not supported ", e);
    //                } finally {
    //                }
    //            }
    //
    //        });
    //
    //        return false;
    //    }
}
