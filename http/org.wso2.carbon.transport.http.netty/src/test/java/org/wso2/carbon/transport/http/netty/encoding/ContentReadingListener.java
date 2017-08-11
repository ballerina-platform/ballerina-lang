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

package org.wso2.carbon.transport.http.netty.encoding;

import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.MessageUtil;
import org.wso2.carbon.transport.http.netty.contract.HTTPConnectorListener;
import org.wso2.carbon.transport.http.netty.contract.ServerConnectorException;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.carbon.transport.http.netty.message.HTTPMessageUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * HTTP connector Listener for Content reading.
 */
public class ContentReadingListener implements HTTPConnectorListener {

    private static final Logger logger = LoggerFactory.getLogger(ContentReadingListener.class);

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void onMessage(HTTPCarbonMessage httpMessage) {
        executor.execute(() -> {
            try {
                InputStream inputStream = httpMessage.getInputStream();
                String response = new String(ByteStreams.toByteArray(inputStream), Charset.defaultCharset());
                String alteredContent = "Altered " + response + " content";

                CarbonMessage newMsg = MessageUtil.cloneCarbonMessageWithOutData(httpMessage);
                newMsg.addMessageBody(ByteBuffer.wrap(alteredContent.getBytes(Charset.defaultCharset())));
                newMsg.setEndOfMsgAdded(true);

                httpMessage.respond(HTTPMessageUtil.convertCarbonMessage(newMsg));
            } catch (IOException | ServerConnectorException e) {
                logger.error("Error occurred during message processing ", e);
            }

        });
    }

    @Override
    public void onError(Throwable throwable) {

    }
}
