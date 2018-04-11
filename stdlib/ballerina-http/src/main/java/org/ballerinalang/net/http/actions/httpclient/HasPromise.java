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
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpClientConnectorListener;
import org.wso2.transport.http.netty.message.ResponseHandle;

/**
 * {@code HasPromise} action can be used to check whether a push promise is available.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "hasPromise",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = HttpConstants.HTTP_CLIENT,
                structPackage = "ballerina.http"),
        args = {
                @Argument(name = "client", type = TypeKind.STRUCT),
                @Argument(name = "httpFuture", type = TypeKind.STRUCT, structType = "HttpFuture",
                        structPackage = "ballerina.http")
        },
        returnType = {
                @ReturnType(type = TypeKind.BOOLEAN)
        }
)
public class HasPromise extends AbstractHTTPAction {

    @Override
    public void execute(Context context, CallableUnitCallback callback) {

        BStruct handleStruct = ((BStruct) context.getRefArgument(1));

        ResponseHandle responseHandle = (ResponseHandle) handleStruct.getNativeData(HttpConstants.TRANSPORT_HANDLE);
        if (responseHandle == null) {
            throw new BallerinaException("invalid http handle");
        }
        BStruct bConnector = (BStruct) context.getRefArgument(0);
        HttpClientConnector clientConnector =
                (HttpClientConnector) bConnector.getNativeData(HttpConstants.HTTP_CLIENT);
        clientConnector.hasPushPromise(responseHandle).
                setPromiseAvailabilityListener(new PromiseAvailabilityCheckListener(context, callback));
    }

    private static class PromiseAvailabilityCheckListener implements HttpClientConnectorListener {

        private Context context;
        private CallableUnitCallback callback;

        PromiseAvailabilityCheckListener(Context context, CallableUnitCallback callback) {
            this.context = context;
            this.callback = callback;
        }

        @Override
        public void onPushPromiseAvailability(boolean isPromiseAvailable) {
            context.setReturnValues(new BBoolean(isPromiseAvailable));
            callback.notifySuccess();
        }
    }
}
