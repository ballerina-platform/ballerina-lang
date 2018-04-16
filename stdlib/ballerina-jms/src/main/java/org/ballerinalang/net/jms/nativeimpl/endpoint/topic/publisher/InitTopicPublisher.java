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

package org.ballerinalang.net.jms.nativeimpl.endpoint.topic.publisher;

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
import org.ballerinalang.net.jms.JMSUtils;
import org.ballerinalang.net.jms.nativeimpl.endpoint.common.SessionConnector;
import org.ballerinalang.net.jms.utils.BallerinaAdapter;
import org.ballerinalang.util.exceptions.BallerinaException;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;

/**
 * Initialize the topic producer.
 *
 * @since 0.966
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "jms",
        functionName = "initTopicPublisher",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "TopicPublisher", structPackage = "ballerina.jms"),
        args = { @Argument(name = "session", type = TypeKind.STRUCT, structType = "Session") },
        isPublic = true
)
public class InitTopicPublisher extends AbstractBlockinAction {

    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        Struct topicProducerBObject = BallerinaAdapter.getReceiverObject(context);
        Struct topicProducerConfig = topicProducerBObject.getStructField(Constants.TOPIC_PUBLISHER_FIELD_CONFIG);
        String topicPattern = topicProducerConfig.getStringField(Constants.TOPIC_PUBLISHER_FIELD_TOPIC_PATTERN);

        if (JMSUtils.isNullOrEmptyAfterTrim(topicPattern)) {
            throw new BallerinaException("Topic pattern cannot be null", context);
        }

        BStruct sessionBObject = (BStruct) context.getRefArgument(1);
        Session session = BallerinaAdapter.getNativeObject(sessionBObject,
                                                           Constants.JMS_SESSION,
                                                           Session.class,
                                                           context);
        try {

            Topic topic = null;
            if (!JMSUtils.isNullOrEmptyAfterTrim(topicPattern)) {
                topic = JMSUtils.getTopic(session, topicPattern);
            }
            MessageProducer producer = session.createProducer(topic);
            Struct topicProducerConnectorBObject
                    = topicProducerBObject.getStructField(Constants.TOPIC_PUBLISHER_FIELD_CONNECTOR);
            topicProducerConnectorBObject.addNativeData(Constants.JMS_PRODUCER_OBJECT, producer);
            topicProducerConnectorBObject.addNativeData(Constants.SESSION_CONNECTOR_OBJECT,
                                                        new SessionConnector(session));
        } catch (JMSException e) {
            BallerinaAdapter.throwBallerinaException("Error creating topic producer", context, e);
        }

    }
}
