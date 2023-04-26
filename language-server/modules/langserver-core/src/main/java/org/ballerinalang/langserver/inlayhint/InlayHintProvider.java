/*
 * Copyright (c) 2023, WSO2 LLC. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.inlayhint;

import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.common.utils.TypeResolverUtil;
import org.ballerinalang.langserver.commons.InlayHintContext;
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represents the Inlay Hint Provider.
 *
 * @since 2201.x.x
 */
public class InlayHintProvider {
    public static List<InlayHint> getInlayHint(InlayHintContext context) {
        InlayHintMethodTypeFinder inlayHintMethodFinder = new InlayHintMethodTypeFinder();
        if (context.currentDocument().isEmpty()) {
            return Collections.emptyList();
        }
        Node rootNode = context.currentDocument().get().syntaxTree().rootNode();
        rootNode.accept(inlayHintMethodFinder);

        // Get the method name list
        List<NonTerminalNode> methodNameList = inlayHintMethodFinder.getMethodNameList();
        List<InlayHint> inlayHints = new ArrayList<>();

        for (NonTerminalNode node : methodNameList) {
            InlayHintArgumentTypeFinder argumentTypeFinder = new InlayHintArgumentTypeFinder();
            node.accept(argumentTypeFinder);

            // Get the argument list
            List<NonTerminalNode> argList = argumentTypeFinder.getArgumentList();
            List<LineRange> commaList = argumentTypeFinder.getCommaList();

            List<ParameterSymbol> parameterSymbols = getParameterSymbols(context, node);

            for (int i = 0; i < commaList.size(); i++) {
                if (argList.isEmpty()) {
                    continue;
                }
                LineRange lineRange = commaList.get(i);
                int startLine = lineRange.endLine().line();
                int startChar = lineRange.endLine().offset();

                Position position = new Position(startLine, startChar);

                if (parameterSymbols.size() <= i || parameterSymbols.get(i).getName().isEmpty()) {
                    continue;
                }
                String label = parameterSymbols.get(i).getName().get();

                InlayHint inlayHint = new InlayHint(position, Either.forLeft(label + ": "));
                inlayHints.add(inlayHint);
            }
        }
        return inlayHints;
    }

    private static List<ParameterSymbol> getParameterSymbols(InlayHintContext context,
                                                             NonTerminalNode node) {
        if (node.kind() == SyntaxKind.METHOD_CALL) {
            MethodCallExpressionNode methodCallExpressionNode = (MethodCallExpressionNode) node;
            Optional<FunctionTypeSymbol> libFunctions = TypeResolverUtil
                    .findMethodInLangLibFunctions(methodCallExpressionNode, context);
            if (libFunctions.isEmpty() || libFunctions.get().params().isEmpty()) {
                return Collections.emptyList();
            }
            // Since the method is a lang-lib function, skip the first parameter
            return libFunctions.get().params().get().stream().skip(1).collect(Collectors.toList());
        } else {
            FunctionCallExpressionNode functionCallExpressionNode = (FunctionCallExpressionNode) node;
            return getFunctionCallParameterSymbols(context, functionCallExpressionNode);
        }
    }

    private static List<ParameterSymbol> getFunctionCallParameterSymbols(InlayHintContext context,
                                                                         FunctionCallExpressionNode node) {
        LineRange lineRange = node.lineRange();
        List<Symbol> visibleSymbols = context.visibleSymbols(new Position(
                lineRange.startLine().line(), lineRange.startLine().offset()));

        List<ParameterSymbol> parameterSymbols = visibleSymbols.stream()
                .filter(symbol -> symbol.kind() == SymbolKind.FUNCTION)
                .map(symbol -> (FunctionSymbol) symbol)
                .filter(functionSymbol -> functionSymbol.getName().get().equals(
                        node.functionName().toString().strip()))
                .map(functionSymbol -> SymbolUtil.getTypeDescriptor(functionSymbol).get())
                .filter(typeDescriptor -> typeDescriptor instanceof FunctionTypeSymbol)
                .map(typeDescriptor -> (FunctionTypeSymbol) typeDescriptor)
                .filter(functionTypeSymbol -> functionTypeSymbol.params().isPresent())
                .map(functionTypeSymbol -> functionTypeSymbol.params().get())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        for (Node child : node.children()) {
            if (child.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
                parameterSymbols = visibleSymbols.stream()
                        .filter(symbol -> symbol.kind() == SymbolKind.MODULE)
                        .map(symbol -> (ModuleSymbol) symbol)
                        .filter(moduleSymbol -> moduleSymbol.getName().get().equals(
                                ((QualifiedNameReferenceNode) child).modulePrefix().toString().strip()))
                        .map(moduleSymbol -> moduleSymbol.functions().stream()
                                .filter(functionSymbol -> functionSymbol.getName().get()
                                        .equals(((QualifiedNameReferenceNode) child).identifier().text()))
                                .map(functionSymbol -> SymbolUtil.getTypeDescriptor(functionSymbol).get())
                                .filter(typeDescriptor -> typeDescriptor instanceof FunctionTypeSymbol)
                                .map(typeDescriptor -> (FunctionTypeSymbol) typeDescriptor)
                                .filter(functionTypeSymbol -> functionTypeSymbol.params().isPresent())
                                .map(functionTypeSymbol -> functionTypeSymbol.params().get())
                                .flatMap(Collection::stream)
                                .collect(Collectors.toList()))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
            }
        }
        return parameterSymbols;
    }

    public static class InlayHintMethodTypeFinder extends NodeVisitor {
        List<NonTerminalNode> methodNameList = new ArrayList<>();

        public InlayHintMethodTypeFinder() {
        }

        public List<NonTerminalNode> getMethodNameList() {
            return methodNameList;
        }

        @Override
        public void visit(FunctionCallExpressionNode functionCallExpressionNode) {
            methodNameList.add(functionCallExpressionNode);
        }

        @Override
        public void visit(MethodCallExpressionNode methodCallExpressionNode) {
            methodNameList.add(methodCallExpressionNode);
        }
    }

    public static class InlayHintArgumentTypeFinder extends NodeVisitor {
        List<NonTerminalNode> argumentList = new ArrayList<>();

        List<LineRange> commaList = new ArrayList<>();

        public InlayHintArgumentTypeFinder() {
        }

        public List<NonTerminalNode> getArgumentList() {
            return argumentList;
        }

        public List<LineRange> getCommaList() {
            return commaList;
        }

        @Override
        public void visit(FunctionCallExpressionNode functionCallExpressionNode) {
            commaList.add(functionCallExpressionNode.openParenToken().lineRange());
            for (Node child : functionCallExpressionNode.children()) {
                if (child.kind() == SyntaxKind.COMMA_TOKEN) {
                    commaList.add(child.lineRange());
                } else {
                    child.accept(this);
                }
            }
        }

        @Override
        public void visit(MethodCallExpressionNode methodCallExpressionNode) {
            commaList.add(methodCallExpressionNode.openParenToken().lineRange());
            for (Node child : methodCallExpressionNode.children()) {
                if (child.kind() == SyntaxKind.COMMA_TOKEN) {
                    commaList.add(child.lineRange());
                } else {
                    child.accept(this);
                }
            }
        }

        @Override
        public void visit(PositionalArgumentNode positionalArgumentNode) {
            argumentList.add(positionalArgumentNode);
        }
    }
}
