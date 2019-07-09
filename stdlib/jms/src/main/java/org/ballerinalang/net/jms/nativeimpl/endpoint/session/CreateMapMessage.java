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
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.jms.JmsConstants;
import org.ballerinalang.net.jms.utils.BallerinaAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Session;

/**
 * Create JMS map message.
 */
@BallerinaFunction(orgName = JmsConstants.BALLERINAX, packageName = JmsConstants.JMS,
                   functionName = "createMapMessage",
                   receiver = @Receiver(type = TypeKind.OBJECT, structType = JmsConstants.SESSION_OBJ_NAME,
                                        structPackage = JmsConstants.PROTOCOL_PACKAGE_JMS)
)
public class CreateMapMessage extends BlockingNativeCallableUnit {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateMapMessage.class);

    @Override
    public void execute(Context context) {
    }

    public static Object createMapMessage(Strand strand, ObjectValue sessionObj, MapValue<String, Object> content) {

        Session session = (Session) sessionObj.getNativeData(JmsConstants.JMS_SESSION);
        MapMessage jmsMessage;
        ObjectValue msgObj = BallerinaValues.createObjectValue(JmsConstants.PROTOCOL_PACKAGE_JMS,
                                                               JmsConstants.MESSAGE_OBJ_NAME);
        try {
            jmsMessage = session.createMapMessage();
            for (String key : content.getKeys()) {
                try {
                    Object value = content.get(key);
                    if (value instanceof String) {
                        jmsMessage.setString(key, (String) value);
                    } else if (value instanceof Boolean) {
                        jmsMessage.setBoolean(key, ((Boolean) value));
                    } else if (value instanceof ArrayValue) {
                        jmsMessage.setBytes(key, ((ArrayValue) value).getBytes());
                    } else if (value instanceof Long) {
                        jmsMessage.setLong(key, (Long) value);
                    } else if (value instanceof Integer) {
                        jmsMessage.setInt(key, (Integer) value);
                    } else if (value instanceof Byte) {
                        jmsMessage.setByte(key, (Byte) value);
                    } else if (value instanceof Double) {
                        jmsMessage.setDouble(key, (Double) value);
                    } else {
                        LOGGER.error("Couldn't set invalid data type to MapMessage");
                    }
                } catch (JMSException e) {
                    return BallerinaAdapter.getError("Failed to create map message", e);
                }
            }
            msgObj.addNativeData(JmsConstants.JMS_MESSAGE_OBJECT, jmsMessage);
        } catch (JMSException e) {
            return BallerinaAdapter.getError("Failed to create message.", e);
        }
        return msgObj;
    }

}
