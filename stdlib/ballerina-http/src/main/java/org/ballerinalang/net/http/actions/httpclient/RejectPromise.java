/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.net.http.actions.httpclient;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.message.Http2PushPromise;

/**
 * {@code RejectPromise} action can be used to reject a push promise.
 */
@BallerinaAction(
        packageName = "ballerina.net.http",
        actionName = "rejectPromise",
        connectorName = HttpConstants.CLIENT_CONNECTOR,
        args = {
                @Argument(name = "c", type = TypeKind.CONNECTOR),
                @Argument(name = "promise", type = TypeKind.STRUCT, structType = "PushPromise",
                        structPackage = "ballerina.net.http")
        },
        returnType = {
                @ReturnType(type = TypeKind.BOOLEAN)
        },
        connectorArgs = {
                @Argument(name = "serviceUri", type = TypeKind.STRING),
                @Argument(name = "options", type = TypeKind.STRUCT, structType = "Options",
                        structPackage = "ballerina.net.http")
        }
)
public class RejectPromise extends AbstractHTTPAction {

    @Override
    public void execute(Context context, CallableUnitCallback callback) {

        BStruct pushPromiseStruct = (BStruct) context.getRefArgument(1);
        Http2PushPromise http2PushPromise = HttpUtil.getPushPromise(pushPromiseStruct, null);
        if (http2PushPromise == null) {
            throw new BallerinaException("invalid push promise");
        }
        BConnector bConnector = (BConnector) context.getRefArgument(0);
        HttpClientConnector clientConnector =
                (HttpClientConnector) bConnector.getNativeData(HttpConstants.CLIENT_CONNECTOR);
        clientConnector.rejectPushResponse(http2PushPromise);
        context.setReturnValues(new BBoolean(true)); // TODO: Implement a listener to see the progress
        callback.notifySuccess();
    }
}
