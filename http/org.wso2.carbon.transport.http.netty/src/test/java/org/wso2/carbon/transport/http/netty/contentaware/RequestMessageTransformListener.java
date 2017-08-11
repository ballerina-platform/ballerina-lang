/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.transport.http.netty.contentaware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.contract.HTTPConnectorListener;
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
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public RequestMessageTransformListener(String transformedValue) {
        this.transformedValue = transformedValue;
    }

    @Override
    public void onMessage(HTTPCarbonMessage httpRequest) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
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
