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

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.SnippetBlock;
import org.ballerinalang.langserver.common.utils.QNameReferenceUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.SortingUtil;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.ballerinalang.langserver.completions.util.SortingUtil.genSortText;

/**
 * Completion provider for {@link ModulePartNode} context.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class ModulePartNodeContext extends AbstractCompletionProvider<ModulePartNode> {

    public ModulePartNodeContext() {
        super(ModulePartNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, ModulePartNode node) {
        NonTerminalNode nodeAtCursor = context.get(CompletionKeys.NODE_AT_CURSOR_KEY);
        if (this.onQualifiedNameIdentifier(context, nodeAtCursor)) {
            Predicate<Symbol> predicate = symbol -> symbol.kind() == SymbolKind.TYPE;
            List<Symbol> types = QNameReferenceUtil.getModuleContent(context,
                    (QualifiedNameReferenceNode) nodeAtCursor, predicate);
            return this.getCompletionItemList(types, context);
        }

        List<LSCompletionItem> completionItems = new ArrayList<>();
        completionItems.addAll(this.getTopLevelItems(context));
        completionItems.addAll(this.getTypeItems(context));
        completionItems.addAll(this.getModuleCompletionItems(context));
        this.sort(context, node, completionItems);

        return completionItems;
    }

    @Override
    public void sort(LSContext context, ModulePartNode node, List<LSCompletionItem> items, Object... metaData) {
        for (LSCompletionItem item : items) {
            CompletionItem cItem = item.getCompletionItem();
            if (this.isSnippetBlock(item)) {
                cItem.setSortText(genSortText(1));
                continue;
            }
            if (this.isKeyword(item)) {
                cItem.setSortText(genSortText(2));
                continue;
            }
            if (SortingUtil.isModuleCompletionItem(item)) {
                cItem.setSortText(genSortText(3));
                continue;
            }
            if (SortingUtil.isTypeCompletionItem(item)) {
                cItem.setSortText(genSortText(4));
                continue;
            }
            cItem.setSortText(genSortText(5));
        }
    }

    private boolean isSnippetBlock(LSCompletionItem completionItem) {
        return completionItem instanceof SnippetCompletionItem
                && (((SnippetCompletionItem) completionItem).kind() == SnippetBlock.Kind.SNIPPET
                || ((SnippetCompletionItem) completionItem).kind() == SnippetBlock.Kind.STATEMENT);
    }

    private boolean isKeyword(LSCompletionItem completionItem) {
        return completionItem instanceof SnippetCompletionItem
                && ((SnippetCompletionItem) completionItem).kind() == SnippetBlock.Kind.KEYWORD;
    }

    /**
     * Add top level items to the given completionItems List.
     *
     * @param context LS Context
     * @return {@link List}     List of populated completion items
     */
    protected List<LSCompletionItem> getTopLevelItems(LSContext context) {
        ArrayList<LSCompletionItem> completionItems = new ArrayList<>();
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_IMPORT.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FUNCTION.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_TYPE.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_PUBLIC.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_ISOLATED.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FINAL.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_CONST.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_LISTENER.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_VAR.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_ENUM.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_XMLNS.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_CLASS.get()));

        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_FUNCTION.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_MAIN_FUNCTION.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_SERVICE.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_SERVICE_WEBSOCKET.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_SERVICE_WS_CLIENT.get()));
//        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_SERVICE_WEBSUB));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_SERVICE_GRPC.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_ANNOTATION.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_NAMESPACE_DECLARATION.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_OBJECT_SNIPPET.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_RECORD.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_CLOSED_RECORD.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_ERROR_TYPE.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_CLASS.get()));
        return completionItems;
    }
}
