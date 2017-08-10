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
import org.ballerinalang.model.values.BMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * CORSMatcher matches request CORS headers with service.
 */
public class CORSMatcher {
    private static Map<String, List<String>> resourceCors;
    private static Map<String, String> responseCors = new HashMap<>();
    private static boolean isCorsResponseHeadersAvailable = false;
    private static final Pattern SPACE_PATTERN = Pattern.compile(" ");
    private static final Pattern FIELD_COMMA_PATTERN = Pattern.compile(",");
    private static final Logger bLog = LoggerFactory.getLogger(BLogManager.BALLERINA_ROOT_LOGGER_NAME);

    public static void process(Map<String, List<String>> resourceCorsHeaders, CarbonMessage cMsg) {
        resourceCors = resourceCorsHeaders;
        String origin = cMsg.getHeader(Constants.ORIGIN);
        if (origin != null && resourceCors != null) {
            String httpMethod = cMsg.getProperty(Constants.HTTP_METHOD).toString();
            if (Constants.HTTP_METHOD_OPTIONS.equals(httpMethod)) {
                //validatePreflightRequest(origin, cMsg);
            } else {
                if (validateSimpleRequest(origin)) {
                    isCorsResponseHeadersAvailable = true;
                } else {
                    //do not set headers.
                }
            }

        }
    }

    private static boolean validateSimpleRequest(String origin) {
        List<String> list = new ArrayList();
        List<String> resourceOrigins = resourceCors.get(Constants.ALLOW_ORIGIN);
        List<String> requestOrigins = getOriginValues(origin);
        if (requestOrigins != null && requestOrigins.size() != 0) {
            if (resourceOrigins.size() == 1 && resourceOrigins.get(0).equals("*")) {
                //origin allowed.
                list = requestOrigins;
            } else {
                List<String> matchedOrigins = resourceOrigins.stream().filter(requestOrigins::contains)
                        .collect(Collectors.toList());
                if (matchedOrigins.size() > 0) {
                    list = matchedOrigins;
                } else {
                    return false;
                }
            }
        } else {
            bLog.info("Origin header parsing failed");
            return false;
        }
        setAllowOriginAndCredentials(list);
        setExposedAllowedOrigins();
        return true;
    }

    private static boolean validatePreflightRequest(String originValue, CarbonMessage cMsg) {
        List<String> requestOrigins = getOriginValues(originValue);
        if (requestOrigins != null && requestOrigins.size() == 1) {
            String origin = requestOrigins.get(0);
            List<String> requestMethodValues = getHeaderValues(Constants.AC_REQUEST_HEADERS, cMsg);
            if (requestMethodValues != null && requestMethodValues.size() == 1) {
                String requestMethod = requestMethodValues.get(0);
            } else {

            }


        } else {
            return false;
        }


        return true;
    }

    private static List<String> getHeaderValues(String key, CarbonMessage cMsg) {
        String value = cMsg.getHeader(key);
        if (value != null) {
            String[] values = FIELD_COMMA_PATTERN.split(value);
            return Arrays.stream(values).collect(Collectors.toList());
        }
        return null;
    }

    private static void setExposedAllowedOrigins() {
        List<String> exposeHeaders = resourceCors.get(Constants.EXPOSE_HEADERS);
        String exposeHeaderResponse = concatValues(exposeHeaders, false);
        if (!exposeHeaderResponse.isEmpty()) {
            getResponseCors().put(Constants.AC_EXPOSE_HEADERS, exposeHeaderResponse);
        }
    }

    private static void setAllowOriginAndCredentials(List<String> effectiveOrigins) {
        String allowCreds = resourceCors.get(Constants.ALLOW_CREDENTIALS).get(0);
        getResponseCors().put(Constants.AC_ALLOW_CREDENTIALS, allowCreds);
        String originResponse;
        if (allowCreds.equals("false") && effectiveOrigins.size() != 0) {
            originResponse = "*";
        } else {
            originResponse = concatValues(effectiveOrigins, true);
        }
        getResponseCors().put(Constants.AC_ALLOW_ORIGIN, originResponse);
    }

    private static String concatValues(List<String> values, boolean spaceSeparated) {
        StringBuilder sb = new StringBuilder();

        for (int x = 0; x < values.size(); ++x) {
            sb.append(values.get(x));
            if (x != values.size() - 1) {
                if (spaceSeparated) {
                    sb.append(" ");
                } else {
                    sb.append(", ");
                }
            }
        }

        return sb.toString();
    }

    private static List<String> getOriginValues(String originValue) {
        String[] origins = SPACE_PATTERN.split(originValue);
        return Arrays.stream(origins).filter(value -> (value.contains("://"))).collect(Collectors.toList());
    }

    public static Map<String, String> getResponseCors() {
        return responseCors;
    }

    public static void generateCORSHeaders(BMessage message) {
        if (isCorsResponseHeadersAvailable) {
            responseCors.entrySet().stream().forEach(header -> {
                message.value().setHeader(header.getKey(), header.getValue());
            });
        }
    }
}
