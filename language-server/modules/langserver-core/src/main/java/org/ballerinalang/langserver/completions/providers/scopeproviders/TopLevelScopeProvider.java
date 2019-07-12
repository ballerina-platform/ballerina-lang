/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.CompletionSubRuleParser;
import org.ballerinalang.langserver.completions.providers.contextproviders.AnnotationAttachmentContextProvider;
import org.ballerinalang.langserver.completions.spi.LSCompletionProvider;
import org.ballerinalang.langserver.completions.util.sorters.ItemSorters;
import org.ballerinalang.langserver.completions.util.sorters.TopLevelContextSorter;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Resolves all items that can appear as a top level element in the file.
 * 
 * @since 0.995.0
 */
@JavaSPIService("org.ballerinalang.langserver.completions.spi.LSCompletionProvider")
public class TopLevelScopeProvider extends LSCompletionProvider {
    public TopLevelScopeProvider() {
        this.attachmentPoints.add(BLangPackage.class);
    }

    @Override
    public List<CompletionItem> getCompletions(LSContext ctx) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        Optional<LSCompletionProvider> contextProvider = this.getContextProvider(ctx);
        
        if (contextProvider.isPresent()) {
            return contextProvider.get().getCompletions(ctx);
        }

        completionItems.addAll(addTopLevelItems(ctx));
        completionItems.addAll(getBasicTypes(ctx.get(CommonKeys.VISIBLE_SYMBOLS_KEY)));

        ItemSorters.get(TopLevelContextSorter.class).sortItems(ctx, completionItems);
        return completionItems;
    }

    @Override
    public Optional<LSCompletionProvider> getContextProvider(LSContext ctx) {
        List<CommonToken> lhsTokens = ctx.get(CompletionKeys.LHS_TOKENS_KEY);
        if (lhsTokens == null || lhsTokens.isEmpty()) {
            return Optional.empty();
        }
        if (this.isAnnotationAttachmentContext(ctx)) {
            return Optional.ofNullable(this.getProvider(AnnotationAttachmentContextProvider.class));
        }
        // Handle with the parser rule context
        Optional<CommonToken> serviceToken = lhsTokens.stream()
                .filter(commonToken -> commonToken.getType() == BallerinaParser.SERVICE)
                .findFirst();

        if (serviceToken.isPresent()) {
            return Optional.ofNullable(this.getProvider(BallerinaParser.ServiceDefinitionContext.class));
        }
        Optional<String> subRule = this.getSubRule(lhsTokens);
        subRule.ifPresent(rule -> CompletionSubRuleParser.parseWithinCompilationUnit(rule, ctx));
        ParserRuleContext parserRuleContext = ctx.get(CompletionKeys.PARSER_RULE_CONTEXT_KEY);

        if (parserRuleContext != null) {
            return Optional.ofNullable(this.getProvider(parserRuleContext.getClass()));
        }
        return super.getContextProvider(ctx);
    }
}
