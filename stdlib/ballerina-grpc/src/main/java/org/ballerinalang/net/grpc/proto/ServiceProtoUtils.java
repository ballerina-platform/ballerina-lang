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
package org.ballerinalang.net.grpc.proto;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.ResourceNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.tree.expressions.AnnotationAttachmentAttributeNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.statements.BlockNode;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.ballerinalang.net.grpc.MessageConstants;
import org.ballerinalang.net.grpc.builder.BallerinaFile;
import org.ballerinalang.net.grpc.exception.GrpcServerException;
import org.ballerinalang.net.grpc.proto.definition.Field;
import org.ballerinalang.net.grpc.proto.definition.File;
import org.ballerinalang.net.grpc.proto.definition.Message;
import org.ballerinalang.net.grpc.proto.definition.MessageKind;
import org.ballerinalang.net.grpc.proto.definition.Method;
import org.ballerinalang.net.grpc.proto.definition.Service;
import org.ballerinalang.net.grpc.proto.definition.UserDefinedMessage;
import org.ballerinalang.net.grpc.proto.definition.WrapperMessage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


/**
 * Utility class providing utility methods.
 */
public class ServiceProtoUtils {

    public static File generateProtoDefinition(ServiceNode serviceNode) throws GrpcServerException {
        // Protobuf file definition builder.
        String packageName = serviceNode.getPosition().getSource().getPackageName();
        File.Builder fileBuilder;
        if(!".".equals(packageName)) {
           fileBuilder = File.newBuilder(serviceNode.getName() + ServiceProtoConstants.PROTO_FILE_EXTENSION)
                    .setSyntax(ServiceProtoConstants.PROTOCOL_SYNTAX).setPackage(serviceNode.getPosition().getSource()
                            .getPackageName());
        } else {
            fileBuilder = File.newBuilder(serviceNode.getName() + ServiceProtoConstants.PROTO_FILE_EXTENSION)
                    .setSyntax(ServiceProtoConstants.PROTOCOL_SYNTAX);
        }
        ServiceConfig serviceConfig = getServiceConfiguration(serviceNode);
        Service serviceDefinition;
        if (serviceConfig != null) {
            serviceDefinition = getStreamingServiceDefinition(serviceNode, serviceConfig, fileBuilder);
        } else {
            serviceDefinition = getUnaryServiceDefinition(serviceNode, fileBuilder);
        }

        fileBuilder.setService(serviceDefinition);
        return fileBuilder.build();
    }

    private static ServiceConfig getServiceConfiguration(ServiceNode serviceNode) {
        ServiceConfig serviceConfig = null;
        for (AnnotationAttachmentNode annotationNode : serviceNode.getAnnotationAttachments()) {
            if (!"serviceConfig".equals(annotationNode.getAnnotationName().getValue())) {
                continue;
            }
            String rpcEndpoint = null;
            boolean clientStreaming = false;
            boolean serverStreaming = false;
            for (AnnotationAttachmentAttributeNode attributeNode : annotationNode.getAttributes()) {
                String attributeName = attributeNode.getName().getValue();
                Node attributeValueNode = attributeNode.getValue() != null ? attributeNode.getValue().getValue() : null;
                Object attributeValue = null;
                if (attributeValueNode instanceof BLangLiteral) {
                    attributeValue = ((BLangLiteral) attributeValueNode).getValue();
                }

                switch (attributeName) {
                    case "rpcEndpoint": {
                        rpcEndpoint = (String) attributeValue;
                        break;
                    }
                    case "clientStreaming": {
                        clientStreaming = attributeValue != null ? (Boolean) attributeValue : false;
                        break;
                    }
                    case "serverStreaming": {
                        serverStreaming = attributeValue != null ? (Boolean) attributeValue : false;
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
            if (rpcEndpoint != null && (clientStreaming || serverStreaming)) {
                serviceConfig = new ServiceConfig(rpcEndpoint, clientStreaming, serverStreaming);
            }
        }
        return serviceConfig;
    }

    private static Service getUnaryServiceDefinition(ServiceNode serviceNode, File.Builder fileBuilder) throws
            GrpcServerException {
        // Protobuf service definition builder.
        Service.Builder serviceBuilder = Service.newBuilder(serviceNode.getName().getValue());

        for (ResourceNode resourceNode : serviceNode.getResources()) {
            Message requestMessage = getRequestMessage(resourceNode);
            if (requestMessage.getMessageKind() == MessageKind.USER_DEFINED) {
                if (!fileBuilder.getRegisteredMessages().contains(requestMessage.getDescriptorProto())) {
                    fileBuilder.setMessage(requestMessage);
                }
            }
            if (requestMessage.getDependency() != null) {
                if (!fileBuilder.getRegisteredDependencies().contains(requestMessage.getDependency())) {
                    fileBuilder.setDependeny(requestMessage.getDependency());
                }
            }
            Message responseMessage = getResponseMessage(resourceNode);

            if (responseMessage == null) {
                throw new GrpcServerException("Connection send expression not found in resource body");
            }

            if (responseMessage.getMessageKind() == MessageKind.USER_DEFINED) {
                if (!fileBuilder.getRegisteredMessages().contains(responseMessage.getDescriptorProto())) {
                    fileBuilder.setMessage(responseMessage);
                }
            }
            if (responseMessage.getDependency() != null) {
                if (!fileBuilder.getRegisteredDependencies().contains(responseMessage.getDependency())) {
                    fileBuilder.setDependeny(responseMessage.getDependency());
                }
            }

            boolean serverStreaming = isServerStreaming(resourceNode);
            Method resourceMethod = Method.newBuilder(resourceNode.getName().getValue())
                    .setClientStreaming(false)
                    .setServerStreaming(serverStreaming)
                    .setInputType(requestMessage.getMessageType())
                    .setOutputType(responseMessage.getMessageType())
                    .build();
            serviceBuilder.addMethod(resourceMethod);
        }
        return serviceBuilder.build();
    }

    private static Service getStreamingServiceDefinition(ServiceNode serviceNode, ServiceConfig serviceConfig, File
            .Builder fileBuilder) throws GrpcServerException {
        // Protobuf service definition builder.
        Service.Builder serviceBuilder = Service.newBuilder(serviceNode.getName().getValue());
        Message requestMessage = null;
        Message responseMessage = null;
        for (ResourceNode resourceNode : serviceNode.getResources()) {
            if ("onMessage".equals(resourceNode.getName().getValue())) {
                requestMessage = getRequestMessage(resourceNode);
                responseMessage = getResponseMessage(resourceNode);
                if (responseMessage != null) {
                    break;
                }
            }

            if ("onComplete".equals(resourceNode.getName().getValue())) {
                responseMessage = getResponseMessage(resourceNode);
            }
        }

        if (requestMessage == null) {
            throw new GrpcServerException("Cannot find request message definition for streaming service: " +
                    serviceNode.getName().getValue());
        }
        if (responseMessage == null) {
            throw new GrpcServerException("Cannot find response message definition for streaming service: " +
                    serviceNode.getName().getValue());
        }

        if (requestMessage.getMessageKind() == MessageKind.USER_DEFINED) {
            if (!fileBuilder.getRegisteredMessages().contains(requestMessage.getDescriptorProto())) {
                fileBuilder.setMessage(requestMessage);
            }
        }
        if (requestMessage.getDependency() != null) {
            if (!fileBuilder.getRegisteredDependencies().contains(requestMessage.getDependency())) {
                fileBuilder.setDependeny(requestMessage.getDependency());
            }
        }

        if (responseMessage.getDependency() != null) {
            if (!fileBuilder.getRegisteredDependencies().contains(responseMessage.getDependency())) {
                fileBuilder.setDependeny(responseMessage.getDependency());
            }
        }

        Method resourceMethod = Method.newBuilder(serviceConfig.getRpcEndpoint())
                .setClientStreaming(serviceConfig.isClientStreaming())
                .setServerStreaming(serviceConfig.isServerStreaming())
                .setInputType(requestMessage.getMessageType())
                .setOutputType(responseMessage.getMessageType())
                .build();
        serviceBuilder.addMethod(resourceMethod);
        return serviceBuilder.build();
    }

    private static boolean isServerStreaming(ResourceNode resourceNode) {
        boolean serverStreaming = false;
        for (AnnotationAttachmentNode annotationNode : resourceNode.getAnnotationAttachments()) {
            if (!"resourceConfig".equals(annotationNode.getAnnotationName().getValue())) {
                continue;
            }
            for (AnnotationAttachmentAttributeNode attributeNode : annotationNode.getAttributes()) {
                if ("streaming".equals(attributeNode.getName().getValue())) {
                    serverStreaming = attributeNode.getValue() != null && (boolean) ((BLangLiteral) attributeNode
                            .getValue().getValue()).getValue();
                }
            }
        }
        return serverStreaming;
    }

    private static Message getResponseMessage(ResourceNode resourceNode) throws GrpcServerException {
        Message responseMessage = null;
        BLangInvocation sendExpression = getInvocationExpression(resourceNode.getBody());
        if (sendExpression != null) {
            org.wso2.ballerinalang.compiler.semantics.model.types.BType responseType = getReturnType(sendExpression);
            responseMessage = responseType != null ? generateMessageDefinition(responseType) : null;
        }
        return responseMessage;
    }

    private static Message getRequestMessage(ResourceNode resourceNode) throws GrpcServerException {
        VariableNode requestVariable = resourceNode.getParameters().get(MessageConstants.REQUEST_MESSAGE_INDEX);
        org.wso2.ballerinalang.compiler.semantics.model.types.BType requestType;
        if (requestVariable instanceof BLangVariable) {
            requestType = ((BLangVariable) requestVariable).type;
        } else {
            throw new GrpcServerException("unsupported request type");
        }
        return generateMessageDefinition(requestType);
    }

    /**
     * Returns protobuf message definition from ballerina struct type.
     *
     * @param messageType ballerina struct type
     * @return message definition
     */
    private static Message generateMessageDefinition(org.wso2.ballerinalang.compiler.semantics.model.types.BType
                                                             messageType) throws GrpcServerException {
        Message message = null;
        switch (messageType.getKind()) {
            case STRING: {
                message = WrapperMessage.newBuilder(ServiceProtoConstants.WRAPPER_STRING_MESSAGE).build();
                break;
            }
            case INT: {
                message = WrapperMessage.newBuilder(ServiceProtoConstants.WRAPPER_INT64_MESSAGE).build();
                break;
            }
            case FLOAT: {
                message = WrapperMessage.newBuilder(ServiceProtoConstants.WRAPPER_FLOAT_MESSAGE).build();
                break;
            }
            case BOOLEAN: {
                message = WrapperMessage.newBuilder(ServiceProtoConstants.WRAPPER_BOOL_MESSAGE).build();
                break;
            }
            case STRUCT: {
                if (messageType instanceof org.wso2.ballerinalang.compiler.semantics.model.types.BStructType) {
                    org.wso2.ballerinalang.compiler.semantics.model.types.BStructType structType = (org
                            .wso2.ballerinalang.compiler.semantics.model.types.BStructType) messageType;
                    message = getStructMessage(structType);
                }
                break;
            }
            default: {
                throw new GrpcServerException("Unsupported field type, field type " + messageType.getKind()
                        .typeName() + " currently not supported.");
            }
        }
        return message;
    }

    private static Message getStructMessage(
            org.wso2.ballerinalang.compiler.semantics.model.types.BStructType messageType) throws GrpcServerException {
        UserDefinedMessage.Builder messageBuilder = UserDefinedMessage.newBuilder(messageType.toString());
        int fieldIndex = 0;
        for (org.wso2.ballerinalang.compiler.semantics.model.types.BStructType.BStructField structField :
                messageType.fields) {
            Field messageField;
            String fieldName = structField.getName().getValue();
            String sfieldtype = structField.getType().getKind().typeName();
            messageField = Field.newBuilder(fieldName)
                    .setIndex(++fieldIndex)
                    .setType(sfieldtype).build();
            messageBuilder.addFieldDefinition(messageField);
        }
        return messageBuilder.build();
    }

    private static BLangInvocation getInvocationExpression(BlockNode body) {
        if (body == null) {
            return null;
        }
        for (StatementNode statementNode : body.getStatements()) {
            BLangExpression expression = null;
            // example : conn.send inside while block.
            if (statementNode instanceof BLangWhile) {
                BLangWhile langWhile = (BLangWhile) statementNode;
                BLangInvocation invocExp = getInvocationExpression(langWhile.getBody());
                if (invocExp != null) {
                    return invocExp;
                }
            }
            // example : conn.send inside for block.
            if (statementNode instanceof BLangForeach) {
                BLangForeach langForeach = (BLangForeach) statementNode;
                BLangInvocation invocExp = getInvocationExpression(langForeach.getBody());
                if (invocExp != null) {
                    return invocExp;
                }
            }
            // example : conn.send inside if block.
            if (statementNode instanceof BLangIf) {
                BLangIf langIf = (BLangIf) statementNode;
                BLangInvocation invocExp = getInvocationExpression(langIf.getBody());
                if (invocExp != null) {
                    return invocExp;
                }
                invocExp = getInvocationExpression((BLangBlockStmt) langIf.getElseStatement());
                if (invocExp != null) {
                    return invocExp;
                }
            }
            // example : _ = conn.send(msg);
            if (statementNode instanceof BLangAssignment) {
                BLangAssignment assignment = (BLangAssignment) statementNode;
                expression = assignment.getExpression();
            }
            // example : grpc:HttpConnectorError err = conn.send(msg);
            if (statementNode instanceof BLangVariableDef) {
                BLangVariableDef variableDef = (BLangVariableDef) statementNode;
                BLangVariable variable = variableDef.getVariable();
                expression = variable.getInitialExpression();
            }

            if (expression != null && expression instanceof BLangInvocation) {
                BLangInvocation invocation = (BLangInvocation) expression;
                if ("send".equals(invocation.getName().getValue())) {
                    return invocation;
                }
            }
        }
        return null;
    }

    private static org.wso2.ballerinalang.compiler.semantics.model.types.BType getReturnType(BLangInvocation invocation)
            throws GrpcServerException {
        if (invocation.getArgumentExpressions() != null &&
                invocation.getArgumentExpressions().size() > 1) {
            throw new GrpcServerException("Incorrect argument expressions defined in send function: " +
                    invocation.toString());
        }
        if (invocation.getArgumentExpressions().size() == 1) {
            ExpressionNode node = invocation.getArgumentExpressions().get(0);
            if (node instanceof BLangNode) {
                BLangNode langNode = (BLangNode) node;
                return langNode.type;
            } else {
                throw new GrpcServerException("response message type is not supported: " + node.getKind()
                        .name());
            }
        } else {
            return null;
        }
    }

    /**
     * Returns protobuf file definition from Ballerina Service.
     *
     * @param service ballerina service
     * @return protobuf file definition
     */
/*    public static File generateServiceDefinition(org.ballerinalang.connector.api.Service service) throws
            GrpcServerException {
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
    }*/

    /**
     * Returns protobuf message definition from ballerina struct type.
     * <p>
     * /@param messageType ballerina struct type
     *
     * @return message definition
     */
/*    private static Message generateMessageDefinition(BType messageType) throws GrpcServerException {
        UserDefinedMessage.Builder requestMsgBuilder = UserDefinedMessage.newBuilder(messageType.getName());
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
    }*/
    public static com.google.protobuf.Descriptors.FileDescriptor getDescriptor(org.ballerinalang.connector.api
                                                                                       .Service service) {

        try {
            Path path = Paths.get(service.getName() + ServiceProtoConstants.DESC_FILE_EXTENSION);
            byte[] descriptor = Files.readAllBytes(path);

            DescriptorProtos.FileDescriptorProto proto = DescriptorProtos.FileDescriptorProto.parseFrom(descriptor);
            return Descriptors.FileDescriptor.buildFrom(proto, new com.google.protobuf.Descriptors.FileDescriptor[]{
                    com.google.protobuf.WrappersProto.getDescriptor(),
            });
        } catch (IOException | Descriptors.DescriptorValidationException e) {
            throw new RuntimeException("Error while reading the service proto descriptor. check the service " +
                    "implementation. ", e);
        }
    }

    /**
     * Write protobuf file definition content to the filename.
     *
     * @param protoFileDefinition protobuf file definition.
     * @param filename            filename
     * @throws GrpcServerException when error occur while writing content to file.
     */
    public static void writeServiceFiles(File protoFileDefinition, String filename) throws GrpcServerException {
        try {
            // write the proto string to the file in protobuf contract directory
            Path protoFilePath = Paths.get(filename + ServiceProtoConstants.PROTO_FILE_EXTENSION);
            Files.write(protoFilePath, protoFileDefinition.getFileDefinition().getBytes(ServiceProtoConstants
                    .UTF_8_CHARSET));

            // write the proto descriptor byte array to the file in protobuf contract directory
            byte[] fileDescriptor = protoFileDefinition.getFileDescriptorProto().toByteArray();
            Path descFilePath = Paths.get(filename + ServiceProtoConstants.DESC_FILE_EXTENSION);
            Files.write(descFilePath, fileDescriptor);
            List<byte[]> dependentDescriptorsList = new ArrayList<>();
            dependentDescriptorsList.add(com.google.protobuf.WrappersProto.getDescriptor
                    ().toProto().toByteArray());
            Path path = Paths.get("");
            BallerinaFile ballerinaFile = new BallerinaFile(dependentDescriptorsList);
            ballerinaFile.setRootDescriptor(fileDescriptor);
            ballerinaFile.build();
        } catch (IOException e) {
            throw new GrpcServerException("Error while writing file descriptor to file.", e);
        }
    }


    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] data) {
        char[] hexChars = new char[data.length * 2];
        for (int j = 0; j < data.length; j++) {
            int v = data[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
