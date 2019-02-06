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

package org.ballerinalang.net.jms.nativeimpl.endpoint.topic.subscriber;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
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

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

/**
 * Create JMS topic subscriber for a topic subscriber endpoint.
 *
 * @since 0.970
 */

@BallerinaFunction(
        orgName = "ballerina",
        packageName = "jms",
        functionName = "createSubscriber",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "TopicSubscriber", structPackage = "ballerina/jms"),
        args = { @Argument(name = "session", type = TypeKind.OBJECT, structType = "Session"),
                @Argument(name = "messageSelector", type = TypeKind.STRING),
                @Argument(name = "destination", type = TypeKind.OBJECT)
        },
        isPublic = true
)
public class CreateSubscriber extends AbstractBlockingAction {

    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        BMap<String, BValue> topicSubscriberBObject = (BMap<String, BValue>) context.getRefArgument(0);

        BMap<String, BValue> sessionBObject = (BMap<String, BValue>) context.getRefArgument(1);
        String messageSelector = context.getStringArgument(0);
        Session session = BallerinaAdapter.getNativeObject(sessionBObject, JmsConstants.JMS_SESSION, Session.class,
                                                           context);
        BValue arg = context.getRefArgument(2);
        String topicPattern = null;
        BMap<String, BValue> destinationBObject = null;
        if (arg instanceof BString) {
            topicPattern = arg.stringValue();
        } else {
            destinationBObject = (BMap<String, BValue>) arg;
        }
        Destination destinationObject = JmsUtils.getDestination(context, destinationBObject);

        if (JmsUtils.isNullOrEmptyAfterTrim(topicPattern) && destinationObject == null) {
            throw new BallerinaException("Topic pattern and destination cannot be null at the same time", context);
        }

        try {
            Destination topic = destinationObject != null ? destinationObject :
                    JmsUtils.getTopic(session, topicPattern);
            MessageConsumer consumer = session.createConsumer(topic, messageSelector);
            BMap<String, BValue> consumerConnectorBObject =
                    (BMap<String, BValue>) topicSubscriberBObject.get(JmsConstants.CONSUMER_ACTIONS);
            consumerConnectorBObject.addNativeData(JmsConstants.JMS_CONSUMER_OBJECT, consumer);
            consumerConnectorBObject.addNativeData(JmsConstants.SESSION_CONNECTOR_OBJECT,
                                                   new SessionConnector(session));
        } catch (JMSException e) {
            BallerinaAdapter.throwBallerinaException("Error while creating Qeueu consumer", context, e);
        }

    }
}
