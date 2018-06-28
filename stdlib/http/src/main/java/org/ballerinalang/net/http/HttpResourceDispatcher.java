/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.net.http;

import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.net.uri.DispatcherUtil;
import org.ballerinalang.net.uri.URITemplateException;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Resource level dispatchers handler for HTTP protocol.
 */
public class HttpResourceDispatcher {

    public static HttpResource findResource(HttpService service, HTTPCarbonMessage inboundRequest)
            throws BallerinaConnectorException {

        String method = (String) inboundRequest.getProperty(HttpConstants.HTTP_METHOD);
        String subPath = (String) inboundRequest.getProperty(HttpConstants.SUB_PATH);
        subPath = sanitizeSubPath(subPath);
        Map<String, String> resourceArgumentValues = new HashMap<>();
        try {
            HttpResource resource = service.getUriTemplate().matches(subPath, resourceArgumentValues, inboundRequest);
            if (resource != null) {
                inboundRequest.setProperty(HttpConstants.RESOURCE_ARGS, resourceArgumentValues);
                inboundRequest.setProperty(HttpConstants.RESOURCES_CORS, resource.getCorsHeaders());
                return resource;
            } else {
                if (method.equals(HttpConstants.HTTP_METHOD_OPTIONS)) {
                    handleOptionsRequest(inboundRequest, service);
                } else {
                    inboundRequest.setProperty(HttpConstants.HTTP_STATUS_CODE, 404);
                    throw new BallerinaConnectorException("no matching resource found for path : "
                            + inboundRequest.getProperty(HttpConstants.TO) + " , method : " + method);
                }
                return null;
            }
        } catch (URITemplateException e) {
            throw new BallerinaConnectorException(e.getMessage());
        }
    }

    private static String sanitizeSubPath(String subPath) {
        if ("/".equals(subPath)) {
            return subPath;
        }
        if (!subPath.startsWith("/")) {
            subPath = HttpConstants.DEFAULT_BASE_PATH + subPath;
        }
        subPath = subPath.endsWith("/") ? subPath.substring(0, subPath.length() - 1) : subPath;
        return subPath;
    }

    private static void handleOptionsRequest(HTTPCarbonMessage cMsg, HttpService service)
            throws URITemplateException {
        HTTPCarbonMessage response = HttpUtil.createHttpCarbonMessage(false);
        if (cMsg.getHeader(HttpHeaderNames.ALLOW.toString()) != null) {
            response.setHeader(HttpHeaderNames.ALLOW.toString(), cMsg.getHeader(HttpHeaderNames.ALLOW.toString()));
        } else if (service.getBasePath().equals(cMsg.getProperty(HttpConstants.TO))
                && !service.getAllAllowedMethods().isEmpty()) {
            response.setHeader(HttpHeaderNames.ALLOW.toString(),
                               DispatcherUtil.concatValues(service.getAllAllowedMethods(), false));
        } else {
            cMsg.setProperty(HttpConstants.HTTP_STATUS_CODE, 404);
            throw new BallerinaConnectorException("no matching resource found for path : "
                    + cMsg.getProperty(HttpConstants.TO) + " , method : " + "OPTIONS");
        }
        CorsHeaderGenerator.process(cMsg, response, false);
        response.setProperty(HttpConstants.HTTP_STATUS_CODE, 200);
        response.addHttpContent(new DefaultLastHttpContent());
        HttpUtil.sendOutboundResponse(cMsg, response);
    }
}
