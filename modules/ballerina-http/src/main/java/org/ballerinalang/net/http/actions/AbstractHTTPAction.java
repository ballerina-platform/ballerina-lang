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

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;
import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.AbstractNativeAction;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.actions.ClientConnectorFuture;
import org.ballerinalang.net.http.Constants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.http.RetryConfig;
import org.ballerinalang.runtime.message.MessageDataSource;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.ClientConnectorException;
import org.wso2.transport.http.netty.contract.HttpClientConnector;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import static org.ballerinalang.mime.util.Constants.CONTENT_TYPE;
import static org.ballerinalang.mime.util.Constants.HEADER_VALUE_STRUCT;
import static org.ballerinalang.mime.util.Constants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.Constants.MULTIPART_ENCODER;
import static org.ballerinalang.mime.util.Constants.MULTIPART_FORM_DATA;
import static org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_MIME;
import static org.ballerinalang.runtime.Constants.BALLERINA_VERSION;

/**
 * {@code AbstractHTTPAction} is the base class for all HTTP Connector Actions.
 */
public abstract class AbstractHTTPAction extends AbstractNativeAction {

    private static final Logger logger = LoggerFactory.getLogger(AbstractHTTPAction.class);

    private static final String CACHE_BALLERINA_VERSION;
    static {
        CACHE_BALLERINA_VERSION = System.getProperty(BALLERINA_VERSION);
    }

    protected HTTPCarbonMessage createCarbonMsg(Context context) {

        // Extract Argument values
        BConnector bConnector = (BConnector) getRefArgument(context, 0);
        String path = getStringArgument(context, 0);
        BStruct requestStruct  = ((BStruct) getRefArgument(context, 1));
        HTTPCarbonMessage requestMsg = HttpUtil
                .getCarbonMsg(requestStruct, HttpUtil.createHttpCarbonMessage(true));

        HttpUtil.checkEntityAvailability(context, requestStruct);
        HttpUtil.enrichOutboundMessage(requestMsg, requestStruct);
        prepareRequest(bConnector, path, requestMsg, requestStruct);
        try {
            String contentType = requestMsg.getHeader(CONTENT_TYPE);
            if (contentType != null) {
                if (MULTIPART_FORM_DATA.equals(new MimeType(contentType).getBaseType())) {
                    HttpUtil.prepareRequestWithMultiparts(requestMsg, requestStruct);
                }
            }
        } catch (MimeTypeParseException e) {
            logger.error("Error occurred while parsing Content-Type header in createCarbonMsg", e.getMessage());
        }
        return requestMsg;
    }

    protected void prepareRequest(BConnector connector, String path, HTTPCarbonMessage outboundRequest,
            BStruct requestStruct) {

        validateParams(connector);
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
        outboundRequest.setProperty(org.wso2.transport.http.netty.common.Constants.HOST, host);
        outboundRequest.setProperty(Constants.PORT, port);

        String outboundReqPath = getOutboundReqPath(url);
        outboundRequest.setProperty(Constants.TO, outboundReqPath);

        outboundRequest.setProperty(Constants.PROTOCOL, url.getProtocol());
    }

    private void setHostHeader(String host, int port, HttpHeaders headers) {
        if (port == 80 || port == 443) {
            headers.set(org.wso2.transport.http.netty.common.Constants.HOST, host);
        } else {
            headers.set(org.wso2.transport.http.netty.common.Constants.HOST, host + ":" + port);
        }
    }

    private void removeConnectionHeader(HttpHeaders headers) {
        // Remove existing Connection header
        if (headers.contains(Constants.CONNECTION_HEADER)) {
            headers.remove(Constants.CONNECTION_HEADER);
        }
    }

    private void setOutboundUserAgent(HttpHeaders headers) {
        String userAgent;
        if (CACHE_BALLERINA_VERSION != null) {
            userAgent = "ballerina/" + CACHE_BALLERINA_VERSION;
        } else {
            userAgent = "ballerina";
        }

        if (!headers.contains(Constants.USER_AGENT_HEADER)) { // If User-Agent is not already set from program
            headers.set(Constants.USER_AGENT_HEADER, userAgent);
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
        } else if (url.getProtocol().equalsIgnoreCase(Constants.PROTOCOL_HTTPS)) {
            port = 443;
        }
        return port;
    }

    private boolean validateParams(BConnector connector) {
        //TODO removed empty string check for URL - fix this properly once the all connectors usages updated
        //TODO remove empty URLs
        if (connector != null && connector.getStringField(0) != null) {
            return true;
        } else {
            throw new BallerinaException("Connector parameters not defined correctly.");
        }
    }

    protected ClientConnectorFuture executeNonBlockingAction(Context context, HTTPCarbonMessage httpRequestMsg)
            throws ClientConnectorException {
        ClientConnectorFuture ballerinaFuture = new ClientConnectorFuture();

        RetryConfig retryConfig = getRetryConfiguration(context);
        HTTPClientConnectorListener httpClientConnectorLister =
                new HTTPClientConnectorListener(context, ballerinaFuture, retryConfig, httpRequestMsg);

        Object sourceHandler = httpRequestMsg.getProperty(Constants.SRC_HANDLER);
        if (sourceHandler == null) {
            httpRequestMsg.setProperty(Constants.SRC_HANDLER,
                    context.getProperty(Constants.SRC_HANDLER));
        }
        executeNonBlocking(context, httpRequestMsg, httpClientConnectorLister);
        return ballerinaFuture;
    }

    private void executeNonBlocking(Context context, HTTPCarbonMessage httpRequestMsg,
                                    HTTPClientConnectorListener httpClientConnectorLister) {
        try {
            BConnector bConnector = (BConnector) getRefArgument(context, 0);
            HttpClientConnector clientConnector =
                    (HttpClientConnector) bConnector.getnativeData(Constants.CONNECTOR_NAME);
            HttpResponseFuture future = clientConnector.send(httpRequestMsg);
            future.setHttpConnectorListener(httpClientConnectorLister);
            if (httpRequestMsg.getHeader(CONTENT_TYPE) != null &&
                    MULTIPART_FORM_DATA.equals(new MimeType(httpRequestMsg.getHeader(CONTENT_TYPE)).getBaseType())) {
                BStruct requestStruct = ((BStruct) getRefArgument(context, 1));
                HttpUtil.addMultipartsToCarbonMessage(httpRequestMsg,
                        (HttpPostRequestEncoder) requestStruct.getNativeData(MULTIPART_ENCODER));
            } else {
                serializeDataSource(context, httpRequestMsg);
            }
        } catch (BallerinaConnectorException e) {
            throw new BallerinaException(e.getMessage(), e, context);
        } catch (Exception e) {
            throw new BallerinaException("Failed to send httpRequestMsg to the backend", e, context);
        }
    }

    private void serializeDataSource(Context context, HTTPCarbonMessage httpRequestMsg) {
        BStruct requestStruct = ((BStruct) getRefArgument(context, 1));
        MessageDataSource messageDataSource = HttpUtil.readMessageDataSource(requestStruct);
        if (messageDataSource != null) {
            OutputStream messageOutputStream = new HttpMessageDataStreamer(httpRequestMsg).getOutputStream();
            messageDataSource.serializeData(messageOutputStream);
            HttpUtil.closeMessageOutputStream(messageOutputStream);
        }
    }

    private RetryConfig getRetryConfiguration(Context context) {
        BConnector bConnector = (BConnector) getRefArgument(context, 0);
        BStruct options = (BStruct) bConnector.getRefField(Constants.OPTIONS_STRUCT_INDEX);
        if (options == null) {
            return new RetryConfig();
        }

        BStruct retryConfig = (BStruct) options.getRefField(Constants.RETRY_STRUCT_INDEX);
        if (retryConfig == null) {
            return new RetryConfig();
        }
        long retryCount = retryConfig.getIntField(Constants.RETRY_COUNT_INDEX);
        long interval = retryConfig.getIntField(Constants.RETRY_INTERVAL_INDEX);
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
        private HTTPCarbonMessage httpRequestMsg;
        // Reference for post validation.

        private HTTPClientConnectorListener(Context context, ClientConnectorFuture ballerinaFuture,
                                            RetryConfig retryConfig, HTTPCarbonMessage httpRequestMsg) {
            this.context = context;
            this.ballerinaFuture = ballerinaFuture;
            this.retryConfig = retryConfig;
            this.httpRequestMsg = httpRequestMsg;
        }

        @Override
        public void onMessage(HTTPCarbonMessage httpCarbonMessage) {
            BStruct inboundResponse = createStruct(this.context, Constants.IN_RESPONSE,
                    Constants.PROTOCOL_PACKAGE_HTTP);
            BStruct entity = createStruct(this.context, Constants.ENTITY, PROTOCOL_PACKAGE_MIME);
            BStruct mediaType = createStruct(this.context, MEDIA_TYPE, PROTOCOL_PACKAGE_MIME);
            HttpUtil.setHeaderValueStructType(createStruct(this.context, HEADER_VALUE_STRUCT, PROTOCOL_PACKAGE_MIME));
            HttpUtil.populateInboundResponse(inboundResponse, entity, mediaType, httpCarbonMessage);
            ballerinaFuture.notifyReply(inboundResponse);
        }

        @Override
        public void onError(Throwable throwable) {
            if (!retryConfig.shouldRetry()) {
                notifyError(throwable);
                return;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("action invocation failed, retrying action, count - "
                        + retryConfig.getCurrentCount() + " limit - " + retryConfig.getRetryCount());
            }
            retryConfig.incrementCountAndWait();
            executeNonBlocking(context, httpRequestMsg, this);
        }

        private void notifyError(Throwable throwable) {
            BStruct httpConnectorError = createStruct(context, Constants.HTTP_CONNECTOR_ERROR,  Constants
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
    }
}
