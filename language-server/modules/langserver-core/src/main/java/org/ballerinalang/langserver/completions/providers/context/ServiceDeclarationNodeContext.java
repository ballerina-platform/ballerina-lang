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

import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.SymbolCompletionItem;
import org.ballerinalang.langserver.completions.providers.context.util.ModulePartNodeContextUtil;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.SortingUtil;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.ballerinalang.langserver.completions.util.SortingUtil.genSortText;
import static org.ballerinalang.langserver.completions.util.SortingUtil.genSortTextForModule;

/**
 * Completion provider for {@link ServiceDeclarationNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class ServiceDeclarationNodeContext extends ObjectBodiedNodeContextProvider<ServiceDeclarationNode> {

    public ServiceDeclarationNodeContext() {
        super(ServiceDeclarationNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, ServiceDeclarationNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        ServiceContext cursorContext;

        if (this.onMemberContext(context, node)) {
            completionItems.addAll(this.getBodyContextItems(context, node));
            cursorContext = ServiceContext.MEMBERS;
        } else if (this.onTypeDescContext(context, node)) {
            /*
            Covers the following cases
            Eg:
            (1) service m<cursor>
            function ...
            (2) service mod:<cursor>
            function ...
             */
            if (QNameRefCompletionUtil.onQualifiedNameIdentifier(context, context.getTokenAtCursor().parent())) {
                QualifiedNameReferenceNode qNameRef = (QualifiedNameReferenceNode) context.getTokenAtCursor().parent();
                List<Symbol> moduleContent = QNameRefCompletionUtil.getModuleContent(context, qNameRef,
                        ModulePartNodeContextUtil.serviceTypeDescPredicate());

                completionItems.addAll(this.getCompletionItemList(moduleContent, context));
            } else {
                List<Symbol> typeDescs = ModulePartNodeContextUtil.serviceTypeDescContextSymbols(context);
                completionItems.addAll(this.getCompletionItemList(typeDescs, context));
                completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_OBJECT_TYPE_DESC_SNIPPET.get()));
                completionItems.addAll(this.getModuleCompletionItems(context));
                if (node.onKeyword().isMissing()) {
                    /*
                    Covers the following
                    Eg: service <cursor> ..
                    
                    and skip the following
                    Eg: service <cursor> on ...
                     */
                    completionItems.add(new SnippetCompletionItem(context, Snippet.KW_ON.get()));
                }
            }
            cursorContext = ServiceContext.TYPE_DESC;
        } else if (onExpressionContext(context, node)) {
            /*
            Covers the following case
            (1) service typedesc on <cursor>
            (2) service typedesc on l<cursor>
            (3) service typedesc on mod:<cursor>
             */
            Predicate<Symbol> predicate = symbol -> symbol instanceof VariableSymbol
                    && ((VariableSymbol) symbol).qualifiers().contains(Qualifier.LISTENER);
            NonTerminalNode nodeAtCursor = context.getNodeAtCursor();
            if (QNameRefCompletionUtil.onQualifiedNameIdentifier(context, nodeAtCursor)) {
                // Covers the use-case 3
                List<Symbol> moduleContent = QNameRefCompletionUtil.getModuleContent(context,
                        (QualifiedNameReferenceNode) nodeAtCursor, predicate);
                completionItems.addAll(this.getCompletionItemList(moduleContent, context));
            } else {
                List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
                List<Symbol> listeners = visibleSymbols.stream()
                        .filter(predicate)
                        .collect(Collectors.toList());
                completionItems.addAll(this.getCompletionItemList(listeners, context));
                completionItems.addAll(this.getModuleCompletionItems(context));
                completionItems.add(new SnippetCompletionItem(context, Snippet.KW_NEW.get()));
            }
            cursorContext = ServiceContext.EXPR_LIST;
        } else {
            /*
            Covers the following cases
            Eg:
            (1) service / m<cursor>
            function ...
             */
            completionItems.add(new SnippetCompletionItem(context, Snippet.KW_ON.get()));
            cursorContext = ServiceContext.OTHER;
        }
        this.sort(context, node, completionItems, cursorContext);

        return completionItems;
    }

    private boolean onTypeDescContext(BallerinaCompletionContext context, ServiceDeclarationNode node) {
        int cursor = context.getCursorPositionInTree();
        Token onKeyword = node.onKeyword();
        Token serviceKeyword = node.serviceKeyword();
        Optional<TypeDescriptorNode> tDesc = node.typeDescriptor();
        NodeList<Node> resourcePath = node.absoluteResourcePath();
        boolean afterResourcePath = resourcePath.isEmpty()
                || cursor >= resourcePath.get(resourcePath.size() - 1).textRange().endOffset() + 1;
        boolean afterTypeDesc = tDesc.isEmpty() || cursor < tDesc.get().textRange().endOffset() + 1;
        boolean beforeOnKw = (onKeyword.isMissing() || onKeyword.textRange().startOffset() > cursor);

        return cursor > serviceKeyword.textRange().endOffset() && beforeOnKw && afterTypeDesc && afterResourcePath;
    }

    private boolean onMemberContext(BallerinaCompletionContext context, ServiceDeclarationNode node) {
        int cursor = context.getCursorPositionInTree();
        Token openBrace = node.openBraceToken();
        Token closeBrace = node.closeBraceToken();

        if (openBrace.isMissing() || closeBrace.isMissing()) {
            return false;
        }

        return cursor > openBrace.textRange().startOffset() && cursor < closeBrace.textRange().endOffset();
    }

    private boolean onExpressionContext(BallerinaCompletionContext context, ServiceDeclarationNode node) {
        int cursor = context.getCursorPositionInTree();
        Token onKeyword = node.onKeyword();

        return !onKeyword.isMissing() && cursor > onKeyword.textRange().endOffset();
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, ServiceDeclarationNode node) {
        /*
        Avoid picking the service declaration context in the following scenario
        p<cursor>
        service ...
         */
        int cursor = context.getCursorPositionInTree();
        Token serviceKeyword = node.serviceKeyword();

        return serviceKeyword.textRange().endOffset() < cursor
                && cursor <= node.closeBraceToken().textRange().endOffset();
    }

    @Override
    public void sort(BallerinaCompletionContext context, ServiceDeclarationNode node,
                     List<LSCompletionItem> completionItems, Object... metaData) {
        ServiceContext serviceContext = (ServiceContext) metaData[0];
        switch (serviceContext) {
            case MEMBERS:
                this.sortMemberContextItems(context, completionItems);
                break;
            case TYPE_DESC:
                this.sortTypeDescContextItems(context, completionItems);
                break;
            case EXPR_LIST:
                this.sortExprListContextItems(context, completionItems);
                break;
            case OTHER:
            default:
                super.sort(context, node, completionItems, metaData);
                break;
        }
    }

    private void sortTypeDescContextItems(BallerinaCompletionContext context, List<LSCompletionItem> completionItems) {
        for (LSCompletionItem lsItem : completionItems) {
            String sortText = "";
            boolean isModuleCItem = SortingUtil.isModuleCompletionItem(lsItem);
            if (!isModuleCItem && lsItem.getType() == LSCompletionItem.CompletionItemType.SYMBOL) {
                Symbol symbol = ((SymbolCompletionItem) lsItem).getSymbol().orElse(null);
                Optional<TypeSymbol> typeDesc = SymbolUtil.getTypeDescriptor(symbol);
                if (typeDesc.isPresent()) {
                    TypeSymbol rawType = CommonUtil.getRawType(typeDesc.get());
                    sortText = rawType.kind() == SymbolKind.CLASS ? genSortText(1) : genSortText(2);
                }
            } else if (isModuleCItem) {
                sortText = genSortText(3) + genSortTextForModule(context, lsItem);
            }

            if (sortText.isEmpty()) {
                sortText = genSortText(4);
            }
            lsItem.getCompletionItem().setSortText(sortText);
        }
    }

    private void sortExprListContextItems(BallerinaCompletionContext context, List<LSCompletionItem> completionItems) {
        for (LSCompletionItem lsItem : completionItems) {
            String sortText;
            CompletionItem cItem = lsItem.getCompletionItem();
            boolean isModuleCItem = SortingUtil.isModuleCompletionItem(lsItem);
            if (!isModuleCItem && lsItem.getType() == LSCompletionItem.CompletionItemType.SYMBOL) {
                sortText = genSortText(1);
            } else if (Snippet.KW_NEW.equals(lsItem)) {
                // Prioritize the new keyword
                sortText = genSortText(2);
            } else if (isModuleCItem) {
                sortText = genSortText(3) + genSortTextForModule(context, lsItem);
            } else {
                // Should not come to this point.
                sortText = genSortText(4);
            }

            cItem.setSortText(sortText);
        }
    }

    private void sortMemberContextItems(BallerinaCompletionContext context, List<LSCompletionItem> completionItems) {
        for (LSCompletionItem lsItem : completionItems) {
            String sortText;
            CompletionItem cItem = lsItem.getCompletionItem();
            if (Snippet.DEF_REMOTE_FUNCTION.equals(lsItem) || Snippet.DEF_RESOURCE_FUNCTION_SIGNATURE.equals(lsItem)
                    || Snippet.DEF_FUNCTION.equals(lsItem) || Snippet.DEF_INIT_FUNCTION.equals(lsItem)) {
                sortText = genSortText(1);
            } else if (SortingUtil.isTypeCompletionItem(lsItem)) {
                sortText = genSortText(2);
            } else if (SortingUtil.isModuleCompletionItem(lsItem)) {
                sortText = genSortText(3) + SortingUtil.genSortTextForModule(context, lsItem);
            } else {
                sortText = genSortText(4);
            }

            cItem.setSortText(sortText);
        }
    }

    enum ServiceContext {
        MEMBERS,
        TYPE_DESC,
        EXPR_LIST,
        OTHER
    }
}
