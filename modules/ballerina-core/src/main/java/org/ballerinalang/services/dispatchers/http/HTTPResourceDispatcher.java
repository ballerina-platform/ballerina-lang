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

package org.ballerinalang.services.dispatchers.http;

import org.ballerinalang.bre.Context;
import org.ballerinalang.logging.BLogManager;
import org.ballerinalang.model.Resource;
import org.ballerinalang.model.Service;
import org.ballerinalang.model.util.MessageUtils;
import org.ballerinalang.runtime.message.StringDataSource;
import org.ballerinalang.services.dispatchers.ResourceDispatcher;
import org.ballerinalang.services.dispatchers.uri.ParamProcessor;
import org.ballerinalang.services.dispatchers.uri.URITemplateException;
import org.ballerinalang.util.codegen.ResourceInfo;
import org.ballerinalang.util.codegen.ServiceInfo;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Resource level dispatchers handler for HTTP protocol.
 */
public class HTTPResourceDispatcher implements ResourceDispatcher {

    private static final Logger log = LoggerFactory.getLogger(HTTPResourceDispatcher.class);
    private static final Logger bLog = LoggerFactory.getLogger(BLogManager.BALLERINA_ROOT_LOGGER_NAME);

    @Override
    public ResourceInfo findResource(ServiceInfo service, CarbonMessage cMsg, CarbonCallback callback)
            throws BallerinaException {

        String method = (String) cMsg.getProperty(Constants.HTTP_METHOD);
        String subPath = (String) cMsg.getProperty(Constants.SUB_PATH);
        subPath = sanitizeSubPath(subPath);
        Map<String, String> resourceArgumentValues = new HashMap<>();
        try {
            ResourceInfo resource = service.getUriTemplate().matches(subPath, resourceArgumentValues, cMsg);
            if (resource != null) {
                populateQueryParams(cMsg, resourceArgumentValues);
                populateFormParams(resource, cMsg, resourceArgumentValues);
                populateHeaderParams(resource, cMsg, resourceArgumentValues);
                cMsg.setProperty(org.ballerinalang.runtime.Constants.RESOURCE_ARGS, resourceArgumentValues);
                return resource;
            }
            cMsg.setProperty(Constants.HTTP_STATUS_CODE, 404);
            throw new BallerinaException("no matching resource found for path : "
                    + cMsg.getProperty(org.wso2.carbon.messaging.Constants.TO) + " , method : " + method);

        } catch (UnsupportedEncodingException | URITemplateException e) {
            throw new BallerinaException(e.getMessage());
        }
    }

    public Resource findResource(Service service, CarbonMessage cMsg, CarbonCallback callback, Context balContext)
            throws BallerinaException {
        return null;
    }

    @Override
    public String getProtocol() {
        return Constants.PROTOCOL_HTTP;
    }

    private String sanitizeSubPath (String subPath) {
        if (!"/".equals(subPath)) {
            if (!subPath.startsWith("/")) {
                subPath = Constants.DEFAULT_BASE_PATH + subPath;
            }
            subPath = subPath.endsWith("/") ? subPath.substring(0, subPath.length() - 1) : subPath;
        }
        return subPath;
    }

    private void populateQueryParams(CarbonMessage cMsg, Map<String, String> resourceArgValues)
            throws UnsupportedEncodingException {
        if (cMsg.getProperty(Constants.QUERY_STR) != null) {
            ParamProcessor.processQueryParams
                    ((String) cMsg.getProperty(Constants.QUERY_STR))
                    .entrySet().forEach(entry -> {
                        if (resourceArgValues.put(entry.getKey(), entry.getValue()) != null) {
                            bLog.warn("parameter key \"" + entry.getKey() + "\" is redeclared.");
                        }
                    });
        }
    }

    private void populateFormParams(ResourceInfo resource, CarbonMessage cMsg, Map<String, String> resourceArgValues)
            throws UnsupportedEncodingException {
        if (resource.getParamNames().length > 1) {
            String contentType = cMsg.getHeader(Constants.CONTENT_TYPE_HEADER);
            if (contentType != null && contentType.contains(Constants.APPLICATION_X_WWW_FORM_URLENCODED)) {
                String payload;
                if (cMsg.isAlreadyRead()) {
                    payload = cMsg.getMessageDataSource().getMessageAsString();
                } else {
                    payload = MessageUtils.getStringFromInputStream(cMsg.getInputStream());
                    StringDataSource stringDataSource = new StringDataSource(payload);
                    cMsg.setMessageDataSource(stringDataSource);
                    cMsg.setAlreadyRead(true);
                }
                if (!payload.isEmpty()) {
                    ParamProcessor.processQueryParams(payload).entrySet().forEach(entry -> {
                        if (resourceArgValues.put(entry.getKey(), entry.getValue()) != null) {
                            bLog.warn("parameter key \"" + entry.getKey() + "\" is redeclared.");
                        }
                    });
                }
            }
        }
    }

    private void populateHeaderParams(ResourceInfo resource, CarbonMessage cMsg, Map<String, String> resourceArgValues)
            throws UnsupportedEncodingException {
        if (resource.getParamNames().length > 1) {
            ParamProcessor.processHeaderParams(cMsg.getHeaders().getAll())
                    .entrySet().forEach(entry -> {
                        if (resourceArgValues.put(entry.getKey(), entry.getValue()) != null) {
                            bLog.warn("parameter key \"" + entry.getKey() + "\" is redeclared.");
                        }
                    });
        }
    }
}
