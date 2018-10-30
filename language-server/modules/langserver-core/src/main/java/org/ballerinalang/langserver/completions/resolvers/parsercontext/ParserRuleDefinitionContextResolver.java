/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.resolvers.parsercontext;

import org.antlr.v4.runtime.Token;
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.resolvers.AbstractItemResolver;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Completion Item Resolver for the Function Definition Context.
 * 
 * @since v0.982.0
 */
public class ParserRuleDefinitionContextResolver extends AbstractItemResolver {
    @Override
    public List<CompletionItem> resolveItems(LSServiceOperationContext context) {
        List<String> consumedTokens = context.get(CompletionKeys.FORCE_CONSUMED_TOKENS_KEY).stream()
                .map(Token::getText)
                .collect(Collectors.toList());
        if (!consumedTokens.get(0).equals(UtilSymbolKeys.FUNCTION_KEYWORD_KEY)) {
            return new ArrayList<>();
        }
        return context.get(CompletionKeys.VISIBLE_SYMBOLS_KEY).stream()
                .filter(symbolInfo -> symbolInfo.getScopeEntry().symbol instanceof BObjectTypeSymbol)
                .map(symbolInfo -> {
                    String symbolName = symbolInfo.getScopeEntry().symbol.getName().getValue();
                    CompletionItem completionItem = new CompletionItem();
                    completionItem.setLabel(symbolName);
                    completionItem.setInsertText(symbolName + "::");
                    completionItem.setDetail(ItemResolverConstants.B_TYPE);
                    completionItem.setKind(CompletionItemKind.Reference);
                    return completionItem;
                }).collect(Collectors.toList());
    }
}
