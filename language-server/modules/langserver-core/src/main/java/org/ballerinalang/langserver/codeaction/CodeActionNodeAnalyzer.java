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
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BinaryExpressionNode;
import io.ballerina.compiler.syntax.tree.BlockStatementNode;
import io.ballerina.compiler.syntax.tree.BracedExpressionNode;
import io.ballerina.compiler.syntax.tree.CheckExpressionNode;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.CompoundAssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerina.compiler.syntax.tree.DoStatementNode;
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.ErrorConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.ForEachStatementNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.IfElseStatementNode;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.IndexedExpressionNode;
import io.ballerina.compiler.syntax.tree.LetExpressionNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.LockStatementNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MatchStatementNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleXMLNamespaceDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ObjectConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.QueryExpressionNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TableConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.TemplateExpressionNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TrapExpressionNode;
import io.ballerina.compiler.syntax.tree.TypeCastExpressionNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeTestExpressionNode;
import io.ballerina.compiler.syntax.tree.TypeofExpressionNode;
import io.ballerina.compiler.syntax.tree.UnaryExpressionNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.WhileStatementNode;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.eclipse.lsp4j.Range;

import java.util.Optional;

/**
 * Node analyzer for the code actions. This analyzer will determine
 * 1. The node for node based code actions
 * 2. Code action syntax kind
 * 3. Closest statement node to the cursor position
 * 4. Documentable node
 * 5. Enclosing documentable node of the cursor position
 *
 * @since 2201.1.2
 */
public class CodeActionNodeAnalyzer extends NodeVisitor {

    private final int startPositionOffset;
    private final int endPositionOffset;
    private NonTerminalNode codeActionNode;
    private SyntaxKind syntaxKind;
    private StatementNode statementNode;
    private NonTerminalNode documentableNode;
    private NonTerminalNode enclosingDocumentableNode;

    private CodeActionNodeAnalyzer(int startPositionOffset, int endPositionOffset) {
        this.startPositionOffset = startPositionOffset;
        this.endPositionOffset = endPositionOffset;
    }

    /**
     * Entry point to the analyzer. Provided the cursor position and the syntax tree, this method will calculate the
     * node details as mentioned in the class level docs.
     *
     * @param range      Highlighted range
     * @param syntaxTree Syntax tree
     */
    public static CodeActionNodeAnalyzer analyze(Range range, SyntaxTree syntaxTree) {
        int startPositionOffset = PositionUtil.getPositionOffset(range.getStart(), syntaxTree);
        int endPositionOffset = PositionUtil.getPositionOffset(range.getEnd(), syntaxTree);

        CodeActionNodeAnalyzer analyzer = new CodeActionNodeAnalyzer(startPositionOffset, endPositionOffset);
        NonTerminalNode node = CommonUtil.findNode(range, syntaxTree);
        if (node.kind() == SyntaxKind.LIST) {
            /*
            * LIST node is used in multiple scenarios(ex: In the import declaration) and,
            * LIST of Statement nodes is special cased here for the extract to function code action
            * */
            if (hasChildStatement(node)) {
                analyzer.checkAndSetCodeActionNode(node);
                analyzer.checkAndSetSyntaxKind(node.kind());
            }
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
        checkAndSetSyntaxKind(node.kind());
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
        checkAndSetSyntaxKind(node.kind());

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
        checkAndSetSyntaxKind(node.kind());

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
            checkAndSetSyntaxKind(typeDescriptor.kind());
        } else if (typeDescriptor.kind() == SyntaxKind.OBJECT_TYPE_DESC) {
            checkAndSetSyntaxKind(typeDescriptor.kind());
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
        checkAndSetSyntaxKind(node.kind());

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
        checkAndSetSyntaxKind(node.kind());

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
        checkAndSetSyntaxKind(node.kind());

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
        checkAndSetSyntaxKind(node.kind());

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
        checkAndSetSyntaxKind(node.kind());

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
        checkAndSetSyntaxKind(node.kind());

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
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    @Override
    public void visit(BasicLiteralNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    @Override
    public void visit(BracedExpressionNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    @Override
    public void visit(ObjectTypeDescriptorNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    @Override
    public void visit(VariableDeclarationNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    @Override
    public void visit(AssignmentStatementNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    @Override
    public void visit(BinaryExpressionNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    @Override
    public void visit(QualifiedNameReferenceNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    @Override
    public void visit(IndexedExpressionNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    @Override
    public void visit(FieldAccessExpressionNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    @Override
    public void visit(MethodCallExpressionNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    @Override
    public void visit(MappingConstructorExpressionNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    @Override
    public void visit(TypeofExpressionNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    @Override
    public void visit(TypeTestExpressionNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    @Override
    public void visit(ListConstructorExpressionNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    @Override
    public void visit(TypeCastExpressionNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    @Override
    public void visit(TableConstructorExpressionNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    @Override
    public void visit(LetExpressionNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    @Override
    public void visit(ImplicitNewExpressionNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    @Override
    public void visit(ExplicitNewExpressionNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    @Override
    public void visit(ObjectConstructorExpressionNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    @Override
    public void visit(ErrorConstructorExpressionNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    @Override
    public void visit(FunctionCallExpressionNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    @Override
    public void visit(IfElseStatementNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    @Override
    public void visit(CheckExpressionNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    @Override
    public void visit(CompoundAssignmentStatementNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    @Override
    public void visit(WhileStatementNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    @Override
    public void visit(UnaryExpressionNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    @Override
    public void visit(ReturnStatementNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    @Override
    public void visit(LockStatementNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    @Override
    public void visit(ForEachStatementNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    @Override
    public void visit(MatchStatementNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    @Override
    public void visit(DoStatementNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    @Override
    public void visit(TrapExpressionNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    @Override
    public void visit(QueryExpressionNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    @Override
    public void visit(TemplateExpressionNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    @Override
    public void visit(ObjectFieldNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());

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

    @Override
    public void visit(SpecificFieldNode node) {
        checkAndSetCodeActionNode(node);
        checkAndSetSyntaxKind(node.kind());
        visitSyntaxNode(node);
    }

    public void visit(Node node) {
        if (node == null) {
            return;
        }
        node.accept(this);
    }

    /**
     * // TODO replace the if condition logic with #37454.
     *
     * @param node External tree node list
     * @return If any of the child node of the external tree node list is a statement node
     */
    private static boolean hasChildStatement(NonTerminalNode node) {
        for (Node childNode : node.children()) {
            if (childNode.kind().compareTo(SyntaxKind.BLOCK_STATEMENT) >= 0
                    && childNode.kind().compareTo(SyntaxKind.BINARY_EXPRESSION) < 0) {
                return true;
            }
        }

        return false;
    }

    private void checkAndSetCodeActionNode(NonTerminalNode node) {
        if (codeActionNode == null) {
            codeActionNode = node;
        }
    }

    private void checkAndSetSyntaxKind(SyntaxKind kind) {
        if (this.syntaxKind == null) {
            this.syntaxKind = kind;
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
        return this.startPositionOffset > startOffSet && this.endPositionOffset <= endOffset;
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

    public SyntaxKind getSyntaxKind() {
        return syntaxKind != null ? syntaxKind : SyntaxKind.NONE;
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
