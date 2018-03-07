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

package org.ballerinalang.net.http.actions;

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.ConnectorFuture;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.actions.ClientConnectorFuture;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.ResponseHandle;

import static org.ballerinalang.mime.util.Constants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_MIME;

/**
 * {@code GetResponse} action can be used to fetch the response message for a previous asynchronous invocation.
 */
@BallerinaAction(
        packageName = "ballerina.net.http",
        actionName = "getResponse",
        connectorName = HttpConstants.CONNECTOR_NAME,
        args = {
                @Argument(name = "c", type = TypeKind.CONNECTOR),
                @Argument(name = "handle", type = TypeKind.STRUCT, structType = "HttpHandle",
                        structPackage = "ballerina.net.http")
        },
        returnType = {
                @ReturnType(type = TypeKind.STRUCT, structType = "InResponse", structPackage = "ballerina.net.http"),
                @ReturnType(type = TypeKind.STRUCT, structType = "HttpConnectorError",
                        structPackage = "ballerina.net.http"),
        },
        connectorArgs = {
                @Argument(name = "serviceUri", type = TypeKind.STRING),
                @Argument(name = "options", type = TypeKind.STRUCT, structType = "Options",
                        structPackage = "ballerina.net.http")
        }
)
public class GetResponse extends AbstractHTTPAction {

    private static final Logger logger = LoggerFactory.getLogger(GetResponse.class);

    @Override
    public ConnectorFuture execute(Context context) {

        if (logger.isDebugEnabled()) {
            logger.debug("Executing Native Action : {}", this.getName());
        }
        BStruct handleStruct = ((BStruct) getRefArgument(context, 1));

        ResponseHandle responseHandle = (ResponseHandle) handleStruct.getNativeData(HttpConstants.TRANSPORT_HANDLE);
        if (responseHandle == null) {
            throw new BallerinaException("invalid handle");
        }
        ClientConnectorFuture ballerinaFuture = new ClientConnectorFuture();
        BConnector bConnector = (BConnector) getRefArgument(context, 0);
        HttpClientConnector clientConnector =
                (HttpClientConnector) bConnector.getnativeData(HttpConstants.CONNECTOR_NAME);
        clientConnector.getResponse(responseHandle).
                setHttpConnectorListener(new ResponseListener(ballerinaFuture, context));
        return ballerinaFuture;
    }

    private BStruct createStruct(Context context, String structName, String protocolPackage) {
        PackageInfo httpPackageInfo = context.getProgramFile()
                .getPackageInfo(protocolPackage);
        StructInfo structInfo = httpPackageInfo.getStructInfo(structName);
        BStructType structType = structInfo.getType();
        return new BStruct(structType);
    }

    private class ResponseListener implements HttpConnectorListener {

        ClientConnectorFuture ballerinaFuture;
        Context context;

        public ResponseListener(ClientConnectorFuture ballerinaFuture, Context context) {
            this.ballerinaFuture = ballerinaFuture;
            this.context = context;
        }

        @Override
        public void onMessage(HTTPCarbonMessage httpCarbonMessage) {
            BStruct inboundResponse = createStruct(this.context, HttpConstants.IN_RESPONSE,
                                                   HttpConstants.PROTOCOL_PACKAGE_HTTP);
            BStruct entity = createStruct(this.context, HttpConstants.ENTITY, PROTOCOL_PACKAGE_MIME);
            BStruct mediaType = createStruct(this.context, MEDIA_TYPE, PROTOCOL_PACKAGE_MIME);
            HttpUtil.populateInboundResponse(inboundResponse, entity, mediaType, httpCarbonMessage);
            ballerinaFuture.notifyReply(inboundResponse);
        }

        @Override
        public void onError(Throwable throwable) {

        }

    }

}
