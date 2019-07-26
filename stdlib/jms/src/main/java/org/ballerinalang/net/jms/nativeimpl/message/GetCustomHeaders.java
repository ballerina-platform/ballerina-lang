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

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

/**
 * Returns the CustomHeaders of the Message.
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = JmsConstants.BALLERINAX, packageName = JmsConstants.JAVA_JMS,
        functionName = "getCustomHeaders",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = JmsConstants.MESSAGE_OBJ_NAME,
                             structPackage = JmsConstants.PROTOCOL_PACKAGE_JMS)
)
public class GetCustomHeaders {

    public static Object getCustomHeaders(Strand strand, ObjectValue msgObj) {
        Message message = JmsUtils.getJMSMessage(msgObj);
        try {
            MapValue<String, Object> headersRecord = BallerinaValues.createRecordValue(
                    JmsConstants.PROTOCOL_INTERNAL_PACKAGE_JMS, JmsConstants.CUSTOM_HEADERS);

            Destination destination = message.getJMSReplyTo();
            if (destination != null) {
                headersRecord.put(JmsConstants.REPLY_TO_FIELD, JmsUtils.populateAndGetDestinationObj(destination));
            }
            String correlationId = message.getJMSCorrelationID();
            if (correlationId != null) {
                headersRecord.put(JmsConstants.CORRELATION_ID_FIELD, correlationId);
            }
            String type = message.getJMSType();
            if (type != null) {
                headersRecord.put(JmsConstants.TYPE_FIELD, type);
            }
            return headersRecord;
        } catch (JMSException ex) {
            return BallerinaAdapter.getError("Error getting the developer assigned headers", ex);
        }
    }

    private GetCustomHeaders() {
    }
}
