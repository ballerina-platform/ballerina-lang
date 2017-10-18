/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http;

import org.ballerinalang.connector.api.AnnAttrValue;
import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CorsRegistry cache CORS headers at service/resource level during load time.
 *
 * @since 0.93
 */
public class CorsRegistry {

    private AnnAttrValue attributeValue;
    private Annotation configAnnotInfo;
    private static final CorsRegistry corsRegistry = new CorsRegistry();
    private Map<String, Map<String, List<String>>> resourceCorsHolder = new ConcurrentHashMap<>();
    private String[] corsHeaders = {Constants.ALLOW_ORIGIN, Constants.MAX_AGE
            , Constants.ALLOW_CREDENTIALS, Constants.ALLOW_METHODS
            , Constants.ALLOW_HEADERS, Constants.EXPOSE_HEADERS};

    public static CorsRegistry getInstance() {
        return corsRegistry;
    }

    public Map<String, List<String>> getServiceCors(Service service) {
        if (isCorsHeadersAvailable(service.getAnnotation(Constants.HTTP_PACKAGE_PATH
                , Constants.ANN_NAME_CONFIG))) {
            return populateCorsHeaders(null);
        } else {
            return null;
        }
    }

    public void processResourceCors(Resource resource, Map<String, List<String>> serviceCorsMap) {
        if (isCorsHeadersAvailable(resource.getAnnotation(Constants.HTTP_PACKAGE_PATH
                , Constants.ANN_NAME_RESOURCE_CONFIG))) {
            resourceCorsHolder.put(createResourceKey(resource), populateCorsHeaders(resource));
        } else if (serviceCorsMap != null && !serviceCorsMap.isEmpty()) {
            resourceCorsHolder.put(createResourceKey(resource), serviceCorsMap);
        }
    }

    public boolean isCorsHeadersAvailable(Annotation configAnnotInfo) {
        if (configAnnotInfo == null) {
            return false;
        }
        this.configAnnotInfo = configAnnotInfo;
        boolean hasCorsHeaders = false;
        for (String header : corsHeaders) {
            attributeValue = configAnnotInfo.getAnnAttrValue(header);
            if (attributeValue != null) {
                hasCorsHeaders = true;
                break;
            }
        }
        return hasCorsHeaders;
    }

    public Map<String, List<String>> populateCorsHeaders(Resource resource) {
        Map<String, List<String>> corsMap = new HashMap<>();
        for (String header : corsHeaders) {
            attributeValue = configAnnotInfo.getAnnAttrValue(header);
            List<String> list = new ArrayList();
            if (attributeValue != null) {
                if (attributeValue.getAnnAttrValueArray() != null) {
                    for (AnnAttrValue att : attributeValue.getAnnAttrValueArray()) {
                        list.add(att.getStringValue().trim());
                    }
                } else {
                    if (header.equals(Constants.ALLOW_CREDENTIALS)) {
                        list.add(String.valueOf(attributeValue.getBooleanValue()));
                    } else if (header.equals(Constants.MAX_AGE)) {
                        list.add(String.valueOf(attributeValue.getIntValue()));
                    }
                }
            } else {
                switch (header) {
                    case Constants.ALLOW_ORIGIN:
                        list.add("*");
                        break;
                    case Constants.ALLOW_CREDENTIALS:
                        list.add("false");
                        break;
                    case Constants.ALLOW_METHODS:
                        if (resource != null) {
                            list.add(getResourceMethod(resource));
                        } else {
                            list.add("*");
                        }
                        break;
                    case Constants.MAX_AGE:
                        list.add("-1");
                        break;
                    default:
                        break;
                }
            }
            corsMap.put(header, list);
        }
        return corsMap;
    }

    private String getResourceMethod(Resource resource) {
        String[] httpMethods = {"GET", "PUT", "POST", "DELETE", "OPTIONS", "HEAD"};
        for (String httpMethod : httpMethods) {
            if (resource.getAnnotation(Constants.HTTP_PACKAGE_PATH, httpMethod) != null) {
                return httpMethod;
            }
        }
        return "*";
    }

    public Map<String, List<String>> getCorsHeaders(Resource resource) {
        Map<String, List<String>> corsheaders = resourceCorsHolder.get(createResourceKey(resource));
        if (corsheaders != null) {
            return corsheaders;
        }
        return null;
    }

    private String createResourceKey(Resource resource) {
        String separator = "%&$";
        return resource.getServiceName() + separator + resource.getName();
    }
}
