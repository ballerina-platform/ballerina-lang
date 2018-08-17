/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.net.grpc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * gRPC Services Registry.
 *
 * @since 0.980.0
 */
public class ServicesRegistry {

    private final List<ServerServiceDefinition> services;
    private final Map<String, ServerMethodDefinition> methods;

    private ServicesRegistry(
            List<ServerServiceDefinition> services, Map<String, ServerMethodDefinition> methods) {
        this.services = services;
        this.methods = methods;
    }

    /**
     * Returns the service definitions in this registry.
     */
    public List<ServerServiceDefinition> getServices() {
        return services;
    }

    ServerMethodDefinition lookupMethod(String methodName) {
        return methods.get(methodName);
    }

    /**
     * gRPC Services Registry Builder.
     */
    public static class Builder {

        // Store per-service first, to make sure services are added/replaced atomically.
        private final HashMap<String, ServerServiceDefinition> services = new LinkedHashMap<>();

        public void addService(ServerServiceDefinition service) {
            services.put(service.getServiceDescriptor().getName(), service);
        }

        public ServicesRegistry build() {
            Map<String, ServerMethodDefinition> map = new HashMap<>();
            for (ServerServiceDefinition service : services.values()) {
                for (ServerMethodDefinition method : service.getMethods()) {
                    map.put(method.getMethodDescriptor().getFullMethodName(), method);
                }
            }
            return new ServicesRegistry(
                    Collections.unmodifiableList(new ArrayList<>(services.values())),
                    Collections.unmodifiableMap(map));
        }
    }
}
