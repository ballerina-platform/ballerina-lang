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
import io.grpc.MethodDescriptor;
import io.grpc.Server;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import io.grpc.ServerServiceDefinition.Builder;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ServerCalls;
import io.netty.channel.nio.NioEventLoopGroup;
import org.ballerinalang.connector.api.AnnAttrValue;
import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.net.grpc.exception.GrpcServerException;
import org.ballerinalang.net.grpc.interceptor.ServerHeaderInterceptor;
import org.ballerinalang.net.grpc.listener.BidirectionalStreamingListener;
import org.ballerinalang.net.grpc.listener.ClientStreamingListener;
import org.ballerinalang.net.grpc.listener.ServerStreamingListener;
import org.ballerinalang.net.grpc.listener.UnaryMethodListener;
import org.ballerinalang.net.grpc.proto.ServiceProtoConstants;
import org.ballerinalang.net.grpc.proto.ServiceProtoUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.ballerinalang.net.grpc.builder.BalGenConstants.FILE_SEPARATOR;

/**
 * This is the gRPC implementation for the {@code BallerinaServerConnector} API.
 *
 * @since 0.96.1
 */
public class GrpcServicesBuilder {
    private io.grpc.ServerBuilder serverBuilder = null;
    
    void registerService(Service service) throws GrpcServerException {
        Annotation serviceAnnotation = MessageUtils.getServiceConfigAnnotation(service, MessageConstants
                .PROTOCOL_PACKAGE_GRPC);
        if (serviceAnnotation != null && serviceAnnotation.getAnnAttrValue("port") != null) {
            serverBuilder = NettyServerBuilder.forPort((int)
                    serviceAnnotation.getAnnAttrValue("port").getIntValue())
                    .bossEventLoopGroup(new NioEventLoopGroup(Runtime.getRuntime()
                            .availableProcessors()))
                    .workerEventLoopGroup(new NioEventLoopGroup(Runtime.getRuntime()
                            .availableProcessors() * 2));
        } else {
            serverBuilder = NettyServerBuilder.forPort(9090)
                    .bossEventLoopGroup(new NioEventLoopGroup(Runtime.getRuntime()
                            .availableProcessors()))
                    .workerEventLoopGroup(new NioEventLoopGroup(Runtime.getRuntime()
                            .availableProcessors() * 2));
        }
        try {
            serverBuilder.addService(ServerInterceptors.intercept(getServiceDefinition(service), new
                    ServerHeaderInterceptor()));
        } catch (Descriptors.DescriptorValidationException | GrpcServerException e) {
            throw new GrpcServerException("Error while registering the service : " + service.getName(), e);
        }
    }
    
    private ServerServiceDefinition getServiceDefinition(Service service) throws Descriptors
            .DescriptorValidationException, GrpcServerException {
        Descriptors.FileDescriptor fileDescriptor = ServiceProtoUtils.getDescriptor(service);
        Descriptors.ServiceDescriptor serviceDescriptor = fileDescriptor.findServiceByName(service.getName());
        
        Annotation serviceAnnotation = MessageUtils.getServiceConfigAnnotation(service, MessageConstants
                .PROTOCOL_PACKAGE_GRPC);
        if (isStreamService(serviceAnnotation)) {
            return getStreamingServiceDefinition(service, serviceDescriptor);
        } else {
            return getUnaryServiceDefinition(service, serviceDescriptor);
        }
    }
    
    private boolean isStreamService(Annotation serviceAnnotation) {
        if (serviceAnnotation == null) {
            return false;
        }
        AnnAttrValue clientStreamValue = serviceAnnotation.getAnnAttrValue("clientStreaming");
        return clientStreamValue != null && clientStreamValue.getBooleanValue();
    }
    
    private ServerServiceDefinition getUnaryServiceDefinition(Service service, Descriptors.ServiceDescriptor
            serviceDescriptor) throws GrpcServerException {
        // Generate full service name for the service definition. <package>.<service>
        final String serviceName = service.getPackage() + ServiceProtoConstants.CLASSPATH_SYMBOL + service.getName();
        // Server Definition Builder for the service.
        Builder serviceDefBuilder = ServerServiceDefinition.builder(serviceName);
        
        for (Resource resource : service.getResources()) {
            // Method name format: <service_name>/<method_name>
            final String methodName = serviceName + FILE_SEPARATOR + resource.getName();
            
            Descriptors.MethodDescriptor methodDescriptor = getMethodDescriptor(serviceDescriptor, resource.getName());
            if (methodDescriptor == null) {
                continue;
            }
            
            Descriptors.Descriptor requestDescriptor = serviceDescriptor.findMethodByName(resource.getName())
                    .getInputType();
            Descriptors.Descriptor responseDescriptor = serviceDescriptor.findMethodByName(resource.getName())
                    .getOutputType();
            MessageRegistry.getInstance().addMessageDescriptor(requestDescriptor.getName(), requestDescriptor);
            MessageRegistry.getInstance().addMessageDescriptor(responseDescriptor.getName(), responseDescriptor);
            
            MethodDescriptor.Marshaller<Message> reqMarshaller = ProtoUtils.marshaller((Message) Message.newBuilder
                    (requestDescriptor.getName()).build());
            MethodDescriptor.Marshaller<Message> resMarshaller = ProtoUtils.marshaller((Message) Message.newBuilder
                    (responseDescriptor.getName()).build());
            
            MethodDescriptor.MethodType methodType;
            ServerCallHandler<Message, Message> serverCallHandler;
            
            if (methodDescriptor.toProto().getServerStreaming()) {
                methodType = MethodDescriptor.MethodType.SERVER_STREAMING;
                serverCallHandler = ServerCalls.asyncServerStreamingCall(new ServerStreamingListener
                        (methodDescriptor, resource));
            } else {
                methodType = MethodDescriptor.MethodType.UNARY;
                serverCallHandler = ServerCalls.asyncUnaryCall(new UnaryMethodListener(methodDescriptor,
                        resource));
            }
            
            MethodDescriptor.Builder<Message, Message> methodBuilder = MethodDescriptor.newBuilder();
            MethodDescriptor<Message, Message> grpcMethodDescriptor = methodBuilder.setType(methodType)
                    .setFullMethodName(methodName)
                    .setRequestMarshaller(reqMarshaller)
                    .setResponseMarshaller(resMarshaller)
                    .setSchemaDescriptor(methodDescriptor).build();
            serviceDefBuilder.addMethod(grpcMethodDescriptor, serverCallHandler);
        }
        return serviceDefBuilder.build();
    }
    
    private ServerServiceDefinition getStreamingServiceDefinition(Service service, Descriptors.ServiceDescriptor
            serviceDescriptor) throws GrpcServerException {
        // Generate full service name for the service definition. <package>.<service>
        final String serviceName = service.getPackage() + ServiceProtoConstants.CLASSPATH_SYMBOL + service.getName();
        // Server Definition Builder for the service.
        Builder serviceDefBuilder = ServerServiceDefinition.builder(serviceName);
        
        // In streaming service, method should be always one.
        if (serviceDescriptor.getMethods().size() != 1) {
            throw new GrpcServerException("Invalid resource count in streaming server. Resource count should be one, " +
                    " no of resources: " + serviceDescriptor.getMethods().size());
        }
        
        Descriptors.MethodDescriptor methodDescriptor = serviceDescriptor.getMethods().get(0);
        final String methodName = serviceName + FILE_SEPARATOR + methodDescriptor.getName();
        Descriptors.Descriptor requestDescriptor = serviceDescriptor.findMethodByName(methodDescriptor.getName())
                .getInputType();
        Descriptors.Descriptor responseDescriptor = serviceDescriptor.findMethodByName(methodDescriptor.getName())
                .getOutputType();
        MessageRegistry.getInstance().addMessageDescriptor(requestDescriptor.getName(), requestDescriptor);
        MessageRegistry.getInstance().addMessageDescriptor(responseDescriptor.getName(), responseDescriptor);
        
        MethodDescriptor.Marshaller<Message> reqMarshaller = ProtoUtils.marshaller((Message) Message.newBuilder
                (requestDescriptor.getName()).build());
        MethodDescriptor.Marshaller<Message> resMarshaller = ProtoUtils.marshaller((Message) Message.newBuilder
                (responseDescriptor.getName()).build());
        
        Map<String, Resource> resourceMap = new HashMap<>();
        for (Resource resource : service.getResources()) {
            resourceMap.put(resource.getName(), resource);
        }
        
        MethodDescriptor.MethodType methodType;
        ServerCallHandler<Message, Message> serverCallHandler;
        if (methodDescriptor.toProto().getServerStreaming() && methodDescriptor.toProto().getClientStreaming()) {
            methodType = MethodDescriptor.MethodType.BIDI_STREAMING;
            serverCallHandler = ServerCalls.asyncBidiStreamingCall(new BidirectionalStreamingListener
                    (methodDescriptor, resourceMap));
        } else {
            methodType = MethodDescriptor.MethodType.CLIENT_STREAMING;
            serverCallHandler = ServerCalls.asyncClientStreamingCall(new ClientStreamingListener
                    (methodDescriptor, resourceMap));
        }
        
        MethodDescriptor.Builder<Message, Message> methodBuilder = MethodDescriptor.newBuilder();
        MethodDescriptor<Message, Message> grpcMethodDescriptor = methodBuilder.setType(methodType)
                .setFullMethodName(methodName)
                .setRequestMarshaller(reqMarshaller)
                .setResponseMarshaller(resMarshaller)
                .setSchemaDescriptor(methodDescriptor).build();
        serviceDefBuilder.addMethod(grpcMethodDescriptor, serverCallHandler);
        
        return serviceDefBuilder.build();
    }
    
    private Descriptors.MethodDescriptor getMethodDescriptor(Descriptors.ServiceDescriptor serviceDescriptor,
                                                             String resourceName) {
        for (Descriptors.MethodDescriptor methodDescriptor : serviceDescriptor.getMethods()) {
            if (methodDescriptor == null || methodDescriptor.getName() == null) {
                continue;
            }
            
            if (methodDescriptor.getName().equals(resourceName)) {
                return methodDescriptor;
            }
        }
        return null;
    }
    
    /**
     * Start this gRPC server. This will startup all the gRPC services.
     *
     * @throws GrpcServerException exception when there is an error in starting the server.
     */
    public Server start() throws GrpcServerException {
        if (serverBuilder != null) { //if no grpc service is not defined
            Server server = serverBuilder.build();
            if (server != null) {
                try {
                    server.start();
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> stop(server)));
                } catch (IOException e) {
                    throw new GrpcServerException("Error while starting gRPC server", e);
                }
            } else {
                throw new GrpcServerException("No gRPC service is registered to start" +
                        ". You need to register the service");
            }
            return server;
        }
        return null;
    }
    
    /**
     * Shutdown grpc server.
     */
    private void stop(Server server) {
        if (server != null) {
            server.shutdown();
        }
    }
    
    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    void blockUntilShutdown(Server server) throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}
