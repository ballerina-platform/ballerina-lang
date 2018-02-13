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
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.model.types.BArrayType;
import org.ballerinalang.model.types.BStructType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.net.grpc.definition.Field;
import org.ballerinalang.net.grpc.definition.File;
import org.ballerinalang.net.grpc.definition.Message;
import org.ballerinalang.net.grpc.definition.Method;
import org.ballerinalang.net.grpc.definition.Service;
import org.ballerinalang.net.grpc.exception.GrpcServerException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * Utility class providing utility methods.
 */
public class ServiceProtoUtils {

    /**
     * Returns protobuf file definition from Ballerina Service.
     *
     * @param service ballerina service
     * @return protobuf file definition
     */
    public static File generateServiceDefinition(org.ballerinalang.connector.api.Service service) {
        // Protobuf file definition builder.
        File.Builder fileBuilder = File.newBuilder(service.getName() + ServiceProtoConstants.PROTO_FILE_EXTENSION)
                .setSyntax(ServiceProtoConstants.PROTOCOL_SYNTAX).setPackage(service.getPackage());
        // Protobuf service definition builder.
        Service.Builder serviceBuilder = Service.newBuilder(service.getName());

        for (Resource resource : service.getResources()) {
            // Assumption: first resource parameter is request message.
            BType requestType = resource.getParamDetails().get(ServiceProtoConstants.REQUEST_MESSAGE_INDEX)
                    .getVarType();
            // Assumption: second resource parameter is response message.
            BType responseType = resource.getParamDetails().get(ServiceProtoConstants.RESPONSE_MESSAGE_INDEX)
                    .getVarType();

            // Protobuf request message definition builder.
            Message requestMessage = generateMessageDefinition(requestType);
            if (!fileBuilder.getRegisteredMessages().contains(requestMessage.getDescriptorProto())) {
                fileBuilder.setMessage(requestMessage);
            }

            // Protobuf response message definition builder.
            Message responseMessage = generateMessageDefinition(responseType);
            if (!fileBuilder.getRegisteredMessages().contains(responseMessage.getDescriptorProto())) {
                fileBuilder.setMessage(responseMessage);
            }

            Method resourceMethod = Method.newBuilder(resource.getName())
                    .setClientStreaming(false)
                    .setServerStreaming(false)
                    .setInputType(requestType.getPackagePath() + ServiceProtoConstants.CLASSPATH_SYMBOL + requestType
                            .getName())
                    .setOutputType(responseType.getPackagePath() + ServiceProtoConstants.CLASSPATH_SYMBOL +
                            responseType.getName())
                    .build();
            serviceBuilder.addMethod(resourceMethod);
        }
        Service grpcService = serviceBuilder.build();
        fileBuilder.setService(grpcService);
        return fileBuilder.build();
    }

    /**
     * Returns protobuf message definition from ballerina struct type.
     *
     * @param messageType ballerina struct type
     * @return message definition
     */
    private static Message generateMessageDefinition(BType messageType) {
        Message.Builder requestMsgBuilder = Message.newBuilder(messageType.getName());
        int fieldIndex = 0;
        for (BStructType.StructField structField : ((BStructType) messageType).getStructFields()) {
            Field messageField;
            String fieldName = structField.getFieldName();
            if (structField.getFieldType() instanceof BArrayType) {
                BType fieldType = ((BArrayType) structField.getFieldType()).getElementType();
                String sfieldtype = fieldType.getName();
                messageField = Field.newBuilder(fieldName).setLabel("repeated").setType
                        (sfieldtype).setIndex(++fieldIndex).build();
            } else {
                String sfieldtype = structField.getFieldType().getName();
                messageField = Field.newBuilder(fieldName)
                        .setIndex(++fieldIndex)
                        .setType(sfieldtype).build();
            }
            requestMsgBuilder.addFieldDefinition(messageField);
        }
        return requestMsgBuilder.build();
    }

    public static com.google.protobuf.Descriptors.FileDescriptor getDescriptor(org.ballerinalang.connector.api
                                                                                       .Service service) {
        java.io.File initialFile = new java.io.File(service.getName() + ".desc");
        try (InputStream targetStream = new FileInputStream(initialFile)) {
            DescriptorProtos.FileDescriptorSet set = DescriptorProtos.FileDescriptorSet.parseFrom(targetStream);
            return Descriptors.FileDescriptor.buildFrom((DescriptorProtos.FileDescriptorProto)
                    set.getFileOrBuilder(0), new com.google.protobuf.Descriptors.FileDescriptor[] {
                    com.google.protobuf.WrappersProto.getDescriptor(),
            });
        } catch (IOException | Descriptors.DescriptorValidationException e) {
            throw new RuntimeException("Error : ", e);
        }
    }

    /**
     * Returns wire type corresponding to the field descriptor type.
     * <p>
     * 0 -> int32, int64, uint32, uint64, sint32, sint64, bool, enum
     * 1 -> fixed64, sfixed64, double
     * 2 -> string, bytes, embedded messages, packed repeated fields
     * 5 -> fixed32, sfixed32, float
     *
     * @param fieldType field descriptor type
     * @return wire type
     */
    public static int getFieldWireType(Descriptors.FieldDescriptor.Type fieldType) {
        if (fieldType == null) {
            return ServiceProtoConstants.INVALID_WIRE_TYPE;
        }
        Integer wireType = ServiceProtoConstants.WIRE_TYPE_MAP.get(fieldType.toProto());
        if (wireType != null) {
            return wireType;
        } else {
            // Returns embedded messages, packed repeated fields message type, if field type doesn't map with the
            // predefined proto types.
            return ServiceProtoConstants.MESSAGE_WIRE_TYPE;
        }
    }

    /**
     * Write protobuf file definition content to the filename.
     *
     * @param protoFileDefinition protobuf file definition.
     * @param filename            filename
     * @throws GrpcServerException when error occur while writing content to file.
     */
    public static void writeConfigurationFile(File protoFileDefinition, String filename) throws GrpcServerException {
        java.io.File configDir = new java.io.File(ServiceProtoConstants.PROTO_BUF_DIRECTORY);

        // create protobuf contract directory.
        if (!configDir.exists() && !configDir.mkdirs()) {
            throw new GrpcServerException("Error while creating protobuf contract directory.");
        }

        // write the proto string to the file in protobuf contract directory
        try (PrintWriter out = new PrintWriter(new java.io.File(configDir.getPath(), filename
                + ServiceProtoConstants.PROTO_FILE_EXTENSION), ServiceProtoConstants.UTF_8_CHARSET)) {
            out.println(protoFileDefinition.getFileDefinition());
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new GrpcServerException("Error while creating new contract file.", e);
        }
    }

    /**
     * Check whether message object is an array.
     *
     * @param object message object
     * @return true if object is array, false otherwise.
     */
    public static boolean isArray(Object object) {
        return object != null && object.getClass().isArray();
    }

}
