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

package org.ballerinalang.net.websub.nativeimpl;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.util.exceptions.BallerinaConnectorException;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.TypedescValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.websocket.server.WebSocketServicesRegistry;
import org.ballerinalang.net.websub.WebSubServicesRegistry;

import java.util.HashMap;

import static org.ballerinalang.net.websub.WebSubSubscriberConstants
        .EXTENSION_CONFIG_HEADER_AND_PAYLOAD_KEY_RESOURCE_MAP;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.EXTENSION_CONFIG_HEADER_RESOURCE_MAP;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.EXTENSION_CONFIG_PAYLOAD_KEY_RESOURCE_MAP;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.EXTENSION_CONFIG_TOPIC_HEADER;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.EXTENSION_CONFIG_TOPIC_IDENTIFIER;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.LISTENER_SERVICE_ENDPOINT_CONFIG;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.SERVICE_CONFIG_EXTENSION_CONFIG;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.TOPIC_ID_HEADER;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.TOPIC_ID_HEADER_AND_PAYLOAD;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.TOPIC_ID_PAYLOAD_KEY;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_HTTP_ENDPOINT;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_PACKAGE;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_SERVICE_LISTENER;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_SERVICE_REGISTRY;

/**
 * Initialize the WebSub subscriber endpoint.
 *
 * @since 0.965.0
 */

@BallerinaFunction(
        orgName = "ballerina", packageName = "websub",
        functionName = "initWebSubSubscriberServiceEndpoint",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = WEBSUB_SERVICE_LISTENER,
                structPackage = WEBSUB_PACKAGE)
)
public class InitWebSubSubscriberServiceEndpoint {

    @SuppressWarnings("unchecked")
    public static void initWebSubSubscriberServiceEndpoint(Strand strand, ObjectValue subscriberServiceEndpoint) {

        ObjectValue serviceEndpoint = (ObjectValue) subscriberServiceEndpoint.get(WEBSUB_HTTP_ENDPOINT);
        MapValue<String, Object> config = (MapValue<String, Object>) subscriberServiceEndpoint.get(
                LISTENER_SERVICE_ENDPOINT_CONFIG);
        WebSubServicesRegistry webSubServicesRegistry;

        if (config == null || config.get(SERVICE_CONFIG_EXTENSION_CONFIG) == null) {
            webSubServicesRegistry = new WebSubServicesRegistry(new WebSocketServicesRegistry());
        } else {
            MapValue<String, Object> extensionConfig = (MapValue<String, Object>) config.get(
                    SERVICE_CONFIG_EXTENSION_CONFIG);
            String topicIdentifier = extensionConfig.getStringValue(EXTENSION_CONFIG_TOPIC_IDENTIFIER);
            String topicHeader = null;
            MapValue<String, Object> headerResourceMap = null;
            MapValue<String, MapValue<String, Object>> payloadKeyResourceMap = null;
            MapValue<String, MapValue<String, MapValue<String, Object>>> headerAndPayloadKeyResourceMap = null;

            if (TOPIC_ID_HEADER.equals(topicIdentifier) || TOPIC_ID_HEADER_AND_PAYLOAD.equals(topicIdentifier)) {
                topicHeader = extensionConfig.getStringValue(EXTENSION_CONFIG_TOPIC_HEADER);
                if (topicHeader == null) {
                    throw new BallerinaConnectorException("Topic Header not specified to dispatch by "
                                                                  + topicIdentifier);
                }
            }

            if (TOPIC_ID_HEADER.equals(topicIdentifier)) {
                headerResourceMap = (MapValue<String, Object>) extensionConfig.get(
                        EXTENSION_CONFIG_HEADER_RESOURCE_MAP);
                if (headerResourceMap == null) {
                    throw new BallerinaConnectorException("Resource map not specified to dispatch by header");
                }
            } else if (TOPIC_ID_HEADER_AND_PAYLOAD.equals(topicIdentifier)) {
                headerAndPayloadKeyResourceMap = (MapValue<String, MapValue<String, MapValue<String, Object>>>)
                        extensionConfig.get(EXTENSION_CONFIG_HEADER_AND_PAYLOAD_KEY_RESOURCE_MAP);
                if (headerAndPayloadKeyResourceMap == null) {
                    throw new BallerinaConnectorException("Resource map not specified to dispatch by header and "
                                                                  + "payload");
                }
                headerResourceMap = (MapValue<String, Object>) extensionConfig.get(
                        EXTENSION_CONFIG_HEADER_RESOURCE_MAP);
                payloadKeyResourceMap = (MapValue<String, MapValue<String, Object>>) extensionConfig.get(
                        EXTENSION_CONFIG_PAYLOAD_KEY_RESOURCE_MAP);
            } else {
                payloadKeyResourceMap = (MapValue<String, MapValue<String, Object>>) extensionConfig.get(
                        EXTENSION_CONFIG_PAYLOAD_KEY_RESOURCE_MAP);
                if (payloadKeyResourceMap == null) {
                    throw new BallerinaConnectorException("Resource map not specified to dispatch by payload");
                }
            }
            HashMap<String, BRecordType> resourceDetails = buildResourceDetailsMap(topicIdentifier, headerResourceMap,
                                                                                   payloadKeyResourceMap,
                                                                                   headerAndPayloadKeyResourceMap);
            webSubServicesRegistry = new WebSubServicesRegistry(new WebSocketServicesRegistry(), topicIdentifier,
                                                                topicHeader, headerResourceMap, payloadKeyResourceMap,
                                                                headerAndPayloadKeyResourceMap, resourceDetails);
        }

        serviceEndpoint.addNativeData(WEBSUB_SERVICE_REGISTRY, webSubServicesRegistry);
    }

    private static HashMap<String, BRecordType> buildResourceDetailsMap(String topicIdentifier,
                                                                        MapValue<String, Object> headerResourceMap,
                                                                        MapValue<String, MapValue<String, Object>>
                                                                                payloadKeyResourceMap,
                                                                        MapValue<String, MapValue<String,
                                                                                MapValue<String, Object>>>
                                                                                headerAndPayloadKeyResourceMap) {
        // Map with resource details where the key is the resource name and the value is the param
        HashMap<String, BRecordType> resourceDetails = new HashMap<>();
        if (topicIdentifier != null) {
            switch (topicIdentifier) {
                case TOPIC_ID_HEADER:
                    populateResourceDetailsByHeader(headerResourceMap, resourceDetails);
                    break;
                case TOPIC_ID_PAYLOAD_KEY:
                    populateResourceDetailsByPayload(payloadKeyResourceMap, resourceDetails);
                    break;
                default:
                    populateResourceDetailsByHeaderAndPayload(headerAndPayloadKeyResourceMap, resourceDetails);
                    if (headerResourceMap != null) {
                        populateResourceDetailsByHeader(headerResourceMap, resourceDetails);
                    }
                    if (payloadKeyResourceMap != null) {
                        populateResourceDetailsByPayload(payloadKeyResourceMap, resourceDetails);
                    }
                    break;
            }
        }
        return resourceDetails;
    }

    private static void populateResourceDetailsByHeader(MapValue<String, Object> headerResourceMap,
                                                        HashMap<String, BRecordType> resourceDetails) {
        headerResourceMap.values().forEach(value -> populateResourceDetails(resourceDetails, (ArrayValue) value));
    }

    private static void populateResourceDetailsByPayload(
            MapValue<String, MapValue<String, Object>> payloadKeyResourceMap,
            HashMap<String, BRecordType> resourceDetails) {
        payloadKeyResourceMap.values().forEach(mapByKey -> {
            mapByKey.values().forEach(value -> populateResourceDetails(resourceDetails, (ArrayValue) value));
        });
    }

    private static void populateResourceDetailsByHeaderAndPayload(
            MapValue<String, MapValue<String, MapValue<String, Object>>> headerAndPayloadKeyResourceMap,
            HashMap<String, BRecordType> resourceDetails) {
        headerAndPayloadKeyResourceMap.values().forEach(mapByHeader -> {
            mapByHeader.values().forEach(mapByKey -> {
                mapByKey.values().forEach(value -> populateResourceDetails(resourceDetails, (ArrayValue) value));
            });
        });
    }

    private static void populateResourceDetails(HashMap<String, BRecordType> resourceDetails,
                                                ArrayValue resourceDetailTuple) {
        String resourceName = resourceDetailTuple.getRefValue(0).toString();
        resourceDetails.put(resourceName,
                            (BRecordType) ((TypedescValue) resourceDetailTuple.getRefValue(1)).getDescribingType());
    }
}
