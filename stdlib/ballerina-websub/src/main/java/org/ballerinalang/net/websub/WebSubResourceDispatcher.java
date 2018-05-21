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

import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpResource;
import org.ballerinalang.net.http.HttpService;
import org.ballerinalang.net.websub.util.WebSubUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import static org.ballerinalang.mime.util.Constants.MESSAGE_DATA_SOURCE;
import static org.ballerinalang.net.http.HttpConstants.HTTP_METHOD;
import static org.ballerinalang.net.http.HttpConstants.HTTP_METHOD_GET;
import static org.ballerinalang.net.http.HttpConstants.HTTP_METHOD_POST;
import static org.ballerinalang.net.http.HttpUtil.extractEntity;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ANNOTATED_TOPIC;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ANN_NAME_WEBSUB_SUBSCRIBER_SERVICE_CONFIG;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ANN_WEBSUB_ATTR_TOPIC;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.DEFERRED_FOR_PAYLOAD_BASED_DISPATCHING;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ENTITY_ACCESSED_REQUEST;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.RESOURCE_NAME_ON_INTENT_VERIFICATION;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.RESOURCE_NAME_ON_NOTIFICATION;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.TOPIC_ID_HEADER;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.TOPIC_ID_PAYLOAD_KEY;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_PACKAGE;
import static org.wso2.transport.http.netty.common.Constants.HTTP_RESOURCE;

/**
 * Resource dispatcher specific for WebSub subscriber services.
 *
 * @since 0.965.0
 */
class WebSubResourceDispatcher {

    static HttpResource findResource(HttpService service, HTTPCarbonMessage inboundRequest,
                                     WebSubServicesRegistry servicesRegistry)
            throws BallerinaConnectorException, ServerConnectorException {

        String method = (String) inboundRequest.getProperty(HTTP_METHOD);
        HttpResource httpResource = null;
        String resourceName;

        if (TOPIC_ID_HEADER.equals(servicesRegistry.getTopicIdentifier()) && HTTP_METHOD_POST.equals(method)) {
            String topic = inboundRequest.getHeader(servicesRegistry.getTopicHeader());
            BMap<String, BString> topicResourceMapForHeader = servicesRegistry.getTopicResourceMap()
                                                                .get(TOPIC_ID_HEADER);
            resourceName = retrieveResourceName(topic, topicResourceMapForHeader);
        } else if (servicesRegistry.getTopicIdentifier() != null && HTTP_METHOD_POST.equals(method)) {
            if (inboundRequest.getProperty(HTTP_RESOURCE) == null) {
                inboundRequest.setProperty(HTTP_RESOURCE, DEFERRED_FOR_PAYLOAD_BASED_DISPATCHING);
                return null;
            }
            ProgramFile programFile = service.getBalService().getServiceInfo().getPackageInfo().getProgramFile();
            if (servicesRegistry.getTopicIdentifier().equals(TOPIC_ID_PAYLOAD_KEY)) {
                resourceName = retrieveResourceName(programFile, inboundRequest, servicesRegistry.getTopicPayloadKeys(),
                                                    servicesRegistry.getTopicResourceMap());
            } else {
                String topic = inboundRequest.getHeader(servicesRegistry.getTopicHeader());
                resourceName = retrieveResourceName(programFile, inboundRequest, topic,
                                                    servicesRegistry.getTopicPayloadKeys(),
                                                    servicesRegistry.getTopicResourceMap());
            }
        } else {
            resourceName = retrieveResourceName(method);
        }

        for (HttpResource resource : service.getResources()) {
            if (resource.getName().equals(resourceName)) {
                httpResource = resource;
                break;
            }
        }

        if (httpResource == null) {
            if (RESOURCE_NAME_ON_INTENT_VERIFICATION.equals(resourceName)) {
                //if the request is a GET request indicating an intent verification request, and the user has not
                //specified an onIntentVerification resource, assume auto intent verification
                String annotatedTopic = (service.getBalService())
                        .getAnnotationList(WEBSUB_PACKAGE, ANN_NAME_WEBSUB_SUBSCRIBER_SERVICE_CONFIG)
                        .get(0).getValue().getStringField(ANN_WEBSUB_ATTR_TOPIC);
                if (annotatedTopic.isEmpty() && service instanceof WebSubHttpService) {
                    annotatedTopic = ((WebSubHttpService) service).getTopic();
                }
                inboundRequest.setProperty(ANNOTATED_TOPIC, annotatedTopic);
                inboundRequest.setProperty(HTTP_RESOURCE, ANNOTATED_TOPIC);
            } else {
                inboundRequest.setProperty(HttpConstants.HTTP_STATUS_CODE, 404);
                throw new BallerinaConnectorException("no matching WebSub Subscriber service  resource " + resourceName
                                                              + " found for method : " + method);
            }
        }
        return httpResource;
    }

    /**
     * Method to retrieve resource names for default WebSub subscriber services.
     *
     * @param method    the method of the received request
     * @return          {@link WebSubSubscriberConstants#RESOURCE_NAME_ON_INTENT_VERIFICATION} if the method is GET,
     *                  {@link WebSubSubscriberConstants#RESOURCE_NAME_ON_NOTIFICATION} if the method is POST
     * @throws BallerinaConnectorException for any method other than GET or POST
     */
    private static String retrieveResourceName(String method) {
        switch (method) {
            case HTTP_METHOD_POST:
                return RESOURCE_NAME_ON_NOTIFICATION;
            case HTTP_METHOD_GET:
                return RESOURCE_NAME_ON_INTENT_VERIFICATION;
            default:
                throw new BallerinaConnectorException("method not allowed for WebSub Subscriber Services : " + method);
        }
    }

    /**
     * Method to retrieve the resource name when the mapping between topic and resource for custom subscriber services
     * is specified as a combination of a header and a key of the JSON payload.
     *
     * @param programFile       the program file representing the Ballerina Program
     * @param inboundRequest    the request received
     * @param topicHeader       the part of the topic specified in the header defined in the custom service
     * @param payloadKeys       the keys of the payload containing elements of the topic, defined in the custom service
     * @param topicResourceMap  the mapping between the topics defined as header + payload key and resources
     * @return                  the name of the resource as identified based on the topic
     * @throws BallerinaConnectorException if a resource could not be mapped to the topic identified
     */
    private static String retrieveResourceName(ProgramFile programFile, HTTPCarbonMessage inboundRequest,
                   String topicHeader, BStringArray payloadKeys, BMap<String, BMap<String, BString>> topicResourceMap) {
        String topicHeaderPrefix = topicHeader + "::";
        BValue httpRequest = WebSubUtils.getHttpRequest(programFile, inboundRequest);
        BJSON jsonBody = retrieveJsonBody(httpRequest);
        inboundRequest.setProperty(ENTITY_ACCESSED_REQUEST, httpRequest);
        for (String key : payloadKeys.getStringArray()) {
            if (jsonBody.value().has(key)) {
                BMap<String, BString> topicResourceMapForValue = topicResourceMap.get(key);
                String valueForKey = jsonBody.value().get(key).stringValue();
                String topic = topicHeaderPrefix + valueForKey;
                if (topicResourceMapForValue.hasKey(topic)) {
                    return retrieveResourceName(topic, topicResourceMapForValue);
                }
            }
        }
        if (topicResourceMap.hasKey(TOPIC_ID_HEADER)) {
            BMap<String, BString> topicResourceMapForHeader = topicResourceMap.get(TOPIC_ID_HEADER);
            if (topicResourceMapForHeader.hasKey(topicHeader)) {
                return retrieveResourceName(topicHeader, topicResourceMapForHeader);
            }
        }
        throw new BallerinaConnectorException("Matching resource not found for dispatching based on Header and "
                                                      + "Payload Key");
    }

    /**
     * Method to retrieve the resource name when the mapping between topic and resource for custom subscriber services
     * is specified as a key of the JSON payload.
     *
     * @param programFile       the program file representing the Ballerina Program
     * @param inboundRequest    the request received
     * @param payloadKeys       the keys of the payload containing elements of the topic, defined in the custom service
     * @param topicResourceMap  the mapping between the topics defined as a value of a payload key and resources
     * @return                  the name of the resource as identified based on the topic
     * @throws BallerinaConnectorException if a resource could not be mapped to the topic identified
     */
    private static String retrieveResourceName(ProgramFile programFile, HTTPCarbonMessage inboundRequest,
                                       BStringArray payloadKeys, BMap<String, BMap<String, BString>> topicResourceMap) {
        BValue httpRequest = WebSubUtils.getHttpRequest(programFile, inboundRequest);
        BJSON jsonBody = retrieveJsonBody(httpRequest);
        inboundRequest.setProperty(ENTITY_ACCESSED_REQUEST, httpRequest);
        for (String key : payloadKeys.getStringArray()) {
            if (jsonBody.value().has(key)) {
                BMap<String, BString> topicResourceMapForValue = topicResourceMap.get(key);
                String valueForKey = jsonBody.value().get(key).stringValue();
                if (topicResourceMapForValue.hasKey(valueForKey)) {
                    return retrieveResourceName(valueForKey, topicResourceMapForValue);
                }
            }
        }
        throw new BallerinaConnectorException("Matching resource not found for dispatching based on Payload Key");
    }

    /**
     * Method to retrieve the JSON body for a request received, to identify topic elements specified in the payload.
     *
     * @param httpRequest   the request received
     * @return              the retrieved JSON representation
     * @throws BallerinaConnectorException if an error occurs retrieving the payload, or the payload is not JSON
     */
    private static BJSON retrieveJsonBody(BValue httpRequest) {
        BStruct entityStruct = extractEntity((BStruct) httpRequest);
        if (entityStruct != null) {
            if (entityStruct.getNativeData(MESSAGE_DATA_SOURCE) instanceof BJSON) {
                return (BJSON) (entityStruct.getNativeData(MESSAGE_DATA_SOURCE));
            } else {
                throw new BallerinaConnectorException("Non-JSON payload received for payload key based dispatching");
            }
        } else {
            throw new BallerinaConnectorException("Error retrieving payload for payload key based dispatching");
        }
    }

    /**
     * Method to retrieve the resource name from the topic -- resource map for a topic.
     *
     * @param topic             the topic for which the resource needs to be identified
     * @param topicResourceMap  the mapping between the topics and resources
     * @return                  the name of the resource as identified based on the topic
     * @throws BallerinaConnectorException if a resource could not be mapped to the topic
     */
    private static String retrieveResourceName(String topic, BMap<String, BString> topicResourceMap) {
        if (topicResourceMap.get(topic) != null) {
            return topicResourceMap.get(topic).stringValue();
        } else {
            throw new BallerinaConnectorException("resource not specified for topic : " + topic);
        }
    }

}
