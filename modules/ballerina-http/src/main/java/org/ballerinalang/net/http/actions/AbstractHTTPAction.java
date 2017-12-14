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
import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.AbstractNativeAction;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.actions.ClientConnectorFuture;
import org.ballerinalang.net.http.Constants;
import org.ballerinalang.net.http.HttpConnectionManager;
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

import java.net.MalformedURLException;
import java.net.URL;

import static org.ballerinalang.runtime.Constants.BALLERINA_VERSION;

/**
 * {@code AbstractHTTPAction} is the base class for all HTTP Connector Actions.
 */
public abstract class AbstractHTTPAction extends AbstractNativeAction {

    private static final Logger logger = LoggerFactory.getLogger(AbstractHTTPAction.class);

    private static final String BALLERINA_USER_AGENT;

    /* Application level timeout */
    private static final long SENDER_TIMEOUT = 180000; // TODO: Make this configurable with endpoint timeout impl

    static {
        String version = System.getProperty(BALLERINA_VERSION);
        if (version != null) {
            BALLERINA_USER_AGENT = "ballerina/" + version;
        } else {
            BALLERINA_USER_AGENT = "ballerina";
        }
    }

    protected void prepareRequest(BConnector connector, String path, HTTPCarbonMessage cMsg) {

        validateParams(connector);

        String uri = null;
        try {
            uri = connector.getStringField(0) + path;

            URL url = new URL(uri);
            String host = url.getHost();
            int port = 80;
            if (url.getPort() != -1) {
                port = url.getPort();
            } else if (url.getProtocol().equalsIgnoreCase(Constants.PROTOCOL_HTTPS)) {
                port = 443;
            }

            cMsg.setProperty(org.wso2.transport.http.netty.common.Constants.HOST, host);
            cMsg.setProperty(Constants.PORT, port);
            String toPath = url.getPath();
            String query = url.getQuery();
            if (query != null) {
                toPath = toPath + "?" + query;
            }
            cMsg.setProperty(Constants.TO, toPath);

            cMsg.setProperty(Constants.PROTOCOL, url.getProtocol());
            setHostHeader(cMsg, host, port);

            HttpHeaders headers = cMsg.getHeaders();

            // Set User-Agent header
            if (!headers.contains(Constants.USER_AGENT_HEADER)) { // If User-Agent is not already set from program
                cMsg.setHeader(Constants.USER_AGENT_HEADER, BALLERINA_USER_AGENT);
            }

            // Remove existing Connection header
            if (headers.contains(Constants.CONNECTION_HEADER)) {
                cMsg.removeHeader(Constants.CONNECTION_HEADER);
            }
        } catch (MalformedURLException e) {
            throw new BallerinaException("Malformed url specified. " + e.getMessage());
        } catch (Throwable t) {
            throw new BallerinaException("Failed to prepare request. " + t.getMessage());
        }

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

    protected void executeNonBlocking(Context context, HTTPCarbonMessage httpRequestMsg,
                                    HTTPClientConnectorListener httpClientConnectorLister) {
        try {
            BConnector bConnector = (BConnector) getRefArgument(context, 0);
            String scheme = (String) httpRequestMsg.getProperty(Constants.PROTOCOL);
            HttpClientConnector clientConnector =
                    HttpConnectionManager.getInstance().getHTTPHttpClientConnector(scheme, bConnector);
            HttpResponseFuture future = clientConnector.send(httpRequestMsg);
            future.setHttpConnectorListener(httpClientConnectorLister);
            serializeDataSource(context);
        } catch (BallerinaConnectorException e) {
            throw new BallerinaException(e.getMessage(), e, context);
        } catch (Exception e) {
            throw new BallerinaException("Failed to send httpRequestMsg to the backend", e, context);
        }
    }

    private void serializeDataSource(Context context) {
        BStruct requestStruct  = ((BStruct) getRefArgument(context, 1));
        MessageDataSource messageDataSource = HttpUtil.getMessageDataSource(requestStruct);
        if (messageDataSource != null) {
            messageDataSource.serializeData();
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
            if (httpCarbonMessage.getMessagingException() == null) {
                BStruct response = createResponseStruct(this.context);
                response.addNativeData("transport_message", httpCarbonMessage);
                ballerinaFuture.notifyReply(response);
            } else {
                //TODO should we throw or should we create error struct and pass? or do we need this at all?
                BallerinaConnectorException ex = new BallerinaConnectorException(httpCarbonMessage
                        .getMessagingException().getMessage(), httpCarbonMessage.getMessagingException());
                logger.error("non-blocking action invocation validation failed. ", ex);
                ballerinaFuture.notifyFailure(ex);
            }
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
            BStruct httpConnectorError = createErrorStruct(context);
            httpConnectorError.setStringField(0, throwable.getMessage());
            if (throwable instanceof ClientConnectorException) {
                ClientConnectorException clientConnectorException = (ClientConnectorException) throwable;
                httpConnectorError.setIntField(0, clientConnectorException.getHttpStatusCode());
            }

            ballerinaFuture.notifyReply(null, httpConnectorError);
        }

        private BStruct createResponseStruct(Context context) {
            PackageInfo sessionPackageInfo = context.getProgramFile()
                    .getPackageInfo(Constants.PROTOCOL_PACKAGE_HTTP);
            StructInfo sessionStructInfo = sessionPackageInfo.getStructInfo(Constants.RESPONSE);
            BStructType structType = sessionStructInfo.getType();
            BStruct bStruct = new BStruct(structType);

            return bStruct;
        }

        private BStruct createErrorStruct(Context context) {
            PackageInfo sessionPackageInfo = context.getProgramFile()
                    .getPackageInfo(Constants.PROTOCOL_PACKAGE_HTTP);
            StructInfo sessionStructInfo = sessionPackageInfo.getStructInfo(Constants.HTTP_CONNECTOR_ERROR);
            BStructType structType = sessionStructInfo.getType();
            return new BStruct(structType);
        }
    }

    protected HTTPCarbonMessage createCarbonMsg(Context context) {

        // Extract Argument values
        BConnector bConnector = (BConnector) getRefArgument(context, 0);
        String path = HttpUtil.sanitizeUri(getStringArgument(context, 0));
        BStruct requestStruct  = ((BStruct) getRefArgument(context, 1));
        //TODO check below line
        HTTPCarbonMessage requestMsg = HttpUtil
                .getCarbonMsg(requestStruct, HttpUtil.createHttpCarbonMessage(true));
        prepareRequest(bConnector, path, requestMsg);
        return requestMsg;
    }

    protected void setHostHeader(HTTPCarbonMessage cMsg, String host, int port) {
        if (port == 80 || port == 443) {
            cMsg.getHeaders().set(org.wso2.transport.http.netty.common.Constants.HOST, host);
        } else {
            cMsg.getHeaders().set(org.wso2.transport.http.netty.common.Constants.HOST, host + ":" + port);
        }
    }
}
