/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.util.filters;

import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Provides the statement templates.
 */
public class StatementTemplateFilter extends AbstractSymbolFilter {
    @Override
    public Either<List<CompletionItem>, List<SymbolInfo>> filterItems(LSServiceOperationContext context) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        boolean isSnippet = context.get(CompletionKeys.CLIENT_CAPABILITIES_KEY).getCompletionItem().getSnippetSupport();

        // Populate If Statement template
        completionItems.add(Snippet.STMT_IF.get().build(isSnippet));
        // Populate While Statement template
        completionItems.add(Snippet.STMT_WHILE.get().build(isSnippet));
        // Populate Lock Statement template
        completionItems.add(Snippet.STMT_LOCK.get().build(isSnippet));
        // Populate Foreach Statement template
        completionItems.add(Snippet.STMT_FOREACH.get().build(isSnippet));
        // Populate Fork Statement template
        completionItems.add(Snippet.STMT_FORK.get().build(isSnippet));
        // Populate Transaction Statement template
        completionItems.add(Snippet.STMT_TRANSACTION.get().build(isSnippet));
        // Populate Match statement template
        completionItems.add(Snippet.STMT_MATCH.get().build(isSnippet));
        
        if (context.get(CompletionKeys.LOOP_COUNT_KEY) > 0 
                && !context.get(CompletionKeys.CURRENT_NODE_TRANSACTION_KEY)) {
            /*
            Populate Continue Statement template only if enclosed within a looping construct
            and not in immediate transaction construct
             */
            completionItems.add(Snippet.STMT_CONTINUE.get().build(isSnippet));
        }
        
        if (context.get(CompletionKeys.LOOP_COUNT_KEY) > 0) {
            // Populate Break Statement template only if there is an enclosing looping construct such as while/ foreach
            completionItems.add(Snippet.STMT_BREAK.get().build(isSnippet));
        }
        // Populate Return Statement template
        completionItems.add(Snippet.STMT_RETURN.get().build(isSnippet));
        
        if (context.get(CompletionKeys.TRANSACTION_COUNT_KEY) > 0) {
            completionItems.add(Snippet.STMT_ABORT.get().build(isSnippet));
            completionItems.add(Snippet.STMT_RETRY.get().build(isSnippet));
        }
        // Populate Throw Statement template
        completionItems.add(Snippet.STMT_PANIC.get().build(isSnippet));

        completionItems.sort(Comparator.comparing(CompletionItem::getLabel));

        // Set the insert text format to be snippet supported format
        completionItems.forEach(completionItem -> completionItem.setInsertTextFormat(InsertTextFormat.Snippet));

        return Either.forLeft(completionItems);
    }
}
