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

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import org.ballerinalang.net.grpc.exception.ClientRuntimeException;
import org.ballerinalang.net.grpc.exception.GrpcClientException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.net.grpc.MessageUtils.setNestedMessages;
import static org.ballerinalang.net.grpc.MethodDescriptor.generateFullMethodName;

/**
 * This class contains proto descriptors of the service.
 *
 * @since 0.980.0
 */
public final class ServiceDefinition {
    
    private byte[] rootDescriptorData;
    private List<byte[]> dependentDescriptorData;
    private Descriptors.FileDescriptor fileDescriptor;
    
    public ServiceDefinition(byte[] rootDescriptorData, List<byte[]> depDescriptorData) {
        this.rootDescriptorData = new byte[rootDescriptorData.length];
        this.rootDescriptorData = Arrays.copyOf(rootDescriptorData, rootDescriptorData.length);
        dependentDescriptorData = depDescriptorData;
    }
    
    /**
     * Returns file descriptor of the gRPC service.
     *
     * @return file descriptor of the service.
     */
    public Descriptors.FileDescriptor getDescriptor() {
        if (fileDescriptor != null) {
            return fileDescriptor;
        }
        Descriptors.FileDescriptor[] depSet = new Descriptors.FileDescriptor[dependentDescriptorData.size()];
        int i = 0;
        for (byte[] dis : dependentDescriptorData) {
            try {
                DescriptorProtos.FileDescriptorProto fileDescriptorSet = DescriptorProtos.FileDescriptorProto
                        .parseFrom(dis);
                depSet[i] = Descriptors.FileDescriptor.buildFrom(fileDescriptorSet, new Descriptors
                        .FileDescriptor[] {});
                i++;
            } catch (InvalidProtocolBufferException | Descriptors.DescriptorValidationException e) {
                throw new ClientRuntimeException("Error while gen extracting depend descriptors. ", e);
            }
        }
        
        try (InputStream targetStream = new ByteArrayInputStream(rootDescriptorData)) {
            DescriptorProtos.FileDescriptorProto descriptorProto = DescriptorProtos.FileDescriptorProto.parseFrom
                    (targetStream);
            fileDescriptor = Descriptors.FileDescriptor.buildFrom(descriptorProto, depSet);
            return fileDescriptor;
        } catch (IOException | Descriptors.DescriptorValidationException e) {
            throw new ClientRuntimeException("Error while generating service descriptor : ", e);
        }
    }

    private Descriptors.ServiceDescriptor getServiceDescriptor() throws GrpcClientException {
        Descriptors.FileDescriptor descriptor = getDescriptor();

        if (descriptor.getFile().getServices().isEmpty()) {
            throw new GrpcClientException("No service found in proto definition file");
        }
        if (descriptor.getFile().getServices().size() > 1) {
            throw new GrpcClientException("Multiple service definitions in signal proto file is not supported. Number" +
                    " of service found: " + descriptor.getFile().getServices().size());
        }
        return descriptor.getFile().getServices().get(0);
    }

    public Map<String, MethodDescriptor> getMethodDescriptors() throws GrpcClientException {
        Map<String, MethodDescriptor> descriptorMap = new HashMap<>();
        Descriptors.ServiceDescriptor serviceDescriptor = getServiceDescriptor();

        for (Descriptors.MethodDescriptor methodDescriptor:  serviceDescriptor.getMethods()) {
            String methodName = methodDescriptor.getName();
            Descriptors.Descriptor reqMessage = methodDescriptor.getInputType();
            Descriptors.Descriptor resMessage = methodDescriptor.getOutputType();
            String fullMethodName = generateFullMethodName(serviceDescriptor.getFullName(), methodName);
            MethodDescriptor descriptor =
                    MethodDescriptor.<Message, Message>newBuilder()
                            .setType(MessageUtils.getMethodType(methodDescriptor.toProto()))
                            .setFullMethodName(fullMethodName)
                            .setRequestMarshaller(ProtoUtils.marshaller(new Message(reqMessage.getName())))
                            .setResponseMarshaller(ProtoUtils.marshaller(new Message(resMessage.getName())))
                            .setSchemaDescriptor(methodDescriptor)
                            .build();
            descriptorMap.put(fullMethodName, descriptor);
            MessageRegistry messageRegistry = MessageRegistry.getInstance();
            // update request message descriptors.
            messageRegistry.addMessageDescriptor(reqMessage.getName(), reqMessage);
            setNestedMessages(reqMessage, messageRegistry);
            // update response message descriptors
            messageRegistry.addMessageDescriptor(resMessage.getName(), resMessage);
            setNestedMessages(resMessage, messageRegistry);
        }
        return Collections.unmodifiableMap(descriptorMap);
    }
}
