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
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.HeaderUtil;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.mime.util.MultipartDataSource;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.runtime.message.MessageDataSource;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;
import org.wso2.transport.http.netty.message.PooledDataStreamerFactory;

import java.io.IOException;
import java.io.OutputStream;

/**
 * {@code {@link ConnectionAction}} represents a Abstract implementation of Native Ballerina Connection Function.
 *
 * @since 0.96
 */
public abstract class ConnectionAction extends BlockingNativeCallableUnit {

    protected BValue[] sendOutboundResponseRobust(Context context, HTTPCarbonMessage requestMessage,
                                                BStruct outboundResponseStruct, HTTPCarbonMessage responseMessage) {
        String contentType = HttpUtil.getContentTypeFromTransportMessage(responseMessage);
        String boundaryString = null;
        if (HeaderUtil.isMultipart(contentType)) {
            boundaryString = HttpUtil.addBoundaryIfNotExist(responseMessage, contentType);
        }
        HttpResponseFuture outboundRespStatusFuture = HttpUtil.sendOutboundResponse(requestMessage, responseMessage);
        BStruct entityStruct = MimeUtil.extractEntity(outboundResponseStruct);
        if (entityStruct != null) {
            if (boundaryString != null) {
                serializeMultiparts(responseMessage, boundaryString, outboundRespStatusFuture, entityStruct);
            } else {
                MessageDataSource outboundMessageSource = EntityBodyHandler.getMessageDataSource(entityStruct);
                serializeMsgDataSource(responseMessage, outboundMessageSource, outboundRespStatusFuture, entityStruct);
            }
        }
        return handleResponseStatus(context, outboundRespStatusFuture);
    }

    /**
     * Serialize multipart entity body. If an array of body parts exist, encode body parts else serialize body content
     * if it exist as a byte channel.
     *
     * @param responseMessage          Response message that needs to be sent out.
     * @param boundaryString           Boundary string that should be used in encoding body parts
     * @param outboundRespStatusFuture Represent the future events and results of connectors
     * @param entityStruct             Represent the entity that holds the actual body
     */
    private void serializeMultiparts(HTTPCarbonMessage responseMessage, String boundaryString,
                                     HttpResponseFuture outboundRespStatusFuture, BStruct entityStruct) {
        BRefValueArray bodyParts = EntityBodyHandler.getBodyPartArray(entityStruct);
        if (bodyParts != null && bodyParts.size() > 0) {
            MultipartDataSource multipartDataSource = new MultipartDataSource(entityStruct, boundaryString);
            serializeMsgDataSource(responseMessage, multipartDataSource, outboundRespStatusFuture,
                    entityStruct);
        } else {
            OutputStream messageOutputStream = getOutputStream(responseMessage, outboundRespStatusFuture);
            try {
                EntityBodyHandler.writeByteChannelToOutputStream(entityStruct, messageOutputStream);
            } catch (IOException e) {
                throw new BallerinaException("Error occurred while serializing byte channel content : " +
                        e.getMessage());
            } finally {
                HttpUtil.closeMessageOutputStream(messageOutputStream);
            }
        }
    }

    protected BValue[] handleResponseStatus(Context context, HttpResponseFuture outboundResponseStatusFuture) {
        try {
            outboundResponseStatusFuture = outboundResponseStatusFuture.sync();
        } catch (InterruptedException e) {
            throw new BallerinaException("interrupted sync: " + e.getMessage());
        }
        Throwable cause = outboundResponseStatusFuture.getStatus().getCause();
        if (cause != null) {
            outboundResponseStatusFuture.resetStatus();
            return new BValue[]{HttpUtil.getHttpConnectorError(context, cause)};
        }
        return new BValue[0];
    }

    protected void serializeMsgDataSource(HTTPCarbonMessage responseMessage, MessageDataSource outboundMessageSource,
                                        HttpResponseFuture outboundResponseStatusFuture, BStruct entityStruct) {
        OutputStream messageOutputStream = getOutputStream(responseMessage, outboundResponseStatusFuture);
        try {
            if (outboundMessageSource != null) {
                outboundMessageSource.serializeData(messageOutputStream);
            } else { //When the entity body is a byte channel
                EntityBodyHandler.writeByteChannelToOutputStream(entityStruct, messageOutputStream);
            }
        } catch (IOException e) {
            throw new BallerinaException("Error occurred while serializing message data source : " + e.getMessage());
        } finally {
            HttpUtil.closeMessageOutputStream(messageOutputStream);
        }
    }

    private OutputStream getOutputStream(HTTPCarbonMessage outboundResponse, HttpResponseFuture
            outboundResponseStatusFuture) {
        final HttpMessageDataStreamer outboundMsgDataStreamer;
        final PooledDataStreamerFactory pooledDataStreamerFactory = (PooledDataStreamerFactory)
                outboundResponse.getProperty(HttpConstants.POOLED_BYTE_BUFFER_FACTORY);
        if (pooledDataStreamerFactory != null) {
            outboundMsgDataStreamer = pooledDataStreamerFactory.createHttpDataStreamer(outboundResponse);
        } else {
            outboundMsgDataStreamer = new HttpMessageDataStreamer(outboundResponse);
        }
        HttpConnectorListener outboundResStatusConnectorListener =
                new HttpResponseConnectorListener(outboundMsgDataStreamer);
        outboundResponseStatusFuture.setHttpConnectorListener(outboundResStatusConnectorListener);
        return outboundMsgDataStreamer.getOutputStream();
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
