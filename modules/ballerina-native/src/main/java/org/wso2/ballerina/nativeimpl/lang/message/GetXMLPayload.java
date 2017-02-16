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

package org.wso2.ballerina.nativeimpl.lang.message;

import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BMessage;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.BXML;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.ReturnType;
import org.wso2.ballerina.nativeimpl.lang.utils.ErrorHandler;
import org.wso2.carbon.messaging.MessageDataSource;

/**
 * Get the payload of the Message as a XML.
 */
@BallerinaFunction(
        packageName = "ballerina.lang.message",
        functionName = "getXmlPayload",
        args = {@Argument(name = "m", type = TypeEnum.MESSAGE)},
        returnType = {@ReturnType(type = TypeEnum.XML)},
        isPublic = true
)
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
