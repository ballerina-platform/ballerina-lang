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

import org.ballerinalang.logging.BLogManager;
import org.ballerinalang.services.dispatchers.uri.DispatcherUtil;
import org.ballerinalang.util.codegen.ResourceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * CorsFilter provides both input and output filter for CORS following http://www.w3.org/TR/cors/.
 *
 * @since 0.93
 */
public class CorsFilter {
    private Map<String, List<String>> resourceCors;
    private Map<String, String> responseCors = new HashMap<>();
    private final Pattern spacePattern = Pattern.compile(" ");
    private final Pattern fieldCommaPattern = Pattern.compile(",");
    private final Logger bLog = LoggerFactory.getLogger(BLogManager.BALLERINA_ROOT_LOGGER_NAME);
    private CarbonMessage requestMsg;
    private CarbonMessage responseMsg;

    public CorsFilter(CarbonMessage requestMsg, CarbonMessage responseMsg, boolean isSimpleRequest) {
        this.requestMsg = requestMsg;
        this.responseMsg = responseMsg;
        generateCORSHeaders(isSimpleRequest);
    }

    public void generateCORSHeaders(boolean isSimpleRequest) {
        boolean isCorsResponseHeadersAvailable = false;
        if (isSimpleRequest) {
            resourceCors = (Map<String, List<String>>) requestMsg.getProperty(Constants.RESOURCES_CORS);
            String origin = requestMsg.getHeader(Constants.ORIGIN);
            if (origin != null && resourceCors != null) {
                if (isSimpleRequest(origin)) {
                    isCorsResponseHeadersAvailable = true;
                }
            }
        } else {
            String origin = requestMsg.getHeader(Constants.ORIGIN);
            if (origin != null) {
                if (isPreflightRequest(origin, requestMsg)) {
                    isCorsResponseHeadersAvailable = true;
                }
            }
        }
        if (isCorsResponseHeadersAvailable) {
            responseCors.entrySet().stream().forEach(header -> {
                responseMsg.setHeader(header.getKey(), header.getValue());
            });
            responseMsg.removeHeader(Constants.ALLOW);
        }
    }

    private boolean isSimpleRequest(String origin) {
        //6.1.1 - There should be an origin
        List<String> requestOrigins = getOriginValues(origin);
        if (requestOrigins == null || requestOrigins.size() == 0) {
            bLog.info("Origin header parsing failed");
            return false;
        }
        //6.1.2 - check all the origins
        if (!isEffectiveOrigin(requestOrigins, resourceCors.get(Constants.ALLOW_ORIGIN))) {
            return false;
        }
        //6.1.3 - set origin and credentials
        setAllowOriginAndCredentials(requestOrigins);
        //6.1.4 - set exposed headers
        setExposedAllowedHeaders();
        return true;
    }

    private boolean isPreflightRequest(String originValue, CarbonMessage cMsg) {
        //6.2.1 - request must have origin, must have one origin.
        List<String> requestOrigins = getOriginValues(originValue);
        if (requestOrigins == null || requestOrigins.size() != 1) {
            return false;
        }
        String origin = requestOrigins.get(0);
        //6.2.3 - request must have access-control-request-method, must be single-valued
        List<String> requestMethods = getHeaderValues(Constants.AC_REQUEST_METHODS, cMsg);
        if (requestMethods == null && requestMethods.size() != 1) {
            return false;
        }
        String requestMethod = requestMethods.get(0);
        if (!hasResourceWithReqMethod(cMsg, requestMethod)) {
            return false;
        }
        if (resourceCors == null) {
            return false;
        }
        //6.2.2 - request origin must be on the list or match with *.
        if (!isEffectiveOrigin(Arrays.asList(origin), resourceCors.get(Constants.ALLOW_ORIGIN))) {
            return false;
        }
        //6.2.4 - get list of request headers.
        List<String> requestHeaders = getHeaderValues(Constants.AC_REQUEST_HEADERS, cMsg);
        if (!isEffectiveHeaders(requestHeaders, resourceCors.get(Constants.ALLOW_HEADERS))) {
            return false;
        }
        //6.2.7 - set origin and credentials
        setAllowOriginAndCredentials(Arrays.asList(origin));
        //6.2.9 - set allow-methods
        getResponseCors().put(Constants.AC_ALLOW_METHODS, requestMethod);
        //6.2.10 - set allow-headers
        getResponseCors().put(Constants.AC_ALLOW_HEADERS, DispatcherUtil.concatValues(requestHeaders, false));
        //6.2.8 - set max-age
        getResponseCors().put(Constants.AC_MAX_AGE, resourceCors.get(Constants.MAX_AGE).get(0));
        return true;
    }


    private boolean isEffectiveOrigin(List<String> requestOrigins, List<String> resourceOrigins) {
        if (resourceOrigins.size() == 1 && resourceOrigins.get(0).equals("*")) {
            return true;
        }
        return resourceOrigins.containsAll(requestOrigins);
    }

    private boolean isEffectiveHeaders(List<String> requestHeaders, List<String> resourceHeaders) {
        if (resourceHeaders.size() == 0) {
            return true;
        } else {
            Set<String> headersSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            headersSet.addAll(resourceHeaders);
            return headersSet.containsAll(requestHeaders);
        }
    }

    private boolean hasResourceWithReqMethod(CarbonMessage cMsg, String requestMethod) {
        List<ResourceInfo> resources = (List<ResourceInfo>) cMsg.getProperty(Constants.PREFLIGHT_RESOURCES);
        if (resources == null) {
            return false;
        } else {
            for (ResourceInfo resource : resources) {
                for (String method : DispatcherUtil.getHttpMethods(resource)) {
                    if (requestMethod.equals(method)) {
                        resourceCors = HTTPCorsRegistry.getInstance().getCorsHeaders(resource);
                        return true;
                    }
                }
            }
            if (requestMethod.equals(Constants.HTTP_METHOD_HEAD)) {
                for (ResourceInfo resource : resources) {
                    for (String method : DispatcherUtil.getHttpMethods(resource)) {
                        if (method.equals(Constants.HTTP_METHOD_GET)) {
                            resourceCors = HTTPCorsRegistry.getInstance().getCorsHeaders(resource);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private List<String> getHeaderValues(String key, CarbonMessage cMsg) {
        String value = cMsg.getHeader(key);
        if (value != null) {
            String[] values = fieldCommaPattern.split(value);
            return Arrays.stream(values).collect(Collectors.toList());
        }
        return null;
    }

    private void setExposedAllowedHeaders() {
        List<String> exposeHeaders = resourceCors.get(Constants.EXPOSE_HEADERS);
        String exposeHeaderResponse = DispatcherUtil.concatValues(exposeHeaders, false);
        if (!exposeHeaderResponse.isEmpty()) {
            getResponseCors().put(Constants.AC_EXPOSE_HEADERS, exposeHeaderResponse);
        }
    }

    private void setAllowOriginAndCredentials(List<String> effectiveOrigins) {
        String allowCreds = resourceCors.get(Constants.ALLOW_CREDENTIALS).get(0);
        getResponseCors().put(Constants.AC_ALLOW_CREDENTIALS, allowCreds);
        String originResponse;
        if (allowCreds.equals("false") && effectiveOrigins.size() != 0) {
            originResponse = "*";
        } else {
            originResponse = DispatcherUtil.concatValues(effectiveOrigins, true);
        }
        getResponseCors().put(Constants.AC_ALLOW_ORIGIN, originResponse);
    }

    private List<String> getOriginValues(String originValue) {
        String[] origins = spacePattern.split(originValue);
        return Arrays.stream(origins).filter(value -> (value.contains("://"))).collect(Collectors.toList());
    }

    public Map<String, String> getResponseCors() {
        return responseCors;
    }
}
