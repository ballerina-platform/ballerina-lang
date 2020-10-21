/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.http.actions.httpclient;

import io.ballerina.runtime.api.BalEnv;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.scheduling.Scheduler;
import io.ballerina.runtime.scheduling.Strand;
import io.ballerina.runtime.util.exceptions.BallerinaException;
import org.ballerinalang.net.http.DataContext;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import static org.ballerinalang.net.http.HttpConstants.CLIENT_ENDPOINT_CONFIG;
import static org.ballerinalang.net.http.HttpConstants.CLIENT_ENDPOINT_SERVICE_URI;

/**
 * Utilities related to HTTP client actions.
 *
 * @since 1.1.0
 */
public class HttpClientAction extends AbstractHTTPAction {

    public static Object executeClientAction(BalEnv env, BObject httpClient, BString path,
                                             BObject requestObj, BString httpMethod) {
        Strand strand = Scheduler.getStrand();
        String url = httpClient.getStringValue(CLIENT_ENDPOINT_SERVICE_URI).getValue();
        BMap<BString, Object> config = (BMap<BString, Object>) httpClient.get(CLIENT_ENDPOINT_CONFIG);
        HttpClientConnector clientConnector = (HttpClientConnector) httpClient.getNativeData(HttpConstants.CLIENT);
        HttpCarbonMessage outboundRequestMsg = createOutboundRequestMsg(strand, url, config, path.getValue().
                replaceAll(HttpConstants.REGEX, HttpConstants.SINGLE_SLASH), requestObj);
        outboundRequestMsg.setHttpMethod(httpMethod.getValue());
        DataContext dataContext = new DataContext(strand, clientConnector, env.markAsync(),
                                                  requestObj, outboundRequestMsg);
        executeNonBlockingAction(dataContext, false);
        return null;
    }

    public static void rejectPromise(BObject clientObj, BObject pushPromiseObj) {
        Http2PushPromise http2PushPromise = HttpUtil.getPushPromise(pushPromiseObj, null);
        if (http2PushPromise == null) {
            throw new BallerinaException("invalid push promise");
        }
        HttpClientConnector clientConnector = (HttpClientConnector) clientObj.getNativeData(HttpConstants.CLIENT);
        clientConnector.rejectPushResponse(http2PushPromise);
    }
}
