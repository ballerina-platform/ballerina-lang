/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.queue.nativeImpl;

import io.ballerina.messaging.broker.core.Message;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.jms.AbstractBlockinAction;
import org.ballerinalang.net.jms.Constants;
import org.ballerinalang.queue.BrokerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create Text JMS Message.
 */
@BallerinaFunction(orgName = "ballerina", packageName = "queue",
                   functionName = "createTextMessage",
                   receiver = @Receiver(type = TypeKind.STRUCT,
                                        structType = "QueueEndpoint",
                                        structPackage = "ballerina.queue"),
                   args = {
                           @Argument(name = "content",
                                     type = TypeKind.STRING),
                   },
                   returnType = {
                           @ReturnType(type = TypeKind.STRUCT,
                                       structPackage = "ballerina.queue",
                                       structType = "Message")
                   },
                   isPublic = true)
public class CreateTextMessage extends AbstractBlockinAction {
    private static final Logger log = LoggerFactory.getLogger(CreateTextMessage.class);

    @Override
    public void execute(Context context, CallableUnitCallback callableUnitCallback) {

        log.info("create text message got called");
        Struct clientEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
        String destination = clientEndpoint.getStructField("config").getStringField("destination");
        String content = context.getStringArgument(0);


//        JMSClientConnector jmsClientConnector
//                = (JMSClientConnector) clientEndpoint.getNativeData(Constants.JMS_TRANSPORT_CLIENT_CONNECTOR);

        Message jmsMessage = BrokerUtils.createMessage(destination, content.getBytes());

//        try {
//            jmsMessage = jmsClientConnector.createMessage(JMSConstants.TEXT_MESSAGE_TYPE);
//            ((TextMessage) jmsMessage).setText(content);
//        } catch (JMSConnectorException | JMSException e) {
//            throw new BallerinaException("Failed to create message. " + e.getMessage(), e, context);
//        }

        BStruct bStruct = BLangConnectorSPIUtil
                .createBStruct(context, Constants.PROTOCOL_PACKAGE_QUEUE, Constants.JMS_MESSAGE_STRUCT_NAME);

        bStruct.addNativeData(org.ballerinalang.net.jms.Constants.JMS_API_MESSAGE, jmsMessage);
        bStruct.addNativeData(Constants.INBOUND_REQUEST, Boolean.FALSE);

        context.setReturnValues(bStruct);
    }
}
