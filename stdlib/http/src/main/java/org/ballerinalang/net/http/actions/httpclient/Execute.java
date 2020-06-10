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

import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
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
    public static Object execute(ObjectValue httpClient, BString verb, BString path, ObjectValue requestObj) {
        String url = httpClient.getStringValue(CLIENT_ENDPOINT_SERVICE_URI).getValue();
        Strand strand = Scheduler.getStrand();
        MapValue<BString, Object> config = (MapValue<BString, Object>) httpClient.get(CLIENT_ENDPOINT_CONFIG);
        HttpClientConnector clientConnector = (HttpClientConnector) httpClient.getNativeData(HttpConstants.CLIENT);
        HttpCarbonMessage outboundRequestMsg = createOutboundRequestMsg(strand, config, url, verb.getValue(),
                                                                        path.getValue(), requestObj);
        DataContext dataContext = new DataContext(strand, clientConnector, new NonBlockingCallback(strand), requestObj,
                                                  outboundRequestMsg);
        executeNonBlockingAction(dataContext, false);
        return null;
    }

    protected static HttpCarbonMessage createOutboundRequestMsg(Strand strand, MapValue<BString, Object> config,
                                                                String serviceUri, String httpVerb, String path,
                                                                ObjectValue requestObj) {
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
