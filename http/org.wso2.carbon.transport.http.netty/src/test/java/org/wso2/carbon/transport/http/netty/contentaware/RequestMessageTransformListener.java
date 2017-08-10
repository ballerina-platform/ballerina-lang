package org.wso2.carbon.transport.http.netty.contentaware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.ClientConnector;
import org.wso2.carbon.messaging.TransportSender;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.contract.HTTPConnectorFactory;
import org.wso2.carbon.transport.http.netty.contract.HTTPConnectorListener;
import org.wso2.carbon.transport.http.netty.contractImpl.HTTPConnectorFactoryImpl;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.carbon.transport.http.netty.util.TestUtil;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Message processor for transform request message
 */
public class RequestMessageTransformListener implements HTTPConnectorListener {
    private Logger logger = LoggerFactory.getLogger(RequestMessageTransformListener.class);

    private String transformedValue;
    private ClientConnector clientConnector;
    private HTTPConnectorFactory httpConnectorFactory = new HTTPConnectorFactoryImpl();
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public RequestMessageTransformListener(String transformedValue) {
        this.transformedValue = transformedValue;
    }

    @Override
    public void onMessage(HTTPCarbonMessage httpRequest) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
//                if (carbonMessage.getProperty(org.wso2.carbon.messaging.Constants.DIRECTION) != null && carbonMessage
//                        .getProperty(org.wso2.carbon.messaging.Constants.DIRECTION)
//                        .equals(org.wso2.carbon.messaging.Constants.DIRECTION_RESPONSE)) {
//
//                    carbonCallback.done(carbonMessage);
//                } else {
//                    List<ByteBuffer> byteBufferList = carbonMessage.getFullMessageBody();
//                    carbonMessage.setProperty(Constants.HOST, TestUtil.TEST_HOST);
//                    carbonMessage.setProperty(Constants.PORT, TestUtil.TEST_SERVER_PORT);
//
//                    try {
//                        if (transformedValue != null) {
//                            byte[] array = transformedValue.getBytes("UTF-8");
//                            ByteBuffer byteBuffer = ByteBuffer.allocate(array.length);
//                            byteBuffer.put(array);
//                            carbonMessage.setHeader(Constants.HTTP_CONTENT_LENGTH, String.valueOf(array.length));
//                            byteBuffer.flip();
//                            carbonMessage.addMessageBody(byteBuffer);
//                            carbonMessage.setEndOfMsgAdded(true);
//                            clientConnector.send(carbonMessage, carbonCallback);
//                        }
//                    } catch (UnsupportedEncodingException e) {
//                        logger.error("Unsupported Exception", e);
//                    } catch (ClientConnectorException e) {
//                        logger.error("MessageProcessor Exception", e);
//                    }
//                }
                List<ByteBuffer> byteBufferList = httpRequest.getFullMessageBody();
                httpRequest.setProperty(Constants.HOST, TestUtil.TEST_HOST);
                httpRequest.setProperty(Constants.PORT, TestUtil.TEST_SERVER_PORT);

                try {
                    if (transformedValue != null) {
                        byte[] array = transformedValue.getBytes("UTF-8");
                        ByteBuffer byteBuffer = ByteBuffer.allocate(array.length);
                        byteBuffer.put(array);
                        httpRequest.setHeader(Constants.HTTP_CONTENT_LENGTH, String.valueOf(array.length));
                        byteBuffer.flip();
                        httpRequest.addMessageBody(byteBuffer);
                        httpRequest.setEndOfMsgAdded(true);

//                        clientConnector = httpConnectorFactory.getHTTPClientConnector();

//                        clientConnector.send(carbonMessage, carbonCallback);
                    }
                } catch (UnsupportedEncodingException e) {
                    logger.error("Unsupported Exception", e);
                } catch (Exception e) {
                    logger.error("MessageProcessor Exception", e);
                }
            }
        });
    }

    @Override
    public void onError(Throwable throwable) {

    }
}
