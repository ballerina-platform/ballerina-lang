/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http.actions.httpclient;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.DataContext;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.observability.ObservabilityUtils;
import org.ballerinalang.util.observability.ObserverContext;
import org.wso2.transport.http.netty.contract.ClientConnectorException;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.util.Map;


/**
 * {@code Head} is the HEAD action implementation of the HTTP Connector.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "head",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = HttpConstants.HTTP_CLIENT,
                structPackage = "ballerina.http"),
        args = {
                @Argument(name = "client", type = TypeKind.CONNECTOR),
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
public class Head extends AbstractHTTPAction {

    @Override
    public void execute(Context context, CallableUnitCallback callback) {
        DataContext dataContext = new DataContext(context, callback);
        try {
            // Execute the operation
            executeNonBlockingAction(dataContext, createOutboundRequestMsg(context));
        } catch (ClientConnectorException clientConnectorException) {
            BallerinaException exception = new BallerinaException("Failed to invoke 'head' action in " +
                    HttpConstants.HTTP_CLIENT + ". " + clientConnectorException.getMessage(), context);
            dataContext.notifyReply(null, HttpUtil.getHttpConnectorError(context, exception));
        }
    }

    protected HTTPCarbonMessage createOutboundRequestMsg(Context context) {
        HTTPCarbonMessage outboundReqMsg = super.createOutboundRequestMsg(context);
        outboundReqMsg.setProperty(HttpConstants.HTTP_METHOD, HttpConstants.HTTP_METHOD_HEAD);

        ObserverContext observerContext = ObservabilityUtils.getCurrentContext(context);
        Map<String, String> traceContext = ObservabilityUtils.getTraceProperties();
        HttpUtil.injectHeaders(outboundReqMsg, traceContext);
        observerContext.addTags(HttpUtil.extractTags(outboundReqMsg));

        return outboundReqMsg;
    }
}
