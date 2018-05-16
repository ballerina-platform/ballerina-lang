/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.websub;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Value;
import org.ballerinalang.mime.util.Constants;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.BallerinaHTTPConnectorListener;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpResource;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.uri.URIUtil;
import org.ballerinalang.net.websub.util.WebSubUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.ballerinalang.net.http.HttpUtil.extractEntity;


import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ANNOTATED_TOPIC;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ENTITY_ACCESSED_REQUEST;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.PARAM_HUB_CHALLENGE;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.PARAM_HUB_LEASE_SECONDS;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.PARAM_HUB_MODE;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.PARAM_HUB_TOPIC;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.RESOURCE_NAME_ON_INTENT_VERIFICATION;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.SERVICE_ENDPOINT;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.STRUCT_WEBSUB_INTENT_VERIFICATION_REQUEST;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.STRUCT_WEBSUB_NOTIFICATION_REQUEST;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.SUBSCRIBE;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.UNSUBSCRIBE;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_PACKAGE;

/**
 * HTTP Connection Listener for Ballerina WebSub services.
 */
public class BallerinaWebSubConnectionListener extends BallerinaHTTPConnectorListener {

    private static final Logger log = LoggerFactory.getLogger(BallerinaWebSubConnectionListener.class);
    private WebSubServicesRegistry webSubServicesRegistry;
    private PrintStream console = System.out;

    public BallerinaWebSubConnectionListener(WebSubServicesRegistry webSubServicesRegistry, Value[] filterHolders) {
        super(webSubServicesRegistry, filterHolders);
        this.webSubServicesRegistry = webSubServicesRegistry;
    }

    @Override
    public void onMessage(HTTPCarbonMessage inboundMessage) {
        try {
            HttpResource httpResource;
            if (accessed(inboundMessage)) {
                if (inboundMessage.getProperty(HTTP_RESOURCE) instanceof String) {
                    if (inboundMessage.getProperty(HTTP_RESOURCE).equals(ANNOTATED_TOPIC)) {
                        autoRespondToIntentVerification(inboundMessage);
                        return;
                    } else {
                        httpResource = WebSubDispatcher.findResource(webSubServicesRegistry, inboundMessage);
                    }
                } else {
                    httpResource = (HttpResource) inboundMessage.getProperty(HTTP_RESOURCE);
                }
                extractPropertiesAndStartResourceExecution(inboundMessage, httpResource);
                return;
            }
            httpResource = WebSubDispatcher.findResource(webSubServicesRegistry, inboundMessage);
            //TODO: fix to avoid defering on GET, when onIntentVerification is included
            if (inboundMessage.getProperty(HTTP_RESOURCE) == null) {
                inboundMessage.setProperty(HTTP_RESOURCE, httpResource);
                return;
            } else if (inboundMessage.getProperty(HTTP_RESOURCE) instanceof String) {
                return;
            }
            extractPropertiesAndStartResourceExecution(inboundMessage, httpResource);
        } catch (BallerinaException ex) {
            try {
                HttpUtil.handleFailure(inboundMessage, new BallerinaConnectorException(ex.getMessage(), ex.getCause()));
            } catch (Exception e) {
                log.error("Cannot handle error using the error handler for: " + e.getMessage(), e);
            }
        }
    }


    protected void extractPropertiesAndStartResourceExecution(HTTPCarbonMessage httpCarbonMessage,
                                                              HttpResource httpResource) {
        BValue subscriberServiceEndpoint = getSubscriberServiceEndpoint(httpResource, httpCarbonMessage);
        BValue httpRequest;
        if (httpCarbonMessage.getProperty(ENTITY_ACCESSED_REQUEST) != null) {
            httpRequest = (BValue) httpCarbonMessage.getProperty(ENTITY_ACCESSED_REQUEST);
        } else {
            httpRequest = WebSubUtils.getHttpRequest(httpResource.getBalResource().getResourceInfo().getServiceInfo()
                                                             .getPackageInfo().getProgramFile(), httpCarbonMessage);
        }

        // invoke request path filters
        WorkerExecutionContext parentCtx = new WorkerExecutionContext(
                httpResource.getBalResource().getResourceInfo().getServiceInfo().getPackageInfo().getProgramFile());
        invokeRequestFilters(httpCarbonMessage, httpRequest, getRequestFilterContext(httpResource), parentCtx);

        Resource balResource = httpResource.getBalResource();
        List<ParamDetail> paramDetails = balResource.getParamDetails();
        BValue[] signatureParams = new BValue[paramDetails.size()];
        String resourceName = httpResource.getName();
        if (RESOURCE_NAME_ON_INTENT_VERIFICATION.equals(resourceName)) {
            signatureParams[0] = subscriberServiceEndpoint;
            BStruct intentVerificationRequestStruct = createIntentVerificationRequestStruct(balResource);
            if (httpCarbonMessage.getProperty(HttpConstants.QUERY_STR) != null) {
                String queryString = (String) httpCarbonMessage.getProperty(HttpConstants.QUERY_STR);
                BMap<String, BString> params = new BMap<>();
                try {
                    URIUtil.populateQueryParamMap(queryString, params);
                    intentVerificationRequestStruct.setStringField(0, params.get(PARAM_HUB_MODE).stringValue());
                    intentVerificationRequestStruct.setStringField(1, params.get(PARAM_HUB_TOPIC).stringValue());
                    intentVerificationRequestStruct.setStringField(2, params.get(PARAM_HUB_CHALLENGE).stringValue());
                    if (params.hasKey(PARAM_HUB_LEASE_SECONDS)) {
                        intentVerificationRequestStruct.setIntField(0, Integer.parseInt(
                                        params.get(PARAM_HUB_LEASE_SECONDS).stringValue()));
                    }
                } catch (UnsupportedEncodingException e) {
                    throw new BallerinaException("Error populating query map for intent verification request received: "
                                                         + e.getMessage());
                }
            }
            intentVerificationRequestStruct.setRefField(0, (BRefType) httpRequest);
            signatureParams[1] = intentVerificationRequestStruct;
        } else { //Notification Resource
            HTTPCarbonMessage response = HttpUtil.createHttpCarbonMessage(false);
            response.waitAndReleaseAllEntities();
            response.setProperty(HttpConstants.HTTP_STATUS_CODE, HttpResponseStatus.ACCEPTED.code());
            response.addHttpContent(new DefaultLastHttpContent());
            HttpUtil.sendOutboundResponse(httpCarbonMessage, response);
            BStruct notificationRequestStruct = createNotificationRequestStruct(balResource);
            BStruct entityStruct = extractEntity((BStruct) httpRequest);
            if (entityStruct != null) {
                if (entityStruct.getNativeData(Constants.MESSAGE_DATA_SOURCE) instanceof BJSON) {
                    BJSON jsonBody = (BJSON) (entityStruct.getNativeData(Constants.MESSAGE_DATA_SOURCE));
                    notificationRequestStruct.setRefField(0, jsonBody);
                } else {
                    console.println("ballerina: Non-JSON payload received as WebSub Notification");
                }
            }
            notificationRequestStruct.setRefField(1, (BRefType) httpRequest);
            signatureParams[0] = notificationRequestStruct;
        }

        CallableUnitCallback callback = new WebSubEmptyCallableUnitCallback();
        //TODO handle BallerinaConnectorException
        Executor.submit(balResource, callback, null, null, signatureParams);
    }

    /**
     * Method to retrieve the struct representing the WebSub subscriber service endpoint.
     *
     * @param httpResource      the resource of the service receiving the request
     * @param httpCarbonMessage the HTTP message representing the request received
     * @return the struct representing the subscriber service endpoint
     */
    private BStruct getSubscriberServiceEndpoint(HttpResource httpResource, HTTPCarbonMessage httpCarbonMessage) {
        BStruct subscriberServiceEndpoint = createSubscriberServiceEndpointStruct(httpResource.getBalResource());
        BStruct serviceEndpoint = BLangConnectorSPIUtil.createBStruct(
                httpResource.getBalResource().getResourceInfo().getServiceInfo().getPackageInfo().getProgramFile(),
                HttpConstants.PROTOCOL_PACKAGE_HTTP, HttpConstants.SERVICE_ENDPOINT);

        BStruct connection = BLangConnectorSPIUtil.createBStruct(
                httpResource.getBalResource().getResourceInfo().getServiceInfo().getPackageInfo().getProgramFile(),
                HttpConstants.PROTOCOL_PACKAGE_HTTP, HttpConstants.CONNECTION);

        HttpUtil.enrichServiceEndpointInfo(serviceEndpoint, httpCarbonMessage, httpResource);
        HttpUtil.enrichConnectionInfo(connection, httpCarbonMessage);
        serviceEndpoint.setRefField(HttpConstants.SERVICE_ENDPOINT_CONNECTION_INDEX, connection);

        subscriberServiceEndpoint.setRefField(1, serviceEndpoint);
        return subscriberServiceEndpoint;
    }

    /**
     * Method to create the struct representing the WebSub subscriber service endpoint.
     */
    private BStruct createSubscriberServiceEndpointStruct(Resource resource) {
        return createBStruct(resource.getResourceInfo().getServiceInfo().getPackageInfo().getProgramFile(),
                             WEBSUB_PACKAGE, SERVICE_ENDPOINT);
    }

    /**
     * Method to create the intent verification request struct representing a subscription/unsubscription intent
     * verification request received.
     */
    private BStruct createIntentVerificationRequestStruct(Resource resource) {
        return createBStruct(resource.getResourceInfo().getServiceInfo().getPackageInfo().getProgramFile(),
                             WEBSUB_PACKAGE, STRUCT_WEBSUB_INTENT_VERIFICATION_REQUEST);
    }

    /**
     * Method to create the notification request struct representing WebSub notifications received.
     */
    private BStruct createNotificationRequestStruct(Resource resource) {
        return createBStruct(resource.getResourceInfo().getServiceInfo().getPackageInfo().getProgramFile(),
                             WEBSUB_PACKAGE, STRUCT_WEBSUB_NOTIFICATION_REQUEST);
    }

    private BStruct createBStruct(ProgramFile programFile, String packagePath, String structName) {
        return BLangConnectorSPIUtil.createBStruct(programFile, packagePath, structName);
    }

    /**
     * Method to automatically respond to intent verification requests for subscriptions/unsubscriptions if a resource
     * named {@link WebSubSubscriberConstants#RESOURCE_NAME_ON_INTENT_VERIFICATION} is not specified.
     *
     * @param httpCarbonMessage the message/request received
     */
    private void autoRespondToIntentVerification(HTTPCarbonMessage httpCarbonMessage) {
        String annotatedTopic = httpCarbonMessage.getProperty(ANNOTATED_TOPIC).toString();
        if (httpCarbonMessage.getProperty(HttpConstants.QUERY_STR) != null) {
            String queryString = (String) httpCarbonMessage.getProperty(HttpConstants.QUERY_STR);
            BMap<String, BString> params = new BMap<>();
            try {
                HTTPCarbonMessage response = HttpUtil.createHttpCarbonMessage(false);
                response.waitAndReleaseAllEntities();
                URIUtil.populateQueryParamMap(queryString, params);
                String mode = params.get(PARAM_HUB_MODE).stringValue();
                if (!params.keySet().contains(PARAM_HUB_MODE) || !params.keySet().contains(PARAM_HUB_TOPIC)
                        || !params.keySet().contains(PARAM_HUB_CHALLENGE)) {
                    response.setProperty(HttpConstants.HTTP_STATUS_CODE, HttpResponseStatus.NOT_FOUND.code());
                    response.addHttpContent(new DefaultLastHttpContent());
                    HttpUtil.sendOutboundResponse(httpCarbonMessage, response);
                    console.println("ballerina: Error auto-responding to intent verification request: Mode, Topic "
                                            + "and/or callback not specified");
                }
                if ((SUBSCRIBE.equals(mode) || UNSUBSCRIBE.equals(mode))
                        && annotatedTopic.equals(params.get(PARAM_HUB_TOPIC).stringValue())) {
                    String challenge = params.get(PARAM_HUB_CHALLENGE).stringValue();
                    response.addHttpContent(new DefaultLastHttpContent(Unpooled.wrappedBuffer(
                            challenge.getBytes(StandardCharsets.UTF_8))));
                    response.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), Constants.TEXT_PLAIN);
                    response.setProperty(HttpConstants.HTTP_STATUS_CODE, HttpResponseStatus.ACCEPTED.code());
                    String intentVerificationMessage = "ballerina: Intent Verification agreed - Mode [" + mode
                                                        + "], Topic [" + annotatedTopic + "]";
                    if (params.hasKey(PARAM_HUB_LEASE_SECONDS)) {
                        intentVerificationMessage = intentVerificationMessage.concat(", Lease Seconds ["
                                         + params.get(PARAM_HUB_LEASE_SECONDS) + "]");
                    }
                    console.println(intentVerificationMessage);
                } else {
                    console.println("ballerina: Intent Verification denied - Mode [" + mode + "], Topic ["
                                        + params.get(PARAM_HUB_TOPIC).stringValue() + "]");
                    response.setProperty(HttpConstants.HTTP_STATUS_CODE, HttpResponseStatus.NOT_FOUND.code());
                    response.addHttpContent(new DefaultLastHttpContent());
                }
                HttpUtil.sendOutboundResponse(httpCarbonMessage, response);
            } catch (UnsupportedEncodingException e) {
                throw new BallerinaConnectorException("Error responding to intent verification request: "
                                                              + e.getMessage());
            }
        }
    }

}
