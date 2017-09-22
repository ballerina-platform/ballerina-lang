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

package org.ballerinalang.composer.service.workspace.langserver.util.resolvers;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.composer.service.workspace.langserver.SymbolInfo;
import org.ballerinalang.composer.service.workspace.langserver.dto.CompletionItem;
import org.ballerinalang.composer.service.workspace.langserver.dto.CompletionItemData;
import org.ballerinalang.composer.service.workspace.suggetions.SuggestionsFilterDataModel;
import org.ballerinalang.model.BallerinaFunction;
import org.ballerinalang.model.NamespaceDeclaration;
import org.ballerinalang.model.NativeUnit;
import org.ballerinalang.model.ParameterDef;
import org.ballerinalang.model.StructDef;
import org.ballerinalang.model.VariableDef;
import org.ballerinalang.model.types.SimpleTypeName;
import org.ballerinalang.natives.NativePackageProxy;
import org.ballerinalang.natives.NativeUnitProxy;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Interface for completion item resolvers
 */
public abstract class AbstractItemResolver {
    public abstract ArrayList<CompletionItem> resolveItems(SuggestionsFilterDataModel dataModel,
                                                           ArrayList<SymbolInfo> symbols, HashMap<Class,
            AbstractItemResolver> resolvers);

    /**
     * Populate the completion item list by considering the
     * @param symbolInfoList - list of symbol information
     * @param completionItems - completion item list to populate
     */
    public void populateCompletionItemList(List<SymbolInfo> symbolInfoList, List<CompletionItem> completionItems) {

        symbolInfoList.forEach(symbolInfo -> {
            CompletionItem completionItem = null;
            if (symbolInfo.getSymbol() instanceof NativeUnitProxy
                    && symbolInfo.getSymbolName().contains("ClientConnector")) {
                completionItem = this.populateClientConnectorCompletionItem(symbolInfo);
            } else if (symbolInfo.getSymbol() instanceof NativeUnitProxy) {
                completionItem = this.populateNativeUnitProxyCompletionItem(symbolInfo);
            } else if (symbolInfo.getScopeEntry().symbol instanceof BInvokableSymbol) {
                completionItem = this.populateBallerinaFunctionCompletionItem(symbolInfo);
            } else if (symbolInfo.getSymbol() instanceof NativePackageProxy) {
                completionItem = this.populateNativePackageProxyCompletionItem(symbolInfo);
            } else if (symbolInfo.getScopeEntry().symbol instanceof BVarSymbol) {
                completionItem = this.populateVariableDefCompletionItem(symbolInfo);
            } else if (symbolInfo.getScopeEntry().symbol instanceof BTypeSymbol) {
                completionItem = this.populateBTypeCompletionItem(symbolInfo);
            } else if (symbolInfo.getSymbol() instanceof NamespaceDeclaration) {
                completionItem = this.populateNamespaceDeclarationCompletionItem(symbolInfo);
            }

            if (completionItem != null) {
                completionItems.add(completionItem);
            }
        });
    }

    /**
     * Populate the Client Connector Completion Item
     * @param symbolInfo - symbol information
     * @return completion item
     */
    CompletionItem populateClientConnectorCompletionItem(SymbolInfo symbolInfo) {
        CompletionItem completionItem = new CompletionItem();
        completionItem.setLabel(symbolInfo.getSymbolName());
        String[] delimiterSeparatedTokens = (symbolInfo.getSymbolName()).split("\\.");
        completionItem.setInsertText(delimiterSeparatedTokens[delimiterSeparatedTokens.length - 1]);
        completionItem.setDetail(ItemResolverConstants.ACTION_TYPE);
        completionItem.setSortText(ItemResolverConstants.PRIORITY_2);
        completionItem.setKind(ItemResolverConstants.ACTION_KIND);

        return completionItem;
    }

    /**
     * Populate the Native Proxy Completion Item
     * @param symbolInfo - symbol information
     * @return completion item
     */
    CompletionItem populateNativeUnitProxyCompletionItem(SymbolInfo symbolInfo) {
        CompletionItem completionItem = new CompletionItem();
        String[] delimiterSeparatedTokens = (symbolInfo.getSymbolName()).split("\\.");
        completionItem.setInsertText(delimiterSeparatedTokens[delimiterSeparatedTokens.length - 1]);
        NativeUnit nativeUnit = ((NativeUnitProxy) symbolInfo.getSymbol()).load();
        completionItem.setLabel(getSignature(symbolInfo, nativeUnit));
        completionItem.setDetail(ItemResolverConstants.FUNCTION_TYPE);
        completionItem.setSortText(ItemResolverConstants.PRIORITY_5);

        return completionItem;
    }

    /**
     * Populate the Ballerina Function Completion Item
     * @param symbolInfo - symbol information
     * @return completion item
     */
    CompletionItem populateBallerinaFunctionCompletionItem(SymbolInfo symbolInfo) {
        CompletionItem completionItem = new CompletionItem();
        BInvokableSymbol bInvokableSymbol = (BInvokableSymbol) symbolInfo.getScopeEntry().symbol;
        if (bInvokableSymbol.getName().getValue().equals(".<init>") ||
                bInvokableSymbol.getName().getValue().equals("main")) {
            return null;
        }
        FunctionSignature functionSignature = getFunctionSignature(bInvokableSymbol);
        completionItem.setLabel(functionSignature.getLabel());
        completionItem.setInsertText(functionSignature.getInsertText());
        completionItem.setDetail(ItemResolverConstants.FUNCTION_TYPE);
        completionItem.setSortText(ItemResolverConstants.PRIORITY_6);
        completionItem.setKind(ItemResolverConstants.FUNCTION_KIND);

        return completionItem;
    }

    /**
     * Populate the Native Package Completion Item
     * @param symbolInfo - symbol information
     * @return completion item
     */
    CompletionItem populateNativePackageProxyCompletionItem(SymbolInfo symbolInfo) {
        CompletionItem completionItem = new CompletionItem();
        completionItem.setLabel(symbolInfo.getSymbolName());
        String[] delimiterSeparatedTokens = (symbolInfo.getSymbolName()).split("\\.");
        completionItem.setInsertText(delimiterSeparatedTokens[delimiterSeparatedTokens.length - 1]);
        completionItem.setDetail(ItemResolverConstants.PACKAGE_TYPE);
        completionItem.setSortText(ItemResolverConstants.PRIORITY_4);
        completionItem.setKind(ItemResolverConstants.PACKAGE_KIND);

        return completionItem;
    }

    /**
     * Populate the Variable Definition Completion Item
     * @param symbolInfo - symbol information
     * @return completion item
     */
    CompletionItem populateVariableDefCompletionItem(SymbolInfo symbolInfo) {
        CompletionItem completionItem = new CompletionItem();
        completionItem.setLabel(symbolInfo.getSymbolName());
        String[] delimiterSeparatedTokens = (symbolInfo.getSymbolName()).split("\\.");
        completionItem.setInsertText(delimiterSeparatedTokens[delimiterSeparatedTokens.length - 1]);
        String typeName = symbolInfo.getScopeEntry().symbol.type.toString();
        completionItem.setDetail((typeName.equals("")) ? ItemResolverConstants.NONE : typeName);

        CompletionItemData data = new CompletionItemData();
//        data.addData("type", type);
        completionItem.setData(data);

        completionItem.setSortText(ItemResolverConstants.PRIORITY_7);
        completionItem.setKind(ItemResolverConstants.VAR_DEF_KIND);

        return completionItem;
    }

    /**
     * Populate the Struct Definition Completion Item
     * @param symbolInfo - symbol information
     * @return completion item
     */
    CompletionItem populateStructDefCompletionItem(SymbolInfo symbolInfo) {
        CompletionItem completionItem = new CompletionItem();
        completionItem.setLabel(symbolInfo.getSymbolName());
        String[] delimiterSeparatedTokens = (symbolInfo.getSymbolName()).split("\\.");
        completionItem.setInsertText(delimiterSeparatedTokens[delimiterSeparatedTokens.length - 1]);
        completionItem.setLabel(((StructDef) symbolInfo.getSymbol()).getName());
        completionItem.setDetail(ItemResolverConstants.STRUCT_TYPE);
        completionItem.setSortText(ItemResolverConstants.PRIORITY_6);
        completionItem.setKind(ItemResolverConstants.STRUCT_KIND);

        return completionItem;
    }

    /**
     * Populate the BType Completion Item
     * @param symbolInfo - symbol information
     * @return completion item
     */
    CompletionItem populateBTypeCompletionItem(SymbolInfo symbolInfo) {
        CompletionItem completionItem = new CompletionItem();
        completionItem.setLabel(symbolInfo.getSymbolName());
        String[] delimiterSeparatedTokens = (symbolInfo.getSymbolName()).split("\\.");
        completionItem.setInsertText(delimiterSeparatedTokens[delimiterSeparatedTokens.length - 1]);
        completionItem.setDetail(ItemResolverConstants.B_TYPE);
        completionItem.setKind(ItemResolverConstants.B_TYPE_KIND);

        return completionItem;
    }

    /**
     * Populate the Namespace declaration Completion Item
     * @param symbolInfo - symbol information
     * @return completion item
     */
    CompletionItem populateNamespaceDeclarationCompletionItem(SymbolInfo symbolInfo) {
        CompletionItem completionItem = new CompletionItem();
        completionItem.setLabel(symbolInfo.getSymbolName());
        String[] delimiterSeparatedTokens = (symbolInfo.getSymbolName()).split("\\.");
        completionItem.setInsertText(delimiterSeparatedTokens[delimiterSeparatedTokens.length - 1]);
        completionItem.setDetail(ItemResolverConstants.XMLNS);

        return completionItem;
    }

    /**
     * Get the function signature for the native unit
     * @param symbolInfo - symbol info instance
     * @param nativeUnit - native unit instance
     * @return {@link String}
     */
    private String getSignature(SymbolInfo symbolInfo, NativeUnit nativeUnit) {
        StringBuffer signature = new StringBuffer(symbolInfo.getSymbolName());
        int i = 0;
        String initString = "";
        for (SimpleTypeName simpleTypeName : nativeUnit.getArgumentTypeNames()) {
            signature.append(initString).append(simpleTypeName.getName()).append(" ").
                    append(nativeUnit.getArgumentNames()[i]);
            ++i;
            initString = ", ";
        }
        signature.append(")");
        initString = "(";
        String endString = "";
        for (SimpleTypeName simpleTypeName : nativeUnit.getReturnParamTypeNames()) {
            signature.append(initString).append(simpleTypeName.getName());
            initString = ", ";
            endString = ")";
        }
        signature.append(endString);
        return signature.toString();
    }

    /**
     * Get the function signature
     * @param bInvokableSymbol - ballerina function instance
     * @return {@link String}
     */
    private FunctionSignature getFunctionSignature(BInvokableSymbol bInvokableSymbol) {
        StringBuffer signature = new StringBuffer(bInvokableSymbol.getName() + "(");
        StringBuffer insertText = new StringBuffer(bInvokableSymbol.getName() + "(");
        List<BVarSymbol> parameterDefs = bInvokableSymbol.getParameters();

        for (int itr = 0; itr < parameterDefs.size(); itr++) {
            signature.append(parameterDefs.get(itr).getType().toString()).append(" ")
                    .append(parameterDefs.get(itr).getName());
            insertText.append("${")
                    .append((itr + 1))
                    .append(":")
                    .append(parameterDefs.get(itr).getName())
                    .append("}");
            if (itr < parameterDefs.size() - 1) {
                signature.append(", ");
                insertText.append(", ");
            }
        }
        signature.append(")");
        insertText.append(")");
        String initString = "(";
        String endString = "";

        List<BType> returnTypes = bInvokableSymbol.type.getReturnTypes();
        List<BVarSymbol> returnParams = bInvokableSymbol.getReturnParameters();
        for (int itr = 0; itr < returnTypes.size(); itr ++) {
            signature.append(initString).append(returnTypes.get(itr).toString());
            if (returnParams.size() > itr) {
                signature.append(" ").append(returnParams.get(itr).getName());
            }
            initString = ", ";
            endString = ")";
        }
        signature.append(endString);
        return new FunctionSignature(insertText.toString(), signature.toString());
    }

    /**
     * Check whether the token stream corresponds to a action invocation or a function invocation
     * @param dataModel - Suggestions filter data model
     * @return {@link Boolean}
     */
    public boolean isActionOrFunctionInvocationStatement(SuggestionsFilterDataModel dataModel) {
        TokenStream tokenStream = dataModel.getTokenStream();
        int currentTokenIndex = dataModel.getTokenIndex();
        int searchTokenIndex = currentTokenIndex + 1;

        ArrayList<String> terminalTokens = new ArrayList<>(Arrays.asList(new String[]{";", "}", "{"}));
        while (true) {
            if (searchTokenIndex >= tokenStream.size()) {
                return false;
            }
            String tokenString = tokenStream.get(searchTokenIndex).getText();
            if (tokenString.equals(".") || tokenString.equals(":")) {
                return true;
            } else if (terminalTokens.contains(tokenString)) {
                return false;
            } else {
                searchTokenIndex++;
            }
        }
    }

    /**
     * Check whether the token stream contains an annotation start (@)
     * @param dataModel - Suggestions filter data model
     * @return {@link Boolean}
     */
    public boolean isAnnotationContext(SuggestionsFilterDataModel dataModel) {
        return findPreviousToken(dataModel, "@", 3) >= 0;
    }

    /**
     * Get the index of the equal sign
     * @param dataModel - suggestions filter data model
     * @return {@link Integer}
     */
    public int isPreviousTokenEqualSign(SuggestionsFilterDataModel dataModel) {
        int equalSignIndex;
        int searchTokenIndex = dataModel.getTokenIndex() - 1;
        TokenStream tokenStream = dataModel.getTokenStream();

        while (true) {
            if (searchTokenIndex > -1) {
                Token token = tokenStream.get(searchTokenIndex);
                String tokenStr = token.getText();

                // If the token's channel is verbose channel we skip to the next token
                if (token.getChannel() != 0) {
                    searchTokenIndex--;
                } else if (tokenStr.equals("=")) {
                    equalSignIndex = searchTokenIndex;
                    break;
                } else {
                    // In this case the token channel is the default channel and also not the equal sign token.
                    equalSignIndex = -1;
                    break;
                }
            } else {
                equalSignIndex = -1;
                break;
            }
        }

        return equalSignIndex;
    }

    /**
     * Inner static class for the Function Signature. This holds both the insert text and the label for the FUnction
     * Signature Completion Item
     */
    private static class FunctionSignature {
        private String insertText;
        private String label;

        FunctionSignature(String insertText, String label) {
            this.insertText = insertText;
            this.label = label;
        }

        String getInsertText() {
            return insertText;
        }

        public void setInsertText(String insertText) {
            this.insertText = insertText;
        }

        String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }

    protected int findPreviousToken(SuggestionsFilterDataModel dataModel, String needle, int maxSteps) {
        TokenStream tokenStream = dataModel.getTokenStream();
        if (tokenStream == null) {
            return -1;
        }
        int searchIndex = dataModel.getTokenIndex() - 1;

        while (maxSteps > 0) {
            if (searchIndex < 0) {
                return -1;
            }
            Token token = tokenStream.get(searchIndex);
            if (token.getChannel() == 0) {
                if (token.getText().equals(needle)) {
                    return searchIndex;
                }
                maxSteps--;
            }
            searchIndex--;
        }
        return -1;
    }

    /**
     * Assign the Priorities to the completion items
     * @param itemPriorityMap - Map of item priorities against the Item type
     * @param completionItems - list of completion items
     */
    public void assignItemPriorities(HashMap<String, Integer> itemPriorityMap, List<CompletionItem> completionItems) {
        completionItems.forEach(completionItem -> {
            if (itemPriorityMap.containsKey(completionItem.getDetail())) {
                completionItem.setSortText(itemPriorityMap.get(completionItem.getDetail()));
            }
        });
    }

    /**
     * Populate a completion item with the given data and return it
     * @param insertText insert text
     * @param type type of the completion item
     * @param priority completion item priority
     * @param label completion item label
     * @return {@link CompletionItem}
     */
    protected CompletionItem populateCompletionItem(String insertText, String type, int priority, String label) {
        CompletionItem completionItem = new CompletionItem();
        completionItem.setInsertText(insertText);
        completionItem.setDetail(type);
        completionItem.setSortText(priority);
        completionItem.setLabel(label);
        return completionItem;
    }

    protected void populateBasicTypes(List<CompletionItem> completionItems, SymbolTable symbolTable) {
        symbolTable.rootScope.entries.forEach((key, value) -> {
            if (value.symbol instanceof BTypeSymbol) {
                String insertText = value.symbol.getName().getValue();
                completionItems.add(populateCompletionItem(insertText, ItemResolverConstants.B_TYPE,
                        ItemResolverConstants.PRIORITY_4, insertText));
            }
        });
    }
}
