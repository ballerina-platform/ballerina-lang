/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.services;

import org.ballerinalang.runtime.ServerConnectorMessageHandler;
import org.ballerinalang.runtime.threadpool.RequestWorkerThread;
import org.ballerinalang.runtime.threadpool.ThreadPoolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.ClientConnector;
import org.wso2.carbon.messaging.TransportSender;

/**
 * {@code MessageProcessor} is the interface between the inbound transport and the Ballerina engine.
 */
public class MessageProcessor implements CarbonMessageProcessor {

    private static final Logger log = LoggerFactory.getLogger(MessageProcessor.class);

    public boolean receive(CarbonMessage cMsg, CarbonCallback carbonCallback) throws Exception {

        if (!org.wso2.carbon.messaging.Constants.DIRECTION_RESPONSE.
                equals(cMsg.getProperty(org.wso2.carbon.messaging.Constants.DIRECTION))) {
            // For Request
            if (log.isDebugEnabled()) {
                log.debug("ballerina received a request message");
            }
            ThreadPoolFactory.getInstance().getExecutor().execute(new RequestWorkerThread(cMsg, carbonCallback));
        } else {
            ServerConnectorMessageHandler.handleOutbound(cMsg, carbonCallback);
        }
        return true;
    }

    public void setTransportSender(TransportSender transportSender) {
    }

    @Override
    public void setClientConnector(ClientConnector clientConnector) {

    }

    public String getId() {
        return "ballerina-message-processor";
    }
}
