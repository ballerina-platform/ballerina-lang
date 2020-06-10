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

import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpResource;
import org.ballerinalang.net.http.HttpService;
import org.ballerinalang.net.uri.DispatcherUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.net.http.HttpConstants.DEFAULT_HOST;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.ANN_NAME_WEBSUB_SUBSCRIBER_SERVICE_CONFIG;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.PATH_FIELD;
import static org.ballerinalang.net.websub.WebSubSubscriberConstants.WEBSUB_PACKAGE_FULL_QUALIFIED_NAME;

/**
 * WebSub HTTP wrapper for the {@code Service} implementation.
 *
 * @since 0.965.0
 */
public class WebSubHttpService extends HttpService {

    private static final Logger logger = LoggerFactory.getLogger(WebSubHttpService.class);
    private String topic;

    private WebSubHttpService(ObjectValue service) {
        super(service);
    }

    private static MapValue getWebSubSubscriberServiceConfigAnnotation(ObjectValue service) {
        return getServiceConfigAnnotation(service, WEBSUB_PACKAGE_FULL_QUALIFIED_NAME,
                                          ANN_NAME_WEBSUB_SUBSCRIBER_SERVICE_CONFIG);
    }

    /**
     * Builds the HTTP service representation of the service.
     *
     * @param service   the service for which the HTTP representation is built
     * @return  the built HttpService representation
     */
    static WebSubHttpService buildWebSubSubscriberHttpService(ObjectValue service) {
        WebSubHttpService websubHttpService = new WebSubHttpService(service);
        MapValue serviceConfigAnnotation = getWebSubSubscriberServiceConfigAnnotation(service);

        if (!serviceConfigAnnotation.containsKey(PATH_FIELD)) {
            logger.debug("'path' not specified in the service config annotation, using the default base path");
            // Service name cannot start with /, hence concat.
            websubHttpService.setBasePath(HttpConstants.DEFAULT_BASE_PATH.concat(websubHttpService.getName()));
        } else {
            websubHttpService.setBasePath(serviceConfigAnnotation.getStringValue(PATH_FIELD).getValue());
        }

        List<HttpResource> resources = new ArrayList<>();
        for (AttachedFunction resource : websubHttpService.getBalService().getType().getAttachedFunctions()) {
            HttpResource httpResource = WebSubHttpResource.buildWebSubHttpResource(resource, websubHttpService);
            resources.add(httpResource);
        }
        websubHttpService.setResources(resources);
        websubHttpService.setAllAllowedMethods(DispatcherUtil.getAllResourceMethods(websubHttpService));
        websubHttpService.setHostName(DEFAULT_HOST);

        return websubHttpService;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

}
