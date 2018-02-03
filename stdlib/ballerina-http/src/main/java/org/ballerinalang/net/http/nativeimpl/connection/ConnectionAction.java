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

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.runtime.message.MessageDataSource;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * {@code {@link ConnectionAction}} represents a Abstract implementation of Native Ballerina Connection Function.
 *
 * @since 0.96
 */
public abstract class ConnectionAction extends AbstractNativeFunction {

    @Override
    public BValue[] execute(Context context) {
        BStruct connectionStruct = (BStruct) getRefArgument(context, 0);
        HTTPCarbonMessage inboundRequestMsg = HttpUtil.getCarbonMsg(connectionStruct, null);
        HttpUtil.checkFunctionValidity(connectionStruct, inboundRequestMsg);

        BStruct outboundResponseStruct = (BStruct) getRefArgument(context, 1);
        HTTPCarbonMessage outboundResponseMsg = HttpUtil
                .getCarbonMsg(outboundResponseStruct, HttpUtil.createHttpCarbonMessage(false));

        HttpUtil.prepareOutboundResponse(context, inboundRequestMsg, outboundResponseMsg, outboundResponseStruct);

        BValue[] outboundResponseStatus = sendOutboundResponseRobust(context, inboundRequestMsg,
                outboundResponseStruct, outboundResponseMsg);
        return outboundResponseStatus;
    }

    private BValue[] sendOutboundResponseRobust(Context context, HTTPCarbonMessage requestMessage,
            BStruct outboundResponseStruct, HTTPCarbonMessage responseMessage) {
        MessageDataSource outboundMessageSource = HttpUtil.readMessageDataSource(outboundResponseStruct);
        HttpResponseFuture outboundRespStatusFuture = HttpUtil.sendOutboundResponse(requestMessage, responseMessage);
        serializeMsgDataSource(responseMessage, outboundMessageSource, outboundRespStatusFuture);

        return handleResponseStatus(context, outboundRespStatusFuture);
    }

    private BValue[] handleResponseStatus(Context context, HttpResponseFuture outboundResponseStatusFuture) {
        try {
            outboundResponseStatusFuture = outboundResponseStatusFuture.sync();
        } catch (InterruptedException e) {
            throw new BallerinaException("interrupted sync: " + e.getMessage());
        }
        if (outboundResponseStatusFuture.getStatus().getCause() != null) {
            return this.getBValues(HttpUtil.getServerConnectorError(context
                    , outboundResponseStatusFuture.getStatus().getCause()));
        }
        return AbstractNativeFunction.VOID_RETURN;
    }

    private void serializeMsgDataSource(HTTPCarbonMessage responseMessage, MessageDataSource outboundMessageSource,
            HttpResponseFuture outboundResponseStatusFuture) {
        if (outboundMessageSource != null) {
            HttpMessageDataStreamer outboundMsgDataStreamer = new HttpMessageDataStreamer(responseMessage);
            HttpConnectorListener outboundResStatusConnectorListener =
                    new HttpResponseConnectorListener(outboundMsgDataStreamer);
            outboundResponseStatusFuture.setHttpConnectorListener(outboundResStatusConnectorListener);
            OutputStream messageOutputStream = outboundMsgDataStreamer.getOutputStream();
            outboundMessageSource.serializeData(messageOutputStream);
            HttpUtil.closeMessageOutputStream(messageOutputStream);
        }
    }

    private static class HttpResponseConnectorListener implements HttpConnectorListener {

        private HttpMessageDataStreamer outboundMsgDataStreamer;

        HttpResponseConnectorListener(HttpMessageDataStreamer outboundMsgDataStreamer) {
            this.outboundMsgDataStreamer = outboundMsgDataStreamer;
        }

        @Override
        public void onMessage(HTTPCarbonMessage httpCarbonMessage) {
        }

        @Override
        public void onError(Throwable throwable) {
            if (throwable instanceof IOException) {
                outboundMsgDataStreamer.setIoException((IOException) throwable);
            }
        }
    }
}
