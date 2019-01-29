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

import org.ballerinalang.net.grpc.exception.GrpcServerException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Descriptor for a service.
 * <p>
 * Referenced from grpc-java implementation.
 * <p>
 * @since 0.980.0
 */
public final class ServiceDescriptor {

    private final String name;
    private final Collection<MethodDescriptor> methods;

    /**
     * Constructs a new Service Descriptor.
     *
     * @param name    The name of the service
     * @param methods The methods that are part of the service
     *
     * @throws GrpcServerException method name validation failure
     */
    private ServiceDescriptor(String name, Collection<MethodDescriptor> methods) throws GrpcServerException {
        this.name = name;
        validateMethodNames(name, methods);
        this.methods = Collections.unmodifiableList(new ArrayList<>(methods));
    }

    /**
     * Simple name of the service. It is not an absolute path.
     *
     * @return Service name.
     */
    public String getName() {
        return name;
    }

    /**
     * A collection of {@link MethodDescriptor} instances describing the methods exposed by the
     * service.
     *
     * @return list of service methods.
     */
    public Collection<MethodDescriptor> getMethods() {
        return methods;
    }

    private static void validateMethodNames(String serviceName, Collection<MethodDescriptor> methods) throws
            GrpcServerException {
        Set<String> allNames = new HashSet<>(methods.size());
        for (MethodDescriptor method : methods) {
            if (method == null) {
                throw new GrpcServerException("method cannot be null");
            }
            String methodServiceName =
                    MethodDescriptor.extractFullServiceName(method.getFullMethodName());
            if (!serviceName.equals(methodServiceName)) {
                throw new GrpcServerException(String.format("service names %s != %s", methodServiceName, serviceName));
            }
            if (!allNames.add(method.getFullMethodName())) {
                throw new GrpcServerException(String.format("duplicate name %s", method.getFullMethodName()));
            }
        }
    }

    /**
     * Creates a new builder for a {@link ServiceDescriptor}.
     *
     * @param name Service name.
     * @return new ServiceDescriptor.Builder instance.
     * @throws GrpcServerException if failed.
     */
    public static Builder newBuilder(String name) throws GrpcServerException {
        return new Builder(name);
    }

    /**
     * A builder for a {@link ServiceDescriptor}.
     * <p>
     * Referenced from grpc-java implementation.
     *
     */
    public static final class Builder {
        private String name;
        private List<MethodDescriptor> methods = new ArrayList<>();

        private Builder(String name) throws GrpcServerException {
            if (name == null) {
                throw new GrpcServerException("name cannot be null");
            }
            this.name = name;
        }

        /**
         * Bulk adds methods to this builder.
         *
         * @param methods collection of methods.
         * @return this builder.
         */
        public Builder addAllMethods(Collection<MethodDescriptor> methods) {
            this.methods.addAll(methods);
            return this;
        }

        /**
         * Constructs a new {@link ServiceDescriptor}.
         *
         * @return a new instance.
         * @throws GrpcServerException fail when initializing
         */
        public ServiceDescriptor build() throws GrpcServerException {
            return new ServiceDescriptor(name, methods);
        }
    }
}
