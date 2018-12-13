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
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.ballerinalang.langserver.completions.util.MatchStatementResolverUtil.generateMatchPattern;
import static org.ballerinalang.langserver.completions.util.MatchStatementResolverUtil.getStructuredFixedValueMatch;
import static org.ballerinalang.langserver.completions.util.MatchStatementResolverUtil.getTupleDestructured;
import static org.ballerinalang.langserver.completions.util.MatchStatementResolverUtil.getVariableValueDestructurePattern;

/**
 * Completion Item resolver for the BLangMatch Scope.
 */
public class BLangMatchContextResolver extends AbstractItemResolver {

    private static final String LABEL_STRUCTURED_FIXED = "Structured Fixed Value Match";
    private static final String LABEL_VARIABLE_VALUE = "Variable Value Destructure Match";

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
                        completionItems.addAll(getPatternClauseForType(bType, ctx)));
                break;
            }
            case RECORD:
            case TUPLE: {
                completionItems.addAll(this.getPatternClauseForType(bLangMatch.expr.type, ctx));
                break;
            }
            default: {
                completionItems.add(getMatchFieldSnippetCompletion(getVariableValueDestructurePattern(ctx),
                        CommonUtil.getBTypeName(bLangMatch.expr.type, ctx)));
                break;
            }
        }
        
        return completionItems;
    }

    private List<CompletionItem> getPatternClauseForType(BType bType, LSContext ctx) {
        List<CompletionItem> completionItems = new ArrayList<>();
        if (bType instanceof BTupleType) {
            String tupleDestructured = "var " + getTupleDestructured((BTupleType) bType,
                    new ArrayList<>(), ctx);
            String variableValuePattern = generateMatchPattern(tupleDestructured, ctx);
            String fixedValuePattern = generateMatchPattern(getStructuredFixedValueMatch(bType), ctx);
            completionItems.add(this.getMatchFieldSnippetCompletion(variableValuePattern,
                    bType.toString() + " : " + LABEL_VARIABLE_VALUE));
            completionItems.add(this.getMatchFieldSnippetCompletion(fixedValuePattern,
                    bType.toString() + " : " + LABEL_STRUCTURED_FIXED));
        } else if (bType instanceof BRecordType) {
            String fixedValuePattern = generateMatchPattern(getStructuredFixedValueMatch(bType), ctx);
            completionItems.add(this.getMatchFieldSnippetCompletion(fixedValuePattern,
                    bType.toString() + " : " + LABEL_STRUCTURED_FIXED));
        } else {
            String variableValuePattern = getVariableValueDestructurePattern(ctx);
            completionItems.add(this.getMatchFieldSnippetCompletion(variableValuePattern,
                    bType.toString() + " : " + LABEL_VARIABLE_VALUE));
        }

        return completionItems;
    }

    private CompletionItem getMatchFieldSnippetCompletion(String snippet, String label) {
        CompletionItem completionItem = new CompletionItem();
        completionItem.setInsertText(snippet);
        completionItem.setLabel(label);
        completionItem.setInsertTextFormat(InsertTextFormat.Snippet);
        completionItem.setDetail(ItemResolverConstants.SNIPPET_TYPE);
        completionItem.setKind(CompletionItemKind.Snippet);

        return completionItem;
    }
}
