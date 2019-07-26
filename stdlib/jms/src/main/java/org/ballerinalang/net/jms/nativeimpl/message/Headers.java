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

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.jms.JmsConstants;
import org.ballerinalang.net.jms.JmsUtils;
import org.ballerinalang.net.jms.utils.BallerinaAdapter;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;

/**
 * Return the Headers of the Message.
 *
 * @since 0.970
 */
@BallerinaFunction(
        orgName = JmsConstants.BALLERINAX, packageName = JmsConstants.JAVA_JMS,
        functionName = "getHeaders",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = JmsConstants.MESSAGE_OBJ_NAME,
                             structPackage = JmsConstants.PROTOCOL_PACKAGE_JMS)
)
public class Headers {

    private static final String DELIVERY_MODE = "deliveryMode";

    public static Object getHeaders(Strand strand, ObjectValue msgObj) {
        Message message = JmsUtils.getJMSMessage(msgObj);
        try {
            MapValue<String, Object> headersRecord = BallerinaValues.createRecordValue(
                    JmsConstants.PROTOCOL_INTERNAL_PACKAGE_JMS, JmsConstants.HEADERS);
            headersRecord.put("destination", JmsUtils.populateAndGetDestinationObj(message.getJMSDestination()));
            int deliveryMode = message.getJMSDeliveryMode();
            if (deliveryMode == DeliveryMode.PERSISTENT) {
                headersRecord.put(DELIVERY_MODE, "PERSISTENT");
            } else {
                headersRecord.put(DELIVERY_MODE, "NON_PERSISTENT");
            }
            headersRecord.put("messageId", message.getJMSMessageID());
            headersRecord.put("timestamp", message.getJMSTimestamp());
            headersRecord.put("expiration", message.getJMSExpiration());
            headersRecord.put("redelivered", message.getJMSRedelivered());
            headersRecord.put("priority", message.getJMSPriority());
            headersRecord.freeze();
            return headersRecord;
        } catch (JMSException ex) {
            return BallerinaAdapter.getError("Error getting the automatically assigned headers", ex);
        }
    }

    private Headers() {
    }
}
