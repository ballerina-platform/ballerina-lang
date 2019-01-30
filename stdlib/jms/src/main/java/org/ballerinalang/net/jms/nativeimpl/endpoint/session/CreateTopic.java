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

package org.ballerinalang.net.jms.nativeimpl.endpoint.session;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.jms.AbstractBlockingAction;
import org.ballerinalang.net.jms.JmsConstants;
import org.ballerinalang.net.jms.utils.BallerinaAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Topic;

/**
 * Create Text JMS Message.
 */
@BallerinaFunction(orgName = "ballerina", packageName = "jms",
                   functionName = "createTopic",
                   receiver = @Receiver(type = TypeKind.OBJECT, structType = "Session",
                                        structPackage = "ballerina/jms"),
                   args = { @Argument(name = "name", type = TypeKind.STRING) },
                   returnType = {
                           @ReturnType(type = TypeKind.OBJECT, structPackage = "ballerina/jms",
                                        structType = "Destination")
                   },
                   isPublic = true)
public class CreateTopic extends AbstractBlockingAction {

    public static final Logger LOGGER = LoggerFactory.getLogger(CreateTopic.class);

    @Override
    public void execute(Context context, CallableUnitCallback callableUnitCallback) {

        Topic jmsDestination;
        Struct sessionBObject = BallerinaAdapter.getReceiverObject(context);
        Session session = BallerinaAdapter.getNativeObject(sessionBObject, JmsConstants.JMS_SESSION, Session.class,
                                                           context);
        String topicName = context.getStringArgument(0);
        BMap<String, BValue> bStruct = BLangConnectorSPIUtil.createBStruct(context, JmsConstants.BALLERINA_PACKAGE_JMS,
                                                                           JmsConstants.JMS_DESTINATION_STRUCT_NAME);
        try {
            jmsDestination = session.createTopic(topicName);
            bStruct.addNativeData(JmsConstants.JMS_DESTINATION_OBJECT, jmsDestination);
            bStruct.put(JmsConstants.DESTINATION_NAME, new BString(jmsDestination.getTopicName()));
            bStruct.put(JmsConstants.DESTINATION_TYPE, new BString("topic"));
        } catch (JMSException e) {
            BallerinaAdapter.returnError("Failed to create topic destination.", context, e);
        }
        context.setReturnValues(bStruct);
    }
}
