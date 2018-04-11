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

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.jms.AbstractBlockinAction;
import org.ballerinalang.net.jms.Constants;
import org.ballerinalang.queue.JMSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.jms.Message;

/**
 * Create Text JMS Message.
 */
@BallerinaFunction(orgName = "ballerina", packageName = "queue", functionName = "createBytesMessage", args = {
        @Argument(name = "clientConnector", type = TypeKind.STRUCT) },
                   returnType = {@ReturnType(type = TypeKind.STRUCT, structPackage = "ballerina.queue",
                                             structType = "Message")},
                   isPublic = true)
public class CreateBytesMessage extends AbstractBlockinAction {
    private static final Logger log = LoggerFactory.getLogger(CreateBytesMessage.class);

    @Override
    public void execute(Context context, CallableUnitCallback callableUnitCallback) {
        Struct clientEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);

//        JMSClientConnector jmsClientConnector
//                = (JMSClientConnector) clientEndpoint.getNativeData(Constants.JMS_TRANSPORT_CLIENT_CONNECTOR);

        Message jmsMessage;

//        try {
//            jmsMessage = jmsClientConnector.createMessage(JMSConstants.BYTES_MESSAGE_TYPE);
//        } catch (JMSConnectorException e) {
//            throw new BallerinaException("Failed to create message. " + e.getMessage(), e, context);
//        }

        BStruct bStruct = BLangConnectorSPIUtil
                .createBStruct(context, Constants.PROTOCOL_PACKAGE_JMS, Constants.JMS_MESSAGE_STRUCT_NAME);

        bStruct.addNativeData(Constants.JMS_API_MESSAGE, JMSUtils.getBrokerMessage(bStruct));
        bStruct.addNativeData(Constants.INBOUND_REQUEST, Boolean.FALSE);

        context.setReturnValues(bStruct);
    }
}
