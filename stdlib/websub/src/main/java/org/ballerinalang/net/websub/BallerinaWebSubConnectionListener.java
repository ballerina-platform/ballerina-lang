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
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Executor;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.util.JSONUtils;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BTypeDescValue;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.BallerinaHTTPConnectorListener;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpResource;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.uri.URIUtil;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.ballerinalang.bre.bvm.BLangVMErrors.ERROR_MESSAGE_FIELD;
import static org.ballerinalang.mime.util.MimeConstants.TEXT_PLAIN;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ANNOTATED_TOPIC;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ENTITY_ACCESSED_REQUEST;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.LISTENER_SERVICE_ENDPOINT;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.PARAM_HUB_CHALLENGE;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.PARAM_HUB_LEASE_SECONDS;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.PARAM_HUB_MODE;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.PARAM_HUB_TOPIC;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.REQUEST;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.RESOURCE_NAME_ON_INTENT_VERIFICATION;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.RESOURCE_NAME_ON_NOTIFICATION;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.SERVICE_ENDPOINT;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.STRUCT_WEBSUB_INTENT_VERIFICATION_REQUEST;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.STRUCT_WEBSUB_NOTIFICATION_REQUEST;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.SUBSCRIBE;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.UNSUBSCRIBE;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.VERIFICATION_REQUEST_CHALLENGE;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.VERIFICATION_REQUEST_LEASE_SECONDS;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.VERIFICATION_REQUEST_MODE;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.VERIFICATION_REQUEST_TOPIC;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_PACKAGE;
import static org.ballerinalang.net.websub.WebSubUtils.getHttpRequest;
import static org.ballerinalang.net.websub.WebSubUtils.getJsonBody;

/**
 * HTTP Connection Listener for Ballerina WebSub services.
 */
public class BallerinaWebSubConnectionListener extends BallerinaHTTPConnectorListener {

    private static final Logger log = LoggerFactory.getLogger(BallerinaWebSubConnectionListener.class);
    private WebSubServicesRegistry webSubServicesRegistry;
    private PrintStream console = System.out;
    private Context context;

    public BallerinaWebSubConnectionListener(WebSubServicesRegistry webSubServicesRegistry, Struct endpointConfig,
                                             Context context) {
        super(webSubServicesRegistry, endpointConfig);
        this.webSubServicesRegistry = webSubServicesRegistry;
        this.context = context;
    }

    @Override
    public void onMessage(HttpCarbonMessage inboundMessage) {
        try {
            HttpResource httpResource;
            if (accessed(inboundMessage)) {
                if (inboundMessage.getProperty(HTTP_RESOURCE) instanceof String) {
                    if (inboundMessage.getProperty(HTTP_RESOURCE).equals(ANNOTATED_TOPIC)) {
                        autoRespondToIntentVerification(inboundMessage);
                        return;
                    } else {
                        //if deferred for dispatching based on payload
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

    @SuppressWarnings("unchecked")
    protected void extractPropertiesAndStartResourceExecution(HttpCarbonMessage httpCarbonMessage,
                                                              HttpResource httpResource) {
        BValue subscriberServiceEndpoint = getSubscriberServiceEndpoint(httpResource, httpCarbonMessage);
        BMap<String, BValue>  httpRequest;
        if (httpCarbonMessage.getProperty(ENTITY_ACCESSED_REQUEST) != null) {
            httpRequest = (BMap<String, BValue>) httpCarbonMessage.getProperty(ENTITY_ACCESSED_REQUEST);
        } else {
            httpRequest = getHttpRequest(httpResource.getBalResource().getResourceInfo().getServiceInfo()
                                                             .getPackageInfo().getProgramFile(), httpCarbonMessage);
        }

        Resource balResource = httpResource.getBalResource();
        List<ParamDetail> paramDetails = balResource.getParamDetails();
        BValue[] signatureParams = new BValue[paramDetails.size()];
        String resourceName = httpResource.getName();
        if (RESOURCE_NAME_ON_INTENT_VERIFICATION.equals(resourceName)) {
            signatureParams[0] = subscriberServiceEndpoint;
            BMap<String, BValue> intentVerificationRequestStruct = createIntentVerificationRequestStruct(balResource);
            if (httpCarbonMessage.getProperty(HttpConstants.QUERY_STR) != null) {
                String queryString = (String) httpCarbonMessage.getProperty(HttpConstants.QUERY_STR);
                BMap<String, BString> params = new BMap<>();
                try {
                    URIUtil.populateQueryParamMap(queryString, params);
                    intentVerificationRequestStruct.put(VERIFICATION_REQUEST_MODE, params.get(PARAM_HUB_MODE));
                    intentVerificationRequestStruct.put(VERIFICATION_REQUEST_TOPIC, params.get(PARAM_HUB_TOPIC));
                    intentVerificationRequestStruct.put(VERIFICATION_REQUEST_CHALLENGE,
                            params.get(PARAM_HUB_CHALLENGE));
                    if (params.hasKey(PARAM_HUB_LEASE_SECONDS)) {
                        int leaseSec = Integer.parseInt(params.get(PARAM_HUB_LEASE_SECONDS).stringValue());
                        intentVerificationRequestStruct.put(VERIFICATION_REQUEST_LEASE_SECONDS,
                                new BInteger(leaseSec));
                    }
                } catch (UnsupportedEncodingException e) {
                    throw new BallerinaException("Error populating query map for intent verification request received: "
                                                         + e.getMessage());
                }
            }
            intentVerificationRequestStruct.put(REQUEST, (BRefType) httpRequest);
            signatureParams[1] = intentVerificationRequestStruct;
        } else { //Notification Resource
            //validate signature for requests received at the callback
            validateSignature(httpCarbonMessage, httpResource, httpRequest);

            HttpCarbonMessage response = HttpUtil.createHttpCarbonMessage(false);
            response.waitAndReleaseAllEntities();
            response.setProperty(HttpConstants.HTTP_STATUS_CODE, HttpResponseStatus.ACCEPTED.code());
            response.addHttpContent(new DefaultLastHttpContent());
            HttpUtil.sendOutboundResponse(httpCarbonMessage, response);

            signatureParams[0] = createNotificationStruct(balResource, httpRequest);
            if (!RESOURCE_NAME_ON_NOTIFICATION.equals(balResource.getName())) {
                signatureParams[1] = createCustomNotificationStruct(httpCarbonMessage, balResource, httpRequest);
            }
        }

        CallableUnitCallback callback = new WebSubEmptyCallableUnitCallback();
        //TODO handle BallerinaConnectorException
        Executor.submit(balResource, callback, null, null, signatureParams);
    }

    @SuppressWarnings("unchecked")
    private void validateSignature(HttpCarbonMessage httpCarbonMessage, HttpResource httpResource,
                                   BMap<String, BValue> requestStruct) {
        //invoke processWebSubNotification function
        PackageInfo packageInfo = context.getProgramFile().getPackageInfo(WEBSUB_PACKAGE);
        FunctionInfo functionInfo = packageInfo.getFunctionInfo("processWebSubNotification");
        BValue[] returnValues = BLangFunctions.invokeCallable(functionInfo, new BValue[]{requestStruct,
                new BTypeDescValue(httpResource.getBalResource().getResourceInfo().getServiceInfo().getType())});

        BError errorStruct = (BError) returnValues[0];
        if (errorStruct != null) {
            log.debug("Signature Validation failed for Notification: " +
                              ((BMap) errorStruct.getDetails()).get(ERROR_MESSAGE_FIELD).stringValue());
            httpCarbonMessage.setProperty(HttpConstants.HTTP_STATUS_CODE, 404);
            throw new BallerinaException("validation failed for notification");
        }
    }

    /**
     * Method to retrieve the struct representing the WebSub subscriber service endpoint.
     *
     * @param httpResource      the resource of the service receiving the request
     * @param httpCarbonMessage the HTTP message representing the request received
     * @return the struct representing the subscriber service endpoint
     */
    private BMap<String, BValue> getSubscriberServiceEndpoint(HttpResource httpResource,
                                                              HttpCarbonMessage httpCarbonMessage) {
        BMap<String, BValue> subscriberServiceEndpoint =
                createSubscriberServiceEndpointStruct(httpResource.getBalResource());
        BMap<String, BValue> serviceEndpoint = BLangConnectorSPIUtil.createBStruct(
                httpResource.getBalResource().getResourceInfo().getServiceInfo().getPackageInfo().getProgramFile(),
                HttpConstants.PROTOCOL_PACKAGE_HTTP, HttpConstants.SERVICE_ENDPOINT);

        BMap<String, BValue> connection = BLangConnectorSPIUtil.createBStruct(
                httpResource.getBalResource().getResourceInfo().getServiceInfo().getPackageInfo().getProgramFile(),
                HttpConstants.PROTOCOL_PACKAGE_HTTP, HttpConstants.CONNECTION);

        HttpUtil.enrichServiceEndpointInfo(serviceEndpoint, httpCarbonMessage, httpResource, endpointConfig);
        HttpUtil.enrichConnectionInfo(connection, httpCarbonMessage, endpointConfig);
        serviceEndpoint.put(HttpConstants.SERVICE_ENDPOINT_CONNECTION_FIELD, connection);

        subscriberServiceEndpoint.put(LISTENER_SERVICE_ENDPOINT, serviceEndpoint);
        return subscriberServiceEndpoint;
    }

    /**
     * Method to create the struct representing the WebSub subscriber service endpoint.
     */
    private BMap<String, BValue> createSubscriberServiceEndpointStruct(Resource resource) {
        return createBStruct(resource.getResourceInfo().getServiceInfo().getPackageInfo().getProgramFile(),
                             WEBSUB_PACKAGE, SERVICE_ENDPOINT);
    }

    /**
     * Method to create the intent verification request struct representing a subscription/unsubscription intent
     * verification request received.
     */
    private BMap<String, BValue> createIntentVerificationRequestStruct(Resource resource) {
        return createBStruct(resource.getResourceInfo().getServiceInfo().getPackageInfo().getProgramFile(),
                             WEBSUB_PACKAGE, STRUCT_WEBSUB_INTENT_VERIFICATION_REQUEST);
    }

    /**
     * Method to create the notification request struct representing WebSub notifications received.
     */
    private BMap<String, BValue> createNotificationStruct(Resource resource, BValue httpRequest) {
        BMap<String, BValue> notificationStruct = createDefaultNotificationStruct(resource);
        notificationStruct.put(REQUEST, httpRequest);
        return notificationStruct;
    }

    /**
     * Method to create the notification request struct representing WebSub notifications received.
     */
    private BMap<String, BValue> createCustomNotificationStruct(HttpCarbonMessage inboundRequest, Resource resource,
                                                                BMap<String, BValue> httpRequest) {
        String[] paramDetails = webSubServicesRegistry.getResourceDetails().get(resource.getName());
        BMap<String, BValue> customNotificationStruct =
                createBStruct(resource.getResourceInfo().getServiceInfo().getPackageInfo().getProgramFile(),
                              paramDetails[0], paramDetails[1]);
        BMap<String, ?> jsonBody = getJsonBody(httpRequest);
        inboundRequest.setProperty(ENTITY_ACCESSED_REQUEST, httpRequest);
        if (jsonBody != null) {
            return JSONUtils.convertJSONToStruct(jsonBody, (BStructureType) customNotificationStruct.getType());
        } else {
            throw new BallerinaException("JSON payload: null. Cannot create custom notification record: "
                                                 + paramDetails[0] + ":" + paramDetails[1]);
        }
    }

    /**
     * Method to create the notification request struct representing WebSub notifications received for the default
     * onNotification resource.
     */
    private BMap<String, BValue> createDefaultNotificationStruct(Resource resource) {
        return createBStruct(resource.getResourceInfo().getServiceInfo().getPackageInfo().getProgramFile(),
                             WEBSUB_PACKAGE, STRUCT_WEBSUB_NOTIFICATION_REQUEST);
    }

    private BMap<String, BValue> createBStruct(ProgramFile programFile, String packagePath, String structName) {
        return BLangConnectorSPIUtil.createBStruct(programFile, packagePath, structName);
    }

    /**
     * Method to automatically respond to intent verification requests for subscriptions/unsubscriptions if a resource
     * named {@link WebSubSubscriberConstants#RESOURCE_NAME_ON_INTENT_VERIFICATION} is not specified.
     *
     * @param httpCarbonMessage the message/request received
     */
    private void autoRespondToIntentVerification(HttpCarbonMessage httpCarbonMessage) {
        String annotatedTopic = httpCarbonMessage.getProperty(ANNOTATED_TOPIC).toString();
        if (httpCarbonMessage.getProperty(HttpConstants.QUERY_STR) != null) {
            String queryString = (String) httpCarbonMessage.getProperty(HttpConstants.QUERY_STR);
            BMap<String, BString> params = new BMap<>();
            try {
                HttpCarbonMessage response = HttpUtil.createHttpCarbonMessage(false);
                response.waitAndReleaseAllEntities();
                URIUtil.populateQueryParamMap(queryString, params);
                String mode = params.get(PARAM_HUB_MODE).stringValue();
                if (!params.hasKey(PARAM_HUB_MODE) || !params.hasKey(PARAM_HUB_TOPIC) ||
                        !params.hasKey(PARAM_HUB_CHALLENGE)) {
                    response.setProperty(HttpConstants.HTTP_STATUS_CODE, HttpResponseStatus.NOT_FOUND.code());
                    response.addHttpContent(new DefaultLastHttpContent());
                    HttpUtil.sendOutboundResponse(httpCarbonMessage, response);
                    console.println("error: Error auto-responding to intent verification request: Mode, Topic "
                                            + "and/or callback not specified");
                }
                if ((SUBSCRIBE.equals(mode) || UNSUBSCRIBE.equals(mode))
                        && annotatedTopic.equals(params.get(PARAM_HUB_TOPIC).stringValue())) {
                    String challenge = params.get(PARAM_HUB_CHALLENGE).stringValue();
                    response.addHttpContent(new DefaultLastHttpContent(Unpooled.wrappedBuffer(
                            challenge.getBytes(StandardCharsets.UTF_8))));
                    response.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), TEXT_PLAIN);
                    response.setProperty(HttpConstants.HTTP_STATUS_CODE, HttpResponseStatus.ACCEPTED.code());
                    String intentVerificationMessage = "ballerina: Intent Verification agreed - Mode [" + mode
                            + "], Topic [" + annotatedTopic + "]";
                    if (params.hasKey(PARAM_HUB_LEASE_SECONDS)) {
                        intentVerificationMessage = intentVerificationMessage.concat(", Lease Seconds ["
                                                                                             + params.get(
                                PARAM_HUB_LEASE_SECONDS) + "]");
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
