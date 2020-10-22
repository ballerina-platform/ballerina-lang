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

import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.types.ObjectTypeDescriptor;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.QNameReferenceUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.SortingUtil;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.ballerina.compiler.api.symbols.SymbolKind.MODULE;
import static io.ballerina.compiler.api.symbols.SymbolKind.VARIABLE;
import static org.ballerinalang.langserver.completions.util.SortingUtil.genSortText;
import static org.ballerinalang.langserver.completions.util.SortingUtil.genSortTextForInitContextItem;
import static org.ballerinalang.langserver.completions.util.SortingUtil.genSortTextForModule;
import static org.ballerinalang.langserver.completions.util.SortingUtil.getAssignableType;

/**
 * Completion provider for {@link ListenerDeclarationNode} context.
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class ListenerDeclarationNodeContext extends AbstractCompletionProvider<ListenerDeclarationNode> {

    public ListenerDeclarationNodeContext() {
        super(ListenerDeclarationNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, ListenerDeclarationNode node)
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
            completionItems.addAll(this.initializerItems(context, listenerNode.get()));
            this.sort(context, node, completionItems, ContextScope.INITIALIZER);
        }

        return completionItems;
    }

    @Override
    public void sort(LSContext context, ListenerDeclarationNode node, List<LSCompletionItem> completionItems,
                     Object... metaData) {
        super.sort(context, node, completionItems, metaData);
        if (metaData.length < 1 || !(metaData[0] instanceof ContextScope)) {
            super.sort(context, node, completionItems);
        }

        ContextScope scope = (ContextScope) metaData[0];

        if (scope == ContextScope.TYPE_DESC) {
            for (LSCompletionItem lsItem : completionItems) {
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
            for (LSCompletionItem lsItem : completionItems) {
                CompletionItem cItem = lsItem.getCompletionItem();
                Optional<TypeSymbol> assignableType = getAssignableType(context, node);
                if (assignableType.isEmpty()) {
                    super.sort(context, node, completionItems);
                    continue;
                }
                String sortText = genSortTextForInitContextItem(context, lsItem,
                        (assignableType.get().typeDescriptor()).kind());
                cItem.setSortText(sortText);
            }
        }
    }

    private List<LSCompletionItem> typeDescriptorContextItems(LSContext context) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        /*
        Type descriptor is null in the following use-case
        (1) public listener ht<cursor>
        because the type descriptor is optional as per the grammar
         */
        NonTerminalNode nodeAtCursor = context.get(CompletionKeys.NODE_AT_CURSOR_KEY);
        if (this.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            String modulePrefix = QNameReferenceUtil.getAlias((QualifiedNameReferenceNode) nodeAtCursor);
            completionItems.addAll(listenersInModule(context, modulePrefix));
        } else {
            completionItems.addAll(listenersAndPackagesItems(context));
        }
        return completionItems;
    }

    private List<LSCompletionItem> listenersAndPackagesItems(LSContext context) {
        List<LSCompletionItem> completionItems = new ArrayList<>(this.getModuleCompletionItems(context));
        List<Symbol> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        List<Symbol> listeners = visibleSymbols.stream()
                .filter(SymbolUtil::isListener)
                .collect(Collectors.toList());
        completionItems.addAll(this.getCompletionItemList(listeners, context));

        return completionItems;
    }

    private List<LSCompletionItem> listenersInModule(LSContext context, String modulePrefix) {
        ArrayList<LSCompletionItem> completionItems = new ArrayList<>();
        List<Symbol> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        Optional<ModuleSymbol> moduleSymbol = visibleSymbols.stream()
                .filter(symbol -> symbol.kind() == MODULE && symbol.name().equals(modulePrefix))
                .map(symbol -> (ModuleSymbol) symbol)
                .findAny();

        if (moduleSymbol.isEmpty()) {
            return completionItems;
        }

        List<TypeSymbol> listeners = moduleSymbol.get().typeDefinitions().stream()
                .filter(SymbolUtil::isListener)
                .collect(Collectors.toList());
        completionItems.addAll(this.getCompletionItemList(listeners, context));

        return completionItems;
    }

    private List<LSCompletionItem> initializerItems(LSContext context, ListenerDeclarationNode listenerNode) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        List<Symbol> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        
        /*
        Supports the following
        (1) public listener mod:Listener test = <cursor>
        (2) public listener mod:Listener test = a<cursor>
         */
        Optional<ObjectTypeDescriptor> objectTypeDesc = getListenerTypeDesc(context, listenerNode);
        List<Symbol> filteredList = visibleSymbols.stream()
                .filter(symbol -> symbol.kind() == VARIABLE)
                .collect(Collectors.toList());
        completionItems.addAll(this.getCompletionItemList(filteredList, context));
        completionItems.addAll(this.getModuleCompletionItems(context));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_NEW.get()));
        objectTypeDesc.ifPresent(tDesc -> completionItems.add(this.getImplicitNewCompletionItem(tDesc, context)));

        return completionItems;
    }

    private Optional<ListenerDeclarationNode> listenerNode(LSContext context) {
        NonTerminalNode node = context.get(CompletionKeys.NODE_AT_CURSOR_KEY);
        while (node.kind() != SyntaxKind.LISTENER_DECLARATION && node.kind() != SyntaxKind.MODULE_PART) {
            node = node.parent();
        }

        if (node.kind() == SyntaxKind.LISTENER_DECLARATION) {
            return Optional.of((ListenerDeclarationNode) node);
        }

        return Optional.empty();
    }

    private boolean withinTypeDescContext(LSContext context, ListenerDeclarationNode node) {
        Position position = context.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        Token varName = node.variableName();
        Token listenerKW = node.listenerKeyword();

        if (listenerKW.isMissing()) {
            return false;
        }

        LineRange listenerKWRange = listenerKW.lineRange();

        return listenerKWRange.endLine().offset() < position.getCharacter()
                && (varName.isMissing()
                || (varName.lineRange().startLine().line() == position.getLine()
                && varName.lineRange().startLine().offset() > position.getCharacter())
                || (varName.lineRange().startLine().line() > position.getLine())
                || (varName.lineRange().endLine().offset() == position.getCharacter()
                && node.typeDescriptor() == null));
    }

    private boolean withinInitializerContext(LSContext context, ListenerDeclarationNode node) {
        Node equalsToken = node.equalsToken();
        if (equalsToken.isMissing()) {
            return false;
        }
        Position cursorPos = context.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        LineRange lineRange = equalsToken.lineRange();
        return (cursorPos.getLine() == lineRange.startLine().line()
                && cursorPos.getCharacter() > lineRange.startLine().offset())
                || (cursorPos.getLine() > lineRange.startLine().line());
    }

    private Optional<ObjectTypeDescriptor> getListenerTypeDesc(LSContext context, ListenerDeclarationNode node) {
        Node typeDescriptor = node.typeDescriptor();
        Optional<TypeSymbol> typeSymbol = Optional.empty();
        List<Symbol> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        if (typeDescriptor.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            QualifiedNameReferenceNode nameReferenceNode = (QualifiedNameReferenceNode) typeDescriptor;
            Optional<ModuleSymbol> moduleSymbol = CommonUtil.searchModuleForAlias(context,
                    QNameReferenceUtil.getAlias(nameReferenceNode));

            if (moduleSymbol.isEmpty()) {
                return Optional.empty();
            }
            typeSymbol = moduleSymbol.get().typeDefinitions().stream()
                    .filter(type -> SymbolUtil.isListener(type)
                            && type.name().equals(nameReferenceNode.identifier().text()))
                    .findAny();

        } else if (typeDescriptor.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            SimpleNameReferenceNode nameReferenceNode = (SimpleNameReferenceNode) typeDescriptor;
            typeSymbol = visibleSymbols.stream()
                    .filter(visibleSymbol -> SymbolUtil.isListener(visibleSymbol)
                            && visibleSymbol.name().equals(nameReferenceNode.name().text()))
                    .map(visibleSymbol -> (TypeSymbol) visibleSymbol)
                    .findAny();
        }

        return typeSymbol.map(symbol -> (ObjectTypeDescriptor) CommonUtil.getRawType(symbol.typeDescriptor()));
    }

    private enum ContextScope {
        TYPE_DESC,
        INITIALIZER,
        OTHER
    }
}
