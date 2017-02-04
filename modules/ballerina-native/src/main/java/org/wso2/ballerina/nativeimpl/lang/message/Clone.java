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

package org.wso2.ballerina.nativeimpl.lang.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BMessage;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.ReturnType;

/**
 * Native function to clone the message.
 * ballerina.lang.message:clone
 */
@BallerinaFunction(
        packageName = "ballerina.lang.message",
        functionName = "clone",
        args = {@Argument(name = "message", type = TypeEnum.MESSAGE)},
        returnType = {@ReturnType(type = TypeEnum.MESSAGE)},
        isPublic = true
)
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
