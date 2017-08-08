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
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.SimpleTypeName;
import org.ballerinalang.natives.NativePackageProxy;
import org.ballerinalang.natives.NativeUnitProxy;

import java.util.ArrayList;
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
            CompletionItem completionItem = new CompletionItem();
            completionItem.setLabel(symbolInfo.getSymbolName());
            String[] delimiterSeparatedTokens = (symbolInfo.getSymbolName()).split("\\.");
            completionItem.setInsertText(delimiterSeparatedTokens[delimiterSeparatedTokens.length - 1]);
            if (symbolInfo.getSymbol() instanceof NativeUnitProxy
                    && symbolInfo.getSymbolName().contains("ClientConnector")) {
                this.populateClientConnectorCompletionItem(completionItem, symbolInfo);
            } else if (symbolInfo.getSymbol() instanceof NativeUnitProxy) {
                this.populateNativeUnitProxyCompletionItem(completionItem, symbolInfo);
            } else if (symbolInfo.getSymbol() instanceof BallerinaFunction) {
                this.populateBallerinaFunctionCompletionItem(completionItem, symbolInfo);
            } else if (symbolInfo.getSymbol() instanceof NativePackageProxy) {
                this.populateNativePackageProxyCompletionItem(completionItem, symbolInfo);
            } else if (symbolInfo.getSymbol() instanceof VariableDef) {
                this.populateVariableDefCompletionItem(completionItem, symbolInfo);
            } else if ((symbolInfo.getSymbol() instanceof StructDef)) {
                this.populateStructDefCompletionItem(completionItem, symbolInfo);
            } else if (symbolInfo.getSymbol() instanceof BType) {
                this.populateBTypeCompletionItem(completionItem, symbolInfo);
            } else if (symbolInfo.getSymbol() instanceof NamespaceDeclaration) {
                this.populateNamespaceDeclarationCompletionItem(completionItem, symbolInfo);
            }
            completionItems.add(completionItem);
        });
    }

    /**
     * Populate the Client Connector Completion Item
     * @param completionItem - completion item
     * @param symbolInfo - symbol information
     */
    void populateClientConnectorCompletionItem(CompletionItem completionItem, SymbolInfo symbolInfo) {
        completionItem.setDetail(ItemResolverConstants.ACTION_TYPE);
        completionItem.setSortText(ItemResolverConstants.PRIORITY_2);
        completionItem.setKind(ItemResolverConstants.ACTION_KIND);
    }

    /**
     * Populate the Native Proxy Completion Item
     * @param completionItem - completion item
     * @param symbolInfo - symbol information
     */
    void populateNativeUnitProxyCompletionItem(CompletionItem completionItem, SymbolInfo symbolInfo) {
        NativeUnit nativeUnit = ((NativeUnitProxy) symbolInfo.getSymbol()).load();
        completionItem.setLabel(getSignature(symbolInfo, nativeUnit));
        completionItem.setDetail(ItemResolverConstants.FUNCTION_TYPE);
        completionItem.setSortText(ItemResolverConstants.PRIORITY_5);
    }

    /**
     * Populate the Ballerina Function Completion Item
     * @param completionItem - completion item
     * @param symbolInfo - symbol information
     */
    void populateBallerinaFunctionCompletionItem(CompletionItem completionItem, SymbolInfo symbolInfo) {
        completionItem.setLabel(getFunctionSignature((BallerinaFunction) symbolInfo.getSymbol()).getLabel());
        completionItem.setInsertText(getFunctionSignature((BallerinaFunction) symbolInfo.getSymbol()).getInsertText());
        completionItem.setDetail(ItemResolverConstants.FUNCTION_TYPE);
        completionItem.setSortText(ItemResolverConstants.PRIORITY_6);
        completionItem.setKind(ItemResolverConstants.FUNCTION_KIND);
    }

    /**
     * Populate the Native Package Completion Item
     * @param completionItem - completion item
     * @param symbolInfo - symbol information
     */
    void populateNativePackageProxyCompletionItem(CompletionItem completionItem, SymbolInfo symbolInfo) {
        completionItem.setDetail(ItemResolverConstants.PACKAGE_TYPE);
        completionItem.setSortText(ItemResolverConstants.PRIORITY_4);
        completionItem.setKind(ItemResolverConstants.PACKAGE_KIND);
    }

    /**
     * Populate the Variable Definition Completion Item
     * @param completionItem - completion item
     * @param symbolInfo - symbol information
     */
    void populateVariableDefCompletionItem(CompletionItem completionItem, SymbolInfo symbolInfo) {
        String typeName = ((VariableDef) symbolInfo.getSymbol()).getTypeName().getName();
        completionItem.setDetail((typeName.equals("")) ? ItemResolverConstants.NONE : typeName);

        CompletionItemData data = new CompletionItemData();
        data.addData("type", ((VariableDef) symbolInfo.getSymbol()).getTypeName());
        completionItem.setData(data);

        completionItem.setSortText(ItemResolverConstants.PRIORITY_7);
        completionItem.setKind(ItemResolverConstants.VAR_DEF_KIND);
    }

    /**
     * Populate the Struct Definition Completion Item
     * @param completionItem - completion item
     * @param symbolInfo - symbol information
     */
    void populateStructDefCompletionItem(CompletionItem completionItem, SymbolInfo symbolInfo) {
        completionItem.setLabel(((StructDef) symbolInfo.getSymbol()).getName());
        completionItem.setDetail(ItemResolverConstants.STRUCT_TYPE);
        completionItem.setSortText(ItemResolverConstants.PRIORITY_6);
        completionItem.setKind(ItemResolverConstants.STRUCT_KIND);
    }

    /**
     * Populate the BType Completion Item
     * @param completionItem - completion item
     * @param symbolInfo - symbol information
     */
    void populateBTypeCompletionItem(CompletionItem completionItem, SymbolInfo symbolInfo) {
        completionItem.setDetail(ItemResolverConstants.B_TYPE);
        completionItem.setKind(ItemResolverConstants.B_TYPE_KIND);
    }

    /**
     * Populate the Namespace declaration Completion Item
     * @param completionItem - completion item
     * @param symbolInfo - symbol information
     */
    void populateNamespaceDeclarationCompletionItem(CompletionItem completionItem, SymbolInfo symbolInfo) {
        completionItem.setDetail(ItemResolverConstants.XMLNS);
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
     * @param ballerinaFunction - ballerina function instance
     * @return {@link String}
     */
    private FunctionSignature getFunctionSignature(BallerinaFunction ballerinaFunction) {
        StringBuffer signature = new StringBuffer(ballerinaFunction.getName() + "(");
        StringBuffer insertText = new StringBuffer(ballerinaFunction.getName() + "(");
        ParameterDef[] parameterDefs = ballerinaFunction.getParameterDefs();

        for (int itr = 0; itr < parameterDefs.length; itr++) {
            signature.append(parameterDefs[itr].getTypeName()).append(" ")
                    .append(parameterDefs[itr].getName());
            insertText.append("${")
                    .append((itr + 1))
                    .append(":")
                    .append(parameterDefs[itr].getName())
                    .append("}");
            if (itr < parameterDefs.length - 1) {
                signature.append(", ");
                insertText.append(", ");
            }
        }
        signature.append(")");
        insertText.append(")");
        String initString = "(";
        String endString = "";
        for (ParameterDef simpleTypeName : ballerinaFunction.getReturnParameters()) {
            signature.append(initString).append(simpleTypeName.getTypeName());
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
        boolean continueSearch = true;
        int searchIndex = currentTokenIndex + 1;

        while (continueSearch) {
            if (tokenStream != null && searchIndex < tokenStream.size()) {
                Token token = tokenStream.get(searchIndex);
                String tokenStr = token.getText();

                // return 'false' once we found the first token which is not in default channel
                if (token.getChannel() != Token.DEFAULT_CHANNEL ||
                        (tokenStr.equals(":") && tokenStream.get(searchIndex - 2) != null &&
                                tokenStream.get(searchIndex - 2).getText().equals("@"))) {
                    return false;
                }
                if (tokenStr.equals(":") || tokenStr.equals(".")) {
                    return true;
                }
                searchIndex++;
            } else {
                continueSearch = false;
            }
        }

        return false;
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
}
