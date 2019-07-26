/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.jms.nativeimpl.message;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.jms.JmsConstants;
import org.ballerinalang.net.jms.utils.BallerinaAdapter;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * Create Message object.
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = JmsConstants.BALLERINAX, packageName = JmsConstants.JAVA_JMS,
        functionName = "createMessage",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = JmsConstants.MESSAGE_OBJ_NAME,
                             structPackage = JmsConstants.PROTOCOL_PACKAGE_JMS)
)
public class CreateMessage {

    public static Object createMessage(Strand strand, ObjectValue msgObj) {
        Session session =
                (Session) msgObj.getObjectValue(JmsConstants.SESSION_FIELD_NAME).getNativeData(
                        JmsConstants.JMS_SESSION);
        Message message;
        String msgType = msgObj.getStringValue(JmsConstants.MESSAGE_TYPE_FIELD_NAME);
        try {
            switch (msgType) {
                case JmsConstants.MESSAGE:
                    message = session.createMessage();
                    break;
                case JmsConstants.TEXT_MESSAGE:
                    message = session.createTextMessage();
                    break;
                case JmsConstants.BYTES_MESSAGE:
                    message = session.createBytesMessage();
                    break;
                case JmsConstants.STREAM_MESSAGE:
                    message = session.createStreamMessage();
                    break;
                case JmsConstants.MAP_MESSAGE:
                    message = session.createMapMessage();
                    break;
                default:
                    return BallerinaAdapter.getError("Invalid message type");
            }
        } catch (JMSException e) {
            return BallerinaAdapter.getError("Error while creating " + msgType, e);
        }
        msgObj.addNativeData(JmsConstants.JMS_MESSAGE_OBJECT, message);
        return null;
    }
}
