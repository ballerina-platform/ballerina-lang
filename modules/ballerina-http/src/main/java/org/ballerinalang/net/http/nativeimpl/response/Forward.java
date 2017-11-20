/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.http.nativeimpl.response;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.Constants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Native function to send client service response directly to the caller.
 *
 */
@BallerinaFunction(
        packageName = "ballerina.net.http",
        functionName = "forward",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Response",
                             structPackage = "ballerina.net.http"),
        args = {@Argument(name = "res", type = TypeKind.STRUCT, structType = "Response",
                structPackage = "ballerina.net.http")},
        isPublic = true
)
public class Forward extends AbstractNativeFunction {

    private static final Logger log = LoggerFactory.getLogger(Forward.class);

    @Override
    public BValue[] execute(Context context) {
        BStruct responseStruct = (BStruct) getRefArgument(context, 0);
        BStruct clientResponseStruct = (BStruct) getRefArgument(context, 1);
        HttpUtil.methodInvocationCheck(responseStruct);
        HttpUtil.operationNotAllowedCheck(responseStruct);
        if (clientResponseStruct.getNativeData(Constants.TRANSPORT_MESSAGE) == null) {
            throw new BallerinaException("Failed to forward: empty response parameter");
        }
        context.getConnectorFuture().notifyReply(clientResponseStruct);
        return VOID_RETURN;
    }
}
