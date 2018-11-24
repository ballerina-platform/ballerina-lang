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

import com.google.protobuf.ByteString;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import org.ballerinalang.bre.Context;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.grpc.exception.ClientRuntimeException;
import org.ballerinalang.net.grpc.exception.GrpcClientException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.ballerinalang.net.grpc.MessageUtils.setNestedMessages;
import static org.ballerinalang.net.grpc.MethodDescriptor.generateFullMethodName;
import static org.ballerinalang.net.grpc.ServicesBuilderUtils.getBallerinaValueType;
import static org.ballerinalang.net.grpc.ServicesBuilderUtils.hexStringToByteArray;

/**
 * This class contains proto descriptors of the service.
 *
 * @since 0.980.0
 */
public final class ServiceDefinition {
    
    private String rootDescriptor;
    private BMap<String, BValue> descriptorMap;
    private Descriptors.FileDescriptor fileDescriptor;
    
    public ServiceDefinition(String rootDescriptor, BMap<String, BValue> descriptorMap) {
        this.rootDescriptor = rootDescriptor;
        this.descriptorMap = descriptorMap;
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
        try {
            return fileDescriptor = getFileDescriptor(rootDescriptor, descriptorMap);
        } catch (IOException | Descriptors.DescriptorValidationException e) {
            throw new ClientRuntimeException("Error while generating service descriptor : ", e);
        }
    }

    private Descriptors.FileDescriptor getFileDescriptor(String rootDescriptor, BMap<String, BValue>
            descriptorMap) throws InvalidProtocolBufferException, Descriptors.DescriptorValidationException {
        byte[] descriptor = hexStringToByteArray(rootDescriptor);
        if (descriptor.length == 0) {
            throw new ClientRuntimeException("Error while reading the service proto descriptor. input descriptor " +
                    "string is null.");
        }
        DescriptorProtos.FileDescriptorProto descriptorProto = DescriptorProtos.FileDescriptorProto.parseFrom
                (descriptor);
        if (descriptorProto == null) {
            throw new ClientRuntimeException("Error while reading the service proto descriptor. File proto descriptor" +
                    " is null.");
        }
        Descriptors.FileDescriptor[] fileDescriptors = new Descriptors.FileDescriptor[descriptorProto
                .getDependencyList().size()];
        int i = 0;
        for (ByteString dependency : descriptorProto.getDependencyList().asByteStringList()) {
            if (descriptorMap.hasKey(dependency.toStringUtf8())) {
                fileDescriptors[i++] = getFileDescriptor(descriptorMap.get(dependency.toString(Charset.forName
                        ("UTF8"))).stringValue(), descriptorMap);
            }
        }
        if (fileDescriptors.length > 0 && i == 0) {
            throw new ClientRuntimeException("Error while reading the service proto descriptor. Couldn't find any " +
                    "dependent descriptors.");
        }
        return Descriptors.FileDescriptor.buildFrom(descriptorProto, fileDescriptors);
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

    public Map<String, MethodDescriptor> getMethodDescriptors(Context context) throws GrpcClientException {
        Map<String, MethodDescriptor> descriptorMap = new HashMap<>();
        Descriptors.ServiceDescriptor serviceDescriptor = getServiceDescriptor();

        for (Descriptors.MethodDescriptor methodDescriptor:  serviceDescriptor.getMethods()) {
            String methodName = methodDescriptor.getName();
            Descriptors.Descriptor reqMessage = methodDescriptor.getInputType();
            Descriptors.Descriptor resMessage = methodDescriptor.getOutputType();
            MessageRegistry messageRegistry = MessageRegistry.getInstance();
            // update request message descriptors.
            messageRegistry.addMessageDescriptor(reqMessage.getName(), reqMessage);
            setNestedMessages(reqMessage, messageRegistry);
            // update response message descriptors
            messageRegistry.addMessageDescriptor(resMessage.getName(), resMessage);
            setNestedMessages(resMessage, messageRegistry);
            String fullMethodName = generateFullMethodName(serviceDescriptor.getFullName(), methodName);
            MethodDescriptor descriptor =
                    MethodDescriptor.<Message, Message>newBuilder()
                            .setType(MessageUtils.getMethodType(methodDescriptor.toProto()))
                            .setFullMethodName(fullMethodName)
                            .setRequestMarshaller(ProtoUtils.marshaller(new MessageParser(reqMessage.getName(), context
                                    .getProgramFile(), getBallerinaValueType(reqMessage.getName(), context
                                    .getProgramFile()))))
                            .setResponseMarshaller(ProtoUtils.marshaller(new MessageParser(resMessage.getName(), context
                                    .getProgramFile(), getBallerinaValueType(resMessage.getName(), context
                                    .getProgramFile()))))
                            .setSchemaDescriptor(methodDescriptor)
                            .build();
            descriptorMap.put(fullMethodName, descriptor);
        }
        return Collections.unmodifiableMap(descriptorMap);
    }
}
