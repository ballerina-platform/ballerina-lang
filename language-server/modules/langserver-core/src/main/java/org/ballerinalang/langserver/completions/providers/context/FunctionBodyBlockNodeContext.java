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

import io.ballerinalang.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerinalang.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerinalang.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerinalang.compiler.text.LinePosition;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.CompletionUtil;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.model.elements.Flag;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangFunctionBody;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Handles the function body context completions.
 * 
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.CompletionProvider")
public class FunctionBodyBlockNodeContext extends AbstractCompletionProvider<FunctionBodyBlockNode> {
    public FunctionBodyBlockNodeContext() {
        super(Kind.MODULE_MEMBER);
        this.attachmentPoints.add(FunctionBodyBlockNode.class);
    }
    
    @Override
    public List<LSCompletionItem> getCompletions(LSContext context, FunctionBodyBlockNode node)
            throws LSCompletionException {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        completionItems.addAll(getStaticCompletionItems(context));
        completionItems.addAll(getStatementCompletionItems(context));
        completionItems.addAll(this.getPackagesCompletionItems(context));
        completionItems.addAll(this.getTypeItems(context));
        completionItems.addAll(this.getSymbolCompletions(context));
        return completionItems;
    }

    private List<LSCompletionItem> getStaticCompletionItems(LSContext context) {

        ArrayList<LSCompletionItem> completionItems = new ArrayList<>();

        // Add the xmlns snippet
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_NAMESPACE_DECLARATION.get()));
        // Add the xmlns keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_XMLNS.get()));
        // Add the var keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_VAR.get()));
        // Add the wait keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_WAIT.get()));
        // Add the start keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_START.get()));
        // Add the flush keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FLUSH.get()));
        // Add the function keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FUNCTION.get()));
        // Add the error snippet
        completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_ERROR.get()));
        // Add the error type CompletionItem
        completionItems.add(CommonUtil.getErrorTypeCompletionItem(context));
        // Add the checkpanic keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_CHECK_PANIC.get()));
        // Add the check keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_CHECK.get()));
        // Add the final keyword
        completionItems.add(new SnippetCompletionItem(context, Snippet.KW_FINAL.get()));

        return completionItems;
    }

    private List<LSCompletionItem> getStatementCompletionItems(LSContext context) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        // Populate If Statement template
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_IF.get()));
        if (context.get(CompletionKeys.PREVIOUS_NODE_KEY) instanceof BLangIf) {
            // Populate Else If Statement template
            completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_ELSE_IF.get()));
            // Populate Else Statement template
            completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_ELSE.get()));
        }

        // Populate While Statement template
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_WHILE.get()));
        // Populate Lock Statement template
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_LOCK.get()));
        // Populate Foreach Statement template
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_FOREACH.get()));
        // Populate Fork Statement template
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_FORK.get()));
        // Populate Transaction Statement template
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_TRANSACTION.get()));
        // Populate Retry Transaction Statement template
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_RETRY_TRANSACTION.get()));
        // Populate Match statement template
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_MATCH.get()));
//        if ((bLangNode instanceof BLangBlockStmt && bLangNode.parent instanceof BLangForkJoin)
//                || (bLangNode instanceof BLangFunctionBody
//                && !(bLangNode.parent instanceof BLangLambdaFunction
//                && (((BLangLambdaFunction) bLangNode.parent).function.flagSet.contains(Flag.WORKER))))) {
//            // Populate Worker Declaration statement template
//            completionItems.add(new SnippetCompletionItem(context, Snippet.DEF_WORKER.get()));
//        }

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
        // Populate Return Statement template
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_RETURN.get()));

        if (context.get(CompletionKeys.TRANSACTION_COUNT_KEY) > 0) {
            completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_ROLLBACK.get()));
        }
        // Populate Throw Statement template
        completionItems.add(new SnippetCompletionItem(context, Snippet.STMT_PANIC.get()));

        return completionItems;
    }
    
    private List<LSCompletionItem> getSymbolCompletions(LSContext context) {
        List<Scope.ScopeEntry> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        // TODO: Can we get this filter to a common place
        List<Scope.ScopeEntry> filteredList = visibleSymbols.stream()
                .filter(scopeEntry -> scopeEntry.symbol instanceof BVarSymbol
                        && !(scopeEntry.symbol instanceof BOperatorSymbol))
                .collect(Collectors.toList());
        
        return this.getCompletionItemList(filteredList, context);
    }
}
