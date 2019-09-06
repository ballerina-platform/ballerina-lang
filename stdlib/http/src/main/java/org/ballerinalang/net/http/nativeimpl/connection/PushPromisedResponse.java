/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.http.nativeimpl.connection;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.NonBlockingCallback;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.net.http.DataContext;
import org.ballerinalang.net.http.HttpUtil;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.io.OutputStream;

import static org.ballerinalang.net.http.HttpConstants.CALLER;
import static org.ballerinalang.net.http.HttpUtil.extractEntity;

/**
 * {@code PushPromisedResponse} is the extern function to respond back the client with Server Push response.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "pushPromisedResponse",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = CALLER,
                structPackage = "ballerina/http"),
        args = {@Argument(name = "promise", type = TypeKind.OBJECT, structType = "PushPromise",
                structPackage = "ballerina/http"),
                @Argument(name = "res", type = TypeKind.OBJECT, structType = "OutResponse",
                        structPackage = "ballerina/http")},
        returnType = @ReturnType(type = TypeKind.RECORD, structType = "HttpConnectorError",
                structPackage = "ballerina/http"),
        isPublic = true
)
public class PushPromisedResponse extends ConnectionAction {

    public static Object pushPromisedResponse(Strand strand, ObjectValue connectionObj, ObjectValue pushPromiseObj,
                                     ObjectValue outboundResponseObj) {
        HttpCarbonMessage inboundRequestMsg = HttpUtil.getCarbonMsg(connectionObj, null);
        DataContext dataContext = new DataContext(strand, new NonBlockingCallback(strand), inboundRequestMsg);
        HttpUtil.serverConnectionStructCheck(inboundRequestMsg);

        Http2PushPromise http2PushPromise = HttpUtil.getPushPromise(pushPromiseObj, null);
        if (http2PushPromise == null) {
            throw BallerinaErrors.createError("invalid push promise");
        }

        HttpCarbonMessage outboundResponseMsg = HttpUtil
                .getCarbonMsg(outboundResponseObj, HttpUtil.createHttpCarbonMessage(false));
        HttpUtil.prepareOutboundResponse(connectionObj, inboundRequestMsg, outboundResponseMsg, outboundResponseObj);
        pushResponseRobust(dataContext, inboundRequestMsg, outboundResponseObj, outboundResponseMsg,
                http2PushPromise);
        return null;
    }

    private static void pushResponseRobust(DataContext dataContext, HttpCarbonMessage requestMessage,
                                    ObjectValue outboundResponseObj, HttpCarbonMessage responseMessage,
                                    Http2PushPromise http2PushPromise) {
        HttpResponseFuture outboundRespStatusFuture =
                HttpUtil.pushResponse(requestMessage, responseMessage, http2PushPromise);
        HttpMessageDataStreamer outboundMsgDataStreamer = getMessageDataStreamer(responseMessage);
        HttpConnectorListener outboundResStatusConnectorListener =
                new ResponseWriter.HttpResponseConnectorListener(dataContext, outboundMsgDataStreamer);
        outboundRespStatusFuture.setHttpConnectorListener(outboundResStatusConnectorListener);
        OutputStream messageOutputStream = outboundMsgDataStreamer.getOutputStream();

        ObjectValue entityObj = extractEntity(outboundResponseObj);
        if (entityObj != null) {
            Object outboundMessageSource = EntityBodyHandler.getMessageDataSource(entityObj);
            serializeMsgDataSource(outboundMessageSource, entityObj, messageOutputStream);
        }
    }
}
