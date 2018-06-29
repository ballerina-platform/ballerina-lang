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

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.util.JSONUtils;
import org.ballerinalang.model.values.*;
import org.ballerinalang.net.uri.URIUtil;
import org.ballerinalang.runtime.message.BlobDataSource;
import org.ballerinalang.runtime.message.StringDataSource;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.mime.util.Constants.ENTITY;
import static org.ballerinalang.mime.util.Constants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_MIME;
import static org.ballerinalang.net.http.HttpConstants.CONNECTION;
import static org.ballerinalang.net.http.HttpConstants.DEFAULT_HOST;
import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_PACKAGE_HTTP;
import static org.ballerinalang.net.http.HttpConstants.REQUEST;
import static org.ballerinalang.net.http.HttpConstants.SERVICE_ENDPOINT;
import static org.ballerinalang.net.http.HttpConstants.SERVICE_ENDPOINT_CONNECTION_FIELD;

/**
 * {@code HttpDispatcher} is responsible for dispatching incoming http requests to the correct resource.
 *
 * @since 0.94
 */
public class HttpDispatcher {

    private static final Logger breLog = LoggerFactory.getLogger(HttpDispatcher.class);

    protected static HttpService findService(HTTPServicesRegistry servicesRegistry, HTTPCarbonMessage inboundReqMsg) {
        try {
            Map<String, HttpService> servicesOnInterface;
            List<String> sortedServiceURIs;
            String hostName = inboundReqMsg.getHeader(HttpHeaderNames.HOST.toString());
            if (hostName != null && servicesRegistry.getServicesMapHolder(hostName) != null) {
                servicesOnInterface = servicesRegistry.getServicesByHost(hostName);
                sortedServiceURIs = servicesRegistry.getSortedServiceURIsByHost(hostName);
            } else {
                servicesOnInterface = servicesRegistry.getServicesByHost(DEFAULT_HOST);
                sortedServiceURIs = servicesRegistry.getSortedServiceURIsByHost(DEFAULT_HOST);
            }

            String rawUri = (String) inboundReqMsg.getProperty(HttpConstants.TO);
            inboundReqMsg.setProperty(HttpConstants.RAW_URI, rawUri);
            Map<String, Map<String, String>> matrixParams = new HashMap<>();
            String uriWithoutMatrixParams = URIUtil.extractMatrixParams(rawUri, matrixParams);

            inboundReqMsg.setProperty(HttpConstants.TO, uriWithoutMatrixParams);
            inboundReqMsg.setProperty(HttpConstants.MATRIX_PARAMS, matrixParams);

            URI validatedUri = getValidatedURI(uriWithoutMatrixParams);

            String basePath = servicesRegistry.findTheMostSpecificBasePath(validatedUri.getPath(),
                    servicesOnInterface, sortedServiceURIs);

            if (basePath == null) {
                inboundReqMsg.setProperty(HttpConstants.HTTP_STATUS_CODE, 404);
                throw new BallerinaConnectorException("no matching service found for path : " +
                        validatedUri.getRawPath());
            }

            HttpService service = servicesOnInterface.get(basePath);
            setInboundReqProperties(inboundReqMsg, validatedUri, basePath);
            return service;
        } catch (Throwable e) {
            throw new BallerinaConnectorException(e.getMessage());
        }
    }

    private static void setInboundReqProperties(HTTPCarbonMessage inboundReqMsg, URI requestUri, String basePath) {
        String subPath = URIUtil.getSubPath(requestUri.getPath(), basePath);
        inboundReqMsg.setProperty(HttpConstants.BASE_PATH, basePath);
        inboundReqMsg.setProperty(HttpConstants.SUB_PATH, subPath);
        inboundReqMsg.setProperty(HttpConstants.QUERY_STR, requestUri.getQuery());
        //store query params comes with request as it is
        inboundReqMsg.setProperty(HttpConstants.RAW_QUERY_STR, requestUri.getRawQuery());
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
        String interfaceId = (String) inboundRequest.getProperty(HttpConstants.LISTENER_INTERFACE_ID);
        if (interfaceId == null) {
            if (breLog.isDebugEnabled()) {
                breLog.debug("Interface id not found on the message, hence using the default interface");
            }
            interfaceId = HttpConstants.DEFAULT_INTERFACE;
        }

        return interfaceId;
    }

    /**
     * This method finds the matching resource for the incoming request.
     *
     * @param inboundMessage incoming message.
     * @return matching resource.
     */
    public static HttpResource findResource(HTTPServicesRegistry servicesRegistry, HTTPCarbonMessage inboundMessage) {
        String protocol = (String) inboundMessage.getProperty(HttpConstants.PROTOCOL);
        if (protocol == null) {
            throw new BallerinaConnectorException("protocol not defined in the incoming request");
        }

        try {
            // Find the Service TODO can be improved
            HttpService service = HttpDispatcher.findService(servicesRegistry, inboundMessage);
            if (service == null) {
                throw new BallerinaConnectorException("no Service found to handle the service request");
                // Finer details of the errors are thrown from the dispatcher itself, Ideally we shouldn't get here.
            }

            // Find the Resource
            return HttpResourceDispatcher.findResource(service, inboundMessage);
        } catch (Throwable throwable) {
            throw new BallerinaConnectorException(throwable.getMessage());
        }
    }

    public static BValue[] getSignatureParameters(HttpResource httpResource, HTTPCarbonMessage httpCarbonMessage,
                                                  Struct endpointConfig) {
        //TODO Think of keeping struct type globally rather than creating for each request
        ProgramFile programFile =
                httpResource.getBalResource().getResourceInfo().getServiceInfo().getPackageInfo().getProgramFile();

        BMap<String, BValue> serviceEndpoint =
                BLangConnectorSPIUtil.createBStruct(programFile, PROTOCOL_PACKAGE_HTTP, SERVICE_ENDPOINT);
        BMap<String, BValue> connection =
                BLangConnectorSPIUtil.createBStruct(programFile, PROTOCOL_PACKAGE_HTTP, CONNECTION);
        BMap<String, BValue> inRequest =
                BLangConnectorSPIUtil.createBStruct(programFile, PROTOCOL_PACKAGE_HTTP, REQUEST);
        BMap<String, BValue> inRequestEntity =
                BLangConnectorSPIUtil.createBStruct(programFile, PROTOCOL_PACKAGE_MIME, ENTITY);
        BMap<String, BValue> mediaType =
                BLangConnectorSPIUtil.createBStruct(programFile, PROTOCOL_PACKAGE_MIME, MEDIA_TYPE);

        HttpUtil.enrichServiceEndpointInfo(serviceEndpoint, httpCarbonMessage, httpResource, endpointConfig);
        HttpUtil.enrichConnectionInfo(connection, httpCarbonMessage, endpointConfig);
        serviceEndpoint.put(SERVICE_ENDPOINT_CONNECTION_FIELD, connection);

        HttpUtil.enrichConnectionInfo(connection, httpCarbonMessage, endpointConfig);
        HttpUtil.populateInboundRequest(inRequest, inRequestEntity, mediaType, httpCarbonMessage, programFile);

        SignatureParams signatureParams = httpResource.getSignatureParams();
        BValue[] bValues = new BValue[signatureParams.getParamCount()];
        bValues[0] = serviceEndpoint;
        bValues[1] = inRequest;
        if (signatureParams.getParamCount() == 2) {
            return bValues;
        }

        Map<String, String> resourceArgumentValues =
                (Map<String, String>) httpCarbonMessage.getProperty(HttpConstants.RESOURCE_ARGS);
        for (int i = 0; i < signatureParams.getPathParams().size(); i++) {
            //No need for validation as validation already happened at deployment time,
            //only string parameters can be found here.
            String argumentValue = resourceArgumentValues.get(signatureParams.getPathParams().get(i).getVarName());
            if (argumentValue != null) {
                try {
                    argumentValue = URLDecoder.decode(argumentValue, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    // we can simply ignore and send the value to application and let the
                    // application deal with the value.
                }
            }
            bValues[i + 2] = new BString(argumentValue);
        }

        if (signatureParams.getEntityBody() == null) {
            return bValues;
        }
        try {
            bValues[bValues.length - 1] = populateAndGetEntityBody(inRequest, inRequestEntity,
                                                                   signatureParams.getEntityBody().getVarType());
        } catch (BallerinaException ex) {
            httpCarbonMessage.setProperty(HttpConstants.HTTP_STATUS_CODE, HttpConstants.HTTP_BAD_REQUEST);
            throw new BallerinaConnectorException("data binding failed: " + ex.getMessage());
        }
        return bValues;
    }

    private static BValue populateAndGetEntityBody(BMap<String, BValue> inRequest,
                                                   BMap<String, BValue> inRequestEntity, BType entityBodyType) {
        HttpUtil.populateEntityBody(null, inRequest, inRequestEntity, true);
        try {
            switch (entityBodyType.getTag()) {
                case TypeTags.STRING_TAG:
                    StringDataSource stringDataSource = EntityBodyHandler.constructStringDataSource(inRequestEntity);
                    EntityBodyHandler.addMessageDataSource(inRequestEntity, stringDataSource);
                    return stringDataSource != null ? new BString(stringDataSource.getMessageAsString()) : null;
                case TypeTags.JSON_TAG:
                    BJSON bjson = EntityBodyHandler.constructJsonDataSource(inRequestEntity);
                    EntityBodyHandler.addMessageDataSource(inRequestEntity, bjson);
                    return bjson;
                case TypeTags.XML_TAG:
                    BXML bxml = EntityBodyHandler.constructXmlDataSource(inRequestEntity);
                    EntityBodyHandler.addMessageDataSource(inRequestEntity, bxml);
                    return bxml;
                case TypeTags.ARRAY_TAG:
                    BlobDataSource blobDataSource = EntityBodyHandler.constructBlobDataSource(inRequestEntity);
                    EntityBodyHandler.addMessageDataSource(inRequestEntity, blobDataSource);
                    return new BByteArray(blobDataSource != null ? blobDataSource.getValue() : new byte[0]);
                case TypeTags.OBJECT_TYPE_TAG:
                case TypeTags.RECORD_TYPE_TAG:
                    bjson = EntityBodyHandler.constructJsonDataSource(inRequestEntity);
                    EntityBodyHandler.addMessageDataSource(inRequestEntity, bjson);
                    try {
                        return JSONUtils.convertJSONToStruct(bjson, (BStructureType) entityBodyType);
                    } catch (NullPointerException ex) {
                        throw new BallerinaConnectorException("cannot convert payload to struct type: " +
                                entityBodyType.getName());
                    }
            }
        } catch (Exception ex) {
            throw new BallerinaConnectorException("Error in reading payload : " + ex.getMessage());
        }
        return null;
    }

    public static boolean shouldDiffer(HttpResource httpResource) {
        return ((httpResource != null && httpResource.getSignatureParams().getEntityBody() != null));
    }

}
