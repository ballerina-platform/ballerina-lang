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

import com.google.protobuf.Descriptors;
import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import org.ballerinalang.net.grpc.exception.GrpcClientException;
import org.ballerinalang.net.grpc.stubs.GrpcBlockingStub;
import org.ballerinalang.net.grpc.stubs.GrpcNonBlockingStub;
import org.ballerinalang.net.grpc.stubs.ProtoFileDefinition;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * This is Factory class for gRPC client connector.
 *
 * @since 1.0.0
 */
public final class ClientConnectorFactory {
    private String serviceFullName;
    // Static method descriptors that strictly reflect the proto.
    private Map<String, MethodDescriptor<Message, Message>> methodDescriptorMap = new HashMap<>();
    
    public ClientConnectorFactory(ProtoFileDefinition protoFileDefinition) throws GrpcClientException {
        Descriptors.FileDescriptor fileDescriptor = protoFileDefinition.getDescriptor();

        if (fileDescriptor.getFile().getServices().isEmpty()) {
            throw new GrpcClientException("No service found in proto definition file");
        }
        if (fileDescriptor.getFile().getServices().size() > 1) {
            throw new GrpcClientException("Multiple service definitions in signal proto file is not supported. Number" +
                    " of service found: " + fileDescriptor.getFile().getServices().size());
        }
        Descriptors.ServiceDescriptor serviceDescriptor = fileDescriptor.getFile().getServices().get(0);
        this.serviceFullName = serviceDescriptor.getFullName();
        registryMethodDescriptorMap(serviceDescriptor);
    }

    private void registryMethodDescriptorMap(Descriptors.ServiceDescriptor serviceDescriptor) {
        Map<String, MethodDescriptor<Message, Message>> descriptorMap = new HashMap<>();
        for (Descriptors.MethodDescriptor methodDescriptor:  serviceDescriptor.getMethods()) {
            String methodName = methodDescriptor.getName();
            Descriptors.Descriptor reqMessage = methodDescriptor.getInputType();
            Descriptors.Descriptor resMessage = methodDescriptor.getOutputType();
            String fullMethodName = generateFullMethodName(serviceFullName, methodName);
            MethodDescriptor<Message, Message> descriptor =
                    MethodDescriptor.<Message, Message>newBuilder()
                            .setType(MessageUtils.getMethodType(methodDescriptor.toProto()))
                            .setFullMethodName(fullMethodName)
                            .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                    org.ballerinalang.net.grpc.Message
                                            .newBuilder(reqMessage.getName()).build()))
                            .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                    org.ballerinalang.net.grpc.Message
                                            .newBuilder(resMessage.getName()).build()))
                            .setSchemaDescriptor(methodDescriptor)
                            .build();
            descriptorMap.put(fullMethodName, descriptor);
            MessageRegistry messageRegistry = MessageRegistry.getInstance();
            messageRegistry.addMethodDescriptor(fullMethodName, methodDescriptor);
            messageRegistry.addMessageDescriptor(reqMessage.getName(), reqMessage);
            for (Descriptors.Descriptor nestedType : reqMessage.getNestedTypes()) {
                messageRegistry.addMessageDescriptor(nestedType.getName(), nestedType);
            }
            messageRegistry.addMessageDescriptor(resMessage.getName(), resMessage);
            for (Descriptors.Descriptor nestedType : resMessage.getNestedTypes()) {
                messageRegistry.addMessageDescriptor(nestedType.getName(), nestedType);
            }
        }
        methodDescriptorMap = Collections.unmodifiableMap(descriptorMap);
    }
    
    /**
     * Creates a new async stub that supports all call types for the service.
     */
    public GrpcNonBlockingStub newNonBlockingStub(Channel channel) {
        return new GrpcNonBlockingStub(channel, methodDescriptorMap);
    }
    
    /**
     * Creates a new blocking-style stub that supports unary and streaming output calls on the service.
     */
    public GrpcBlockingStub newBlockingStub(Channel channel) {
        return new GrpcBlockingStub(channel, methodDescriptorMap);
    }

}
