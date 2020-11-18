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

import io.ballerina.runtime.api.BalEnv;
import io.ballerina.runtime.api.values.BError;
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
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

/**
 * {@code GetPromisedResponse} action can be used to get a push response message associated with a
 * previous asynchronous invocation.
 */
public class GetPromisedResponse extends AbstractHTTPAction {

    public static Object getPromisedResponse(BalEnv env, BObject clientObj, BObject pushPromiseObj) {
        Strand strand = Scheduler.getStrand();
        HttpClientConnector clientConnector = (HttpClientConnector) clientObj.getNativeData(HttpConstants.CLIENT);
        DataContext dataContext = new DataContext(strand, clientConnector, env.markAsync(), pushPromiseObj, null);
        Http2PushPromise http2PushPromise = HttpUtil.getPushPromise(pushPromiseObj, null);
        if (http2PushPromise == null) {
            throw new BallerinaException("invalid push promise");
        }
        clientConnector.getPushResponse(http2PushPromise).
                setPushResponseListener(new PushResponseListener(dataContext), http2PushPromise.getPromisedStreamId());
        return null;
    }

    private static class PushResponseListener implements HttpClientConnectorListener {

        private DataContext dataContext;

        PushResponseListener(DataContext dataContext) {
            this.dataContext = dataContext;
        }

        @Override
        public void onPushResponse(int promisedId, HttpCarbonMessage httpCarbonMessage) {
            dataContext.notifyInboundResponseStatus(
                    HttpUtil.createResponseStruct(httpCarbonMessage), null);
        }

        @Override
        public void onError(Throwable throwable) {
            BError httpConnectorError = HttpUtil
                    .createHttpError(throwable.getMessage());
            dataContext.notifyInboundResponseStatus(null, httpConnectorError);
        }
    }
}
