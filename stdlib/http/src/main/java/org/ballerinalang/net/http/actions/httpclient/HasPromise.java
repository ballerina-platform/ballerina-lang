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
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
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
        receiver = @Receiver(type = TypeKind.OBJECT, structType = HttpConstants.HTTP_CLIENT,
                structPackage = "ballerina/http")
)
public class HasPromise extends AbstractHTTPAction {

    @Override
    public void execute(Context context, CallableUnitCallback callback) {

        BMap<String, BValue> handleStruct = ((BMap<String, BValue>) context.getRefArgument(1));

        ResponseHandle responseHandle = (ResponseHandle) handleStruct.getNativeData(HttpConstants.TRANSPORT_HANDLE);
        if (responseHandle == null) {
            throw new BallerinaException("invalid http handle");
        }
        BMap<String, BValue> bConnector = (BMap<String, BValue>) context.getRefArgument(0);
        HttpClientConnector clientConnector = (HttpClientConnector) ((BMap<String, BValue>) bConnector.values()[0])
                .getNativeData(HttpConstants.CLIENT);
        clientConnector.hasPushPromise(responseHandle).
                setPromiseAvailabilityListener(new BPromiseAvailabilityCheckListener(context, callback));
    }

    public static void hasPromise(Strand strand, ObjectValue clientObj, ObjectValue handleObj) {
        ResponseHandle responseHandle = (ResponseHandle) handleObj.getNativeData(HttpConstants.TRANSPORT_HANDLE);
        if (responseHandle == null) {
            throw new BallerinaException("invalid http handle");
        }
        HttpClientConnector clientConnector = (HttpClientConnector) clientObj.getNativeData(HttpConstants.CLIENT);
        clientConnector.hasPushPromise(responseHandle).
                setPromiseAvailabilityListener(new PromiseAvailabilityCheckListener(new NonBlockingCallback(strand)));
    }

    private static class BPromiseAvailabilityCheckListener implements HttpClientConnectorListener {

        private Context context;
        private CallableUnitCallback callback;

        BPromiseAvailabilityCheckListener(Context context, CallableUnitCallback callback) {
            this.context = context;
            this.callback = callback;
        }

        @Override
        public void onPushPromiseAvailability(boolean isPromiseAvailable) {
            context.setReturnValues(new BBoolean(isPromiseAvailable));
            callback.notifySuccess();
        }
    }

    private static class PromiseAvailabilityCheckListener implements HttpClientConnectorListener {

        private NonBlockingCallback callback;

        PromiseAvailabilityCheckListener(NonBlockingCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onPushPromiseAvailability(boolean isPromiseAvailable) {
            callback.setReturnValues(isPromiseAvailable);
            callback.notifySuccess();
        }
    }
}
