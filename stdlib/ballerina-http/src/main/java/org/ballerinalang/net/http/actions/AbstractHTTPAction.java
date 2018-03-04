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

package org.ballerinalang.net.http.actions;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.AbstractNativeAction;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.mime.util.HeaderUtil;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.mime.util.MultipartDataSource;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.actions.ClientConnectorFuture;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.http.RetryConfig;
import org.ballerinalang.runtime.message.MessageDataSource;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.transactions.LocalTransactionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.common.Constants;
import org.wso2.transport.http.netty.contract.ClientConnectorException;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import static org.ballerinalang.mime.util.Constants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.Constants.MESSAGE_ENTITY;
import static org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_MIME;
import static org.ballerinalang.runtime.Constants.BALLERINA_VERSION;
import static org.wso2.transport.http.netty.common.Constants.ENCODING_DEFLATE;
import static org.wso2.transport.http.netty.common.Constants.ENCODING_GZIP;

/**
 * {@code AbstractHTTPAction} is the base class for all HTTP Connector Actions.
 */
public abstract class AbstractHTTPAction extends AbstractNativeAction {

    private static final Logger logger = LoggerFactory.getLogger(AbstractHTTPAction.class);

    private static final String CACHE_BALLERINA_VERSION;

    static {
        CACHE_BALLERINA_VERSION = System.getProperty(BALLERINA_VERSION);
    }

    protected HTTPCarbonMessage createOutboundRequestMsg(Context context) {

        // Extract Argument values
        BConnector bConnector = (BConnector) getRefArgument(context, 0);
        String path = getStringArgument(context, 0);
        BStruct requestStruct = ((BStruct) getRefArgument(context, 1));
        HTTPCarbonMessage requestMsg = HttpUtil
                .getCarbonMsg(requestStruct, HttpUtil.createHttpCarbonMessage(true));

        HttpUtil.checkEntityAvailability(context, requestStruct);
        HttpUtil.enrichOutboundMessage(requestMsg, requestStruct);
        prepareOutboundRequest(context, bConnector, path, requestMsg);
        if (requestMsg.getHeader(HttpHeaderNames.ACCEPT_ENCODING.toString()) == null) {
            requestMsg.setHeader(HttpHeaderNames.ACCEPT_ENCODING.toString(), ENCODING_DEFLATE + ", " + ENCODING_GZIP);
        }
        return requestMsg;
    }

    protected void prepareOutboundRequest(Context context, BConnector connector, String path,
                                          HTTPCarbonMessage outboundRequest) {
        validateParams(connector);
        if (context.isInTransaction()) {
            LocalTransactionInfo localTransactionInfo = context.getLocalTransactionInfo();
            outboundRequest.setHeader(HttpConstants.HEADER_X_XID, localTransactionInfo.getGlobalTransactionId());
            outboundRequest.setHeader(HttpConstants.HEADER_X_REGISTER_AT_URL, localTransactionInfo.getURL());
        }
        try {
            String uri = connector.getStringField(0) + path;
            URL url = new URL(uri);

            int port = getOutboundReqPort(url);
            String host = url.getHost();

            setOutboundReqProperties(outboundRequest, url, port, host);
            setOutboundReqHeaders(outboundRequest, port, host);

        } catch (MalformedURLException e) {
            throw new BallerinaException("Malformed url specified. " + e.getMessage());
        } catch (Throwable t) {
            throw new BallerinaException("Failed to prepare request. " + t.getMessage());
        }
    }

    private void setOutboundReqHeaders(HTTPCarbonMessage outboundRequest, int port, String host) {
        HttpHeaders headers = outboundRequest.getHeaders();
        setHostHeader(host, port, headers);
        setOutboundUserAgent(headers);
        removeConnectionHeader(headers);
    }

    private void setOutboundReqProperties(HTTPCarbonMessage outboundRequest, URL url, int port, String host) {
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

    private void validateParams(BConnector connector) {
        if (connector == null || connector.getStringField(0) == null) {
            throw new BallerinaException("Connector parameters not defined correctly.");
        }
    }

    protected ClientConnectorFuture executeNonBlockingAction(Context context, HTTPCarbonMessage outboundRequestMsg)
            throws ClientConnectorException {
        ClientConnectorFuture ballerinaFuture = new ClientConnectorFuture();

        RetryConfig retryConfig = getRetryConfiguration(context);
        HTTPClientConnectorListener httpClientConnectorLister =
                new HTTPClientConnectorListener(context, ballerinaFuture, retryConfig, outboundRequestMsg);

        Object sourceHandler = outboundRequestMsg.getProperty(HttpConstants.SRC_HANDLER);
        if (sourceHandler == null) {
            outboundRequestMsg.setProperty(HttpConstants.SRC_HANDLER, context.getProperty(HttpConstants.SRC_HANDLER));
        }
        Object remoteAddress = outboundRequestMsg.getProperty(HttpConstants.REMOTE_ADDRESS);
        if (remoteAddress == null) {
            outboundRequestMsg.setProperty(HttpConstants.REMOTE_ADDRESS,
                    context.getProperty(HttpConstants.REMOTE_ADDRESS));
        }
        outboundRequestMsg.setProperty(HttpConstants.ORIGIN_HOST, context.getProperty(HttpConstants.ORIGIN_HOST));
        sendOutboundRequest(context, outboundRequestMsg, httpClientConnectorLister);
        return ballerinaFuture;
    }

    private void sendOutboundRequest(Context context, HTTPCarbonMessage outboundRequestMsg,
                                     HTTPClientConnectorListener httpClientConnectorLister) {
        try {
            send(context, outboundRequestMsg, httpClientConnectorLister);
        } catch (BallerinaConnectorException e) {
            throw new BallerinaException(e.getMessage(), e, context);
        } catch (Exception e) {
            throw new BallerinaException("Failed to send outboundRequestMsg to the backend", e, context);
        }
    }

    /**
     * Send outbound request through the client connector. If the Content-Type is multipart, check whether the boundary
     * exist. If not get a new boundary string and add it as a parameter to Content-Type, just before sending header
     * info through wire. If a boundary string exist at this point, serialize multipart entity body, else serialize
     * entity body which can either be a message data source or a byte channel.
     *
     * @param context                   Represent the ballerina context which is the runtime state of the program
     * @param outboundRequestMsg        Outbound request that needs to be sent across the wire
     * @param httpClientConnectorLister Represent http client connector listener
     * @throws Exception When an error occurs while sending the outbound request via client connector
     */
    private void send(Context context, HTTPCarbonMessage outboundRequestMsg,
                      HTTPClientConnectorListener httpClientConnectorLister) throws Exception {
        BConnector bConnector = (BConnector) getRefArgument(context, 0);
        HttpClientConnector clientConnector =
                (HttpClientConnector) bConnector.getnativeData(HttpConstants.CONNECTOR_NAME);
        String contentType = HttpUtil.getContentTypeFromTransportMessage(outboundRequestMsg);
        String boundaryString = null;
        if (HeaderUtil.isMultipart(contentType)) {
            boundaryString = HttpUtil.addBoundaryIfNotExist(outboundRequestMsg, contentType);
        }
        HttpResponseFuture future = clientConnector.send(outboundRequestMsg);
        future.setHttpConnectorListener(httpClientConnectorLister);
        if (boundaryString != null) {
            serializeMultiparts(context, outboundRequestMsg, httpClientConnectorLister, boundaryString);
        } else {
            serializeDataSource(context, outboundRequestMsg, httpClientConnectorLister);
        }
    }

    /**
     * Serlaize multipart entity body. If an array of body parts exist, encode body parts else serialize body content
     * if it exist as a byte channel.
     *
     * @param context                   Represent the ballerina context which is the runtime state of the program
     * @param outboundRequestMsg        Outbound request that needs to be sent across the wire
     * @param httpClientConnectorLister Represent http client connector listener
     * @param boundaryString            Boundary string that should be used in encoding body parts
     */
    private void serializeMultiparts(Context context, HTTPCarbonMessage outboundRequestMsg, HTTPClientConnectorListener
            httpClientConnectorLister, String boundaryString) {
        BStruct entityStruct = getEntityStruct(context);
        if (entityStruct != null) {
            BRefValueArray bodyParts = EntityBodyHandler.getBodyPartArray(entityStruct);
            if (bodyParts != null && bodyParts.size() > 0) {
                serializeMultipartDataSource(outboundRequestMsg, httpClientConnectorLister, boundaryString,
                        entityStruct);
            } else { //If the content is in a byte channel
                serializeDataSource(context, outboundRequestMsg, httpClientConnectorLister);
            }
        }
    }

    private BStruct getEntityStruct(Context context) {
        BStruct requestStruct = ((BStruct) getRefArgument(context, 1));
        return requestStruct.getNativeData(MESSAGE_ENTITY) != null ?
                (BStruct) requestStruct.getNativeData(MESSAGE_ENTITY) : null;
    }

    /**
     * Encode body parts with the given boundary and send it across the wire.
     *
     * @param outboundRequestMsg        Outbound request message
     * @param httpClientConnectorLister Represent http client connector listener
     * @param boundaryString            Boundary string of multipart entity
     * @param entityStruct              Represent ballerina entity struct
     */
    private void serializeMultipartDataSource(HTTPCarbonMessage outboundRequestMsg, HTTPClientConnectorListener
            httpClientConnectorLister, String boundaryString, BStruct entityStruct) {
        MultipartDataSource multipartDataSource = new MultipartDataSource(entityStruct, boundaryString);
        HttpMessageDataStreamer outboundMsgDataStreamer = new HttpMessageDataStreamer(outboundRequestMsg);
        OutputStream messageOutputStream = outboundMsgDataStreamer.getOutputStream();
        httpClientConnectorLister.setOutboundMsgDataStreamer(outboundMsgDataStreamer);
        multipartDataSource.serializeData(messageOutputStream);
        HttpUtil.closeMessageOutputStream(messageOutputStream);
    }

    private void serializeDataSource(Context context, HTTPCarbonMessage outboundReqMsg,
                                     HTTPClientConnectorListener httpClientConnectorListener) {
        BStruct requestStruct = ((BStruct) getRefArgument(context, 1));
        BStruct entityStruct = MimeUtil.extractEntity(requestStruct);
        if (entityStruct != null) {
            MessageDataSource messageDataSource = EntityBodyHandler.getMessageDataSource(entityStruct);
            OutputStream messageOutputStream = getOutputStream(outboundReqMsg, httpClientConnectorListener);
            if (messageDataSource != null) {
                messageDataSource.serializeData(messageOutputStream);
                HttpUtil.closeMessageOutputStream(messageOutputStream);
            } else { //When the entity body is a byte channel
                try {
                    EntityBodyHandler.writeByteChannelToOutputStream(entityStruct, messageOutputStream);
                } catch (IOException e) {
                    throw new BallerinaException("An error occurred while writing byte channel content to outputstream",
                            context);
                } finally {
                    HttpUtil.closeMessageOutputStream(messageOutputStream);
                }
            }
        }
    }

    private static OutputStream getOutputStream(HTTPCarbonMessage outboundReqMsg, HTTPClientConnectorListener
            httpClientConnectorListener) {
        HttpMessageDataStreamer outboundMsgDataStreamer = new HttpMessageDataStreamer(outboundReqMsg);
        OutputStream messageOutputStream = outboundMsgDataStreamer.getOutputStream();
        httpClientConnectorListener.setOutboundMsgDataStreamer(outboundMsgDataStreamer);
        return messageOutputStream;
    }

    private RetryConfig getRetryConfiguration(Context context) {
        BConnector bConnector = (BConnector) getRefArgument(context, 0);
        BStruct options = (BStruct) bConnector.getRefField(HttpConstants.OPTIONS_STRUCT_INDEX);
        if (options == null) {
            return new RetryConfig();
        }

        BStruct retryConfig = (BStruct) options.getRefField(HttpConstants.RETRY_STRUCT_INDEX);
        if (retryConfig == null) {
            return new RetryConfig();
        }
        long retryCount = retryConfig.getIntField(HttpConstants.RETRY_COUNT_INDEX);
        long interval = retryConfig.getIntField(HttpConstants.RETRY_INTERVAL_INDEX);
        return new RetryConfig(retryCount, interval);
    }

    @Override
    public boolean isNonBlockingAction() {
        return true;
    }

    private class HTTPClientConnectorListener implements HttpConnectorListener {

        private Context context;
        private ClientConnectorFuture ballerinaFuture;
        private RetryConfig retryConfig;
        private HTTPCarbonMessage outboundReqMsg;
        private HttpMessageDataStreamer outboundMsgDataStreamer;
        // Reference for post validation.

        private HTTPClientConnectorListener(Context context, ClientConnectorFuture ballerinaFuture,
                                            RetryConfig retryConfig, HTTPCarbonMessage outboundReqMsg) {
            this.context = context;
            this.ballerinaFuture = ballerinaFuture;
            this.retryConfig = retryConfig;
            this.outboundReqMsg = outboundReqMsg;
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
            if (throwable instanceof IOException) {
                this.outboundMsgDataStreamer.setIoException((IOException) throwable);
            }
            if (checkRetryState(throwable)) {
                return;
            }

            sendOutboundRequest(context, outboundReqMsg, this);
        }

        private boolean checkRetryState(Throwable throwable) {
            if (!retryConfig.shouldRetry()) {
                notifyError(throwable);
                return true;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("action invocation failed, retrying action, count - "
                        + retryConfig.getCurrentCount() + " limit - " + retryConfig.getRetryCount());
            }
            retryConfig.incrementCountAndWait();
            return false;
        }

        private void notifyError(Throwable throwable) {
            BStruct httpConnectorError = createStruct(context, HttpConstants.HTTP_CONNECTOR_ERROR, HttpConstants
                    .PROTOCOL_PACKAGE_HTTP);
            httpConnectorError.setStringField(0, throwable.getMessage());
            if (throwable instanceof ClientConnectorException) {
                ClientConnectorException clientConnectorException = (ClientConnectorException) throwable;
                httpConnectorError.setIntField(0, clientConnectorException.getHttpStatusCode());
            }

            ballerinaFuture.notifyReply(null, httpConnectorError);
        }

        private BStruct createStruct(Context context, String structName, String protocolPackage) {
            PackageInfo httpPackageInfo = context.getProgramFile()
                    .getPackageInfo(protocolPackage);
            StructInfo structInfo = httpPackageInfo.getStructInfo(structName);
            BStructType structType = structInfo.getType();
            return new BStruct(structType);
        }

        void setOutboundMsgDataStreamer(HttpMessageDataStreamer outboundMsgDataStreamer) {
            this.outboundMsgDataStreamer = outboundMsgDataStreamer;
        }
    }
}
