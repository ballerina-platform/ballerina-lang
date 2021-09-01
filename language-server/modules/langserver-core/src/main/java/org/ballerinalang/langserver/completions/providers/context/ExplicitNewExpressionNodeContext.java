/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.providers.context;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Completion provider for {@link ExplicitNewExpressionNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ExplicitNewExpressionNodeContext extends AbstractCompletionProvider<ExplicitNewExpressionNode> {

    public ExplicitNewExpressionNodeContext() {
        super(ExplicitNewExpressionNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, ExplicitNewExpressionNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        if (this.withinArgs(context, node)) {
            /*
            Covers
            lhs = new module:Client(<cursor>)
             */
            completionItems.addAll(this.getCompletionsWithinArgs(context, node));
        } else if (QNameReferenceUtil.onQualifiedNameIdentifier(context, context.getNodeAtCursor())) {
            /*
            Supports the following
            (1) new module:<cursor>
            (2) new module:a<cursor>
             */
            QualifiedNameReferenceNode referenceNode = (QualifiedNameReferenceNode) context.getNodeAtCursor();
            String moduleName = QNameReferenceUtil.getAlias(referenceNode);
            Optional<ModuleSymbol> module = CommonUtil.searchModuleForAlias(context, moduleName);
            if (module.isEmpty()) {
                return completionItems;
            }
            module.get().allSymbols().stream()
                    .filter(this.getSymbolFilterPredicate(node))
                    .forEach(symbol -> {
                        Optional<LSCompletionItem> cItem = this.getExplicitNewCompletionItem(symbol, context);
                        cItem.ifPresent(completionItems::add);
                    });
        } else {
            /*
            Supports the following
            (1) new <cursor>
            (2) new a<cursor>
             */
            List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
            visibleSymbols.stream()
                    .filter(getSymbolFilterPredicate(node))
                    .forEach(symbol -> {
                        Optional<LSCompletionItem> cItem = this.getExplicitNewCompletionItem(symbol, context);
                        cItem.ifPresent(completionItems::add);
                    });

            completionItems.addAll(this.getModuleCompletionItems(context));
        }
        this.sort(context, node, completionItems);

        return completionItems;
    }

    private Optional<ClassSymbol> getClassSymbol(BallerinaCompletionContext context) {
        Optional<TypeSymbol> contextType = context.getContextType();
        if (contextType.isEmpty()) {
            return Optional.empty();
        }
        TypeSymbol rawType = CommonUtil.getRawType(contextType.get());
        if (rawType.kind() == SymbolKind.CLASS) {
            return Optional.of((ClassSymbol) rawType);
        }

        return Optional.empty();
    }

    private Predicate<Symbol> getSymbolFilterPredicate(ExplicitNewExpressionNode node) {
        if (node.parent().kind() == SyntaxKind.SERVICE_DECLARATION
                || node.parent().kind() == SyntaxKind.LISTENER_DECLARATION) {
            return symbol -> SymbolUtil.isClassDefinition(symbol) && SymbolUtil.isListener(symbol);
        }

        return symbol -> SymbolUtil.isClassDefinition(symbol) || (SymbolUtil.isOfType(symbol, TypeDescKind.STREAM));
    }

    private boolean withinArgs(BallerinaCompletionContext context, ExplicitNewExpressionNode node) {
        ParenthesizedArgList parenthesizedArgList = node.parenthesizedArgList();
        int cursor = context.getCursorPositionInTree();

        return cursor > parenthesizedArgList.openParenToken().textRange().startOffset()
                && cursor < parenthesizedArgList.closeParenToken().textRange().endOffset();
    }

    private List<LSCompletionItem> getCompletionsWithinArgs(BallerinaCompletionContext ctx,
                                                            ExplicitNewExpressionNode node) {
        NonTerminalNode nodeAtCursor = ctx.getNodeAtCursor();
        if (QNameReferenceUtil.onQualifiedNameIdentifier(ctx, nodeAtCursor)) {
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            return this.getCompletionItemList(QNameReferenceUtil.getExpressionContextEntries(ctx, qNameRef), ctx);
        }
        List<LSCompletionItem> completionItems = new ArrayList<>();
        completionItems.addAll(this.expressionCompletions(ctx));
        if (!withInNamedArgAssignmentContext(ctx)) {
            completionItems.addAll(getNamedArgExpressionCompletionItems(ctx, node));
        }
        return this.expressionCompletions(ctx);
    }

    private List<LSCompletionItem> getNamedArgExpressionCompletionItems(BallerinaCompletionContext context,
                                                                        ExplicitNewExpressionNode node) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        Optional<SemanticModel> semanticModel = context.currentSemanticModel();
        if (semanticModel.isEmpty()) {
            return completionItems;
        }
        Optional<Symbol> symbol = context.currentSemanticModel().get().symbol(node.typeDescriptor());
        if (symbol.isEmpty()) {
            return completionItems;
        }
        ClassSymbol classSymbol;
        if (symbol.get().kind() == SymbolKind.TYPE) {
            TypeSymbol typeSymbol = CommonUtil.getRawType(((TypeReferenceTypeSymbol) symbol.get()).typeDescriptor());
            if (typeSymbol.kind() != SymbolKind.CLASS) {
                return completionItems;
            }
            classSymbol = (ClassSymbol) typeSymbol;
        } else if (symbol.get().kind() == SymbolKind.CLASS) {
            classSymbol = (ClassSymbol) symbol.get();
        } else {
            return completionItems;
        }

        Optional<MethodSymbol> methodSymbol = classSymbol.initMethod();
        if (methodSymbol.isEmpty()) {
            return completionItems;
        }

        FunctionTypeSymbol functionTypeSymbol = methodSymbol.get().typeDescriptor();
        Optional<List<ParameterSymbol>> params = functionTypeSymbol.params();
        if (params.isEmpty()) {
            return completionItems;
        }
        List<String> existingNamedArgs = node.parenthesizedArgList().arguments().stream()
                .filter(arg -> arg.kind() == SyntaxKind.NAMED_ARG)
                .map(arg -> ((NamedArgumentNode) arg).argumentName().name().text()).collect(Collectors.toList());
        return getNamedArgCompletionItems(context, params.get(), existingNamedArgs);
    }
}
