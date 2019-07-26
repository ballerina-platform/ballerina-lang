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

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.jms.JmsConstants;
import org.ballerinalang.net.jms.JmsUtils;
import org.ballerinalang.net.jms.utils.BallerinaAdapter;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;

/**
 * {@code Receive} is the receive action implementation of the JMS Connector.
 */
public class ReceiveActionHandler {

    private ReceiveActionHandler() {
    }

    public static Object handle(Strand strand, ObjectValue connectorBObject, long timeInMilliSeconds) {

        MessageConsumer messageConsumer =
                (MessageConsumer) connectorBObject.getNativeData(JmsConstants.JMS_CONSUMER_OBJECT);
        SessionConnector sessionConnector =
                (SessionConnector) connectorBObject.getNativeData(JmsConstants.SESSION_CONNECTOR_OBJECT);

        try {
            sessionConnector.handleTransactionBlock(strand);
            Message message = messageConsumer.receive(timeInMilliSeconds);
            if (message != null) {
                return JmsUtils.createAndPopulateMessageObject(message, (ObjectValue) connectorBObject
                        .getNativeData(JmsConstants.SESSION_OBJECT));
            }
        } catch (JMSException e) {
            return BallerinaAdapter.getError("Message receiving failed.", e);
        }
        return null;
    }
}





































