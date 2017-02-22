/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.nativeimpl.lang.messages;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Native function to clone the message.
 * ballerina.model.messages:clone
 */
@BallerinaFunction(
        packageName = "ballerina.lang.messages",
        functionName = "clone",
        args = {@Argument(name = "m", type = TypeEnum.MESSAGE)},
        returnType = {@ReturnType(type = TypeEnum.MESSAGE)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Clones and creates a new instance of a message object") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "m",
        value = "The message object") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "message",
        value = "The new instance of the message object ") })
public class Clone  extends AbstractNativeFunction {

    private static final Logger LOG = LoggerFactory.getLogger(AddHeader.class);

    @Override
    public BValue[] execute(Context context) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Invoke message clone.");
        }
        BMessage msg = (BMessage) getArgument(context, 0);
        return getBValues(getCopyOfMessage(msg));
    }

    private BMessage getCopyOfMessage(BMessage originalMessage) {

        // Fix this
        throw new RuntimeException("Not supported yet");
//        MessageValue newMessageObj = new MessageValue(new DefaultCarbonMessage());
//        try {
//            // Clone headers
//            List<Header> allHeaders = originalMessage.getHeaders();
//            newMessageObj.setHeaderList(allHeaders);
//
//            // Clone payload
//            if (originalMessage.isAlreadyRead()) {
//                newMessageObj.setBuiltPayload(MessageUtils.getBValueCopy(originalMessage.getBuiltPayload()));
//            } else {
//                String payload = MessageUtils.getStringFromInputStream(originalMessage.value().getInputStream());
//                BString result = new BString(payload);
//                newMessageObj.setBuiltPayload(result);
//                originalMessage.setBuiltPayload(result);
//            }
//            newMessageObj.setAlreadyRead(true);
//        } catch (Throwable e) {
//            throw new BallerinaException("Error while cloning message: " + e.getMessage());
//        }
        
//        return newMessageObj;
    }
}
