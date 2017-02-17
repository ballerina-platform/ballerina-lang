/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nativeimpl.net.jms;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.connectors.jms.utils.JMSConstants;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Get HTTP Method from the message.
 */
@BallerinaFunction(
        packageName = "ballerina.net.jms",
        functionName = "getMessageType",
        args = {@Argument(name = "message", type = TypeEnum.MESSAGE)},
        returnType = {@ReturnType(type = TypeEnum.STRING)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "To get the jms message type") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "message",
        value = "message") })
public class GetMessageType extends AbstractNativeFunction {
    private String jmsMessageType;
    public BValue[] execute(Context ctx) {
        BMessage msg = (BMessage) getArgument(ctx, 0);
        if (msg.value().getProperty(JMSConstants.JMS_MESSAGE_TYPE) != null) {
            jmsMessageType = ctx.getCarbonMessage().getProperty(JMSConstants.JMS_MESSAGE_TYPE).toString();
        }
        return getBValues(new BString(jmsMessageType));
    }
}
