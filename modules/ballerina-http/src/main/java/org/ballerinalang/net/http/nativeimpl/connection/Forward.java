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

package org.ballerinalang.net.http.nativeimpl.connection;

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
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import static org.ballerinalang.mime.util.Constants.MESSAGE_ENTITY;

/**
 * Native function to respond back the caller with inbound response.
 *
 * @since 0.96
 */
@BallerinaFunction(
        packageName = "ballerina.net.http",
        functionName = "forward",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "Connection",
                             structPackage = "ballerina.net.http"),
        args = {@Argument(name = "res", type = TypeKind.STRUCT, structType = "InResponse",
                structPackage = "ballerina.net.http")},
        returnType = @ReturnType(type = TypeKind.STRUCT, structType = "HttpConnectorError",
                                 structPackage = "ballerina.net.http"),
        isPublic = true
)
public class Forward extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        BStruct connectionStruct = (BStruct) getRefArgument(context, 0);
        BStruct inboundResponseStruct = (BStruct) getRefArgument(context, 1);
        HTTPCarbonMessage requestMessage = HttpUtil.getCarbonMsg(connectionStruct, null);

        HttpUtil.checkFunctionValidity(connectionStruct, requestMessage);
        HTTPCarbonMessage responseMessage = HttpUtil
                .getCarbonMsg(inboundResponseStruct, HttpUtil.createHttpCarbonMessage(false));
        HttpUtil.setKeepAliveHeader(context, responseMessage);

        BStruct entity = (BStruct) inboundResponseStruct.getNativeData(MESSAGE_ENTITY);
        if (entity == null) {
            HttpUtil.setNewEntityToStruct(context, inboundResponseStruct);
        }
        HttpUtil.setHeadersToTransportMessage(responseMessage, inboundResponseStruct);

        return HttpUtil.prepareResponseAndSend(context, this, requestMessage, responseMessage,
                inboundResponseStruct);
    }
}
