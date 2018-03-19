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
package org.ballerinalang.net.grpc.proto.definition;

import com.google.protobuf.DescriptorProtos;
import org.ballerinalang.net.grpc.proto.ServiceProtoConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Service Definition Builder.
 *
 * @since 1.0.0
 */
public class Service {
    private DescriptorProtos.ServiceDescriptorProto serviceDescriptor;
    private List<Method> methodList = new ArrayList<>();
    
    private Service(DescriptorProtos.ServiceDescriptorProto serviceDescriptor) {
        this.serviceDescriptor = serviceDescriptor;
    }
    
    public static Builder newBuilder(String serviceName) {
        return new Builder(serviceName);
    }
    
    public DescriptorProtos.ServiceDescriptorProto getServiceDescriptor() {
        return serviceDescriptor;
    }
    
    public String getServiceDefinition() {
        StringBuilder serviceDefinition = new StringBuilder();
        serviceDefinition.append("service ").append(serviceDescriptor.getName()).append(" {").append
                (ServiceProtoConstants.NEW_LINE_CHARACTER);
        
        for (Method method : methodList) {
            serviceDefinition.append("\t").append(method.getMethodDefinition());
        }
        serviceDefinition.append("}").append(ServiceProtoConstants.NEW_LINE_CHARACTER);
        return serviceDefinition.toString();
    }
    
    /**
     * ServiceDefinition.Builder
     */
    public static class Builder {
        private DescriptorProtos.ServiceDescriptorProto.Builder serviceBuilder;
        private List<Method> methodList = new ArrayList<>();
        
        public Builder addMethod(Method methodDefinition) {
            methodList.add(methodDefinition);
            serviceBuilder.addMethod(methodDefinition.getMethodDescriptor());
            return this;
        }
        
        public Service build() {
            Service service = new Service(serviceBuilder.build());
            service.methodList = methodList;
            return service;
        }
        
        private Builder(String serviceName) {
            serviceBuilder = DescriptorProtos.ServiceDescriptorProto.newBuilder();
            serviceBuilder.setName(serviceName);
        }
    }
}
