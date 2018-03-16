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
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BTypeValue;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.serviceendpoint.FilterHolder;
import org.ballerinalang.runtime.Constants;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_PACKAGE_HTTP;

/**
 * HTTP connector listener for Ballerina.
 */
public class BallerinaHTTPConnectorListener implements HttpConnectorListener {

    private static final Logger log = LoggerFactory.getLogger(BallerinaHTTPConnectorListener.class);
    private static final String HTTP_RESOURCE = "httpResource";

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
            if (HttpDispatcher.isDiffered(httpResource)) {
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

    private void extractPropertiesAndStartResourceExecution(HTTPCarbonMessage httpCarbonMessage,
                                                            HttpResource httpResource) {
        Map<String, Object> properties = collectRequestProperties(httpCarbonMessage);
        properties.put(HttpConstants.REMOTE_ADDRESS, httpCarbonMessage.getProperty(HttpConstants.REMOTE_ADDRESS));
        properties.put(HttpConstants.ORIGIN_HOST, httpCarbonMessage.getHeader(HttpConstants.ORIGIN_HOST));
        BValue[] signatureParams = HttpDispatcher.getSignatureParameters(httpResource, httpCarbonMessage);
        // invoke the request path filters
        invokeRequestFilters(httpCarbonMessage, signatureParams[1], getRequestFilterContext(httpResource));
        CallableUnitCallback callback = new HttpCallableUnitCallback(httpCarbonMessage);
        //TODO handle BallerinaConnectorException
        Executor.submit(httpResource.getBalResource(), callback, properties, signatureParams);
    }

    private BValue getRequestFilterContext(HttpResource httpResource) {
        BStruct filterCtxtStruct = BLangConnectorSPIUtil.createBStruct(
                httpResource.getBalResource().getResourceInfo().getServiceInfo().getPackageInfo().getProgramFile(),
                PROTOCOL_PACKAGE_HTTP, "FilterContext");
        filterCtxtStruct.setRefField(0,
                new BTypeValue(httpResource.getBalResource().getResourceInfo().getServiceInfo().getType()));
        filterCtxtStruct.setStringField(0, httpResource.getParentService().getName());
        filterCtxtStruct.setStringField(1, httpResource.getName());
        return filterCtxtStruct;
    }

    private boolean accessed(HTTPCarbonMessage httpCarbonMessage) {
        return httpCarbonMessage.getProperty(HTTP_RESOURCE) != null;
    }

    private Map<String, Object> collectRequestProperties(HTTPCarbonMessage httpCarbonMessage) {
        Map<String, Object> properties = new HashMap<>();
        if (httpCarbonMessage.getProperty(HttpConstants.SRC_HANDLER) != null) {
            Object srcHandler = httpCarbonMessage.getProperty(HttpConstants.SRC_HANDLER);
            properties.put(HttpConstants.SRC_HANDLER, srcHandler);
        }
        if (httpCarbonMessage.getHeader(HttpConstants.HEADER_X_XID) == null ||
                httpCarbonMessage.getHeader(HttpConstants.HEADER_X_REGISTER_AT_URL) == null) {
            return properties;
        }
        properties.put(Constants.GLOBAL_TRANSACTION_ID, httpCarbonMessage.getHeader(HttpConstants.HEADER_X_XID));
        properties.put(Constants.TRANSACTION_URL, httpCarbonMessage.getHeader(HttpConstants.HEADER_X_REGISTER_AT_URL));
        return properties;
    }

    /**
     * Invokes the set of filters for the request path. If one filter fails, the execution would stop and the
     * relevant error message given from the filter will be returned to the user.
     *
     * @param httpCarbonMessage {@link HTTPCarbonMessage} instance
     * @param requestObject     Representation of ballerina.net.Request struct
     * @param filterCtxt        filtering criteria
     */
    private void invokeRequestFilters(HTTPCarbonMessage httpCarbonMessage, BValue requestObject, BValue filterCtxt) {

        if (filterHolders == null || filterHolders.isEmpty()) {
            // no filters, return
            return;
        }

        for (FilterHolder filterHolder : filterHolders) {
            // get the request filter function and invoke
            BValue[] returnValue = BLangFunctions
                    .invokeCallable(filterHolder.getRequestFilterFunction().getFunctionInfo(),
                            new BValue[] { requestObject, filterCtxt });
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
}
