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

import io.ballerina.runtime.api.BStringUtils;
import io.ballerina.runtime.api.BValueCreator;
import io.ballerina.runtime.api.BalEnv;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.scheduling.Scheduler;
import io.ballerina.runtime.scheduling.Strand;
import io.ballerina.runtime.util.exceptions.BallerinaException;
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
public class GetNextPromise extends AbstractHTTPAction {

    public static Object getNextPromise(BalEnv env, BObject clientObj, BObject handleObj) {
        Strand strand = Scheduler.getStrand();
        HttpClientConnector clientConnector = (HttpClientConnector) clientObj.getNativeData(HttpConstants.CLIENT);
        DataContext dataContext = new DataContext(strand, clientConnector, env.markAsync(), handleObj, null);
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
            BObject pushPromiseObj =
                    BValueCreator.createObjectValue(HttpConstants.PROTOCOL_HTTP_PKG_ID,
                                                    HttpConstants.PUSH_PROMISE,
                                                    BStringUtils.fromString(pushPromise.getPath()),
                                                    BStringUtils.fromString(pushPromise.getMethod()));
            HttpUtil.populatePushPromiseStruct(pushPromiseObj, pushPromise);
            dataContext.notifyInboundResponseStatus(pushPromiseObj, null);
        }
    }
}
