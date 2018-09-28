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

package org.ballerinalang.net.grpc.builder.components;

import org.ballerinalang.net.grpc.exception.BalGenerationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service stub definition bean class.
 *
 * @since 0.982.0
 */
public class ServiceStub {
    private String stubType;
    private String serviceName;
    private List<Method> blockingFunctions = new ArrayList<>();
    private List<Method> nonBlockingFunctions = new ArrayList<>();
    private List<Method> streamingFunctions = new ArrayList<>();

    private ServiceStub(String serviceName, String stubType) {
        this.serviceName = serviceName;
        this.stubType = stubType;
    }

    public static ServiceStub.Builder newBuilder(String serviceName) {
        return new ServiceStub.Builder(serviceName);
    }

    public String getStubType() {
        return stubType;
    }

    public String getServiceName() {
        return serviceName;
    }

    public List<Method> getBlockingFunctions() {
        return Collections.unmodifiableList(blockingFunctions);
    }

    public List<Method> getNonBlockingFunctions() {
        return Collections.unmodifiableList(nonBlockingFunctions);
    }

    public List<Method> getStreamingFunctions() {
        return Collections.unmodifiableList(streamingFunctions);
    }

    /**
     * Service stub definition builder.
     */
    public static class Builder {
        String serviceName;
        List<Method> methodList = new ArrayList<>();
        StubType stubType;

        private Builder(String serviceName) {
            this.serviceName = serviceName;
        }

        public Builder addMethod(Method method) {
            methodList.add(method);
            return this;
        }

        public Builder setType(StubType stubType) {
            this.stubType = stubType;
            return this;
        }

        public ServiceStub build() {
            ServiceStub serviceStub = new ServiceStub(serviceName, stubType.getType());
            for (Method method : methodList) {
                switch (method.getMethodType()) {
                    case UNARY:
                        if (stubType == StubType.BLOCKING) {
                            serviceStub.blockingFunctions.add(method);
                        } else {
                            serviceStub.nonBlockingFunctions.add(method);
                        }
                        break;
                    case SERVER_STREAMING:
                        serviceStub.nonBlockingFunctions.add(method);
                        break;
                    case CLIENT_STREAMING:
                    case BIDI_STREAMING:
                        serviceStub.streamingFunctions.add(method);
                        break;
                    default:
                        throw new BalGenerationException("Method type is unknown or not supported.");
                }
            }
            return serviceStub;
        }
    }

    /**
     * Service stub type enum.
     */
    public enum StubType {
        BLOCKING("blocking"),
        NONBLOCKING("non-blocking");

        private String type;

        StubType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
}
