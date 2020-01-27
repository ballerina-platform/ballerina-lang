/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.antlr.v4.runtime.CommonToken;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.SnippetGenerator;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser rule based variable definition statement context resolver.
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.LSCompletionProvider")
public class WorkerDeclarationContextProvider extends AbstractCompletionProvider {
    public WorkerDeclarationContextProvider() {
        this.attachmentPoints.add(BallerinaParser.WorkerDeclarationContext.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<LSCompletionItem> getCompletions(LSContext context) {
        ArrayList<LSCompletionItem> completionItems = new ArrayList<>();
        List<Scope.ScopeEntry> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        Boolean inWorkerReturnCtx = context.get(CompletionKeys.IN_WORKER_RETURN_CONTEXT_KEY);
        int invocationOrDelimiterTokenType = context.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY);
        if (invocationOrDelimiterTokenType == BallerinaParser.COLON) {
            List<CommonToken> defaultTokens = context.get(CompletionKeys.LHS_DEFAULT_TOKENS_KEY);
            List<Integer> defaultTokenTypes = context.get(CompletionKeys.LHS_DEFAULT_TOKEN_TYPES_KEY);
            int pkgDelimIndex = defaultTokenTypes.indexOf(BallerinaParser.COLON);
            String pkgName = defaultTokens.get(pkgDelimIndex - 1).getText();
            completionItems.addAll(this.getTypeItemsInPackage(visibleSymbols, pkgName, context));
        } else if (inWorkerReturnCtx != null && inWorkerReturnCtx) {
            completionItems.addAll(this.getBasicTypesItems(context, visibleSymbols));
            completionItems.addAll(this.getPackagesCompletionItems(context));
        } else {
            completionItems.add(new SnippetCompletionItem(context, SnippetGenerator.getReturnsKeywordSnippet()));
        }

        return completionItems;
    }
}
