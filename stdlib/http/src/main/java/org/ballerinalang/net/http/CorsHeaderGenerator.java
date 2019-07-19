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

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.logging.BLogManager;
import org.ballerinalang.net.uri.DispatcherUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.util.Arrays;
import java.util.Collections;
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
    private static final String ACTION = "Failed to process CORS :";

    public static void process(HttpCarbonMessage requestMsg, HttpCarbonMessage responseMsg, boolean isSimpleRequest) {

        boolean isCorsResponseHeadersAvailable = false;
        Map<String, String> responseHeaders;
        CorsHeaders resourceCors;
        if (isSimpleRequest) {
            resourceCors = (CorsHeaders) requestMsg.getProperty(HttpConstants.RESOURCES_CORS);
            String origin = requestMsg.getHeader(HttpHeaderNames.ORIGIN.toString());
            //resourceCors cannot be null here
            if (origin == null || resourceCors == null || !resourceCors.isAvailable()) {
                return;
            }
            if ((responseHeaders = processSimpleRequest(origin, resourceCors)) != null) {
                isCorsResponseHeadersAvailable = true;
            }
        } else {
            String origin = requestMsg.getHeader(HttpHeaderNames.ORIGIN.toString());
            if (origin == null) {
                return;
            }
            if ((responseHeaders = processPreflightRequest(origin, requestMsg)) != null) {
                isCorsResponseHeadersAvailable = true;
            }
        }
        if (isCorsResponseHeadersAvailable) {
            responseHeaders.forEach(responseMsg::setHeader);
            responseMsg.removeHeader(HttpHeaderNames.ALLOW.toString());
        }
    }

    private static Map<String, String> processSimpleRequest(String origin, CorsHeaders resourceCors) {
        Map<String, String> responseHeaders = new HashMap<>();
        //6.1.1 - There should be an origin
        List<String> requestOrigins = getOriginValues(origin);
        if (requestOrigins == null || requestOrigins.isEmpty()) {
            bLog.info("{} origin header field parsing failed", ACTION);
            return null;
        }
        //6.1.2 - check all the origins
        if (!isEffectiveOrigin(requestOrigins, resourceCors.getAllowOrigins())) {
            bLog.info("{} not allowed origin", ACTION);
            return null;
        }
        //6.1.3 - set origin and credentials
        setAllowOriginAndCredentials(requestOrigins, resourceCors, responseHeaders);
        //6.1.4 - set exposed headers
        setExposedAllowedHeaders(resourceCors, responseHeaders);
        return responseHeaders;
    }

    private static Map<String, String> processPreflightRequest(String originValue, HttpCarbonMessage cMsg) {
        Map<String, String> responseHeaders = new HashMap<>();
        //6.2.1 - request must have origin, must have one origin.
        List<String> requestOrigins = getOriginValues(originValue);
        if (requestOrigins == null || requestOrigins.size() != 1) {
            bLog.info("{} origin header field parsing failed", ACTION);
            return null;
        }
        String origin = requestOrigins.get(0);
        //6.2.3 - request must have access-control-request-method, must be single-valued
        List<String> requestMethods = getHeaderValues(HttpHeaderNames.ACCESS_CONTROL_REQUEST_METHOD.toString(), cMsg);
        if (requestMethods == null || requestMethods.size() != 1) {
            String error = requestMethods == null ? "Access-Control-Request-Method header is unavailable" :
                    "Access-Control-Request-Method header value must be single-valued";
            bLog.info("{} {}", ACTION, error);
            return null;
        }
        String requestMethod = requestMethods.get(0);
        CorsHeaders resourceCors = getResourceCors(cMsg, requestMethod);
        if (resourceCors == null || !resourceCors.isAvailable()) {
            String error = resourceCors == null ? "access control request method not allowed" :
                    "CORS headers not declared properly";
            bLog.info("{} {}", ACTION, error);
            return null;
        }
        if (!isEffectiveMethod(requestMethod, resourceCors.getAllowMethods())) {
            bLog.info("{} access control request method not allowed", ACTION);
            return null;
        }
        //6.2.2 - request origin must be on the list or match with *.
        if (!isEffectiveOrigin(Collections.singletonList(origin), resourceCors.getAllowOrigins())) {
            bLog.info("{} origin not allowed", ACTION);
            return null;
        }
        //6.2.4 - get list of request headers.
        List<String> requestHeaders = getHeaderValues(HttpHeaderNames.ACCESS_CONTROL_REQUEST_HEADERS.toString(), cMsg);
        if (!isEffectiveHeader(requestHeaders, resourceCors.getAllowHeaders())) {
            bLog.info("{} header field parsing failed", ACTION);
            return null;
        }
        //6.2.7 - set origin and credentials
        setAllowOriginAndCredentials(Collections.singletonList(origin), resourceCors, responseHeaders);
        //6.2.9 - set allow-methods
        responseHeaders.put(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS.toString(), requestMethod);
        //6.2.10 - set allow-headers
        if (requestHeaders != null) {
            responseHeaders.put(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS.toString(),
                    DispatcherUtil.concatValues(requestHeaders, false));
        }
        //6.2.8 - set max-age
        responseHeaders.put(HttpHeaderNames.ACCESS_CONTROL_MAX_AGE.toString(),
                String.valueOf(resourceCors.getMaxAge()));
        return responseHeaders;
    }

    private static boolean isEffectiveOrigin(List<String> requestOrigins, List<String> resourceOrigins) {
        return resourceOrigins.size() == 1 && resourceOrigins.get(0).equals("*") ||
                resourceOrigins.containsAll(requestOrigins);
    }

    private static boolean isEffectiveHeader(List<String> requestHeaders, List<String> resourceHeaders) {
        if (resourceHeaders == null || requestHeaders == null) {
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

    @SuppressWarnings("unchecked")
    private static CorsHeaders getResourceCors(HttpCarbonMessage cMsg, String requestMethod) {
        List<HttpResource> resources = (List<HttpResource>) cMsg.getProperty(HttpConstants.PREFLIGHT_RESOURCES);
        if (resources == null) {
            return null;
        }
        for (HttpResource resource : resources) {
            if (resource.getMethods() != null && resource.getMethods().contains(requestMethod)) {
                return resource.getCorsHeaders();
            }
        }
        if (!requestMethod.equals(HttpConstants.HTTP_METHOD_HEAD)) {
            return null;
        }
        for (HttpResource resource : resources) {
            if (resource.getMethods() != null && resource.getMethods().contains(HttpConstants.HTTP_METHOD_GET)) {
                return resource.getCorsHeaders();
            }
        }
        return null;
    }

    private static List<String> getHeaderValues(String key, HttpCarbonMessage cMsg) {
        String value = cMsg.getHeader(key);
        if (value != null) {
            String[] values = fieldCommaPattern.split(value);
            return Arrays.stream(values).collect(Collectors.toList());
        }
        return null;
    }

    private static void setExposedAllowedHeaders(CorsHeaders resCors, Map<String, String> respHeaders) {
        //TODO can cache concatenated expose headers in the resource.
        List<String> exposeHeaders = resCors.getExposeHeaders();
        if (exposeHeaders == null) {
            return;
        }
        String exposeHeaderResponse = DispatcherUtil.concatValues(exposeHeaders, false);
        if (!exposeHeaderResponse.isEmpty()) {
            respHeaders.put(HttpHeaderNames.ACCESS_CONTROL_EXPOSE_HEADERS.toString(), exposeHeaderResponse);
        }
    }

    private static void setAllowOriginAndCredentials(List<String> effectiveOrigins, CorsHeaders resCors
            , Map<String, String> responseHeaders) {
        int allowCreds = resCors.getAllowCredentials();
        if (allowCreds == 1) {
            responseHeaders.put(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS.toString(), String.valueOf(true));
        }
        responseHeaders.put(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN.toString(),
                DispatcherUtil.concatValues(effectiveOrigins, true));
    }

    private static List<String> getOriginValues(String originValue) {
        String[] origins = spacePattern.split(originValue);
        return Arrays.stream(origins).filter(value -> (value.contains("://"))).collect(Collectors.toList());
    }

    private CorsHeaderGenerator() {
    }
}
