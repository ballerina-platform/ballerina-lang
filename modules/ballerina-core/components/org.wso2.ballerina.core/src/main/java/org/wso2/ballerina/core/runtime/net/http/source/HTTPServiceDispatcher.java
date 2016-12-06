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

import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.runtime.core.BalCallback;
import org.wso2.ballerina.core.runtime.core.BalContext;
import org.wso2.ballerina.core.runtime.core.dispatching.ServiceDispatcher;

import java.util.HashMap;
import java.util.Map;

/**
 * Service Dispatcher for HTTP Protocol
 *
 * @since 1.0.0
 */
public class HTTPServiceDispatcher implements ServiceDispatcher {

    // Outer Map key=interface, Inner Map key=basePath
    private Map<String, Map<String, Service>> services = new HashMap<>();

    @Override
    public boolean dispatch(BalContext context, BalCallback callback) {
        return false;
    }

    @Override
    public String getProtocol() {
        return "http";
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
            servicesOnInterface = new HashMap<>();
            services.put(listenerInterface, servicesOnInterface);
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
            }
        }
    }
}
