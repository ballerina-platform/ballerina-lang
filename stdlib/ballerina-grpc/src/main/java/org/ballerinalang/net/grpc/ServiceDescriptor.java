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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Descriptor for a service.
 *
 * @since 1.0.0
 */
public final class ServiceDescriptor {

    private final String name;
    private final Collection<MethodDescriptor<?, ?>> methods;

    /**
     * Constructs a new Service Descriptor.  Users are encouraged to use {@link #newBuilder}
     * instead.
     *
     * @param name    The name of the service
     * @param methods The methods that are part of the service
     * @since 1.0.0
     */
    public ServiceDescriptor(String name, MethodDescriptor<?, ?>... methods) throws GrpcServerException {

        this(name, Arrays.asList(methods));
    }

    /**
     * Constructs a new Service Descriptor.  Users are encouraged to use {@link #newBuilder}
     * instead.
     *
     * @param name    The name of the service
     * @param methods The methods that are part of the service
     * @since 1.0.0
     */
    public ServiceDescriptor(String name, Collection<MethodDescriptor<?, ?>> methods) throws GrpcServerException {
        this(newBuilder(name).addAllMethods(methods));
    }

    private ServiceDescriptor(Builder b) throws GrpcServerException {

        this.name = b.name;
        validateMethodNames(name, b.methods);
        this.methods = Collections.unmodifiableList(new ArrayList<>(b.methods));
    }

    /**
     * Simple name of the service. It is not an absolute path.
     *
     * @since 1.0.0
     */
    public String getName() {

        return name;
    }

    /**
     * A collection of {@link MethodDescriptor} instances describing the methods exposed by the
     * service.
     *
     * @since 1.0.0
     */
    public Collection<MethodDescriptor<?, ?>> getMethods() {

        return methods;
    }

    private static void validateMethodNames(String serviceName, Collection<MethodDescriptor<?, ?>> methods) throws
            GrpcServerException {

        Set<String> allNames = new HashSet<>(methods.size());
        for (MethodDescriptor<?, ?> method : methods) {
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
     * @since 1.1.0
     */
    public static Builder newBuilder(String name) throws GrpcServerException {

        return new Builder(name);
    }

    /**
     * A builder for a {@link ServiceDescriptor}.
     *
     * @since 1.1.0
     */
    public static final class Builder {

        private Builder(String name) throws GrpcServerException {

            setName(name);
        }

        private String name;
        private List<MethodDescriptor<?, ?>> methods = new ArrayList<>();
        private Object schemaDescriptor;

        /**
         * Sets the name.  This should be non-{@code null}.
         *
         * @param name The name of the service.
         * @return this builder.
         * @since 1.1.0
         */
        public Builder setName(String name) throws GrpcServerException {
            if (name == null) {
                throw new GrpcServerException("name cannot be null");
            }
            this.name = name;
            return this;
        }

        /**
         * Adds a method to this service.  This should be non-{@code null}.
         *
         * @param method the method to add to the descriptor.
         * @return this builder.
         * @since 1.1.0
         */
        public Builder addMethod(MethodDescriptor<?, ?> method) throws GrpcServerException {
            if (method == null) {
                throw new GrpcServerException("method cannot be null");
            }
            methods.add(method);
            return this;
        }

        /**
         * Currently not exposed.  Bulk adds methods to this builder.
         *
         * @param methods the methods to add.
         * @return this builder.
         */
        private Builder addAllMethods(Collection<MethodDescriptor<?, ?>> methods) {

            this.methods.addAll(methods);
            return this;
        }

        /**
         * Constructs a new {@link ServiceDescriptor}.  {@link #setName} should have been called with a
         * non-{@code null} value before calling this.
         *
         * @return a new ServiceDescriptor
         * @since 1.1.0
         */
        public ServiceDescriptor build() throws GrpcServerException {

            return new ServiceDescriptor(this);
        }
    }
}
