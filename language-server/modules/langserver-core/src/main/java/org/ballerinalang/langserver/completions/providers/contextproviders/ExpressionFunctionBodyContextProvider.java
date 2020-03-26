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
package org.ballerinalang.langserver.completions.providers.contextproviders;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.filters.DelimiterBasedContentFilter;
import org.ballerinalang.langserver.completions.util.filters.SymbolFilters;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the expression function body completion.
 *
 * @since 1.2.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.LSCompletionProvider")
public class ExpressionFunctionBodyContextProvider extends AbstractCompletionProvider {
    public ExpressionFunctionBodyContextProvider() {
        this.attachmentPoints.add(BallerinaParser.ExprFunctionBodyContext.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(LSContext ctx) {
        Integer invocationType = ctx.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY);

        if (invocationType > -1) {
            Either<List<LSCompletionItem>, List<Scope.ScopeEntry>> filterItems
                    = SymbolFilters.get(DelimiterBasedContentFilter.class).filterItems(ctx);
            return this.getCompletionItemList(filterItems, ctx);
        }

        ArrayList<LSCompletionItem> completionItems = new ArrayList<>(this.getVarDefCompletions(ctx));
        completionItems.add(new SnippetCompletionItem(ctx, Snippet.KW_TYPEOF.get()));

        return completionItems;
    }
}
