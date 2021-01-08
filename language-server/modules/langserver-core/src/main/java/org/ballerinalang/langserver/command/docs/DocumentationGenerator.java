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

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
     * Checks whether the node has documentation.
     *
     * @param node documentatable {@link NonTerminalNode}
     * @return returns True if has documentation False otherwise
     */
    public static boolean hasDocs(NonTerminalNode node) {
        for (Node next : node.children()) {
            if (next.kind() == SyntaxKind.METADATA && ((MetadataNode) next).documentationString().isPresent()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns range of current documentation.
     *
     * @param node documentatable {@link NonTerminalNode}
     * @return returns {@link Range}
     */
    public static Optional<Range> getDocsRange(NonTerminalNode node) {
        for (Node next : node.children()) {
            if (next.kind() == SyntaxKind.METADATA && ((MetadataNode) next).documentationString().isPresent()) {
                return Optional.of(CommonUtil.toRange(next.lineRange()));
            }
        }
        return Optional.empty();
    }

    /**
     * Generate documentation for non-terminal node.
     *
     * @param node non-terminal node
     * @return optional documentation
     */
    public static Optional<DocAttachmentInfo> getDocumentationEditForNode(NonTerminalNode node) {
        switch (node.kind()) {
            case FUNCTION_DEFINITION:
            case OBJECT_METHOD_DEFINITION: {
                return Optional.of(generateFunctionDocumentation((FunctionDefinitionNode) node));
            }
            case METHOD_DECLARATION: {
                return Optional.of(generateMethodDocumentation((MethodDeclarationNode) node));
            }
            case SERVICE_DECLARATION: {
                return Optional.of(generateServiceDocumentation((ServiceDeclarationNode) node));
            }
            case TYPE_DEFINITION: {
                return Optional.of(generateRecordOrObjectDocumentation((TypeDefinitionNode) node));
            }
            case CLASS_DEFINITION: {
                return Optional.of(generateClassDocumentation((ClassDefinitionNode) node));
            }
            default:
                break;
        }
        return Optional.empty();
    }

    public static Optional<Symbol> getDocumentableSymbol(NonTerminalNode node, SemanticModel semanticModel,
                                                         String fileName) {
        switch (node.kind()) {
            case FUNCTION_DEFINITION:
            case OBJECT_METHOD_DEFINITION: {
                FunctionDefinitionNode functionDefNode = (FunctionDefinitionNode) node;
                return semanticModel.symbol(fileName, functionDefNode.functionName().lineRange().startLine());
            }
            case METHOD_DECLARATION: {
                MethodDeclarationNode methodDeclrNode = (MethodDeclarationNode) node;
                return semanticModel.symbol(fileName, methodDeclrNode.methodName().lineRange().startLine());
            }
//            case SERVICE_DECLARATION: {
//                ServiceDeclarationNode serviceDeclrNode = (ServiceDeclarationNode) node;
//                return semanticModel.symbol(fileName, serviceDeclrNode.typeDescriptor().map(s->s.lineRange()
//                .startLine()).);
//            }
            case TYPE_DEFINITION: {
                TypeDefinitionNode typeDefNode = (TypeDefinitionNode) node;
                return semanticModel.symbol(fileName, typeDefNode.typeName().lineRange().startLine());
            }
            case CLASS_DEFINITION: {
                ClassDefinitionNode classDefNode = (ClassDefinitionNode) node;
                return semanticModel.symbol(fileName, classDefNode.className().lineRange().startLine());
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
    private static DocAttachmentInfo generateServiceDocumentation(ServiceDeclarationNode serviceDeclrNode) {
        MetadataNode metadata = serviceDeclrNode.metadata().orElse(null);
        Position docStart = CommonUtil.toRange(serviceDeclrNode.lineRange()).getStart();
        if (metadata != null && !metadata.annotations().isEmpty()) {
            docStart = CommonUtil.toRange(metadata.annotations().get(0).lineRange()).getStart();
        }
        int offset = docStart.getCharacter();
        String desc = String.format("# Description%n%s",
                                    String.join("", Collections.nCopies(offset, " ")));
        return new DocAttachmentInfo(desc, docStart);
    }

    /**
     * Generate documentation for function definition node.
     *
     * @param bLangFunction function definition node
     * @return
     */
    private static DocAttachmentInfo generateFunctionDocumentation(FunctionDefinitionNode bLangFunction) {
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
    private static DocAttachmentInfo generateMethodDocumentation(MethodDeclarationNode methodDeclrNode) {
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
    private static DocAttachmentInfo generateRecordOrObjectDocumentation(TypeDefinitionNode typeDefNode) {
        MetadataNode metadata = typeDefNode.metadata().orElse(null);
        Position docStart = CommonUtil.toRange(typeDefNode.lineRange()).getStart();
        if (metadata != null && !metadata.annotations().isEmpty()) {
            docStart = CommonUtil.toRange(metadata.annotations().get(0).lineRange()).getStart();
        }
        int offset = docStart.getCharacter();
        io.ballerina.compiler.syntax.tree.Node typeDesc = typeDefNode.typeDescriptor();
        String desc = String.format("# Description%n%s",
                                    String.join("", Collections.nCopies(offset, " ")));
        Map<String, String> parameters = new HashMap<>();
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
                    paramName.ifPresent(param -> parameters.put(param.text(), "Parameter Description"));
                });
                break;
            case OBJECT_TYPE_DESC:
                ObjectTypeDescriptorNode objectTypeDescNode = (ObjectTypeDescriptorNode) typeDesc;
                objectTypeDescNode.members().forEach(field -> {
                    if (field.kind() == SyntaxKind.OBJECT_FIELD &&
                            ((ObjectFieldNode) field).visibilityQualifier().isPresent()) {
                        ObjectFieldNode fieldNode = (ObjectFieldNode) field;
                        if (fieldNode.visibilityQualifier().get().kind() == SyntaxKind.PUBLIC_KEYWORD) {
                            parameters.put(fieldNode.fieldName().text(), "Parameter Description");
                        }
                    }
                });
                break;
            default:
                break;
        }
        return new DocAttachmentInfo(desc, parameters, null, docStart);
    }

    /**
     * Generate documentation for class definition node.
     *
     * @param classDefNode class definition node
     * @return
     */
    private static DocAttachmentInfo generateClassDocumentation(ClassDefinitionNode classDefNode) {
        MetadataNode metadata = classDefNode.metadata().orElse(null);
        Position docStart = CommonUtil.toRange(classDefNode.lineRange()).getStart();
        if (metadata != null && !metadata.annotations().isEmpty()) {
            docStart = CommonUtil.toRange(metadata.annotations().get(0).lineRange()).getStart();
        }
        int offset = docStart.getCharacter();
        String desc = String.format("# Description%n%s",
                                    String.join("", Collections.nCopies(offset, " ")));
        Map<String, String> parameters = new HashMap<>();
        classDefNode.members().forEach(field -> {
            if (field.kind() == SyntaxKind.OBJECT_FIELD &&
                    ((ObjectFieldNode) field).visibilityQualifier().isPresent()) {
                ObjectFieldNode fieldNode = (ObjectFieldNode) field;
                if (fieldNode.visibilityQualifier().get().kind() == SyntaxKind.PUBLIC_KEYWORD) {
                    parameters.put(fieldNode.fieldName().text(), "Parameter Description");
                }
            }
        });
        return new DocAttachmentInfo(desc, parameters, null, docStart);
    }


    private static DocAttachmentInfo getFunctionNodeDocumentation(FunctionSignatureNode signatureNode,
                                                                  MetadataNode metadata, Range functionRange) {
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
        String desc = String.format("# Description%n%s",
                                    String.join("", Collections.nCopies(offset, " ")));
        Map<String, String> parameters = new HashMap<>();
        signatureNode.parameters().forEach(param -> {
            Optional<Token> paramName = Optional.empty();
            if (param.kind() == SyntaxKind.REQUIRED_PARAM) {
                paramName = ((RequiredParameterNode) param).paramName();
            } else if (param.kind() == SyntaxKind.DEFAULTABLE_PARAM) {
                paramName = ((DefaultableParameterNode) param).paramName();
            } else if (param.kind() == SyntaxKind.REST_PARAM) {
                paramName = ((RestParameterNode) param).paramName();
            }
            paramName.ifPresent(token -> parameters.put(token.text(), "Parameter Description"));
        });
        String returnDesc = signatureNode.returnTypeDesc().isPresent() ? "Return Value Description" : null;
        if (hasDeprecated) {
//            attributes.add(getDeprecatedDescription(offset));
        }
        return new DocAttachmentInfo(desc, parameters, returnDesc, docStart);
    }
}
