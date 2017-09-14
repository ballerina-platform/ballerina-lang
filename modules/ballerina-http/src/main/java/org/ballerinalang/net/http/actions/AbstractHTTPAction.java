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

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.connectors.AbstractNativeAction;
import org.ballerinalang.net.http.Constants;
import org.ballerinalang.net.http.HttpConnectionManager;
import org.ballerinalang.runtime.threadpool.ResponseWorkerThread;
import org.ballerinalang.runtime.threadpool.ThreadPoolFactory;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.Headers;
import org.wso2.carbon.messaging.exceptions.ClientConnectorException;
import org.wso2.carbon.transport.http.netty.contract.HttpClientConnector;
import org.wso2.carbon.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.carbon.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;

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

        // Handle operations for empty content messages initiated from the Ballerina core itself
        if (cMsg.isEmpty() && cMsg.getMessageDataSource() == null) {
            cMsg.setEndOfMsgAdded(true);
        }

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

            cMsg.setProperty(Constants.HOST, host);
            cMsg.setProperty(Constants.PORT, port);
            String toPath = url.getPath();
            String query = url.getQuery();
            if (query != null) {
                toPath = toPath + "?" + query;
            }
            cMsg.setProperty(Constants.TO, toPath);

            cMsg.setProperty(Constants.PROTOCOL, url.getProtocol());
            if (port != 80) {
                cMsg.getHeaders().set(Constants.HOST, host + ":" + port);
            } else {
                cMsg.getHeaders().set(Constants.HOST, host);
            }

            //Set User-Agent Header
            Object headerObj = cMsg.getProperty(org.ballerinalang.runtime.Constants.INTERMEDIATE_HEADERS);

            if (headerObj == null) {
                headerObj = new Headers();
                cMsg.setProperty(org.ballerinalang.runtime.Constants.INTERMEDIATE_HEADERS, headerObj);
            }
            Headers headers = (Headers) headerObj;

            if (!headers.contains(Constants.USER_AGENT_HEADER)) { // If User-Agent is not already set from program
                cMsg.setHeader(Constants.USER_AGENT_HEADER, BALLERINA_USER_AGENT);
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

    protected BValue executeAction(Context context, HTTPCarbonMessage message) {

        try {
            HTTPClientConnectorLister httpClientConnectorLister = new HTTPClientConnectorLister(context);
            Object sourceHandler = message.getProperty(Constants.SRC_HANDLER);
            if (sourceHandler == null) {
                message.setProperty(Constants.SRC_HANDLER, context.getProperty(Constants.SRC_HANDLER));
            }

            HttpClientConnector clientConnector =
                    HttpConnectionManager.getInstance().getHTTPHttpClientConnector();
            HttpResponseFuture future = clientConnector.send(message);
            future.setHttpConnectorListener(new HTTPClientConnectorLister(context));

            // Wait till Response comes
            long startTime = System.currentTimeMillis();
            while (!httpClientConnectorLister.isResponseArrived()) {
                synchronized (context) {
                    if (!httpClientConnectorLister.isResponseArrived()) {
                        logger.debug("Waiting for a response");
                        context.wait(SENDER_TIMEOUT);
                        if (System.currentTimeMillis() >= (startTime + SENDER_TIMEOUT)) {
                            throw new RuntimeException("response was not received within sender timeout of " +
                                                       SENDER_TIMEOUT / 1000 + " seconds");
                        }
                    }
                }
            }
            handleTransportException(httpClientConnectorLister.getValueRef());
            return httpClientConnectorLister.getValueRef();
        } catch (InterruptedException ignore) {
        } catch (Throwable e) {
            throw new BallerinaException(e.getMessage(), context);
        }
        return null;
    }

    void executeNonBlockingAction(Context context, HTTPCarbonMessage httpRequestMsg)
            throws ClientConnectorException {
        HTTPClientConnectorLister httpClientConnectorLister = new HTTPClientConnectorLister(context);
        httpClientConnectorLister.setNonBlockingExecution(true);

        try {
            Object sourceHandler = httpRequestMsg.getProperty(Constants.SRC_HANDLER);
            if (sourceHandler == null) {
                httpRequestMsg.setProperty(Constants.SRC_HANDLER,
                        context.getProperty(Constants.SRC_HANDLER));
            }

            HttpClientConnector clientConnector =
                    HttpConnectionManager.getInstance().getHTTPHttpClientConnector();
            HttpResponseFuture future = clientConnector.send(httpRequestMsg);
            future.setHttpConnectorListener(httpClientConnectorLister);
        } catch (Exception e) {
            throw new BallerinaException("Failed to send httpRequestMsg to the backend", e, context);
        }
    }

    @Override
    public boolean isNonBlockingAction() {
        return true;
    }

    private void handleTransportException(BValue valueRef) {
        if (valueRef instanceof BMessage) {
            BMessage bMsg = (BMessage) valueRef;
            if (bMsg.value() == null) {
                String msg = "Received unknown message for the action invocation";
                throw new BallerinaException(msg);
            }
            if (bMsg.value().getMessagingException() != null) {
                String msg = bMsg.value().getMessagingException().getMessage();
                throw new BallerinaException(msg);
            }
        } else {
            String msg = "Invalid message received for the action invocation";
            throw new BallerinaException(msg);
        }
    }

    private static class HTTPClientConnectorLister implements HttpConnectorListener {

        private Context context;
        private boolean responseArrived = false;
        private BValue valueRef;
        private boolean nonBlockingExecution;
        // Reference for post validation.

        private HTTPClientConnectorLister(Context context) {
            this.context = context;
        }

        @Override
        public void onMessage(HTTPCarbonMessage httpCarbonMessage) {
            if (httpCarbonMessage.getMessagingException() == null) {
                BStruct response = createResponseStruct(this.context);
                response.addNativeData("transport_message", httpCarbonMessage);
                valueRef = response;

                context.getControlStackNew().currentFrame.returnValues[0] = valueRef;
                responseArrived = true;

                // Release Thread.
                if (nonBlockingExecution) {
                    ThreadPoolFactory.getInstance().getExecutor()
                            .execute(new ResponseWorkerThread(context));
                } else {
                    synchronized (context) {
                        context.notifyAll();
                    }
                }
            } else {
                Exception exception = httpCarbonMessage.getMessagingException();
                logger.error("non-blocking action invocation validation failed. ", exception);
                BStruct err = BLangVMErrors.createError(context, context.getStartIP() - 1,
                        exception.getMessage());
                context.setError(err);
            }
        }

        @Override
        public void onError(Throwable throwable) {
            throw new BallerinaException(throwable.getMessage());
        }

        private BStruct createResponseStruct(Context context) {
            PackageInfo sessionPackageInfo = context.getProgramFile()
                    .getPackageInfo(Constants.PROTOCOL_PACKAGE_HTTP);
            StructInfo sessionStructInfo = sessionPackageInfo.getStructInfo(Constants.RESPONSE);
            BStructType structType = sessionStructInfo.getType();
            BStruct bStruct = new BStruct(structType);

            return bStruct;
        }

        public boolean isResponseArrived() {
            return responseArrived;
        }

        public void setNonBlockingExecution(boolean nonBlockingExecution) {
            this.nonBlockingExecution = nonBlockingExecution;
        }

        public BValue getValueRef() {
            return this.valueRef;
        }
    }

    protected HTTPCarbonMessage createCarbonMsg(Context context) {

        // Extract Argument values
        BConnector bConnector = (BConnector) getRefArgument(context, 0);
        String path = getStringArgument(context, 0);
        BStruct requestStruct  = ((BStruct) getRefArgument(context, 1));
        HTTPCarbonMessage cMsg = (HTTPCarbonMessage) requestStruct
                .getNativeData(Constants.TRANSPORT_MESSAGE);
        prepareRequest(bConnector, path, cMsg);
        return cMsg;
    }

}
