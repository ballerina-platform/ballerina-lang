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

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.net.http.BHttpUtil;
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
@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "nativeExecute"
)
public class Execute extends AbstractHTTPAction {

    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        DataContext dataContext = new DataContext(context, callback, createOutboundRequestMsg(context));
        // Execute the operation
        executeNonBlockingAction(dataContext, false);
    }

    protected HttpCarbonMessage createOutboundRequestMsg(Context context) {
        // Extract Argument values
        BMap<String, BValue> bConnector = (BMap<String, BValue>) context.getRefArgument(0);
        String httpVerb = context.getStringArgument(1);
        String path = context.getStringArgument(2);
        BMap<String, BValue> requestStruct = ((BMap<String, BValue>) context.getRefArgument(1));

        HttpCarbonMessage outboundRequestMsg = HttpUtil
                .getCarbonMsg(requestStruct, HttpUtil.createHttpCarbonMessage(true));

        BHttpUtil.checkEntityAvailability(context, requestStruct);
        BHttpUtil.enrichOutboundMessage(outboundRequestMsg, requestStruct);
        prepareOutboundRequest(context, path, outboundRequestMsg, isNoEntityBodyRequest(requestStruct));

        // If the verb is not specified, use the verb in incoming message
        if (httpVerb == null || httpVerb.isEmpty()) {
            httpVerb = outboundRequestMsg.getHttpMethod();
        }
        outboundRequestMsg.setHttpMethod(httpVerb.trim().toUpperCase(Locale.getDefault()));
        handleAcceptEncodingHeader(outboundRequestMsg, getCompressionConfigFromEndpointConfig(bConnector));

        return outboundRequestMsg;
    }

    @SuppressWarnings("unchecked")
    public static Object nativeExecute(Strand strand, ObjectValue httpClient, String verb, String path,
                                       ObjectValue requestObj) {
        String url = httpClient.getStringValue(CLIENT_ENDPOINT_SERVICE_URI);
        MapValue<String, Object> config = (MapValue<String, Object>) httpClient.get(CLIENT_ENDPOINT_CONFIG);
        HttpClientConnector clientConnector = (HttpClientConnector) httpClient.getNativeData(HttpConstants.CLIENT);
        HttpCarbonMessage outboundRequestMsg = createOutboundRequestMsg(config, url, verb, path, requestObj);
        DataContext dataContext = new DataContext(strand, clientConnector, new NonBlockingCallback(strand), requestObj,
                                                  outboundRequestMsg);
        executeNonBlockingAction(dataContext, false);
        return null;
    }

    protected static HttpCarbonMessage createOutboundRequestMsg(MapValue config, String serviceUri, String httpVerb,
                                                                String path, ObjectValue requestObj) {
        HttpCarbonMessage outboundRequestMsg = HttpUtil
                .getCarbonMsg(requestObj, HttpUtil.createHttpCarbonMessage(true));

        HttpUtil.checkEntityAvailability(requestObj);
        HttpUtil.enrichOutboundMessage(outboundRequestMsg, requestObj);
        prepareOutboundRequest(serviceUri, path, outboundRequestMsg, isNoEntityBodyRequest(requestObj));

        // If the verb is not specified, use the verb in incoming message
        if (httpVerb == null || httpVerb.isEmpty()) {
            httpVerb = outboundRequestMsg.getHttpMethod();
        }
        outboundRequestMsg.setHttpMethod(httpVerb.trim().toUpperCase(Locale.getDefault()));
        handleAcceptEncodingHeader(outboundRequestMsg, getCompressionConfigFromEndpointConfig(config));
        return outboundRequestMsg;
    }
}
