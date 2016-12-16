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

package org.wso2.ballerina.core.runtime.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.runtime.Constants;
import org.wso2.ballerina.core.runtime.core.threading.threadpool.RequestWorkerThread;
import org.wso2.ballerina.core.runtime.core.threading.threadpool.ThreadPoolFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.TransportSender;

/**
 * {@code MessageProcessor} is the interface between the inbound transport and the Ballerina engine
 */
public class MessageProcessor implements CarbonMessageProcessor {

    private static final Logger log = LoggerFactory.getLogger(MessageProcessor.class);

    public boolean receive(CarbonMessage cMsg, CarbonCallback carbonCallback) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("ballerina received a message");
        }

        String protocol = (String) cMsg.getProperty(org.wso2.carbon.messaging.Constants.PROTOCOL);
        if (protocol == null) {
            log.error("Protocol not defined in the incoming request");
            //TODO: Handler error
            return false;
        }
        Context balContext = new Context(cMsg);
        balContext.setProperty(Constants.PROTOCOL, protocol);

        RequestWorkerThread workerThread =
                new RequestWorkerThread(balContext, new DefaultBalCallback(carbonCallback));

        ThreadPoolFactory.getInstance().getExecutor().execute(workerThread);

        return false;
    }

    public void setTransportSender(TransportSender transportSender) {

    }

    public String getId() {
        return "ballerina-message-processor";
    }
}
