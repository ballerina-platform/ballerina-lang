/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.providers.context;

import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.SymbolCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.SortingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Generic completion resolver for the Block Nodes.
 *
 * @param <T> block node type
 * @since 2.0.0
 */
public abstract class VariableDeclarationProvider<T extends Node> extends AbstractCompletionProvider<T> {

    public VariableDeclarationProvider(Class<T> attachmentPoint) {
        super(attachmentPoint);
    }

    @Override
    public void sort(BallerinaCompletionContext context, T node, List<LSCompletionItem> completionItems) {
        Optional<Symbol> symbolAtCursor = context.currentSemanticModel()
                .flatMap(semanticModel -> semanticModel.symbol(node));

        Optional<TypeSymbol> typeSymbolAtCursor = symbolAtCursor.flatMap(SymbolUtil::getTypeDescriptor);

        if (typeSymbolAtCursor.isEmpty()) {
            super.sort(context, node, completionItems);
            return;
        }

        TypeSymbol typeSymbol = typeSymbolAtCursor.get();
        completionItems.forEach(completionItem -> {
            int rank = SortingUtil.toRank(completionItem, 1);

            // If a completion item is a symbol and is assignable to the variable at left hand side (and is not the 
            // same as variable), this assigns the highest rank to such variables and methods.
            if (completionItem.getType() == LSCompletionItem.CompletionItemType.SYMBOL) {
                SymbolCompletionItem symbolCompletionItem = (SymbolCompletionItem) completionItem;

                Optional<TypeSymbol> completionItemType = symbolCompletionItem.getSymbol().isEmpty() ?
                        Optional.empty() : SymbolUtil.getTypeDescriptor(symbolCompletionItem.getSymbol().get());
                if (completionItemType.isPresent() && completionItemType.get() instanceof FunctionTypeSymbol) {
                    completionItemType = ((FunctionTypeSymbol) completionItemType.get()).returnTypeDescriptor();
                }

                // TODO: Remove the symbol equality check after #25607
                if (symbolCompletionItem.getSymbol().isPresent() && completionItemType.isPresent() &&
                        !symbolCompletionItem.getSymbol().get().equals(symbolAtCursor.get()) &&
                        completionItemType.get().assignableTo(typeSymbol)) {
                    rank = 1;
                }
            }

            completionItem.getCompletionItem().setSortText(SortingUtil.genSortText(rank));
        });
    }

    protected List<LSCompletionItem> initializerContextCompletions(BallerinaCompletionContext context,
                                                                   TypeDescriptorNode typeDsc) {
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        if (QNameReferenceUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            /*
            Captures the following cases
            (1) [module:]TypeName c = module:<cursor>
            (2) [module:]TypeName c = module:a<cursor>
             */
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            Predicate<Symbol> filter = symbol -> symbol instanceof VariableSymbol
                    || symbol.kind() == SymbolKind.FUNCTION
                    || symbol.kind() == SymbolKind.TYPE_DEFINITION
                    || symbol.kind() == SymbolKind.CLASS;
            List<Symbol> moduleContent = QNameReferenceUtil.getModuleContent(context, qNameRef, filter);
            return this.getCompletionItemList(moduleContent, context);
        }
        
        /*
        Captures the following cases
        (1) [module:]TypeName c = <cursor>
        (2) [module:]TypeName c = a<cursor>
         */
        List<LSCompletionItem> completionItems = new ArrayList<>();
        completionItems.addAll(this.actionKWCompletions(context));
        completionItems.addAll(this.expressionCompletions(context));
        completionItems.addAll(getNewExprCompletionItems(context, typeDsc));
        if (withinTransactionStatementNode(context)) {
            completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_COMMIT.get()));
        }
        return completionItems;
    }

    private List<LSCompletionItem> getNewExprCompletionItems(BallerinaCompletionContext context,
                                                             TypeDescriptorNode typeDescriptorNode) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        Optional<ClassSymbol> classSymbol;
        if (QNameReferenceUtil.onQualifiedNameIdentifier(context, typeDescriptorNode)) {
            String modulePrefix = QNameReferenceUtil.getAlias(((QualifiedNameReferenceNode) typeDescriptorNode));
            Optional<ModuleSymbol> module = CommonUtil.searchModuleForAlias(context, modulePrefix);
            if (module.isEmpty()) {
                return completionItems;
            }
            String identifier = ((QualifiedNameReferenceNode) typeDescriptorNode).identifier().text();
            ModuleSymbol moduleSymbol = module.get();
            Stream<Symbol> classesAndTypes = Stream.concat(moduleSymbol.classes().stream(),
                    moduleSymbol.typeDefinitions().stream());
            classSymbol = classesAndTypes
                    .filter(typeSymbol -> SymbolUtil.isClass(typeSymbol)
                            && Objects.equals(typeSymbol.getName().orElse(null), identifier))
                    .map(SymbolUtil::getTypeDescForClassSymbol)
                    .findAny();
        } else if (typeDescriptorNode.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            String identifier = ((SimpleNameReferenceNode) typeDescriptorNode).name().text();
            classSymbol = visibleSymbols.stream()
                    .filter(symbol -> SymbolUtil.isClass(symbol)
                            && Objects.equals(symbol.getName().orElse(null), identifier))
                    .map(SymbolUtil::getTypeDescForClassSymbol)
                    .findAny();
        } else {
            classSymbol = Optional.empty();
        }

        classSymbol.ifPresent(typeDesc -> completionItems.add(this.getImplicitNewCompletionItem(typeDesc, context)));

        return completionItems;
    }

    private boolean withinTransactionStatementNode(BallerinaCompletionContext context) {
        NonTerminalNode evalNode = context.getNodeAtCursor().parent();
        boolean withinTransaction = false;

        while (evalNode != null) {
            if (evalNode.kind() == SyntaxKind.TRANSACTION_STATEMENT) {
                withinTransaction = true;
                break;
            }
            evalNode = evalNode.parent();
        }
        return withinTransaction;
    }
}
