/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.command.docs;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.RestParameterNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * This class provides functionality for generating documentation.
 *
 * @since 0.985.0
 */
public class DocumentationGenerator {

    private DocumentationGenerator() {
    }

    /**
     * Generate documentation for non-terminal node.
     *
     * @param node non-terminal node
     * @return optional documentation
     */
    public static Optional<DocAttachmentInfo> getDocumentationEditForNode(NonTerminalNode node, boolean skipIfExists) {
        switch (node.kind()) {
            case FUNCTION_DEFINITION:
            case OBJECT_METHOD_DEFINITION: {
                FunctionDefinitionNode functionDefNode = (FunctionDefinitionNode) node;
                Optional<MetadataNode> metadata = functionDefNode.metadata();
                if (skipIfExists && metadata.isPresent() && metadata.get().documentationString().isPresent()) {
                    return Optional.empty();
                }
                return Optional.of(getFunctionDefNodeDocumentation(functionDefNode));
            }
            case METHOD_DECLARATION: {
                MethodDeclarationNode methodDeclrNode = (MethodDeclarationNode) node;
                Optional<MetadataNode> metadata = methodDeclrNode.metadata();
                if (skipIfExists && metadata.isPresent() && metadata.get().documentationString().isPresent()) {
                    return Optional.empty();
                }
                return Optional.of(getMethodDeclrNodeDocumentation(methodDeclrNode));
            }
            case SERVICE_DECLARATION: {
                ServiceDeclarationNode serviceDeclrNode = (ServiceDeclarationNode) node;
                Optional<MetadataNode> metadata = serviceDeclrNode.metadata();
                if (skipIfExists && metadata.isPresent() && metadata.get().documentationString().isPresent()) {
                    return Optional.empty();
                }
                return Optional.of(getServiceDocumentation((ServiceDeclarationNode) node));
            }
            case TYPE_DEFINITION: {
                TypeDefinitionNode typeDefNode = (TypeDefinitionNode) node;
                Optional<MetadataNode> metadata = typeDefNode.metadata();
                if (skipIfExists && metadata.isPresent() && metadata.get().documentationString().isPresent()) {
                    return Optional.empty();
                }
                return Optional.of(getRecordOrObjectDocumentation(typeDefNode));
            }
            case CLASS_DEFINITION: {
                ClassDefinitionNode classDefNode = (ClassDefinitionNode) node;
                Optional<MetadataNode> metadata = classDefNode.metadata();
                if (skipIfExists && metadata.isPresent() && metadata.get().documentationString().isPresent()) {
                    return Optional.empty();
                }
                return Optional.of(getClassDefNodeDocumentation(classDefNode));
            }
            default:
                break;
        }
        return Optional.empty();
    }

    /**
     * Generate documentation for service node.
     *
     * @param serviceDeclrNode service declaration node
     * @return
     */
    private static DocAttachmentInfo getServiceDocumentation(ServiceDeclarationNode serviceDeclrNode) {
        MetadataNode metadata = serviceDeclrNode.metadata().orElse(null);
        Position docStart = CommonUtil.toRange(serviceDeclrNode.lineRange()).getStart();
        if (metadata != null && !metadata.annotations().isEmpty()) {
            docStart = CommonUtil.toRange(metadata.annotations().get(0).lineRange()).getStart();
        }
        int offset = docStart.getCharacter();
        return new DocAttachmentInfo(getDocumentationAttachment(null, offset), docStart);
    }

    /**
     * Generate documentation for function definition node.
     *
     * @param bLangFunction function definition node
     * @return
     */
    private static DocAttachmentInfo getFunctionDefNodeDocumentation(FunctionDefinitionNode bLangFunction) {
        return getFunctionNodeDocumentation(bLangFunction.functionSignature(),
                                            bLangFunction.metadata().orElse(null),
                                            CommonUtil.toRange(bLangFunction.lineRange()));
    }

    /**
     * Generate documentation for method declaration node.
     *
     * @param methodDeclrNode method declaration node
     * @return
     */
    private static DocAttachmentInfo getMethodDeclrNodeDocumentation(MethodDeclarationNode methodDeclrNode) {
        return getFunctionNodeDocumentation(methodDeclrNode.methodSignature(),
                                            methodDeclrNode.metadata().orElse(null),
                                            CommonUtil.toRange(methodDeclrNode.lineRange()));
    }

    /**
     * Generate documentation for record or object type definition node.
     *
     * @param typeDefNode type definition node
     * @return
     */
    private static DocAttachmentInfo getRecordOrObjectDocumentation(TypeDefinitionNode typeDefNode) {
        MetadataNode metadata = typeDefNode.metadata().orElse(null);
        Position docStart = CommonUtil.toRange(typeDefNode.lineRange()).getStart();
        if (metadata != null && !metadata.annotations().isEmpty()) {
            docStart = CommonUtil.toRange(metadata.annotations().get(0).lineRange()).getStart();
        }
        int offset = docStart.getCharacter();
        io.ballerina.compiler.syntax.tree.Node typeDesc = typeDefNode.typeDescriptor();
        List<String> attributes = new ArrayList<>();
        switch (typeDesc.kind()) {
            case RECORD_TYPE_DESC:
                RecordTypeDescriptorNode recordTypeDescNode = (RecordTypeDescriptorNode) typeDesc;
                recordTypeDescNode.fields().forEach(field -> {
                    Optional<Token> paramName = Optional.empty();
                    if (field.kind() == SyntaxKind.RECORD_FIELD) {
                        paramName = Optional.of(((RecordFieldNode) field).fieldName());
                    } else if (field.kind() == SyntaxKind.RECORD_FIELD_WITH_DEFAULT_VALUE) {
                        paramName = Optional.of(((RecordFieldWithDefaultValueNode) field).fieldName());
                    }
                    paramName.ifPresent(param -> attributes.add(getDocumentationAttribute(param.text(), offset)));
                });
                break;
            case OBJECT_TYPE_DESC:
                ObjectTypeDescriptorNode objectTypeDescNode = (ObjectTypeDescriptorNode) typeDesc;
                objectTypeDescNode.members().forEach(field -> {
                    if (field.kind() == SyntaxKind.OBJECT_FIELD &&
                            ((ObjectFieldNode) field).visibilityQualifier().isPresent()) {
                        ObjectFieldNode fieldNode = (ObjectFieldNode) field;
                        if (fieldNode.visibilityQualifier().get().kind() == SyntaxKind.PUBLIC_KEYWORD) {
                            attributes.add(getDocumentationAttribute(fieldNode.fieldName().text(), offset));
                        }
                    }
                });
                break;
            default:
                break;
        }
        return new DocAttachmentInfo(getDocumentationAttachment(attributes, offset), docStart);
    }

    /**
     * Generate documentation for class definition node.
     *
     * @param classDefNode class definition node
     * @return
     */
    private static DocAttachmentInfo getClassDefNodeDocumentation(ClassDefinitionNode classDefNode) {
        MetadataNode metadata = classDefNode.metadata().orElse(null);
        Position docStart = CommonUtil.toRange(classDefNode.lineRange()).getStart();
        if (metadata != null && !metadata.annotations().isEmpty()) {
            docStart = CommonUtil.toRange(metadata.annotations().get(0).lineRange()).getStart();
        }
        int offset = docStart.getCharacter();
        List<String> attributes = new ArrayList<>();
        classDefNode.members().forEach(field -> {
            if (field.kind() == SyntaxKind.OBJECT_FIELD &&
                    ((ObjectFieldNode) field).visibilityQualifier().isPresent()) {
                ObjectFieldNode fieldNode = (ObjectFieldNode) field;
                if (fieldNode.visibilityQualifier().get().kind() == SyntaxKind.PUBLIC_KEYWORD) {
                    attributes.add(getDocumentationAttribute(fieldNode.fieldName().text(), offset));
                }
            }
        });
        return new DocAttachmentInfo(getDocumentationAttachment(attributes, offset), docStart);
    }


    private static DocAttachmentInfo getFunctionNodeDocumentation(FunctionSignatureNode signatureNode,
                                                                  MetadataNode metadata, Range functionRange) {
        List<String> attributes = new ArrayList<>();
        Position docStart = functionRange.getStart();
        boolean hasDeprecated = false;
        if (metadata != null && !metadata.annotations().isEmpty()) {
            for (AnnotationNode annotationNode : metadata.annotations()) {
                io.ballerina.compiler.syntax.tree.Node annotReference = annotationNode.annotReference();
                if (annotReference.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE &&
                        "deprecated".equals(((SimpleNameReferenceNode) annotReference).name().text())) {
                    hasDeprecated = true;
                }
            }
            docStart = CommonUtil.toRange(metadata.annotations().get(0).lineRange()).getStart();
        }
        int offset = docStart.getCharacter();

        signatureNode.parameters().forEach(param -> {
            Optional<Token> paramName = Optional.empty();
            if (param.kind() == SyntaxKind.REQUIRED_PARAM) {
                paramName = ((RequiredParameterNode) param).paramName();
            } else if (param.kind() == SyntaxKind.DEFAULTABLE_PARAM) {
                paramName = ((DefaultableParameterNode) param).paramName();
            } else if (param.kind() == SyntaxKind.REST_PARAM) {
                paramName = ((RestParameterNode) param).paramName();
            }
            paramName.ifPresent(token -> attributes.add(getDocumentationAttribute(token.text(), offset)));
        });
        signatureNode.returnTypeDesc().ifPresent(s -> attributes.add(getReturnFieldDescription(offset)));
        if (hasDeprecated) {
            attributes.add(getDeprecatedDescription(offset));
        }
        return new DocAttachmentInfo(getDocumentationAttachment(attributes, functionRange.getStart().getCharacter()),
                                     docStart);
    }

    private static String getDocumentationAttribute(String field, int offset) {
        String offsetStr = String.join("", Collections.nCopies(offset, " "));
        return String.format("%s# + %s - %s Parameter Description", offsetStr, field, field);
    }

    private static String getReturnFieldDescription(int offset) {
        String offsetStr = String.join("", Collections.nCopies(offset, " "));
        return String.format("%s# + return - Return Value Description", offsetStr);
    }

    private static String getDeprecatedDescription(int offset) {
        String offsetStr = String.join("", Collections.nCopies(offset, " "));
        return String.format("%s# # Deprecated", offsetStr);
    }

    private static String getDocumentationAttachment(List<String> attributes, int offset) {
        String offsetStr = String.join("", Collections.nCopies(offset, " "));
        if (attributes == null || attributes.isEmpty()) {
            return String.format("# Description%n%s", offsetStr);
        }

        String joinedList = String.join(" \r\n", attributes);
        return String.format("# Description%n%s#%n%s%n%s", offsetStr, joinedList, offsetStr);
    }
}
