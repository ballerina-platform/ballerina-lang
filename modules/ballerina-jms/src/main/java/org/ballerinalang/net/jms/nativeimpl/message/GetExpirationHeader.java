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
import org.ballerinalang.net.jms.JMSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * Get Expiration header from the JMS Message.
 */
@BallerinaFunction(
        packageName = "ballerina.net.jms.jmsmessage",
        functionName = "getExpiration",
        args = {@Argument(name = "msg", type = TypeEnum.STRUCT, structType = "JMSMessage",
                          structPackage = "ballerina.net.jms")},
        returnType = {@ReturnType(type = TypeEnum.INT)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Get Expiration header from the message") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "message",
        value = "The JMS message") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "int",
        value = "The header value") })
public class GetExpirationHeader extends AbstractNativeFunction {

    private static final Logger log = LoggerFactory.getLogger(GetExpirationHeader.class);

    public BValue[] execute(Context context) {

        BStruct messageStruct  = ((BStruct) this.getRefArgument(context, 0));
        Message jmsMessage = JMSUtils.getJMSMessage(messageStruct);
        BValue[] headerValue = null;
        try {
            headerValue = getBValues(new BInteger(jmsMessage.getJMSExpiration()));
        } catch (JMSException e) {
            log.error("Unable to retrieve Expiration from the JMS Message. " + e.getLocalizedMessage());
        }

        if (log.isDebugEnabled()) {
            log.debug("Get Expiration from message");
        }

        return headerValue;
    }
}
