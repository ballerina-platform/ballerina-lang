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

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BError;
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
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.ResponseHandle;

/**
 * {@code GetResponse} action can be used to fetch the response message for a previous asynchronous invocation.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "getResponse",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = HttpConstants.CALLER_ACTIONS,
                structPackage = "ballerina/http"),
        args = {
                @Argument(name = "client", type = TypeKind.OBJECT),
                @Argument(name = "httpFuture", type = TypeKind.OBJECT, structType = "HttpFuture",
                        structPackage = "ballerina/http")
        },
        returnType = {
                @ReturnType(type = TypeKind.OBJECT, structType = "InResponse", structPackage = "ballerina/http"),
                @ReturnType(type = TypeKind.RECORD, structType = "HttpConnectorError",
                        structPackage = "ballerina/http"),
        }
)
public class GetResponse extends AbstractHTTPAction {

    @Override
    public void execute(Context context, CallableUnitCallback callback) {

        DataContext dataContext = new DataContext(context, callback, null);
        BMap<String, BValue> handleStruct = ((BMap<String, BValue>) context.getRefArgument(1));

        ResponseHandle responseHandle = (ResponseHandle) handleStruct.getNativeData(HttpConstants.TRANSPORT_HANDLE);
        if (responseHandle == null) {
            throw new BallerinaException("invalid http handle");
        }
        BMap<String, BValue> bConnector = (BMap<String, BValue>) context.getRefArgument(0);
        HttpClientConnector clientConnector =
                (HttpClientConnector) bConnector.getNativeData(HttpConstants.CALLER_ACTIONS);
        clientConnector.getResponse(responseHandle).
                setHttpConnectorListener(new ResponseListener(dataContext));
    }

    private static class ResponseListener implements HttpConnectorListener {

        private DataContext dataContext;

        ResponseListener(DataContext dataContext) {
            this.dataContext = dataContext;
        }

        @Override
        public void onMessage(HttpCarbonMessage httpCarbonMessage) {
            dataContext.notifyInboundResponseStatus(
                    HttpUtil.createResponseStruct(this.dataContext.context, httpCarbonMessage), null);
        }

        public void onError(Throwable throwable) {
            BError httpConnectorError = HttpUtil.getError(dataContext.context, throwable);
            dataContext.notifyInboundResponseStatus(null, httpConnectorError);
        }
    }
}
