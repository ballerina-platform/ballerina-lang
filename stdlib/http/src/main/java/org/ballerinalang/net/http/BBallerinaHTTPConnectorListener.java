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

import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.runtime.Constants;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP connector listener for Ballerina.
 */
public class BBallerinaHTTPConnectorListener implements HttpConnectorListener {

    private static final Logger log = LoggerFactory.getLogger(BallerinaHTTPConnectorListener.class);
    protected static final String HTTP_RESOURCE = "httpResource";

    private final BHTTPServicesRegistry httpServicesRegistry;

    protected final Struct endpointConfig;

    public BBallerinaHTTPConnectorListener(BHTTPServicesRegistry httpServicesRegistry,
                                           Struct endpointConfig) {
        this.httpServicesRegistry = httpServicesRegistry;
        this.endpointConfig = endpointConfig;
    }

    @Override
    public void onMessage(HttpCarbonMessage inboundMessage) {
        try {
            BHttpResource httpResource;
            if (accessed(inboundMessage)) {
                httpResource = (BHttpResource) inboundMessage.getProperty(HTTP_RESOURCE);
                extractPropertiesAndStartResourceExecution(inboundMessage, httpResource);
                return;
            }
            httpResource = BHttpDispatcher.findResource(httpServicesRegistry, inboundMessage);
            if (BHttpDispatcher.shouldDiffer(httpResource)) {
                inboundMessage.setProperty(HTTP_RESOURCE, httpResource);
                //Removes inbound content listener since data binding waits for all contents to be received
                //before executing its logic.  
                inboundMessage.removeInboundContentListener();
                return;
            }
            if (httpResource != null) {
                extractPropertiesAndStartResourceExecution(inboundMessage, httpResource);
            }
        } catch (BallerinaException ex) {
            try {
                BHttpUtil.handleFailure(inboundMessage,
                                        new BallerinaConnectorException(ex.getMessage(), ex.getCause()));
            } catch (Exception e) {
                log.error("Cannot handle error using the error handler for: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("Error in HTTP server connector: {}", throwable.getMessage());
    }

    protected void extractPropertiesAndStartResourceExecution(HttpCarbonMessage inboundMessage,
                                                              BHttpResource httpResource) {
        boolean isTransactionInfectable = httpResource.isTransactionInfectable();
        boolean isInterruptible = httpResource.isInterruptible();
        Map<String, Object> properties = collectRequestProperties(inboundMessage, isTransactionInfectable,
                isInterruptible, httpResource.isTransactionAnnotated());
        BValue[] signatureParams = BHttpDispatcher.getSignatureParameters(httpResource, inboundMessage, endpointConfig);
        Resource balResource = httpResource.getBalResource();

//        ObserverContext observerContext = null;
//        if (ObserveUtils.isObservabilityEnabled()) {
//            observerContext = new ObserverContext();
//            observerContext.setConnectorName(SERVER_CONNECTOR_HTTP);
//            observerContext.setServiceName(ObserveUtils.getFullServiceName(httpResource.getParentService()
//                                                                                       .getBalService()
//                                                                                       .getServiceInfo()));
//            observerContext.setResourceName(balResource.getName());
//
//            Map<String, String> httpHeaders = new HashMap<>();
//            inboundMessage.getHeaders().forEach(entry -> httpHeaders.put(entry.getKey(), entry.getValue()));
//            observerContext.addProperty(PROPERTY_TRACE_PROPERTIES, httpHeaders);
//            observerContext.addTag(TAG_KEY_HTTP_METHOD, inboundMessage.getHttpMethod());
//            observerContext.addTag(TAG_KEY_PROTOCOL, (String) inboundMessage.getProperty(HttpConstants.PROTOCOL));
//            observerContext.addTag(TAG_KEY_HTTP_URL, inboundMessage.getRequestUrl());
//        }
//
//        CallableUnitCallback callback = new BHttpCallableUnitCallback(inboundMessage);
//        //TODO handle BallerinaConnectorException
//        Executor.submit(balResource, callback, properties, observerContext, signatureParams);
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
            properties.put(Constants.GLOBAL_TRANSACTION_ID, txnId);
            properties.put(Constants.TRANSACTION_URL, registerAtUrl);
            return properties;
        }
        properties.put(HttpConstants.REMOTE_ADDRESS, inboundMessage.getProperty(HttpConstants.REMOTE_ADDRESS));
        properties.put(HttpConstants.ORIGIN_HOST, inboundMessage.getHeader(HttpConstants.ORIGIN_HOST));
        properties.put(HttpConstants.POOLED_BYTE_BUFFER_FACTORY,
                       inboundMessage.getHeader(HttpConstants.POOLED_BYTE_BUFFER_FACTORY));
        properties.put(Constants.IS_INTERRUPTIBLE, isInterruptible);
        return properties;
    }

}
