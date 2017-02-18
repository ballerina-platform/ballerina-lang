/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.nativeimpl.lang.messages;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.nativeimpl.lang.utils.ErrorHandler;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.wso2.carbon.messaging.MessageDataSource;

/**
 * Get the payload of the Message as a XML.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.messages",
        functionName = "getXmlPayload",
        args = {@Argument(name = "m", type = TypeEnum.MESSAGE)},
        returnType = {@ReturnType(type = TypeEnum.XML)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Gets the message payload in XML format") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "m",
        value = "The message object") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "xml",
        value = "The XML representation of the message payload") })
public class GetXMLPayload extends AbstractNativeFunction {

    private static final String OPERATION = "get xml payload";
    
    @Override
    public BValue[] execute(Context context) {
        BXML result = null;
        try {
            // Accessing First Parameter Value.
            BMessage msg = (BMessage) getArgument(context, 0);
            
            if (msg.isAlreadyRead()) {
                MessageDataSource payload = msg.getMessageDataSource();
                if (payload instanceof BXML) {
                    // if the payload is already xml, return it as it is.
                    result = (BXML) payload;
                } else {
                    // else, build the xml from the string representation of the payload.
                    result = new BXML(msg.getMessageDataSource().getMessageAsString());
                }
            } else {
                result = new BXML(msg.value().getInputStream());
                msg.setMessageDataSource(result);
                msg.setAlreadyRead(true);
            }
        } catch (Throwable e) {
            ErrorHandler.handleJsonException(OPERATION, e);
        }
        // Setting output value.
        return getBValues(result);
    }
}
