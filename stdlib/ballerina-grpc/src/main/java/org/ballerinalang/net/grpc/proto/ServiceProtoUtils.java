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
import org.ballerinalang.model.tree.ResourceNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.VariableNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.statements.BlockNode;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.ballerinalang.net.grpc.MessageConstants;
import org.ballerinalang.net.grpc.builder.BallerinaFileBuilder;
import org.ballerinalang.net.grpc.config.ServiceConfiguration;
import org.ballerinalang.net.grpc.exception.GrpcServerException;
import org.ballerinalang.net.grpc.proto.definition.EmptyMessage;
import org.ballerinalang.net.grpc.proto.definition.Field;
import org.ballerinalang.net.grpc.proto.definition.File;
import org.ballerinalang.net.grpc.proto.definition.Message;
import org.ballerinalang.net.grpc.proto.definition.MessageKind;
import org.ballerinalang.net.grpc.proto.definition.Method;
import org.ballerinalang.net.grpc.proto.definition.Service;
import org.ballerinalang.net.grpc.proto.definition.UserDefinedMessage;
import org.ballerinalang.net.grpc.proto.definition.WrapperMessage;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
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

import static org.ballerinalang.net.grpc.MessageConstants.ANN_ATTR_RESOURCE_SERVER_STREAM;
import static org.ballerinalang.net.grpc.MessageConstants.ANN_RESOURCE_CONFIG;
import static org.ballerinalang.net.grpc.MessageConstants.ON_COMPLETE_RESOURCE;
import static org.ballerinalang.net.grpc.MessageConstants.ON_MESSAGE_RESOURCE;


/**
 * Utility class providing proto file based of the Ballerina service.
 *
 * @since 1.0.0
 */
public class ServiceProtoUtils {
    
    static File generateProtoDefinition(ServiceNode serviceNode) throws GrpcServerException {
        // Protobuf file definition builder.
        String packageName = serviceNode.getPosition().getSource().getPackageName();
        File.Builder fileBuilder;
        if (!".".equals(packageName)) {
            fileBuilder = File.newBuilder(serviceNode.getName() + ServiceProtoConstants.PROTO_FILE_EXTENSION)
                    .setSyntax(ServiceProtoConstants.PROTOCOL_SYNTAX).setPackage(serviceNode.getPosition().getSource()
                            .getPackageName());
        } else {
            fileBuilder = File.newBuilder(serviceNode.getName() + ServiceProtoConstants.PROTO_FILE_EXTENSION)
                    .setSyntax(ServiceProtoConstants.PROTOCOL_SYNTAX);
        }
        ServiceConfiguration serviceConfig = getServiceConfiguration(serviceNode);
        Service serviceDefinition;
        if (serviceConfig.getRpcEndpoint() != null && (serviceConfig.isClientStreaming())) {
            serviceDefinition = getStreamingServiceDefinition(serviceNode, serviceConfig, fileBuilder);
        } else {
            serviceDefinition = getUnaryServiceDefinition(serviceNode, fileBuilder);
        }
        
        fileBuilder.setService(serviceDefinition);
        return fileBuilder.build();
    }
    
    static ServiceConfiguration getServiceConfiguration(ServiceNode serviceNode) {
        String rpcEndpoint = null;
        boolean clientStreaming = false;
        boolean serverStreaming = false;
        boolean generateClientConnector = false;
        
        for (AnnotationAttachmentNode annotationNode : serviceNode.getAnnotationAttachments()) {
            if (!ServiceProtoConstants.ANN_SERVICE_CONFIG.equals(annotationNode.getAnnotationName().getValue())) {
                continue;
            }
            if (annotationNode.getExpression() instanceof BLangRecordLiteral) {
                List<BLangRecordLiteral.BLangRecordKeyValue> attributes = ((BLangRecordLiteral) annotationNode
                        .getExpression()).getKeyValuePairs();
                for (BLangRecordLiteral.BLangRecordKeyValue attributeNode : attributes) {
                    String attributeName = attributeNode.getKey().toString();
                    String attributeValue = attributeNode.getValue() != null ? attributeNode.getValue().toString() :
                            null;
                    
                    switch (attributeName) {
                        case ServiceProtoConstants.SERVICE_CONFIG_RPC_ENDPOINT: {
                            rpcEndpoint = attributeValue != null ? attributeValue : null;
                            break;
                        }
                        case ServiceProtoConstants.SERVICE_CONFIG_CLIENT_STREAMING: {
                            clientStreaming = attributeValue != null && Boolean.parseBoolean(attributeValue);
                            break;
                        }
                        case ServiceProtoConstants.SERVICE_CONFIG_SERVER_STREAMING: {
                            serverStreaming = attributeValue != null && Boolean.parseBoolean(attributeValue);
                            break;
                        }
                        case ServiceProtoConstants.SERVICE_CONFIG_GENERATE_CLIENT: {
                            generateClientConnector = attributeValue != null && Boolean.parseBoolean(attributeValue);
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                }
            }
        }
        return new ServiceConfiguration(rpcEndpoint, clientStreaming, serverStreaming,
                generateClientConnector);
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
    
    private static Service getStreamingServiceDefinition(ServiceNode serviceNode, ServiceConfiguration serviceConfig,
                                                         File.Builder fileBuilder) throws GrpcServerException {
        // Protobuf service definition builder.
        Service.Builder serviceBuilder = Service.newBuilder(serviceNode.getName().getValue());
        Message requestMessage = null;
        Message responseMessage = null;
        for (ResourceNode resourceNode : serviceNode.getResources()) {
            if (ON_MESSAGE_RESOURCE.equals(resourceNode.getName().getValue())) {
                requestMessage = getRequestMessage(resourceNode);
                Message respMsg = getResponseMessage(resourceNode);
                if (respMsg != null && !(MessageKind.EMPTY.equals(respMsg.getMessageKind()))) {
                    responseMessage = getResponseMessage(resourceNode);
                    break;
                }
            }
            
            if (ON_COMPLETE_RESOURCE.equals(resourceNode.getName().getValue())) {
                Message respMsg = getResponseMessage(resourceNode);
                if (respMsg != null && !(MessageKind.EMPTY.equals(respMsg.getMessageKind()))) {
                    responseMessage = respMsg;
                }
            }
        }
        
        if (requestMessage == null) {
            throw new GrpcServerException("Cannot find request message definition for streaming service: " +
                    serviceNode.getName().getValue());
        }
        if (responseMessage == null) {
            responseMessage = generateMessageDefinition(new BNilType());
            /*throw new GrpcServerException("Cannot find response message definition for streaming service: " +
                    serviceNode.getName().getValue());*/
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
            if (!ANN_RESOURCE_CONFIG.equals(annotationNode.getAnnotationName().getValue())) {
                continue;
            }
            
            if (annotationNode.getExpression() instanceof BLangRecordLiteral) {
                List<BLangRecordLiteral.BLangRecordKeyValue> attributes = ((BLangRecordLiteral) annotationNode
                        .getExpression()).getKeyValuePairs();
                for (BLangRecordLiteral.BLangRecordKeyValue attributeNode : attributes) {
                    String attributeName = attributeNode.getKey().toString();
                    String attributeValue = attributeNode.getValue() != null ? attributeNode.getValue().toString() :
                            null;
                    if (ANN_ATTR_RESOURCE_SERVER_STREAM.equals(attributeName)) {
                        serverStreaming = attributeValue != null && Boolean.parseBoolean(attributeValue);
                    }
                }
            }
        }
        return serverStreaming;
    }
    
    private static Message getResponseMessage(ResourceNode resourceNode) throws GrpcServerException {
        org.wso2.ballerinalang.compiler.semantics.model.types.BType responseType;
        BLangInvocation sendExpression = getInvocationExpression(resourceNode.getBody());
        if (sendExpression != null) {
            responseType = getReturnType(sendExpression);
        } else {
            // if compiler plugin could not find
            responseType = new BNilType();
        }
        return responseType != null ? generateMessageDefinition(responseType) : null;
    }
    
    private static Message getRequestMessage(ResourceNode resourceNode) throws GrpcServerException {
        if (!(resourceNode.getParameters().size() == 1 || resourceNode.getParameters().size() == 2)) {
            throw new GrpcServerException("Service resource definition should contain either one param or two params." +
                    " but contains " + resourceNode.getParameters().size());
        }

        org.wso2.ballerinalang.compiler.semantics.model.types.BType requestType;
        if (resourceNode.getParameters().size() == 2) {
            VariableNode requestVariable = resourceNode.getParameters().get(MessageConstants
                    .REQUEST_MESSAGE_PARAM_INDEX);
            if (requestVariable instanceof BLangVariable) {
                requestType = ((BLangVariable) requestVariable).type;
            } else {
                throw new GrpcServerException("Request Message type is not supported, should be lang variable.");
            }
        } else {
            requestType = new BNilType();
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
            case NIL: {
                message = EmptyMessage.newBuilder().build();
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
            return new BNilType();
        }
    }

    /**
     * Returns file descriptor for the service.
     * Reads file descriptor from the .desc file generated at compile time.
     *
     * @param service gRPC service.
     * @return File Descriptor of the service.
     */
    public static com.google.protobuf.Descriptors.FileDescriptor getDescriptor(org.ballerinalang.connector.api
                                                                                       .Service service) {
        try {
            Path path = Paths.get(service.getName() + ServiceProtoConstants.DESC_FILE_EXTENSION);
            byte[] descriptor = Files.readAllBytes(path);
            DescriptorProtos.FileDescriptorProto proto = DescriptorProtos.FileDescriptorProto.parseFrom(descriptor);
            Descriptors.FileDescriptor fileDescriptor = Descriptors.FileDescriptor.buildFrom(proto, new com.google
                    .protobuf.Descriptors.FileDescriptor[] {com.google.protobuf.WrappersProto.getDescriptor(), com
                    .google.protobuf.EmptyProto.getDescriptor()});
            Files.delete(path);
            return fileDescriptor;
        } catch (IOException | Descriptors.DescriptorValidationException e) {
            throw new BallerinaException("Error while reading the service proto descriptor. check the service " +
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
    static void writeServiceFiles(File protoFileDefinition, String filename, boolean generateClientConnector)
            throws GrpcServerException {
        try {
            // write the proto string to the file in protobuf contract directory
            Path protoFilePath = Paths.get(filename + ServiceProtoConstants.PROTO_FILE_EXTENSION);
            Files.write(protoFilePath, protoFileDefinition.getFileDefinition().getBytes(ServiceProtoConstants
                    .UTF_8_CHARSET));
            
            // write the proto descriptor byte array to the file in protobuf contract directory
            byte[] fileDescriptor = protoFileDefinition.getFileDescriptorProto().toByteArray();
            Path descFilePath = Paths.get(filename + ServiceProtoConstants.DESC_FILE_EXTENSION);
            Files.write(descFilePath, fileDescriptor);
            
            if (generateClientConnector) {
                List<byte[]> dependentDescriptorsList = new ArrayList<>();
                dependentDescriptorsList.add(com.google.protobuf.WrappersProto.getDescriptor
                        ().toProto().toByteArray());
                //Path path = Paths.get("");
                BallerinaFileBuilder ballerinaFileBuilder = new BallerinaFileBuilder(dependentDescriptorsList);
                ballerinaFileBuilder.setRootDescriptor(fileDescriptor);
                ballerinaFileBuilder.build();
            }
        } catch (IOException e) {
            throw new GrpcServerException("Error while writing file descriptor to file.", e);
        }
    }
}
