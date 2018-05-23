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
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValueType;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.jms.AbstractBlockinAction;
import org.ballerinalang.net.jms.Constants;
import org.ballerinalang.net.jms.utils.BallerinaAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Session;

/**
 * Create JMS map message.
 */
@BallerinaFunction(orgName = "ballerina", packageName = "jms",
        functionName = "createMapMessage",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Session",
                structPackage = "ballerina.jms"),
        args = {@Argument(name = "content", type = TypeKind.MAP)},
        returnType = {
                @ReturnType(type = TypeKind.STRUCT, structPackage = "ballerina.jms", structType = "Message")
        },
        isPublic = true)
public class CreateMapMessage extends AbstractBlockinAction {

    public static final Logger LOGGER = LoggerFactory.getLogger(CreateMapMessage.class);

    @Override
    public void execute(Context context, CallableUnitCallback callableUnitCallback) {

        Struct sessionBObject = BallerinaAdapter.getReceiverObject(context);

        Session session = BallerinaAdapter.getNativeObject(sessionBObject, Constants.JMS_SESSION, Session.class,
                                                           context);
        BMap content = (BMap) context.getRefArgument(1);

        MapMessage jmsMessage;

        BStruct bStruct = BLangConnectorSPIUtil.createBStruct(context, Constants.BALLERINA_PACKAGE_JMS,
                Constants.JMS_MESSAGE_STRUCT_NAME);
        try {
            jmsMessage = session.createMapMessage();
            Map<String, BValueType> contentMap = content.getMap();

            contentMap.forEach((key, value) -> {
                try {
                    if (value instanceof BString) {
                        jmsMessage.setString(key, value.stringValue());
                    } else if (value instanceof BBoolean) {
                        jmsMessage.setBoolean(key, value.booleanValue());
                    } else if (value instanceof BBlob) {
                        jmsMessage.setBytes(key, value.blobValue());
                    } else if (value instanceof BInteger) {
                        jmsMessage.setLong(key, value.intValue());
                    } else if (value instanceof BFloat) {
                        jmsMessage.setDouble(key, value.floatValue());
                    } else {
                        LOGGER.error("Couldn't set invalid data type to MapMessage : " + value.getType().getName());
                    }
                } catch (JMSException e) {
                    BallerinaAdapter.returnError("Failed to create map message", context, e);
                }
            });
            bStruct.addNativeData(Constants.JMS_MESSAGE_OBJECT, jmsMessage);
        } catch (JMSException e) {
            BallerinaAdapter.returnError("Failed to create message.", context, e);
        }
        context.setReturnValues(bStruct);
    }
}
