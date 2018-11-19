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
import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.connector.api.Value;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.net.grpc.exception.GrpcServerException;
import org.ballerinalang.net.grpc.listener.ServerCallHandler;
import org.ballerinalang.net.grpc.listener.StreamingServerCallHandler;
import org.ballerinalang.net.grpc.listener.UnaryServerCallHandler;
import org.ballerinalang.net.grpc.proto.definition.StandardDescriptorBuilder;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.net.grpc.GrpcConstants.EMPTY_DATATYPE_NAME;
import static org.ballerinalang.net.grpc.GrpcConstants.WRAPPER_BOOL_MESSAGE;
import static org.ballerinalang.net.grpc.GrpcConstants.WRAPPER_BYTES_MESSAGE;
import static org.ballerinalang.net.grpc.GrpcConstants.WRAPPER_DOUBLE_MESSAGE;
import static org.ballerinalang.net.grpc.GrpcConstants.WRAPPER_FLOAT_MESSAGE;
import static org.ballerinalang.net.grpc.GrpcConstants.WRAPPER_INT32_MESSAGE;
import static org.ballerinalang.net.grpc.GrpcConstants.WRAPPER_INT64_MESSAGE;
import static org.ballerinalang.net.grpc.GrpcConstants.WRAPPER_STRING_MESSAGE;
import static org.ballerinalang.net.grpc.GrpcConstants.WRAPPER_UINT32_MESSAGE;
import static org.ballerinalang.net.grpc.GrpcConstants.WRAPPER_UINT64_MESSAGE;
import static org.ballerinalang.net.grpc.MessageUtils.setNestedMessages;

/**
 * This is the gRPC server implementation for registering service and start/stop server.
 *
 * @since 0.980.0
 */
public class ServicesBuilderUtils {
    
    public static ServerServiceDefinition getServiceDefinition(Service service, Context context) throws
            GrpcServerException {
        Descriptors.FileDescriptor fileDescriptor = getDescriptor(service);
        Descriptors.ServiceDescriptor serviceDescriptor = fileDescriptor.findServiceByName(service.getName());
        return getServiceDefinition(service, serviceDescriptor, context);
    }
    
    private static ServerServiceDefinition getServiceDefinition(Service service, Descriptors.ServiceDescriptor
            serviceDescriptor, Context context) throws GrpcServerException {
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
            MethodDescriptor.Marshaller reqMarshaller = ProtoUtils.marshaller(new MessageParser(requestDescriptor
                    .getName(), context.getProgramFile(), getBallerinaValueType(requestDescriptor.getName(), context
                    .getProgramFile())));
            MethodDescriptor.Marshaller resMarshaller = ProtoUtils.marshaller(new MessageParser(responseDescriptor
                    .getName(), context.getProgramFile(), getBallerinaValueType(responseDescriptor.getName(), context
                    .getProgramFile())));
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

    /**
     * Returns file descriptor for the service.
     * Reads file descriptor from internal annotation attached to the service at compile time.
     *
     * @param service gRPC service.
     * @return File Descriptor of the service.
     * @throws GrpcServerException cannot read service descriptor
     */
    public static com.google.protobuf.Descriptors.FileDescriptor getDescriptor(
            org.ballerinalang.connector.api.Service service) throws GrpcServerException {
        try {
            List<Annotation> annotationList = service.getAnnotationList("ballerina/grpc", "ServiceDescriptor");
            if (annotationList == null || annotationList.size() != 1) {
                throw new GrpcServerException("Couldn't find the service descriptor.");
            }
            Annotation descriptorAnn = annotationList.get(0);
            Struct descriptorStruct = descriptorAnn.getValue();
            if (descriptorStruct == null) {
                throw new GrpcServerException("Couldn't find the service descriptor.");
            }
            String descriptorData = descriptorStruct.getStringField("descriptor");
            Map<String, Value> descMap = descriptorStruct.getMapField("descMap");
            return getFileDescriptor(descriptorData, descMap);
        } catch (IOException | Descriptors.DescriptorValidationException e) {
            throw new GrpcServerException("Error while reading the service proto descriptor. check the service " +
                    "implementation. ", e);
        }
    }

    private static Descriptors.FileDescriptor getFileDescriptor(String descriptorData, Map<String, Value> descMap)
            throws InvalidProtocolBufferException, Descriptors.DescriptorValidationException, GrpcServerException {
        byte[] descriptor = hexStringToByteArray(descriptorData);
        if (descriptor.length == 0) {
            throw new GrpcServerException("Error while reading the service proto descriptor. input descriptor string " +
                    "is null.");
        }
        DescriptorProtos.FileDescriptorProto descriptorProto = DescriptorProtos.FileDescriptorProto.parseFrom
                (descriptor);
        if (descriptorProto == null) {
            throw new GrpcServerException("Error while reading the service proto descriptor. File proto descriptor is" +
                    " null.");
        }
        Descriptors.FileDescriptor[] fileDescriptors = new Descriptors.FileDescriptor[descriptorProto
                .getDependencyList().size()];
        int i = 0;
        for (ByteString dependency : descriptorProto.getDependencyList().asByteStringList()) {
            String dependencyKey = dependency.toStringUtf8();
            if (descMap.containsKey(dependencyKey)) {
                fileDescriptors[i++] = getFileDescriptor(descMap.get(dependencyKey).getStringValue(), descMap);
            } else if (descMap.size() == 0) {
                Descriptors.FileDescriptor dependentDescriptor = StandardDescriptorBuilder.getFileDescriptor
                        (dependencyKey);
                if (dependentDescriptor != null) {
                    fileDescriptors[i++] = dependentDescriptor;
                }
            }
        }
        if (fileDescriptors.length > 0 && i == 0) {
            throw new GrpcServerException("Error while reading the service proto descriptor. Couldn't find any " +
                    "dependent descriptors.");
        }
        return Descriptors.FileDescriptor.buildFrom(descriptorProto, fileDescriptors);
    }

    /**
     * Convert Hex string value to byte array.
     * @param sDescriptor hexadecimal string value
     * @return Byte array
     */
    public static byte[] hexStringToByteArray(String sDescriptor) {
        if (sDescriptor == null) {
            return new byte[0];
        }
        int len = sDescriptor.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(sDescriptor.charAt(i), 16) << 4)
                    + Character.digit(sDescriptor.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Returns corresponding Ballerina type for the proto buffer type.
     *
     * @param protoType Protocol buffer type
     * @param programFile   Ballerina Program File
     * @return Mapping BType of the proto type.
     */
    static BType getBallerinaValueType(String protoType, ProgramFile programFile) throws BallerinaException {
        if (protoType.equalsIgnoreCase(WRAPPER_DOUBLE_MESSAGE) || protoType
                .equalsIgnoreCase(WRAPPER_FLOAT_MESSAGE)) {
            return BTypes.typeFloat;
        } else if (protoType.equalsIgnoreCase(WRAPPER_INT32_MESSAGE) || protoType
                .equalsIgnoreCase(WRAPPER_INT64_MESSAGE) || protoType
                .equalsIgnoreCase(WRAPPER_UINT32_MESSAGE) || protoType
                .equalsIgnoreCase(WRAPPER_UINT64_MESSAGE)) {
            return BTypes.typeInt;
        } else if (protoType.equalsIgnoreCase(WRAPPER_BOOL_MESSAGE)) {
            return BTypes.typeBoolean;
        } else if (protoType.equalsIgnoreCase(WRAPPER_STRING_MESSAGE)) {
            return BTypes.typeString;
        } else if (protoType.equalsIgnoreCase(EMPTY_DATATYPE_NAME)) {
            return BTypes.typeNull;
        } else if (protoType.equalsIgnoreCase(WRAPPER_BYTES_MESSAGE)) {
            return new BArrayType(BTypes.typeByte);
        } else {
            if (!programFile.getEntryPackage().typeDefInfoMap.containsKey(protoType)) {
                throw new BallerinaException("Error while retrieving Ballerina type for " + protoType);
            }
            return programFile.getEntryPackage().getStructInfo(protoType).getType();
        }
    }

}
