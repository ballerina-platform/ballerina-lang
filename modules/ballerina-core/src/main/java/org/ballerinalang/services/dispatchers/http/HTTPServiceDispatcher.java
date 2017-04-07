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
import org.ballerinalang.model.AnnotationAttachment;
import org.ballerinalang.model.Resource;
import org.ballerinalang.model.Service;
import org.ballerinalang.services.dispatchers.ServiceDispatcher;
import org.ballerinalang.services.dispatchers.uri.URITemplateException;
import org.ballerinalang.services.dispatchers.uri.URIUtil;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;

/**
 * Service Dispatcher for HTTP Protocol.
 *
 * @since 0.8.0
 */
public class HTTPServiceDispatcher implements ServiceDispatcher {

    private static final Logger log = LoggerFactory.getLogger(HTTPServiceDispatcher.class);


    public Service findService(CarbonMessage cMsg, CarbonCallback callback, Context balContext) {

        try {
            String interfaceId = getInterface(cMsg);
            Map<String, Service> servicesOnInterface = HTTPServicesRegistry
                    .getInstance().getServicesByInterface(interfaceId);
            if (servicesOnInterface == null) {
                throw new BallerinaException("No services found for interface : " + interfaceId);
            }
            String uriStr = (String) cMsg.getProperty(org.wso2.carbon.messaging.Constants.TO);
            //replace multiple slashes from single slash if exist in request path to enable
            // dispatchers when request path contains multiple slashes
            URI requestUri = URI.create(uriStr.replaceAll("//+", Constants.DEFAULT_BASE_PATH));
            if (requestUri == null) {
                throw new BallerinaException("uri not found in the message or found an invalid URI.");
            }

            // Most of the time we will find service from here
            String basePath = findTheMostSpecificBasePath(requestUri.getPath(), servicesOnInterface);
            Service service = servicesOnInterface.get(basePath);
            if (service == null) {
                throw new BallerinaException("no service found to handle incoming request recieved to : " + uriStr);
            }

            String subPath = URIUtil.getSubPath(requestUri.getPath(), basePath);
            cMsg.setProperty(Constants.BASE_PATH, basePath);
            cMsg.setProperty(Constants.SUB_PATH, subPath);
            cMsg.setProperty(Constants.QUERY_STR, requestUri.getQuery());
            //store query params comes with request as it is
            cMsg.setProperty(Constants.RAW_QUERY_STR, requestUri.getRawQuery());

            return service;
        } catch (Throwable e) {
            throw new BallerinaException(e.getMessage(), balContext);
        }
    }



    @Override
    public String getProtocol() {
        return Constants.PROTOCOL_HTTP;
    }

    @Override
    public void serviceRegistered(Service service) {
        HTTPServicesRegistry.getInstance().registerService(service);
        for (Resource resource : service.getResources()) {
            AnnotationAttachment subPathAnnotation = resource.getAnnotation(Constants.PROTOCOL_HTTP
                    , Constants.ANNOTATION_NAME_PATH);
            String subPathAnnotationVal;
            if (subPathAnnotation != null && subPathAnnotation.getValue() != null  &&
                    !subPathAnnotation.getValue().trim().isEmpty()) {
                subPathAnnotationVal = subPathAnnotation.getValue();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Path not specified in the Resource, using default sub path");
                }
                subPathAnnotationVal = Constants.DEFAULT_SUB_PATH;
            }

            try {
                service.getUriTemplate().parse(subPathAnnotationVal, resource);
            } catch (URITemplateException e) {
                log.error("Failed to parse URIs", e);
            }
        }
    }

    @Override
    public void serviceUnregistered(Service service) {
        HTTPServicesRegistry.getInstance().unregisterService(service);
    }

    protected String getInterface(CarbonMessage cMsg) {
        String interfaceId = (String) cMsg.getProperty(org.wso2.carbon.messaging.Constants.LISTENER_INTERFACE_ID);
        if (interfaceId == null) {
            if (log.isDebugEnabled()) {
                log.debug("Interface id not found on the message, hence using the default interface");
            }
            interfaceId = Constants.DEFAULT_INTERFACE;
        }

        return interfaceId;
    }

    private String findTheMostSpecificBasePath(String requestURIPath, Map<String, Service> services) {
        Object[] keys = services.keySet().toArray();
        Arrays.sort(keys, (o1, o2) -> o2.toString().length() - o1.toString().length());

        for (Object key : keys) {
            if (requestURIPath.contains(key.toString())) {
                return key.toString();
            }
        }

        if (services.containsKey(Constants.DEFAULT_BASE_PATH)) {
            return Constants.DEFAULT_BASE_PATH;
        }
        return null;
    }
}
