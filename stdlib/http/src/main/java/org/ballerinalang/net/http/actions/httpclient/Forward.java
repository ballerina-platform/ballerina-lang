/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.DataContext;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.util.Locale;


/**
 * {@code Forward} action can be used to invoke an http call with incoming request httpVerb.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "forward",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = HttpConstants.CALLER_ACTIONS,
                structPackage = "ballerina/http"),
        args = {
                @Argument(name = "client", type = TypeKind.OBJECT),
                @Argument(name = "path", type = TypeKind.STRING),
                @Argument(name = "req", type = TypeKind.OBJECT, structType = "Request",
                        structPackage = "ballerina/http")
        },
        returnType = {
                @ReturnType(type = TypeKind.OBJECT, structType = "Response", structPackage = "ballerina/http"),
                @ReturnType(type = TypeKind.RECORD, structType = "HttpConnectorError",
                        structPackage = "ballerina/http"),
        }
)
public class Forward extends AbstractHTTPAction {

    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        DataContext dataContext = new DataContext(context, callback, createOutboundRequestMsg(context));
        // Execute the operation
        executeNonBlockingAction(dataContext, false);
    }

    @Override
    protected HTTPCarbonMessage createOutboundRequestMsg(Context context) {
        BMap<String, BValue> bConnector = (BMap<String, BValue>) context.getRefArgument(0);
        String path = context.getStringArgument(0);
        BMap<String, BValue> requestStruct = ((BMap<String, BValue>) context.getRefArgument(1));

        if (requestStruct.getNativeData(HttpConstants.REQUEST) == null &&
                !HttpUtil.isEntityDataSourceAvailable(requestStruct)) {
            throw new BallerinaException("invalid inbound request parameter");
        }
        HTTPCarbonMessage outboundRequestMsg = HttpUtil
                .getCarbonMsg(requestStruct, HttpUtil.createHttpCarbonMessage(true));

        if (HttpUtil.isEntityDataSourceAvailable(requestStruct)) {
            HttpUtil.enrichOutboundMessage(outboundRequestMsg, requestStruct);
            prepareOutboundRequest(context, bConnector, path, outboundRequestMsg);
            outboundRequestMsg.setProperty(HttpConstants.HTTP_METHOD,
                    BLangConnectorSPIUtil.toStruct(requestStruct).getStringField(HttpConstants.HTTP_REQUEST_METHOD));
        } else {
            prepareOutboundRequest(context, bConnector, path, outboundRequestMsg);
            String httpVerb = (String) outboundRequestMsg.getProperty(HttpConstants.HTTP_METHOD);
            outboundRequestMsg.setProperty(HttpConstants.HTTP_METHOD, httpVerb.trim().toUpperCase(Locale.getDefault()));
        }
        return outboundRequestMsg;
    }
}
