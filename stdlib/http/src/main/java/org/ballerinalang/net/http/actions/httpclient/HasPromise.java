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

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.HttpConstants;
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

    public static boolean hasPromise(Strand strand, ObjectValue clientObj, ObjectValue handleObj) {
        ResponseHandle responseHandle = (ResponseHandle) handleObj.getNativeData(HttpConstants.TRANSPORT_HANDLE);
        if (responseHandle == null) {
            throw new BallerinaException("invalid http handle");
        }
        HttpClientConnector clientConnector = (HttpClientConnector) clientObj.getNativeData(HttpConstants.CLIENT);
        clientConnector.hasPushPromise(responseHandle).
                setPromiseAvailabilityListener(new PromiseAvailabilityCheckListener(new NonBlockingCallback(strand)));
        return false;
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
