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
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.net.grpc.exception.GrpcServerException;
import org.ballerinalang.net.grpc.listener.ServerCallHandler;
import org.ballerinalang.net.grpc.listener.StreamingServerCallHandler;
import org.ballerinalang.net.grpc.listener.UnaryServerCallHandler;
import org.ballerinalang.net.grpc.proto.ServiceProtoUtils;

import java.util.HashMap;
import java.util.Map;

import static org.ballerinalang.net.grpc.MessageUtils.setNestedMessages;

/**
 * This is the gRPC server implementation for registering service and start/stop server.
 *
 * @since 0.980.0
 */
public class ServicesBuilderUtils {
    
    public static ServerServiceDefinition getServiceDefinition(Service service) throws GrpcServerException {
        Descriptors.FileDescriptor fileDescriptor = ServiceProtoUtils.getDescriptor(service);
        Descriptors.ServiceDescriptor serviceDescriptor = fileDescriptor.findServiceByName(service.getName());
        return getServiceDefinition(service, serviceDescriptor);
    }
    
    private static ServerServiceDefinition getServiceDefinition(Service service, Descriptors.ServiceDescriptor
            serviceDescriptor) throws GrpcServerException {
        // Get full service name for the service definition. <package>.<service>
        final String serviceName = serviceDescriptor.getFullName();
        // Server Definition Builder for the service.
        ServerServiceDefinition.Builder serviceDefBuilder = ServerServiceDefinition.builder(serviceName);
        
        for (Descriptors.MethodDescriptor methodDescriptor : serviceDescriptor.getMethods()) {
            final String methodName = serviceName + "/" + methodDescriptor.getName();
            Descriptors.Descriptor requestDescriptor = serviceDescriptor.findMethodByName(methodDescriptor.getName())
                    .getInputType();
            Descriptors.Descriptor responseDescriptor = serviceDescriptor.findMethodByName(methodDescriptor.getName())
                    .getOutputType();
            MessageRegistry messageRegistry = MessageRegistry.getInstance();
            // update request message descriptors.
            messageRegistry.addMessageDescriptor(requestDescriptor.getName(), requestDescriptor);
            setNestedMessages(requestDescriptor, messageRegistry);
            // update response message descriptors.
            messageRegistry.addMessageDescriptor(responseDescriptor.getName(), responseDescriptor);
            setNestedMessages(responseDescriptor, messageRegistry);

            MethodDescriptor.Marshaller reqMarshaller = ProtoUtils.marshaller(new Message(requestDescriptor
                    .getName()));
            MethodDescriptor.Marshaller resMarshaller = ProtoUtils.marshaller(new Message(responseDescriptor
                    .getName()));

            MethodDescriptor.MethodType methodType;
            ServerCallHandler serverCallHandler;
            Map<String, Resource> resourceMap = new HashMap<>();
            Resource mappedResource = null;

            for (Resource resource : service.getResources()) {
                if (methodDescriptor.getName().equals(resource.getName())) {
                    mappedResource = resource;
                }
                resourceMap.put(resource.getName(), resource);
            }

            if (methodDescriptor.toProto().getServerStreaming() && methodDescriptor.toProto().getClientStreaming()) {
                methodType = MethodDescriptor.MethodType.BIDI_STREAMING;
                serverCallHandler = new StreamingServerCallHandler(methodDescriptor, resourceMap);
            } else if (methodDescriptor.toProto().getClientStreaming()) {
                methodType = MethodDescriptor.MethodType.CLIENT_STREAMING;
                serverCallHandler = new StreamingServerCallHandler(methodDescriptor, resourceMap);
            } else if (methodDescriptor.toProto().getServerStreaming()) {
                methodType = MethodDescriptor.MethodType.SERVER_STREAMING;
                serverCallHandler = new UnaryServerCallHandler(methodDescriptor, mappedResource);
            } else {
                methodType = MethodDescriptor.MethodType.UNARY;
                serverCallHandler = new UnaryServerCallHandler(methodDescriptor, mappedResource);
            }

            MethodDescriptor.Builder methodBuilder = MethodDescriptor.newBuilder();
            MethodDescriptor grpcMethodDescriptor = methodBuilder.setType(methodType)
                    .setFullMethodName(methodName)
                    .setRequestMarshaller(reqMarshaller)
                    .setResponseMarshaller(resMarshaller)
                    .setSchemaDescriptor(methodDescriptor).build();
            serviceDefBuilder.addMethod(grpcMethodDescriptor, serverCallHandler);
        }
        return serviceDefBuilder.build();
    }

    private ServicesBuilderUtils() {

    }

}
