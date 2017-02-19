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

package org.ballerinalang.nativeimpl.net.http;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.connectors.http.Constants;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.util.exceptions.BallerinaException;

/**
 * Set Content-Length header to the message.
 */
@BallerinaFunction(
        packageName = "ballerina.net.http",
        functionName = "setContentLength",
        args = {@Argument(name = "m", type = TypeEnum.MESSAGE),
                @Argument(name = "contentLength", type = TypeEnum.INT)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Sets the Content-Length header on the message") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "m",
        value = "A message object") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "contentLength",
        value = "Length of the message") })
public class SetContentLength extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        try {
            BMessage bMsg = (BMessage) getArgument(context, 0);
            int statusCode = ((BInteger) getArgument(context, 1)).intValue();
            bMsg.setHeader(Constants.HTTP_CONTENT_LENGTH, String.valueOf(statusCode));
        } catch (ClassCastException e) {
            throw new BallerinaException("Invalid message or Content-Length");
        }
        return VOID_RETURN;
    }
}
