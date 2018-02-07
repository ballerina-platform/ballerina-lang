/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
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
import org.ballerinalang.connector.api.ConnectorUtils;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.uri.URIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code HttpDispatcher} is responsible for dispatching incoming http requests to the correct resource.
 *
 * @since 0.94
 */
public class HttpDispatcher {

    private static final Logger breLog = LoggerFactory.getLogger(HttpDispatcher.class);

    private static HttpService findService(HTTPServicesRegistry servicesRegistry, HTTPCarbonMessage inboundReqMsg) {
        try {
            Map<String, HttpService> servicesOnInterface = getServicesOnInterface(servicesRegistry, inboundReqMsg);

            // Extracting Matrix params and clean the URI
            Map<String, Map<String, String>> matrixParams = new HashMap<>();
            String uriStr = (String) inboundReqMsg.getProperty(Constants.TO);
            uriStr = URIUtil.removeMatrixParams(uriStr, matrixParams);

            inboundReqMsg.setProperty(Constants.TO, uriStr);
            inboundReqMsg.setProperty(Constants.MATRIX_PARAMS, matrixParams);

            URI requestUri = getValidatedURI(uriStr);

            // Most of the time we will find service from here
            String basePath =
                    servicesRegistry.findTheMostSpecificBasePath(requestUri.getPath(), servicesOnInterface);
            HttpService service = servicesOnInterface.get(basePath);
            if (service == null) {
                inboundReqMsg.setProperty(Constants.HTTP_STATUS_CODE, 404);
                throw new BallerinaConnectorException("no matching service found for path : " +
                        requestUri.getRawPath());
            }

            setInboundReqProperties(inboundReqMsg, requestUri, basePath);

            return service;
        } catch (Throwable e) {
            throw new BallerinaConnectorException(e.getMessage());
        }
    }

    private static Map<String, HttpService> getServicesOnInterface(HTTPServicesRegistry servicesRegistry,
            HTTPCarbonMessage inboundReqMsg) {
        String interfaceId = getInterface(inboundReqMsg);
        Map<String, HttpService> servicesOnInterface = servicesRegistry.getServicesInfoByInterface(interfaceId);
        if (servicesOnInterface == null) {
            throw new BallerinaConnectorException("no services found for interface : " + interfaceId);
        }
        return servicesOnInterface;
    }

    private static void setInboundReqProperties(HTTPCarbonMessage inboundReqMsg, URI requestUri, String basePath) {
        String subPath = URIUtil.getSubPath(requestUri.getPath(), basePath);
        inboundReqMsg.setProperty(Constants.BASE_PATH, basePath);
        inboundReqMsg.setProperty(Constants.SUB_PATH, subPath);
        inboundReqMsg.setProperty(Constants.QUERY_STR, requestUri.getQuery());
        //store query params comes with request as it is
        inboundReqMsg.setProperty(Constants.RAW_QUERY_STR, requestUri.getRawQuery());
    }

    private static URI getValidatedURI(String uriStr) {
        URI requestUri;
        try {
            requestUri = URI.create(uriStr);
        } catch (IllegalArgumentException e) {
            throw new BallerinaConnectorException(e.getMessage());
        }
        return requestUri;
    }

    private static String getInterface(HTTPCarbonMessage inboundRequest) {
        String interfaceId = (String) inboundRequest.getProperty(Constants.LISTENER_INTERFACE_ID);
        if (interfaceId == null) {
            if (breLog.isDebugEnabled()) {
                breLog.debug("Interface id not found on the message, hence using the default interface");
            }
            interfaceId = Constants.DEFAULT_INTERFACE;
        }

        return interfaceId;
    }

    private static void handleError(HTTPCarbonMessage cMsg, Throwable throwable) {
        String errorMsg = throwable.getMessage();

        // bre log should contain bre stack trace, not the ballerina stack trace
        breLog.error("error: " + errorMsg, throwable);
        try {
            HttpUtil.handleFailure(cMsg, new BallerinaConnectorException(errorMsg, throwable.getCause()));
        } catch (Exception e) {
            breLog.error("Cannot handle error using the error handler for: " + e.getMessage(), e);
        }
    }

    /**
     * This method finds the matching resource for the incoming request.
     *
     * @param httpCarbonMessage incoming message.
     * @return matching resource.
     */
    public static HttpResource findResource(HTTPServicesRegistry servicesRegistry,
                                            HTTPCarbonMessage httpCarbonMessage) {
        HttpResource resource = null;
        String protocol = (String) httpCarbonMessage.getProperty(Constants.PROTOCOL);
        if (protocol == null) {
            throw new BallerinaConnectorException("protocol not defined in the incoming request");
        }

        try {
            // Find the Service TODO can be improved
            HttpService service = HttpDispatcher.findService(servicesRegistry, httpCarbonMessage);
            if (service == null) {
                throw new BallerinaConnectorException("no Service found to handle the service request");
                // Finer details of the errors are thrown from the dispatcher itself, Ideally we shouldn't get here.
            }

            // Find the Resource
            resource = HTTPResourceDispatcher.findResource(service, httpCarbonMessage);
        } catch (Throwable throwable) {
            handleError(httpCarbonMessage, throwable);
        }
        return resource;
    }

    public static BValue[] getSignatureParameters(HttpResource httpResource, HTTPCarbonMessage httpCarbonMessage) {
        //TODO Think of keeping struct type globally rather than creating for each request
        BStruct connection = ConnectorUtils.createStruct(httpResource.getBalResource(),
                Constants.PROTOCOL_PACKAGE_HTTP, Constants.CONNECTION);
        BStruct inRequest = ConnectorUtils.createStruct(httpResource.getBalResource(),
                Constants.PROTOCOL_PACKAGE_HTTP, Constants.IN_REQUEST);

        BStruct entityForRequest = ConnectorUtils.createStruct(httpResource.getBalResource(),
                org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_MIME,
                org.ballerinalang.mime.util.Constants.ENTITY);

        BStruct mediaType = ConnectorUtils.createStruct(httpResource.getBalResource(),
                org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_MIME,
                org.ballerinalang.mime.util.Constants.MEDIA_TYPE);

        HttpUtil.enrichConnectionInfo(connection, httpCarbonMessage);
        HttpUtil.populateInboundRequest(inRequest, entityForRequest, mediaType, httpCarbonMessage);

        List<ParamDetail> paramDetails = httpResource.getParamDetails();
        BValue[] bValues = new BValue[paramDetails.size()];
        bValues[0] = connection;
        bValues[1] = inRequest;
        if (paramDetails.size() == 2) {
            return bValues;
        }

        Map<String, String> resourceArgumentValues =
                (Map<String, String>) httpCarbonMessage.getProperty(Constants.RESOURCE_ARGS);
        for (int i = 2; i < paramDetails.size(); i++) {
            //No need for validation as validation already happened at deployment time,
            //only string parameters can be found here.
            String argumentValue = resourceArgumentValues.get(paramDetails.get(i).getVarName());
            if (argumentValue != null) {
                try {
                    argumentValue = URLDecoder.decode(argumentValue, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    // we can simply ignore and send the value to application and let the
                    // application deal with the value.
                }
            }
            bValues[i] = new BString(argumentValue);
        }
        return bValues;
    }
}
