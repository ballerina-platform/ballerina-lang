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
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.model.elements.Flag;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.tree.BLangFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
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
    public Either<List<LSCompletionItem>, List<Scope.ScopeEntry>> filterItems(LSContext context) {
        ArrayList<LSCompletionItem> completionItems = new ArrayList<>();
        BLangNode bLangNode = context.get(CompletionKeys.SCOPE_NODE_KEY);

        // Populate If Statement template
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_IF.get()));

        if (context.get(CompletionKeys.PREVIOUS_NODE_KEY) instanceof BLangIf) {
            // Populate Else If Statement template
            completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_ELSE_IF.get()));
            // Populate Else Statement template
            completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_ELSE.get()));
        }

        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_WHILE.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_DO.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_LOCK.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_FOREACH.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_FORK.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_TRANSACTION.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_RETRY_TRANSACTION.get()));
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_MATCH.get()));
        if ((bLangNode instanceof BLangBlockStmt && bLangNode.parent instanceof BLangForkJoin)
                || (bLangNode instanceof BLangFunctionBody
                && !(bLangNode.parent instanceof  BLangLambdaFunction
                && (((BLangLambdaFunction) bLangNode.parent).function.flagSet.contains(Flag.WORKER))))) {
            completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_WORKER.get()));
        }

        if (context.get(CompletionKeys.LOOP_COUNT_KEY) > 0
                && !context.get(CompletionKeys.CURRENT_NODE_TRANSACTION_KEY)) {
            /*
            Populate Continue Statement template only if enclosed within a looping construct
            and not in immediate transaction construct
             */
            completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_CONTINUE.get()));
        }

        if (context.get(CompletionKeys.LOOP_COUNT_KEY) > 0) {
            // Populate Break Statement template only if there is an enclosing looping construct such as while/ foreach
            completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_BREAK.get()));
        }
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_RETURN.get()));

        if (context.get(CompletionKeys.TRANSACTION_COUNT_KEY) > 0) {
            completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_ROLLBACK.get()));
        }
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_PANIC.get()));

        completionItems.sort(Comparator.comparing(lsCompletionItem -> lsCompletionItem.getCompletionItem().getLabel()));

        // Set the insert text format to be snippet supported format
        completionItems.forEach(completionItem -> completionItem.getCompletionItem()
                .setInsertTextFormat(InsertTextFormat.Snippet));

        return Either.forLeft(completionItems);
    }
}
