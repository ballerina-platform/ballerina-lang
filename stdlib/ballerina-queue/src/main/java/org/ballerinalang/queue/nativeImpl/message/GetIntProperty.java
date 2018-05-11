/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.queue.nativeImpl.message;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.jms.AbstractBlockinAction;
import org.ballerinalang.net.jms.JMSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * Get Integer Property from the JMS Message.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "queue",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Message",
                             structPackage = "ballerina.queue"),
        functionName = "getIntProperty",
        args = {@Argument(name = "propertyName", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.INT)},
        isPublic = true
)
public class GetIntProperty extends AbstractBlockinAction {

    private static final Logger log = LoggerFactory.getLogger(GetIntProperty.class);

    @Override
    public void execute(Context context, CallableUnitCallback callableUnitCallback) {

        BStruct messageStruct  = ((BStruct) context.getRefArgument(0));
        String propertyName = context.getStringArgument(0);

        Message jmsMessage = JMSUtils.getJMSMessage(messageStruct);

        int propertyValue = 0;
        try {
            propertyValue = jmsMessage.getIntProperty(propertyName);
        } catch (JMSException e) {
            log.error("Error when retrieving the integer property :" + e.getLocalizedMessage());
        }

        if (log.isDebugEnabled()) {
            log.debug("Get integer property" + propertyName + " from message with value: " + propertyValue);
        }

        context.setReturnValues(new BInteger(propertyValue));
    }
}
