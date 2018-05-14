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
import java.util.List;
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
            CompletionItem completionItem = new CompletionItem();
            String memberTypesSnippet = getMatchExpressionMemberTypesSnippet((BUnionType) bType,
                    getExpectedReturnType(symbolEnvNode.parent));
            completionItem.setInsertText(memberTypesSnippet);
            completionItem.setLabel(bType.toString());
            completionItem.setDetail(bType.toString());
            completionItem.setKind(CompletionItemKind.Unit);
            completionItem.setInsertTextFormat(InsertTextFormat.Snippet);
            completionItems.add(completionItem);
        }

        return completionItems;
    }

    private String getMatchExpressionMemberTypesSnippet(BUnionType bUnionType, BType parentType) {
        Set<BType> memberTypes = bUnionType.getMemberTypes();
        List<String> memberTypeSnippets = new ArrayList<>();
        int placeholderCounter = 1;
        for (BType bType : memberTypes) {
            String defaultType = CommonUtil.getDefaultValueForType(parentType);
            memberTypeSnippets.add(bType.toString() + " => ${" + placeholderCounter + ":" + defaultType + "}");
            placeholderCounter++;
        }

        return String.join("," + CommonUtil.LINE_SEPARATOR, memberTypeSnippets);
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
