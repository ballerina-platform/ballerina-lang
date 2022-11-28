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
import io.ballerina.compiler.syntax.tree.AnnotationDeclarationNode;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.compiler.syntax.tree.RestParameterNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.TextDocument;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.util.LinkedHashMap;
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
                return Optional.of(PositionUtil.toRange(((MetadataNode) next).documentationString().get().lineRange()));
            }
        }
        return Optional.empty();
    }

    /**
     * Generate documentation for non-terminal node.
     *
     * @param node       non-terminal node
     * @param syntaxTree syntaxTree {@link SyntaxTree}
     * @return optional documentation
     */
    public static Optional<DocAttachmentInfo> getDocumentationEditForNode(NonTerminalNode node,
                                                                          SyntaxTree syntaxTree) {
        switch (node.kind()) {
            case FUNCTION_DEFINITION:
            case RESOURCE_ACCESSOR_DEFINITION:
            case OBJECT_METHOD_DEFINITION: {
                return Optional.of(generateFunctionDocumentation((FunctionDefinitionNode) node, syntaxTree));
            }
            case METHOD_DECLARATION: {
                return Optional.of(generateMethodDocumentation((MethodDeclarationNode) node, syntaxTree));
            }
            case SERVICE_DECLARATION: {
                return Optional.of(generateServiceDocumentation((ServiceDeclarationNode) node, syntaxTree));
            }
            case TYPE_DEFINITION: {
                return Optional.of(generateRecordOrObjectDocumentation((TypeDefinitionNode) node, syntaxTree));
            }
            case CLASS_DEFINITION: {
                return Optional.of(generateClassDocumentation((ClassDefinitionNode) node, syntaxTree));
            }
            case CONST_DECLARATION: {
                return Optional.of(generateModuleMemberDocumentation((ConstantDeclarationNode) node, syntaxTree));
            }
            case ENUM_DECLARATION: {
                return Optional.of(generateModuleMemberDocumentation((EnumDeclarationNode) node, syntaxTree));
            }
            case MODULE_VAR_DECL: {
                return Optional.of(generateModuleMemberDocumentation((ModuleVariableDeclarationNode) node, syntaxTree));
            }
            case ANNOTATION_DECLARATION: {
                return Optional.of(generateAnnotationDocumentation((AnnotationDeclarationNode) node, syntaxTree));
            }
            default:
                break;
        }
        return Optional.empty();
    }

    public static Optional<Symbol> getDocumentableSymbol(NonTerminalNode node, SemanticModel semanticModel) {
        switch (node.kind()) {
            case FUNCTION_DEFINITION:
            case OBJECT_METHOD_DEFINITION:
            case RESOURCE_ACCESSOR_DEFINITION:
            case METHOD_DECLARATION:
            case SERVICE_DECLARATION:    
//            case SERVICE_DECLARATION: {
//                ServiceDeclarationNode serviceDeclrNode = (ServiceDeclarationNode) node;
//                return semanticModel.symbol(fileName, serviceDeclrNode.typeDescriptor().map(s->s.lineRange()
//                .startLine()).);
//            }
            case TYPE_DEFINITION:
            case ANNOTATION_DECLARATION:
            case CLASS_DEFINITION:
                return semanticModel.symbol(node);
            default:
                break;
        }
        return Optional.empty();
    }

    /**
     * Generate documentation for service node.
     *
     * @param serviceDeclrNode service declaration node
     * @param syntaxTree       syntaxTree {@link SyntaxTree}
     * @return generated doc attachment
     */
    private static DocAttachmentInfo generateServiceDocumentation(ServiceDeclarationNode serviceDeclrNode,
                                                                  SyntaxTree syntaxTree) {
        MetadataNode metadata = serviceDeclrNode.metadata().orElse(null);
        Position docStart = PositionUtil.toRange(serviceDeclrNode.lineRange()).getStart();
        if (metadata != null && !metadata.annotations().isEmpty()) {
            docStart = PositionUtil.toRange(metadata.annotations().get(0).lineRange()).getStart();
        }
        String desc = String.format("Description%n");
        return new DocAttachmentInfo(desc, docStart, getPadding(serviceDeclrNode, syntaxTree));
    }

    /**
     * Generate documentation for module member declaration nodes.
     *
     * @param declarationNode declaration node
     * @param syntaxTree       syntaxTree {@link SyntaxTree}
     * @return generated doc attachment
     */
    private static DocAttachmentInfo generateModuleMemberDocumentation(ModuleMemberDeclarationNode declarationNode,
                                                                       SyntaxTree syntaxTree) {
        Position docStart = PositionUtil.toRange(declarationNode.lineRange()).getStart();
        String desc = String.format("Description%n");
        return new DocAttachmentInfo(desc, docStart, getPadding(declarationNode, syntaxTree));
    }

    /**
     * Generate documentation for function definition node.
     *
     * @param bLangFunction function definition node
     * @param syntaxTree    syntaxTree {@link SyntaxTree}
     * @return generated doc attachment
     */
    private static DocAttachmentInfo generateFunctionDocumentation(FunctionDefinitionNode bLangFunction,
                                                                   SyntaxTree syntaxTree) {
        return getFunctionNodeDocumentation(bLangFunction.functionSignature(),
                                            bLangFunction.relativeResourcePath(),
                                            bLangFunction.metadata().orElse(null),
                                            PositionUtil.toRange(bLangFunction.lineRange()),
                                            syntaxTree);
    }

    /**
     * Generate documentation for method declaration node.
     *
     * @param methodDeclrNode method declaration node
     * @param syntaxTree      syntaxTree {@link SyntaxTree}
     * @return generated doc attachment
     */
    private static DocAttachmentInfo generateMethodDocumentation(MethodDeclarationNode methodDeclrNode,
                                                                 SyntaxTree syntaxTree) {
        return getFunctionNodeDocumentation(methodDeclrNode.methodSignature(),
                                            methodDeclrNode.relativeResourcePath(),
                                            methodDeclrNode.metadata().orElse(null),
                                            PositionUtil.toRange(methodDeclrNode.lineRange()),
                                            syntaxTree);
    }

    /**
     * Generate documentation for record or object type definition node.
     *
     * @param typeDefNode type definition node
     * @param syntaxTree  syntaxTree {@link SyntaxTree}
     * @return generated doc attachment
     */
    private static DocAttachmentInfo generateRecordOrObjectDocumentation(TypeDefinitionNode typeDefNode,
                                                                         SyntaxTree syntaxTree) {
        MetadataNode metadata = typeDefNode.metadata().orElse(null);
        Position docStart = PositionUtil.toRange(typeDefNode.lineRange()).getStart();
        if (metadata != null && !metadata.annotations().isEmpty()) {
            docStart = PositionUtil.toRange(metadata.annotations().get(0).lineRange()).getStart();
        }
        io.ballerina.compiler.syntax.tree.Node typeDesc = typeDefNode.typeDescriptor();
        String desc = String.format("Description%n");
        LinkedHashMap<String, String> parameters = new LinkedHashMap<>();
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
                    paramName.ifPresent(param -> parameters.put(param.text(), "Field Description"));
                });
                break;
            case OBJECT_TYPE_DESC:
                ObjectTypeDescriptorNode objectTypeDescNode = (ObjectTypeDescriptorNode) typeDesc;
                objectTypeDescNode.members().forEach(field -> {
                    if (field.kind() == SyntaxKind.OBJECT_FIELD &&
                            ((ObjectFieldNode) field).visibilityQualifier().isPresent()) {
                        ObjectFieldNode fieldNode = (ObjectFieldNode) field;
                        if (fieldNode.visibilityQualifier().get().kind() == SyntaxKind.PUBLIC_KEYWORD) {
                            parameters.put(fieldNode.fieldName().text(), "Field Description");
                        }
                    }
                });
                break;
            default:
                break;
        }
        return new DocAttachmentInfo(desc, parameters, null, null, docStart, getPadding(typeDefNode, syntaxTree));
    }

    /**
     * Generate documentation for class definition node.
     *
     * @param classDefNode class definition node
     * @param syntaxTree   {@link SyntaxTree}
     * @return generated doc attachment
     */
    private static DocAttachmentInfo generateClassDocumentation(ClassDefinitionNode classDefNode,
                                                                SyntaxTree syntaxTree) {
        MetadataNode metadata = classDefNode.metadata().orElse(null);
        Position docStart = PositionUtil.toRange(classDefNode.lineRange()).getStart();
        if (metadata != null && !metadata.annotations().isEmpty()) {
            docStart = PositionUtil.toRange(metadata.annotations().get(0).lineRange()).getStart();
        }
        String desc = String.format("Description%n");
        LinkedHashMap<String, String> parameters = new LinkedHashMap<>();
        classDefNode.members().forEach(field -> {
            if (field.kind() == SyntaxKind.OBJECT_FIELD &&
                    ((ObjectFieldNode) field).visibilityQualifier().isPresent()) {
                ObjectFieldNode fieldNode = (ObjectFieldNode) field;
                if (fieldNode.visibilityQualifier().get().kind() == SyntaxKind.PUBLIC_KEYWORD) {
                    parameters.put(fieldNode.fieldName().text(), "Parameter Description");
                }
            }
        });
        return new DocAttachmentInfo(desc, parameters, null, null, docStart, getPadding(classDefNode, syntaxTree));
    }


    private static DocAttachmentInfo getFunctionNodeDocumentation(FunctionSignatureNode signatureNode,
                                                                  NodeList<Node> resourceNodes,
                                                                  MetadataNode metadata, Range functionRange,
                                                                  SyntaxTree syntaxTree) {
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
            docStart = PositionUtil.toRange(metadata.annotations().get(0).lineRange()).getStart();
        }
        String desc = String.format("Description%n");
        LinkedHashMap<String, String> parameters = new LinkedHashMap<>();
        // Resource function path parameters
        if (!resourceNodes.isEmpty()) {
            resourceNodes.forEach(param-> {
                if (param instanceof ResourcePathParameterNode) {
                    Optional<Token> paramName = Optional.empty();
                    if (param.kind() == SyntaxKind.RESOURCE_PATH_SEGMENT_PARAM
                            || param.kind() == SyntaxKind.RESOURCE_PATH_REST_PARAM) {
                        paramName = Optional.ofNullable(((ResourcePathParameterNode) param).paramName().orElse(null));
                    }
                    paramName.ifPresent(token -> parameters.put(token.text(), "Parameter Description"));
                } 
            });
        }
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

        String deprecatedDesc = null;
        if (hasDeprecated) {
            deprecatedDesc = "Deprecated Description";
        }
        //TODO: Handle deprecated parameters
        return new DocAttachmentInfo(desc, parameters, returnDesc, deprecatedDesc, docStart,
                                     getPadding(signatureNode.parent(), syntaxTree));
    }
    
    /**
     * Generate documentation for annotation declaration node.
     *
     * @param annotationDeclarationNode    service declaration node
     * @param syntaxTree                   syntaxTree {@link SyntaxTree}
     * @return generated doc attachment
     */
    private static DocAttachmentInfo generateAnnotationDocumentation(
            AnnotationDeclarationNode annotationDeclarationNode, SyntaxTree syntaxTree) {
        MetadataNode metadata = annotationDeclarationNode.metadata().orElse(null);
        Position docStart = PositionUtil.toRange(annotationDeclarationNode.lineRange()).getStart();
        if (metadata != null && !metadata.annotations().isEmpty()) {
            docStart = PositionUtil.toRange(metadata.annotations().get(0).lineRange()).getStart();
        }
        String desc = String.format("Description%n");
        return new DocAttachmentInfo(desc, docStart, getPadding(annotationDeclarationNode, syntaxTree));
    }

    private static String getPadding(NonTerminalNode bLangFunction, SyntaxTree syntaxTree) {
        LinePosition position = bLangFunction.location().lineRange().startLine();
        TextDocument textDocument = syntaxTree.textDocument();
        int prevCharIndex = textDocument.textPositionFrom(LinePosition.from(position.line(), position.offset()));
        int lineStartIndex = textDocument.textPositionFrom(LinePosition.from(position.line(), 0));
        String sourceCode = syntaxTree.toSourceCode();
        String padding = sourceCode.substring(lineStartIndex, prevCharIndex);
        return padding.isBlank() ? padding : " ";
    }
}
