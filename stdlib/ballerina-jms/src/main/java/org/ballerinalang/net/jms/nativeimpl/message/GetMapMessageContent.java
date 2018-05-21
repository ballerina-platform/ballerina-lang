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

package org.ballerinalang.net.jms.nativeimpl.message;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValueType;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.jms.AbstractBlockinAction;
import org.ballerinalang.net.jms.JMSUtils;
import org.ballerinalang.net.jms.utils.BallerinaAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

/**
 * Get text content of the JMS Message.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "jms",
        functionName = "getMapMessageContent",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Message", structPackage = "ballerina.jms"),
        returnType = {@ReturnType(type = TypeKind.MAP)},
        isPublic = true
)
public class GetMapMessageContent extends AbstractBlockinAction {

    private static final Logger log = LoggerFactory.getLogger(GetMapMessageContent.class);

    @Override
    public void execute(Context context, CallableUnitCallback callableUnitCallback) {

        BStruct messageStruct = ((BStruct) context.getRefArgument(0));
        Message jmsMessage = JMSUtils.getJMSMessage(messageStruct);
        BMap<String, BValueType> messageContent = new BMap<>();

        try {
            if (jmsMessage instanceof MapMessage) {
                MapMessage mapMessage = (MapMessage) jmsMessage;
                Enumeration enumeration = mapMessage.getMapNames();
                while (enumeration.hasMoreElements()) {
                    String key = (String) enumeration.nextElement();
                    Object value = mapMessage.getObject(key);
                    if (value instanceof String || value instanceof Character) {
                        messageContent.put(key, new BString(String.valueOf(value)));
                    } else if (value instanceof Boolean) {
                        messageContent.put(key, new BBoolean((Boolean) value));
                    } else if (value instanceof Integer || value instanceof Long || value instanceof Short) {
                        messageContent.put(key, new BInteger((Long) value));
                    } else if (value instanceof Float || value instanceof Double) {
                        messageContent.put(key, new BFloat((Double) value));
                    } else if (value instanceof byte[]) {
                        messageContent.put(key, new BBlob((byte[]) value));
                    } else {
                        log.error("Couldn't set invalid data type to map : " + value.getClass().getSimpleName());
                    }
                }
            } else {
                log.error("JMSMessage is not a Map message. ");
            }
        } catch (JMSException e) {
            BallerinaAdapter.returnError("Error when retrieving JMS message content.", context, e);
        }

        if (log.isDebugEnabled()) {
            log.debug("Get content from the JMS message");
        }

        context.setReturnValues(messageContent);
    }
}
