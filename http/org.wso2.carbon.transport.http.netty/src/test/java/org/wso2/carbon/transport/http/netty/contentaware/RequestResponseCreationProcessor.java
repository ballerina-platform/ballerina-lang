/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.transport.http.netty.contentaware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.MessageProcessorException;
import org.wso2.carbon.messaging.MessageUtil;
import org.wso2.carbon.messaging.TransportSender;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.util.TestUtil;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A Message Processor which creates Request and Response
 */
public class RequestResponseCreationProcessor implements CarbonMessageProcessor {

    private Logger logger = LoggerFactory.getLogger(RequestMessageTransformProcessor.class);

    private String responseValue;

    private TransportSender transportSender;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public RequestResponseCreationProcessor(String responseValue) {
        this.responseValue = responseValue;
    }

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (carbonMessage.getProperty(org.wso2.carbon.messaging.Constants.DIRECTION) != null
                            && carbonMessage.getProperty(org.wso2.carbon.messaging.Constants.DIRECTION)
                            .equals(org.wso2.carbon.messaging.Constants.DIRECTION_RESPONSE)) {

                        carbonCallback.done(carbonMessage);
                    } else {
                        int length = carbonMessage.getFullMessageLength();
                        List<ByteBuffer> byteBufferList = carbonMessage.getFullMessageBody();
                        ByteBuffer byteBuffer = ByteBuffer.allocate(length);
                        byteBufferList.forEach(buf -> byteBuffer.put(buf));
                        String requestValue = new String(byteBuffer.array());
                        byte[] arry = responseValue.getBytes("UTF-8");
                        CarbonMessage newMsg = MessageUtil.cloneCarbonMessageWithOutData(carbonMessage);
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
                        EngineCallBack engineCallBack = new EngineCallBack(requestValue, carbonCallback);
                        transportSender.send(newMsg, engineCallBack);
                    }
                } catch (UnsupportedEncodingException e) {
                    logger.error("Encoding is not supported", e);
                } catch (MessageProcessorException e) {
                    logger.error("MessageProcessor is not supported ", e);
                } finally {
                }
            }

        });

        return false;
    }

    @Override
    public void setTransportSender(TransportSender transportSender) {
        this.transportSender = transportSender;
    }

    @Override
    public String getId() {
        return "RequestResponseCreationProcessor";
    }

    private class EngineCallBack implements CarbonCallback {

        String requestValue = "test";
        CarbonCallback requestCallBack;

        EngineCallBack(String requestValue, CarbonCallback carbonCallback) {
            this.requestValue = requestValue;
            requestCallBack = carbonCallback;
        }

        @Override
        public void done(CarbonMessage carbonMessage) {
            int length = carbonMessage.getFullMessageLength();
            List<ByteBuffer> byteBufferList = carbonMessage.getFullMessageBody();

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
                carbonMessage = MessageUtil.cloneCarbonMessageWithOutData(carbonMessage);
                if (carbonMessage.getHeader(Constants.HTTP_TRANSFER_ENCODING) == null) {
                    carbonMessage.setHeader(Constants.HTTP_CONTENT_LENGTH, String.valueOf(array.length));
                }

                carbonMessage.addMessageBody(byteBuff);
                carbonMessage.setEndOfMsgAdded(true);
                requestCallBack.done(carbonMessage);
            }

        }
    }
}
