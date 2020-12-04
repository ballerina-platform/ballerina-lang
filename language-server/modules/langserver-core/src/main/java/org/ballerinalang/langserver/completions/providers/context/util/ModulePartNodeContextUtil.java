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
package org.ballerinalang.langserver.completions.providers.context.util;

import org.ballerinalang.langserver.SnippetBlock;
import org.ballerinalang.langserver.commons.CompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.SortingUtil;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.ballerinalang.langserver.completions.util.SortingUtil.genSortText;

/**
 * Utilities for the module part node context.
 * 
 * @since 2.0.0
 */
public class ModulePartNodeContextUtil {
    private ModulePartNodeContextUtil() {
    }

    /**
     * Add top level items to the given completionItems List.
     *
     * @param context LS Context
     * @return {@link List}     List of populated completion items
     */
    public static List<LSCompletionItem> getTopLevelItems(CompletionContext context) {
        ArrayList<LSCompletionItem> completionItems = new ArrayList<>();
        List<Snippet> snippets = Arrays.asList(
                Snippet.KW_IMPORT, Snippet.KW_FUNCTION, Snippet.KW_TYPE, Snippet.KW_PUBLIC, Snippet.KW_ISOLATED,
                Snippet.KW_FINAL, Snippet.KW_CONST, Snippet.KW_LISTENER, Snippet.KW_CLIENT, Snippet.KW_VAR,
                Snippet.KW_ENUM, Snippet.KW_XMLNS, Snippet.KW_CLASS, Snippet.KW_DISTINCT, Snippet.KW_SERVICE,
                Snippet.DEF_FUNCTION, Snippet.DEF_MAIN_FUNCTION, Snippet.DEF_SERVICE, Snippet.DEF_SERVICE_WEBSOCKET,
                Snippet.DEF_SERVICE_WS_CLIENT, Snippet.DEF_SERVICE_GRPC, Snippet.DEF_ANNOTATION, Snippet.DEF_RECORD,
                Snippet.STMT_NAMESPACE_DECLARATION, Snippet.DEF_OBJECT_SNIPPET, Snippet.DEF_CLASS, Snippet.DEF_ENUM,
                Snippet.DEF_CLOSED_RECORD, Snippet.DEF_ERROR_TYPE
        );

        snippets.forEach(snippet -> completionItems.add(new SnippetCompletionItem(context, snippet.get())));

        return completionItems;
    }
    
    public static void sort(List<LSCompletionItem> items) {
        for (LSCompletionItem item : items) {
            CompletionItem cItem = item.getCompletionItem();
            if (isSnippetBlock(item)) {
                cItem.setSortText(genSortText(1));
                continue;
            }
            if (isKeyword(item)) {
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

    private static boolean isSnippetBlock(LSCompletionItem completionItem) {
        return completionItem instanceof SnippetCompletionItem
                && (((SnippetCompletionItem) completionItem).kind() == SnippetBlock.Kind.SNIPPET
                || ((SnippetCompletionItem) completionItem).kind() == SnippetBlock.Kind.STATEMENT);
    }

    private static boolean isKeyword(LSCompletionItem completionItem) {
        return completionItem instanceof SnippetCompletionItem
                && ((SnippetCompletionItem) completionItem).kind() == SnippetBlock.Kind.KEYWORD;
    }
}
