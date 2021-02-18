/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.langserver.command.visitors;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;

import java.util.Optional;

/**
 * Finds the expected {@link TypeSymbol} of a provided {@link FunctionCallExpressionNode} node. This will try its
 * best to return a valid {@link TypeSymbol}. Else, the returned result will probably be a
 * {@link TypeDescKind#COMPILATION_ERROR}. Rarely, will return {@code null}.
 *
 * @since 2.0.0
 */
public class FunctionCallExpressionTypeFinder extends NodeVisitor {

    private final SemanticModel semanticModel;
    private TypeSymbol result;
    private boolean resultFound = false;

    public FunctionCallExpressionTypeFinder(SemanticModel semanticModel) {
        this.semanticModel = semanticModel;
    }

    /**
     * Search for the type symbol of the provided function call expression node.
     *
     * @param fnCallExprNode function call expression node whose type requires to be found
     * @return Optional of a valid type, compilation error type or null
     */
    public Optional<TypeSymbol> typeOf(FunctionCallExpressionNode fnCallExprNode) {
        fnCallExprNode.accept(this);
        return Optional.ofNullable(result);
    }

    @Override
    public void visit(ModuleVariableDeclarationNode moduleVariableDeclarationNode) {
        TypeSymbol typeSymbol = semanticModel.type(moduleVariableDeclarationNode).orElse(null);
        checkAndSetTypeResult(typeSymbol);
    }

    @Override
    public void visit(AssignmentStatementNode assignmentStatementNode) {
        TypeSymbol typeSymbol = semanticModel.type(assignmentStatementNode).orElse(null);
        checkAndSetTypeResult(typeSymbol);
        if (resultFound) {
            return;
        }

        assignmentStatementNode.varRef().accept(this);
        // We don't check the expression as it mostly is the original function call expression
    }

    @Override
    public void visit(VariableDeclarationNode variableDeclarationNode) {
        TypeSymbol typeSymbol = semanticModel.type(variableDeclarationNode).orElse(null);
        checkAndSetTypeResult(typeSymbol);
    }

    @Override
    public void visit(FunctionCallExpressionNode fnCallExprNode) {
        fnCallExprNode.functionName().accept(this);
        if (resultFound) {
            return;
        }

        TypeSymbol typeSymbol = semanticModel.type(fnCallExprNode.functionName()).orElse(null);
        checkAndSetTypeResult(typeSymbol);
        if (resultFound) {
            return;
        }

        fnCallExprNode.parent().accept(this);
    }

    @Override
    public void visit(SimpleNameReferenceNode simpleNameReferenceNode) {
        TypeSymbol typeSymbol = semanticModel.type(simpleNameReferenceNode).orElse(null);
        checkAndSetTypeResult(typeSymbol);
    }

    @Override
    protected void visitSyntaxNode(Node node) {
        // Do nothing
    }

    private void checkAndSetTypeResult(TypeSymbol typeSymbol) {
        if (typeSymbol == null) {
            return;
        }

        result = typeSymbol;
        if (typeSymbol.typeKind() != TypeDescKind.COMPILATION_ERROR) {
            resultFound = true;
        }
    }
}
