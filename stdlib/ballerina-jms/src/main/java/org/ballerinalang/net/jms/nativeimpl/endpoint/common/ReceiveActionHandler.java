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
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.net.jms.Constants;
import org.ballerinalang.net.jms.JMSUtils;
import org.ballerinalang.net.jms.utils.BallerinaAdapter;

import java.util.Objects;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;

/**
 * {@code Receive} is the receive action implementation of the JMS Connector.
 */
public class ReceiveActionHandler {

    private ReceiveActionHandler() {
    }

    public static void handle(Context context) {

        Struct connectorBObject = BallerinaAdapter.getReceiverStruct(context);
        MessageConsumer messageConsumer = BallerinaAdapter.getNativeObject(connectorBObject,
                                                                           Constants.JMS_CONSUMER_OBJECT,
                                                                           MessageConsumer.class,
                                                                           context
                                                                          );

        long timeInMilliSeconds = context.getIntArgument(0);

        try {
            Message message = messageConsumer.receive(timeInMilliSeconds);
            if (Objects.nonNull(message)) {
                BStruct messageBObject = BLangConnectorSPIUtil.createBStruct(context,
                                                                             Constants.BALLERINA_PACKAGE_JMS,
                                                                             Constants.JMS_MESSAGE_STRUCT_NAME);
                messageBObject.addNativeData(Constants.JMS_MESSAGE_OBJECT, message);
                context.setReturnValues(messageBObject);
            } else {
                context.setReturnValues();
            }
        } catch (JMSException e) {
            JMSUtils.throwBallerinaException("Message receiving failed.", context, e);
        }
    }
}





































