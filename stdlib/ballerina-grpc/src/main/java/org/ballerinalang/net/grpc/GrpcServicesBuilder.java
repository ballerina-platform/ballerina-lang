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
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import io.grpc.ServerServiceDefinition.Builder;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ServerCalls;
import io.netty.channel.nio.NioEventLoopGroup;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.net.grpc.definition.File;
import org.ballerinalang.net.grpc.exception.GrpcServerException;
import org.ballerinalang.net.grpc.interceptor.ServerHeaderInterceptor;

import java.io.IOException;

/**
 * This is the gRPC implementation for the {@code BallerinaServerConnector} API.
 *
 * @since 0.96.1
 */
public class GrpcServicesBuilder {
    private io.grpc.ServerBuilder serverBuilder = null;

    public void registerService(Service service) throws GrpcServerException {
        serverBuilder = NettyServerBuilder.forPort(9090)
                .bossEventLoopGroup(new NioEventLoopGroup(Runtime.getRuntime().availableProcessors()))
                .workerEventLoopGroup(new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2));
        try {
            serverBuilder.addService(ServerInterceptors.intercept(getServiceDefinition(service), new
                    ServerHeaderInterceptor()));
        } catch (Descriptors.DescriptorValidationException | GrpcServerException e) {
            throw new GrpcServerException("Error while registering the service : " + service.getName(), e);
        }
    }

    private ServerServiceDefinition getServiceDefinition(Service service) throws Descriptors
            .DescriptorValidationException, GrpcServerException {
        // Generate full service name for the service definition. <package>.<service>
        final String serviceName = service.getPackage() + ServiceProtoConstants.CLASSPATH_SYMBOL + service.getName();
        // Server Definition Builder for the service.
        Builder serviceDefBuilder = ServerServiceDefinition.builder(serviceName);
        // Generate protobuf definition from Ballerina Service.
        File protobufFileDefinition = ServiceProtoUtils.generateServiceDefinition(service);
        // Write protobuf file definition to .proto file.
        ServiceProtoUtils.writeConfigurationFile(protobufFileDefinition, service.getName());
        // we are registering one service. So there will be only one service in file descriptor.
        Descriptors.ServiceDescriptor serviceDescriptor = protobufFileDefinition.getFileDescriptor().getServices()
                .get(0);

        for (Resource resource : service.getResources()) {
            // Method name format: <service_name>/<method_name>
            final String methodName = serviceName + '/' + resource.getName();

            Descriptors.MethodDescriptor methodDescriptor = getMethodDescriptor(serviceDescriptor, resource);
            if (methodDescriptor == null) {
                continue;
            }

            Descriptors.Descriptor requestDescriptor = protobufFileDefinition.getFileDescriptor().findServiceByName
                    (service.getName()).findMethodByName(resource.getName()).getInputType();
            Descriptors.Descriptor responseDescriptor = protobufFileDefinition.getFileDescriptor().findServiceByName
                    (service.getName()).findMethodByName(resource.getName()).getOutputType();
                    MessageRegistry.getInstance().addMessageDescriptor(requestDescriptor.getName(), requestDescriptor);
            MessageRegistry.getInstance().addMessageDescriptor(responseDescriptor.getName(), responseDescriptor);

            MethodDescriptor.Marshaller reqMarshaller = ProtoUtils.marshaller(Message.newBuilder(requestDescriptor
                    .getName()).build());
            MethodDescriptor.Marshaller resMarshaller = ProtoUtils.marshaller(Message.newBuilder(responseDescriptor
                    .getName()).build());


            MethodDescriptor grpcMethodDescriptor = MethodDescriptor.newBuilder().setType(MethodDescriptor
                    .MethodType.UNARY).setFullMethodName(methodName)
                    .setRequestMarshaller(reqMarshaller)
                    .setResponseMarshaller(resMarshaller)
                    .setSchemaDescriptor(methodDescriptor).build();

            ServerCalls.UnaryMethod<Object, Object> methodInvokation = new UnaryMethodInvoker(service, resource);

            serviceDefBuilder.addMethod(grpcMethodDescriptor, ServerCalls.asyncUnaryCall(methodInvokation));
        }

        return serviceDefBuilder.build();
    }

    private Descriptors.MethodDescriptor getMethodDescriptor(Descriptors.ServiceDescriptor serviceDescriptor,
                                                             Resource resource) {
        for (Descriptors.MethodDescriptor methodDescriptor : serviceDescriptor.getMethods()) {
            if (methodDescriptor == null || methodDescriptor.getName() == null) {
                continue;
            }

            if (methodDescriptor.getName().equals(resource.getName())) {
                return methodDescriptor;
            }
        }
        return null;
    }

    /**
     * Start this gRPC server. This will startup all the gRPC services.
     * @throws GrpcServerException exception when there is an error in starting the server.
     */
    public Server start() throws GrpcServerException {
        Server server = serverBuilder.build();
        if (server != null) {
            try {
                server.start();
                Runtime.getRuntime().addShutdownHook(new Thread(() -> stop(server)));
            } catch (IOException e) {
                throw new GrpcServerException("Error while starting gRPC server", e);
            }
        } else {
            throw new GrpcServerException("No gRPC service is registered to start. You need to register the service");
        }
        return server;
    }

    /**
     * Shutdown grpc server.
     */
    public void stop(Server server) {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    public void blockUntilShutdown(Server server) throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}
