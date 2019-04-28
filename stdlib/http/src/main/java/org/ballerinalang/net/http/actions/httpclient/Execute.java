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
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.net.http.DataContext;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.util.Locale;

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
//        DataContext dataContext = new DataContext(context, callback, createOutboundRequestMsg(context));
//        // Execute the operation
//        executeNonBlockingAction(dataContext, false);
    }

    public static void nativeExecute(Strand strand, ObjectValue clientObj, String verb, String path, ObjectValue requestObj) {
        String serviceUri = clientObj.get(CLIENT_ENDPOINT_SERVICE_URI).toString();
        HttpCarbonMessage outboundRequestMsg = createOutboundRequestMsg(clientObj, serviceUri, verb, path, requestObj);
        DataContext dataContext = new DataContext(strand, clientObj, requestObj, outboundRequestMsg);
        // Execute the operation
        executeNonBlockingAction(dataContext, false);
    }

    protected static HttpCarbonMessage createOutboundRequestMsg(ObjectValue clientObj, String serviceUri, String httpVerb,
                                                         String path, ObjectValue requestObj) {

        HttpCarbonMessage outboundRequestMsg = HttpUtil
                .getCarbonMsg(requestObj, HttpUtil.createHttpCarbonMessage(true));

        HttpUtil.checkEntityAvailability(requestObj);
        HttpUtil.enrichOutboundMessage(outboundRequestMsg, requestObj);
        prepareOutboundRequest(serviceUri, path, outboundRequestMsg, isNoEntityBodyRequest(requestObj));

        // If the verb is not specified, use the verb in incoming message
        if (httpVerb == null || httpVerb.isEmpty()) {
            httpVerb = (String) outboundRequestMsg.getProperty(HttpConstants.HTTP_METHOD);
        }
        outboundRequestMsg.setProperty(HttpConstants.HTTP_METHOD, httpVerb.trim().toUpperCase(Locale.getDefault()));
        handleAcceptEncodingHeader(outboundRequestMsg, getCompressionConfigFromEndpointConfig(clientObj));

        return outboundRequestMsg;
    }
}
