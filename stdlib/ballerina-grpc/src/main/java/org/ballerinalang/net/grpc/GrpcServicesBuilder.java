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
import io.netty.handler.ssl.SslContext;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.net.grpc.config.EndpointConfiguration;
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

import static org.ballerinalang.net.grpc.MessageUtils.setNestedMessages;
import static org.ballerinalang.net.grpc.builder.utils.BalGenConstants.FILE_SEPARATOR;

/**
 * This is the gRPC server implementation for registering service and start/stop server.
 *
 * @since 1.0.0
 */
public class GrpcServicesBuilder {

    /**
     * Initializes and returns gRPC server builder instance for endpoint configuration.
     *
     * @param serviceEndpointConfig service endpoint configuration.
     * @param sslContext SSL context, needed for setup secured connection.
     * @return gRPC server builder.
     */
    public static io.grpc.ServerBuilder initService(EndpointConfiguration serviceEndpointConfig, SslContext
            sslContext) {
        io.grpc.ServerBuilder serverBuilder;
        if (sslContext != null) {
            serverBuilder = NettyServerBuilder.forPort(serviceEndpointConfig.getPort())
                    .bossEventLoopGroup(new NioEventLoopGroup(Runtime.getRuntime()
                            .availableProcessors()))
                    .workerEventLoopGroup(new NioEventLoopGroup(Runtime.getRuntime()
                            .availableProcessors() * 2)).sslContext(sslContext);
        } else {
            serverBuilder = NettyServerBuilder.forPort(serviceEndpointConfig.getPort())
                    .bossEventLoopGroup(new NioEventLoopGroup(Runtime.getRuntime()
                            .availableProcessors()))
                    .workerEventLoopGroup(new NioEventLoopGroup(Runtime.getRuntime()
                            .availableProcessors() * 2));
        }
        return serverBuilder;
    }

    /**
     * Register new service to the gRPC Server Builder.
     *
     * @param serverBuilder gRPC server Builder initiated when initializing service endpoint.
     * @param service   Service to register.
     * @throws GrpcServerException  Exception while registering the service.
     */
    public static void registerService(io.grpc.ServerBuilder serverBuilder, Service service) throws
            GrpcServerException {
        try {
            serverBuilder.addService(ServerInterceptors.intercept(getServiceDefinition(service), new
                    ServerHeaderInterceptor()));
        } catch (GrpcServerException e) {
            throw new GrpcServerException("Error while registering the service : " + service.getName(), e);
        }
    }
    
    private static ServerServiceDefinition getServiceDefinition(Service service) throws GrpcServerException {
        Descriptors.FileDescriptor fileDescriptor = ServiceProtoUtils.getDescriptor(service);
        if (fileDescriptor == null) {
            throw new GrpcServerException("Error while reading the service descriptor. Service file definition not " +
                    "found");
        }

        Descriptors.ServiceDescriptor serviceDescriptor = fileDescriptor.findServiceByName(service.getName());
        return getServiceDefinition(service, serviceDescriptor);
    }
    
    private static ServerServiceDefinition getServiceDefinition(Service service, Descriptors.ServiceDescriptor
            serviceDescriptor) {
        // Generate full service name for the service definition. <package>.<service>
        final String serviceName;
        if (ServiceProtoConstants.CLASSPATH_SYMBOL.equals(service.getPackage())) {
            serviceName = service.getName();
        } else {
            serviceName = service.getPackage() + ServiceProtoConstants.CLASSPATH_SYMBOL + service.getName();
        }
        // Server Definition Builder for the service.
        Builder serviceDefBuilder = ServerServiceDefinition.builder(serviceName);
        
        for (Descriptors.MethodDescriptor methodDescriptor : serviceDescriptor.getMethods()) {
            final String methodName = serviceName + FILE_SEPARATOR + methodDescriptor.getName();
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

            MethodDescriptor.Marshaller<Message> reqMarshaller = ProtoUtils.marshaller(Message.newBuilder
                    (requestDescriptor.getName()).build());
            MethodDescriptor.Marshaller<Message> resMarshaller = ProtoUtils.marshaller(Message.newBuilder
                    (responseDescriptor.getName()).build());

            MethodDescriptor.MethodType methodType;
            ServerCallHandler<Message, Message> serverCallHandler;
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
                serverCallHandler = ServerCalls.asyncBidiStreamingCall(new BidirectionalStreamingListener
                        (methodDescriptor, resourceMap));
            } else if (methodDescriptor.toProto().getClientStreaming()) {
                methodType = MethodDescriptor.MethodType.CLIENT_STREAMING;
                serverCallHandler = ServerCalls.asyncClientStreamingCall(new ClientStreamingListener
                        (methodDescriptor, resourceMap));
            } else if (methodDescriptor.toProto().getServerStreaming()) {
                methodType = MethodDescriptor.MethodType.SERVER_STREAMING;
                serverCallHandler = ServerCalls.asyncServerStreamingCall(new ServerStreamingListener
                        (methodDescriptor, mappedResource));
            } else {
                methodType = MethodDescriptor.MethodType.UNARY;
                serverCallHandler = ServerCalls.asyncUnaryCall(new UnaryMethodListener(methodDescriptor,
                        mappedResource));
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
    
    /**
     * Start this gRPC server. This will startup all the gRPC registered services.
     *
     * @throws GrpcServerException exception when there is an error in starting the server.
     */
    public static Server start(io.grpc.ServerBuilder serverBuilder) throws
            GrpcServerException {

        if (serverBuilder == null) {
            throw new GrpcServerException("Error while starting gRPC server, client responder builder is null");
        }
        Server server = serverBuilder.build();
        if (server != null) {
            try {
                server.start();
            } catch (IOException e) {
                throw new GrpcServerException(e);
            }
        } else {
            throw new GrpcServerException("No gRPC service is registered to Start" +
                    ". You need to Register the service");
        }
        return server;
    }
    
    /**
     * Shutdown gRPC server.
     */
    public static void stop(Server server) {
        if (server != null) {
            server.shutdown();
        }
    }
    
    /**
     * Await termination on the main thread since the gRPC library uses daemon threads.
     */
    public static void blockUntilShutdown(Server server) throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}
