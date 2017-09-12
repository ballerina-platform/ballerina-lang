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
 * CorsHeaderGenerator provides both input and output filter for CORS following http://www.w3.org/TR/cors/.
 *
 * @since 0.93
 */
public class CorsHeaderGenerator {
    private static final Pattern spacePattern = Pattern.compile(" ");
    private static final Pattern fieldCommaPattern = Pattern.compile(",");
    private static final Logger bLog = LoggerFactory.getLogger(BLogManager.BALLERINA_ROOT_LOGGER_NAME);
    private static final String action = "Failed to process CORS : ";

    public static void process(CarbonMessage requestMsg, CarbonMessage responseMsg, boolean isSimpleRequest) {

        boolean isCorsResponseHeadersAvailable = false;
        Map<String, String> responseHeaders = null;
        Map<String, List<String>> resourceCors;
        if (isSimpleRequest) {
            resourceCors = (Map<String, List<String>>) requestMsg.getProperty(Constants.RESOURCES_CORS);
            String origin = requestMsg.getHeader(Constants.ORIGIN);
            if (origin != null && resourceCors != null) {
                if ((responseHeaders = processSimpleRequest(origin, resourceCors)) != null) {
                    isCorsResponseHeadersAvailable = true;
                }
            }
        } else {
            String origin = requestMsg.getHeader(Constants.ORIGIN);
            if (origin != null) {
                if ((responseHeaders = processPreflightRequest(origin, requestMsg)) != null) {
                    isCorsResponseHeadersAvailable = true;
                }
            }
        }
        if (isCorsResponseHeadersAvailable) {
            responseHeaders.entrySet().stream().forEach(header -> {
                responseMsg.setHeader(header.getKey(), header.getValue());
            });
            responseMsg.removeHeader(Constants.ALLOW);
        }
    }

    private static Map<String, String> processSimpleRequest(String origin, Map<String, List<String>> resourceCors) {
        Map<String, String> responseHeaders = new HashMap<>();
        //6.1.1 - There should be an origin
        List<String> requestOrigins = getOriginValues(origin);
        if (requestOrigins == null || requestOrigins.size() == 0) {
            bLog.info(action + "origin header field parsing failed");
            return null;
        }
        //6.1.2 - check all the origins
        if (!isEffectiveOrigin(requestOrigins, resourceCors.get(Constants.ALLOW_ORIGIN))) {
            bLog.info(action + "not allowed origin");
            return null;
        }
        //6.1.3 - set origin and credentials
        setAllowOriginAndCredentials(requestOrigins, resourceCors, responseHeaders);
        //6.1.4 - set exposed headers
        setExposedAllowedHeaders(resourceCors, responseHeaders);
        return responseHeaders;
    }

    private static Map<String, String> processPreflightRequest(String originValue, CarbonMessage cMsg) {
        Map<String, String> responseHeaders = new HashMap<>();
        //6.2.1 - request must have origin, must have one origin.
        List<String> requestOrigins = getOriginValues(originValue);
        if (requestOrigins == null || requestOrigins.size() != 1) {
            bLog.info(action + "origin header field parsing failed");
            return null;
        }
        String origin = requestOrigins.get(0);
        //6.2.3 - request must have access-control-request-method, must be single-valued
        List<String> requestMethods = getHeaderValues(Constants.AC_REQUEST_METHOD, cMsg);
        if (requestMethods == null || requestMethods.size() != 1) {
            bLog.info(action + "not allowed request methods");
            return null;
        }
        String requestMethod = requestMethods.get(0);
        Map<String, List<String>> resourceCors;
        if ((resourceCors = getResourceCors(cMsg, requestMethod)) == null) {
            bLog.info(action + "headers are not declared properly");
            return null;
        }
        if (!isEffectiveMethod(requestMethod, resourceCors.get(Constants.ALLOW_METHODS))) {
            bLog.info(action + "not allowed method");
            return null;
        }
        //6.2.2 - request origin must be on the list or match with *.
        if (!isEffectiveOrigin(Arrays.asList(origin), resourceCors.get(Constants.ALLOW_ORIGIN))) {
            bLog.info(action + "not allowed origin");
            return null;
        }
        //6.2.4 - get list of request headers.
        List<String> requestHeaders = getHeaderValues(Constants.AC_REQUEST_HEADERS, cMsg);
        if (!isEffectiveHeader(requestHeaders, resourceCors.get(Constants.ALLOW_HEADERS))) {
            bLog.info(action + "header field parsing failed");
            return null;
        }
        //6.2.7 - set origin and credentials
        setAllowOriginAndCredentials(Arrays.asList(origin), resourceCors, responseHeaders);
        //6.2.9 - set allow-methods
        responseHeaders.put(Constants.AC_ALLOW_METHODS, requestMethod);
        //6.2.10 - set allow-headers
        if (requestHeaders != null) {
            responseHeaders.put(Constants.AC_ALLOW_HEADERS, DispatcherUtil.concatValues(requestHeaders, false));
        }
        //6.2.8 - set max-age
        responseHeaders.put(Constants.AC_MAX_AGE, resourceCors.get(Constants.MAX_AGE).get(0));
        return responseHeaders;
    }

    private static boolean isEffectiveOrigin(List<String> requestOrigins, List<String> resourceOrigins) {
        if (resourceOrigins.size() == 1 && resourceOrigins.get(0).equals("*")) {
            return true;
        }
        return resourceOrigins.containsAll(requestOrigins);
    }

    private static boolean isEffectiveHeader(List<String> requestHeaders, List<String> resourceHeaders) {
        if (resourceHeaders.size() == 0 || requestHeaders == null) {
            return true;
        } else {
            Set<String> headersSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            headersSet.addAll(resourceHeaders);
            return headersSet.containsAll(requestHeaders);
        }
    }

    private static boolean isEffectiveMethod(String requestMethod, List<String> resourceMethods) {
        if (resourceMethods.size() == 1 && resourceMethods.get(0).equals("*")) {
            return true;
        }
        for (String method : resourceMethods) {
            if (requestMethod.equals(method)) {
                return true;
            }
        }
        return false;
    }

    private static Map<String, List<String>> getResourceCors(CarbonMessage cMsg, String requestMethod) {
        List<ResourceInfo> resources = (List<ResourceInfo>) cMsg.getProperty(Constants.PREFLIGHT_RESOURCES);
        if (resources == null) {
            return null;
        } else {
            for (ResourceInfo resource : resources) {
                if (DispatcherUtil.getHttpMethods(resource) != null) {
                    for (String method : DispatcherUtil.getHttpMethods(resource)) {
                        if (requestMethod.equals(method)) {
                            return CorsRegistry.getInstance().getCorsHeaders(resource);
                        }
                    }
                }
            }
            if (requestMethod.equals(Constants.HTTP_METHOD_HEAD)) {
                for (ResourceInfo resource : resources) {
                    if (DispatcherUtil.getHttpMethods(resource) != null) {
                        for (String method : DispatcherUtil.getHttpMethods(resource)) {
                            if (method.equals(Constants.HTTP_METHOD_GET)) {
                                return CorsRegistry.getInstance().getCorsHeaders(resource);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private static List<String> getHeaderValues(String key, CarbonMessage cMsg) {
        String value = cMsg.getHeader(key);
        if (value != null) {
            String[] values = fieldCommaPattern.split(value);
            return Arrays.stream(values).collect(Collectors.toList());
        }
        return null;
    }

    private static void setExposedAllowedHeaders(Map<String, List<String>> resCors, Map<String, String> respHeaders) {
        List<String> exposeHeaders = resCors.get(Constants.EXPOSE_HEADERS);
        String exposeHeaderResponse = DispatcherUtil.concatValues(exposeHeaders, false);
        if (!exposeHeaderResponse.isEmpty()) {
            respHeaders.put(Constants.AC_EXPOSE_HEADERS, exposeHeaderResponse);
        }
    }

    private static void setAllowOriginAndCredentials(List<String> effectiveOrigins, Map<String, List<String>> resCors
            , Map<String, String> responseHeaders) {
        String allowCreds = resCors.get(Constants.ALLOW_CREDENTIALS).get(0);
        if (allowCreds.equals("true")) {
            responseHeaders.put(Constants.AC_ALLOW_CREDENTIALS, allowCreds);
        }
        responseHeaders.put(Constants.AC_ALLOW_ORIGIN, DispatcherUtil.concatValues(effectiveOrigins, true));
    }

    private static List<String> getOriginValues(String originValue) {
        String[] origins = spacePattern.split(originValue);
        return Arrays.stream(origins).filter(value -> (value.contains("://"))).collect(Collectors.toList());
    }
}
