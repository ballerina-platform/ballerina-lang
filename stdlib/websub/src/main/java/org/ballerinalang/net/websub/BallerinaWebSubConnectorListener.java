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
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.exceptions.BallerinaConnectorException;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.MapValueImpl;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.jvm.values.connector.CallableUnitCallback;
import org.ballerinalang.jvm.values.connector.Executor;
import org.ballerinalang.langlib.value.CloneWithType;
import org.ballerinalang.net.http.BallerinaHTTPConnectorListener;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpResource;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.net.uri.URIUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.ballerinalang.mime.util.MimeConstants.TEXT_PLAIN;
import static org.ballerinalang.net.http.HttpConstants.CALLER;
import static org.ballerinalang.net.http.HttpConstants.HTTP_LISTENER_ENDPOINT;
import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_HTTP_PKG_ID;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ANNOTATED_TOPIC;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.BALLERINA;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ENTITY_ACCESSED_REQUEST;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.GENERATED_PACKAGE_VERSION;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.PARAM_HUB_CHALLENGE;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.PARAM_HUB_LEASE_SECONDS;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.PARAM_HUB_MODE;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.PARAM_HUB_TOPIC;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.REQUEST;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.RESOURCE_NAME_ON_INTENT_VERIFICATION;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.RESOURCE_NAME_ON_NOTIFICATION;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.SUBSCRIBE;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.UNSUBSCRIBE;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.VERIFICATION_REQUEST_CHALLENGE;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.VERIFICATION_REQUEST_LEASE_SECONDS;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.VERIFICATION_REQUEST_MODE;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.VERIFICATION_REQUEST_TOPIC;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_INTENT_VERIFICATION_REQUEST;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_NOTIFICATION_REQUEST;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_PACKAGE_ID;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_SERVICE_CALLER;
import static org.ballerinalang.net.websub.WebSubUtils.getHttpRequest;
import static org.ballerinalang.net.websub.WebSubUtils.getJsonBody;

/**
 * HTTP Connection Listener for Ballerina WebSub services.
 */
public class BallerinaWebSubConnectorListener extends BallerinaHTTPConnectorListener {

    private static final Logger log = LoggerFactory.getLogger(BallerinaWebSubConnectorListener.class);
    private WebSubServicesRegistry webSubServicesRegistry;
    private PrintStream console = System.out;
    private Scheduler scheduler;

    public BallerinaWebSubConnectorListener(Strand strand, WebSubServicesRegistry webSubServicesRegistry,
                                            MapValue endpointConfig) {
        super(webSubServicesRegistry, endpointConfig);
        this.scheduler = strand.scheduler;
        this.webSubServicesRegistry = webSubServicesRegistry;
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
        int paramIndex = 0;
        ObjectValue httpRequest;
        if (httpCarbonMessage.getProperty(ENTITY_ACCESSED_REQUEST) != null) {
            httpRequest = (ObjectValue) httpCarbonMessage.getProperty(ENTITY_ACCESSED_REQUEST);
        } else {
            httpRequest = getHttpRequest(httpCarbonMessage);
        }

        AttachedFunction balResource = httpResource.getBalResource();
        List<BType> paramTypes = httpResource.getParamTypes();
        Object[] signatureParams = new Object[paramTypes.size() * 2];
        String resourceName = httpResource.getName();
        if (RESOURCE_NAME_ON_INTENT_VERIFICATION.equals(resourceName)) {
            signatureParams[paramIndex++] = getWebSubCaller(httpResource, httpCarbonMessage, endpointConfig);
            signatureParams[paramIndex++] = true;
            ObjectValue intentVerificationRequest = createIntentVerificationRequest();
            if (httpCarbonMessage.getProperty(HttpConstants.QUERY_STR) != null) {
                String queryString = (String) httpCarbonMessage.getProperty(HttpConstants.QUERY_STR);
                MapValue<BString, Object> params = new MapValueImpl<>();
                try {
                    URIUtil.populateQueryParamMap(queryString, params);
                    intentVerificationRequest.set(StringUtils.fromString(VERIFICATION_REQUEST_MODE),
                                                  getParamStringValue(params, PARAM_HUB_MODE));
                    intentVerificationRequest.set(StringUtils.fromString(VERIFICATION_REQUEST_TOPIC),
                                                  getParamStringValue(params, PARAM_HUB_TOPIC));
                    intentVerificationRequest.set(StringUtils.fromString(VERIFICATION_REQUEST_CHALLENGE),
                                                  getParamStringValue(params, PARAM_HUB_CHALLENGE));
                    if (params.containsKey(PARAM_HUB_LEASE_SECONDS)) {
                        long leaseSec = Long.parseLong(getParamStringValue(params, PARAM_HUB_LEASE_SECONDS).getValue());
                        intentVerificationRequest.set(StringUtils.fromString(VERIFICATION_REQUEST_LEASE_SECONDS),
                                                      leaseSec);
                    }
                } catch (UnsupportedEncodingException e) {
                    log.error("Error populating query map for intent verification request received: "
                                      + e.getMessage());
                    HttpCarbonMessage response = HttpUtil.createHttpCarbonMessage(false);
                    response.waitAndReleaseAllEntities();
                    response.setHttpStatusCode(HttpResponseStatus.NOT_FOUND.code());
                    response.addHttpContent(new DefaultLastHttpContent());
                    HttpUtil.sendOutboundResponse(httpCarbonMessage, response);
                    return;
                }
            }
            intentVerificationRequest.set(StringUtils.fromString(REQUEST), httpRequest);
            signatureParams[paramIndex++] = intentVerificationRequest;
            signatureParams[paramIndex] = true;
        } else { //Notification Resource
            //validate signature for requests received at the callback
            validateSignature(httpCarbonMessage, httpResource, httpRequest);

            HttpCarbonMessage response = HttpUtil.createHttpCarbonMessage(false);
            response.waitAndReleaseAllEntities();
            response.setHttpStatusCode(HttpResponseStatus.ACCEPTED.code());
            response.addHttpContent(new DefaultLastHttpContent());
            HttpUtil.sendOutboundResponse(httpCarbonMessage, response);

            signatureParams[paramIndex++] = createNotification(httpRequest);
            signatureParams[paramIndex++] = true;
            if (!RESOURCE_NAME_ON_NOTIFICATION.equals(balResource.getName())) {
                Object customRecordOrError = createCustomNotification(httpCarbonMessage, balResource, httpRequest);
                if (TypeChecker.getType(customRecordOrError).getTag() == TypeTags.ERROR_TAG) {
                    log.error("Data binding failed: " + ((ErrorValue) customRecordOrError).getPrintableStackTrace());
                    return;
                }

                signatureParams[paramIndex++] = customRecordOrError;
                signatureParams[paramIndex] = true;
            }
        }

        CallableUnitCallback callback = new WebSubEmptyCallableUnitCallback();
        //TODO handle BallerinaConnectorException
        ObjectValue service = httpResource.getParentService().getBalService();
        Executor.submit(scheduler, service, balResource.getName(), callback, null,
                        signatureParams);
    }

    @SuppressWarnings("unchecked")
    private void validateSignature(HttpCarbonMessage httpCarbonMessage, HttpResource httpResource,
                                   ObjectValue request) {
        //invoke processWebSubNotification function
        Object returnValue;
        try {
            Object[] args = {request, httpResource.getParentService().getBalService()};
            returnValue = Executor.executeFunction(scheduler, this.getClass().getClassLoader(), BALLERINA,
                                                   WEBSUB, GENERATED_PACKAGE_VERSION, "commons",
                                                   "processWebSubNotification", args);
        } catch (BallerinaException ex) {
            log.debug("Signature Validation failed: " + ex.getMessage());
            httpCarbonMessage.setHttpStatusCode(404);
            throw ex;
        }
        ErrorValue error = (ErrorValue) returnValue;
        if (error != null) {
            log.debug("Signature Validation failed for Notification: " + error.getMessage());
            httpCarbonMessage.setHttpStatusCode(404);
            throw new BallerinaException("validation failed for notification");
        }
    }

    /**
     * Method to retrieve the struct representing the WebSub subscriber service endpoint.
     *
     * @param httpResource      the resource of the service receiving the request
     * @param httpCarbonMessage the HTTP message representing the request received
     * @param endpointConfig    listener endpoint configuration
     * @return the struct representing the subscriber service endpoint
     */
    private ObjectValue getWebSubCaller(HttpResource httpResource, HttpCarbonMessage httpCarbonMessage,
                                        MapValue endpointConfig) {
        ObjectValue httpServiceServer = BallerinaValues.createObjectValue(PROTOCOL_HTTP_PKG_ID, HTTP_LISTENER_ENDPOINT,
                          9090, endpointConfig); // sending a dummy port here as it gets initialized later - fix
        ObjectValue httpCaller = BallerinaValues.createObjectValue(PROTOCOL_HTTP_PKG_ID, CALLER);

        HttpUtil.enrichHttpCallerWithConnectionInfo(httpCaller, httpCarbonMessage, httpResource, endpointConfig);
        HttpUtil.enrichHttpCallerWithNativeData(httpCaller, httpCarbonMessage, endpointConfig);
        httpServiceServer.addNativeData(HttpConstants.SERVICE_ENDPOINT_CONNECTION_FIELD, httpCaller);
        return BallerinaValues.createObjectValue(WEBSUB_PACKAGE_ID, WEBSUB_SERVICE_CALLER, httpCaller);
    }

    /**
     * Method to create the intent verification request struct representing a subscription/unsubscription intent
     * verification request received.
     */
    private ObjectValue createIntentVerificationRequest() {
        return BallerinaValues.createObjectValue(WEBSUB_PACKAGE_ID, WEBSUB_INTENT_VERIFICATION_REQUEST);
    }

    /**
     * Method to create the notification request representing WebSub notifications received.
     */
    private ObjectValue createNotification(ObjectValue httpRequest) {
        ObjectValue notification = BallerinaValues.createObjectValue(WEBSUB_PACKAGE_ID, WEBSUB_NOTIFICATION_REQUEST);
        notification.set(StringUtils.fromString(REQUEST), httpRequest);
        return notification;
    }

    /**
     * Method to create the notification request struct representing WebSub notifications received.
     */
    private Object createCustomNotification(HttpCarbonMessage inboundRequest, AttachedFunction resource,
                                              ObjectValue httpRequest) {
        BRecordType recordType = webSubServicesRegistry.getResourceDetails().get(resource.getName());
        MapValue<BString, ?> jsonBody = getJsonBody(httpRequest);
        inboundRequest.setProperty(ENTITY_ACCESSED_REQUEST, httpRequest);
        return CloneWithType.convert(recordType, jsonBody);
    }

    /**
     * Method to automatically respond to intent verification requests for subscriptions/unsubscriptions if a resource
     * named {@link WebSubSubscriberConstants#RESOURCE_NAME_ON_INTENT_VERIFICATION} is not specified.
     *
     * @param httpCarbonMessage the message/request received
     */
    private void autoRespondToIntentVerification(HttpCarbonMessage httpCarbonMessage) {
        HttpCarbonMessage response = HttpUtil.createHttpCarbonMessage(false);
        response.waitAndReleaseAllEntities();

        if (httpCarbonMessage.getProperty(ANNOTATED_TOPIC) == null) {
            console.println("ballerina: Intent Verification denied - expected topic details not found");
            sendIntentVerificiationDenialResponse(httpCarbonMessage, response);
            return;
        }

        if (httpCarbonMessage.getProperty(HttpConstants.QUERY_STR) == null) {
            console.println("ballerina: Intent Verification denied - invalid intent verification request");
            sendIntentVerificiationDenialResponse(httpCarbonMessage, response);
            return;
        }

        String annotatedTopic = httpCarbonMessage.getProperty(ANNOTATED_TOPIC).toString();
        String queryString = (String) httpCarbonMessage.getProperty(HttpConstants.QUERY_STR);
        MapValue<BString, Object> params = new MapValueImpl<>();
        try {
            URIUtil.populateQueryParamMap(queryString, params);
            if (!params.containsKey(PARAM_HUB_MODE) || !params.containsKey(PARAM_HUB_TOPIC) ||
                    !params.containsKey(PARAM_HUB_CHALLENGE)) {
                sendIntentVerificiationDenialResponse(httpCarbonMessage, response);
                console.println("error: Error auto-responding to intent verification request: Mode, Topic "
                                        + "and/or challenge not specified");
                return;
            }

            BString mode = getParamStringValue(params, PARAM_HUB_MODE);
            if ((SUBSCRIBE.equals(mode) || UNSUBSCRIBE.equals(mode))
                    && annotatedTopic.equals(getParamStringValue(params, PARAM_HUB_TOPIC).getValue())) {
                BString challenge = getParamStringValue(params, PARAM_HUB_CHALLENGE);
                response.addHttpContent(new DefaultLastHttpContent(Unpooled.wrappedBuffer(
                        challenge.getValue().getBytes(StandardCharsets.UTF_8))));
                response.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), TEXT_PLAIN);
                response.setHttpStatusCode(HttpResponseStatus.ACCEPTED.code());
                String intentVerificationMessage = "ballerina: Intent Verification agreed - Mode [" + mode
                        + "], Topic [" + annotatedTopic + "]";
                if (params.containsKey(PARAM_HUB_LEASE_SECONDS)) {
                    intentVerificationMessage = intentVerificationMessage.concat(
                            ", Lease Seconds [" + getParamStringValue(params, PARAM_HUB_LEASE_SECONDS) + "]");
                }
                console.println(intentVerificationMessage);
                HttpUtil.sendOutboundResponse(httpCarbonMessage, response);
            } else {
                console.println("ballerina: Intent Verification denied - Mode [" + mode + "], Topic ["
                                        + getParamStringValue(params, PARAM_HUB_TOPIC) + "]");
                sendIntentVerificiationDenialResponse(httpCarbonMessage, response);
            }
        } catch (UnsupportedEncodingException e) {
            console.println("ballerina: Intent Verification denied - error extracting query parameters: " +
                                    e.getMessage());
            sendIntentVerificiationDenialResponse(httpCarbonMessage, response);
        }
    }

    private static void sendIntentVerificiationDenialResponse(HttpCarbonMessage httpCarbonMessage,
                                                              HttpCarbonMessage response) {
        response.setHttpStatusCode(HttpResponseStatus.NOT_FOUND.code());
        response.addHttpContent(new DefaultLastHttpContent());
        HttpUtil.sendOutboundResponse(httpCarbonMessage, response);
    }

    private BString getParamStringValue(MapValue<BString, Object> params, BString key) {
        if (!params.containsKey(key)) {
            return StringUtils.fromString("");
        }
        Object param = params.get(key);
        if (TypeChecker.getType(param).getTag() != TypeTags.ARRAY_TAG || ((ArrayValue) param).size() < 1) {
            return StringUtils.fromString("");
        }
        return StringUtils.fromString(((ArrayValue) param).get(0).toString());
    }
}
