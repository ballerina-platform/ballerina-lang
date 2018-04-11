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
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.net.jms.Constants;
import org.ballerinalang.net.jms.utils.BallerinaAdapter;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * Util class to acknowledge a received message.
 */
public class MessageAcknowledgementHandler {

    private MessageAcknowledgementHandler() {
    }

    public static void handle(Context context) {
        Struct consumerConnectorObject = BallerinaAdapter.getReceiverObject(context);
        SessionConnector sessionConnector = BallerinaAdapter.getNativeObject(consumerConnectorObject,
                                                                             Constants.SESSION_CONNECTOR_OBJECT,
                                                                             SessionConnector.class,
                                                                             context);
        BStruct messageBObject = (BStruct) context.getRefArgument(1);
        Message message = BallerinaAdapter.getNativeObject(messageBObject,
                                                                Constants.JMS_MESSAGE_OBJECT,
                                                                Message.class,
                                                                context);
        try {
            sessionConnector.handleTransactionBlock(context);
            message.acknowledge();
            context.setReturnValues();
        } catch (JMSException e) {
            BallerinaAdapter.returnError("Message acknowledgement failed.", context, e);
        }
    }
}
