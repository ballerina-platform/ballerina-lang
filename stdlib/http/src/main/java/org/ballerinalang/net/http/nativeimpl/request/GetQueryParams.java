/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http.nativeimpl.request;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.BMapType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.uri.URIUtil;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

/**
 * Get the Query params from HTTP message and return a map.
 *
 * @since 0.94
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "getQueryParams",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Request",
                             structPackage = "ballerina.http"),
        returnType = {@ReturnType(type = TypeKind.MAP, elementType = TypeKind.STRING)},
        isPublic = true
)
public class GetQueryParams extends BlockingNativeCallableUnit {
    @Override
    public void execute(Context context) {
        try {
            BStruct requestStruct  = ((BStruct) context.getRefArgument(0));
            HTTPCarbonMessage httpCarbonMessage = (HTTPCarbonMessage) requestStruct
                    .getNativeData(HttpConstants.TRANSPORT_MESSAGE);
            BMapType mapType = new BMapType(BTypes.typeString);
            if (httpCarbonMessage.getProperty(HttpConstants.QUERY_STR) != null) {
                String queryString = (String) httpCarbonMessage.getProperty(HttpConstants.QUERY_STR);
                BMap<String, BString> params = new BMap<>(mapType);
                URIUtil.populateQueryParamMap(queryString, params);
                context.setReturnValues(params);
            } else {
                context.setReturnValues(new BMap<>(mapType));
            }
        } catch (Throwable e) {
            throw new BallerinaException("Error while retrieving query param from message: " + e.getMessage());
        }
    }
}
