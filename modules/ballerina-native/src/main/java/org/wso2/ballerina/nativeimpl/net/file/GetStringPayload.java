/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.nativeimpl.net.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.util.MessageUtils;
import org.wso2.ballerina.core.model.values.BMessage;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.ReturnType;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.StreamingCarbonMessage;

/**
 * Function to get payload as String.
 * This function reads the {@link java.io.InputStream} of the incoming file,
 * converts it to a String, closes the stream and returns the String message.
 * ballerina.net.file:getStringPayload
 */
@BallerinaFunction(
        packageName = "ballerina.net.file",
        functionName = "getStringPayload",
        args = {@Argument(name = "message", type = TypeEnum.MESSAGE)},
        returnType = {@ReturnType(type = TypeEnum.STRING)},
        isPublic = true
)
public class GetStringPayload extends AbstractNativeFunction {

    private static final Logger log = LoggerFactory.getLogger(GetStringPayload.class);

    @Override
    public BValue[] execute(Context context) {
        BString result;
        try {
            BMessage msg = (BMessage) getArgument(context, 0);
            if (msg.isAlreadyRead()) {
                result = new BString(msg.getMessageDataSource().getMessageAsString());
            } else {
                CarbonMessage cMsg = msg.value();
                String payload = MessageUtils.getStringFromInputStream(cMsg.getInputStream());
                handleCallBack(cMsg, context);
                result = new BString(payload);
                msg.setMessageDataSource(payload);
                msg.setAlreadyRead(true);
            }
            if (log.isDebugEnabled()) {
                log.debug("Payload in String:" + result.stringValue());
            }
        } catch (Throwable e) {
            throw new BallerinaException("Error while retrieving string payload from message: " + e.getMessage());
        }
        return getBValues(result);
    }

    private void handleCallBack(CarbonMessage message, Context context) {
        if (message instanceof StreamingCarbonMessage) {
            context.getBalCallback().done(message);
        } else {
            throw new BallerinaException("Invalid message type received. " +
                    "Required: " + StreamingCarbonMessage.class.getCanonicalName() +
                    ". Found: " + message.getClass().getCanonicalName());
        }
    }
}
