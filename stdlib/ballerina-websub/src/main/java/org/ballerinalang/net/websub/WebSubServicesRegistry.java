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
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.net.http.HTTPServicesRegistry;
import org.ballerinalang.net.http.HttpService;
import org.ballerinalang.net.http.WebSocketServicesRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The WebSub service registry which uses an {@link HTTPServicesRegistry} to maintain WebSub Subscriber HTTP services.
 *
 * @since 0.965.0
 */
public class WebSubServicesRegistry extends HTTPServicesRegistry {

    private static final Logger logger = LoggerFactory.getLogger(WebSubServicesRegistry.class);

    private String topicIdentifier;
    private String topicHeader;
    private BStringArray topicPayloadKeys;
    private BMap<String, BMap<String, BString>> topicResourceMap;

    public WebSubServicesRegistry(WebSocketServicesRegistry webSocketServicesRegistry) {
        super(webSocketServicesRegistry);
    }

    public String getTopicIdentifier() {
        return topicIdentifier;
    }

    public void setTopicIdentifier(String topicIdentifier) {
        this.topicIdentifier = topicIdentifier;
    }

    /**
     * Method to retrieve the topic header upon which routing would be based, if specified.
     *
     * @return the topic header to consider
     */
    public String getTopicHeader() {
        return topicHeader;
    }

    /**
     * Method to set the topic header upon which routing should be based, if specified.
     *
     * @param topicHeader the topic header to consider
     */
    public void setTopicHeader(String topicHeader) {
        this.topicHeader = topicHeader;
    }

    public BStringArray getTopicPayloadKeys() {
        return topicPayloadKeys;
    }

    public void setTopicPayloadKeys(BStringArray topicPayloadKeys) {
        this.topicPayloadKeys = topicPayloadKeys;
    }

    /**
     * Method to set the topic resource mapping if specified.
     *
     * @param topicResourceMap topic-resource map specified for the service
     */
    public void setTopicResourceMap(BMap<String, BMap<String, BString>> topicResourceMap) {
        this.topicResourceMap = topicResourceMap;
    }

    /**
     * Method to retrieve the topic resource mapping if specified.
     *
     * @return the topic-resource map specified for the service
     */
    public BMap<String, BMap<String, BString>> getTopicResourceMap() {
        return topicResourceMap;
    }

    /**
     * Register a WebSubSubscriber service in the map.
     *
     * @param service the {@link Service} to be registered
     */
    public void registerWebSubSubscriberService(Service service) {
        HttpService httpService = WebSubHttpService.buildWebSubSubscriberHttpService(service);

        servicesInfoMap.put(httpService.getBasePath(), httpService);
        logger.info("Service deployed : " + service.getName() + " with context " + httpService.getBasePath());

        //basePath will get cached after registering service
        sortedServiceURIs.add(httpService.getBasePath());
        sortedServiceURIs.sort((basePath1, basePath2) -> basePath2.length() - basePath1.length());

        WebSubSubscriberServiceValidator.validateResources(httpService, topicHeader, topicResourceMap);
    }

}
