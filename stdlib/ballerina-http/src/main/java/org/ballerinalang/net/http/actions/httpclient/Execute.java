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

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.DataContext;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.transport.http.netty.contract.ClientConnectorException;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.util.Locale;

import static org.wso2.transport.http.netty.common.Constants.ENCODING_DEFLATE;
import static org.wso2.transport.http.netty.common.Constants.ENCODING_GZIP;

/**
 * {@code Execute} action can be used to invoke execute a http call with any httpVerb.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "execute",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = HttpConstants.CALLER_ACTIONS,
                structPackage = "ballerina.http"),
        args = {
                @Argument(name = "client", type = TypeKind.STRUCT),
                @Argument(name = "httpVerb", type = TypeKind.STRING),
                @Argument(name = "path", type = TypeKind.STRING),
                @Argument(name = "req", type = TypeKind.STRUCT, structType = "Request",
                        structPackage = "ballerina.http")
        },
        returnType = {
                @ReturnType(type = TypeKind.STRUCT, structType = "Response", structPackage = "ballerina.http"),
                @ReturnType(type = TypeKind.STRUCT, structType = "HttpConnectorError",
                        structPackage = "ballerina.http"),
        }
)
public class Execute extends AbstractHTTPAction {

    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        DataContext dataContext = new DataContext(context, callback, createOutboundRequestMsg(context));
        try {
            // Execute the operation
            executeNonBlockingAction(dataContext);
        } catch (ClientConnectorException clientConnectorException) {
            BallerinaException exception = new BallerinaException("Failed to invoke 'execute' action in " +
                    HttpConstants.CALLER_ACTIONS + ". " + clientConnectorException.getMessage(), context);
            dataContext.notifyReply(null, HttpUtil.getHttpConnectorError(context, exception));
        }
    }

    protected HTTPCarbonMessage createOutboundRequestMsg(Context context) {
        // Extract Argument values
        BStruct bConnector = (BStruct) context.getRefArgument(0);
        String httpVerb = context.getStringArgument(0);
        String path = context.getStringArgument(1);
        BStruct requestStruct = ((BStruct) context.getRefArgument(1));
        //TODO check below line
        HTTPCarbonMessage defaultCarbonMsg = HttpUtil.createHttpCarbonMessage(true);
        HTTPCarbonMessage outboundRequestMsg = HttpUtil.getCarbonMsg(requestStruct, defaultCarbonMsg);
        prepareOutboundRequest(context, bConnector, path, outboundRequestMsg);

        // If the verb is not specified, use the verb in incoming message
        if (httpVerb == null || "".equals(httpVerb)) {
            httpVerb = (String) outboundRequestMsg.getProperty(HttpConstants.HTTP_METHOD);
        }
        outboundRequestMsg.setProperty(HttpConstants.HTTP_METHOD, httpVerb.trim().toUpperCase(Locale.getDefault()));
        if (outboundRequestMsg.getHeader(HttpHeaderNames.ACCEPT_ENCODING.toString()) == null) {
            outboundRequestMsg.setHeader(HttpHeaderNames.ACCEPT_ENCODING.toString(),
                    ENCODING_DEFLATE + ", " + ENCODING_GZIP);
        }

        return outboundRequestMsg;
    }
}
