/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.http.nativeimpl.inbound.request;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.uri.URIUtil;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

/**
 * Get the Query params from HTTP message and return a map.
 *
 * @since 0.94
 */
@BallerinaFunction(
        packageName = "ballerina.net.http",
        functionName = "getMatrixParams",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "InRequest",
                             structPackage = "ballerina.net.http"),
        args = {@Argument(name = "path", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.MAP, elementType = TypeKind.STRING)},
        isPublic = true
)
public class GetMatrixParams extends AbstractNativeFunction {
    @Override
    public BValue[] execute(Context context) {
        BStruct requestStruct  = ((BStruct) getRefArgument(context, 0));
        String path = getStringArgument(context, 0);
        HTTPCarbonMessage httpCarbonMessage = HttpUtil.getCarbonMsg(requestStruct, null);
        return new BValue[]{URIUtil.getMatrixParamsMap(path, httpCarbonMessage)};
    }
}
