/*
 *  Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.extension.output.transport.jms;

import org.apache.log4j.Logger;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.TextCarbonMessage;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;
import org.wso2.carbon.transport.jms.sender.JMSClientConnector;
import org.wso2.carbon.transport.jms.utils.JMSConstants;

import java.util.Map;

public class JMSPublisher implements Runnable {
    private static final Logger log = Logger.getLogger(JMSPublisher.class);

    private Map<String, String> jmsProperties;
    private JMSClientConnector jmsClientConnector;
    private CarbonMessage message;

    public JMSPublisher(String destination, Map<String, String> staticJMSProperties,
                        JMSClientConnector jmsClientConnector, Object payload) {
        this.jmsProperties.putAll(staticJMSProperties);
        this.jmsProperties.put(JMSConstants.DESTINATION_PARAM_NAME, destination);
        this.jmsClientConnector = jmsClientConnector;
        this.message = new TextCarbonMessage(payload.toString());
    }

    @Override
    public void run() {
        try {
            jmsClientConnector.send(message, null, jmsProperties);
        } catch (ClientConnectorException e) {
            log.error("Error sending the JMS message: ", e);
        }
    }
}
