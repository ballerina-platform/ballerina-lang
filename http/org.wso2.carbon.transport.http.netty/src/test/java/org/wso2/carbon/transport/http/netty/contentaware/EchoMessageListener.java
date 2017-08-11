package org.wso2.carbon.transport.http.netty.contentaware;

import io.netty.handler.codec.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.ClientConnector;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.contract.HTTPConnectorListener;
import org.wso2.carbon.transport.http.netty.contract.ServerConnectorException;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A Message processor which echos the incoming message
 */
public class EchoMessageListener implements HTTPConnectorListener {
    private static final Logger logger = LoggerFactory.getLogger(EchoMessageProcessor.class);

    private ClientConnector clientConnector;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void onMessage(HTTPCarbonMessage httpRequest) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
//                    if (carbonMessage.getProperty(org.wso2.carbon.messaging.Constants.DIRECTION) != null &&
//                            carbonMessage.getProperty(org.wso2.carbon.messaging.Constants.DIRECTION)
//                                    .equals(org.wso2.carbon.messaging.Constants.DIRECTION_RESPONSE)) {
//
//                        carbonCallback.done(carbonMessage);
//                    } else {
//                        int length = carbonMessage.getFullMessageLength();
//                        List<ByteBuffer> fullMessage = carbonMessage.getFullMessageBody();
//                        ByteBuffer byteBuffer = ByteBuffer.allocate(length);
//                        fullMessage.forEach(buffer -> byteBuffer.put(buffer));
//                        CarbonMessage cMsg = new DefaultCarbonMessage();
//                        cMsg.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
//                        cMsg.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(length));
//                        cMsg.setHeader(HttpHeaders.Names.CONTENT_TYPE, Constants.TEXT_PLAIN);
//                        cMsg.setProperty(Constants.HTTP_STATUS_CODE, 200);
//                        byteBuffer.flip();
//                        cMsg.addMessageBody(byteBuffer);
//                        cMsg.setEndOfMsgAdded(true);
//                        carbonCallback.done(cMsg);
//                    }

                    int length = httpRequest.getFullMessageLength();
                    List<ByteBuffer> fullMessage = httpRequest.getFullMessageBody();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(length);
                    fullMessage.forEach(buffer -> byteBuffer.put(buffer));
                    HTTPCarbonMessage cMsg = new HTTPCarbonMessage();
                    cMsg.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
                    cMsg.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(length));
                    cMsg.setHeader(HttpHeaders.Names.CONTENT_TYPE, Constants.TEXT_PLAIN);
                    cMsg.setProperty(Constants.HTTP_STATUS_CODE, 200);
                    byteBuffer.flip();
                    cMsg.addMessageBody(byteBuffer);
                    cMsg.setEndOfMsgAdded(true);
                    httpRequest.respond(cMsg);
                } catch (ServerConnectorException e) {
                    logger.error("Error occurred during message notification: " + e.getMessage());
                } finally {
                    // Calling the release method to make sure that there won't be any memory leaks from netty
                    httpRequest.release();
                }
            }
        });
    }

    @Override
    public void onError(Throwable throwable) {

    }
}
