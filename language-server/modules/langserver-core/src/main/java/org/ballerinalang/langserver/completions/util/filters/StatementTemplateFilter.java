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

import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Provides the statement templates.
 */
public class StatementTemplateFilter extends AbstractSymbolFilter {
    @Override
    public Either<List<CompletionItem>, List<SymbolInfo>> filterItems(LSContext context) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        BLangNode bLangNode = context.get(CompletionKeys.SCOPE_NODE_KEY);

        // Populate If Statement template
        completionItems.add(Snippet.STMT_IF.get().build(context));
        
        if (context.get(CompletionKeys.PREVIOUS_NODE_KEY) instanceof BLangIf) {
            // Populate Else If Statement template
            completionItems.add(Snippet.STMT_ELSE_IF.get().build(context));
            // Populate Else Statement template
            completionItems.add(Snippet.STMT_ELSE.get().build(context));
        }
        
        // Populate While Statement template
        completionItems.add(Snippet.STMT_WHILE.get().build(context));
        // Populate Lock Statement template
        completionItems.add(Snippet.STMT_LOCK.get().build(context));
        // Populate Foreach Statement template
        completionItems.add(Snippet.STMT_FOREACH.get().build(context));
        // Populate Fork Statement template
        completionItems.add(Snippet.STMT_FORK.get().build(context));
        // Populate Transaction Statement template
        completionItems.add(Snippet.STMT_TRANSACTION.get().build(context));
        // Populate Match statement template
        completionItems.add(Snippet.STMT_MATCH.get().build(context));
        if (bLangNode instanceof BLangBlockStmt
                && (bLangNode.parent instanceof BLangFunction || bLangNode.parent instanceof BLangForkJoin)) {
            // Populate Worker Declaration statement template
            completionItems.add(Snippet.DEF_WORKER.get().build(context));
        }
        
        if (context.get(CompletionKeys.LOOP_COUNT_KEY) > 0 
                && !context.get(CompletionKeys.CURRENT_NODE_TRANSACTION_KEY)) {
            /*
            Populate Continue Statement template only if enclosed within a looping construct
            and not in immediate transaction construct
             */
            completionItems.add(Snippet.STMT_CONTINUE.get().build(context));
        }
        
        if (context.get(CompletionKeys.LOOP_COUNT_KEY) > 0) {
            // Populate Break Statement template only if there is an enclosing looping construct such as while/ foreach
            completionItems.add(Snippet.STMT_BREAK.get().build(context));
        }
        // Populate Return Statement template
        completionItems.add(Snippet.STMT_RETURN.get().build(context));
        
        if (context.get(CompletionKeys.TRANSACTION_COUNT_KEY) > 0) {
            completionItems.add(Snippet.STMT_ABORT.get().build(context));
            completionItems.add(Snippet.STMT_RETRY.get().build(context));
        }
        // Populate Throw Statement template
        completionItems.add(Snippet.STMT_PANIC.get().build(context));

        completionItems.sort(Comparator.comparing(CompletionItem::getLabel));

        // Set the insert text format to be snippet supported format
        completionItems.forEach(completionItem -> completionItem.setInsertTextFormat(InsertTextFormat.Snippet));

        return Either.forLeft(completionItems);
    }
}
