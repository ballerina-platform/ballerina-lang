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

package org.ballerinalang.net.jms.nativeimpl.endpoint.topic.durable.subscriber;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.jms.AbstractBlockingAction;
import org.ballerinalang.net.jms.JmsConstants;
import org.ballerinalang.net.jms.JmsUtils;
import org.ballerinalang.net.jms.nativeimpl.endpoint.common.SessionConnector;
import org.ballerinalang.net.jms.utils.BallerinaAdapter;
import org.ballerinalang.util.exceptions.BallerinaException;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Topic;

/**
 * Create JMS topic subscriber for a durable topic subscriber endpoint.
 *
 * @since 0.970
 */

@BallerinaFunction(
        orgName = "ballerina",
        packageName = "jms",
        functionName = "createSubscriber",
        receiver = @Receiver(type = TypeKind.OBJECT,
                structType = "DurableTopicSubscriber",
                structPackage = "ballerina/jms"),
        args = {
                @Argument(name = "session", type = TypeKind.OBJECT, structType = "Session"),
                @Argument(name = "messageSelector", type = TypeKind.STRING)
        },
        isPublic = true
)
public class CreateSubscriber extends AbstractBlockingAction {

    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        BMap<String, BValue> topicSubscriberBObject = (BMap<String, BValue>) context.getRefArgument(0);

        BMap<String, BValue> sessionBObject = (BMap<String, BValue>) context.getRefArgument(1);
        Session session = BallerinaAdapter.getNativeObject(sessionBObject, JmsConstants.JMS_SESSION, Session.class,
                                                           context);

        String topicPattern = context.getStringArgument(0);
        String consumerId = context.getStringArgument(1);
        String messageSelector = context.getStringArgument(2);
        if (JmsUtils.isNullOrEmptyAfterTrim(consumerId)) {
            throw new BallerinaException("Please provide a durable subscription ID", context);
        }

        try {
            Topic topic = JmsUtils.getTopic(session, topicPattern);
            MessageConsumer consumer = session.createDurableSubscriber(topic, consumerId, messageSelector, false);
            BMap<String, BValue> consumerConnectorBObject =
                    (BMap<String, BValue>) topicSubscriberBObject.get(JmsConstants.CONSUMER_ACTIONS);
            consumerConnectorBObject.addNativeData(JmsConstants.JMS_CONSUMER_OBJECT, consumer);
            consumerConnectorBObject.addNativeData(JmsConstants.SESSION_CONNECTOR_OBJECT,
                                                   new SessionConnector(session));
        } catch (JMSException e) {
            BallerinaAdapter.throwBallerinaException("Error while creating queue consumer", context, e);
        }

    }

}
