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

package org.ballerinalang.nativeimpl.net.file;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.StreamingCarbonMessage;

/**
 * Function to acknowledge that file processing is done.
 *
 * This function sends an acknowledgement to the sender of the message,
 * saying that the message processing is done.
 *
 * What happens under the hood:
 * As the received {@link StreamingCarbonMessage} carries
 * a reference to a file {@link java.io.InputStream}, once acknowledged,
 * the message sender will close the file input stream.
 *
 * This means, this function needs to be called after all the processing
 * with the message has being done.
 *
 * ballerina.net.file:acknowledge
 */
@BallerinaFunction(
        packageName = "ballerina.net.file",
        functionName = "acknowledge",
        args = {@Argument(name = "message", type = TypeEnum.MESSAGE)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "This function acknowledges to the message sender that processing of the file has finished.") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "message",
        value = "message") })
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
                    (cMsg == null ? ". " : ": " + cMsg.getHeader(FILE_NAME)), e);
        }
        return VOID_RETURN;
    }
}
