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
package org.ballerinalang.langserver.completions.util.sorters.scope;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.ParserRuleContext;
import org.ballerinalang.langserver.SnippetBlock;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.StaticCompletionItem;
import org.ballerinalang.langserver.completions.SymbolCompletionItem;
import org.ballerinalang.langserver.completions.util.Priority;
import org.ballerinalang.langserver.completions.util.sorters.CompletionItemSorter;
import org.ballerinalang.langserver.completions.util.sorters.ItemSorters;
import org.ballerinalang.langserver.sourceprune.SourcePruneKeys;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * Item sorter for Package Context.
 *
 * @since 1.2.0
 */
public class PackageScope extends CompletionItemSorter {
    @Override
    public List<CompletionItem> sortItems(LSContext ctx, List<LSCompletionItem> completionItems) {
        ParserRuleContext parserRuleCtx = ctx.get(CompletionKeys.PARSER_RULE_CONTEXT_KEY);

        if (this.isTopLevel(ctx, parserRuleCtx)) {
            return this.sortInTopLevelScope(completionItems);
        } else if (isGlobalVarDef(parserRuleCtx)) {
            return this.getGlobalVarDefCompletions(ctx, completionItems);
        }
        CompletionItemSorter sorter = ItemSorters.get(parserRuleCtx.getClass());
        return sorter.sortItems(ctx, completionItems);
    }

    @Nonnull
    @Override
    protected List<Class> getAttachedContexts() {
        return Collections.singletonList(BLangPackage.class);
    }

    private List<CompletionItem> sortInTopLevelScope(List<LSCompletionItem> completionItems) {
        List<CompletionItem> cItems = new ArrayList<>();
        for (LSCompletionItem completionItem : completionItems) {
            CompletionItem cItem = completionItem.getCompletionItem();
            cItems.add(cItem);
            if (completionItem instanceof SnippetCompletionItem && ((SnippetCompletionItem) completionItem)
                    .getSnippetType().equals(SnippetBlock.SnippetType.SNIPPET)) {
                cItem.setSortText(Priority.PRIORITY110.toString());
            } else if (completionItem instanceof SnippetCompletionItem && ((SnippetCompletionItem) completionItem)
                    .getSnippetType().equals(SnippetBlock.SnippetType.KEYWORD)) {
                cItem.setSortText(Priority.PRIORITY120.toString());
            } else if (completionItem instanceof StaticCompletionItem
                    && cItem.getKind().equals(CompletionItemKind.Module)) {
                cItem.setSortText(Priority.PRIORITY130.toString());
            } else if (completionItem instanceof SymbolCompletionItem
                    && ((SymbolCompletionItem) completionItem).getSymbol() instanceof BTypeSymbol) {
                cItem.setSortText(Priority.PRIORITY140.toString());
            } else {
                cItem.setSortText(Priority.PRIORITY150.toString());
            }
        }

        return cItems;
    }

    private List<CompletionItem> getGlobalVarDefCompletions(LSContext context, List<LSCompletionItem> completionItems) {
        List<Integer> defaultTokenTypes = context.get(SourcePruneKeys.LHS_DEFAULT_TOKENS_KEY).stream()
                .map(CommonToken::getType)
                .collect(Collectors.toList());
        List<CompletionItem> cItems = new ArrayList<>();

        int listenerKWIndex = defaultTokenTypes.indexOf(BallerinaParser.LISTENER);
        // Listener Types are sorted
        /*
        Ex: public listener <cursor>
            public listener h<cursor>
         */
        boolean suggestListeners = listenerKWIndex > 0 && (listenerKWIndex == defaultTokenTypes.size() - 1
                || listenerKWIndex == defaultTokenTypes.size() - 2);
        for (LSCompletionItem lsCItem : completionItems) {
            CompletionItem cItem = lsCItem.getCompletionItem();
            if (suggestListeners && lsCItem instanceof SymbolCompletionItem
                    && ((SymbolCompletionItem) lsCItem).getSymbol() instanceof BTypeSymbol) {
                cItem.setSortText(Priority.PRIORITY110.toString());
            } else {
                cItem.setSortText(Priority.PRIORITY120.toString());
            }
            cItems.add(cItem);
        }

        return cItems;
    }

    private boolean isTopLevel(LSContext context,  ParserRuleContext parserRuleCtx) {
        List<CommonToken> commonTokens = context.get(SourcePruneKeys.LHS_DEFAULT_TOKENS_KEY);
        return parserRuleCtx == null || (parserRuleCtx instanceof BallerinaParser.GlobalVariableDefinitionContext
                && commonTokens.size() < 2);
    }

    private boolean isGlobalVarDef(ParserRuleContext context) {
        return context instanceof BallerinaParser.GlobalVariableDefinitionContext;
    }
}
