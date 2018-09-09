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

import org.ballerinalang.connector.api.Service;
import org.ballerinalang.model.types.BStructureType;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BTypeDescValue;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.HTTPServicesRegistry;
import org.ballerinalang.net.http.HttpService;
import org.ballerinalang.net.http.WebSocketServicesRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.ballerinalang.net.websub.WebSubSubscriberConstants.RESOURCE_NAME_ON_INTENT_VERIFICATION;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.RESOURCE_NAME_ON_NOTIFICATION;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.STRUCT_WEBSUB_INTENT_VERIFICATION_REQUEST;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.STRUCT_WEBSUB_NOTIFICATION_REQUEST;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.TOPIC_ID_HEADER;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.TOPIC_ID_PAYLOAD_KEY;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_PACKAGE;
import static org.ballerinalang.net.websub.WebSubSubscriberServiceValidator.validateCustomResources;

/**
 * The WebSub service registry which uses an {@link HTTPServicesRegistry} to maintain WebSub Subscriber HTTP services.
 *
 * @since 0.965.0
 */
public class WebSubServicesRegistry extends HTTPServicesRegistry {

    private static final Logger logger = LoggerFactory.getLogger(WebSubServicesRegistry.class);

    private String topicIdentifier;
    private String topicHeader;

    private BMap<String, BValue> headerResourceMap;
    private BMap<String, BMap<String, BValue>> payloadKeyResourceMap;
    private BMap<String, BMap<String, BMap<String, BValue>>> headerAndPayloadKeyResourceMap;

    private HashMap<String, String[]> resourceDetails;

    public WebSubServicesRegistry(WebSocketServicesRegistry webSocketServicesRegistry) {
        super(webSocketServicesRegistry);
    }

    public WebSubServicesRegistry(WebSocketServicesRegistry webSocketServicesRegistry,
                                  String topicIdentifier, String topicHeader, BMap<String, BValue> headerResourceMap,
                                  BMap<String, BMap<String, BValue>> payloadKeyResourceMap,
                                  BMap<String, BMap<String, BMap<String, BValue>>> headerAndPayloadKeyResourceMap) {
        super(webSocketServicesRegistry);
        this.topicIdentifier = topicIdentifier;
        this.topicHeader = topicHeader;
        this.headerResourceMap = headerResourceMap;
        this.payloadKeyResourceMap = payloadKeyResourceMap;
        this.headerAndPayloadKeyResourceMap = headerAndPayloadKeyResourceMap;
        populateResourceDetails();
    }

    /**
     * Method to retrieve the identifier for topics based on which dispatching would happen for custom subscriber
     * services.
     *
     * @return the identifier for topics
     */
    String getTopicIdentifier() {
        return topicIdentifier;
    }

    /**
     * Method to retrieve the topic header upon which routing would be based, if specified.
     *
     * @return the topic header to consider
     */
    String getTopicHeader() {
        return topicHeader;
    }

    BMap<String, BValue> getHeaderResourceMap() {
        return headerResourceMap;
    }

    BMap<String, BMap<String, BValue>> getPayloadKeyResourceMap() {
        return payloadKeyResourceMap;
    }

    /**
     * Method to retrieve the topic resource mapping if specified.
     *
     * @return the topic-resource map specified for the service
     */
    BMap<String, BMap<String, BMap<String, BValue>>> getHeaderAndPayloadKeyResourceMap() {
        return headerAndPayloadKeyResourceMap;
    }

    HashMap<String, String[]> getResourceDetails() {
        return resourceDetails;
    }

    /**
     * Register a WebSubSubscriber service in the map.
     *
     * @param service the {@link Service} to be registered
     */
    public void registerWebSubSubscriberService(Service service) {
        HttpService httpService = WebSubHttpService.buildWebSubSubscriberHttpService(service);
        String hostName = httpService.getHostName();
        if (servicesMapByHost.get(hostName) == null) {
            servicesByBasePath = new ConcurrentHashMap<>();
            sortedServiceURIs = new CopyOnWriteArrayList<>();
            servicesMapByHost.put(hostName, new ServicesMapHolder(servicesByBasePath, sortedServiceURIs));
        } else {
            servicesByBasePath = getServicesByHost(hostName);
            sortedServiceURIs = getSortedServiceURIsByHost(hostName);
        }
        servicesByBasePath.put(httpService.getBasePath(), httpService);
        logger.info("Service deployed : " + service.getName() + " with context " + httpService.getBasePath());

        //basePath will get cached after registering service
        sortedServiceURIs.add(httpService.getBasePath());
        sortedServiceURIs.sort((basePath1, basePath2) -> basePath2.length() - basePath1.length());

        if (topicIdentifier != null) {
            // i.e., extension config exists
            validateCustomResources(httpService.getResources(), this);
        }
    }

    private void populateResourceDetails() {
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
        this.resourceDetails = resourceDetails;
    }

    private static void populateResourceDetailsByHeader(BMap<String, BValue> headerResourceMap,
                                                        HashMap<String, String[]> resourceDetails) {
        headerResourceMap.getMap().values().forEach(value -> {
            BRefValueArray resourceDetailTuple = (BRefValueArray) value;
            String resourceName = resourceDetailTuple.getBValue(0).stringValue();
            BStructureType paramDetails =
                    (BStructureType) ((BTypeDescValue) (resourceDetailTuple).getBValue(1)).value();
            resourceDetails.put(resourceName,
                                new String[]{paramDetails.getPackagePath(), paramDetails.getName()});
        });
    }

    private static void populateResourceDetailsByPayload(BMap<String, BMap<String, BValue>> payloadKeyResourceMap,
                                                         HashMap<String, String[]> resourceDetails) {
        payloadKeyResourceMap.getMap().values().forEach(mapByKey -> {
            mapByKey.getMap().values().forEach(value -> {
                BRefValueArray resourceDetailTuple = (BRefValueArray) value;
                String resourceName = resourceDetailTuple.getBValue(0).stringValue();
                BStructureType paramDetails =
                        (BStructureType) ((BTypeDescValue) (resourceDetailTuple).getBValue(1)).value();
                resourceDetails.put(resourceName,
                                    new String[]{paramDetails.getPackagePath(), paramDetails.getName()});
            });
        });
    }

    private static void populateResourceDetailsByHeaderAndPayload(BMap<String, BMap<String, BMap<String, BValue>>>
                                                                          headerAndPayloadKeyResourceMap,
                                                                  HashMap<String, String[]> resourceDetails) {
        headerAndPayloadKeyResourceMap.getMap().values().forEach(mapByHeader -> {
            mapByHeader.getMap().values().forEach(mapByKey -> {
                mapByKey.getMap().values().forEach(value -> {
                    BRefValueArray resourceDetailTuple = (BRefValueArray) value;
                    String resourceName = resourceDetailTuple.getBValue(0).stringValue();
                    BStructureType paramDetails =
                            (BStructureType) ((BTypeDescValue) (resourceDetailTuple).getBValue(1)).value();
                    resourceDetails.put(resourceName,
                                        new String[]{paramDetails.getPackagePath(), paramDetails.getName()});
                });
            });
        });
    }
}
