/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.net.http;

import io.ballerina.runtime.api.BRuntime;
import io.ballerina.runtime.api.connector.CallableUnitCallback;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.observability.ObservabilityConstants;
import io.ballerina.runtime.observability.ObserveUtils;
import io.ballerina.runtime.observability.ObserverContext;
import io.ballerina.runtime.runtime.RuntimeConstants;
import io.ballerina.runtime.util.exceptions.BallerinaConnectorException;
import io.ballerina.runtime.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.util.HashMap;
import java.util.Map;

import static io.ballerina.runtime.observability.ObservabilityConstants.PROPERTY_TRACE_PROPERTIES;
import static io.ballerina.runtime.observability.ObservabilityConstants.SERVER_CONNECTOR_HTTP;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_HTTP_METHOD;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_HTTP_URL;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_PROTOCOL;
import static org.ballerinalang.net.http.HttpConstants.ON_MESSAGE_METADATA;

/**
 * HTTP connector listener for Ballerina.
 */
public class BallerinaHTTPConnectorListener implements HttpConnectorListener {

    private static final Logger log = LoggerFactory.getLogger(BallerinaHTTPConnectorListener.class);
    protected static final String HTTP_RESOURCE = "httpResource";

    private final HTTPServicesRegistry httpServicesRegistry;

    protected final BMap endpointConfig;

    public BallerinaHTTPConnectorListener(HTTPServicesRegistry httpServicesRegistry, BMap endpointConfig) {
        this.httpServicesRegistry = httpServicesRegistry;
        this.endpointConfig = endpointConfig;
    }

    @Override
    public void onMessage(HttpCarbonMessage inboundMessage) {
        try {
            HttpResource httpResource;
            if (accessed(inboundMessage)) {
                httpResource = (HttpResource) inboundMessage.getProperty(HTTP_RESOURCE);
                extractPropertiesAndStartResourceExecution(inboundMessage, httpResource);
                return;
            }
            httpResource = HttpDispatcher.findResource(httpServicesRegistry, inboundMessage);
            if (HttpDispatcher.shouldDiffer(httpResource)) {
                inboundMessage.setProperty(HTTP_RESOURCE, httpResource);
                //Removes inbound content listener since data binding waits for all contents to be received
                //before executing its logic.
                inboundMessage.removeInboundContentListener();
                return;
            }
            try {
                if (httpResource != null) {
                    extractPropertiesAndStartResourceExecution(inboundMessage, httpResource);
                }
            } catch (BallerinaException ex) {
                HttpUtil.handleFailure(inboundMessage, new BallerinaConnectorException(ex.getMessage(), ex.getCause()));
            }
        } catch (Exception ex) {
            HttpUtil.handleFailure(inboundMessage, new BallerinaConnectorException(ex.getMessage(), ex.getCause()));
        }
    }

    @Override
    public void onError(Throwable throwable) {
        log.warn("Error in HTTP server connector: {}", throwable.getMessage());
    }

    protected void extractPropertiesAndStartResourceExecution(HttpCarbonMessage inboundMessage,
                                                              HttpResource httpResource) {
        boolean isTransactionInfectable = httpResource.isTransactionInfectable();
        boolean isInterruptible = httpResource.isInterruptible();
        Map<String, Object> properties = collectRequestProperties(inboundMessage, isTransactionInfectable,
                isInterruptible, httpResource.isTransactionAnnotated());
        Object[] signatureParams = HttpDispatcher.getSignatureParameters(httpResource, inboundMessage, endpointConfig);

        if (ObserveUtils.isObservabilityEnabled()) {
            ObserverContext observerContext = new ObserverContext();
            observerContext.setObjectName(SERVER_CONNECTOR_HTTP);
            Map<String, String> httpHeaders = new HashMap<>();
            inboundMessage.getHeaders().forEach(entry -> httpHeaders.put(entry.getKey(), entry.getValue()));
            observerContext.addProperty(PROPERTY_TRACE_PROPERTIES, httpHeaders);
            observerContext.addMainTag(TAG_KEY_HTTP_METHOD, inboundMessage.getHttpMethod());
            observerContext.addMainTag(TAG_KEY_PROTOCOL, (String) inboundMessage.getProperty(HttpConstants.PROTOCOL));
            observerContext.addMainTag(TAG_KEY_HTTP_URL, inboundMessage.getRequestUrl());
            properties.put(ObservabilityConstants.KEY_OBSERVER_CONTEXT, observerContext);
        }
        CallableUnitCallback callback = new HttpCallableUnitCallback(inboundMessage);
        BObject service = httpResource.getParentService().getBalService();
        BRuntime runtime = httpServicesRegistry.getRuntime();
        runtime.invokeMethodAsync(service, httpResource.getName(), null, ON_MESSAGE_METADATA,
                                  callback, properties, signatureParams);
    }

    protected boolean accessed(HttpCarbonMessage inboundMessage) {
        return inboundMessage.getProperty(HTTP_RESOURCE) != null;
    }

    private Map<String, Object> collectRequestProperties(HttpCarbonMessage inboundMessage, boolean isInfectable,
                                                         boolean isInterruptible, boolean isTransactionAnnotated) {
        Map<String, Object> properties = new HashMap<>();
        if (inboundMessage.getProperty(HttpConstants.SRC_HANDLER) != null) {
            Object srcHandler = inboundMessage.getProperty(HttpConstants.SRC_HANDLER);
            properties.put(HttpConstants.SRC_HANDLER, srcHandler);
        }
        String txnId = inboundMessage.getHeader(HttpConstants.HEADER_X_XID);
        String registerAtUrl = inboundMessage.getHeader(HttpConstants.HEADER_X_REGISTER_AT_URL);
        //Return 500 if txn context is received when transactionInfectable=false
        if (!isInfectable && txnId != null) {
            log.error("Infection attempt on resource with transactionInfectable=false, txnId:" + txnId);
            throw new BallerinaConnectorException("Cannot create transaction context: " +
                                                          "resource is not transactionInfectable");
        }
        if (isTransactionAnnotated && isInfectable && txnId != null && registerAtUrl != null) {
            properties.put(RuntimeConstants.GLOBAL_TRANSACTION_ID, txnId);
            properties.put(RuntimeConstants.TRANSACTION_URL, registerAtUrl);
            return properties;
        }
        properties.put(HttpConstants.REMOTE_ADDRESS, inboundMessage.getProperty(HttpConstants.REMOTE_ADDRESS));
        properties.put(HttpConstants.ORIGIN_HOST, inboundMessage.getHeader(HttpConstants.ORIGIN_HOST));
        properties.put(HttpConstants.POOLED_BYTE_BUFFER_FACTORY,
                       inboundMessage.getHeader(HttpConstants.POOLED_BYTE_BUFFER_FACTORY));
        properties.put(RuntimeConstants.IS_INTERRUPTIBLE, isInterruptible);
        return properties;
    }
}
