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
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.EntityBodyReader;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.io.channels.FileIOChannel;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.runtime.message.MessageDataSource;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;

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
        BStruct entityStruct = MimeUtil.extractEntity(outboundResponseStruct);
        HttpResponseFuture outboundRespStatusFuture = HttpUtil.sendOutboundResponse(requestMessage, responseMessage);
        if (entityStruct != null) {
            MessageDataSource outboundMessageSource = EntityBodyHandler.readMessageDataSource(entityStruct);
                serializeMsgDataSource(responseMessage, outboundMessageSource, outboundRespStatusFuture, entityStruct);
        }
        return handleResponseStatus(context, outboundRespStatusFuture);
    }

  /*  private void writeToOutputStreamFromFile(Context context, HTTPCarbonMessage responseMessage, BStruct entityStruct,
                                             HttpResponseFuture outboundRespStatusFuture) {
        String overFlowFilePath = EntityBodyHandler.getOverFlowFileLocation(entityStruct);
        HttpMessageDataStreamer outboundMsgDataStreamer = new HttpMessageDataStreamer(responseMessage);
        HttpConnectorListener outboundResStatusConnectorListener =
                new HttpResponseConnectorListener(outboundMsgDataStreamer);
        outboundRespStatusFuture.setHttpConnectorListener(outboundResStatusConnectorListener);
        OutputStream messageOutputStream = outboundMsgDataStreamer.getOutputStream();
        try {
            Files.copy(Paths.get(overFlowFilePath), messageOutputStream);
            HttpUtil.closeMessageOutputStream(messageOutputStream);
        } catch (IOException e) {
            throw new BallerinaException("Failed to send outbound response payload is in overflow" +
                    " file location", e, context);
        }
    }*/

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
                                        HttpResponseFuture outboundResponseStatusFuture, BStruct entityStruct) {
        HttpMessageDataStreamer outboundMsgDataStreamer = new HttpMessageDataStreamer(responseMessage);
        HttpConnectorListener outboundResStatusConnectorListener =
                new HttpResponseConnectorListener(outboundMsgDataStreamer);
        outboundResponseStatusFuture.setHttpConnectorListener(outboundResStatusConnectorListener);
        OutputStream messageOutputStream = outboundMsgDataStreamer.getOutputStream();
        try {
            if (outboundMessageSource != null) {
                outboundMessageSource.serializeData(messageOutputStream);
                HttpUtil.closeMessageOutputStream(messageOutputStream);
            } else {
                EntityBodyReader entityBodyReader = MimeUtil.constructEntityBodyReader(entityStruct);
                byte[] buffer = new byte[1024];
                if (entityBodyReader.isStream()) {
                    InputStream inputStream = Channels.newInputStream(entityBodyReader.getEntityBodyChannel());
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        messageOutputStream.write(buffer, 0, len);
                    }

                } else {
                    FileIOChannel fileIOChannel = entityBodyReader.getFileIOChannel();
                    InputStream inputStream = new ByteArrayInputStream(fileIOChannel.readAll());
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        messageOutputStream.write(buffer, 0, len);
                    }
                }
                HttpUtil.closeMessageOutputStream(messageOutputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
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
