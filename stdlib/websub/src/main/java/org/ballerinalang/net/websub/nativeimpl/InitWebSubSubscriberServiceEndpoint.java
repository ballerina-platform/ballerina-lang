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

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BTypeDescValue;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.WebSocketServicesRegistry;
import org.ballerinalang.net.websub.WebSubServicesRegistry;

import java.util.HashMap;

import static org.ballerinalang.net.websub.WebSubSubscriberConstants.EXTENSION_CONFIG_HEADER_AND_PAYLOAD_KEY_RESOURCE_MAP;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.EXTENSION_CONFIG_HEADER_RESOURCE_MAP;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.EXTENSION_CONFIG_PAYLOAD_KEY_RESOURCE_MAP;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.EXTENSION_CONFIG_TOPIC_HEADER;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.EXTENSION_CONFIG_TOPIC_IDENTIFIER;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.LISTENER_SERVICE_ENDPOINT_CONFIG;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.RESOURCE_NAME_ON_INTENT_VERIFICATION;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.RESOURCE_NAME_ON_NOTIFICATION;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.SERVICE_CONFIG_EXTENSION_CONFIG;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.STRUCT_WEBSUB_INTENT_VERIFICATION_REQUEST;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.STRUCT_WEBSUB_NOTIFICATION_REQUEST;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.TOPIC_ID_HEADER;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.TOPIC_ID_HEADER_AND_PAYLOAD;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.TOPIC_ID_PAYLOAD_KEY;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_HTTP_ENDPOINT;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_PACKAGE;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_SERVICE_REGISTRY;

/**
 * Initialize the WebSub subscriber endpoint.
 *
 * @since 0.965.0
 */

@BallerinaFunction(
        orgName = "ballerina", packageName = "websub",
        functionName = "initWebSubSubscriberServiceEndpoint",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Listener", structPackage = WEBSUB_PACKAGE)
)
public class InitWebSubSubscriberServiceEndpoint extends BlockingNativeCallableUnit {

    @SuppressWarnings("unchecked")
    @Override
    public void execute(Context context) {

        Struct subscriberServiceEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
        Struct serviceEndpoint = ((subscriberServiceEndpoint).getRefField(WEBSUB_HTTP_ENDPOINT).getStructValue());
        BMap<String, BValue> listener = (BMap<String, BValue>) context.getRefArgument(0);
        BMap<String, BValue> config = (BMap<String, BValue>) listener.get(LISTENER_SERVICE_ENDPOINT_CONFIG);

        WebSubServicesRegistry webSubServicesRegistry;

        BMap<String, BValue> extensionConfig = (BMap<String, BValue>) config.get(SERVICE_CONFIG_EXTENSION_CONFIG);
        if (extensionConfig == null) {
             webSubServicesRegistry = new WebSubServicesRegistry(new WebSocketServicesRegistry());
        } else {
            String topicIdentifier = extensionConfig.get(EXTENSION_CONFIG_TOPIC_IDENTIFIER).stringValue();
            BString topicHeader = null;
            BMap<String, BValue> headerResourceMap = null;
            BMap<String, BMap<String, BValue>> payloadKeyResourceMap = null;
            BMap<String, BMap<String, BMap<String, BValue>>> headerAndPayloadKeyResourceMap = null;

            if (TOPIC_ID_HEADER.equals(topicIdentifier) || TOPIC_ID_HEADER_AND_PAYLOAD.equals(topicIdentifier)) {
                topicHeader = (BString) extensionConfig.get(EXTENSION_CONFIG_TOPIC_HEADER);
                if (topicHeader == null) {
                    throw new BallerinaConnectorException("Topic Header not specified to dispatch by "
                                                                  + topicIdentifier);
                }
            }

            if (TOPIC_ID_HEADER.equals(topicIdentifier)) {
                headerResourceMap = (BMap<String, BValue>) extensionConfig.get(EXTENSION_CONFIG_HEADER_RESOURCE_MAP);
                if (headerResourceMap == null) {
                    throw new BallerinaConnectorException("Resource map not specified to dispatch by header");
                }
            } else if (TOPIC_ID_HEADER_AND_PAYLOAD.equals(topicIdentifier)) {
                headerAndPayloadKeyResourceMap = (BMap<String, BMap<String, BMap<String, BValue>>>)
                        extensionConfig.get(EXTENSION_CONFIG_HEADER_AND_PAYLOAD_KEY_RESOURCE_MAP);
                if (headerAndPayloadKeyResourceMap == null) {
                    throw new BallerinaConnectorException("Resource map not specified to dispatch by header and "
                                                                  + "payload");
                }
                headerResourceMap = (BMap<String, BValue>) extensionConfig.get(
                                                            EXTENSION_CONFIG_HEADER_RESOURCE_MAP);
                payloadKeyResourceMap = (BMap<String, BMap<String, BValue>>) extensionConfig.get(
                                                            EXTENSION_CONFIG_PAYLOAD_KEY_RESOURCE_MAP);
            } else {
                payloadKeyResourceMap = (BMap<String, BMap<String, BValue>>) extensionConfig.get(
                                                            EXTENSION_CONFIG_PAYLOAD_KEY_RESOURCE_MAP);
                if (payloadKeyResourceMap == null) {
                    throw new BallerinaConnectorException("Resource map not specified to dispatch by payload");
                }
            }
            HashMap<String, String[]> resourceDetails = buildResourceDetailsMap(topicIdentifier, headerResourceMap,
                                                                                payloadKeyResourceMap,
                                                                                headerAndPayloadKeyResourceMap);
            webSubServicesRegistry = new WebSubServicesRegistry(new WebSocketServicesRegistry(), topicIdentifier,
                                                                topicHeader == null ? null : topicHeader.stringValue(),
                                                                headerResourceMap, payloadKeyResourceMap,
                                                                headerAndPayloadKeyResourceMap, resourceDetails);
        }

        serviceEndpoint.addNativeData(WEBSUB_SERVICE_REGISTRY, webSubServicesRegistry);
        context.setReturnValues();
    }

    private HashMap<String, String[]> buildResourceDetailsMap(String topicIdentifier,
                                                              BMap<String, BValue> headerResourceMap,
                                                              BMap<String, BMap<String, BValue>> payloadKeyResourceMap,
                                                              BMap<String, BMap<String, BMap<String, BValue>>>
                                                                      headerAndPayloadKeyResourceMap) {
        //Map with resource details where the key is the resource name and the value is the param
        HashMap<String, String[]> resourceDetails = new HashMap<>();
        resourceDetails.put(RESOURCE_NAME_ON_INTENT_VERIFICATION,
                            new String[]{WEBSUB_PACKAGE, STRUCT_WEBSUB_INTENT_VERIFICATION_REQUEST});
        resourceDetails.put(RESOURCE_NAME_ON_NOTIFICATION,
                            new String[]{WEBSUB_PACKAGE, STRUCT_WEBSUB_NOTIFICATION_REQUEST});

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

    private static void populateResourceDetailsByHeader(BMap<String, BValue> headerResourceMap,
                                                        HashMap<String, String[]> resourceDetails) {
        headerResourceMap.getMap().values().forEach(value -> populateResourceDetails(resourceDetails,
                                                                                     (BRefValueArray) value));
    }

    private static void populateResourceDetailsByPayload(BMap<String, BMap<String, BValue>> payloadKeyResourceMap,
                                                         HashMap<String, String[]> resourceDetails) {
        payloadKeyResourceMap.getMap().values().forEach(mapByKey -> {
            mapByKey.getMap().values().forEach(value -> populateResourceDetails(resourceDetails,
                                                                                (BRefValueArray) value));
        });
    }

    private static void populateResourceDetailsByHeaderAndPayload(BMap<String, BMap<String, BMap<String, BValue>>>
                                                                          headerAndPayloadKeyResourceMap,
                                                                  HashMap<String, String[]> resourceDetails) {
        headerAndPayloadKeyResourceMap.getMap().values().forEach(mapByHeader -> {
            mapByHeader.getMap().values().forEach(mapByKey -> {
                mapByKey.getMap().values().forEach(value -> populateResourceDetails(resourceDetails,
                                                                                    (BRefValueArray) value));
            });
        });
    }

    private static void populateResourceDetails(HashMap<String, String[]> resourceDetails,
                                                BRefValueArray resourceDetailTuple) {
        String resourceName = resourceDetailTuple.getBValue(0).stringValue();
        BStructureType paramDetails =
                (BStructureType) ((BTypeDescValue) (resourceDetailTuple).getBValue(1)).value();
        resourceDetails.put(resourceName,
                            new String[]{paramDetails.getPackagePath(), paramDetails.getName()});
    }
}
