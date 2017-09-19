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
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
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

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * Get Header from the JMS Message.
 */
@BallerinaFunction(
        packageName = "ballerina.net.jms.jmsmessage",
        functionName = "getHeader",
        args = {@Argument(name = "msg", type = TypeEnum.STRUCT, structType = "JMSMessage",
                          structPackage = "ballerina.net.jms"),
                @Argument(name = "HeaderName", type = TypeEnum.STRING)},
        returnType = {@ReturnType(type = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Gets a transport property from the message") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "message",
        value = "The JMS message") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "headerName",
        value = "The header name") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "string",
        value = "The header value") })
public class GetHeader extends AbstractNativeFunction {

    private static final Logger log = LoggerFactory.getLogger(GetHeader.class);

    public BValue[] execute(Context context) {

        BStruct messageStruct  = ((BStruct) this.getRefArgument(context, 0));
        String headerName = this.getStringArgument(context, 0);

        BValue[] headerValue = null;

        //TODO: validations and null checks
//        CarbonMessage carbonMessage = (CarbonMessage) messageStruct.getNativeData(Constants.TRANSPORT_MESSAGE);
//        Message jmsMessage = (Message) carbonMessage.getProperty(Constants.JMS_API_MESSAGE);

        Message jmsMessage = JMSUtils.getJMSMessage(messageStruct);

        try {
            switch (headerName.toUpperCase()) {
            case Constants.HEADER_MESSAGE_ID:
                headerValue = this.getBValues(new BString(jmsMessage.getJMSMessageID()));
                break;
            case Constants.HEADER_PRIORITY:
                headerValue = this.getBValues(new BInteger(jmsMessage.getJMSPriority()));
                break;
            case Constants.HEADER_EXPIRATION:
                headerValue = this.getBValues(new BInteger(jmsMessage.getJMSExpiration()));
                break;
            case Constants.HEADER_REDELIVERED:
                headerValue = this.getBValues(new BBoolean(jmsMessage.getJMSRedelivered()));
                break;
            case Constants.HEADER_CORRELATION_ID:
                headerValue = this.getBValues(new BString(jmsMessage.getJMSCorrelationID()));
                break;
            case Constants.HEADER_DESTINATION:
                headerValue = (jmsMessage.getJMSDestination() != null) ?
                        this.getBValues(new BString(jmsMessage.getJMSDestination().toString())) :
                        null;
                break;
            case Constants.HEADER_TIMESTAMP:
                headerValue = this.getBValues(new BInteger(jmsMessage.getJMSTimestamp()));
                break;
            case Constants.HEADER_REPLY_TO:
                headerValue = (jmsMessage.getJMSReplyTo() != null) ?
                        this.getBValues(new BString(jmsMessage.getJMSReplyTo().toString())) :
                        null;
                break;
            case Constants.HEADER_MESSAGE_TYPE:
                headerValue = this.getBValues(new BString(jmsMessage.getJMSType()));
                break;
            case Constants.HEADER_DELIVERY_MODE:
                headerValue = this.getBValues(new BInteger(jmsMessage.getJMSDeliveryMode()));
                break;
            default:
                  log.warn("Invalid JMS Header " + headerName);
            }
        } catch (JMSException e) {
            log.error("Unable to retrieve " + headerName + " from the JMS Message. " + e.getLocalizedMessage());
        }

        if (log.isDebugEnabled()) {
            log.debug("Get " + headerName + " from message");
        }

        return headerValue;
    }
}
