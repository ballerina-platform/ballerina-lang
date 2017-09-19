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

package org.ballerinalang.net.jms.nativeimpl.message;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.jms.Constants;
import org.ballerinalang.net.jms.JMSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessage;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

/**
 * Set content of the JMS Message.
 */
@BallerinaFunction(
        packageName = "ballerina.net.jms.jmsmessage",
        functionName = "setContent",
        args = {@Argument(name = "msg", type = TypeEnum.STRUCT, structType = "JMSMessage",
                          structPackage = "ballerina.net.jms"),
                @Argument(name = "content", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Set content for the message") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "message",
        value = "The JMS message") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "content",
        value = "The message content") })
public class SetContent extends AbstractNativeFunction {

    private static final Logger log = LoggerFactory.getLogger(SetContent.class);

    public BValue[] execute(Context context) {

        BStruct messageStruct  = ((BStruct) this.getRefArgument(context, 0));
        String messageContent = this.getStringArgument(context, 0);

//        CarbonMessage carbonMessage = (CarbonMessage) messageStruct.getNativeData(Constants.TRANSPORT_MESSAGE);
//        Message jmsMessage = (Message) carbonMessage.getProperty(Constants.JMS_API_MESSAGE);

        Message jmsMessage = JMSUtils.getJMSMessage(messageStruct);

        try {
            if (jmsMessage instanceof TextMessage) {
                ((TextMessage) jmsMessage).setText(messageContent);
            } else {
                log.error("Unsupported JMS Message type. ");
            }
        } catch (JMSException e) {
            log.error("Error when setting JMS message content :" + e.getLocalizedMessage());
        }

        if (log.isDebugEnabled()) {
            log.debug("Set content for the JMS message");
        }

        return AbstractNativeFunction.VOID_RETURN;
    }
}
