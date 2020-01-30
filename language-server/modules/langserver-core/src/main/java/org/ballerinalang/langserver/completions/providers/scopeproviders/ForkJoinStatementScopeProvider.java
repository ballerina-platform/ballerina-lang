/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.langserver.completions.providers.scopeproviders;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.ParserRuleContext;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.CompletionSubRuleParser;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Resolves all items that can appear within the fork join statement.
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.LSCompletionProvider")
public class ForkJoinStatementScopeProvider extends AbstractCompletionProvider {

    public ForkJoinStatementScopeProvider() {
        this.attachmentPoints.add(BLangForkJoin.class);
    }

    @Override
    public List<CompletionItem> getCompletions(LSContext context) throws LSCompletionException {
        Boolean inWorkerReturn = context.get(CompletionKeys.IN_WORKER_RETURN_CONTEXT_KEY);
        List<CommonToken> lhsTokens = context.get(CompletionKeys.LHS_TOKENS_KEY);
        if (inWorkerReturn != null && inWorkerReturn) {
            return this.getProvider(BallerinaParser.WorkerDeclarationContext.class).getCompletions(context);
        }
        Optional<String> subRule = this.getSubRule(lhsTokens);
        subRule.ifPresent(rule -> CompletionSubRuleParser.parseWithinFunctionDefinition(rule, context));
        ParserRuleContext parserRuleContext = context.get(CompletionKeys.PARSER_RULE_CONTEXT_KEY);
        if (parserRuleContext instanceof BallerinaParser.WorkerDeclarationContext
                && this.getProvider(parserRuleContext.getClass()) != null) {
            return this.getProvider(parserRuleContext.getClass()).getCompletions(context);
        }
        return Collections.singletonList(Snippet.DEF_WORKER.get().build(context));
    }
}
