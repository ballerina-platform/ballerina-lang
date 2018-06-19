/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE2.0
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
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.util.filters.PackageActionFunctionAndTypesFilter;
import org.ballerinalang.langserver.completions.util.filters.SymbolFilters;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.List;

/**
 * BLangEndpoint context Item Resolver.
 */
public class BLangEndpointContextResolver extends AbstractItemResolver {
    
    private static final String INIT = "init";
    
    @Override
    @SuppressWarnings("unchecked")
    public ArrayList<CompletionItem> resolveItems(LSServiceOperationContext completionContext) {
        BLangNode bLangEndpoint = completionContext.get(CompletionKeys.SYMBOL_ENV_NODE_KEY);
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        List<BAttachedFunction> attachedFunctions = new ArrayList<>();

        if (bLangEndpoint instanceof BLangEndpoint) {
            BLangExpression configurationExpr = ((BLangEndpoint) bLangEndpoint).configurationExpr;
            int cursorLine = completionContext.get(DocumentServiceKeys.POSITION_KEY).getPosition().getLine();
            if (configurationExpr instanceof BLangRecordLiteral) {
                List<BLangRecordLiteral.BLangRecordKeyValue> keyValuePairs =
                        ((BLangRecordLiteral) configurationExpr).getKeyValuePairs();
                for (BLangRecordLiteral.BLangRecordKeyValue keyValuePair : keyValuePairs) {
                    BLangExpression valueExpr = keyValuePair.valueExpr;
                    DiagnosticPos valuePos = CommonUtil.toZeroBasedPosition(valueExpr.getPosition());
                    if (valuePos.getStartLine() == cursorLine) {
                        if (isInvocationOrFieldAccess(completionContext)) {
                            Either<List<CompletionItem>, List<SymbolInfo>> filteredItems =
                                    SymbolFilters.getFilterByClass(PackageActionFunctionAndTypesFilter.class)
                                            .filterItems(completionContext);
                            if (filteredItems.isLeft()) {
                                completionItems.addAll(filteredItems.getLeft());
                            } else {
                                this.populateCompletionItemList(filteredItems.getRight(), completionItems);
                            }
                        } else {
                            completionItems.addAll(this.getVariableDefinitionCompletionItems(completionContext));
                        }

                        return completionItems;
                    }
                }
            }
        }
        
        if (bLangEndpoint instanceof  BLangEndpoint
                && ((BLangEndpoint) bLangEndpoint).type.tsymbol instanceof BObjectTypeSymbol) {
            attachedFunctions.addAll(((BObjectTypeSymbol) ((BLangEndpoint) bLangEndpoint).type.tsymbol).attachedFuncs);
        }

        BAttachedFunction initFunction = attachedFunctions.stream()
                .filter(bAttachedFunction -> bAttachedFunction.funcName.getValue().equals(INIT))
                .findFirst()
                .orElseGet(null);

        BVarSymbol configSymbol = initFunction.symbol.getParameters().get(0);

        BType configSymbolType = configSymbol.getType();
        if (configSymbolType instanceof BRecordType) {
            completionItems.addAll(
                    CommonUtil.getStructFieldPopulateCompletionItems(((BRecordType) configSymbolType).getFields())
            );
            completionItems.add(CommonUtil.getFillAllStructFieldsItem(((BRecordType) configSymbolType).getFields()));
        }

        return completionItems;
    }
}
