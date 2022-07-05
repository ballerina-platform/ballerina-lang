/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction;

import io.ballerina.compiler.syntax.tree.AnnotationDeclarationNode;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.BlockStatementNode;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleXMLNamespaceDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.util.Optional;

/**
 * Node analyzer for the code actions. This analyzer will determine
 * 1. The node for node based code actions
 * 2. Code action node type
 * 3. Closest statement node to the cursor position
 * 4. Documentable node
 * 5. Enclosing documentable node of the cursor position
 *
 * @since 2201.1.2
 */
public class CodeActionNodeAnalyzer extends NodeVisitor {

    private final int positionOffset;

    private NonTerminalNode codeActionNode;
    private CodeActionNodeType codeActionNodeType;
    private StatementNode statementNode;
    private NonTerminalNode documentableNode;
    private NonTerminalNode enclosingDocumentableNode;

    private CodeActionNodeAnalyzer(int positionOffset) {
        this.positionOffset = positionOffset;
    }

    /**
     * Entry point to the analyzer. Provided the cursor position and the syntax tree, this method will calculate the
     * node details as mentioned in the class level docs.
     *
     * @param cursorPosition Cursor position
     * @param syntaxTree     Syntax tree
     */
    public static CodeActionNodeAnalyzer analyze(Position cursorPosition, SyntaxTree syntaxTree) {
        LinePosition linePos = LinePosition.from(cursorPosition.getLine(), cursorPosition.getCharacter());
        int positionOffset = syntaxTree.textDocument().textPositionFrom(linePos);
        CodeActionNodeAnalyzer analyzer = new CodeActionNodeAnalyzer(positionOffset);
        NonTerminalNode node = CommonUtil.findNode(new Range(cursorPosition, cursorPosition), syntaxTree);
        if (node.kind() == SyntaxKind.LIST) {
            node.parent().accept(analyzer);
        } else {
            node.accept(analyzer);
        }
        
        return analyzer;
    }

    /*
     * ================================
     * Start- Module Level Declarations
     * ================================
     */

    @Override
    public void visit(ImportDeclarationNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetCodeActionNodeType(CodeActionNodeType.IMPORTS);
        visitSyntaxNode(node);
    }

    @Override
    public void visit(ListenerDeclarationNode node) {
        checkAndSetCodeActionNode(node);
        visitSyntaxNode(node);
    }

    @Override
    public void visit(ServiceDeclarationNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetCodeActionNodeType(CodeActionNodeType.SERVICE);

        int serviceKwStart = node.serviceKeyword().textRange().startOffset();
        int openBraceEnd = node.openBraceToken().textRange().endOffset();
        int closeBraceEnd = node.closeBraceToken().textRange().endOffset();

        if (isWithinRange(serviceKwStart, closeBraceEnd)) {
            checkAndSetEnclosingDocumentableNode(node);
            if (isWithinRange(serviceKwStart, openBraceEnd)) {
                checkAndSetDocumentableNode(node);
                return;
            }
        }

        this.visitSyntaxNode(node);
    }

    @Override
    public void visit(FunctionDefinitionNode node) {
        if (node.functionBody().kind() != SyntaxKind.FUNCTION_BODY_BLOCK) {
            return;
        }

        checkAndSetCodeActionNode(node);
        checkAndSetCodeActionNodeType(CodeActionNodeType.FUNCTION);

        NodeList<Token> qualifiers = node.qualifierList();
        int startOffset = !qualifiers.isEmpty() ?
                qualifiers.get(0).textRange().startOffset() : node.functionKeyword().textRange().startOffset();
        FunctionBodyBlockNode functionBody = (FunctionBodyBlockNode) node.functionBody();
        int openBraceEnd = functionBody.openBraceToken().textRange().endOffset();
        int closeBraceEnd = functionBody.closeBraceToken().textRange().endOffset();

        if (isWithinRange(startOffset, closeBraceEnd)) {
            checkAndSetEnclosingDocumentableNode(node);
            if (isWithinRange(startOffset, openBraceEnd)) {
                checkAndSetDocumentableNode(node);
                return;
            }
        }

        this.visitSyntaxNode(node);
    }

    @Override
    public void visit(TypeDefinitionNode node) {
        checkAndSetCodeActionNode(node);

        // If cursor was outside object/record type desc, we have to manually check for the type
        Node typeDescriptor = node.typeDescriptor();
        if (typeDescriptor.kind() == SyntaxKind.RECORD_TYPE_DESC) {
            checkAndSetCodeActionNodeType(CodeActionNodeType.RECORD);
        } else if (typeDescriptor.kind() == SyntaxKind.OBJECT_TYPE_DESC) {
            checkAndSetCodeActionNodeType(CodeActionNodeType.OBJECT);
        }

        Optional<Token> qualifier = node.visibilityQualifier();
        int startOffset = qualifier.isEmpty() ?
                node.typeKeyword().textRange().startOffset() : qualifier.get().textRange().startOffset();
        int typeNameEnd = node.typeName().textRange().endOffset();
        int nodeEnd = node.textRange().endOffset();

        if (isWithinRange(startOffset, nodeEnd)) {
            checkAndSetEnclosingDocumentableNode(node);
            if (isWithinRange(startOffset, typeNameEnd)) {
                checkAndSetDocumentableNode(node);
                return;
            }
        }
        this.visitSyntaxNode(node);
    }

    @Override
    public void visit(ClassDefinitionNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetCodeActionNodeType(CodeActionNodeType.CLASS);

        Optional<Token> qualifier = node.visibilityQualifier();
        int startOffset = qualifier.isEmpty() ?
                node.classKeyword().textRange().startOffset() : qualifier.get().textRange().startOffset();
        int openBraceEnd = node.openBrace().textRange().endOffset();
        int closeBraceEnd = node.closeBrace().textRange().endOffset();

        if (isWithinRange(startOffset, closeBraceEnd)) {
            checkAndSetEnclosingDocumentableNode(node);
            if (isWithinRange(startOffset, openBraceEnd)) {
                checkAndSetDocumentableNode(node);
                return;
            }
        }

        this.visitSyntaxNode(node);
    }

    @Override
    public void visit(ModuleVariableDeclarationNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetCodeActionNodeType(CodeActionNodeType.MODULE_VARIABLE);

        Optional<Token> visibilityQualifier = node.visibilityQualifier();
        NodeList<Token> qualifiers = node.qualifiers();

        int startOffset;
        if (visibilityQualifier.isPresent()) {
            startOffset = visibilityQualifier.get().textRange().startOffset();
        } else if (!qualifiers.isEmpty()) {
            startOffset = qualifiers.get(0).textRange().startOffset();
        } else {
            startOffset = node.typedBindingPattern().textRange().startOffset();
        }

        int bpEnd = node.typedBindingPattern().bindingPattern().textRange().endOffset();
        int nodeEnd = node.textRange().endOffset();

        if (isWithinRange(startOffset, nodeEnd)) {
            checkAndSetEnclosingDocumentableNode(node);
            if (isWithinRange(startOffset, bpEnd)) {
                checkAndSetDocumentableNode(node);
                return;
            }
        }

        this.visitSyntaxNode(node);
    }

    @Override
    public void visit(ConstantDeclarationNode node) {
        checkAndSetCodeActionNode(node);

        Optional<Token> visibilityQualifier = node.visibilityQualifier();
        int startOffset = visibilityQualifier.map(token -> token.textRange().startOffset())
                .orElseGet(() -> node.constKeyword().textRange().startOffset());
        int varNameEnd = node.variableName().textRange().endOffset();
        int nodeEnd = node.textRange().endOffset();

        if (isWithinRange(startOffset, nodeEnd)) {
            checkAndSetEnclosingDocumentableNode(node);
            if (isWithinRange(startOffset, varNameEnd)) {
                checkAndSetDocumentableNode(node);
                return;
            }
        }

        this.visitSyntaxNode(node);
    }

    @Override
    public void visit(EnumDeclarationNode node) {
        checkAndSetCodeActionNode(node);

        int startOffset = node.qualifier().map(token -> token.textRange().startOffset())
                .orElseGet(() -> node.enumKeywordToken().textRange().startOffset());
        int openBraceEnd = node.openBraceToken().textRange().endOffset();
        int closeBraceEnd = node.closeBraceToken().textRange().endOffset();

        if (isWithinRange(startOffset, closeBraceEnd)) {
            checkAndSetEnclosingDocumentableNode(node);
            if (isWithinRange(startOffset, openBraceEnd)) {
                checkAndSetDocumentableNode(node);
                return;
            }
        }

        this.visitSyntaxNode(node);
    }

    @Override
    public void visit(ModuleXMLNamespaceDeclarationNode node) {
        checkAndSetCodeActionNode(node);

        int startOffset = node.xmlnsKeyword().textRange().startOffset();
        int endOffset = node.semicolonToken().textRange().endOffset();

        if (isWithinRange(startOffset, endOffset)) {
            checkAndSetEnclosingDocumentableNode(node);
            checkAndSetDocumentableNode(node);
            return;
        }
        this.visitSyntaxNode(node);
    }

    @Override
    public void visit(AnnotationDeclarationNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetCodeActionNodeType(CodeActionNodeType.ANNOTATION);

        int startOffset = node.visibilityQualifier()
                .map(token -> token.textRange().startOffset())
                .orElseGet(() -> node.constKeyword()
                        .map(token -> token.textRange().startOffset())
                        .orElseGet(() -> node.annotationKeyword().textRange().startOffset())
                );
        int annotationNameEnd = node.annotationTag().textRange().endOffset();
        int endOffset = node.semicolonToken().textRange().endOffset();
        if (isWithinRange(startOffset, endOffset)) {
            checkAndSetEnclosingDocumentableNode(node);
            if (isWithinRange(startOffset, annotationNameEnd)) {
                checkAndSetDocumentableNode(node);
                return;
            }
        }
        this.visitSyntaxNode(node);
    }

    /*
     * ===============================
     * End - Module Level Declarations
     * ===============================
     */

    @Override
    public void visit(MethodDeclarationNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetCodeActionNodeType(CodeActionNodeType.OBJECT_FUNCTION);

        int startOffset;
        NodeList<Token> qualifierList = node.qualifierList();
        if (!qualifierList.isEmpty()) {
            startOffset = qualifierList.get(0).textRange().startOffset();
        } else {
            startOffset = node.functionKeyword().textRange().startOffset();
        }

        int endOffset = node.textRange().endOffset();

        if (isWithinRange(startOffset, endOffset)) {
            checkAndSetEnclosingDocumentableNode(node);
            checkAndSetDocumentableNode(node);
        }

        this.visitSyntaxNode(node);
    }

    @Override
    public void visit(RecordTypeDescriptorNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetCodeActionNodeType(CodeActionNodeType.RECORD);
        visitSyntaxNode(node);
    }

    @Override
    public void visit(ObjectTypeDescriptorNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetCodeActionNodeType(CodeActionNodeType.OBJECT);
        visitSyntaxNode(node);
    }

    @Override
    public void visit(VariableDeclarationNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetCodeActionNodeType(CodeActionNodeType.LOCAL_VARIABLE);
        visitSyntaxNode(node);
    }

    @Override
    public void visit(AssignmentStatementNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetCodeActionNodeType(CodeActionNodeType.ASSIGNMENT);
        visitSyntaxNode(node);
    }

    @Override
    public void visit(ObjectFieldNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetCodeActionNodeType(CodeActionNodeType.OBJECT_FIELD);
        
        int startOffset = node.visibilityQualifier()
                .map(token -> token.textRange().startOffset())
                .or(() -> node.qualifierList().stream().findFirst().map(token -> token.textRange().startOffset()))
                .orElseGet(() -> node.typeName().textRange().startOffset());
        int endOffset = node.semicolonToken().textRange().endOffset();
        if (isWithinRange(startOffset, endOffset)) {
           checkAndSetEnclosingDocumentableNode(node);
           checkAndSetDocumentableNode(node);
        }
    }

    public void visit(Node node) {
        if (node == null) {
            return;
        }
        node.accept(this);
    }

    private void checkAndSetCodeActionNode(NonTerminalNode node) {
        if (codeActionNode == null) {
            codeActionNode = node;
        }
    }

    private void checkAndSetCodeActionNodeType(CodeActionNodeType type) {
        if (this.codeActionNodeType == null) {
            this.codeActionNodeType = type;
        }
    }

    private void checkAndSetStatementNode(StatementNode node) {
        if (statementNode == null) {
            statementNode = node;
        }
    }

    private void checkAndSetDocumentableNode(NonTerminalNode node) {
        if (documentableNode == null) {
            documentableNode = node;
        }
    }

    private void checkAndSetEnclosingDocumentableNode(NonTerminalNode node) {
        if (enclosingDocumentableNode == null) {
            enclosingDocumentableNode = node;
        }
    }

    private boolean isWithinRange(int startOffSet, int endOffset) {
        return this.positionOffset > startOffSet && this.positionOffset <= endOffset;
    }

    @Override
    protected void visitSyntaxNode(Node node) {
        // Here we check for the statement nodes explicitly to identify the closest statement node
        if (node instanceof StatementNode) {
            if (!(node instanceof BlockStatementNode)) {
                checkAndSetStatementNode((StatementNode) node);
            }
        }

        if (node.parent() != null) {
            node.parent().accept(this);
        }
    }

    public Optional<NonTerminalNode> getCodeActionNode() {
        return Optional.ofNullable(codeActionNode);
    }

    public CodeActionNodeType getCodeActionNodeType() {
        return codeActionNodeType != null ? codeActionNodeType : CodeActionNodeType.NONE;
    }

    public Optional<StatementNode> getStatementNode() {
        return Optional.ofNullable(statementNode);
    }

    public Optional<NonTerminalNode> getDocumentableNode() {
        return Optional.ofNullable(documentableNode);
    }

    public Optional<NonTerminalNode> getEnclosingDocumentableNode() {
        return Optional.ofNullable(enclosingDocumentableNode);
    }
}
