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
import org.ballerinalang.model.Service;
import org.ballerinalang.services.dispatchers.ServiceDispatcher;
import org.ballerinalang.services.dispatchers.uri.URIUtil;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

import java.net.URI;
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

            String basePath = URIUtil.getFirstPathSegment(requestUri.getPath());
            String subPath = URIUtil.getSubPath(requestUri.getPath());

            // Most of the time we will find service from here
            Service service = servicesOnInterface.get(Constants.DEFAULT_BASE_PATH + basePath);

            // Check if there is a service with default base path ("/")
            if (service == null) {
                service = servicesOnInterface.get(Constants.DEFAULT_BASE_PATH);
                basePath = Constants.DEFAULT_BASE_PATH;
            }

            if (service == null) {
                throw new BallerinaException("no service found to handle incoming request recieved to : " + uriStr);
            }

            cMsg.setProperty(Constants.BASE_PATH, basePath);
            cMsg.setProperty(Constants.SUB_PATH, subPath);
            cMsg.setProperty(Constants.QUERY_STR, requestUri.getQuery());

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
}
