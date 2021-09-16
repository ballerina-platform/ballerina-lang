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
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.common.utils.completion.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.SymbolCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.SortingUtil;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.compiler.api.symbols.SymbolKind.FUNCTION;
import static io.ballerina.compiler.api.symbols.SymbolKind.METHOD;
import static io.ballerina.compiler.api.symbols.SymbolKind.MODULE;
import static io.ballerina.compiler.api.symbols.SymbolKind.VARIABLE;
import static org.ballerinalang.langserver.completions.util.SortingUtil.genSortText;
import static org.ballerinalang.langserver.completions.util.SortingUtil.genSortTextForModule;

/**
 * Completion provider for {@link ListenerDeclarationNode} context.
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ListenerDeclarationNodeContext extends AbstractCompletionProvider<ListenerDeclarationNode> {

    private static final String INIT_METHOD_NAME = "init";

    public ListenerDeclarationNodeContext() {
        super(ListenerDeclarationNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, ListenerDeclarationNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();

        Optional<ListenerDeclarationNode> listenerNode = listenerNode(context);
        if (listenerNode.isEmpty()) {
            return completionItems;
        }
        if (withinTypeDescContext(context, listenerNode.get())) {
            completionItems.addAll(this.typeDescriptorContextItems(context));
            this.sort(context, node, completionItems, ContextScope.TYPE_DESC);
        } else if (withinInitializerContext(context, listenerNode.get())) {
            completionItems.addAll(this.expressionCompletions(context, listenerNode.get()));
            this.sort(context, node, completionItems, ContextScope.INITIALIZER);
        }

        return completionItems;
    }

    @Override
    public void sort(BallerinaCompletionContext context, ListenerDeclarationNode node, List<LSCompletionItem> cmpItems,
                     Object... metaData) {
        super.sort(context, node, cmpItems, metaData);
        if (metaData.length < 1 || !(metaData[0] instanceof ContextScope)) {
            super.sort(context, node, cmpItems);
        }

        ContextScope scope = (ContextScope) metaData[0];

        if (scope == ContextScope.TYPE_DESC) {
            for (LSCompletionItem lsItem : cmpItems) {
                CompletionItem cItem = lsItem.getCompletionItem();
                String sortText;
                if (SortingUtil.isTypeCompletionItem(lsItem)) {
                    sortText = genSortText(1);
                } else if (SortingUtil.isModuleCompletionItem(lsItem)) {
                    sortText = genSortText(2) + genSortTextForModule(context, lsItem);
                } else {
                    sortText = genSortText(3);
                }
                cItem.setSortText(sortText);
            }
            return;
        }

        if (scope == ContextScope.INITIALIZER) {
            Optional<TypeSymbol> ctxType = context.getContextType();
            for (LSCompletionItem lsItem : cmpItems) {
                CompletionItem cItem = lsItem.getCompletionItem();
                int rank = -1;
                if (this.isValidSymbolCompletionItem(lsItem)) {
                    SymbolCompletionItem symbolCItem = (SymbolCompletionItem) lsItem;
                    /*
                    In the validity check, we check for the empty symbol. Hence do not use the isPresent check here 
                     */
                    Symbol symbol = symbolCItem.getSymbol().get();
                    if (symbol.kind() == VARIABLE && ctxType.isPresent() &&
                            SymbolUtil.getTypeDescriptor(symbol).get().assignableTo(ctxType.get())) {
                        /*
                        When there is an explicitly defined type descriptor
                        Eg: public listener mod:Listener l = ...
                         */
                        rank = 1;
                    } else if (symbol.kind() == VARIABLE
                            && ((VariableSymbol) symbol).qualifiers().contains(Qualifier.LISTENER)) {
                        /*
                        When there is no explicitly defined type descriptor
                        Eg: public listener l = ...
                         */
                        rank = 2;
                    } else if (symbol.kind() == METHOD && symbol.getName().get().equals(INIT_METHOD_NAME)) {
                        // new() snippet is generated
                        rank = 3;
                    } else if (symbol.kind() == FUNCTION && ctxType.isPresent()) {
                        FunctionTypeSymbol functionTypeSymbol = ((FunctionSymbol) symbol).typeDescriptor();
                        Optional<TypeSymbol> typeSymbol = functionTypeSymbol.returnTypeDescriptor();
                        if (typeSymbol.isPresent() && typeSymbol.get().assignableTo(ctxType.get())) {
                            rank = 4;
                        }
                    }
                } else if (Snippet.KW_NEW.equals(lsItem)) {
                    // new keyword completion item
                    rank = 5;
                }

                rank = rank < 0 ? SortingUtil.toRank(context, lsItem, 5) : rank;
                cItem.setSortText(genSortText(rank));
            }
        }
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, ListenerDeclarationNode node) {
        int cursor = context.getCursorPositionInTree();
        Token listenerKeyword = node.listenerKeyword();

        // Added +1 since the completion is valid after listener <cursor>
        return !listenerKeyword.isMissing() && listenerKeyword.textRange().endOffset() + 1 <= cursor;
    }

    private List<LSCompletionItem> typeDescriptorContextItems(BallerinaCompletionContext context) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        /*
        Type descriptor is null in the following use-case
        (1) public listener ht<cursor>
        because the type descriptor is optional as per the grammar
         */
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        if (QNameReferenceUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            String modulePrefix = QNameReferenceUtil.getAlias((QualifiedNameReferenceNode) nodeAtCursor);
            completionItems.addAll(listenersInModule(context, modulePrefix));
        } else {
            completionItems.addAll(listenersAndPackagesItems(context));
        }
        return completionItems;
    }

    private List<LSCompletionItem> listenersAndPackagesItems(BallerinaCompletionContext context) {
        List<LSCompletionItem> completionItems = new ArrayList<>(this.getModuleCompletionItems(context));
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        List<Symbol> listeners = visibleSymbols.stream()
                .filter(SymbolUtil::isListener)
                .collect(Collectors.toList());
        completionItems.addAll(this.getCompletionItemList(listeners, context));

        return completionItems;
    }

    private List<LSCompletionItem> listenersInModule(BallerinaCompletionContext context, String modulePrefix) {
        ArrayList<LSCompletionItem> completionItems = new ArrayList<>();
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        Optional<ModuleSymbol> moduleSymbol = visibleSymbols.stream()
                .filter(symbol -> symbol.kind() == MODULE && symbol.getName().get().equals(modulePrefix))
                .map(symbol -> (ModuleSymbol) symbol)
                .findAny();

        if (moduleSymbol.isEmpty()) {
            return completionItems;
        }

        ModuleSymbol module = moduleSymbol.get();
        Stream<Symbol> classesAndTypeDefs = Stream.concat(module.classes().stream(), module.typeDefinitions().stream());
        List<Symbol> listeners = classesAndTypeDefs
                .filter(SymbolUtil::isListener)
                .collect(Collectors.toList());
        completionItems.addAll(this.getCompletionItemList(listeners, context));

        return completionItems;
    }

    private Optional<ListenerDeclarationNode> listenerNode(BallerinaCompletionContext context) {
        NonTerminalNode node = context.getNodeAtCursor();
        while (node.kind() != SyntaxKind.LISTENER_DECLARATION && node.kind() != SyntaxKind.MODULE_PART) {
            node = node.parent();
        }

        if (node.kind() == SyntaxKind.LISTENER_DECLARATION) {
            return Optional.of((ListenerDeclarationNode) node);
        }

        return Optional.empty();
    }

    private boolean withinTypeDescContext(BallerinaCompletionContext context, ListenerDeclarationNode node) {
        int cursor = context.getCursorPositionInTree();
        Token varName = node.variableName();
        Token listenerKW = node.listenerKeyword();
        Optional<TypeDescriptorNode> tDesc = node.typeDescriptor();

        if (listenerKW.isMissing()) {
            return false;
        }

        return cursor >= listenerKW.textRange().startOffset() + 1
                && ((tDesc.isEmpty() && varName.isMissing())
                || (tDesc.isEmpty() && !varName.isMissing() && cursor <= varName.textRange().endOffset())
                || (tDesc.isPresent() && cursor <= tDesc.get().textRange().endOffset()));
    }

    private boolean withinInitializerContext(BallerinaCompletionContext context, ListenerDeclarationNode node) {
        Node equalsToken = node.equalsToken();
        if (equalsToken.isMissing()) {
            return false;
        }
        Position cursorPos = context.getCursorPosition();
        LineRange lineRange = equalsToken.lineRange();
        return (cursorPos.getLine() == lineRange.startLine().line()
                && cursorPos.getCharacter() > lineRange.startLine().offset())
                || (cursorPos.getLine() > lineRange.startLine().line());
    }

    private Optional<ClassSymbol> getListenerTypeDesc(BallerinaCompletionContext context,
                                                      ListenerDeclarationNode node) {
        Node typeDescriptor = node.typeDescriptor().orElse(null);
        Optional<ClassSymbol> typeSymbol = Optional.empty();
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        if (typeDescriptor == null) {
            return typeSymbol;
        }
        if (typeDescriptor.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            QualifiedNameReferenceNode nameReferenceNode = (QualifiedNameReferenceNode) typeDescriptor;
            Optional<ModuleSymbol> moduleSymbol = CommonUtil.searchModuleForAlias(context,
                    QNameReferenceUtil.getAlias(nameReferenceNode));

            if (moduleSymbol.isEmpty()) {
                return Optional.empty();
            }
            ModuleSymbol module = moduleSymbol.get();
            Stream<Symbol> objsAndClasses = Stream.concat(module.classes().stream(), module.typeDefinitions().stream());
            typeSymbol = objsAndClasses
                    .filter(type -> SymbolUtil.isListener(type)
                            && Objects.equals(type.getName().orElse(null), nameReferenceNode.identifier().text()))
                    .map(symbol -> (ClassSymbol) symbol)
                    .findAny();

        } else if (typeDescriptor.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            SimpleNameReferenceNode nameReferenceNode = (SimpleNameReferenceNode) typeDescriptor;
            typeSymbol = visibleSymbols.stream()
                    .filter(visibleSymbol -> SymbolUtil.isListener(visibleSymbol)
                            && Objects.equals(visibleSymbol.getName().orElse(null), nameReferenceNode.name().text()))
                    .map(symbol -> (ClassSymbol) symbol)
                    .findAny();
        }

        return typeSymbol;
    }

    @Override
    protected List<LSCompletionItem> expressionCompletions(BallerinaCompletionContext context,
                                                           ListenerDeclarationNode listenerNode) {
        NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
        if (QNameReferenceUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            /*
            Supports the following
            (1) public listener mod:Listener test = module:<cursor>
            (2) public listener mod:Listener test = module:a<cursor>
             */
            QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) nodeAtCursor;
            List<Symbol> ctxEntries = QNameReferenceUtil.getExpressionContextEntries(context, qNameRef);

            return this.getCompletionItemList(ctxEntries, context);
        }

        List<LSCompletionItem> completionItems = new ArrayList<>();
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        /*
        Supports the following
        (1) public listener mod:Listener test = <cursor>
        (2) public listener mod:Listener test = a<cursor>
         */
        Optional<ClassSymbol> objectTypeDesc = getListenerTypeDesc(context, listenerNode);
        List<Symbol> filteredList = visibleSymbols.stream()
                .filter(symbol -> symbol.kind() == VARIABLE || symbol.kind() == FUNCTION)
                .collect(Collectors.toList());
        completionItems.addAll(this.getCompletionItemList(filteredList, context));
        completionItems.addAll(this.getModuleCompletionItems(context));
        objectTypeDesc.ifPresent(tDesc -> completionItems.add(this.getImplicitNewCItemForClass(tDesc, context)));

        /*
        Get the keyword completion items. Here we add the keywords associated with the allowed expressions.
        The table and stream keywords (for query expressions) will be suggested via the module items
         */
        List<Snippet> keywords = Arrays.asList(Snippet.KW_NEW, Snippet.KW_CHECK, Snippet.KW_CHECK_PANIC,
                Snippet.KW_TRAP);
        for (Snippet keyword : keywords) {
            completionItems.add(new SnippetCompletionItem(context, keyword.get()));
        }

        return completionItems;
    }

    private boolean isValidSymbolCompletionItem(LSCompletionItem lsCompletionItem) {
        return lsCompletionItem.getType() == LSCompletionItem.CompletionItemType.SYMBOL
                && ((SymbolCompletionItem) lsCompletionItem).getSymbol().isPresent();
    }

    private enum ContextScope {
        TYPE_DESC,
        INITIALIZER,
        OTHER
    }
}
