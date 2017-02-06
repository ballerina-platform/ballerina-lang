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

package org.wso2.carbon.transport.http.netty.encoding;

import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.messaging.MessageProcessorException;
import org.wso2.carbon.messaging.TransportSender;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.util.TestUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContentReadingProcessor implements CarbonMessageProcessor {

    private TransportSender transportSender;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private static final Logger logger = LoggerFactory.getLogger(ContentReadingProcessor.class);

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {
        executorService.execute(() -> {
            try {
                if (carbonMessage.getProperty(org.wso2.carbon.messaging.Constants.DIRECTION) != null && carbonMessage
                        .getProperty(org.wso2.carbon.messaging.Constants.DIRECTION)
                        .equals(org.wso2.carbon.messaging.Constants.DIRECTION_RESPONSE)) {

                    InputStream inputStream = carbonMessage.getInputStream();
                    String response = new String(ByteStreams.toByteArray(inputStream), Charset.defaultCharset());
                    String alteredContent = "Altered " + response + " content";

                    DefaultCarbonMessage defaultCarbonMessage = new DefaultCarbonMessage();

                    defaultCarbonMessage.setStringMessageBody(alteredContent);
                    carbonCallback.done(defaultCarbonMessage);

                } else {
                    carbonMessage.setProperty(Constants.HOST, TestUtil.TEST_HOST);
                    carbonMessage.setProperty(Constants.PORT, TestUtil.TEST_SERVER_PORT);
                    transportSender.send(carbonMessage, carbonCallback);
                }
            } catch (MessageProcessorException | IOException e) {
                logger.error("MessageProcessor is not supported ", e);
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
        return "content-reading";
    }
}
