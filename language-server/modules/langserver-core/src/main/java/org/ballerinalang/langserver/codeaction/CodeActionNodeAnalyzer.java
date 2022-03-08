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

import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerina.compiler.syntax.tree.EnumDeclarationNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleXMLNamespaceDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import org.ballerinalang.langserver.codeaction.NodeBasedPositionDetailsImpl.PositionDetailsBuilder;

import java.util.Optional;

/**
 * Node analyzer for the code actions.
 * This will calculate the node information and fill the {@link PositionDetailsBuilder} and the information will be
 * reflected to the code actions via the PositionDetails API.
 *
 * @since 2201.1.x
 */
public class CodeActionNodeAnalyzer extends NodeVisitor {
    private final PositionDetailsBuilder positionDetailsBuilder;
    private final int positionOffset;

    public CodeActionNodeAnalyzer(PositionDetailsBuilder positionDetailsBuilder, int positionOffset) {
        this.positionDetailsBuilder = positionDetailsBuilder;
        this.positionOffset = positionOffset;
    }

    @Override
    public void visit(ServiceDeclarationNode node) {
        int serviceKwStart = node.serviceKeyword().textRange().startOffset();
        int openBraceEnd = node.openBraceToken().textRange().endOffset();
        int closeBraceEnd = node.closeBraceToken().textRange().endOffset();

        if (isWithinRange(serviceKwStart, closeBraceEnd)) {
            positionDetailsBuilder.setEnclosingDocumentableNode(node);
            if (isWithinRange(serviceKwStart, openBraceEnd)) {
                positionDetailsBuilder.setDocumentableNode(node);
                return;
            }
        }

        this.visitNode(node);
    }

    @Override
    public void visit(FunctionDefinitionNode node) {
        if (node.functionBody().kind() != SyntaxKind.FUNCTION_BODY_BLOCK) {
            return;
        }

        NodeList<Token> qualifiers = node.qualifierList();
        int startOffset = !qualifiers.isEmpty() ?
                qualifiers.get(0).textRange().startOffset() :
                node.functionKeyword().textRange().startOffset();
        FunctionBodyBlockNode functionBody = (FunctionBodyBlockNode) node.functionBody();
        int openBraceEnd = functionBody.openBraceToken().textRange().endOffset();
        int closeBraceEnd = functionBody.closeBraceToken().textRange().endOffset();

        if (isWithinRange(startOffset, closeBraceEnd)) {
            positionDetailsBuilder.setEnclosingDocumentableNode(node);
            if (isWithinRange(startOffset, openBraceEnd)) {
                positionDetailsBuilder.setDocumentableNode(node);
                return;
            }
        }

        this.visitNode(node);
    }

    @Override
    public void visit(TypeDefinitionNode node) {
        Optional<Token> qualifier = node.visibilityQualifier();
        int startOffset = qualifier.isEmpty() ?
                node.typeKeyword().textRange().startOffset() :
                qualifier.get().textRange().startOffset();
        int typeNameEnd = node.typeName().textRange().endOffset();
        int nodeEnd = node.textRange().endOffset();

        if (isWithinRange(startOffset, nodeEnd)) {
            positionDetailsBuilder.setEnclosingDocumentableNode(node);
            if (isWithinRange(startOffset, typeNameEnd)) {
                positionDetailsBuilder.setDocumentableNode(node);
                return;
            }
        }
        this.visitNode(node);
    }

    @Override
    public void visit(ClassDefinitionNode node) {
        Optional<Token> qualifier = node.visibilityQualifier();
        int startOffset = qualifier.isEmpty() ?
                node.classKeyword().textRange().startOffset() :
                qualifier.get().textRange().startOffset();
        int openBraceEnd = node.openBrace().textRange().endOffset();
        int closeBraceEnd = node.closeBrace().textRange().endOffset();

        if (isWithinRange(startOffset, closeBraceEnd)) {
            positionDetailsBuilder.setEnclosingDocumentableNode(node);
            if (isWithinRange(startOffset, openBraceEnd)) {
                positionDetailsBuilder.setDocumentableNode(node);
                return;
            }
        }

        this.visitNode(node);
    }

    @Override
    public void visit(ModuleVariableDeclarationNode node) {
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
            positionDetailsBuilder.setEnclosingDocumentableNode(node);
            if (isWithinRange(startOffset, bpEnd)) {
                positionDetailsBuilder.setDocumentableNode(node);
                return;
            }
        }

        this.visitNode(node);
    }

    @Override
    public void visit(ConstantDeclarationNode node) {
        Optional<Token> visibilityQualifier = node.visibilityQualifier();
        int startOffset = visibilityQualifier.map(token -> token.textRange().startOffset())
                .orElseGet(() -> node.constKeyword().textRange().startOffset());
        int varNameEnd = node.variableName().textRange().endOffset();
        int nodeEnd = node.textRange().endOffset();

        if (isWithinRange(startOffset, nodeEnd)) {
            positionDetailsBuilder.setEnclosingDocumentableNode(node);
            if (isWithinRange(startOffset, varNameEnd)) {
                positionDetailsBuilder.setDocumentableNode(node);
                return;
            }
        }

        this.visitNode(node);
    }

    @Override
    public void visit(EnumDeclarationNode node) {
        int startOffset = node.qualifier().map(token -> token.textRange().startOffset())
                .orElseGet(() -> node.enumKeywordToken().textRange().startOffset());
        int openBraceEnd = node.openBraceToken().textRange().endOffset();
        int closeBraceEnd = node.closeBraceToken().textRange().endOffset();

        if (isWithinRange(startOffset, closeBraceEnd)) {
            positionDetailsBuilder.setEnclosingDocumentableNode(node);
            if (isWithinRange(startOffset, openBraceEnd)) {
                positionDetailsBuilder.setDocumentableNode(node);
                return;
            }
        }

        this.visitNode(node);
    }

    @Override
    public void visit(ModuleXMLNamespaceDeclarationNode node) {
        int startOffset = node.xmlnsKeyword().textRange().startOffset();
        int endOffset = node.semicolonToken().textRange().endOffset();

        if (isWithinRange(startOffset, endOffset)) {
            positionDetailsBuilder.setEnclosingDocumentableNode(node);
            positionDetailsBuilder.setDocumentableNode(node);
            return;
        }
        this.visitNode(node);
    }

    @Override
    public void visit(MethodDeclarationNode node) {
        int startOffset;
        NodeList<Token> qualifierList = node.qualifierList();
        if (!qualifierList.isEmpty()) {
            startOffset = qualifierList.get(0).textRange().startOffset();
        } else {
            startOffset = node.functionKeyword().textRange().startOffset();
        }

        int endOffset = node.textRange().endOffset();

        if (isWithinRange(startOffset, endOffset)) {
            positionDetailsBuilder.setEnclosingDocumentableNode(node);
            positionDetailsBuilder.setDocumentableNode(node);
            return;
        }

        this.visitNode(node);
    }

    @Override
    public void visit(ModulePartNode modulePartNode) {
    }

    public void visit(Node node) {
        if (node == null) {
            return;
        }
        node.accept(this);
    }

    private void visitNode(Node node) {
        if (node.parent() == null) {
            return;
        }
        node.parent().accept(this);
    }

    @Override
    protected void visitSyntaxNode(Node node) {
        this.visitNode(node);
    }

    private boolean isWithinRange(int startOffSet, int endOffset) {
        return this.positionOffset > startOffSet && this.positionOffset <= endOffset;
    }
}
