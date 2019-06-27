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

import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.http.HTTPServicesRegistry;
import org.ballerinalang.net.http.HttpService;
import org.ballerinalang.net.http.WebSocketServicesRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

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

    private MapValue<String, Object> headerResourceMap;
    private MapValue<String, MapValue<String, Object>> payloadKeyResourceMap;
    private MapValue<String, MapValue<String, MapValue<String, Object>>> headerAndPayloadKeyResourceMap;
    private HashMap<String, String[]> resourceDetails;

    public WebSubServicesRegistry(WebSocketServicesRegistry webSocketServicesRegistry) {
        super(webSocketServicesRegistry);
    }

    public WebSubServicesRegistry(WebSocketServicesRegistry webSocketServicesRegistry,
                                  String topicIdentifier, String topicHeader,
                                  MapValue<String, Object> headerResourceMap,
                                  MapValue<String, MapValue<String, Object>> payloadKeyResourceMap,
                                  MapValue<String, MapValue<String, MapValue<String, Object>>>
                                          headerAndPayloadKeyResourceMap,
                                  HashMap<String, String[]> resourceDetails) {
        super(webSocketServicesRegistry);
        this.topicIdentifier = topicIdentifier;
        this.topicHeader = topicHeader;
        this.headerResourceMap = headerResourceMap;
        this.payloadKeyResourceMap = payloadKeyResourceMap;
        this.headerAndPayloadKeyResourceMap = headerAndPayloadKeyResourceMap;
        this.resourceDetails = resourceDetails;
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

    MapValue<String, Object> getHeaderResourceMap() {
        return headerResourceMap;
    }

    MapValue<String, MapValue<String, Object>> getPayloadKeyResourceMap() {
        return payloadKeyResourceMap;
    }

    /**
     * Method to retrieve the topic resource mapping if specified.
     *
     * @return the topic-resource map specified for the service
     */
    MapValue<String, MapValue<String, MapValue<String, Object>>> getHeaderAndPayloadKeyResourceMap() {
        return headerAndPayloadKeyResourceMap;
    }

    HashMap<String, String[]> getResourceDetails() {
        return resourceDetails;
    }

    /**
     * Register a WebSubSubscriber service in the map.
     *
     * @param service to be registered
     */
    public void registerWebSubSubscriberService(ObjectValue service) {
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
        logger.info("Service deployed : " + service.getType().getName() + " with context " + httpService.getBasePath());

        //basePath will get cached after registering service
        sortedServiceURIs.add(httpService.getBasePath());
        sortedServiceURIs.sort((basePath1, basePath2) -> basePath2.length() - basePath1.length());

        if (topicIdentifier != null) {
            // i.e., extension config exists
            validateCustomResources(httpService.getResources(), this);
        }
    }
}
