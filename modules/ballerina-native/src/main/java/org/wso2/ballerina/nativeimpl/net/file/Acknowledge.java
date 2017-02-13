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
import org.wso2.ballerina.core.model.values.BMessage;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
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
        functionName = "acknowledge",
        args = {@Argument(name = "message", type = TypeEnum.MESSAGE)},
        isPublic = true
)
public class Acknowledge extends AbstractNativeFunction {

    private static final Logger log = LoggerFactory.getLogger(Acknowledge.class);
    private static final String FILE_NAME = "FILE_NAME";

    @Override
    public BValue[] execute(Context context) {
        BMessage msg;
        CarbonMessage cMsg = null;
        try {
            msg = (BMessage) getArgument(context, 0);
            cMsg = msg.value();

            if (cMsg instanceof StreamingCarbonMessage) {
                context.getBalCallback().done(cMsg);
            } else {
                throw new BallerinaException("Invalid message type received. " +
                        "Required: " + StreamingCarbonMessage.class.getCanonicalName() +
                        ". Found: " + cMsg.getClass().getCanonicalName());
            }

            if (log.isDebugEnabled()) {
                log.debug("Acknowledged file: " + cMsg.getHeader(FILE_NAME));
            }
        } catch (Throwable e) {
            throw new BallerinaException("Error while acknowledging file" +
                    (cMsg == null ? ". " : ": " + cMsg.getHeader(FILE_NAME))
                    + " . Reason : " + e.getMessage());
        }
        return VOID_RETURN;
    }
}
