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

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.DataContext;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpClientConnectorListener;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.ResponseHandle;

/**
 * {@code GetNextPromise} action can be used to get the next available push promise message associated with
 * a previous asynchronous invocation.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "getNextPromise",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = HttpConstants.HTTP_CLIENT,
                structPackage = "ballerina/http")
)
public class GetNextPromise extends AbstractHTTPAction {

    public static Object getNextPromise(Strand strand, ObjectValue clientObj, ObjectValue handleObj) {
        HttpClientConnector clientConnector = (HttpClientConnector) clientObj.getNativeData(HttpConstants.CLIENT);
        DataContext dataContext = new DataContext(strand, clientConnector, new NonBlockingCallback(strand), handleObj,
                                                  null);
        ResponseHandle responseHandle = (ResponseHandle) handleObj.getNativeData(HttpConstants.TRANSPORT_HANDLE);
        if (responseHandle == null) {
            throw new BallerinaException("invalid http handle");
        }
        clientConnector.getNextPushPromise(responseHandle).setPushPromiseListener(new PromiseListener(dataContext));
        return null;
    }

    private static class PromiseListener implements HttpClientConnectorListener {

        private DataContext dataContext;

        PromiseListener(DataContext dataContext) {
            this.dataContext = dataContext;
        }

        @Override
        public void onPushPromise(Http2PushPromise pushPromise) {
            ObjectValue pushPromiseObj = BallerinaValues.createObjectValue(HttpConstants.PROTOCOL_HTTP_PKG_ID,
                    HttpConstants.PUSH_PROMISE, pushPromise.getPath(), pushPromise.getMethod());
            HttpUtil.populatePushPromiseStruct(pushPromiseObj, pushPromise);
            dataContext.notifyInboundResponseStatus(pushPromiseObj, null);
        }
    }
}
