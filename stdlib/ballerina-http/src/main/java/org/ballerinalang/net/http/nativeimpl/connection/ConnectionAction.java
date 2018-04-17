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

import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.HeaderUtil;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.mime.util.MultipartDataSource;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.net.http.DataContext;
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
public abstract class ConnectionAction implements NativeCallableUnit {

    protected void sendOutboundResponseRobust(DataContext dataContext, HTTPCarbonMessage requestMessage,
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
                serializeMultiparts(dataContext, responseMessage, boundaryString, outboundRespStatusFuture,
                                    entityStruct);
            } else {
                MessageDataSource outboundMessageSource = EntityBodyHandler.getMessageDataSource(entityStruct);
                serializeMsgDataSource(dataContext, responseMessage, outboundMessageSource, outboundRespStatusFuture,
                                       entityStruct);
            }
        }
    }

    /**
     * Serialize multipart entity body. If an array of body parts exist, encode body parts else serialize body content
     * if it exist as a byte channel.
     *
     * @param dataContext              Holds the ballerina context and callback
     * @param responseMessage          Response message that needs to be sent out.
     * @param boundaryString           Boundary string that should be used in encoding body parts
     * @param outboundRespStatusFuture Represent the future events and results of connectors
     * @param entityStruct             Represent the entity that holds the actual body
     */
    private void serializeMultiparts(DataContext dataContext, HTTPCarbonMessage responseMessage, String boundaryString,
                                     HttpResponseFuture outboundRespStatusFuture, BStruct entityStruct) {
        BRefValueArray bodyParts = EntityBodyHandler.getBodyPartArray(entityStruct);
        if (bodyParts != null && bodyParts.size() > 0) {
            MultipartDataSource multipartDataSource = new MultipartDataSource(entityStruct, boundaryString);
            serializeMsgDataSource(dataContext, responseMessage, multipartDataSource, outboundRespStatusFuture,
                    entityStruct);
        } else {
            OutputStream messageOutputStream = getOutputStream(dataContext, responseMessage, outboundRespStatusFuture);
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

    protected void setResponseConnectorListener(DataContext dataContext, HttpResponseFuture outResponseStatusFuture) {
        HttpConnectorListener outboundResStatusConnectorListener =
                new HttpResponseConnectorListener(dataContext);
        outResponseStatusFuture.setHttpConnectorListener(outboundResStatusConnectorListener);
    }

    protected void serializeMsgDataSource(DataContext dataContext, HTTPCarbonMessage responseMessage,
                                          MessageDataSource outboundMessageSource,
                                          HttpResponseFuture outboundResponseStatusFuture, BStruct entityStruct) {
        OutputStream messageOutputStream = getOutputStream(dataContext, responseMessage, outboundResponseStatusFuture);
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

    private OutputStream getOutputStream(DataContext dataContext, HTTPCarbonMessage outboundResponse, HttpResponseFuture
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
                new HttpResponseConnectorListener(dataContext, outboundMsgDataStreamer);
        outboundResponseStatusFuture.setHttpConnectorListener(outboundResStatusConnectorListener);
        return outboundMsgDataStreamer.getOutputStream();
    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    static class HttpResponseConnectorListener implements HttpConnectorListener {

        private final DataContext dataContext;
        private HttpMessageDataStreamer outboundMsgDataStreamer;

        HttpResponseConnectorListener(DataContext dataContext) {
            this.dataContext = dataContext;
        }

        HttpResponseConnectorListener(DataContext dataContext, HttpMessageDataStreamer outboundMsgDataStreamer) {
            this.dataContext = dataContext;
            this.outboundMsgDataStreamer = outboundMsgDataStreamer;
        }

        @Override
        public void onMessage(HTTPCarbonMessage httpCarbonMessage) {
            this.dataContext.notifyOutboundResponseStatus(null);
        }

        @Override
        public void onError(Throwable throwable) {
            BStruct httpConnectorError = BLangConnectorSPIUtil.createBStruct(this.dataContext.context,
                                            HttpConstants.PROTOCOL_PACKAGE_HTTP, HttpConstants.HTTP_CONNECTOR_ERROR);
            if (outboundMsgDataStreamer != null) {
                if (throwable instanceof IOException) {
                    this.outboundMsgDataStreamer.setIoException((IOException) throwable);
                } else {
                    this.outboundMsgDataStreamer.setIoException(new IOException(throwable.getMessage()));
                }
            }
            httpConnectorError.setStringField(0, throwable.getMessage());
            this.dataContext.notifyOutboundResponseStatus(httpConnectorError);
        }
    }
}
