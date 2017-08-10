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

package org.ballerinalang.services.dispatchers.http;

import org.ballerinalang.util.codegen.AnnAttachmentInfo;
import org.ballerinalang.util.codegen.AnnAttributeValue;
import org.ballerinalang.util.codegen.ResourceInfo;
import org.ballerinalang.util.codegen.ServiceInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTPCorsFilter loads CORS headers in a server.
 */
public class HTTPCorsFilter {

    private ResourceInfo resource;
    private AnnAttributeValue attributeValue;
    private AnnAttachmentInfo resCorsAnnotInfo;
    private AnnAttachmentInfo serviceConfigAnnotInfo;
    private static final HTTPCorsFilter corsRegistry = new HTTPCorsFilter();
    private String[] corsHeaders = {Constants.ALLOW_ORIGIN, Constants.MAX_AGE
            , Constants.ALLOW_CREDENTIALS, Constants.ALLOW_METHODS
            , Constants.ALLOW_HEADERS, Constants.EXPOSE_HEADERS};


    public static HTTPCorsFilter getInstance() {
        return corsRegistry;
    }

    public boolean isCorsHeadersAvailable(ServiceInfo service) {
        serviceConfigAnnotInfo = service.getAnnotationAttachmentInfo(Constants.HTTP_PACKAGE_PATH
                , Constants.ANNOTATION_NAME_CONFIGURATION);
        boolean hasCorsHeaders = false;
        for (String header : corsHeaders) {
            attributeValue = serviceConfigAnnotInfo.getAttributeValue(header);
            if (attributeValue != null) {
                hasCorsHeaders = true;
                break;
            }
        }
        return hasCorsHeaders;
    }

    public Map<String, List<String>> getServiceCoresHeaders() {

        Map<String, List<String>> serviceCorsMap = new HashMap<>();
        for (String header : corsHeaders) {
            attributeValue = serviceConfigAnnotInfo.getAttributeValue(header);
            List<String> list = new ArrayList();
            if (attributeValue != null ) {
                if (attributeValue.getAttributeValueArray() != null) {
                    for (AnnAttributeValue att : attributeValue.getAttributeValueArray()) {
                        list.add(att.getStringValue().trim());
                    }
                } else {
                    list.add(attributeValue.getStringValue().trim());
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
                        list.add("*");
                        break;
                    case Constants.MAX_AGE:
                        list.add("100");
                        break;
                    default:
                        break;
                }
            }
            serviceCorsMap.put(header, list);
        }
        return serviceCorsMap;
    }

    public boolean isResourceCorsDefined(ResourceInfo resource) {
        this.resource = resource;
        resCorsAnnotInfo = resource
                .getAnnotationAttachmentInfo(Constants.HTTP_PACKAGE_PATH, "CORS");
        if (resCorsAnnotInfo != null) {
            return true;
        }
        return false;
    }

    public Map<String, List<String>> getResourceCorsHeaders() {
        Map<String, List<String>> resourceCorsMap = new HashMap<>();
        for (String header : corsHeaders) {
            attributeValue = resCorsAnnotInfo.getAttributeValue(header);
            List<String> list = new ArrayList();
            if (attributeValue != null) {
                if (attributeValue.getAttributeValueArray() != null) {
                    for (AnnAttributeValue att : attributeValue.getAttributeValueArray()) {
                        list.add(att.getStringValue().trim());
                    }
                } else {
                    list.add(attributeValue.getStringValue().trim());
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
                        list.add(getResourceMethod(resource));
                        break;
                    case Constants.MAX_AGE:
                        list.add("100");
                        break;
                    default:
                        break;
                }
            }
            resourceCorsMap.put(header, list);
        }
        return resourceCorsMap;
    }

    private String getResourceMethod(ResourceInfo resource) {
        String[] httpMethods = {"GET", "PUT", "POST", "DELETE", "OPTIONS", "HEAD"};
        for (String httpMethod : httpMethods) {
            if (resource.getAnnotationAttachmentInfo(Constants.HTTP_PACKAGE_PATH, httpMethod) != null) {
                return httpMethod;
            }
        }
        return "*";
    }



}
