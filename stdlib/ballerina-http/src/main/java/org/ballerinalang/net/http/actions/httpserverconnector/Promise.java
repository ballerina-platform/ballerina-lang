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

package org.ballerinalang.net.http.actions.httpserverconnector;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.Http2PushPromise;

/**
 * {@code Promise} is the native action to respond back to the client with a PUSH_PROMISE frame.
 *
 * @since 0.965.0
 */
@BallerinaAction(
        packageName = "ballerina.net.http",
        actionName = "promise",
        connectorName = HttpConstants.SERVER_CONNECTOR,
        args = {@Argument(name = "promise", type = TypeKind.STRUCT, structType = "PushPromise",
                        structPackage = "ballerina.net.http")},
        returnType = @ReturnType(type = TypeKind.STRUCT, structType = "HttpConnectorError",
                                 structPackage = "ballerina.net.http")
)
public class Promise extends AbstractConnectorAction {

    @Override
    public void execute(Context context) {
        BConnector serverConnector = (BConnector) context.getRefArgument(0);
        HTTPCarbonMessage inboundRequestMsg =
                (HTTPCarbonMessage) serverConnector.getNativeData(HttpConstants.TRANSPORT_MESSAGE);
        HttpUtil.serverBConnectorCheck(inboundRequestMsg);

        BStruct pushPromiseStruct = (BStruct) context.getRefArgument(1);
        Http2PushPromise http2PushPromise = HttpUtil.getPushPromise(pushPromiseStruct,
                                                                    HttpUtil.createHttpPushPromise(pushPromiseStruct));
        HttpResponseFuture outboundRespStatusFuture = HttpUtil.pushPromise(inboundRequestMsg, http2PushPromise);
        BValue[] outboundResponseStatus = handleResponseStatus(context, outboundRespStatusFuture);
        context.setReturnValues(outboundResponseStatus);
    }
}
