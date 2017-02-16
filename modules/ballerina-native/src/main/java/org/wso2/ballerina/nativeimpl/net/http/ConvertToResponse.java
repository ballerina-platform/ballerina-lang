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
package org.wso2.ballerina.nativeimpl.net.http;

import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.core.runtime.Constants;
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
public class ConvertToResponse extends AbstractNativeFunction {
    public BValue[] execute(Context ctx) {
        if (!org.wso2.carbon.messaging.Constants.DIRECTION_RESPONSE.
                equals(ctx.getCarbonMessage().getProperty(org.wso2.carbon.messaging.Constants.DIRECTION))) {
            // getting the Content-Type of request message
            String requestContentType = ctx.getCarbonMessage().getHeader(
                    org.wso2.ballerina.nativeimpl.lang.utils.Constants.CONTENT_TYPE);
            ctx.getCarbonMessage().getHeaders().clear();
            // setting the request Content-Type for response message
            ctx.getCarbonMessage().setHeader(org.wso2.ballerina.nativeimpl.lang.utils.Constants.CONTENT_TYPE
                    , requestContentType);
            // Set any intermediate headers set during ballerina execution
            if (ctx.getCarbonMessage().getProperty(Constants.INTERMEDIATE_HEADERS) != null) {
                Headers headers = (Headers) ctx.getCarbonMessage().getProperty(Constants.INTERMEDIATE_HEADERS);
                ctx.getCarbonMessage().setHeaders(headers.getAll());
            }
        }
        return VOID_RETURN;
    }
}
