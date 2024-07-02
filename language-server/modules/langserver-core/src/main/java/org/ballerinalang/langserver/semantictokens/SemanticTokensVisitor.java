/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.semantictokens;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationDeclarationNode;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.EnumMemberNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportPrefixNode;
import io.ballerina.compiler.syntax.tree.IncludedRecordParameterNode;
import io.ballerina.compiler.syntax.tree.IntersectionTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.KeySpecifierNode;
import io.ballerina.compiler.syntax.tree.MarkdownParameterDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.RestParameterNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.projects.Document;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.langserver.commons.SemanticTokensContext;
import org.ballerinalang.langserver.commons.SemanticTokensContext.TokenTypeModifiers;
import org.ballerinalang.langserver.commons.SemanticTokensContext.TokenTypes;
import org.eclipse.lsp4j.SemanticTokens;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * Visitor class for semantic tokens.
 *
 * @since 2.0.0
 */
public class SemanticTokensVisitor extends NodeVisitor {

    // Sorted tree of semantic tokens
    private final Set<SemanticToken> semanticTokens;
    private final SemanticTokensContext semanticTokensContext;

    public SemanticTokensVisitor(SemanticTokensContext semanticTokensContext) {
        this.semanticTokens = new TreeSet<>(SemanticToken.semanticTokenComparator);
        this.semanticTokensContext = semanticTokensContext;
    }

    /**
     * Collects semantic tokens while traversing the semantic tress and returns the processed list of semantic tokens
     * for highlighting.
     *
     * @param node Root node
     * @return {@link SemanticTokens}
     */
    public SemanticTokens getSemanticTokens(Node node) {
        List<Integer> data = new ArrayList<>();
        visitSyntaxNode(node);
        SemanticToken previousToken = null;
        for (SemanticToken semanticToken : this.semanticTokens) {
            previousToken = semanticToken.processSemanticToken(data, previousToken);
        }
        return new SemanticTokens(data);
    }

    @Override
    public void visit(ImportDeclarationNode importDeclarationNode) {
        Optional<ImportPrefixNode> importPrefixNode = importDeclarationNode.prefix();
        importPrefixNode.ifPresent(prefixNode -> this.addSemanticToken(prefixNode.prefix(),
                TokenTypes.NAMESPACE.getId(), TokenTypeModifiers.DECLARATION.getId(), true,
                TokenTypes.NAMESPACE.getId(), 0));
    }

    @Override
    public void visit(FunctionDefinitionNode functionDefinitionNode) {
        int type = functionDefinitionNode.kind() == SyntaxKind.OBJECT_METHOD_DEFINITION ? TokenTypes.METHOD.getId() :
                TokenTypes.FUNCTION.getId();
        if (functionDefinitionNode.kind() == SyntaxKind.RESOURCE_ACCESSOR_DEFINITION) {
            this.addSemanticToken(functionDefinitionNode.functionName(), type, TokenTypeModifiers.DECLARATION.getId(),
                    false, -1, -1);
            functionDefinitionNode.relativeResourcePath().stream()
                    .filter(resourcePath -> resourcePath.kind() == SyntaxKind.IDENTIFIER_TOKEN)
                    .forEach(resourcePath ->
                    this.addSemanticToken(resourcePath, type, TokenTypeModifiers.DECLARATION.getId(), false, -1, -1));
        } else {
            this.addSemanticToken(functionDefinitionNode.functionName(), type, TokenTypeModifiers.DECLARATION.getId(),
                    true, type, 0);
        }
        visitSyntaxNode(functionDefinitionNode);
    }

    @Override
    public void visit(MethodDeclarationNode methodDeclarationNode) {
        this.addSemanticToken(methodDeclarationNode.methodName(), TokenTypes.METHOD.getId(),
                TokenTypeModifiers.DECLARATION.getId(), false, -1, -1);
        visitSyntaxNode(methodDeclarationNode);
    }

    @Override
    public void visit(FunctionCallExpressionNode functionCallExpressionNode) {
        Node functionName = functionCallExpressionNode.functionName();
        if (functionName instanceof QualifiedNameReferenceNode qualifiedNameReferenceNode) {
            functionName = qualifiedNameReferenceNode.identifier();
        }
        this.addSemanticToken(functionName, TokenTypes.FUNCTION.getId(), 0, false, -1, -1);
        visitSyntaxNode(functionCallExpressionNode);
    }

    @Override
    public void visit(MethodCallExpressionNode methodCallExpressionNode) {
        this.addSemanticToken(methodCallExpressionNode.methodName(), TokenTypes.METHOD.getId(),
                TokenTypeModifiers.DECLARATION.getId(), false, -1, -1);
        visitSyntaxNode(methodCallExpressionNode);
    }

    @Override
    public void visit(RequiredParameterNode requiredParameterNode) {
        boolean isReadonly = isReadonly(requiredParameterNode.typeName());
        requiredParameterNode.paramName().ifPresent(token -> this.addSemanticToken(token, TokenTypes.PARAMETER.getId(),
                isReadonly ? TokenTypeModifiers.DECLARATION.getId() | TokenTypeModifiers.READONLY.getId() :
                        TokenTypeModifiers.DECLARATION.getId(), true, TokenTypes.PARAMETER.getId(), isReadonly ?
                        TokenTypeModifiers.READONLY.getId() : 0));
        visitSyntaxNode(requiredParameterNode);
    }

    @Override
    public void visit(TypedBindingPatternNode typedBindingPatternNode) {
        TypeDescriptorNode typeDescriptorNode = typedBindingPatternNode.typeDescriptor();
        processSymbols(typeDescriptorNode, typeDescriptorNode.lineRange().startLine());
        visitSyntaxNode(typedBindingPatternNode);
    }

    @Override
    public void visit(CaptureBindingPatternNode captureBindingPatternNode) {
        boolean readonly = false;
        if (captureBindingPatternNode.parent() instanceof TypedBindingPatternNode) {
            readonly = this.isReadonly(((TypedBindingPatternNode) captureBindingPatternNode.parent()).typeDescriptor());
        }
        this.addSemanticToken(captureBindingPatternNode, TokenTypes.VARIABLE.getId(), readonly ?
                TokenTypeModifiers.DECLARATION.getId() | TokenTypeModifiers.READONLY.getId() :
                TokenTypeModifiers.DECLARATION.getId(), true, TokenTypes.VARIABLE.getId(), readonly ?
                TokenTypeModifiers.READONLY.getId() : 0);
        visitSyntaxNode(captureBindingPatternNode);
    }

    @Override
    public void visit(SimpleNameReferenceNode simpleNameReferenceNode) {
        if (!SemanticTokensConstants.SELF.equals(simpleNameReferenceNode.name().text())) {
            processSymbols(simpleNameReferenceNode, simpleNameReferenceNode.lineRange().startLine());
        }
        visitSyntaxNode(simpleNameReferenceNode);
    }

    @Override
    public void visit(QualifiedNameReferenceNode qualifiedNameReferenceNode) {
        this.addSemanticToken(qualifiedNameReferenceNode.modulePrefix(), TokenTypes.NAMESPACE.getId(), 0, false, -1,
                -1);
        Token identifier = qualifiedNameReferenceNode.identifier();
        processSymbols(identifier, identifier.lineRange().startLine());
        visitSyntaxNode(qualifiedNameReferenceNode);
    }

    @Override
    public void visit(ConstantDeclarationNode constantDeclarationNode) {
        this.addSemanticToken(constantDeclarationNode.variableName(), TokenTypes.VARIABLE.getId(),
                TokenTypeModifiers.DECLARATION.getId() | TokenTypeModifiers.READONLY.getId(), true,
                TokenTypes.VARIABLE.getId(), TokenTypeModifiers.READONLY.getId());
        visitSyntaxNode(constantDeclarationNode);
    }

    @Override
    public void visit(ClassDefinitionNode classDefinitionNode) {
        boolean isReadonly = false;
        if (!classDefinitionNode.classTypeQualifiers().isEmpty() &&
                classDefinitionNode.classTypeQualifiers().stream().anyMatch(qualifier ->
                        qualifier.text().equals(SemanticTokensConstants.READONLY))) {
            isReadonly = true;
        }
        this.addSemanticToken(classDefinitionNode.className(), TokenTypes.CLASS.getId(),
                isReadonly ? TokenTypeModifiers.DECLARATION.getId() | TokenTypeModifiers.READONLY.getId() :
                        TokenTypeModifiers.DECLARATION.getId(), true,
                TokenTypes.CLASS.getId(), isReadonly ? TokenTypeModifiers.READONLY.getId() : 0);
        visitSyntaxNode(classDefinitionNode);
    }

    @Override
    public void visit(ServiceDeclarationNode serviceDeclarationNode) {
        serviceDeclarationNode.absoluteResourcePath().forEach(serviceName -> {
            LinePosition startLine = serviceName.lineRange().startLine();
            SemanticToken semanticToken = new SemanticToken(startLine.line(), startLine.offset(),
                    serviceName.textRange().length(), TokenTypes.TYPE.getId(),
                    TokenTypeModifiers.DECLARATION.getId());
            semanticTokens.add(semanticToken);
        });
        visitSyntaxNode(serviceDeclarationNode);
    }

    @Override
    public void visit(EnumDeclarationNode enumDeclarationNode) {
        this.addSemanticToken(enumDeclarationNode.identifier(), TokenTypes.ENUM.getId(),
                TokenTypeModifiers.DECLARATION.getId(), true, TokenTypes.ENUM.getId(), 0);
        visitSyntaxNode(enumDeclarationNode);
    }

    @Override
    public void visit(EnumMemberNode enumMemberNode) {
        this.addSemanticToken(enumMemberNode.identifier(), TokenTypes.ENUM_MEMBER.getId(),
                TokenTypeModifiers.DECLARATION.getId() | TokenTypeModifiers.READONLY.getId(), true,
                TokenTypes.ENUM_MEMBER.getId(), TokenTypeModifiers.READONLY.getId());
        visitSyntaxNode(enumMemberNode);
    }

    @Override
    public void visit(AnnotationNode annotationNode) {
        this.addSemanticToken(annotationNode.atToken(), TokenTypes.NAMESPACE.getId(), 0, false, -1, -1);
        visitSyntaxNode(annotationNode);
    }

    @Override
    public void visit(MarkdownParameterDocumentationLineNode markdownParameterDocumentationLineNode) {
        //TODO: handle readonly modifier for document parameter
        if (!markdownParameterDocumentationLineNode.parameterName().text().equals(SemanticTokensConstants.RETURN)) {
            int type;
            switch (markdownParameterDocumentationLineNode.parent().parent().parent().kind()) {
                case RECORD_FIELD:
                case OBJECT_FIELD:
                    type = TokenTypes.PROPERTY.getId();
                    break;
                case TYPE_DEFINITION:
                    Node node = markdownParameterDocumentationLineNode.parent().parent().parent();
                    type = TokenTypes.TYPE_PARAMETER.getId();
                    if (node instanceof TypeDefinitionNode typeDefinitionNode) {
                        SyntaxKind kind = typeDefinitionNode.typeDescriptor().kind();
                        if (kind == SyntaxKind.OBJECT_TYPE_DESC || kind == SyntaxKind.RECORD_TYPE_DESC) {
                            type = TokenTypes.PROPERTY.getId();
                        }
                    }
                    break;
                default:
                    type = TokenTypes.PARAMETER.getId();
                    break;
            }
            this.addSemanticToken(markdownParameterDocumentationLineNode.parameterName(), type,
                    TokenTypeModifiers.DOCUMENTATION.getId(), false, -1, -1);
        }
        visitSyntaxNode(markdownParameterDocumentationLineNode);
    }

    @Override
    public void visit(TypeDefinitionNode typeDefinitionNode) {
        int type = TokenTypes.TYPE.getId();
        int modifiers = 0;
        int refModifiers = 0;
        Node typeDescriptor = typeDefinitionNode.typeDescriptor();
        switch (typeDescriptor.kind()) {
            case OBJECT_TYPE_DESC:
                type = TokenTypes.INTERFACE.getId();
                modifiers = TokenTypeModifiers.DECLARATION.getId();
                break;
            case RECORD_TYPE_DESC:
                type = TokenTypes.STRUCT.getId();
                modifiers = TokenTypeModifiers.DECLARATION.getId();
                break;
            case INTERSECTION_TYPE_DESC:
                if (typeDescriptor instanceof IntersectionTypeDescriptorNode intSecDescriptor) {
                    SyntaxKind left = intSecDescriptor.leftTypeDesc().kind();
                    SyntaxKind right = intSecDescriptor.rightTypeDesc().kind();
                    if (left == SyntaxKind.RECORD_TYPE_DESC || right == SyntaxKind.RECORD_TYPE_DESC) {
                        type = TokenTypes.STRUCT.getId();
                    }
                    if (left == SyntaxKind.READONLY_TYPE_DESC || right == SyntaxKind.READONLY_TYPE_DESC) {
                        modifiers = TokenTypeModifiers.DECLARATION.getId() | TokenTypeModifiers.READONLY.getId();
                        refModifiers = TokenTypeModifiers.READONLY.getId();
                    } else {
                        modifiers = TokenTypeModifiers.DECLARATION.getId();
                    }
                }
                break;
            default:
                type = TokenTypes.TYPE.getId();
                modifiers = TokenTypeModifiers.DECLARATION.getId();
                break;
        }
        this.addSemanticToken(typeDefinitionNode.typeName(), type, modifiers, true, type, refModifiers);
        visitSyntaxNode(typeDefinitionNode);
    }

    @Override
    public void visit(RecordFieldNode recordFieldNode) {
        Token token = recordFieldNode.fieldName();
        LinePosition startLine = token.lineRange().startLine();
        SemanticToken semanticToken = new SemanticToken(startLine.line(), startLine.offset());
        if (!semanticTokens.contains(semanticToken)) {
            int length = token.text().trim().length();
            int modifiers;
            int refModifiers;
            if (recordFieldNode.readonlyKeyword().isPresent()) {
                modifiers = TokenTypeModifiers.DECLARATION.getId() | TokenTypeModifiers.READONLY.getId();
                refModifiers = TokenTypeModifiers.READONLY.getId();
            } else {
                modifiers = TokenTypeModifiers.DECLARATION.getId();
                refModifiers = 0;
            }
            semanticToken.setProperties(length, TokenTypes.PROPERTY.getId(), modifiers);
            semanticTokens.add(semanticToken);
            handleReferences(startLine, length, TokenTypes.PROPERTY.getId(), refModifiers);
        }
        visitSyntaxNode(recordFieldNode);
    }

    @Override
    public void visit(RecordFieldWithDefaultValueNode recordFieldWithDefaultValueNode) {
        Token token = recordFieldWithDefaultValueNode.fieldName();
        LinePosition startLine = token.lineRange().startLine();
        SemanticToken semanticToken = new SemanticToken(startLine.line(), startLine.offset());
        if (!semanticTokens.contains(semanticToken)) {
            int length = token.text().trim().length();
            int modifiers;
            int refModifiers;
            if (recordFieldWithDefaultValueNode.readonlyKeyword().isPresent()) {
                modifiers = TokenTypeModifiers.DECLARATION.getId() | TokenTypeModifiers.READONLY.getId();
                refModifiers = TokenTypeModifiers.READONLY.getId();
            } else {
                modifiers = TokenTypeModifiers.DECLARATION.getId();
                refModifiers = 0;
            }
            semanticToken.setProperties(length, TokenTypes.PROPERTY.getId(), modifiers);
            semanticTokens.add(semanticToken);
            handleReferences(startLine, length, TokenTypes.PROPERTY.getId(), refModifiers);
        }
        visitSyntaxNode(recordFieldWithDefaultValueNode);
    }

    @Override
    public void visit(KeySpecifierNode keySpecifierNode) {
        keySpecifierNode.fieldNames().forEach(field -> this.addSemanticToken(field, TokenTypes.PROPERTY.getId(),
                TokenTypeModifiers.DECLARATION.getId(), false, -1, -1));
        visitSyntaxNode(keySpecifierNode);
    }

    @Override
    public void visit(SpecificFieldNode specificFieldNode) {
        processSymbols(specificFieldNode.fieldName(),
                specificFieldNode.fieldName().location().lineRange().startLine());
        visitSyntaxNode(specificFieldNode);
    }

    @Override
    public void visit(ObjectFieldNode objectFieldNode) {
        SyntaxKind kind = objectFieldNode.parent().kind();
        int type = kind == SyntaxKind.CLASS_DEFINITION || kind == SyntaxKind.OBJECT_TYPE_DESC ||
                kind == SyntaxKind.RECORD_TYPE_DESC || kind == SyntaxKind.OBJECT_CONSTRUCTOR ?
                TokenTypes.PROPERTY.getId() :
                kind == SyntaxKind.SERVICE_DECLARATION ? TokenTypes.VARIABLE.getId() :
                        TokenTypes.TYPE_PARAMETER.getId();
        boolean isReadOnly = isReadonly(objectFieldNode.typeName());
        this.addSemanticToken(objectFieldNode.fieldName(), type, isReadOnly ? TokenTypeModifiers.DECLARATION.getId() |
                        TokenTypeModifiers.READONLY.getId() : TokenTypeModifiers.DECLARATION.getId(), true, type,
                isReadOnly ? TokenTypeModifiers.READONLY.getId() : 0);
        visitSyntaxNode(objectFieldNode);
    }

    @Override
    public void visit(AnnotationDeclarationNode annotationDeclarationNode) {
        this.addSemanticToken(annotationDeclarationNode.annotationTag(), TokenTypes.TYPE.getId(),
                TokenTypeModifiers.DECLARATION.getId(), true, TokenTypes.TYPE.getId(), 0);
        visitSyntaxNode(annotationDeclarationNode);
    }

    @Override
    public void visit(DefaultableParameterNode defaultableParameterNode) {
        defaultableParameterNode.paramName().ifPresent(token -> this.addSemanticToken(token,
                TokenTypes.PARAMETER.getId(), TokenTypeModifiers.DECLARATION.getId(), true,
                TokenTypes.PARAMETER.getId(), 0));
        visitSyntaxNode(defaultableParameterNode);
    }

    @Override
    public void visit(IncludedRecordParameterNode includedRecordParameterNode) {
        includedRecordParameterNode.paramName().ifPresent(token -> this.addSemanticToken(token,
                TokenTypes.PARAMETER.getId(), TokenTypeModifiers.DECLARATION.getId(), true,
                TokenTypes.PARAMETER.getId(), 0));
        visitSyntaxNode(includedRecordParameterNode);
    }

    @Override
    public void visit(RestParameterNode restParameterNode) {
        restParameterNode.paramName().ifPresent(token -> this.addSemanticToken(token, TokenTypes.PARAMETER.getId(),
                TokenTypeModifiers.DECLARATION.getId(), true, TokenTypes.PARAMETER.getId(), 0));
        visitSyntaxNode(restParameterNode);
    }

    @Override
    public void visit(NamedArgumentNode namedArgumentNode) {
        this.addSemanticToken(namedArgumentNode.argumentName(), TokenTypes.VARIABLE.getId(),
                TokenTypeModifiers.DECLARATION.getId(), false, -1, -1);
        visitSyntaxNode(namedArgumentNode);
    }

    /**
     * Returns if the given IntersectionTypeDescriptorNode has a readonly typeDescriptor.
     *
     * @param node Current node
     * @return True if a readonly typeDescriptor is present, false otherwise.
     */
    private boolean isReadonly(Node node) {
        if (node instanceof IntersectionTypeDescriptorNode intSecDescriptor) {
            SyntaxKind left = intSecDescriptor.leftTypeDesc().kind();
            SyntaxKind right = intSecDescriptor.rightTypeDesc().kind();
            return left == SyntaxKind.READONLY_TYPE_DESC || right == SyntaxKind.READONLY_TYPE_DESC;
        }
        return false;
    }

    /**
     * Get the symbol of the given node and process the semantic tokens for the symbol and it's references.
     *
     * @param node      Current node
     * @param startLine Start line position
     */
    private void processSymbols(Node node, LinePosition startLine) {
        if (node.kind() == SyntaxKind.STRING_LITERAL ||
                semanticTokens.contains(new SemanticToken(startLine.line(), startLine.offset()))) {
            return;
        }
        Optional<SemanticModel> semanticModel = this.semanticTokensContext.currentSemanticModel();
        if (semanticModel.isEmpty()) {
            return;
        }

        Optional<Symbol> symbol = semanticModel.get().symbol(node);
        if (symbol.isEmpty() || symbol.get().getLocation().isEmpty()) {
            return;
        }
        LineRange symbolLineRange = symbol.get().getLocation().get().lineRange();
        LinePosition linePosition = symbolLineRange.startLine();
        SymbolKind kind = symbol.get().kind();
        String nodeName = node.toString().trim();
        if (nodeName.equals(SemanticTokensConstants.SELF)) {
            return;
        }

        int declarationType = -1, declarationModifiers = -1, referenceType = -1, referenceModifiers = -1;
        switch (kind) {
            case CLASS:
                declarationType = TokenTypes.CLASS.getId();
                declarationModifiers = TokenTypeModifiers.DECLARATION.getId();
                referenceType = TokenTypes.CLASS.getId();
                break;
            case CLASS_FIELD:
                declarationType = TokenTypes.PROPERTY.getId();
                declarationModifiers = TokenTypeModifiers.DECLARATION.getId();
                referenceType = TokenTypes.PROPERTY.getId();
                break;
            case CONSTANT:
                declarationType = TokenTypes.VARIABLE.getId();
                declarationModifiers = TokenTypeModifiers.DECLARATION.getId() |
                        TokenTypeModifiers.READONLY.getId();
                referenceType = TokenTypes.VARIABLE.getId();
                referenceModifiers = TokenTypeModifiers.READONLY.getId();
                break;
            case VARIABLE:
                boolean isReadonly = ((VariableSymbol) symbol.get()).typeDescriptor().typeKind() ==
                        TypeDescKind.INTERSECTION && ((IntersectionTypeSymbol) ((VariableSymbol) symbol.get())
                        .typeDescriptor()).memberTypeDescriptors().stream()
                        .anyMatch(desc -> desc.typeKind() == TypeDescKind.READONLY);
                declarationType = TokenTypes.VARIABLE.getId();
                declarationModifiers = isReadonly ? TokenTypeModifiers.DECLARATION.getId() |
                        TokenTypeModifiers.READONLY.getId() : TokenTypeModifiers.DECLARATION.getId();
                referenceType = TokenTypes.VARIABLE.getId();
                referenceModifiers = isReadonly ? TokenTypeModifiers.READONLY.getId() : 0;
                break;
            case TYPE:
                if (symbol.get() instanceof TypeReferenceTypeSymbol) {
                    TypeSymbol typeDescriptor = ((TypeReferenceTypeSymbol) symbol.get()).typeDescriptor();
                    int type = TokenTypes.TYPE.getId();
                    switch (typeDescriptor.kind()) {
                        case CLASS:
                            type = TokenTypes.CLASS.getId();
                            if (typeDescriptor instanceof ClassSymbol &&
                                    ((ClassSymbol) typeDescriptor).qualifiers().contains(Qualifier.READONLY)) {
                                declarationModifiers = TokenTypeModifiers.DECLARATION.getId() |
                                        TokenTypeModifiers.READONLY.getId();
                                referenceModifiers = TokenTypeModifiers.READONLY.getId();
                            } else {
                                declarationModifiers = TokenTypeModifiers.DECLARATION.getId();
                            }
                            break;
                        case TYPE:
                            switch (typeDescriptor.typeKind()) {
                                case RECORD:
                                    type = TokenTypes.STRUCT.getId();
                                    declarationModifiers = TokenTypeModifiers.DECLARATION.getId();
                                    break;
                                case OBJECT:
                                    type = TokenTypes.INTERFACE.getId();
                                    break;
                                case INTERSECTION:
                                    IntersectionTypeSymbol intSecSymbol =
                                            (IntersectionTypeSymbol) typeDescriptor;
                                    if (intSecSymbol.effectiveTypeDescriptor().typeKind() ==
                                            TypeDescKind.RECORD) {
                                        type = TokenTypes.STRUCT.getId();
                                        if (intSecSymbol.memberTypeDescriptors().stream().anyMatch(desc ->
                                                desc.typeKind() == TypeDescKind.READONLY)) {
                                            declarationModifiers = TokenTypeModifiers.DECLARATION.getId() |
                                                    TokenTypeModifiers.READONLY.getId();
                                            referenceModifiers = TokenTypeModifiers.READONLY.getId();
                                        } else {
                                            declarationModifiers = TokenTypeModifiers.DECLARATION.getId();
                                        }
                                    }
                                    break;
                                case UNION:
                                    if (((TypeReferenceTypeSymbol) symbol.get()).definition().kind() ==
                                            SymbolKind.ENUM) {
                                        type = TokenTypes.ENUM.getId();
                                        declarationModifiers = TokenTypeModifiers.DECLARATION.getId();
                                    }
                                    break;
                                default:
                                    type = TokenTypes.TYPE.getId();
                                    break;
                            }
                            break;
                        default:
                            type = TokenTypes.TYPE.getId();
                            break;
                    }
                    declarationType = type;
                    referenceType = type;
                } else {
                    declarationType = TokenTypes.TYPE.getId();
                    referenceType = TokenTypes.TYPE.getId();
                    declarationModifiers = TokenTypeModifiers.DECLARATION.getId();
                }
                break;
            case RECORD_FIELD:
                declarationType = TokenTypes.PROPERTY.getId();
                referenceType = TokenTypes.PROPERTY.getId();
                if (symbol.get() instanceof RecordFieldSymbol &&
                        ((RecordFieldSymbol) symbol.get()).qualifiers().contains(Qualifier.READONLY)) {
                    declarationModifiers = TokenTypeModifiers.DECLARATION.getId() |
                            TokenTypeModifiers.READONLY.getId();
                    referenceModifiers = TokenTypeModifiers.READONLY.getId();
                } else {
                    declarationModifiers = TokenTypeModifiers.DECLARATION.getId();
                }
                break;
            case ENUM_MEMBER:
                declarationType = TokenTypes.ENUM_MEMBER.getId();
                declarationModifiers = TokenTypeModifiers.DECLARATION.getId() |
                        TokenTypeModifiers.READONLY.getId();
                referenceType = TokenTypes.ENUM_MEMBER.getId();
                referenceModifiers = TokenTypeModifiers.READONLY.getId();
                break;
            case FUNCTION:
                declarationType = TokenTypes.FUNCTION.getId();
                declarationModifiers = TokenTypeModifiers.DECLARATION.getId();
                referenceType = TokenTypes.FUNCTION.getId();
                break;
            case METHOD:
                declarationType = TokenTypes.METHOD.getId();
                declarationModifiers = TokenTypeModifiers.DECLARATION.getId();
                referenceType = TokenTypes.METHOD.getId();
                break;
            case ANNOTATION:
                this.addSemanticToken(node, TokenTypes.TYPE.getId(), 0, false, -1, -1);
                break;
            default:
                break;
        }

        int length = node.textRange().length();
        if (declarationType != -1) {
            Optional<ModuleSymbol> moduleSymbol = symbol.get().getModule();
            // Add the symbol's semantic token if it is in the same file
            if (symbolLineRange.fileName().equals(this.semanticTokensContext.currentDocument().get().name()) &&
                    moduleSymbol.isPresent() && moduleSymbol.get().getName().isPresent() &&
                    this.semanticTokensContext.currentModule().isPresent() && moduleSymbol.get().getName().get()
                    .equals(this.semanticTokensContext.currentModule().get().moduleId().moduleName())) {
                SemanticToken semanticToken = new SemanticToken(linePosition.line(), linePosition.offset());
                if (!semanticTokens.contains(semanticToken)) {
                    semanticToken.setProperties(length, declarationType, declarationModifiers == -1 ?
                            0 : declarationModifiers);
                    semanticTokens.add(semanticToken);
                }
            }
        }

        // Add symbol's references if they reside in the same file
        if (referenceType != -1) {
            final int type = referenceType;
            final int modifiers = referenceModifiers == -1 ? 0 : referenceModifiers;

            List<Location> locations = semanticModel.get().references(symbol.get(),
                    this.semanticTokensContext.currentDocument().get(), false);
            locations.stream().filter(location -> location.lineRange().fileName()
                    .equals(this.semanticTokensContext.currentDocument().get().name())).forEach(location -> {
                        LinePosition position = location.lineRange().startLine();
                        SemanticToken semanticToken = new SemanticToken(position.line(), position.offset());
                        if (!semanticTokens.contains(semanticToken) && location.textRange().length() == length) {
                            semanticToken.setProperties(length, type, modifiers);
                            semanticTokens.add(semanticToken);
                        }
                    });
        }
    }

    /**
     * Adds a semantic token instance into the semanticTokens set for the given node.
     *
     * @param node              Current node
     * @param type              Semantic token type's index
     * @param modifiers         Semantic token type modifiers' index
     * @param processReferences True if node references should be processed, false otherwise
     * @param refType           Reference's semantic token type's index
     * @param refModifiers      Reference's semantic token type modifiers' index
     */
    private void addSemanticToken(Node node, int type, int modifiers, boolean processReferences, int refType,
                                  int refModifiers) {
        LinePosition startLine = node.lineRange().startLine();
        SemanticToken semanticToken = new SemanticToken(startLine.line(), startLine.offset());
        if (!semanticTokens.contains(semanticToken)) {
            int length = node instanceof Token ? ((Token) node).text().trim().length() : node.textRange().length();
            semanticToken.setProperties(length, type, modifiers);
            semanticTokens.add(semanticToken);
            if (processReferences) {
                handleReferences(startLine, length, refType, refModifiers);
            }
        }
    }

    /**
     * Handles references of the node that is located in the given position.
     *
     * @param linePosition Start position of the node
     * @param length       Length to highlight
     * @param type         Semantic token type's index
     * @param modifiers    Semantic token type modifiers' index
     */
    private void handleReferences(LinePosition linePosition, int length, int type, int modifiers) {
        Optional<SemanticModel> semanticModel = this.semanticTokensContext.currentSemanticModel();
        if (semanticModel.isEmpty()) {
            return;
        }

        Document document = this.semanticTokensContext.currentDocument().get();
        List<Location> locations = semanticModel.get().references(document, document, linePosition,
                false);
        locations.stream().filter(location ->
                location.lineRange().fileName().equals(document.name())).forEach(location -> {
            LinePosition position = location.lineRange().startLine();
            SemanticToken semanticToken = new SemanticToken(position.line(), position.offset());
            if (!semanticTokens.contains(semanticToken) && location.textRange().length() == length) {
                semanticToken.setProperties(length, type, modifiers);
                semanticTokens.add(semanticToken);
            }
        });
    }

    /**
     * Represents semantic token data for a node.
     */
    static class SemanticToken implements Comparable<SemanticToken> {

        private final int line;
        private final int column;
        private int length;
        private int type;
        private int modifiers;

        private SemanticToken(int line, int column) {
            this.line = line;
            this.column = column;
        }

        private SemanticToken(int line, int column, int length, int type, int modifiers) {
            this.line = line;
            this.column = column;
            this.length = length;
            this.type = type;
            this.modifiers = modifiers;
        }

        private int getLine() {
            return line;
        }

        private int getColumn() {
            return column;
        }

        private int getLength() {
            return length;
        }

        private int getType() {
            return type;
        }

        private int getModifiers() {
            return modifiers;
        }

        public void setProperties(int length, int type, int modifiers) {
            this.length = length;
            this.type = type;
            this.modifiers = modifiers;
        }

        public SemanticToken processSemanticToken(List<Integer> data, SemanticToken previousToken) {
            int line = this.getLine();
            int column = this.getColumn();
            int prevTokenLine = line;
            int prevTokenColumn = column;

            if (previousToken != null) {
                if (line == previousToken.getLine()) {
                    column -= previousToken.getColumn();
                }
                line -= previousToken.getLine();
            }
            data.add(line);
            data.add(column);
            data.add(this.getLength());
            data.add(this.getType());
            data.add(this.getModifiers());
            return new SemanticToken(prevTokenLine, prevTokenColumn);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            SemanticToken semanticToken = (SemanticToken) obj;
            return line == semanticToken.line && column == semanticToken.column;
        }

        @Override
        public int hashCode() {
            return Objects.hash(line, column);
        }

        @Override
        public int compareTo(SemanticToken semanticToken) {
            if (this.line == semanticToken.line) {
                return this.column - semanticToken.column;
            }
            return this.line - semanticToken.line;
        }

        public static Comparator<SemanticToken> semanticTokenComparator = SemanticToken::compareTo;
    }
}
