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
import org.wso2.carbon.messaging.MapCarbonMessage;
import org.wso2.carbon.messaging.TextCarbonMessage;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;
import org.wso2.carbon.transport.jms.sender.JMSClientConnector;
import org.wso2.carbon.transport.jms.utils.JMSConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * JMS publisher which creates the message and sends to JMS.
 */
public class JMSPublisher implements Runnable {
    private static final Logger log = Logger.getLogger(JMSPublisher.class);

    // this field has to be set for the TextCarbonMessage for it to work
    private final static String MESSAGE_TYPE_FIELD = "JMS_MESSAGE_TYPE";
    private final static String TEXT_MESSAGE_TYPE = "TextMessage";
    private final static String BYTEARRAY_MESSAGE_TYPE = "ByteMessage";

    private Map<String, String> jmsProperties;
    private JMSClientConnector jmsClientConnector;
    private CarbonMessage message;

    public JMSPublisher(String destination, Map<String, String> staticJMSProperties,
                        JMSClientConnector jmsClientConnector, Object payload) {
        this.jmsProperties = new HashMap<>();
        this.jmsProperties.putAll(staticJMSProperties);
        this.jmsProperties.put(JMSConstants.DESTINATION_PARAM_NAME, destination);
        this.jmsClientConnector = jmsClientConnector;
        this.message = handleCarbonMessage(payload);
    }

    @Override
    public void run() {
        try {
            jmsClientConnector.send(message, null, jmsProperties);
        } catch (ClientConnectorException e) {
            log.error("Error sending JMS message: ", e);
        }
    }

    private CarbonMessage handleCarbonMessage(Object payload) {
        if (payload instanceof String) {
            CarbonMessage textCarbonMessage = new TextCarbonMessage(payload.toString());
            this.jmsProperties.put(MESSAGE_TYPE_FIELD, TEXT_MESSAGE_TYPE);
            return textCarbonMessage;
        } else if (payload instanceof Map) {
            MapCarbonMessage mapCarbonMessage = new MapCarbonMessage();
            ((Map) payload).forEach((key, value) -> {
                mapCarbonMessage.setValue((String) key, (String) value);
            });
            return mapCarbonMessage;
        } else if (payload instanceof Byte[]) {
            TextCarbonMessage byteCarbonMessage = new TextCarbonMessage(payload.toString());
            this.jmsProperties.put(MESSAGE_TYPE_FIELD, BYTEARRAY_MESSAGE_TYPE);
            return byteCarbonMessage;
        } else {
            throw new RuntimeException(
                    "The type of the output payload cannot be cast to String, Map or Byte[] from JMS");
        }
    }
}
