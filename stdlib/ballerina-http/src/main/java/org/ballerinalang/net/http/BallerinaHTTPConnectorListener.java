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
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BTypeDescValue;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.serviceendpoint.FilterHolder;
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
import java.util.HashSet;
import java.util.Map;

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

    private final HashSet<FilterHolder> filterHolders;

    public BallerinaHTTPConnectorListener(HTTPServicesRegistry httpServicesRegistry,
                                          HashSet<FilterHolder> filterHolders) {
        this.httpServicesRegistry = httpServicesRegistry;
        this.filterHolders = filterHolders;
    }

    @Override
    public void onMessage(HTTPCarbonMessage httpCarbonMessage) {
        try {
            HttpResource httpResource;
            if (accessed(httpCarbonMessage)) {
                httpResource = (HttpResource) httpCarbonMessage.getProperty(HTTP_RESOURCE);
                extractPropertiesAndStartResourceExecution(httpCarbonMessage, httpResource);
                return;
            }
            httpResource = HttpDispatcher.findResource(httpServicesRegistry, httpCarbonMessage);
            if (HttpDispatcher.shouldDiffer(httpResource, hasFilters())) {
                httpCarbonMessage.setProperty(HTTP_RESOURCE, httpResource);
                return;
            }
            extractPropertiesAndStartResourceExecution(httpCarbonMessage, httpResource);
        } catch (BallerinaException ex) {
            HttpUtil.handleFailure(httpCarbonMessage, new BallerinaConnectorException(ex.getMessage(), ex.getCause()));
        }
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("Error in http server connector" + throwable.getMessage(), throwable);
    }

    protected void extractPropertiesAndStartResourceExecution(HTTPCarbonMessage httpCarbonMessage,
                                                              HttpResource httpResource) {
        boolean isTransactionInfectable = httpResource.isTransactionInfectable();
        Map<String, Object> properties = collectRequestProperties(httpCarbonMessage, isTransactionInfectable);
        BValue[] signatureParams = HttpDispatcher.getSignatureParameters(httpResource, httpCarbonMessage);
        // invoke the request path filters
        WorkerExecutionContext parentCtx = new WorkerExecutionContext(
                httpResource.getBalResource().getResourceInfo().getServiceInfo().getPackageInfo().getProgramFile());
        invokeRequestFilters(httpCarbonMessage, signatureParams[1], getRequestFilterContext(httpResource), parentCtx);

        Resource balResource = httpResource.getBalResource();

        ObserverContext ctx = ObservabilityUtils.startServerObservation(SERVER_CONNECTOR_HTTP,
                balResource.getServiceName(), balResource.getName(), null, null);
        Map<String, String> httpHeaders = new HashMap<>();
        httpCarbonMessage.getHeaders().forEach(entry -> httpHeaders.put(entry.getKey(), entry.getValue()));
        ctx.addProperty(PROPERTY_TRACE_PROPERTIES, httpHeaders);

        ctx.addTag(TAG_KEY_HTTP_METHOD, (String) httpCarbonMessage.getProperty(HttpConstants.HTTP_METHOD));
        ctx.addTag(TAG_KEY_PROTOCOL, (String) httpCarbonMessage.getProperty(HttpConstants.PROTOCOL));
        ctx.addTag(TAG_KEY_HTTP_URL, (String) httpCarbonMessage.getProperty(HttpConstants.REQUEST_URL));

        CallableUnitCallback callback = new HttpCallableUnitCallback(httpCarbonMessage);
        //TODO handle BallerinaConnectorException
        Executor.submit(balResource, callback, properties, ctx, parentCtx, signatureParams);
    }

    protected BValue getRequestFilterContext(HttpResource httpResource) {
        BStruct filterCtxtStruct = BLangConnectorSPIUtil.createBStruct(
                httpResource.getBalResource().getResourceInfo().getServiceInfo().getPackageInfo().getProgramFile(),
                PROTOCOL_PACKAGE_HTTP, "FilterContext");
        filterCtxtStruct.setRefField(0,
                new BTypeDescValue(httpResource.getBalResource().getResourceInfo().getServiceInfo().getType()));
        filterCtxtStruct.setStringField(0, httpResource.getParentService().getName());
        filterCtxtStruct.setStringField(1, httpResource.getName());
        return filterCtxtStruct;
    }

    protected boolean accessed(HTTPCarbonMessage httpCarbonMessage) {
        return httpCarbonMessage.getProperty(HTTP_RESOURCE) != null;
    }

    private Map<String, Object> collectRequestProperties(HTTPCarbonMessage httpCarbonMessage, boolean isInfectable) {
        Map<String, Object> properties = new HashMap<>();
        if (httpCarbonMessage.getProperty(HttpConstants.SRC_HANDLER) != null) {
            Object srcHandler = httpCarbonMessage.getProperty(HttpConstants.SRC_HANDLER);
            properties.put(HttpConstants.SRC_HANDLER, srcHandler);
        }
        String txnId = httpCarbonMessage.getHeader(HttpConstants.HEADER_X_XID);
        String registerAtUrl = httpCarbonMessage.getHeader(HttpConstants.HEADER_X_REGISTER_AT_URL);
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
        properties.put(HttpConstants.REMOTE_ADDRESS, httpCarbonMessage.getProperty(HttpConstants.REMOTE_ADDRESS));
        properties.put(HttpConstants.ORIGIN_HOST, httpCarbonMessage.getHeader(HttpConstants.ORIGIN_HOST));
        properties.put(HttpConstants.POOLED_BYTE_BUFFER_FACTORY,
                httpCarbonMessage.getHeader(HttpConstants.POOLED_BYTE_BUFFER_FACTORY));
        return properties;
    }

    /**
     * Invokes the set of filters for the request path. If one filter fails, the execution would stop and the
     * relevant error message given from the filter will be returned to the user.
     *
     * @param httpCarbonMessage {@link HTTPCarbonMessage} instance
     * @param requestObject     Representation of ballerina.net.Request struct
     * @param filterCtxt        filtering criteria
     * @param parentCtx         WorkerExecutionContext instance, which corresponds to the current worker execution
     *                          context
     */
    protected void invokeRequestFilters(HTTPCarbonMessage httpCarbonMessage, BValue requestObject, BValue filterCtxt,
            WorkerExecutionContext parentCtx) {

        if (!hasFilters()) {
            // no filters, return
            return;
        }

        for (FilterHolder filterHolder : filterHolders) {
            // get the request filter function and invoke
            BValue[] returnValue = BLangFunctions
                    .invokeCallable(filterHolder.getRequestFilterFunction().getFunctionInfo(), parentCtx,
                            new BValue[]{requestObject, filterCtxt});
            BStruct filterResultStruct = (BStruct) returnValue[0];
            if (filterResultStruct.getBooleanField(0) == 0) {
                // filter returned false, stop further execution
                // get the status code and the message
                int filterStatusCode = Math.toIntExact(filterResultStruct.getIntField(0));
                // if the status code returned by filter result is 4xx, use it, else use the code 400
                int responseStatusCode = filterStatusCode >= 400 && filterStatusCode < 500 ? filterStatusCode : 400;
                httpCarbonMessage.setProperty(HttpConstants.HTTP_STATUS_CODE, responseStatusCode);
                throw new BallerinaException("Request failed: " + filterResultStruct.getStringField(0));
            }
            // filter has returned true, can continue
        }
    }

    /**
     * Method to retrive if filters have been specified.
     */
    protected boolean hasFilters() {
        return filterHolders != null && !filterHolders.isEmpty();
    }

}
