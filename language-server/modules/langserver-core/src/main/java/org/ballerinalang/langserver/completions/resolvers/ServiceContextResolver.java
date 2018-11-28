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
package org.ballerinalang.langserver.completions.resolvers;

import org.antlr.v4.runtime.ParserRuleContext;
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleAnnotationAttachmentResolver;
import org.ballerinalang.langserver.completions.util.CompletionItemResolver;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;

import java.util.ArrayList;
import java.util.List;

/**
 * ServiceContextResolver.
 */
public class ServiceContextResolver extends AbstractItemResolver {

    @Override
    public List<CompletionItem> resolveItems(LSServiceOperationContext ctx) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        ParserRuleContext parserRuleContext = ctx.get(CompletionKeys.PARSER_RULE_CONTEXT_KEY);
        boolean isSnippet = ctx.get(CompletionKeys.CLIENT_CAPABILITIES_KEY).getCompletionItem().getSnippetSupport();
        List<String> poppedTokens = CommonUtil.getPoppedTokenStrings(ctx);

        if (this.isAnnotationStart(ctx)) {
            completionItems.addAll(CompletionItemResolver
                    .get(ParserRuleAnnotationAttachmentResolver.class).resolveItems(ctx));
        } else if (parserRuleContext == null
                || parserRuleContext instanceof BallerinaParser.ObjectFieldDefinitionContext) {
            if (poppedTokens.contains(UtilSymbolKeys.EQUAL_SYMBOL_KEY)) {
                AbstractItemResolver resolver = CompletionItemResolver
                        .get(BallerinaParser.VariableDefinitionStatementContext.class);
                completionItems.addAll(resolver.resolveItems(ctx));
            } else {
                completionItems.addAll(this.populateBasicTypes(ctx.get(CompletionKeys.VISIBLE_SYMBOLS_KEY)));
                completionItems.add(Snippet.DEF_RESOURCE.get().build(new CompletionItem(), isSnippet));
                completionItems.add(Snippet.DEF_FUNCTION.get().build(new CompletionItem(), isSnippet));
            }
//            CompletionItemSorter itemSorter =
//                    ItemSorters.get(ctx.get(CompletionKeys.SYMBOL_ENV_NODE_KEY).getClass());
//            itemSorter.sortItems(ctx, completionItems);
        } else {
            AbstractItemResolver resolver = CompletionItemResolver.get(parserRuleContext.getClass());
            if (resolver != null) {
                completionItems.addAll(resolver.resolveItems(ctx));
            }
        }
        return completionItems;
    }
}
