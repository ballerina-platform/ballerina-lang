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

package org.ballerinalang.net.http.nativeimpl.request;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.Constants;
import org.ballerinalang.net.http.HttpUtil;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

/**
 * Get the request url of the Message.
 */
@BallerinaFunction(
        packageName = "ballerina.net.http.request",
        functionName = "getRequestURL",
        args = {@Argument(name = "req", type = TypeKind.STRUCT, structType = "Request",
                          structPackage = "ballerina.net.http")},
        returnType = {@ReturnType(type = TypeKind.STRING)},
        isPublic = true
)
public class GetRequestURL extends AbstractNativeFunction {
    public BValue[] execute(Context ctx) {
        String requestURL = "";
        BStruct requestStruct  = ((BStruct) getRefArgument(ctx, 0));
        //TODO check below line
        HTTPCarbonMessage httpCarbonMessage = HttpUtil.getCarbonMsg(requestStruct, null);
        Object requestURLPropertyObject = httpCarbonMessage.getProperty(Constants.TO);
        if (requestURLPropertyObject != null) {
            requestURL = requestURLPropertyObject.toString();
        }
        return getBValues(new BString(requestURL));
    }
}
