/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.HeaderUtil;
import org.ballerinalang.mime.util.MultipartDataSource;
import org.ballerinalang.model.InterruptibleNativeCallableUnit;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.CompressionConfigState;
import org.ballerinalang.net.http.DataContext;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.observability.ObservabilityConstants;
import org.ballerinalang.util.observability.ObservabilityUtils;
import org.ballerinalang.util.observability.ObserverContext;
import org.ballerinalang.util.transactions.LocalTransactionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.ClientConnectorException;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.EndpointTimeOutException;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpClientConnectorListener;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;
import org.wso2.transport.http.netty.message.PooledDataStreamerFactory;
import org.wso2.transport.http.netty.message.ResponseHandle;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import static io.netty.handler.codec.http.HttpHeaderNames.ACCEPT_ENCODING;
import static org.ballerinalang.net.http.HttpConstants.ANN_CONFIG_ATTR_COMPRESSION;
import static org.ballerinalang.net.http.HttpConstants.CLIENT_ENDPOINT_CONFIG;
import static org.ballerinalang.net.http.HttpConstants.CLIENT_ENDPOINT_SERVICE_URI;
import static org.ballerinalang.net.http.HttpConstants.HTTP_PACKAGE_PATH;
import static org.ballerinalang.net.http.HttpConstants.HTTP_STATUS_CODE;
import static org.ballerinalang.net.http.HttpConstants.REQUEST;
import static org.ballerinalang.net.http.HttpUtil.extractEntity;
import static org.ballerinalang.net.http.HttpUtil.getCompressionState;
import static org.ballerinalang.runtime.Constants.BALLERINA_VERSION;
import static org.wso2.transport.http.netty.contract.Constants.ENCODING_DEFLATE;
import static org.wso2.transport.http.netty.contract.Constants.ENCODING_GZIP;

/**
 * {@code AbstractHTTPAction} is the base class for all HTTP Connector Actions.
 */
public abstract class AbstractHTTPAction implements InterruptibleNativeCallableUnit {

    private static final Logger logger = LoggerFactory.getLogger(AbstractHTTPAction.class);

    private static final String CACHE_BALLERINA_VERSION;

    static {
        CACHE_BALLERINA_VERSION = System.getProperty(BALLERINA_VERSION);
    }

    @Override
    public boolean persistBeforeOperation() {
        return false;
    }

    @Override
    public boolean persistAfterOperation() {
        return false;
    }


    protected HttpCarbonMessage createOutboundRequestMsg(Context context) {

        // Extract Argument values
        BMap<String, BValue> bConnector = (BMap<String, BValue>) context.getRefArgument(0);
        String path = context.getStringArgument(0);

        BMap<String, BValue> requestStruct = ((BMap<String, BValue>) context.getNullableRefArgument(1));
        if (requestStruct == null) {
            requestStruct = BLangConnectorSPIUtil.createBStruct(context, HTTP_PACKAGE_PATH, REQUEST);
        }

        HttpCarbonMessage requestMsg = HttpUtil
                .getCarbonMsg(requestStruct, HttpUtil.createHttpCarbonMessage(true));

        HttpUtil.checkEntityAvailability(context, requestStruct);
        HttpUtil.enrichOutboundMessage(requestMsg, requestStruct);
        prepareOutboundRequest(context, bConnector, path, requestMsg);
        handleAcceptEncodingHeader(requestMsg, getCompressionConfigFromEndpointConfig(bConnector));
        return requestMsg;
    }

    String getCompressionConfigFromEndpointConfig(BMap<String, BValue> httpClientStruct) {
        Struct clientEndpointConfig = BLangConnectorSPIUtil.toStruct(httpClientStruct);
        Struct epConfig = (Struct) clientEndpointConfig.getNativeData(CLIENT_ENDPOINT_CONFIG);
        return epConfig.getRefField(ANN_CONFIG_ATTR_COMPRESSION).getStringValue();
    }

    void handleAcceptEncodingHeader(HttpCarbonMessage outboundRequest, String compressionConfigValue) {
        CompressionConfigState compressionState = getCompressionState(compressionConfigValue);

        if (compressionState == CompressionConfigState.ALWAYS && (outboundRequest.getHeader(
                ACCEPT_ENCODING.toString()) == null)) {
            outboundRequest.setHeader(ACCEPT_ENCODING.toString(), ENCODING_DEFLATE + ", " + ENCODING_GZIP);
        } else if (compressionState == CompressionConfigState.NEVER && (outboundRequest.getHeader(
                ACCEPT_ENCODING.toString()) != null)) {
            outboundRequest.removeHeader(ACCEPT_ENCODING.toString());
        }
    }

    protected void prepareOutboundRequest(Context context, BMap<String, BValue> connector, String path,
                                          HttpCarbonMessage outboundRequest) {
        validateParams(connector);
        if (context.isInTransaction()) {
            LocalTransactionInfo localTransactionInfo = context.getLocalTransactionInfo();
            outboundRequest.setHeader(HttpConstants.HEADER_X_XID, localTransactionInfo.getGlobalTransactionId());
            outboundRequest.setHeader(HttpConstants.HEADER_X_REGISTER_AT_URL, localTransactionInfo.getURL());
        }
        try {
            String uri = connector.get(CLIENT_ENDPOINT_SERVICE_URI).stringValue() + path;
            URL url = new URL(uri);

            int port = getOutboundReqPort(url);
            String host = url.getHost();

            setOutboundReqProperties(outboundRequest, url, port, host);
            setOutboundReqHeaders(outboundRequest, port, host);

        } catch (MalformedURLException e) {
            throw new BallerinaException("Malformed url specified. " + e.getMessage());
        } catch (Exception e) {
            throw new BallerinaException("Failed to prepare request. " + e.getMessage());
        }
    }

    private void setOutboundReqHeaders(HttpCarbonMessage outboundRequest, int port, String host) {
        HttpHeaders headers = outboundRequest.getHeaders();
        setHostHeader(host, port, headers);
        setOutboundUserAgent(headers);
        removeConnectionHeader(headers);
    }

    private void setOutboundReqProperties(HttpCarbonMessage outboundRequest, URL url, int port, String host) {
        outboundRequest.setProperty(Constants.HTTP_HOST, host);
        outboundRequest.setProperty(Constants.HTTP_PORT, port);

        String outboundReqPath = getOutboundReqPath(url);
        outboundRequest.setProperty(HttpConstants.TO, outboundReqPath);

        outboundRequest.setProperty(HttpConstants.PROTOCOL, url.getProtocol());
    }

    private void setHostHeader(String host, int port, HttpHeaders headers) {
        if (port == 80 || port == 443) {
            headers.set(HttpHeaderNames.HOST, host);
        } else {
            headers.set(HttpHeaderNames.HOST, host + ":" + port);
        }
    }

    private void removeConnectionHeader(HttpHeaders headers) {
        // Remove existing Connection header
        if (headers.contains(HttpHeaderNames.CONNECTION)) {
            headers.remove(HttpHeaderNames.CONNECTION);
        }
    }

    private void setOutboundUserAgent(HttpHeaders headers) {
        String userAgent;
        if (CACHE_BALLERINA_VERSION != null) {
            userAgent = "ballerina/" + CACHE_BALLERINA_VERSION;
        } else {
            userAgent = "ballerina";
        }

        if (!headers.contains(HttpHeaderNames.USER_AGENT)) { // If User-Agent is not already set from program
            headers.set(HttpHeaderNames.USER_AGENT, userAgent);
        }
    }

    private String getOutboundReqPath(URL url) {
        String toPath = url.getPath();
        String query = url.getQuery();
        if (query != null) {
            toPath = toPath + "?" + query;
        }
        return toPath;
    }

    private int getOutboundReqPort(URL url) {
        int port = 80;
        if (url.getPort() != -1) {
            port = url.getPort();
        } else if (url.getProtocol().equalsIgnoreCase(HttpConstants.PROTOCOL_HTTPS)) {
            port = 443;
        }
        return port;
    }

    private void validateParams(BMap<String, BValue> connector) {
        if (connector == null || connector.get(CLIENT_ENDPOINT_SERVICE_URI) == null) {
            throw new BallerinaException("Connector parameters not defined correctly.");
        }
    }

    protected void executeNonBlockingAction(DataContext dataContext, boolean async) {
        HttpCarbonMessage outboundRequestMsg = dataContext.getOutboundRequest();

        //Make the request associate with this response consumable again so that it can be reused.
        checkDirtiness(dataContext, outboundRequestMsg);

        Object sourceHandler = outboundRequestMsg.getProperty(HttpConstants.SRC_HANDLER);
        if (sourceHandler == null) {
            outboundRequestMsg.setProperty(HttpConstants.SRC_HANDLER,
                    dataContext.context.getProperty(HttpConstants.SRC_HANDLER));
        }
        Object poolableByteBufferFactory = outboundRequestMsg.getProperty(HttpConstants.POOLED_BYTE_BUFFER_FACTORY);
        if (poolableByteBufferFactory == null) {
            outboundRequestMsg.setProperty(HttpConstants.POOLED_BYTE_BUFFER_FACTORY,
                    dataContext.context.getProperty(HttpConstants.POOLED_BYTE_BUFFER_FACTORY));
        }
        Object remoteAddress = outboundRequestMsg.getProperty(HttpConstants.REMOTE_ADDRESS);
        if (remoteAddress == null) {
            outboundRequestMsg.setProperty(HttpConstants.REMOTE_ADDRESS,
                    dataContext.context.getProperty(HttpConstants.REMOTE_ADDRESS));
        }
        outboundRequestMsg.setProperty(HttpConstants.ORIGIN_HOST,
                dataContext.context.getProperty(HttpConstants.ORIGIN_HOST));
        sendOutboundRequest(dataContext, outboundRequestMsg, async);
    }

    private void checkDirtiness(DataContext dataContext, HttpCarbonMessage outboundRequestMsg) {
        BMap<String, BValue> requestStruct = ((BMap<String, BValue>) dataContext.context.
                getNullableRefArgument(HttpConstants.REQUEST_STRUCT_INDEX));
        String contentType = HttpUtil.getContentTypeFromTransportMessage(outboundRequestMsg);
        outboundRequestMsg.setIoException(null);
        if (requestStruct != null) {
            if (dirty(requestStruct)) {
                cleanOutboundReq(outboundRequestMsg, requestStruct, contentType);
            } else {
                requestStruct.put(HttpConstants.REQUEST_REUSE_STATUS_INDEX, new BBoolean(HttpConstants.DIRTY_REQUEST));
            }
        }
    }

    private void cleanOutboundReq(HttpCarbonMessage outboundRequestMsg, BMap<String, BValue> requestStruct,
                                  String contentType) {
        BMap<String, BValue> entityStruct = extractEntity(requestStruct);
        if (entityStruct != null) {
            BValue messageDataSource = EntityBodyHandler.getMessageDataSource(entityStruct);
            if (messageDataSource == null && EntityBodyHandler.getByteChannel(entityStruct) == null
                    && !HeaderUtil.isMultipart(contentType)) {
                outboundRequestMsg.addHttpContent(new DefaultLastHttpContent());
            } else {
                outboundRequestMsg.waitAndReleaseAllEntities();
            }
        } else {
            outboundRequestMsg.addHttpContent(new DefaultLastHttpContent());
        }
    }

    private boolean dirty(BMap<String, BValue> requestStruct) {
        BValue isDirty = requestStruct.get(HttpConstants.REQUEST_REUSE_STATUS_INDEX);
        return ((BBoolean) isDirty).booleanValue() == HttpConstants.DIRTY_REQUEST;
    }

    private void sendOutboundRequest(DataContext dataContext, HttpCarbonMessage outboundRequestMsg, boolean async) {
        try {
            send(dataContext, outboundRequestMsg, async);
        } catch (BallerinaConnectorException e) {
            dataContext.notifyInboundResponseStatus(null, HttpUtil.getError(dataContext.context, e));
        } catch (Exception e) {
            BallerinaException exception = new BallerinaException("Failed to send outboundRequestMsg to the backend",
                    e, dataContext.context);
            dataContext.notifyInboundResponseStatus(null, HttpUtil.getError(dataContext.context, exception));
        }
    }

    /**
     * Send outbound request through the client connector. If the Content-Type is multipart, check whether the boundary
     * exist. If not get a new boundary string and add it as a parameter to Content-Type, just before sending header
     * info through wire. If a boundary string exist at this point, serialize multipart entity body, else serialize
     * entity body which can either be a message data source or a byte channel.
     *
     * @param dataContext        holds the ballerina context and callback
     * @param outboundRequestMsg Outbound request that needs to be sent across the wire
     * @param async              whether a handle should be return
     */
    private void send(DataContext dataContext, HttpCarbonMessage outboundRequestMsg, boolean async) {
        BMap<String, BValue> bConnector = (BMap<String, BValue>) dataContext.context.getRefArgument(0);
        Struct httpClient = BLangConnectorSPIUtil.toStruct(bConnector);
        HttpClientConnector clientConnector = (HttpClientConnector)
                httpClient.getNativeData(HttpConstants.CALLER_ACTIONS);
        String contentType = HttpUtil.getContentTypeFromTransportMessage(outboundRequestMsg);
        String boundaryString = null;

        if (HeaderUtil.isMultipart(contentType)) {
            boundaryString = HttpUtil.addBoundaryIfNotExist(outboundRequestMsg, contentType);
        }

        HttpUtil.checkAndObserveHttpRequest(dataContext.context, outboundRequestMsg);

        final HTTPClientConnectorListener httpClientConnectorLister = ObservabilityUtils.isObservabilityEnabled() ?
                new ObservableHttpClientConnectorListener(dataContext) :
                new HTTPClientConnectorListener(dataContext);
        final HttpMessageDataStreamer outboundMsgDataStreamer = getHttpMessageDataStreamer(outboundRequestMsg);
        final OutputStream messageOutputStream = outboundMsgDataStreamer.getOutputStream();
        BMap<String, BValue> requestStruct = ((BMap<String, BValue>) dataContext.context.
                getNullableRefArgument(HttpConstants.REQUEST_STRUCT_INDEX));
        BMap<String, BValue> entityStruct = null;
        if (requestStruct != null) {
            entityStruct = extractEntity(requestStruct);
            if (entityStruct == null) {
                //This is reached when it is a passthrough scenario(the body has not been built) or when the
                // entity body is empty/null. It is not possible to differentiate the two scenarios in Ballerina,
                // hence the value for passthrough is set to be true for both cases because transport side will
                // interpret this value only when there is an unbuilt body in carbon message.
                outboundRequestMsg.setPassthrough(true);
            }
        }

        HttpResponseFuture future = clientConnector.send(outboundRequestMsg);
        if (async) {
            future.setResponseHandleListener(httpClientConnectorLister);
        } else {
            future.setHttpConnectorListener(httpClientConnectorLister);
        }
        try {
            if (entityStruct != null) {
                if (boundaryString != null) {
                    serializeMultiparts(entityStruct, messageOutputStream, boundaryString);
                } else {
                    serializeDataSource(entityStruct, messageOutputStream);
                }
            }
        } catch (IOException | EncoderException serializerException) {
            // We don't have to do anything here as the client connector will notify the error though the listener
            logger.warn("couldn't serialize the message", serializerException);
        } catch (RuntimeException exception) {
            if (exception.getMessage() != null &&
                    exception.getMessage().contains(Constants.INBOUND_RESPONSE_ALREADY_RECEIVED)) {
                logger.warn("Response already received before completing the outbound request", exception);
            } else {
                throw exception;
            }
        }
    }

    private HttpMessageDataStreamer getHttpMessageDataStreamer(HttpCarbonMessage outboundRequestMsg) {
        final HttpMessageDataStreamer outboundMsgDataStreamer;
        final PooledDataStreamerFactory pooledDataStreamerFactory = (PooledDataStreamerFactory)
                outboundRequestMsg.getProperty(HttpConstants.POOLED_BYTE_BUFFER_FACTORY);
        if (pooledDataStreamerFactory != null) {
            outboundMsgDataStreamer = pooledDataStreamerFactory.createHttpDataStreamer(outboundRequestMsg);
        } else {
            outboundMsgDataStreamer = new HttpMessageDataStreamer(outboundRequestMsg);
        }
        return outboundMsgDataStreamer;
    }

    /**
     * Serialize multipart entity body. If an array of body parts exist, encode body parts else serialize body content
     * if it exist as a byte channel.
     *
     * @param entityStruct        Represents the entity that holds the actual body
     * @param boundaryString      Boundary string that should be used in encoding body parts
     * @param messageOutputStream Output stream to which the payload is written
     */
    private void serializeMultiparts(BMap<String, BValue> entityStruct, OutputStream messageOutputStream,
                                     String boundaryString) throws IOException {
        BRefValueArray bodyParts = EntityBodyHandler.getBodyPartArray(entityStruct);
        if (bodyParts != null && bodyParts.size() > 0) {
            serializeMultipartDataSource(messageOutputStream, boundaryString, entityStruct);
        } else { //If the content is in a byte channel
            serializeDataSource(entityStruct, messageOutputStream);
        }
    }

    /**
     * Encode body parts with the given boundary and send it across the wire.
     *
     * @param boundaryString      Boundary string of multipart entity
     * @param entityStruct        Represent ballerina entity struct
     * @param messageOutputStream Output stream to which the payload is written
     */
    private void serializeMultipartDataSource(OutputStream messageOutputStream,
                                              String boundaryString, BMap<String, BValue> entityStruct) {
        MultipartDataSource multipartDataSource = new MultipartDataSource(entityStruct, boundaryString);
        multipartDataSource.serialize(messageOutputStream);
        HttpUtil.closeMessageOutputStream(messageOutputStream);
    }

    private void serializeDataSource(BMap<String, BValue> entityStruct, OutputStream messageOutputStream)
            throws IOException {
        BValue messageDataSource = EntityBodyHandler.getMessageDataSource(entityStruct);
        if (messageDataSource != null) {
            HttpUtil.serializeDataSource(messageDataSource, entityStruct, messageOutputStream);
            HttpUtil.closeMessageOutputStream(messageOutputStream);
        } else if (EntityBodyHandler.getByteChannel(entityStruct) != null) {
            //When the entity body is a byte channel and when it is not null
            EntityBodyHandler.writeByteChannelToOutputStream(entityStruct, messageOutputStream);
            HttpUtil.closeMessageOutputStream(messageOutputStream);
        }
    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    private class HTTPClientConnectorListener implements HttpClientConnectorListener {

        private DataContext dataContext;

        private HTTPClientConnectorListener(DataContext dataContext) {
            this.dataContext = dataContext;
        }

        @Override
        public void onMessage(HttpCarbonMessage inboundResponseMessage) {
            this.dataContext.notifyInboundResponseStatus
                    (HttpUtil.createResponseStruct(this.dataContext.context, inboundResponseMessage), null);
        }

        @Override
        public void onResponseHandle(ResponseHandle responseHandle) {
            BMap<String, BValue> httpFuture = BLangConnectorSPIUtil.createBStruct(this.dataContext.context,
                    HttpConstants.PROTOCOL_PACKAGE_HTTP,
                    HttpConstants.HTTP_FUTURE);
            httpFuture.addNativeData(HttpConstants.TRANSPORT_HANDLE, responseHandle);
            this.dataContext.notifyInboundResponseStatus(httpFuture, null);
        }

        @Override
        public void onError(Throwable throwable) {
            BError httpConnectorError;
            if (throwable instanceof EndpointTimeOutException) {
                httpConnectorError = HttpUtil.getError(this.dataContext.context, throwable);
            } else if (throwable instanceof IOException) {
                this.dataContext.getOutboundRequest().setIoException((IOException) throwable);
                httpConnectorError = HttpUtil.getError(this.dataContext.context, throwable);
            } else {
                this.dataContext.getOutboundRequest()
                        .setIoException(new IOException(throwable.getMessage(), throwable));
                httpConnectorError = HttpUtil.getError(this.dataContext.context, throwable);
            }
            this.dataContext.notifyInboundResponseStatus(null, httpConnectorError);
        }
    }

    /**
     * Observe {@link HTTPClientConnectorListener} and add HTTP status code as a tag to {@link ObserverContext}.
     */
    private class ObservableHttpClientConnectorListener extends HTTPClientConnectorListener {

        private final Context context;

        private ObservableHttpClientConnectorListener(DataContext dataContext) {
            super(dataContext);
            this.context = dataContext.context;
        }

        @Override
        public void onMessage(HttpCarbonMessage httpCarbonMessage) {
            super.onMessage(httpCarbonMessage);
            Integer statusCode = (Integer) httpCarbonMessage.getProperty(HTTP_STATUS_CODE);
            addHttpStatusCode(statusCode != null ? statusCode : 0);
        }

        @Override
        public void onError(Throwable throwable) {
            super.onError(throwable);
            if (throwable instanceof ClientConnectorException) {
                ClientConnectorException clientConnectorException = (ClientConnectorException) throwable;
                addHttpStatusCode(clientConnectorException.getHttpStatusCode());
            }
        }

        private void addHttpStatusCode(int statusCode) {
            Optional<ObserverContext> observerContext = ObservabilityUtils.getParentContext(context);
            observerContext.ifPresent(ctx -> ctx.addTag(ObservabilityConstants.TAG_KEY_HTTP_STATUS_CODE,
                    String.valueOf(statusCode)));
        }
    }
}
