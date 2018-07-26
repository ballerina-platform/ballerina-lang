/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.ballerinalang.net.jms.nativeimpl.endpoint.common;

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.jms.Constants;
import org.ballerinalang.net.jms.utils.BallerinaAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

/**
 * {@code Receive} is the receive action implementation of the JMS Connector.
 */
public class ReceiveFromActionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiveFromActionHandler.class);

    private ReceiveFromActionHandler() {
    }

    public static void handle(Context context) {

        Struct connectorBObject = BallerinaAdapter.getReceiverObject(context);
        SessionConnector sessionConnector = BallerinaAdapter.getNativeObject(connectorBObject,
                                                                             Constants.SESSION_CONNECTOR_OBJECT,
                                                                             SessionConnector.class,
                                                                             context);
        BMap<String, BValue> destinationBObject = ((BMap<String, BValue>) context.getRefArgument(1));
        Destination destination = BallerinaAdapter.getNativeObject(destinationBObject,
                                                        Constants.JMS_DESTINATION_OBJECT,
                                                        Destination.class,
                                                        context);
        
                                                        
//        long timeInMilliSeconds = context.getIntArgument(2);
        long timeInMilliSeconds = 5000;
        
        try {
            Session session = sessionConnector.getSession();
            sessionConnector.handleTransactionBlock(context);
            MessageConsumer consumer = session.createConsumer(destination);
            Message message = consumer.receive(timeInMilliSeconds);
            if (Objects.nonNull(message)) {
                BMap<String, BValue> messageBObject = BLangConnectorSPIUtil.createBStruct(context,
                                                                             Constants.BALLERINA_PACKAGE_JMS,
                                                                             Constants.JMS_MESSAGE_STRUCT_NAME);
                messageBObject.addNativeData(Constants.JMS_MESSAGE_OBJECT, message);
                context.setReturnValues(messageBObject);
                consumer.close();
            } else {
                context.setReturnValues();
            }
        } catch (JMSException e) {
            BallerinaAdapter.returnError("Message receiving failed.", context, e);
        }
    }
}





































