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
import org.ballerinalang.langserver.SnippetBlock;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.sorters.ActionAndFieldAccessContextItemSorter;
import org.ballerinalang.langserver.completions.util.sorters.ItemSorters;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
            getResourceFunction(ctx).map(completionItems::addAll);
            completionItems.add(Snippet.DEF_FUNCTION.get().build(ctx));
        }
        return completionItems;
    }

    private Optional<List<CompletionItem>> getResourceFunction(LSContext ctx) {
        BLangNode symbolEnvNode = ctx.get(CompletionKeys.SYMBOL_ENV_NODE_KEY);
        List<CompletionItem> items = new ArrayList<>();
        if (symbolEnvNode instanceof BLangService) {
            BLangService service = (BLangService) symbolEnvNode;
            String owner = service.listenerType.tsymbol.owner.name.value;
            String serviceTypeName = service.listenerType.tsymbol.name.value;
            // Only http, grpc have generic resource templates, others will have generic resource snippet
            switch (owner) {
                case "http":
                    if ("Listener".equals(serviceTypeName)) {
                        items.add(Snippet.DEF_RESOURCE.get().build(ctx));
                        break;
                    } else if ("WebSocketListener".equals(serviceTypeName)) {
                        addIfNotExists(Snippet.DEF_RESOURCE_WS_OPEN.get(), service, items, ctx);
                        addIfNotExists(Snippet.DEF_RESOURCE_WS_TEXT.get(), service, items, ctx);
                        addIfNotExists(Snippet.DEF_RESOURCE_WS_CLOSE.get(), service, items, ctx);
                        break;
                    }
                    return Optional.empty();
                case "grpc":
                    items.add(Snippet.DEF_RESOURCE_GRPC.get().build(ctx));
                    break;
                case "websub":
                    addIfNotExists(Snippet.DEF_RESOURCE_WEBSUB_INTENT.get(), service, items, ctx);
                    addIfNotExists(Snippet.DEF_RESOURCE_WEBSUB_NOTIFY.get(), service, items, ctx);
                    break;
                default:
                    return Optional.empty();
            }
        }
        return !items.isEmpty() ? Optional.of(items) : Optional.empty();
    }

    private void addIfNotExists(SnippetBlock snippet, BLangService service, List<CompletionItem> items, LSContext ctx) {
        boolean found = false;
        for (BLangFunction resource : service.getResources()) {
            if (snippet.getLabel().equals(resource.name.value + " " + ItemResolverConstants.RESOURCE)) {
                found = true;
            }
        }
        if (!found) {
            items.add(snippet.build(ctx));
        }
    }
}
