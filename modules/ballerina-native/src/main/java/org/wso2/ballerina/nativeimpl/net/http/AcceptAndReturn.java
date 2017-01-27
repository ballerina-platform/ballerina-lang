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
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.messaging.Headers;

/**
 * Accept the message and return with status code.
 */
@BallerinaFunction(
        packageName = "ballerina.net.http",
        functionName = "acceptAndReturn",
        args = {@Argument(name = "statusCode", type = TypeEnum.INT)},
        isPublic = true
)
public class AcceptAndReturn extends AbstractNativeFunction {
    public BValue[] execute(Context ctx) {
        String statusCode = getArgument(ctx, 0).stringValue();
        DefaultCarbonMessage carbonMessage = new DefaultCarbonMessage();
        carbonMessage.setProperty(org.wso2.carbon.messaging.Constants.DIRECTION,
                org.wso2.carbon.messaging.Constants.DIRECTION_RESPONSE);
        // Set any intermediate headers set during ballerina execution
        if (ctx.getCarbonMessage().getProperty(Constants.INTERMEDIATE_HEADERS) != null) {
            Headers headers = (Headers) ctx.getCarbonMessage().getProperty(Constants.INTERMEDIATE_HEADERS);
            carbonMessage.setHeaders(headers.getAll());
        }
        if (statusCode != null) {
            carbonMessage.setProperty(org.wso2.carbon.transport.http.netty.common.Constants.HTTP_STATUS_CODE,
                    Integer.parseInt(statusCode));
        } else {
            carbonMessage.setProperty(org.wso2.carbon.transport.http.netty.common.Constants.HTTP_STATUS_CODE, 202);
        }
        carbonMessage.setStringMessageBody("");
        ctx.getBalCallback().done(carbonMessage);
        return VOID_RETURN;
    }
}
