/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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

package org.ballerinalang.services.dispatchers.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.ServerConnectorErrorHandler;

/**
 * Error handler for jms listener.
 */
public class JMSServerConnectorErrorHandler implements ServerConnectorErrorHandler {
    private Logger logger = LoggerFactory.getLogger(JMSServerConnectorErrorHandler.class);
    /**
     * {@inheritDoc}
     */
    @Override
    public void handleError(Exception e, CarbonMessage carbonMessage, CarbonCallback carbonCallback)
            throws Exception {
        if (null != carbonCallback) {
            carbonMessage
                    .setProperty(Constants.JMS_MESSAGE_DELIVERY_STATUS, Constants.JMS_MESSAGE_DELIVERY_ERROR);
            carbonCallback.done(carbonMessage);
        } else {
            /*
             * This code-block will be executed in auto-acknowledgement mode and dups-ok-acknowledgement mode. As in
             * those acknowledgement modes, JMS provider will deliver the message and will forgot it. Even if there
             * is a problem in the message delivery, the message will be lost. So in that case, we need to indicate
             * that error to the user.
             */
            logger.error("Error while trying to deliver the jms message. ", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProtocol() {
        return Constants.PROTOCOL_JMS;
    }
}
