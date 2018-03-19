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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nativeimpl.runtime;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.Map;

/**
 * Native function to get user claims from authentication context.
 *
 * @since 0.965.0
 */
@BallerinaFunction(
        packageName = "ballerina.runtime",
        functionName = "getUserClaims",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "AuthenticationContext", structPackage = "ballerina" +
                ".io"),
        returnType = {@ReturnType(type = TypeKind.MAP)},
        isPublic = true
)
public class GetUserClaims extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        AuthenticationContext authContext = AuthenticationContextUtils.getAuthenticationContext(context);
        Map<String, String> userClaims = authContext.getClaims();
        BMap<String, BString> bMap = new BMap<>();
        if (userClaims != null) {
            userClaims.forEach((key, value) -> bMap.put(key, new BString(value)));
        }
        context.setReturnValues(bMap);
    }
}
