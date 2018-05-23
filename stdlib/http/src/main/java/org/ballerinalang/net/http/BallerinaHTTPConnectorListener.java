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

import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Value;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BTypeDescValue;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.runtime.Constants;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.observability.ObservabilityUtils;
import org.ballerinalang.util.observability.ObserverContext;
import org.ballerinalang.util.program.BLangFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_PACKAGE_HTTP;
import static org.ballerinalang.util.observability.ObservabilityConstants.PROPERTY_TRACE_PROPERTIES;
import static org.ballerinalang.util.observability.ObservabilityConstants.SERVER_CONNECTOR_HTTP;
import static org.ballerinalang.util.observability.ObservabilityConstants.TAG_KEY_HTTP_METHOD;
import static org.ballerinalang.util.observability.ObservabilityConstants.TAG_KEY_HTTP_URL;
import static org.ballerinalang.util.observability.ObservabilityConstants.TAG_KEY_PROTOCOL;

/**
 * HTTP connector listener for Ballerina.
 */
public class BallerinaHTTPConnectorListener implements HttpConnectorListener {

    private static final Logger log = LoggerFactory.getLogger(BallerinaHTTPConnectorListener.class);
    protected static final String HTTP_RESOURCE = "httpResource";

    private final HTTPServicesRegistry httpServicesRegistry;

    private final Value[] filterHolders;

    public BallerinaHTTPConnectorListener(HTTPServicesRegistry httpServicesRegistry,
                                          Value[] filterHolders) {
        this.httpServicesRegistry = httpServicesRegistry;
        this.filterHolders = filterHolders;
    }

    @Override
    public void onMessage(HTTPCarbonMessage inboundMessage) {
        try {
            HttpResource httpResource;
            if (accessed(inboundMessage)) {
                httpResource = (HttpResource) inboundMessage.getProperty(HTTP_RESOURCE);
                extractPropertiesAndStartResourceExecution(inboundMessage, httpResource);
                return;
            }
            httpResource = HttpDispatcher.findResource(httpServicesRegistry, inboundMessage);
            if (HttpDispatcher.shouldDiffer(httpResource, hasFilters())) {
                inboundMessage.setProperty(HTTP_RESOURCE, httpResource);
                return;
            }
            if (httpResource != null) {
                extractPropertiesAndStartResourceExecution(inboundMessage, httpResource);
            }
        } catch (BallerinaException ex) {
            try {
                HttpUtil.handleFailure(inboundMessage, new BallerinaConnectorException(ex.getMessage(), ex.getCause()));
            } catch (Exception e) {
                log.error("Cannot handle error using the error handler for: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("Error in HTTP server connector: " + throwable.getMessage(), throwable);
    }

    protected void extractPropertiesAndStartResourceExecution(HTTPCarbonMessage inboundMessage,
                                                              HttpResource httpResource) {
        boolean isTransactionInfectable = httpResource.isTransactionInfectable();
        Map<String, Object> properties = collectRequestProperties(inboundMessage, isTransactionInfectable);
        BValue[] signatureParams = HttpDispatcher.getSignatureParameters(httpResource, inboundMessage);
        // invoke the request path filters
        WorkerExecutionContext parentCtx = new WorkerExecutionContext(
                httpResource.getBalResource().getResourceInfo().getServiceInfo().getPackageInfo().getProgramFile());
        invokeRequestFilters(inboundMessage, signatureParams[1], getRequestFilterContext(httpResource), parentCtx);

        Resource balResource = httpResource.getBalResource();

        Optional<ObserverContext> observerContext = ObservabilityUtils.startServerObservation(SERVER_CONNECTOR_HTTP,
                balResource.getServiceName(), balResource.getName(), null);
        observerContext.ifPresent(ctx -> {
            Map<String, String> httpHeaders = new HashMap<>();
            inboundMessage.getHeaders().forEach(entry -> httpHeaders.put(entry.getKey(), entry.getValue()));
            ctx.addProperty(PROPERTY_TRACE_PROPERTIES, httpHeaders);
            ctx.addTag(TAG_KEY_HTTP_METHOD, (String) inboundMessage.getProperty(HttpConstants.HTTP_METHOD));
            ctx.addTag(TAG_KEY_PROTOCOL, (String) inboundMessage.getProperty(HttpConstants.PROTOCOL));
            ctx.addTag(TAG_KEY_HTTP_URL, (String) inboundMessage.getProperty(HttpConstants.REQUEST_URL));
        });

        CallableUnitCallback callback = new HttpCallableUnitCallback(inboundMessage);
        //TODO handle BallerinaConnectorException
        Executor.submit(balResource, callback, properties, observerContext.orElse(null), parentCtx, signatureParams);
    }

    protected BValue getRequestFilterContext(HttpResource httpResource) {
        BStruct filterCtxtStruct = BLangConnectorSPIUtil.createBStruct(
                httpResource.getBalResource().getResourceInfo().getServiceInfo().getPackageInfo().getProgramFile(),
                PROTOCOL_PACKAGE_HTTP, "FilterContext");
        filterCtxtStruct.setRefField(0,
                                     new BTypeDescValue(httpResource.getBalResource().getResourceInfo().getServiceInfo()
                                                                .getType()));
        filterCtxtStruct.setStringField(0, httpResource.getParentService().getName());
        filterCtxtStruct.setStringField(1, httpResource.getName());
        filterCtxtStruct.setRefField(1, new BMap());
        return filterCtxtStruct;
    }

    protected boolean accessed(HTTPCarbonMessage inboundMessage) {
        return inboundMessage.getProperty(HTTP_RESOURCE) != null;
    }

    private Map<String, Object> collectRequestProperties(HTTPCarbonMessage inboundMessage, boolean isInfectable) {
        Map<String, Object> properties = new HashMap<>();
        if (inboundMessage.getProperty(HttpConstants.SRC_HANDLER) != null) {
            Object srcHandler = inboundMessage.getProperty(HttpConstants.SRC_HANDLER);
            properties.put(HttpConstants.SRC_HANDLER, srcHandler);
        }
        String txnId = inboundMessage.getHeader(HttpConstants.HEADER_X_XID);
        String registerAtUrl = inboundMessage.getHeader(HttpConstants.HEADER_X_REGISTER_AT_URL);
        //Return 500 if txn context is received when transactionInfectable=false
        if (!isInfectable && txnId != null) {
            throw new BallerinaConnectorException("Cannot create transaction context: " +
                                                          "resource is not transactionInfectable");
        }
        if (isInfectable && txnId != null && registerAtUrl != null) {
            properties.put(Constants.GLOBAL_TRANSACTION_ID, txnId);
            properties.put(Constants.TRANSACTION_URL, registerAtUrl);
            return properties;
        }
        properties.put(HttpConstants.REMOTE_ADDRESS, inboundMessage.getProperty(HttpConstants.REMOTE_ADDRESS));
        properties.put(HttpConstants.ORIGIN_HOST, inboundMessage.getHeader(HttpConstants.ORIGIN_HOST));
        properties.put(HttpConstants.POOLED_BYTE_BUFFER_FACTORY,
                       inboundMessage.getHeader(HttpConstants.POOLED_BYTE_BUFFER_FACTORY));
        return properties;
    }

    /**
     * Invokes the set of filters for the request path. If one filter fails, the execution would stop and the relevant
     * error message given from the filter will be returned to the user.
     *
     * @param inboundMessage {@link HTTPCarbonMessage} instance
     * @param requestObject     Representation of ballerina.net.Request struct
     * @param filterCtxt        filtering criteria
     * @param parentCtx         WorkerExecutionContext instance, which corresponds to the current worker execution
     *                          context
     */
    protected void invokeRequestFilters(HTTPCarbonMessage inboundMessage, BValue requestObject, BValue filterCtxt,
                                        WorkerExecutionContext parentCtx) {

        if (!hasFilters()) {
            // no filters, return
            return;
        }

        for (Value filterHolder : filterHolders) {
            BStructType structType = (BStructType) filterHolder.getVMValue().getType();
            // get the request filter function and invoke
            BValue[] returnValue = BLangFunctions
                    .invokeCallable(structType.structInfo.funcInfoEntries
                                            .get(HttpConstants.HTTP_REQUEST_FILTER_FUNCTION_NAME).functionInfo,
                                    parentCtx, new BValue[]{filterHolder.getVMValue(), requestObject, filterCtxt});
            BStruct filterResultStruct = (BStruct) returnValue[0];
            if (filterResultStruct.getBooleanField(0) == 0) {
                // filter returned false, stop further execution
                // get the status code and the message
                int filterStatusCode = Math.toIntExact(filterResultStruct.getIntField(0));
                // if the status code returned by filter result is 4xx, use it, else use the code 400
                int responseStatusCode = filterStatusCode >= 400 && filterStatusCode < 500 ? filterStatusCode : 400;
                inboundMessage.setProperty(HttpConstants.HTTP_STATUS_CODE, responseStatusCode);
                throw new BallerinaException("Request failed: " + filterResultStruct.getStringField(0));
            }
            // filter has returned true, can continue
        }
    }

    /**
     * Method to retrive if filters have been specified.
     */
    protected boolean hasFilters() {
        return filterHolders != null && filterHolders.length > 0;
    }

}
