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

package org.ballerinalang.net.http.nativeimpl.response;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.Constants;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

/**
 * Get HTTP StatusCode from the message.
 */
@BallerinaFunction(
        packageName = "ballerina.net.http.response",
        functionName = "getStatusCode",
        args = {@Argument(name = "res", type = TypeEnum.STRUCT, structType = "Response",
                          structPackage = "ballerina.net.http")},
        returnType = {@ReturnType(type = TypeEnum.INT)},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = {@Attribute(name = "value",
        value = "Gets the HTTP status code from the message") })
@BallerinaAnnotation(annotationName = "Param", attributes = {@Attribute(name = "res",
        value = "A response message") })
@BallerinaAnnotation(annotationName = "Return", attributes = {@Attribute(name = "int",
        value = "http status code") })
public class GetStatusCode extends AbstractNativeFunction {
    public BValue[] execute(Context ctx) {
        int statusCode = -1;
        BStruct responseStruct  = ((BStruct) getRefArgument(ctx, 0));
        HTTPCarbonMessage httpCarbonMessage = (HTTPCarbonMessage) responseStruct
                .getNativeData(Constants.TRANSPORT_MESSAGE);
        String statusCodeStr = String.valueOf(httpCarbonMessage.getProperty(Constants.HTTP_STATUS_CODE));

        try {
            statusCode = Integer.parseInt(statusCodeStr);
        } catch (NumberFormatException e) {
            throw new BallerinaException("Invalid status code found");
        }
        return getBValues(new BInteger(statusCode));
    }
}
