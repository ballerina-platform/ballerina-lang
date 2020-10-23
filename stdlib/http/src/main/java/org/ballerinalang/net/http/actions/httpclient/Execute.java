/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.scheduling.Scheduler;
import io.ballerina.runtime.scheduling.Strand;
import org.ballerinalang.net.http.DataContext;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.util.Locale;

import static org.ballerinalang.net.http.HttpConstants.CLIENT_ENDPOINT_CONFIG;
import static org.ballerinalang.net.http.HttpConstants.CLIENT_ENDPOINT_SERVICE_URI;

/**
 * {@code Execute} action can be used to invoke execute a http call with any httpVerb.
 */
public class Execute extends AbstractHTTPAction {
    @SuppressWarnings("unchecked")
    public static Object execute(BalEnv env, BObject httpClient, BString verb, BString path, BObject requestObj) {
        String url = httpClient.getStringValue(CLIENT_ENDPOINT_SERVICE_URI).getValue();
        Strand strand = Scheduler.getStrand();
        BMap<BString, Object> config = (BMap<BString, Object>) httpClient.get(CLIENT_ENDPOINT_CONFIG);
        HttpClientConnector clientConnector = (HttpClientConnector) httpClient.getNativeData(HttpConstants.CLIENT);
        HttpCarbonMessage outboundRequestMsg = createOutboundRequestMsg(strand, config, url, verb.getValue(),
                                                                        path.getValue(), requestObj);
        DataContext dataContext = new DataContext(strand, clientConnector, env.markAsync(), requestObj,
                                                  outboundRequestMsg);
        executeNonBlockingAction(dataContext, false);
        return null;
    }

    protected static HttpCarbonMessage createOutboundRequestMsg(Strand strand, BMap<BString, Object> config,
                                                                String serviceUri, String httpVerb, String path,
                                                                BObject requestObj) {
        HttpCarbonMessage outboundRequestMsg = HttpUtil
                .getCarbonMsg(requestObj, HttpUtil.createHttpCarbonMessage(true));

        HttpUtil.checkEntityAvailability(requestObj);
        HttpUtil.enrichOutboundMessage(outboundRequestMsg, requestObj);
        prepareOutboundRequest(strand, serviceUri, path, outboundRequestMsg, isNoEntityBodyRequest(requestObj));

        // If the verb is not specified, use the verb in incoming message
        if (httpVerb == null || httpVerb.isEmpty()) {
            httpVerb = outboundRequestMsg.getHttpMethod();
        }
        outboundRequestMsg.setHttpMethod(httpVerb.trim().toUpperCase(Locale.getDefault()));
        handleAcceptEncodingHeader(outboundRequestMsg, getCompressionConfigFromEndpointConfig(config));
        return outboundRequestMsg;
    }
}
