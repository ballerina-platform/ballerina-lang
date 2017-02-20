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
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.runtime.Constants;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.Headers;

/**
 * Convert the message into an HTTP Response.
 */
@BallerinaFunction(
        packageName = "ballerina.net.http",
        functionName = "convertToResponse",
        args = {@Argument(name = "m", type = TypeEnum.MESSAGE)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Converts the message into an HTTP response") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "m",
        value = "A message object") })
public class ConvertToResponse extends AbstractNativeFunction {
    public BValue[] execute(Context ctx) {

        CarbonMessage carbonMessage = ((BMessage) getArgument(ctx, 0)).value();

        if (!org.wso2.carbon.messaging.Constants.DIRECTION_RESPONSE.
                equals(carbonMessage.getProperty(org.wso2.carbon.messaging.Constants.DIRECTION))) {
            // getting the Content-Type of request message
            String requestContentType = carbonMessage.getHeader(
                    org.ballerinalang.nativeimpl.lang.utils.Constants.CONTENT_TYPE);
            carbonMessage.getHeaders().clear();
            // setting the request Content-Type for response message
            carbonMessage.setHeader(org.ballerinalang.nativeimpl.lang.utils.Constants.CONTENT_TYPE
                    , requestContentType);
            // Set any intermediate headers set during ballerina execution
            if (carbonMessage.getProperty(Constants.INTERMEDIATE_HEADERS) != null) {
                Headers headers = (Headers) carbonMessage.getProperty(Constants.INTERMEDIATE_HEADERS);
                carbonMessage.setHeaders(headers.getAll());
            }
        }
        return VOID_RETURN;
    }
}
