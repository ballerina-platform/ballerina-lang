/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.net.jms.nativeimpl.message;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.net.jms.Constants;
import org.ballerinalang.net.jms.JMSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessage;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * Native function to set given header to carbon message.
 * ballerina.model.messages:setHeader
 */

@BallerinaFunction(
        packageName = "ballerina.net.jms.jmsmessage",
        functionName = "setHeader",
        args = {@Argument(name = "jmsmessage", type = TypeEnum.STRUCT, structType = "JMSMessage",
                          structPackage = "ballerina.net.jms"),
                @Argument(name = "key", type = TypeEnum.STRING),
                @Argument(name = "value", type = TypeEnum.ANY)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Sets the value of a transport header") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "jmsmessage",
        value = "The current message") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "key",
        value = "The header name") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "value",
        value = "The header value") })
public class SetHeader extends AbstractNativeFunction {

    private static final Logger log = LoggerFactory.getLogger(SetHeader.class);

    @Override
    public BValue[] execute(Context context) {

        BStruct messageStruct  = ((BStruct) this.getRefArgument(context, 0));
        String headerName = this.getStringArgument(context, 0);

        //TODO: validations and null checks
//        CarbonMessage carbonMessage = (CarbonMessage) messageStruct.getNativeData(Constants.TRANSPORT_MESSAGE);
//        Message jmsMessage = (Message) carbonMessage.getProperty(Constants.JMS_API_MESSAGE);

        Message jmsMessage = JMSUtils.getJMSMessage(messageStruct);

        try {
            switch (headerName) {
            case Constants.HEADER_MESSAGE_ID:
                jmsMessage.setJMSMessageID(this.getStringArgument(context, 1));
            case Constants.HEADER_PRIORITY:
                jmsMessage.setJMSPriority((int)this.getIntArgument(context, 0));
            case Constants.HEADER_EXPIRATION:
                jmsMessage.setJMSExpiration(this.getIntArgument(context,0));
            case Constants.HEADER_REDELIVERED:
                jmsMessage.setJMSRedelivered(this.getBooleanArgument(context, 0));
            case Constants.HEADER_CORRELATION_ID:
                jmsMessage.setJMSCorrelationID(this.getStringArgument(context, 1));
            case Constants.HEADER_DESTINATION:
//                jmsMessage.setJMSDestination(new );
//                headerValue = (jmsMessage.getJMSDestination() != null) ?
//                        this.getBValues(new BString(jmsMessage.getJMSDestination().toString())) :
//                        null;
            case Constants.HEADER_TIMESTAMP:
                jmsMessage.setJMSTimestamp((long)this.getFloatArgument(context, 0));
            case Constants.HEADER_REPLY_TO:
//                headerValue = (jmsMessage.getJMSReplyTo() != null) ?
//                        this.getBValues(new BString(jmsMessage.getJMSReplyTo().toString())) :
//                        null;
            case Constants.HEADER_MESSAGE_TYPE:
//                headerValue = this.getBValues(new BString(jmsMessage.getJMSType()));
            case Constants.HEADER_DELIVERY_MODE:
                jmsMessage.setJMSDeliveryMode((int)this.getIntArgument(context, 0));
            }
        } catch (JMSException e) {
            log.error("Unable to set " + headerName + " from the JMS Message. " + e.getLocalizedMessage());
        }

        if (log.isDebugEnabled()) {
            log.debug("Add " + headerName + " to message");
        }

        return AbstractNativeFunction.VOID_RETURN;
    }
}