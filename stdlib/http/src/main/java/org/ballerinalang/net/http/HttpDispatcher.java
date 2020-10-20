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

import io.ballerina.runtime.api.BStringUtils;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXML;
import io.ballerina.runtime.util.exceptions.BallerinaConnectorException;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.langlib.value.CloneWithType;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.net.uri.URIUtil;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.net.http.HttpConstants.DEFAULT_HOST;
import static org.ballerinalang.net.http.compiler.ResourceSignatureValidator.COMPULSORY_PARAM_COUNT;

/**
 * {@code HttpDispatcher} is responsible for dispatching incoming http requests to the correct resource.
 *
 * @since 0.94
 */
public class HttpDispatcher {

    public static HttpService findService(HTTPServicesRegistry servicesRegistry, HttpCarbonMessage inboundReqMsg) {
        try {
            Map<String, HttpService> servicesOnInterface;
            List<String> sortedServiceURIs;
            String hostName = inboundReqMsg.getHeader(HttpHeaderNames.HOST.toString());

            if (hostName != null && servicesRegistry.getServicesMapHolder(hostName) != null) {
                servicesOnInterface = servicesRegistry.getServicesByHost(hostName);
                sortedServiceURIs = servicesRegistry.getSortedServiceURIsByHost(hostName);
            } else if (servicesRegistry.getServicesMapHolder(DEFAULT_HOST) != null) {
                servicesOnInterface = servicesRegistry.getServicesByHost(DEFAULT_HOST);
                sortedServiceURIs = servicesRegistry.getSortedServiceURIsByHost(DEFAULT_HOST);
            } else {
                inboundReqMsg.setHttpStatusCode(404);
                String localAddress = inboundReqMsg.getProperty(HttpConstants.LOCAL_ADDRESS).toString();
                throw new BallerinaConnectorException("no service has registered for listener : " + localAddress);
            }

            String rawUri = (String) inboundReqMsg.getProperty(HttpConstants.TO);
            inboundReqMsg.setProperty(HttpConstants.RAW_URI, rawUri);
            Map<String, Map<String, String>> matrixParams = new HashMap<>();
            String uriWithoutMatrixParams = URIUtil.extractMatrixParams(rawUri, matrixParams);

            inboundReqMsg.setProperty(HttpConstants.TO, uriWithoutMatrixParams);
            inboundReqMsg.setProperty(HttpConstants.MATRIX_PARAMS, matrixParams);

            URI validatedUri = getValidatedURI(uriWithoutMatrixParams);

            String basePath = servicesRegistry.findTheMostSpecificBasePath(validatedUri.getRawPath(),
                    servicesOnInterface, sortedServiceURIs);

            if (basePath == null) {
                inboundReqMsg.setHttpStatusCode(404);
                throw new BallerinaConnectorException("no matching service found for path : " +
                        validatedUri.getRawPath());
            }

            HttpService service = servicesOnInterface.get(basePath);
            setInboundReqProperties(inboundReqMsg, validatedUri, basePath);
            return service;
        } catch (Exception e) {
            throw new BallerinaConnectorException(e.getMessage());
        }
    }

    private static void setInboundReqProperties(HttpCarbonMessage inboundReqMsg, URI requestUri, String basePath) {
        String subPath = URIUtil.getSubPath(requestUri.getRawPath(), basePath);
        inboundReqMsg.setProperty(HttpConstants.BASE_PATH, basePath);
        inboundReqMsg.setProperty(HttpConstants.SUB_PATH, subPath);
        inboundReqMsg.setProperty(HttpConstants.QUERY_STR, requestUri.getQuery());
        //store query params comes with request as it is
        inboundReqMsg.setProperty(HttpConstants.RAW_QUERY_STR, requestUri.getRawQuery());
    }

    public static URI getValidatedURI(String uriStr) {
        URI requestUri;
        try {
            requestUri = URI.create(uriStr);
        } catch (IllegalArgumentException e) {
            throw new BallerinaConnectorException(e.getMessage());
        }
        return requestUri;
    }

    /**
     * This method finds the matching resource for the incoming request.
     *
     * @param servicesRegistry HTTP service registry
     * @param inboundMessage incoming message.
     * @return matching resource.
     */
    public static HttpResource findResource(HTTPServicesRegistry servicesRegistry, HttpCarbonMessage inboundMessage) {
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
        } catch (Exception e) {
            throw new BallerinaConnectorException(e.getMessage());
        }
    }

    public static Object[] getSignatureParameters(HttpResource httpResource, HttpCarbonMessage httpCarbonMessage,
                                                  BMap<BString, Object> endpointConfig) {
        BObject httpCaller = ValueCreatorUtils.createCallerObject();
        BObject inRequest = ValueCreatorUtils.createRequestObject();
        BObject inRequestEntity = ValueCreatorUtils.createEntityObject();

        HttpUtil.enrichHttpCallerWithConnectionInfo(httpCaller, httpCarbonMessage, httpResource, endpointConfig);
        HttpUtil.enrichHttpCallerWithNativeData(httpCaller, httpCarbonMessage, endpointConfig);

        HttpUtil.populateInboundRequest(inRequest, inRequestEntity, httpCarbonMessage);

        SignatureParams signatureParams = httpResource.getSignatureParams();
        Object[] paramValues = new Object[signatureParams.getParamCount() * 2];
        int paramIndex = 0;
        paramValues[paramIndex++] = httpCaller;
        paramValues[paramIndex++] = true;
        paramValues[paramIndex++] = inRequest;
        paramValues[paramIndex] = true;
        if (signatureParams.getParamCount() == COMPULSORY_PARAM_COUNT) {
            return paramValues;
        }

        HttpResourceArguments resourceArgumentValues =
                (HttpResourceArguments) httpCarbonMessage.getProperty(HttpConstants.RESOURCE_ARGS);
        BMap pathParamOrder = HttpResource.getPathParamOrderMap(httpResource.getBalResource());

        for (Object paramName : pathParamOrder.getKeys()) {
            String argumentValue = resourceArgumentValues.getMap().get(paramName.toString());
            try {
                argumentValue = URLDecoder.decode(argumentValue, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // we can simply ignore and send the value to application and let the
                // application deal with the value.
            }
            int actualSignatureParamIndex = ((Long) pathParamOrder.get(paramName)).intValue();
            paramIndex = actualSignatureParamIndex * 2;
            Type signatureParamType = signatureParams.getPathParamTypes().get(
                    actualSignatureParamIndex - COMPULSORY_PARAM_COUNT);
            try {
                switch (signatureParamType.getTag()) {
                    case TypeTags.INT_TAG:
                        paramValues[paramIndex++] = Long.parseLong(argumentValue);
                        break;
                    case TypeTags.FLOAT_TAG:
                        paramValues[paramIndex++] = Double.parseDouble(argumentValue);
                        break;
                    case TypeTags.BOOLEAN_TAG:
                        paramValues[paramIndex++] = Boolean.parseBoolean(argumentValue);
                        break;
                    default:
                        paramValues[paramIndex++] = BStringUtils.fromString(argumentValue);
                }
                paramValues[paramIndex] = true;
            } catch (Exception ex) {
                throw new BallerinaConnectorException("Error in casting path param : " + ex.getMessage());
            }
        }

        if (signatureParams.getEntityBody() == null) {
            return paramValues;
        }
        try {
            paramValues[paramValues.length - 2] = populateAndGetEntityBody(inRequest, inRequestEntity,
                                                                   signatureParams.getEntityBody());
            paramValues[paramValues.length - 1] = true;
        } catch (Exception ex) {
            httpCarbonMessage.setHttpStatusCode(Integer.parseInt(HttpConstants.HTTP_BAD_REQUEST));
            throw new BallerinaConnectorException("data binding failed: " + ex.getMessage());
        }
        return paramValues;
    }

    private static Object populateAndGetEntityBody(BObject inRequest, BObject inRequestEntity,
                                                   Type entityBodyType)
            throws IOException {
        HttpUtil.populateEntityBody(inRequest, inRequestEntity, true, true);
        try {
            switch (entityBodyType.getTag()) {
                case TypeTags.STRING_TAG:
                    BString stringDataSource = EntityBodyHandler.constructStringDataSource(inRequestEntity);
                    EntityBodyHandler.addMessageDataSource(inRequestEntity, stringDataSource);
                    return stringDataSource;
                case TypeTags.JSON_TAG:
                    Object bjson = EntityBodyHandler.constructJsonDataSource(inRequestEntity);
                    EntityBodyHandler.addJsonMessageDataSource(inRequestEntity, bjson);
                    return bjson;
                case TypeTags.XML_TAG:
                    BXML bxml = EntityBodyHandler.constructXmlDataSource(inRequestEntity);
                    EntityBodyHandler.addMessageDataSource(inRequestEntity, bxml);
                    return bxml;
                case TypeTags.ARRAY_TAG:
                    if (((ArrayType) entityBodyType).getElementType().getTag() == TypeTags.BYTE_TAG) {
                        BArray blobDataSource = EntityBodyHandler.constructBlobDataSource(inRequestEntity);
                        EntityBodyHandler.addMessageDataSource(inRequestEntity, blobDataSource);
                        return blobDataSource;
                    } else if (((ArrayType) entityBodyType).getElementType().getTag() == TypeTags.RECORD_TYPE_TAG) {
                        return getRecordEntity(inRequestEntity, entityBodyType);
                    } else {
                        throw new BallerinaConnectorException("Incompatible Element type found inside an array " +
                                ((ArrayType) entityBodyType).getElementType().getName());
                    }
                case TypeTags.RECORD_TYPE_TAG:
                    return getRecordEntity(inRequestEntity, entityBodyType);
                default:
                        //Do nothing
            }
        } catch (BError ex) {
            throw new BallerinaConnectorException(ex.toString());
        }
        return null;
    }

    private static Object getRecordEntity(BObject inRequestEntity, Type entityBodyType) {
        Object result = getRecord(entityBodyType, getBJsonValue(inRequestEntity));
        if (result instanceof BError) {
            throw (BError) result;
        }
        return result;
    }

    /**
     * Convert a json to the relevant record type.
     *
     * @param entityBodyType Represents entity body type
     * @param bjson          Represents the json value that needs to be converted
     * @return the relevant ballerina record or object
     */
    private static Object getRecord(Type entityBodyType, Object bjson) {
        try {
            return CloneWithType.convert(entityBodyType, bjson);
        } catch (NullPointerException ex) {
            throw new BallerinaConnectorException("cannot convert payload to record type: " +
                    entityBodyType.getName());
        }
    }

    /**
     * Given an inbound request entity construct the ballerina json.
     *
     * @param inRequestEntity Represents inbound request entity
     * @return a ballerina json value
     */
    private static Object getBJsonValue(BObject inRequestEntity) {
        Object bjson = EntityBodyHandler.constructJsonDataSource(inRequestEntity);
        EntityBodyHandler.addJsonMessageDataSource(inRequestEntity, bjson);
        return bjson;
    }

    public static boolean shouldDiffer(HttpResource httpResource) {
        return (httpResource != null && httpResource.getSignatureParams().getEntityBody() != null);
    }

    private HttpDispatcher() {
    }
}
