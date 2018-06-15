package org.ballerinalang.net.grpc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * gRPC Services Registry.
 */
public class ServicesRegistry {

    private final List<ServerServiceDefinition> services;
    private final Map<String, ServerMethodDefinition<?, ?>> methods;

    private ServicesRegistry(
            List<ServerServiceDefinition> services, Map<String, ServerMethodDefinition<?, ?>> methods) {
        this.services = services;
        this.methods = methods;
    }

    /**
     * Returns the service definitions in this registry.
     */
    public List<ServerServiceDefinition> getServices() {
        return services;
    }


    ServerMethodDefinition<?, ?> lookupMethod(String methodName) {
        return methods.get(methodName);
    }

    /**
     * gRPC Services Registry Builder.
     */
    public static class Builder {

        // Store per-service first, to make sure services are added/replaced atomically.
        private final HashMap<String, ServerServiceDefinition> services = new LinkedHashMap<>();

        public ServicesRegistry.Builder addService(ServerServiceDefinition service) {
            services.put(service.getServiceDescriptor().getName(), service);
            return this;
        }

        public ServicesRegistry build() {
            Map<String, ServerMethodDefinition<?, ?>> map = new HashMap<>();
            for (ServerServiceDefinition service : services.values()) {
                for (ServerMethodDefinition<?, ?> method : service.getMethods()) {
                    map.put(method.getMethodDescriptor().getFullMethodName(), method);
                }
            }
            return new ServicesRegistry(
                    Collections.unmodifiableList(new ArrayList<>(services.values())),
                    Collections.unmodifiableMap(map));
        }
    }
}
