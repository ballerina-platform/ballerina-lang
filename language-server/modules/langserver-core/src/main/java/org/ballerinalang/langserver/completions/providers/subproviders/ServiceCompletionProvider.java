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
package org.ballerinalang.langserver.completions.providers.subproviders;

import org.antlr.v4.runtime.ParserRuleContext;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.sorters.ActionAndFieldAccessContextItemSorter;
import org.ballerinalang.langserver.completions.util.sorters.ItemSorters;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;

import java.util.ArrayList;
import java.util.List;

/**
 * ServiceContextResolver.
 */
public class ServiceCompletionProvider extends AbstractSubCompletionProvider {
    @Override
    public List<CompletionItem> resolveItems(LSContext ctx) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        ParserRuleContext parserRuleContext = ctx.get(CompletionKeys.PARSER_RULE_CONTEXT_KEY);
        if (isInvocationOrInteractionOrFieldAccess(ctx)) {
            completionItems.addAll(getDelimiterBasedCompletionItems(ctx));
            ItemSorters.get(ActionAndFieldAccessContextItemSorter.class).sortItems(ctx, completionItems);
        } else if (parserRuleContext == null ||
                parserRuleContext instanceof BallerinaParser.ObjectFieldDefinitionContext) {
            completionItems.addAll(this.getBasicTypes(ctx.get(CompletionKeys.VISIBLE_SYMBOLS_KEY)));
            completionItems.addAll(this.getPackagesCompletionItems(ctx));
            completionItems.add(Snippet.DEF_RESOURCE.get().build(ctx));
            completionItems.add(Snippet.DEF_FUNCTION.get().build(ctx));
        }
        return completionItems;
    }
}
