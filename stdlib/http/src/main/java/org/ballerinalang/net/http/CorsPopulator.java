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
import org.ballerinalang.net.uri.DispatcherUtil;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * CorsRegistry cache CORS headers at service/resource level during load time.
 *
 * @since 0.93
 */
public class CorsPopulator {

    @Deprecated
    public static void populateServiceCors(HttpService service) {
        CorsHeaders corsHeaders = populateAndGetCorsHeaders(
                HttpUtil.getServiceConfigAnnotation(service.getBalService(), HttpConstants.HTTP_PACKAGE_PATH));
        service.setCorsHeaders(corsHeaders);
        if (!corsHeaders.isAvailable()) {
            return;
        }
        if (corsHeaders.getAllowOrigins() == null) {
            corsHeaders.setAllowOrigins(Stream.of("*").collect(Collectors.toList()));
        }
        if (corsHeaders.getAllowMethods() == null) {
            corsHeaders.setAllowMethods(Stream.of("*").collect(Collectors.toList()));
        }
    }

    @Deprecated
    public static void processResourceCors(HttpResource resource, HttpService service) {
        CorsHeaders corsHeaders = populateAndGetCorsHeaders(
                HttpUtil.getResourceConfigAnnotation(resource.getBalResource(), HttpConstants.HTTP_PACKAGE_PATH));
        if (!corsHeaders.isAvailable()) {
            //resource doesn't have cors headers, hence use service cors
            resource.setCorsHeaders(service.getCorsHeaders());
            return;
        }
        resource.setCorsHeaders(corsHeaders);
        if (corsHeaders.getAllowOrigins() == null) {
            corsHeaders.setAllowOrigins(Stream.of("*").collect(Collectors.toList()));
        }
        if (corsHeaders.getAllowMethods() != null) {
            return;
        }
        if (resource.getMethods() != null) {
            corsHeaders.setAllowMethods(resource.getMethods());
            return;
        }
        corsHeaders.setAllowMethods(DispatcherUtil.addAllMethods());
    }

    private static CorsHeaders populateAndGetCorsHeaders(Annotation configAnnotInfo) {
        CorsHeaders corsHeaders = new CorsHeaders();
        if (configAnnotInfo == null) {
            return corsHeaders;
        }
        AnnAttrValue allowOriginAttr = configAnnotInfo.getAnnAttrValue(HttpConstants.ALLOW_ORIGIN);
        if (allowOriginAttr != null) {
            corsHeaders.setAllowOrigins(DispatcherUtil.getValueList(allowOriginAttr, null));
        }
        AnnAttrValue allowCredentials = configAnnotInfo.getAnnAttrValue(HttpConstants.ALLOW_CREDENTIALS);
        if (allowCredentials != null) {
            corsHeaders.setAllowCredentials(allowCredentials.getBooleanValue() ? 1 : 0);
        }
        AnnAttrValue allowMethodsAttr = configAnnotInfo.getAnnAttrValue(HttpConstants.ALLOW_METHODS);
        if (allowMethodsAttr != null) {
            corsHeaders.setAllowMethods(DispatcherUtil.getValueList(allowMethodsAttr, null));
        }
        AnnAttrValue allowHeadersAttr = configAnnotInfo.getAnnAttrValue(HttpConstants.ALLOW_HEADERS);
        if (allowHeadersAttr != null) {
            corsHeaders.setAllowHeaders(DispatcherUtil.getValueList(allowHeadersAttr, null));
        }
        AnnAttrValue maxAgeAttr = configAnnotInfo.getAnnAttrValue(HttpConstants.MAX_AGE);
        if (maxAgeAttr != null) {
            corsHeaders.setMaxAge(maxAgeAttr.getIntValue());
        }
        AnnAttrValue exposeHeadersAttr = configAnnotInfo.getAnnAttrValue(HttpConstants.EXPOSE_HEADERS);
        if (exposeHeadersAttr != null) {
            corsHeaders.setExposeHeaders(DispatcherUtil.getValueList(exposeHeadersAttr, null));
        }
        return corsHeaders;
    }

    private CorsPopulator() {
    }
}
