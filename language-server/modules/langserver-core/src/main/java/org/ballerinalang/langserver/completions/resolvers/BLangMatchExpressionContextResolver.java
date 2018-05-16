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

import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Priority;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Completion Item resolver for the Match Expression.
 */
public class BLangMatchExpressionContextResolver extends AbstractItemResolver {
    @Override
    public ArrayList<CompletionItem> resolveItems(LSServiceOperationContext completionContext) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        BLangNode symbolEnvNode = completionContext.get(CompletionKeys.SYMBOL_ENV_NODE_KEY);
        BType bType = null;
        if (symbolEnvNode instanceof BLangMatchExpression) {
            BLangExpression bLangExpression = ((BLangMatchExpression) symbolEnvNode).expr;
            if (bLangExpression instanceof BLangInvocation
                    && ((BLangInvocation) bLangExpression).symbol instanceof BInvokableSymbol) {
                bType = ((BInvokableSymbol) (((BLangInvocation) bLangExpression).symbol)).retType;
            } else if (bLangExpression instanceof BLangSimpleVarRef) {
                bType = ((BLangSimpleVarRef) bLangExpression).type;
            }
        }
        
        if (bType instanceof BUnionType) {
            String matchSnippet = "Match ";
            CompletionItem allFillerItem = new CompletionItem();
            CompletionItem anyFillerItem = new CompletionItem();
            String defaultType = CommonUtil.getDefaultValueForType(getExpectedReturnType(symbolEnvNode.parent));
            Map<String, String> memberTypesSnippets = getMatchExpressionMemberTypesSnippets((BUnionType) bType,
                    defaultType);
            String allFieldFiller = String.join("," + CommonUtil.LINE_SEPARATOR, memberTypesSnippets.values());
            String anyFieldFiller = UtilSymbolKeys.ANY_KEYWORD_KEY + " => ${1:" + defaultType + "}";
            
            memberTypesSnippets.entrySet().forEach(entry -> {
                CompletionItem memberItem = new CompletionItem();
                memberItem.setLabel(matchSnippet + entry.getKey());
                memberItem.setDetail(ItemResolverConstants.SNIPPET_TYPE);
                memberItem.setInsertText(entry.getValue());
                memberItem.setKind(CompletionItemKind.Unit);
                memberItem.setInsertTextFormat(InsertTextFormat.Snippet);
                memberItem.setSortText(Priority.PRIORITY130.toString());
                completionItems.add(memberItem);
            });
            
            // Set the all field Filler completion Item
            allFillerItem.setInsertText(allFieldFiller);
            allFillerItem.setLabel(matchSnippet + bType.toString());
            allFillerItem.setDetail(ItemResolverConstants.SNIPPET_TYPE);
            allFillerItem.setKind(CompletionItemKind.Unit);
            allFillerItem.setInsertTextFormat(InsertTextFormat.Snippet);
            allFillerItem.setSortText(Priority.PRIORITY110.toString());

            // Set the any field Filler completion Item
            anyFillerItem.setInsertText(anyFieldFiller);
            anyFillerItem.setLabel(matchSnippet + UtilSymbolKeys.ANY_KEYWORD_KEY);
            anyFillerItem.setDetail(ItemResolverConstants.SNIPPET_TYPE);
            anyFillerItem.setKind(CompletionItemKind.Unit);
            anyFillerItem.setInsertTextFormat(InsertTextFormat.Snippet);
            anyFillerItem.setSortText(Priority.PRIORITY120.toString());
            
            completionItems.add(allFillerItem);
            completionItems.add(anyFillerItem);
        }

        return completionItems;
    }

    private Map<String, String> getMatchExpressionMemberTypesSnippets(BUnionType bUnionType, String defaultType) {
        Set<BType> memberTypes = bUnionType.getMemberTypes();
        Map<String, String> memberSnippets = new LinkedHashMap<>();
        int placeholderCounter = 1;
        for (BType bType : memberTypes) {
            memberSnippets.put(bType.toString(),
                    bType.toString() + " => ${" + placeholderCounter + ":" + defaultType + "}");
            placeholderCounter++;
        }

        return memberSnippets;
    }
    
    private BType getExpectedReturnType(BLangNode parentNode) {
        if (parentNode instanceof BLangVariable) {
            return parentNode.type;
        } else if (parentNode instanceof BLangAssignment) {
            return ((BLangAssignment) parentNode).varRef.type;
        }

        return null;
    }
}
