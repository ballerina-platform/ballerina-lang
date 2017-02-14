/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.transport.http.netty.http2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.ClientConnector;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.messaging.TransportSender;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A Message Processor class to be used for test http2 scenarios
 */
public class HTTP2MessageProcessor implements CarbonMessageProcessor {
    private static final Logger logger = LoggerFactory.getLogger(HTTP2MessageProcessor.class);
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private ClientConnector clientConnector;
    public static final String TEST_VALUE = "Test Message";

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {

        DefaultCarbonMessage defaultCarbonMessage = new DefaultCarbonMessage();
        carbonMessage.getProperties().forEach(defaultCarbonMessage::setProperty);
        defaultCarbonMessage.setHeader("content-type", "text/plain");
        defaultCarbonMessage.setStringMessageBody(TEST_VALUE);
        defaultCarbonMessage.setEndOfMsgAdded(true);
        int length = TEST_VALUE.getBytes().length;
        defaultCarbonMessage.setHeader("content-length", String.valueOf(length));
        carbonCallback.done(defaultCarbonMessage);
        return true;
    }

    @Override
    public void setTransportSender(TransportSender transportSender) {
    }

    @Override
    public void setClientConnector(ClientConnector clientConnector) {
        this.clientConnector = clientConnector;
    }

    @Override
    public String getId() {
        return "http2";
    }
}
