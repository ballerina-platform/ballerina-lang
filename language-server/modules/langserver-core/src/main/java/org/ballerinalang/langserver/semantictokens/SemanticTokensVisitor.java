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

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.EnumMemberNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportPrefixNode;
import io.ballerina.compiler.syntax.tree.MarkdownParameterDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.tools.text.LinePosition;

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

    private static final String SELF = "self";

    private enum TokenTypes {
        NAMESPACE(0), TYPE(1), CLASS(2), ENUM(3), INTERFACE(4), STRUCT(5),
        TYPE_PARAMETER(6), PARAMETER(7), VARIABLE(8), PROPERTY(9), ENUM_MEMBER(10),
        EVENT(11), FUNCTION(12), METHOD(13), MACRO(14), KEYWORD(15), MODIFIER(16),
        COMMENT(17), STRING(18), NUMBER(19), REGXP(20), OPERATOR(21);

        private final int value;

        TokenTypes(int value) {

            this.value = value;
        }
    }

    private enum TypeModifiers {
        DECLARATION(1), DEFINITION(1 << 1), READONLY(1 << 2), STATIC(1 << 3), DEPRECATED(1 << 4),
        ABSTRACT(1 << 5), ASYNC(1 << 6), MODIFICATION(1 << 7), DOCUMENTATION(1 << 8), DEFAULT_LIBRARY(1 << 9);
        private final int value;

        TypeModifiers(int value) {

            this.value = value;
        }
    }

    private final List<Integer> data;
    private SemanticToken previousToken;
    // sorted
    private final Set<SemanticToken> semanticTokens;
    private final SemanticTokensHandler semanticTokensHandler;

    public SemanticTokensVisitor(List<Integer> data, SemanticTokensHandler semanticTokensHandler) {

        this.data = data;
        this.semanticTokens = new TreeSet<>(SemanticToken.semanticTokenComparator);
        this.semanticTokensHandler = semanticTokensHandler;
    }

    public void visitSemanticTokens(Node node) {

        visitSyntaxNode(node);
        this.semanticTokens.forEach(this::processSemanticToken);
    }

    public void visit(ImportDeclarationNode importDeclarationNode) {

        Optional<ImportPrefixNode> importPrefixNode = importDeclarationNode.prefix();
        importPrefixNode.ifPresent(prefixNode -> this.addSemanticToken(prefixNode.prefix(), TokenTypes.NAMESPACE.value,
                TypeModifiers.DECLARATION.value, true, TokenTypes.NAMESPACE.value, 0));
        visitSyntaxNode(importDeclarationNode);
    }

    public void visit(FunctionDefinitionNode functionDefinitionNode) {

        LinePosition startLine = functionDefinitionNode.functionName().lineRange().startLine();
        SemanticToken semanticToken = new SemanticToken(startLine.line(), startLine.offset());
        if (!semanticTokens.contains(semanticToken)) {
            int length = functionDefinitionNode.functionName().text().length();
            semanticToken.setProperties(length, TokenTypes.FUNCTION.value, TypeModifiers.DECLARATION.value);
            semanticTokens.add(semanticToken);

            if (functionDefinitionNode.kind() == SyntaxKind.RESOURCE_ACCESSOR_DEFINITION) {
                functionDefinitionNode.relativeResourcePath().forEach(resourcePath -> {
                    SemanticToken resourcePathToken = new SemanticToken(resourcePath.lineRange().startLine().line(),
                            resourcePath.lineRange().startLine().offset(), resourcePath.toString().trim().length(),
                            TokenTypes.FUNCTION.value, TypeModifiers.DECLARATION.value);
                    semanticTokens.add(resourcePathToken);
                });
            } else {
                handleReferences(startLine, length, TokenTypes.FUNCTION.value, 0);
            }
        }
        visitSyntaxNode(functionDefinitionNode);
    }

    public void visit(RequiredParameterNode requiredParameterNode) {

        requiredParameterNode.paramName().ifPresent(token -> this.addSemanticToken(token, TokenTypes.PARAMETER.value,
                TypeModifiers.DECLARATION.value, true, TokenTypes.PARAMETER.value, 0));
        visitSyntaxNode(requiredParameterNode);
    }

    public void visit(CaptureBindingPatternNode captureBindingPatternNode) {

        this.addSemanticToken(captureBindingPatternNode, TokenTypes.VARIABLE.value, TypeModifiers.DECLARATION.value,
                true, TokenTypes.VARIABLE.value, 0);
        visitSyntaxNode(captureBindingPatternNode);
    }

    public void visit(SimpleNameReferenceNode simpleNameReferenceNode) {

        processSymbols(simpleNameReferenceNode, simpleNameReferenceNode.lineRange().startLine());
        visitSyntaxNode(simpleNameReferenceNode);
    }

    public void visit(QualifiedNameReferenceNode qualifiedNameReferenceNode) {

        this.addSemanticToken(qualifiedNameReferenceNode.modulePrefix(),
                TokenTypes.NAMESPACE.value, 0, false, -1, -1);

        Token identifier = qualifiedNameReferenceNode.identifier();
        LinePosition position = identifier.lineRange().startLine();
        SemanticToken identifierSemanticToken = new SemanticToken(position.line(), position.offset());
        if (!semanticTokens.contains(identifierSemanticToken)) {
            processSymbols(identifier, position);
        }
        visitSyntaxNode(qualifiedNameReferenceNode);
    }

    public void visit(ConstantDeclarationNode constantDeclarationNode) {

        this.addSemanticToken(constantDeclarationNode.variableName(), TokenTypes.VARIABLE.value,
                TypeModifiers.DECLARATION.value | TypeModifiers.READONLY.value, true, TokenTypes.VARIABLE.value,
                TypeModifiers.READONLY.value);
        visitSyntaxNode(constantDeclarationNode);
    }

    public void visit(ClassDefinitionNode classDefinitionNode) {

        this.addSemanticToken(classDefinitionNode.className(), TokenTypes.CLASS.value, TypeModifiers.DECLARATION.value,
                true, TokenTypes.CLASS.value, 0);
        visitSyntaxNode(classDefinitionNode);
    }

    public void visit(ServiceDeclarationNode serviceDeclarationNode) {

        serviceDeclarationNode.absoluteResourcePath().forEach(serviceName -> {
            LinePosition startLine = serviceName.lineRange().startLine();
            SemanticToken semanticToken = new SemanticToken(startLine.line(), startLine.offset(),
                    serviceName.toString().trim().length(), TokenTypes.TYPE.value, TypeModifiers.DECLARATION.value);
            semanticTokens.add(semanticToken);
        });
        visitSyntaxNode(serviceDeclarationNode);
    }

    public void visit(EnumDeclarationNode enumDeclarationNode) {

        this.addSemanticToken(enumDeclarationNode.identifier(), TokenTypes.ENUM.value, TypeModifiers.DECLARATION.value,
                false, -1, -1);
        visitSyntaxNode(enumDeclarationNode);
    }

    public void visit(EnumMemberNode enumMemberNode) {

        this.addSemanticToken(enumMemberNode.identifier(), TokenTypes.ENUM_MEMBER.value,
                TypeModifiers.DECLARATION.value | TypeModifiers.READONLY.value, true, TokenTypes.ENUM_MEMBER.value,
                TypeModifiers.READONLY.value);
        visitSyntaxNode(enumMemberNode);
    }

    public void visit(MarkdownParameterDocumentationLineNode markdownParameterDocumentationLineNode) {

        this.addSemanticToken(markdownParameterDocumentationLineNode.parameterName(), TokenTypes.PARAMETER.value,
                TypeModifiers.DOCUMENTATION.value, false, -1, -1);
        visitSyntaxNode(markdownParameterDocumentationLineNode);
    }

    public void visit(TypeDefinitionNode typeDefinitionNode) {

        this.addSemanticToken(typeDefinitionNode.typeName(), TokenTypes.TYPE.value, TypeModifiers.DECLARATION.value,
                true, TokenTypes.TYPE.value, 0);
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
                modifiers = TypeModifiers.DECLARATION.value | TypeModifiers.READONLY.value;
                refModifiers = TypeModifiers.READONLY.value;
            } else {
                modifiers = TypeModifiers.DECLARATION.value;
                refModifiers = 0;
            }
            semanticToken.setProperties(length, TokenTypes.TYPE_PARAMETER.value, modifiers);
            semanticTokens.add(semanticToken);
            handleReferences(startLine, length, TokenTypes.TYPE_PARAMETER.value, refModifiers);
        }
        visitSyntaxNode(recordFieldNode);
    }

    public void visit(AnnotationNode annotationNode) {

        visitSyntaxNode(annotationNode);
    }

    //public int age;
    public void visit(ObjectFieldNode objectFieldNode) {

        this.addSemanticToken(objectFieldNode.fieldName(), TokenTypes.PROPERTY.value, TypeModifiers.DECLARATION.value,
                true, TokenTypes.PROPERTY.value, 0);
        visitSyntaxNode(objectFieldNode);
    }

    private void processSymbols(Node node, LinePosition startLine) {

        SemanticToken semanticToken = new SemanticToken(startLine.line(), startLine.offset());
        if (!semanticTokens.contains(semanticToken)) {
            Optional<Symbol> symbol = this.semanticTokensHandler.getSemanticModelSymbol(node);
            if (symbol.isPresent()) {
                SymbolKind kind = symbol.get().kind();
                String nodeName = node.toString().trim();
                int type = -1;
                int modifiers = -1;
                switch (kind) {
                    case CLASS:
                        type = nodeName.equals(SELF) ? TokenTypes.KEYWORD.value : TokenTypes.CLASS.value;
                        break;
                    case CLASS_FIELD:
                        type = TokenTypes.PROPERTY.value;
                        break;
                    case CONSTANT:
                        type = TokenTypes.VARIABLE.value;
                        modifiers = TypeModifiers.READONLY.value;
                        break;
                    case VARIABLE:
                        type = nodeName.equals(SELF) ? TokenTypes.KEYWORD.value : TokenTypes.VARIABLE.value;
                        break;
                    case TYPE:
                    case RECORD_FIELD:
                        type = TokenTypes.TYPE.value;
                        break;
                    case ENUM_MEMBER:
                        type = TokenTypes.ENUM_MEMBER.value;
                        modifiers = TypeModifiers.READONLY.value;
                        break;
                    case PARAMETER:
                        type = TokenTypes.PARAMETER.value;
                        break;
                    case FUNCTION:
                        type = TokenTypes.FUNCTION.value;
                        break;
                    case METHOD:
                        type = TokenTypes.METHOD.value;
                        break;
                    default:
                        break;
                }
                if (type != -1) {
                    semanticToken.setProperties(nodeName.length(), type, modifiers == -1 ? 0 : modifiers);
                    semanticTokens.add(semanticToken);
                }
            }
        }
    }

    private void addSemanticToken(Node node, int type, int modifiers, boolean processReferences, int refType,
                                  int refModifiers) {

        LinePosition startLine = node.lineRange().startLine();
        SemanticToken semanticToken = new SemanticToken(startLine.line(), startLine.offset());
        if (!semanticTokens.contains(semanticToken)) {
            int length = node instanceof Token ? ((Token) node).text().trim().length() :
                    node.toString().trim().length();
            semanticToken.setProperties(length, type, modifiers);
            semanticTokens.add(semanticToken);
            if (processReferences) {
                handleReferences(startLine, length, refType, refModifiers);
            }
        }
    }

    private void processSemanticToken(SemanticToken semanticToken) {

        int line = semanticToken.getLine();
        int column = semanticToken.getColumn();
        int prevTokenLine = line;
        int prevTokenColumn = column;

        if (this.previousToken != null) {
            if (line == this.previousToken.getLine()) {
                column -= this.previousToken.getColumn();
            }
            line -= this.previousToken.getLine();
        }
        this.data.add(line);
        this.data.add(column);
        this.data.add(semanticToken.getLength());
        this.data.add(semanticToken.getType());
        this.data.add(semanticToken.getModifiers());
        this.previousToken = new SemanticToken(prevTokenLine, prevTokenColumn);
    }

    private void handleReferences(LinePosition linePosition, int length, int type, int modifiers) {

        this.semanticTokensHandler.getSemanticModelReferences(linePosition).forEach(location -> {
            LinePosition position = location.lineRange().startLine();
            SemanticToken semanticToken = new SemanticToken(position.line(), position.offset());
            if (!semanticTokens.contains(semanticToken)) {
                semanticToken.setProperties(length, type, modifiers);
                semanticTokens.add(semanticToken);
            }
        });
    }

    static class SemanticToken implements Comparable<SemanticToken> {

        private final int line;
        private final int column;
        private int length;
        private int type;
        private int modifiers;

        SemanticToken(int line, int column) {

            this.line = line;
            this.column = column;
        }

        public SemanticToken(int line, int column, int length, int type, int modifiers) {

            this.line = line;
            this.column = column;
            this.length = length;
            this.type = type;
            this.modifiers = modifiers;
        }

        public int getLine() {

            return line;
        }

        public int getColumn() {

            return column;
        }

        public int getLength() {

            return length;
        }

        public int getType() {

            return type;
        }

        public int getModifiers() {

            return modifiers;
        }

        public void setProperties(int length, int type, int modifiers) {

            this.length = length;
            this.type = type;
            this.modifiers = modifiers;
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
