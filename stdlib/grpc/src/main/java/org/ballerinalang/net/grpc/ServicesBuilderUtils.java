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
import org.ballerinalang.jvm.BRuntime;
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BPackage;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.TypeFlags;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.api.BString;
import org.ballerinalang.net.grpc.exception.GrpcServerException;
import org.ballerinalang.net.grpc.listener.ServerCallHandler;
import org.ballerinalang.net.grpc.listener.StreamingServerCallHandler;
import org.ballerinalang.net.grpc.listener.UnaryServerCallHandler;
import org.ballerinalang.net.grpc.proto.definition.StandardDescriptorBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.ballerinalang.net.grpc.GrpcConstants.EMPTY_DATATYPE_NAME;
import static org.ballerinalang.net.grpc.GrpcConstants.ON_MESSAGE_RESOURCE;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_PACKAGE_GRPC;
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
import static org.ballerinalang.net.grpc.proto.ServiceProtoConstants.ANN_SERVICE_CONFIG_FQN;

/**
 * This is the gRPC server implementation for registering service and start/stop server.
 *
 * @since 0.980.0
 */
public class ServicesBuilderUtils {
    
    public static ServerServiceDefinition getServiceDefinition(BRuntime runtime, ObjectValue service,
                                                               Object annotationData) throws
            GrpcServerException {
        Descriptors.FileDescriptor fileDescriptor = getDescriptor(annotationData);
        if (fileDescriptor == null) {
            throw new GrpcServerException("Couldn't find the service descriptor.");
        }
        String serviceName = getServiceName(service);
        Descriptors.ServiceDescriptor serviceDescriptor = fileDescriptor.findServiceByName(serviceName);
        if (serviceDescriptor == null) {
            throw new GrpcServerException("Couldn't find the service descriptor for the service: " + serviceName);
        }
        return getServiceDefinition(runtime, service, serviceDescriptor);
    }

    private static String getServiceName(ObjectValue service) {
        Object serviceConfigData = service.getType().getAnnotation(StringUtils.fromString(ANN_SERVICE_CONFIG_FQN));
        if (serviceConfigData != null) {
            MapValue configMap = (MapValue) serviceConfigData;
            BString providedName = configMap.getStringValue(StringUtils.fromString("name"));
            if (providedName != null && !providedName.getValue().isEmpty()) {
                return providedName.getValue();
            }
        }
        String serviceTypeName = service.getType().getName(); // typeName format: <name>$$<type>$$<version>
        String[] splitValues = serviceTypeName.split("\\$\\$");
        return splitValues[0];
    }

    private static ServerServiceDefinition getServiceDefinition(BRuntime runtime, ObjectValue service,
                                                                Descriptors.ServiceDescriptor serviceDescriptor)
            throws GrpcServerException {
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
            MethodDescriptor.Marshaller reqMarshaller = null;
            Map<String, ServiceResource> resourceMap = new HashMap<>();
            ServiceResource mappedResource = null;

            for (AttachedFunction function : service.getType().getAttachedFunctions()) {
                if (methodDescriptor.getName().equals(function.getName())) {
                    mappedResource = new ServiceResource(runtime, service, function);
                    reqMarshaller = ProtoUtils.marshaller(new MessageParser(requestDescriptor.getName(),
                            getResourceInputParameterType(function)));
                } else if (ON_MESSAGE_RESOURCE.equals(function.getName())) {
                    reqMarshaller = ProtoUtils.marshaller(new MessageParser(requestDescriptor.getName(),
                            getResourceInputParameterType(function)));
                }
                resourceMap.put(function.getName(), new ServiceResource(runtime, service, function));
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
            if (reqMarshaller == null) {
                reqMarshaller = ProtoUtils.marshaller(new MessageParser(requestDescriptor
                        .getName(), getBallerinaValueType(service.getType().getPackage(),
                        requestDescriptor.getName())));
            }
            MethodDescriptor.Marshaller resMarshaller = ProtoUtils.marshaller(new MessageParser(responseDescriptor
                    .getName(), getBallerinaValueType(service.getType().getPackage(), responseDescriptor.getName())));
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
     * @param annotationData gRPC service annotation.
     * @return File Descriptor of the service.
     * @throws GrpcServerException cannot read service descriptor
     */
    @SuppressWarnings("unchecked")
    private static com.google.protobuf.Descriptors.FileDescriptor getDescriptor(Object annotationData)
            throws GrpcServerException {
        if (annotationData == null) {
            return null;
        }

        try {
            MapValue<BString, Object> annotationMap = (MapValue) annotationData;
            BString descriptorData = annotationMap.getStringValue(StringUtils.fromString("descriptor"));
            MapValue<BString, BString> descMap = (MapValue<BString, BString>) annotationMap.getMapValue(
                StringUtils.fromString("descMap"));
            return getFileDescriptor(descriptorData, descMap);
        } catch (IOException | Descriptors.DescriptorValidationException e) {
            throw new GrpcServerException("Error while reading the service proto descriptor. check the service " +
                                                  "implementation. ", e);
        }
    }

    private static Descriptors.FileDescriptor getFileDescriptor(
        BString descriptorData, MapValue<BString, BString> descMap) 
        throws InvalidProtocolBufferException, Descriptors.DescriptorValidationException, GrpcServerException {
        byte[] descriptor = hexStringToByteArray(descriptorData.getValue());
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
            if (descMap.containsKey(StringUtils.fromString(dependencyKey))) {
                fileDescriptors[i++] = getFileDescriptor(descMap.get(StringUtils.fromString(dependencyKey)), descMap);
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
     * @return Mapping BType of the proto type.
     */
    static BType getBallerinaValueType(BPackage bPackage, String protoType) {
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
            return new BRecordType(protoType, bPackage, 0, true,
                    TypeFlags.asMask(TypeFlags.ANYDATA, TypeFlags.PURETYPE));
        }
    }

    private static BType getResourceInputParameterType(AttachedFunction attachedFunction) {
        BType[] inputParams = attachedFunction.type.getParameterType();
        if (inputParams.length > 1) {
            BType inputType = inputParams[1];
            if (inputType != null && "Headers".equals(inputType.getName()) &&
                    inputType.getPackage() != null && PROTOCOL_PACKAGE_GRPC.equals(inputType.getPackage().getName())) {
                return BTypes.typeNull;
            } else {
                return inputParams[1];
            }
        }
        return BTypes.typeNull;
    }

}
