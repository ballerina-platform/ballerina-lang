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

package org.ballerinalang.net.jms.nativeimpl.endpoint.topic.publisher.action;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.jms.AbstractBlockinAction;
import org.ballerinalang.net.jms.Constants;
import org.ballerinalang.net.jms.nativeimpl.endpoint.common.SessionConnector;
import org.ballerinalang.net.jms.utils.BallerinaAdapter;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;

/**
 * {@code Send} is the send action implementation of the topic producer Connector.
 */
@BallerinaFunction(orgName = "ballerina",
                   packageName = "jms",
                   functionName = "send",
                   receiver = @Receiver(type = TypeKind.STRUCT,
                                        structType = "TopicProducerConnector",
                                        structPackage =
                                                "ballerina.jms"),
                   args = {
                           @Argument(name = "message",
                                     type = TypeKind.STRUCT)
                   },
                   isPublic = true
)
public class Send extends AbstractBlockinAction {

    @Override
    public void execute(Context context, CallableUnitCallback callableUnitCallback) {

        Struct topicProducerConnector = BallerinaAdapter.getReceiverObject(context);
        MessageProducer messageProducer = BallerinaAdapter.getNativeObject(topicProducerConnector,
                                                                           Constants.JMS_TOPIC_PRODUCER_OBJECT,
                                                                           MessageProducer.class,
                                                                           context);
        SessionConnector sessionConnector = BallerinaAdapter.getNativeObject(topicProducerConnector,
                                                                             Constants.SESSION_CONNECTOR_OBJECT,
                                                                             SessionConnector.class,
                                                                             context);

        BStruct messageBObject = ((BStruct) context.getRefArgument(1));
        Message message = BallerinaAdapter.getNativeObject(messageBObject,
                                                           Constants.JMS_MESSAGE_OBJECT,
                                                           Message.class,
                                                           context);

        try {
            sessionConnector.handleTransactionBlock(context);
            messageProducer.send(message);
        } catch (JMSException e) {
            BallerinaAdapter.throwBallerinaException("Message sending failed.", context, e);
        }
    }
}
