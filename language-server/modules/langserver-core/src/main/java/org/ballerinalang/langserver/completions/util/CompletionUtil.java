/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.util;

import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.LSCompletionProvider;
import org.ballerinalang.langserver.completions.LSCompletionProviderFactory;
import org.ballerinalang.langserver.completions.TreeVisitor;
import org.ballerinalang.langserver.completions.resolvers.CompletionItemsContext;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.InsertTextFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.util.ArrayList;
import java.util.List;

/**
 * Common utility methods for the completion operation.
 */
public class CompletionUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompletionUtil.class);

    /**
     * Resolve the visible symbols from the given BLang Package and the current context.
     *
     * @param completionContext     Completion Service Context
     */
    public static void resolveSymbols(LSServiceOperationContext completionContext) {
        // Visit the package to resolve the symbols
        TreeVisitor treeVisitor = new TreeVisitor(completionContext);
        BLangPackage bLangPackage = completionContext.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
        bLangPackage.accept(treeVisitor);
        completionContext.put(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY, bLangPackage);
    }

    /**
     * Get the completion Items for the context.
     *
     * @param ctx Completion context
     * @return {@link List}         List of resolved completion Items
     */
    public static List<CompletionItem>  getCompletionItems(LSContext ctx) {
        if (ctx == null) {
            return null;
        }
        Class scopeContext = ctx.get(CompletionKeys.SYMBOL_ENV_NODE_KEY).getClass();
        CompletionItemsContext completionItemsContext = CompletionItemResolver.get(scopeContext, ctx);

        List<LSCompletionProvider> providers = LSCompletionProviderFactory.getInstance().getProviders();
        List<CompletionItem> items = new ArrayList<>();
        for (LSCompletionProvider provider : providers) {
            try {
                items.addAll(provider.getCompletions(ctx, completionItemsContext));
            } catch (Exception e) {
                LOGGER.error("Error while retrieving completions from: " + provider.getName());
            }
        }

        boolean isSnippetSupported = ctx.get(CompletionKeys.CLIENT_CAPABILITIES_KEY).getCompletionItem()
                .getSnippetSupport();
        for (CompletionItem item : items) {
            if (!isSnippetSupported) {
                item.setInsertText(CommonUtil.getPlainTextSnippet(item.getInsertText()));
                item.setInsertTextFormat(InsertTextFormat.PlainText);
            } else {
                item.setInsertTextFormat(InsertTextFormat.Snippet);
            }
        }
        return items;
    }
}
