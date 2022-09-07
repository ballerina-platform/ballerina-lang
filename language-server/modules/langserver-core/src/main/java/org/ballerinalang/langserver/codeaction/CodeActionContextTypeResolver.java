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

import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.LetVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import org.ballerinalang.langserver.common.utils.TypeResolverUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This visitor is used to resolve the type of given code action context.
 * <p>
 * Todo: Use the ContextTypeResolver instead.
 *
 * @since 2201.1.1
 */
public class CodeActionContextTypeResolver extends NodeTransformer<Optional<TypeSymbol>> {

    private final CodeActionContext context;
    private final List<Node> visitedNodes = new ArrayList<>();

    public CodeActionContextTypeResolver(CodeActionContext context) {
        this.context = context;
    }

    @Override
    protected Optional<TypeSymbol> transformSyntaxNode(Node node) {
        return this.visit(node.parent());
    }

    private Optional<TypeSymbol> visit(Node node) {
        if (node == null || visitedNodes.contains(node)) {
            return Optional.empty();
        }
        visitedNodes.add(node);
        return node.apply(this);
    }

    @Override
    public Optional<TypeSymbol> transform(VariableDeclarationNode node) {
        return getTypeDescriptorOfVariable(node);
    }

    @Override
    public Optional<TypeSymbol> transform(AssignmentStatementNode assignmentStatementNode) {
        return getTypeDescriptorOfVariable(assignmentStatementNode.varRef());
    }

    @Override
    public Optional<TypeSymbol> transform(ModuleVariableDeclarationNode moduleVariableDeclarationNode) {
        return getTypeDescriptorOfVariable(moduleVariableDeclarationNode);
    }

    @Override
    public Optional<TypeSymbol> transform(LetVariableDeclarationNode letVariableDeclarationNode) {
        return getTypeDescriptorOfVariable(letVariableDeclarationNode);
    }

    @Override
    public Optional<TypeSymbol> transform(FunctionDefinitionNode functionDefinitionNode) {
        Optional<ReturnTypeDescriptorNode> returnTypeDesc = functionDefinitionNode.functionSignature().returnTypeDesc();
        if (returnTypeDesc.isEmpty() || context.currentSemanticModel().isEmpty()) {
            return Optional.empty();
        }

        Optional<Symbol> functionSymbol = context.currentSemanticModel().get().symbol(functionDefinitionNode);

        if (functionSymbol.isEmpty()) {
            return Optional.empty();
        }

        return ((FunctionSymbol) functionSymbol.get()).typeDescriptor().returnTypeDescriptor();
    }

    @Override
    public Optional<TypeSymbol> transform(ConstantDeclarationNode constantDeclarationNode) {
        if (context.currentSemanticModel().isEmpty()) {
            return Optional.empty();
        }
        Optional<Symbol> symbol = context.currentSemanticModel().get().symbol(constantDeclarationNode);
        
        if (symbol.isEmpty()) {
            return Optional.empty();
        }
        
        return Optional.ofNullable(((ConstantSymbol) symbol.get()).typeDescriptor());
    }

    @Override
    public Optional<TypeSymbol> transform(PositionalArgumentNode positionalArgumentNode) {
        // TODO: Add other cases like error constructors here
        switch (positionalArgumentNode.parent().kind()) {
            case FUNCTION_CALL:
                return TypeResolverUtil.getPositionalArgumentTypeForFunction(
                        ((FunctionCallExpressionNode) positionalArgumentNode.parent()).arguments(),
                        positionalArgumentNode.parent(), context, context.cursorPositionInTree());
            case METHOD_CALL:
                return TypeResolverUtil.getPositionalArgumentTypeForFunction(
                        ((MethodCallExpressionNode) positionalArgumentNode.parent()).arguments(),
                        positionalArgumentNode.parent(), context, context.cursorPositionInTree());
            case PARENTHESIZED_ARG_LIST:
                ParenthesizedArgList parenthesizedArgList = (ParenthesizedArgList) positionalArgumentNode.parent();
                switch (parenthesizedArgList.parent().kind()) {
                    case IMPLICIT_NEW_EXPRESSION:
                        ImplicitNewExpressionNode implicitNewExpressionNode =
                                (ImplicitNewExpressionNode) parenthesizedArgList.parent();
                        Optional<ParenthesizedArgList> argList = implicitNewExpressionNode.parenthesizedArgList();
                        if (argList.isEmpty()) {
                            return Optional.empty();
                        }
                        return TypeResolverUtil.getPositionalArgumentTypeForNewExpr(argList.get().arguments(),
                                implicitNewExpressionNode, context, context.cursorPositionInTree());
                    case EXPLICIT_NEW_EXPRESSION:
                        ExplicitNewExpressionNode explicitNewExpressionNode =
                                (ExplicitNewExpressionNode) parenthesizedArgList.parent();
                        return TypeResolverUtil.getPositionalArgumentTypeForNewExpr(
                                explicitNewExpressionNode.parenthesizedArgList().arguments(),
                                explicitNewExpressionNode, context, context.cursorPositionInTree());
                }
        }

        return Optional.empty();
    }

    private Optional<TypeSymbol> getTypeDescriptorOfVariable(Node node) {
        Optional<Symbol> symbol = context.currentSemanticModel().flatMap(semanticModel -> semanticModel.symbol(node));
        if (symbol.isEmpty() || symbol.get().kind() != SymbolKind.VARIABLE) {
            return Optional.empty();
        }
        return Optional.of(((VariableSymbol) symbol.get()).typeDescriptor());
    }
}
