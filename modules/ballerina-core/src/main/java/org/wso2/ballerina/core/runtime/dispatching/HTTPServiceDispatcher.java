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

package org.wso2.ballerina.core.runtime.dispatching;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.nativeimpl.connectors.BallerinaConnectorManager;
import org.wso2.ballerina.core.runtime.dispatching.uri.URIUtil;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.ServerConnector;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;


import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Service Dispatcher for HTTP Protocol.
 *
 * @since 0.8.0
 */
public class HTTPServiceDispatcher implements ServiceDispatcher {

    private static final Logger log = LoggerFactory.getLogger(HTTPServiceDispatcher.class);

    // Outer Map key=interface, Inner Map key=basePath
    private Map<String, Map<String, Service>> services = new HashMap<>();

    public Service findService(CarbonMessage cMsg, CarbonCallback callback, Context balContext) {

        try {
            String interfaceId = (String) cMsg.getProperty(org.wso2.carbon.messaging.Constants.LISTENER_INTERFACE_ID);
            if (interfaceId == null) {
                if (log.isDebugEnabled()) {
                    log.debug("interface id not found on the message, hence using the default interface");
                }
                interfaceId = Constants.DEFAULT_INTERFACE;
            }

            Map<String, Service> servicesOnInterface = services.get(interfaceId);
            if (servicesOnInterface == null) {
                throw new BallerinaException("No services found for interface : " + interfaceId);
            }

            String uriStr = (String) cMsg.getProperty(org.wso2.carbon.messaging.Constants.TO);
            //replace multiple slashes from single slash if exist in request path to enable
            // dispatching when request path contains multiple slashes
            URI requestUri = URI.create(uriStr.replaceAll("//+", "/"));
            if (requestUri == null) {
                throw new BallerinaException("uri not found in the message or found an invalid URI.");
            }

            String basePath = URIUtil.getFirstPathSegment(requestUri.getPath());
            String subPath = URIUtil.getSubPath(requestUri.getPath());

            // Most of the time we will find service from here
            Service service = servicesOnInterface.get("/" + basePath);

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

        String listenerInterface = Constants.DEFAULT_INTERFACE;
        String basePath = service.getSymbolName().getName();
        for (Annotation annotation : service.getAnnotations()) {
            if (annotation.getName().equals(Constants.ANNOTATION_NAME_SOURCE)) {
                String sourceInterfaceVal = annotation
                        .getValueOfElementPair(new SymbolName(Constants.ANNOTATION_SOURCE_KEY_INTERFACE));
                if (sourceInterfaceVal != null) {   //TODO: Filter non-http protocols
                    listenerInterface = sourceInterfaceVal;
                }
            } else if (annotation.getName().equals(Constants.ANNOTATION_NAME_BASE_PATH)) {
                basePath = annotation.getValue();
            }
        }
        if (basePath.startsWith("\"")) {
            basePath = basePath.substring(1, basePath.length() - 1);
        }

        if (!basePath.startsWith("/")) {
            basePath = "/".concat(basePath);
        }

        Map<String, Service> servicesOnInterface = services.get(listenerInterface);
        if (servicesOnInterface == null) {
            // Assumption : this is always sequential, no two simultaneous calls can get here
            servicesOnInterface = new HashMap<>();
            services.put(listenerInterface, servicesOnInterface);
            ServerConnector connector = BallerinaConnectorManager.getInstance().getServerConnector(listenerInterface);
            if (connector == null) {
                throw new BallerinaException(
                        "ServerConnector interface not registered for : " + listenerInterface);
            }
            try {
                connector.start(Collections.emptyMap());
            } catch (ServerConnectorException e) {
                throw new BallerinaException("Cannot start the connector for the interface : " +
                        listenerInterface, e);
            }
        }
        if (servicesOnInterface.containsKey(basePath)) {
            throw new BallerinaException(
                    "service with base path :" + basePath + " already exists in listener : " + listenerInterface);
        }

        servicesOnInterface.put(basePath, service);

        log.info("Service deployed : " +
                (service.getSymbolName().getPkgPath() != null ? service.getSymbolName().getPkgPath() + ":" : "") +
                service.getSymbolName().getName() +
                " with context " + basePath);

    }

    @Override
    public void serviceUnregistered(Service service) {

        String listenerInterface = Constants.DEFAULT_INTERFACE;
        // String basePath = Constants.DEFAULT_BASE_PATH;
        String basePath = service.getSymbolName().getName();

        for (Annotation annotation : service.getAnnotations()) {
            if (annotation.getName().equals(Constants.ANNOTATION_NAME_SOURCE)) {
                String sourceInterfaceVal = annotation
                        .getValueOfElementPair(new SymbolName(Constants.ANNOTATION_SOURCE_KEY_INTERFACE));
                if (sourceInterfaceVal != null) {   //TODO: Filter non-http protocols
                    listenerInterface = sourceInterfaceVal;
                }
            } else if (annotation.getName().equals(Constants.ANNOTATION_NAME_BASE_PATH)) {
                basePath = annotation.getValue();
            }
        }

        if (basePath.startsWith("\"")) {
            basePath = basePath.substring(1, basePath.length() - 1);
        }

        if (!basePath.startsWith("/")) {
            basePath = "/".concat(basePath);
        }

        Map<String, Service> servicesOnInterface = services.get(listenerInterface);
        if (servicesOnInterface != null) {
            servicesOnInterface.remove(basePath);
            if (servicesOnInterface.isEmpty()) {
                services.remove(listenerInterface);
                ServerConnector connector =
                        BallerinaConnectorManager.getInstance().getServerConnector(listenerInterface);
                if (connector != null) {
                    try {
                        connector.stop();
                    } catch (ServerConnectorException e) {
                        throw new BallerinaException("Cannot stop the connector for the interface : " +
                                listenerInterface, e);
                    }
                }
            }
        }
    }
}
