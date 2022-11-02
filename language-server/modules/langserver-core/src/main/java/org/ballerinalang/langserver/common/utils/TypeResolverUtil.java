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
package org.ballerinalang.langserver.common.utils;

import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ParameterKind;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.NewExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.PositionedOperationContext;

import java.util.List;
import java.util.Optional;

/**
 * Carries a set of utilities used to resolve the context type.
 *
 * @since 2201.1.1
 */
public class TypeResolverUtil {
    
    /**
     * Given the function type symbol and existing arguments
     * returns the type of the parameter symbol corresponding to the given context.
     *
     * @param functionTypeSymbol Referenced FunctionTypeSymbol
     * @param cursorPosition     Cursor Position.
     * @param arguments          List of function argument nodes.
     * @return {@link Optional<ParameterSymbol>} Parameter's type symbol.
     */
    public static Optional<TypeSymbol> resolveParameterTypeSymbol(FunctionTypeSymbol functionTypeSymbol,
                                                                  int cursorPosition,
                                                                  NodeList<FunctionArgumentNode> arguments) {
        return resolveParameterTypeSymbol(functionTypeSymbol, cursorPosition, arguments, false);
    }

    /**
     * Given the function type symbol and existing arguments
     * returns the type of the parameter symbol corresponding to the given context.
     *
     * @param functionTypeSymbol Referenced FunctionTypeSymbol
     * @param cursorPosition     Cursor Position.
     * @param arguments          List of function argument nodes.
     * @param isLangLibFunction  Flag indicating whether the provided function type belongs to a langlib.
     * @return {@link Optional<ParameterSymbol>} Parameter's type symbol.
     */
    public static Optional<TypeSymbol> resolveParameterTypeSymbol(FunctionTypeSymbol functionTypeSymbol,
                                                                  int cursorPosition,
                                                                  NodeList<FunctionArgumentNode> arguments,
                                                                  boolean isLangLibFunction) {
        Optional<ParameterSymbol> parameterSymbol =
                resolveParameterSymbol(functionTypeSymbol, cursorPosition, arguments, isLangLibFunction);
        if (parameterSymbol.isEmpty()) {
            return Optional.empty();
        }
        TypeSymbol typeSymbol = parameterSymbol.get().typeDescriptor();
        if (parameterSymbol.get().paramKind() == ParameterKind.REST && typeSymbol.typeKind() == TypeDescKind.ARRAY) {
            return Optional.of(((ArrayTypeSymbol) typeSymbol).memberTypeDescriptor());
        }
        return Optional.of(typeSymbol);
    }

    /**
     * Given a positional argument node, it's parent (function or method call expression node) and function's/method's
     * argument nodes; this method returns the type symbol of the argument corresponding to the positional argument
     * provided.
     *
     * @param argumentNodes            Argument nodes of the function/method call expression
     * @param functionOrMethodCallExpr Function/method call expression
     * @param context                  Document Service context
     * @param cursorPosition           Cursor position
     * @return {@link Optional<TypeSymbol>} Type symbol.
     */
    public static Optional<TypeSymbol> getPositionalArgumentTypeForFunction(
            NodeList<FunctionArgumentNode> argumentNodes,
            NonTerminalNode functionOrMethodCallExpr,
            DocumentServiceContext context,
            int cursorPosition) {

        FunctionTypeSymbol functionTypeSymbol = null;

        // Look for function symbol in lang lib functions
        boolean isLangLibMethod = false;
        if (functionOrMethodCallExpr.kind() == SyntaxKind.METHOD_CALL) {
            Optional<FunctionTypeSymbol> langLibMethod = TypeResolverUtil.findMethodInLangLibFunctions(
                    (MethodCallExpressionNode) functionOrMethodCallExpr, context);
            if (langLibMethod.isPresent()) {
                functionTypeSymbol = langLibMethod.get();
                isLangLibMethod = true;
            }
        }

        if (functionTypeSymbol == null) {
            functionTypeSymbol = context.currentSemanticModel()
                    .flatMap(semanticModel -> semanticModel.symbol(functionOrMethodCallExpr))
                    .filter(symbol -> symbol.kind() == SymbolKind.FUNCTION ||
                            symbol.kind() == SymbolKind.METHOD ||
                            symbol.kind() == SymbolKind.RESOURCE_METHOD
                    )
                    .map(symbol -> ((FunctionSymbol) symbol).typeDescriptor())
                    .orElse(null);
        }

        if (functionTypeSymbol == null) {
            return Optional.empty();
        }
        return TypeResolverUtil.resolveParameterTypeSymbol(functionTypeSymbol, cursorPosition,
                argumentNodes, isLangLibMethod);
    }

    /**
     * Given a new expression node and a positional argument node, this method finds the type of the argument at the
     * positional argument.
     *
     * @param argumentNodes     Argument nodes
     * @param newExpressionNode Implicit/explicit new expression node
     * @param context           Document Service context
     * @param cursorPosition    Cursor position
     * @return Optional type symbol of the parameter
     */
    public static Optional<TypeSymbol> getPositionalArgumentTypeForNewExpr(NodeList<FunctionArgumentNode> argumentNodes,
                                                                           NewExpressionNode newExpressionNode,
                                                                           DocumentServiceContext context,
                                                                           int cursorPosition) {

        Optional<MethodSymbol> methodSymbol = context.currentSemanticModel()
                .flatMap(semanticModel -> semanticModel.typeOf(newExpressionNode))
                .flatMap(typeSymbol -> Optional.of(CommonUtil.getRawType(typeSymbol)))
                .map(typeSymbol -> {
                    if (typeSymbol.typeKind() == TypeDescKind.UNION) {
                        Optional<TypeSymbol> classType =
                                ((UnionTypeSymbol) typeSymbol).memberTypeDescriptors().stream()
                                        .map(CommonUtil::getRawType)
                                        .filter(member -> member instanceof ClassSymbol).findFirst();
                        if (classType.isPresent()) {
                            return classType.get();
                        }
                    }
                    return typeSymbol;
                })
                .filter(typeSymbol -> typeSymbol instanceof ClassSymbol)
                .flatMap(typeSymbol -> (((ClassSymbol) typeSymbol).initMethod()));

        if (methodSymbol.isEmpty()) {
            return Optional.empty();
        }
        return TypeResolverUtil.resolveParameterTypeSymbol(methodSymbol.get().typeDescriptor(), cursorPosition,
                argumentNodes);
    }
    
    /**
     * Finds the corresponding function type symbol from lang libs method given a method call expression node. 
     *
     * @param methodCallExprNode method call expression node.
     * @param context context
     * @return {@link Optional<ParameterSymbol>} function type symbol
     */
    public static Optional<FunctionTypeSymbol> findMethodInLangLibFunctions(MethodCallExpressionNode methodCallExprNode,
                                                                            DocumentServiceContext context) {

        if (methodCallExprNode.methodName().kind() != SyntaxKind.SIMPLE_NAME_REFERENCE) {
            return Optional.empty();
        }
        SimpleNameReferenceNode typeRefNode = (SimpleNameReferenceNode) methodCallExprNode.methodName();
        Optional<TypeSymbol> typeSymbol = context.currentSemanticModel()
                .flatMap(semanticModel -> semanticModel.typeOf(methodCallExprNode.expression()));
        return typeSymbol.flatMap(value -> value.langLibMethods().stream()
                .filter(method -> method.getName().orElse("").equals(typeRefNode.name().text()))
                .findFirst()
                .map(FunctionSymbol::typeDescriptor));
    }

    /**
     * Check if the cursor is positioned in a function call expression parameter context.
     *
     * @param ctx  PositionedOperationContext
     * @param node FunctionCallExpressionNode
     * @return {@link Boolean} whether the cursor is in parameter context.
     */
    public static Boolean isInFunctionCallParameterContext(PositionedOperationContext ctx,
                                                           FunctionCallExpressionNode node) {
        return isWithinParenthesis(ctx, node.openParenToken(), node.closeParenToken());
    }

    /**
     * Check if the cursor is positioned in a method call expression parameter context.
     *
     * @param ctx  PositionedOperationContext
     * @param node MethodCallExpressionNode
     * @return {@link Boolean} whether the cursor is in parameter context.
     */
    public static Boolean isInMethodCallParameterContext(PositionedOperationContext ctx,
                                                         MethodCallExpressionNode node) {
        return isWithinParenthesis(ctx, node.openParenToken(), node.closeParenToken());
    }

    /**
     * Check if the cursor is positioned in a method call expression parameter context.
     *
     * @param ctx  PositionedOperationContext
     * @param node RemoteMethodCallActionNode
     * @return {@link Boolean} whether the cursor is in parameter context.
     */
    public static Boolean isInMethodCallParameterContext(PositionedOperationContext ctx,
                                                         RemoteMethodCallActionNode node) {
        return isWithinParenthesis(ctx, node.openParenToken(), node.closeParenToken());
    }

    /**
     * Check if the cursor is positioned in a method call expression parameter context.
     *
     * @param ctx  PositionedOperationContext
     * @param node RemoteMethodCallActionNode
     * @return {@link Boolean} whether the cursor is in parameter context.
     */
    public static Boolean isInNewExpressionParameterContext(PositionedOperationContext ctx,
                                                            ImplicitNewExpressionNode node) {
        Optional<ParenthesizedArgList> argList = node.parenthesizedArgList();
        if (argList.isEmpty()) {
            return false;
        }
        return isWithinParenthesis(ctx, argList.get().openParenToken(), argList.get().closeParenToken());
    }

    /**
     * Check if the cursor is positioned in a method call expression parameter context.
     *
     * @param ctx  PositionedOperationContext
     * @param node RemoteMethodCallActionNode
     * @return {@link Boolean} whether the cursor is in parameter context.
     */
    public static Boolean isInNewExpressionParameterContext(PositionedOperationContext ctx,
                                                            ExplicitNewExpressionNode node) {
        ParenthesizedArgList argList = node.parenthesizedArgList();
        return isWithinParenthesis(ctx, argList.openParenToken(), argList.closeParenToken());
    }

    /**
     * Given the function type symbol and existing arguments
     * returns the parameter symbol of the function type corresponding to the given context.
     *
     * @param functionTypeSymbol Referenced FunctionTypeSymbol
     * @param cursorPosition     Cursor Position.
     * @param arguments          List of function argument nodes.
     * @param isLangLibFunction  Whether the function is a langlib function.                         
     * @return {@link Optional<ParameterSymbol>} Parameter's type symbol.
     */
    private static Optional<ParameterSymbol> resolveParameterSymbol(FunctionTypeSymbol functionTypeSymbol,
                                                                    int cursorPosition,
                                                                    NodeList<FunctionArgumentNode> arguments,
                                                                    boolean isLangLibFunction) {
        int argIndex = 0;
        for (Node child : arguments) {
            if (child.textRange().endOffset() < cursorPosition) {
                argIndex += 1;
            }
        }

        Optional<List<ParameterSymbol>> parameterSymbols = functionTypeSymbol.params();
        Optional<ParameterSymbol> restParam = functionTypeSymbol.restParam();

        // Check if we are not in an erroneous state. If rest params is empty and params are empty or params size is
        // lower than the arg index, we are in an invalid state
        if (restParam.isEmpty() && (parameterSymbols.isEmpty() || parameterSymbols.get().size() < argIndex + 1)) {
            return Optional.empty();
        }

        // If the function is a lang lib method, need to add 1 to skip the 1st parameter which is the same type.
        if (isLangLibFunction) {
            argIndex = argIndex + 1;
        }

        // We can be in required params or rest params
        if (parameterSymbols.isPresent() && parameterSymbols.get().size() > argIndex) {
            return Optional.of(parameterSymbols.get().get(argIndex));
        }

        return restParam;
    }

    private static boolean isWithinParenthesis(PositionedOperationContext ctx, Token openParen, Token closedParen) {
        int cursorPosition = ctx.getCursorPositionInTree();
        return (!openParen.isMissing())
                && (openParen.textRange().endOffset() <= cursorPosition)
                && (!closedParen.isMissing())
                && (cursorPosition <= closedParen.textRange().startOffset());
    }
}
