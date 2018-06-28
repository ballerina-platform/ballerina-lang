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

package org.ballerinalang.net.http.serviceendpoint;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Get the ID of the connection.
 *
 * @since 0.966
 */

@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "getCallerActions",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Listener",
                             structPackage = "ballerina/http"),
        returnType = {@ReturnType(type = TypeKind.RECORD)},
        isPublic = true
)
public class GetCallerActions extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        BMap<String, BValue> endpoint = (BMap<String, BValue>) context.getRefArgument(0);
        BMap<String, BValue> connection = (BMap<String, BValue>) endpoint.get(
                                        org.ballerinalang.net.http.HttpConstants.SERVICE_ENDPOINT_CONNECTION_FIELD);
        context.setReturnValues(connection);
    }
}
