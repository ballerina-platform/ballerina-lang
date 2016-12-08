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

package org.wso2.ballerina.core.runtime.net.http.source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.runtime.core.BalCallback;
import org.wso2.ballerina.core.runtime.core.BalContext;
import org.wso2.ballerina.core.runtime.core.dispatching.ServiceDispatcher;
import org.wso2.carbon.messaging.CarbonMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Service Dispatcher for HTTP Protocol
 *
 * @since 1.0.0
 */
public class HTTPServiceDispatcher implements ServiceDispatcher {

    private static final Logger log = LoggerFactory.getLogger(HTTPServiceDispatcher.class);

    // Outer Map key=interface, Inner Map key=basePath
    private Map<String, Map<String, Service>> services = new HashMap<>();

    @Override
    public boolean dispatch(BalContext context, BalCallback callback) {

        CarbonMessage cMsg = context.getCarbonMessage();
        String interfaceId = (String) cMsg.getProperty(org.wso2.carbon.messaging.Constants.LISTENER_INTERFACE_ID);
        if (interfaceId == null) {
            if (log.isDebugEnabled()) {
                log.debug("Interface id not found on the message, hence using the default interface");
            }
            interfaceId = Constants.DEFAULT_INTERFACE;
        }

        Map<String, Service> servicesOnInterface = services.get(interfaceId);
        if (servicesOnInterface == null) {
            log.error("No services found for interface : " + interfaceId);
            //TODO: Throw an exception
            return false;
        }

        String uri = (String) cMsg.getProperty(org.wso2.carbon.messaging.Constants.TO);
        if (uri == null) {
            log.error("Uri not found in the message");
            return false;
            //TODO: Throw an exception
        }
        String[] path = uri.split("/");
        String basePath = path[0];

        Service service = servicesOnInterface.get(basePath);  // 90% of the time we will find service from here
        if (service == null) {
            for (int i = 1; i < path.length; i++) {
                basePath = basePath.concat("/").concat(path[i]);
                service = servicesOnInterface.get(basePath);
                if (service != null) {
                    context.setProperty(Constants.BASE_PATH, basePath);
                    context.setProperty(Constants.SUB_PATH, uri.split(Constants.BASE_PATH)[1]);
                    break;
                }
            }
        }

        if (service == null) {
            log.error("No Service found to handle request sent to : " + uri);
            return false;
            //TODO: Throw an exception
        }
        return service.execute(context, callback);
    }

    @Override
    public String getProtocol() {
        return Constants.PROTOCOL_HTTP;
    }

    @Override
    public void serviceRegistered(Service service) {

        String listenerInterface = Constants.DEFAULT_INTERFACE;
        Annotation sourceAnnotation = service.getAnnotation(Constants.ANNOTATION_NAME_SOURCE);
        if (sourceAnnotation != null) {
            String sourceInterfaceVal =
                    sourceAnnotation.getValueOfKeyValuePair(Constants.ANNOTATION_SOURCE_KEY_INTERFACE);
            if (sourceInterfaceVal != null) {   //TODO: Filter non-http protocols
                listenerInterface = sourceInterfaceVal;
            }
        }

        String basePath = Constants.DEFAULT_BASE_PATH;
        Annotation basePathAnnotation = service.getAnnotation(Constants.ANNOTATION_NAME_BASE_PATH);
        if (basePathAnnotation != null) {
            basePath = basePathAnnotation.getValue();
        }

        Map<String, Service> servicesOnInterface = services.get(listenerInterface);
        if (servicesOnInterface == null) {
            // Assumption : this is always sequential, no two simultaneous calls can get here
            servicesOnInterface = new HashMap<>();
            services.put(listenerInterface, servicesOnInterface);
            HTTPListenerManager.getInstance().bindInterface(listenerInterface);
        }
        if (servicesOnInterface.containsKey(basePath)) {
            //TODO: Through deployment exception
            throw new RuntimeException("Service with base path :" + basePath
                                       + " already exists in listener : " + listenerInterface);
        }

        servicesOnInterface.put(basePath, service);

    }

    @Override
    public void serviceUnregistered(Service service) {

        String listenerInterface = Constants.DEFAULT_INTERFACE;
        Annotation sourceAnnotation = service.getAnnotation(Constants.ANNOTATION_NAME_SOURCE);
        if (sourceAnnotation != null) {
            String sourceInterfaceVal =
                    sourceAnnotation.getValueOfKeyValuePair(Constants.ANNOTATION_SOURCE_KEY_INTERFACE);
            if (sourceInterfaceVal != null) {
                listenerInterface = sourceInterfaceVal;
            }
        }

        String basePath = Constants.DEFAULT_BASE_PATH;
        Annotation basePathAnnotation = service.getAnnotation(Constants.ANNOTATION_NAME_BASE_PATH);
        if (basePathAnnotation != null) {
            basePath = basePathAnnotation.getValue();
        }

        Map<String, Service> servicesOnInterface = services.get(listenerInterface);
        if (servicesOnInterface != null) {
            servicesOnInterface.remove(basePath);
            if (servicesOnInterface.isEmpty()) {
                services.remove(listenerInterface);
                HTTPListenerManager.getInstance().unbindInterface(listenerInterface);
            }
        }
    }
}
