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
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.AnnotationDeclarationNode;
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
import io.ballerina.compiler.syntax.tree.MarkdownParameterDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.RestParameterNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.projects.Document;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.langserver.commons.SemanticTokensContext;
import org.ballerinalang.langserver.commons.SemanticTokensContext.TokenTypeModifiers;
import org.ballerinalang.langserver.commons.SemanticTokensContext.TokenTypes;
import org.eclipse.lsp4j.SemanticTokens;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * Visitor class for semantic tokens.
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
    public SemanticTokens visitSemanticTokens(Node node) {
        List<Integer> data = new ArrayList<>();
        visitSyntaxNode(node);

        SemanticToken previousToken = null;
        for (SemanticToken semanticToken : this.semanticTokens) {
            previousToken = semanticToken.processSemanticToken(data, previousToken);
        }
        return new SemanticTokens(data);
    }

    public void visit(ImportDeclarationNode importDeclarationNode) {
        Optional<ImportPrefixNode> importPrefixNode = importDeclarationNode.prefix();
        importPrefixNode.ifPresent(prefixNode -> this.addSemanticToken(prefixNode.prefix(),
                TokenTypes.NAMESPACE.getId(), TokenTypeModifiers.DECLARATION.getId(), true,
                TokenTypes.NAMESPACE.getId(), 0));
        visitSyntaxNode(importDeclarationNode);
    }

    public void visit(FunctionDefinitionNode functionDefinitionNode) {
        LinePosition startLine = functionDefinitionNode.functionName().lineRange().startLine();
        SemanticToken semanticToken = new SemanticToken(startLine.line(), startLine.offset());
        if (!semanticTokens.contains(semanticToken)) {
            int length = functionDefinitionNode.functionName().text().length();
            int type = functionDefinitionNode.kind() == SyntaxKind.OBJECT_METHOD_DEFINITION ?
                    TokenTypes.METHOD.getId() : TokenTypes.FUNCTION.getId();
            semanticToken.setProperties(length, type, TokenTypeModifiers.DECLARATION.getId());
            semanticTokens.add(semanticToken);

            if (functionDefinitionNode.kind() == SyntaxKind.RESOURCE_ACCESSOR_DEFINITION) {
                functionDefinitionNode.relativeResourcePath().forEach(resourcePath -> {
                    SemanticToken resourcePathToken = new SemanticToken(resourcePath.lineRange().startLine().line(),
                            resourcePath.lineRange().startLine().offset(), resourcePath.textRange().length(), type,
                            TokenTypeModifiers.DECLARATION.getId());
                    semanticTokens.add(resourcePathToken);
                });
            } else {
                handleReferences(startLine, length, type, 0);
            }
        }
        visitSyntaxNode(functionDefinitionNode);
    }

    public void visit(FunctionCallExpressionNode functionCallExpressionNode) {
        Node functionName = functionCallExpressionNode.functionName();
        if (functionName instanceof QualifiedNameReferenceNode) {
            functionName = ((QualifiedNameReferenceNode) functionName).identifier();
        }
        processSymbols(functionName, functionName.location().lineRange().startLine());
        visitSyntaxNode(functionCallExpressionNode);
    }

    public void visit(RequiredParameterNode requiredParameterNode) {
        requiredParameterNode.paramName().ifPresent(token -> this.addSemanticToken(token, TokenTypes.PARAMETER.getId(),
                TokenTypeModifiers.DECLARATION.getId(), true, TokenTypes.PARAMETER.getId(), 0));
        visitSyntaxNode(requiredParameterNode);
    }

    public void visit(CaptureBindingPatternNode captureBindingPatternNode) {
        this.addSemanticToken(captureBindingPatternNode, TokenTypes.VARIABLE.getId(),
                TokenTypeModifiers.DECLARATION.getId(), true, TokenTypes.VARIABLE.getId(), 0);
        visitSyntaxNode(captureBindingPatternNode);
    }

    public void visit(SimpleNameReferenceNode simpleNameReferenceNode) {
        if (!SemanticTokensConstants.SELF.equals(simpleNameReferenceNode.name().text())) {
            processSymbols(simpleNameReferenceNode, simpleNameReferenceNode.lineRange().startLine());
        }
        visitSyntaxNode(simpleNameReferenceNode);
    }

    public void visit(QualifiedNameReferenceNode qualifiedNameReferenceNode) {
        this.addSemanticToken(qualifiedNameReferenceNode.modulePrefix(), TokenTypes.NAMESPACE.getId(), 0, false, -1,
                -1);
        Token identifier = qualifiedNameReferenceNode.identifier();
        LinePosition position = identifier.lineRange().startLine();
        processSymbols(identifier, position);
        visitSyntaxNode(qualifiedNameReferenceNode);
    }

    public void visit(ConstantDeclarationNode constantDeclarationNode) {
        this.addSemanticToken(constantDeclarationNode.variableName(), TokenTypes.VARIABLE.getId(),
                TokenTypeModifiers.DECLARATION.getId() | TokenTypeModifiers.READONLY.getId(), true,
                TokenTypes.VARIABLE.getId(), TokenTypeModifiers.READONLY.getId());
        visitSyntaxNode(constantDeclarationNode);
    }

    public void visit(ClassDefinitionNode classDefinitionNode) {
        this.addSemanticToken(classDefinitionNode.className(), TokenTypes.CLASS.getId(),
                TokenTypeModifiers.DECLARATION.getId(), true, TokenTypes.CLASS.getId(), 0);
        visitSyntaxNode(classDefinitionNode);
    }

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

    public void visit(EnumDeclarationNode enumDeclarationNode) {
        this.addSemanticToken(enumDeclarationNode.identifier(), TokenTypes.ENUM.getId(),
                TokenTypeModifiers.DECLARATION.getId(), true, TokenTypes.ENUM.getId(), 0);
        visitSyntaxNode(enumDeclarationNode);
    }

    public void visit(EnumMemberNode enumMemberNode) {
        this.addSemanticToken(enumMemberNode.identifier(), TokenTypes.ENUM_MEMBER.getId(),
                TokenTypeModifiers.DECLARATION.getId() | TokenTypeModifiers.READONLY.getId(), true,
                TokenTypes.ENUM_MEMBER.getId(), TokenTypeModifiers.READONLY.getId());
        visitSyntaxNode(enumMemberNode);
    }

    public void visit(MarkdownParameterDocumentationLineNode markdownParameterDocumentationLineNode) {
        int type;
        SyntaxKind kind = markdownParameterDocumentationLineNode.parent().parent().parent().kind();
        switch (kind) {
            case OBJECT_FIELD:
                type = TokenTypes.PROPERTY.getId();
                break;
            case RECORD_FIELD:
            case TYPE_DEFINITION:
                type = TokenTypes.TYPE_PARAMETER.getId();
                break;
            default:
                type = TokenTypes.PARAMETER.getId();
                break;
        }
        this.addSemanticToken(markdownParameterDocumentationLineNode.parameterName(), type,
                TokenTypeModifiers.DOCUMENTATION.getId(), false, -1, -1);
        visitSyntaxNode(markdownParameterDocumentationLineNode);
    }

    public void visit(TypeDefinitionNode typeDefinitionNode) {
        this.addSemanticToken(typeDefinitionNode.typeName(), TokenTypes.TYPE.getId(),
                TokenTypeModifiers.DECLARATION.getId(), true, TokenTypes.TYPE.getId(), 0);
        visitSyntaxNode(typeDefinitionNode);
    }

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
            semanticToken.setProperties(length, TokenTypes.TYPE_PARAMETER.getId(), modifiers);
            semanticTokens.add(semanticToken);
            handleReferences(startLine, length, TokenTypes.TYPE_PARAMETER.getId(), refModifiers);
        }
        visitSyntaxNode(recordFieldNode);
    }

    public void visit(ObjectFieldNode objectFieldNode) {
        int type = objectFieldNode.parent().kind() == SyntaxKind.CLASS_DEFINITION ? TokenTypes.PROPERTY.getId() :
                TokenTypes.TYPE_PARAMETER.getId();
        this.addSemanticToken(objectFieldNode.fieldName(), type, TokenTypeModifiers.DECLARATION.getId(), true, type,
                0);
        visitSyntaxNode(objectFieldNode);
    }

    public void visit(AnnotationDeclarationNode annotationDeclarationNode) {
        this.addSemanticToken(annotationDeclarationNode.annotationTag(), TokenTypes.TYPE.getId(),
                TokenTypeModifiers.DECLARATION.getId(), true, TokenTypes.TYPE.getId(), 0);
        visitSyntaxNode(annotationDeclarationNode);
    }

    public void visit(DefaultableParameterNode defaultableParameterNode) {
        defaultableParameterNode.paramName().ifPresent(token -> this.addSemanticToken(token,
                TokenTypes.PARAMETER.getId(), TokenTypeModifiers.DECLARATION.getId(), true,
                TokenTypes.PARAMETER.getId(), 0));
        visitSyntaxNode(defaultableParameterNode);
    }

    public void visit(IncludedRecordParameterNode includedRecordParameterNode) {
        includedRecordParameterNode.paramName().ifPresent(token -> this.addSemanticToken(token,
                TokenTypes.PARAMETER.getId(), TokenTypeModifiers.DECLARATION.getId(), true,
                TokenTypes.PARAMETER.getId(), 0));
        visitSyntaxNode(includedRecordParameterNode);
    }

    public void visit(RestParameterNode restParameterNode) {
        restParameterNode.paramName().ifPresent(token -> this.addSemanticToken(token, TokenTypes.PARAMETER.getId(),
                TokenTypeModifiers.DECLARATION.getId(), true, TokenTypes.PARAMETER.getId(), 0));
        visitSyntaxNode(restParameterNode);
    }

    /**
     * Get the symbol of the given node and process the semantic tokens for the symbol and it's references.
     *
     * @param node      Current node
     * @param startLine Start line position
     */
    private void processSymbols(Node node, LinePosition startLine) {
        if (!semanticTokens.contains(new SemanticToken(startLine.line(), startLine.offset()))) {
            Optional<SemanticModel> semanticModel = this.semanticTokensContext.currentSemanticModel();
            if (semanticModel.isEmpty()) {
                return;
            }
            Optional<Symbol> symbol = semanticModel.get().symbol(node);
            if (symbol.isPresent() && symbol.get().getLocation().isPresent()) {
                LineRange symbolLineRange = symbol.get().getLocation().get().lineRange();
                LinePosition linePosition = symbolLineRange.startLine();
                SymbolKind kind = symbol.get().kind();
                String nodeName = node.toString().trim();

                int declarationType = -1, declarationModifiers = -1, referenceType = -1, referenceModifiers = -1;
                switch (kind) {
                    case CLASS:
                        if (!nodeName.equals(SemanticTokensConstants.SELF)) {
                            declarationType = TokenTypes.CLASS.getId();
                            declarationModifiers = TokenTypeModifiers.DECLARATION.getId();
                            referenceType = TokenTypes.CLASS.getId();
                        }
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
                        if (!nodeName.equals(SemanticTokensConstants.SELF)) {
                            declarationType = TokenTypes.VARIABLE.getId();
                            declarationModifiers = TokenTypeModifiers.DECLARATION.getId();
                            referenceType = TokenTypes.VARIABLE.getId();
                        }
                        break;
                    case TYPE:
                        declarationType = TokenTypes.TYPE.getId();
                        referenceType = TokenTypes.TYPE.getId();
                        declarationModifiers = TokenTypeModifiers.DECLARATION.getId();
                        break;
                    case RECORD_FIELD:
                        declarationType = TokenTypes.TYPE_PARAMETER.getId();
                        referenceType = TokenTypes.TYPE_PARAMETER.getId();
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
                        declarationModifiers = TokenTypeModifiers.DECLARATION.getId();
                        referenceType = TokenTypes.ENUM_MEMBER.getId();
                        break;
                    case PARAMETER:
                        declarationType = TokenTypes.PARAMETER.getId();
                        declarationModifiers = TokenTypeModifiers.DECLARATION.getId();
                        referenceType = TokenTypes.PARAMETER.getId();
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
                    default:
                        break;
                }

                Path path = this.semanticTokensContext.filePath().getFileName();
                if (path == null) {
                    return;
                }
                // Add the symbol's semantic token if it is in the same file
                if (declarationType != -1) {
                    if (symbolLineRange.filePath().equals(path.toString())) {
                        SemanticToken semanticToken = new SemanticToken(linePosition.line(), linePosition.offset());
                        if (!semanticTokens.contains(semanticToken)) {
                            semanticToken.setProperties(node.textRange().length(), declarationType,
                                    declarationModifiers == -1 ? 0 : declarationModifiers);
                            semanticTokens.add(semanticToken);
                        }
                    }
                }

                // Add symbol's references if they reside in the same file
                if (referenceType != -1) {
                    final int type = referenceType;
                    final int modifiers = referenceModifiers == -1 ? 0 : referenceModifiers;
                    semanticModel.get().references(symbol.get(), false).stream().filter(location ->
                            location.lineRange().filePath().equals(path.toString())).forEach(location -> {
                        LinePosition position = location.lineRange().startLine();
                        SemanticToken semanticToken = new SemanticToken(position.line(), position.offset());
                        if (!semanticTokens.contains(semanticToken)) {
                            semanticToken.setProperties(node.textRange().length(), type, modifiers);
                            semanticTokens.add(semanticToken);
                        }
                    });
                }
            }
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

        Optional<Document> docOptional = this.semanticTokensContext.currentDocument();
        if (docOptional.isEmpty()) {
            return;
        }

        Path path = this.semanticTokensContext.filePath().getFileName();
        if (path == null) {
            return;
        }

        semanticModel.get().references(docOptional.get(), linePosition).stream().filter(location ->
                location.lineRange().filePath().equals(path.toString())).forEach(location -> {
            LinePosition position = location.lineRange().startLine();
            SemanticToken semanticToken = new SemanticToken(position.line(), position.offset());
            if (!semanticTokens.contains(semanticToken)) {
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
