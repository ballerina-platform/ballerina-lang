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
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.BinaryCarbonMessage;

/**
 * Native function to get payload as Blob..
 * ballerina.model.messages:getBinaryPayload
 */
@BallerinaFunction(
        packageName = "ballerina.lang.messages",
        functionName = "getBinaryPayload",
        args = {@Argument(name = "m", type = TypeEnum.MESSAGE)},
        returnType = {@ReturnType(type = TypeEnum.BLOB)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Gets the message payload in blob format") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "m",
        value = "The message object") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "blob",
        value = "The blob representation of the message payload") })
public class GetBinaryPayload extends AbstractNativeFunction {

    private static final Logger log = LoggerFactory.getLogger(GetBinaryPayload.class);

    @Override
    public BValue[] execute(Context context) {
        BBlob result;
        try {
            BMessage msg = (BMessage) getRefArgument(context, 0);
            if (msg.isAlreadyRead()) {
                result = new BBlob((byte[]) msg.getMessageDataSource().getDataObject());
            } else {
                BinaryCarbonMessage binaryCarbonMessage = (BinaryCarbonMessage) msg.value();
                byte[] arr = binaryCarbonMessage.readBytes().array();
                result = new BBlob(arr);
            }
            if (log.isDebugEnabled()) {
                log.debug("Payload in String:" + result.stringValue());
            }
        } catch (Throwable e) {
            throw new BallerinaException("Error while retrieving string payload from message: " + e.getMessage());
        }
        return getBValues(result);
    }
}
