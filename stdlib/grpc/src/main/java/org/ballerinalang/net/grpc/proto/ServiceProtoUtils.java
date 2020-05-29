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
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.BlockNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.ballerinalang.model.types.FiniteType;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.net.grpc.config.ResourceConfiguration;
import org.ballerinalang.net.grpc.config.ServiceConfiguration;
import org.ballerinalang.net.grpc.exception.GrpcServerException;
import org.ballerinalang.net.grpc.proto.definition.EmptyMessage;
import org.ballerinalang.net.grpc.proto.definition.EnumField;
import org.ballerinalang.net.grpc.proto.definition.Field;
import org.ballerinalang.net.grpc.proto.definition.File;
import org.ballerinalang.net.grpc.proto.definition.Message;
import org.ballerinalang.net.grpc.proto.definition.MessageKind;
import org.ballerinalang.net.grpc.proto.definition.Method;
import org.ballerinalang.net.grpc.proto.definition.Service;
import org.ballerinalang.net.grpc.proto.definition.UserDefinedEnumMessage;
import org.ballerinalang.net.grpc.proto.definition.UserDefinedMessage;
import org.ballerinalang.net.grpc.proto.definition.WrapperMessage;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.net.grpc.GrpcConstants.ANN_ATTR_RESOURCE_SERVER_STREAM;
import static org.ballerinalang.net.grpc.GrpcConstants.ANN_RESOURCE_CONFIG;
import static org.ballerinalang.net.grpc.GrpcConstants.BYTE;
import static org.ballerinalang.net.grpc.GrpcConstants.CALLER_ENDPOINT_TYPE;
import static org.ballerinalang.net.grpc.GrpcConstants.ON_COMPLETE_RESOURCE;
import static org.ballerinalang.net.grpc.GrpcConstants.ON_MESSAGE_RESOURCE;
import static org.ballerinalang.net.grpc.GrpcConstants.WRAPPER_BOOL_MESSAGE;
import static org.ballerinalang.net.grpc.GrpcConstants.WRAPPER_BYTES_MESSAGE;
import static org.ballerinalang.net.grpc.GrpcConstants.WRAPPER_FLOAT_MESSAGE;
import static org.ballerinalang.net.grpc.GrpcConstants.WRAPPER_INT64_MESSAGE;
import static org.ballerinalang.net.grpc.GrpcConstants.WRAPPER_STRING_MESSAGE;

/**
 * Utility class providing proto file based of the Ballerina service.
 *
 * @since 1.0.0
 */
public class ServiceProtoUtils {

    private static final String NO_PACKAGE_PATH = ".";
    private static final BNilType bNilType = new BNilType();

    private ServiceProtoUtils() {

    }

    static File generateProtoDefinition(ServiceNode serviceNode) throws GrpcServerException {
        // Protobuf file definition builder.
        String packageName = serviceNode.getPosition().getSource().getPackageName();
        File.Builder fileBuilder;
        if (!NO_PACKAGE_PATH.equals(packageName)) {
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
        BType requestType = null;
        BType responseType = null;
        boolean clientStreaming = false;
        boolean serverStreaming = false;
        
        for (AnnotationAttachmentNode annotationNode : serviceNode.getAnnotationAttachments()) {
            if (!ServiceProtoConstants.ANN_SERVICE_CONFIG.equals(annotationNode.getAnnotationName().getValue())) {
                continue;
            }
            if (annotationNode.getExpression() instanceof BLangRecordLiteral) {
                List<BLangRecordLiteral.BLangRecordKeyValueField> attributes = new ArrayList<>();

                for (RecordLiteralNode.RecordField attribute :
                        ((BLangRecordLiteral) annotationNode.getExpression()).getFields()) {
                    attributes.add((BLangRecordLiteral.BLangRecordKeyValueField) attribute);
                }

                for (BLangRecordLiteral.BLangRecordKeyValueField attributeNode : attributes) {
                    String attributeName = attributeNode.getKey().toString();
                    BLangExpression attributeValue = attributeNode.getValue();
                    
                    switch (attributeName) {
                        case ServiceProtoConstants.SERVICE_CONFIG_RPC_ENDPOINT: {
                            rpcEndpoint = attributeValue != null ? attributeValue.toString() : null;
                            break;
                        }
                        case ServiceProtoConstants.SERVICE_CONFIG_CLIENT_STREAMING: {
                            clientStreaming = attributeValue != null && Boolean.parseBoolean(attributeValue.toString());
                            break;
                        }
                        case ServiceProtoConstants.SERVICE_CONFIG_SERVER_STREAMING: {
                            serverStreaming = attributeValue != null && Boolean.parseBoolean(attributeValue.toString());
                            break;
                        }
                        case ServiceProtoConstants.RESOURCE_CONFIG_REQUEST_TYPE: {
                            requestType = getMessageBType(attributeValue);
                            break;
                        }
                        case ServiceProtoConstants.RESOURCE_CONFIG_RESPONSE_TYPE: {
                            responseType = getMessageBType(attributeValue);
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                }
            }
        }
        return new ServiceConfiguration(rpcEndpoint, requestType, responseType, clientStreaming, serverStreaming);
    }

    private static BType getMessageBType(BLangExpression attributeValue) {
        BType requestType = null;
        if (NodeKind.SIMPLE_VARIABLE_REF.equals(attributeValue.getKind())) {
            requestType = ((BLangSimpleVarRef) attributeValue).symbol.getType();
        } else if (NodeKind.TYPEDESC_EXPRESSION.equals(attributeValue.getKind())) {
            requestType = ((BLangTypedescExpr) attributeValue).resolvedType;
        }
        return requestType;
    }

    private static ResourceConfiguration getResourceConfiguration(FunctionNode resourceNode) {
        boolean streaming = false;
        BType requestType = null;
        BType responseType = null;

        for (AnnotationAttachmentNode annotationNode : resourceNode.getAnnotationAttachments()) {
            if (!ServiceProtoConstants.ANN_RESOURCE_CONFIG.equals(annotationNode.getAnnotationName().getValue())) {
                continue;
            }
            if (annotationNode.getExpression() instanceof BLangRecordLiteral) {
                List<BLangRecordLiteral.BLangRecordKeyValueField> attributes = new ArrayList<>();

                for (RecordLiteralNode.RecordField attribute :
                        ((BLangRecordLiteral) annotationNode.getExpression()).getFields()) {
                    attributes.add((BLangRecordLiteral.BLangRecordKeyValueField) attribute);
                }

                for (BLangRecordLiteral.BLangRecordKeyValueField attributeNode : attributes) {
                    String attributeName = attributeNode.getKey().toString();
                    BLangExpression attributeValue = attributeNode.getValue();

                    switch (attributeName) {
                        case ServiceProtoConstants.RESOURCE_CONFIG_STREAMING: {
                            streaming = attributeValue != null && Boolean.parseBoolean(attributeValue.toString());
                            break;
                        }
                        case ServiceProtoConstants.RESOURCE_CONFIG_REQUEST_TYPE: {
                            requestType = getMessageBType(attributeValue);
                            break;
                        }
                        case ServiceProtoConstants.RESOURCE_CONFIG_RESPONSE_TYPE: {
                            responseType = getMessageBType(attributeValue);
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                }
            }
        }
        return new ResourceConfiguration(streaming, requestType, responseType);
    }
    
    private static Service getUnaryServiceDefinition(ServiceNode serviceNode, File.Builder fileBuilder) throws
            GrpcServerException {
        // Protobuf service definition builder.
        Service.Builder serviceBuilder = Service.newBuilder(serviceNode.getName().getValue());

        for (FunctionNode resourceNode : serviceNode.getResources()) {
            ResourceConfiguration resourceConfiguration = getResourceConfiguration(resourceNode);
            Message requestMessage;
            if (resourceConfiguration.getRequestType() != null) {
                requestMessage = generateMessageDefinition(resourceConfiguration.getRequestType());
            } else {
                requestMessage = getRequestMessage(resourceNode);
            }
            if (requestMessage == null) {
                throw new GrpcServerException("Error while deriving request message of the resource");
            }
            if (requestMessage.getMessageKind() == MessageKind.USER_DEFINED) {
                updateFileBuilder(fileBuilder, requestMessage);
            }
            if (requestMessage.getDependency() != null && !fileBuilder.getRegisteredDependencies().contains
                    (requestMessage.getDependency())) {
                fileBuilder.setDependency(requestMessage.getDependency());
            }

            Message responseMessage;
            if (resourceConfiguration.getResponseType() != null) {
                responseMessage = generateMessageDefinition(resourceConfiguration.getResponseType());
            } else {
                responseMessage = getResponseMessage(resourceNode);
            }
            if (responseMessage == null) {
                throw new GrpcServerException("Connection send expression not found in resource body");
            }
            if (responseMessage.getMessageKind() == MessageKind.USER_DEFINED) {
                updateFileBuilder(fileBuilder, responseMessage);
            }
            if (responseMessage.getDependency() != null && !fileBuilder.getRegisteredDependencies().contains
                    (responseMessage.getDependency())) {
                fileBuilder.setDependency(responseMessage.getDependency());
            }
            
            boolean serverStreaming = isServerStreaming(resourceNode);
            Method resourceMethod = Method.newBuilder(resourceNode.getName().getValue())
                    .setClientStreaming(false)
                    .setServerStreaming(serverStreaming)
                    .setInputType(requestMessage.getCanonicalName())
                    .setOutputType(responseMessage.getCanonicalName())
                    .build();
            serviceBuilder.addMethod(resourceMethod);
        }
        return serviceBuilder.build();
    }

    private static boolean isNewMessageDefinition(File.Builder fileBuilder, Message message) {
        for (DescriptorProtos.DescriptorProto messageProto : fileBuilder.getRegisteredMessages()) {
            if (messageProto.getName() == null) {
                continue;
            }
            if (messageProto.getName().equals(message.getSimpleName())) {
                return false;
            }
        }
        return true;
    }

    private static boolean isNewEnumDefinition(File.Builder fileBuilder, Message enumMsg) {
        for (DescriptorProtos.EnumDescriptorProto enumDescriptorProto : fileBuilder.getRegisteredEnums()) {
            if (enumDescriptorProto.getName() == null) {
                continue;
            }
            if (enumDescriptorProto.getName().equals(enumMsg.getSimpleName())) {
                return false;
            }
        }
        return true;
    }

    private static Service getStreamingServiceDefinition(ServiceNode serviceNode, ServiceConfiguration serviceConfig,
                                                         File.Builder fileBuilder) throws GrpcServerException {
        // Protobuf service definition builder.
        Service.Builder serviceBuilder = Service.newBuilder(serviceNode.getName().getValue());
        Message requestMessage = null;
        Message responseMessage = null;
        if (serviceConfig.getRequestType() != null) {
            requestMessage = generateMessageDefinition(serviceConfig.getRequestType());
        }
        if (serviceConfig.getResponseType() != null) {
            responseMessage = generateMessageDefinition(serviceConfig.getResponseType());
        }
        if (requestMessage == null || responseMessage == null) {
            for (FunctionNode resourceNode : serviceNode.getResources()) {
                if (ON_MESSAGE_RESOURCE.equals(resourceNode.getName().getValue())) {
                    requestMessage = requestMessage == null ? getRequestMessage(resourceNode) : requestMessage;
                    Message respMsg = responseMessage == null ? getResponseMessage(resourceNode) : responseMessage;
                    if (respMsg != null && !(MessageKind.EMPTY.equals(respMsg.getMessageKind()))) {
                        responseMessage = respMsg;
                        break;
                    }
                }

                if (ON_COMPLETE_RESOURCE.equals(resourceNode.getName().getValue())) {
                    Message respMsg = responseMessage == null ? getResponseMessage(resourceNode) : responseMessage;
                    if (respMsg != null && !(MessageKind.EMPTY.equals(respMsg.getMessageKind()))) {
                        responseMessage = respMsg;
                    }
                }
            }
        }
        // if we cannot retrieve request/response messages. assuming it is empty type.
        if (requestMessage == null) {
            requestMessage = generateMessageDefinition(bNilType);
        }
        if (responseMessage == null) {
            responseMessage = generateMessageDefinition(bNilType);
        }
        // update file builder with request/response messages.
        if (requestMessage.getMessageKind() == MessageKind.USER_DEFINED) {
            updateFileBuilder(fileBuilder, requestMessage);
        }
        if (requestMessage.getDependency() != null && !fileBuilder.getRegisteredDependencies().contains
                (requestMessage.getDependency())) {
            fileBuilder.setDependency(requestMessage.getDependency());
        }
        if (responseMessage.getMessageKind() == MessageKind.USER_DEFINED) {
            updateFileBuilder(fileBuilder, responseMessage);
        }
        if (responseMessage.getDependency() != null && !fileBuilder.getRegisteredDependencies().contains
                (responseMessage.getDependency())) {
            fileBuilder.setDependency(responseMessage.getDependency());
        }
        Method resourceMethod = Method.newBuilder(serviceConfig.getRpcEndpoint())
                .setClientStreaming(serviceConfig.isClientStreaming())
                .setServerStreaming(serviceConfig.isServerStreaming())
                .setInputType(requestMessage.getCanonicalName())
                .setOutputType(responseMessage.getCanonicalName())
                .build();
        serviceBuilder.addMethod(resourceMethod);
        return serviceBuilder.build();
    }

    private static void updateFileBuilder(File.Builder fileBuilder, Message message) {
        if (isNewMessageDefinition(fileBuilder, message)) {
            fileBuilder.setMessage(message);
        }
        for (UserDefinedMessage msg : message.getNestedMessageList()) {
            if (isNewMessageDefinition(fileBuilder, msg)) {
                fileBuilder.setMessage(msg);
            }
        }
        for (UserDefinedEnumMessage enumMessage : message.getNestedEnumList()) {
            if (isNewEnumDefinition(fileBuilder, enumMessage)) {
                fileBuilder.setEnum(enumMessage);
            }
        }
    }

    private static boolean isServerStreaming(FunctionNode resourceNode) {
        boolean serverStreaming = false;
        for (AnnotationAttachmentNode annotationNode : resourceNode.getAnnotationAttachments()) {
            if (!ANN_RESOURCE_CONFIG.equals(annotationNode.getAnnotationName().getValue())) {
                continue;
            }
            
            if (annotationNode.getExpression() instanceof BLangRecordLiteral) {
                List<BLangRecordLiteral.BLangRecordKeyValueField> attributes = new ArrayList<>();

                for (RecordLiteralNode.RecordField attribute :
                        ((BLangRecordLiteral) annotationNode.getExpression()).getFields()) {
                    attributes.add((BLangRecordLiteral.BLangRecordKeyValueField) attribute);
                }
                for (BLangRecordLiteral.BLangRecordKeyValueField attributeNode : attributes) {
                    String attributeName = attributeNode.getKey().toString();
                    String attributeValue = attributeNode.getValue() != null ? attributeNode.getValue().toString() :
                            null;
                    if (ANN_ATTR_RESOURCE_SERVER_STREAM.equals(attributeName)) {
                        serverStreaming = Boolean.parseBoolean(attributeValue);
                    }
                }
            }
        }
        return serverStreaming;
    }

    private static Message getResponseMessage(FunctionNode resourceNode) throws GrpcServerException {
        org.wso2.ballerinalang.compiler.semantics.model.types.BType responseType;
        BLangFunctionBody body = resourceNode.getBody();
        BLangInvocation sendExpression =
                (body != null && body.getKind() == NodeKind.BLOCK_FUNCTION_BODY) ?
                        getInvocationExpression((BLangBlockFunctionBody) body) : null;
        if (sendExpression != null) {
            responseType = getReturnType(sendExpression);
        } else {
            // if compiler plugin could not find
            responseType = bNilType;
        }
        return generateMessageDefinition(responseType);
    }

    private static Message getRequestMessage(FunctionNode resourceNode) throws GrpcServerException {
        if (!(!resourceNode.getParameters().isEmpty() || resourceNode.getParameters().size() < 4)) {
            throw new GrpcServerException("Service resource definition should contain more than one and less than " +
                    "four params. but contains " + resourceNode.getParameters().size());
        }
        BType requestType = getMessageParamType(resourceNode.getParameters());
        return generateMessageDefinition(requestType);
    }

    private static BType getMessageParamType(List<?> variableNodes) throws GrpcServerException {
        BType requestType = null;
        for (Object variable : variableNodes)  {
            if (variable instanceof BLangNode) {
                BType tempType = ((BLangNode) variable).type;
                if (tempType.getKind().equals(TypeKind.ARRAY)) {
                    requestType = tempType;
                    break;
                }
                // union type and tuple type doesn't have type symbol. If those values pass as response, compiler
                // failed to derive response message type. Validate and send proper error message.
                if (tempType.tsymbol == null) {
                    throw new GrpcServerException("Invalid message type. Message type doesn't have type symbol");
                }

                if (CALLER_ENDPOINT_TYPE.equals(tempType.tsymbol.toString()) || "ballerina/grpc:Headers"
                        .equals(tempType.tsymbol.toString())) {
                    continue;
                }
                requestType = tempType;
                break;
            } else {
                throw new GrpcServerException("Request Message type is not supported, should be lang variable.");
            }
        }
        if (requestType == null) {
            requestType = bNilType;
        }
        return requestType;
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
                message = WrapperMessage.newBuilder(WRAPPER_STRING_MESSAGE).build();
                break;
            }
            case INT: {
                message = WrapperMessage.newBuilder(WRAPPER_INT64_MESSAGE).build();
                break;
            }
            case FLOAT: {
                message = WrapperMessage.newBuilder(WRAPPER_FLOAT_MESSAGE).build();
                break;
            }
            case BOOLEAN: {
                message = WrapperMessage.newBuilder(WRAPPER_BOOL_MESSAGE).build();
                break;
            }
            case ARRAY: {
                message = WrapperMessage.newBuilder(WRAPPER_BYTES_MESSAGE).build();
                break;
            }
            case OBJECT:
            case RECORD: {
                if (messageType instanceof org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType) {
                    org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType structType = (org
                            .wso2.ballerinalang.compiler.semantics.model.types.BStructureType) messageType;
                    message = getStructMessage(structType);
                }
                break;
            }
            case NIL: {
                message = EmptyMessage.newBuilder().build();
                break;
            }
            default: {
                throw new GrpcServerException("Field type '" + messageType.toString() + "' currently not supported");
            }
        }
        return message;
    }
    
    private static Message getStructMessage(BStructureType messageType) throws
            GrpcServerException {
        UserDefinedMessage.Builder messageBuilder = UserDefinedMessage.newBuilder(messageType.tsymbol.name.value);
        int fieldIndex = 0;
        for (BField structField : messageType.fields.values()) {
            Field messageField;
            String fieldName = structField.getName().getValue();
            BType fieldType = structField.getType();
            String fieldLabel = null;
            if (fieldType instanceof BStructureType) {
                BStructureType structType = (BStructureType) fieldType;
                messageBuilder.addMessageDefinition(getStructMessage(structType));
            } else if (fieldType instanceof BArrayType) {
                BArrayType arrayType = (BArrayType) fieldType;
                BType elementType = arrayType.getElementType();
                if (elementType instanceof BStructureType) {
                    messageBuilder.addMessageDefinition(getStructMessage((BStructureType) elementType));
                }
                fieldType = elementType;
                if (!fieldType.toString().equals(BYTE)) {
                    fieldLabel = "repeated";
                }
            } else if (fieldType instanceof FiniteType) {
                UserDefinedEnumMessage.Builder enumBuilder = UserDefinedEnumMessage
                        .newBuilder(fieldType.tsymbol.name.value);
                BFiniteType finiteType = (BFiniteType) fieldType;
                int enumFieldIndex = 0;
                EnumField.Builder enumFieldBuilder = EnumField.newBuilder();
                EnumField enumField;
                for (BLangExpression bLangExpression : finiteType.getValueSpace()) {
                    String enumValue = String.valueOf(bLangExpression);
                    enumField = enumFieldBuilder.setIndex(enumFieldIndex++).setName(enumValue).build();
                    enumBuilder.addFieldDefinition(enumField);
                }
                messageBuilder.addMessageDefinition(enumBuilder.build());
            }
            messageField = Field.newBuilder(fieldName)
                    .setIndex(++fieldIndex)
                    .setLabel(fieldLabel)
                    .setType(fieldType).build();
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
            // send inside while block.
            if (statementNode instanceof BLangWhile) {
                BLangWhile langWhile = (BLangWhile) statementNode;
                BLangInvocation invocExp = getInvocationExpression(langWhile.getBody());
                if (invocExp != null) {
                    return invocExp;
                }
            }
            // send inside for block.
            if (statementNode instanceof BLangForeach) {
                BLangForeach langForeach = (BLangForeach) statementNode;
                BLangInvocation invocExp = getInvocationExpression(langForeach.getBody());
                if (invocExp != null) {
                    return invocExp;
                }
            }
            // send inside if block.
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
            //send inside match block.
            if (statementNode instanceof BLangMatch) {
                BLangMatch langMatch = (BLangMatch) statementNode;
                for (BLangMatch.BLangMatchBindingPatternClause patternClause : langMatch.patternClauses) {
                    BLangInvocation invocExp = getInvocationExpression(patternClause.body);
                    if (invocExp != null) {
                        return invocExp;
                    }
                }
            }
            // ignore return value of send method.
            if (statementNode instanceof BLangAssignment) {
                BLangAssignment assignment = (BLangAssignment) statementNode;
                expression = assignment.getExpression();
            }
            // variable assignment.
            if (statementNode instanceof BLangSimpleVariableDef) {
                BLangSimpleVariableDef variableDef = (BLangSimpleVariableDef) statementNode;
                BLangSimpleVariable variable = variableDef.getVariable();
                expression = variable.getInitialExpression();
            }

            // Expression statement.
            if (statementNode instanceof BLangExpressionStmt) {
                BLangExpressionStmt expressionStmt = (BLangExpressionStmt) statementNode;
                BLangExpression langExpression = expressionStmt.getExpression();
                // checked expression
                if (langExpression instanceof BLangCheckedExpr) {
                    BLangCheckedExpr checkedExpr = (BLangCheckedExpr) langExpression;
                    expression = checkedExpr.getExpression();
                }
            }
            
            if (expression instanceof BLangInvocation) {
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
                invocation.getArgumentExpressions().size() > 2) {
            throw new GrpcServerException("Incorrect argument expressions defined in send function: " +
                    invocation.toString());
        }
        return getMessageParamType(invocation.getArgumentExpressions());
    }

    /**
     * Write protobuf file definition content to the filename.
     *
     * @param protoFileDefinition protobuf file definition.
     * @param filename            filename
     * @throws GrpcServerException when error occur while writing content to file.
     */
    static void writeServiceFiles(Path targetDirPath, String filename, File protoFileDefinition)
            throws GrpcServerException {
        if (targetDirPath == null) {
            throw new GrpcServerException("Target file directory path is null");
        }
        try {
            // create parent directory. if doesn't exist.
            if (!Files.exists(targetDirPath)) {
                Files.createDirectories(targetDirPath);
            }
            // write the proto string to the file in protobuf contract directory
            Path protoFilePath = Paths.get(targetDirPath.toString(), filename + ServiceProtoConstants
                    .PROTO_FILE_EXTENSION);
            Files.write(protoFilePath, protoFileDefinition.getFileDefinition().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new GrpcServerException("Error while writing file descriptor to file.", e);
        }
    }
}
