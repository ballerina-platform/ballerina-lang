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
package org.ballerinalang.langserver.completions.resolvers;

import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Completion Item resolver for the BLangMatch Scope.
 */
public class BLangMatchContextResolver extends AbstractItemResolver {

    @Override
    public List<CompletionItem> resolveItems(LSServiceOperationContext ctx) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        BLangNode symbolEnvNode = ctx.get(CompletionKeys.SYMBOL_ENV_NODE_KEY);
        List<SymbolInfo> visibleSymbols = ctx.get(CompletionKeys.VISIBLE_SYMBOLS_KEY);
        visibleSymbols.removeIf(CommonUtil.invalidSymbolsPredicate());

        if (!(symbolEnvNode instanceof BLangMatch)) {
            return completionItems;
        }

        BLangMatch bLangMatch = (BLangMatch) symbolEnvNode;
        
        switch (bLangMatch.expr.type.getKind()) {
            case UNION: {
                Set<BType> memberTypes = ((BUnionType) bLangMatch.expr.type).getMemberTypes();
                memberTypes.forEach(bType ->
                        completionItems.add(getMatchFieldSnippetCompletion(CommonUtil.getBTypeName(bType, ctx))));
                break;
            }
            case JSON: {
                ArrayList<Integer> typeTagsList = new ArrayList<>(Arrays.asList(TypeTags.INT, TypeTags.FLOAT,
                        TypeTags.BOOLEAN, TypeTags.STRING, TypeTags.NIL, TypeTags.JSON));
                visibleSymbols.forEach(symbolInfo -> {
                    BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
                    if (bSymbol instanceof BTypeSymbol && typeTagsList.contains(bSymbol.getType().tag)) {
                        completionItems.add(getMatchFieldSnippetCompletion(
                                CommonUtil.getBTypeName(bSymbol.getType(), ctx)));
                    }
                });
                break;
            }
            case RECORD: {
                visibleSymbols.forEach(symbolInfo -> {
                    BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
                    if ((bSymbol instanceof BObjectTypeSymbol || bSymbol instanceof BRecordTypeSymbol)) {
                        completionItems.add(getMatchFieldSnippetCompletion(
                                CommonUtil.getBTypeName(bSymbol.getType(), ctx)));
                    }
                });
                break;
            }
            default:
                break;
        }
        
        return completionItems;
    }

    private CompletionItem getMatchFieldSnippetCompletion(String type) {
        CompletionItem completionItem = new CompletionItem();
        String insertText = type + " => {" +
                CommonUtil.LINE_SEPARATOR +
                "\t\t" +
                CommonUtil.LINE_SEPARATOR +
                "}";
        completionItem.setInsertText(insertText);
        completionItem.setLabel(type);
        completionItem.setInsertTextFormat(InsertTextFormat.Snippet);
        completionItem.setDetail(ItemResolverConstants.SNIPPET_TYPE);
        completionItem.setKind(CompletionItemKind.Snippet);

        return completionItem;
    }
}
