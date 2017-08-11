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

package org.ballerinalang.composer.service.workspace.langserver.util.filters;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.composer.service.workspace.langserver.SymbolInfo;
import org.ballerinalang.composer.service.workspace.langserver.dto.CompletionItem;
import org.ballerinalang.composer.service.workspace.langserver.util.resolvers.ItemResolverConstants;
import org.ballerinalang.composer.service.workspace.suggetions.SuggestionsFilterDataModel;
import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.NativeUnit;
import org.ballerinalang.model.symbols.BLangSymbol;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.SimpleTypeName;
import org.ballerinalang.natives.NativePackageProxy;
import org.ballerinalang.natives.NativeUnitProxy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filter the actions and the functions in a package
 */
public class PackageActionAndFunctionFilter implements SymbolFilter {
    @Override
    public List<SymbolInfo> filterItems(SuggestionsFilterDataModel dataModel, ArrayList<SymbolInfo> symbols,
                                        HashMap<String, Object> properties) {

        int currentTokenIndex = dataModel.getTokenIndex();
        TokenStream tokenStream = dataModel.getTokenStream();
        String searchTokenString = tokenStream.get(currentTokenIndex).getText();
        int delimiterIndex = currentTokenIndex + 1;
        String delimiter = tokenStream.get(delimiterIndex).getText();
        int colonTokenIndex;

        if (delimiter.equals(":")) {
            colonTokenIndex = delimiterIndex;
        } else {
            int initTokenIndex = this.getIndexOfTokenString(searchTokenString, 0, dataModel);
            int equalTokenIndex = this.getIndexOfTokenString("create", initTokenIndex, dataModel);
            colonTokenIndex = this.getIndexOfTokenString(":", equalTokenIndex, dataModel);
        }

        ArrayList<SymbolInfo> returnSymbolsInfoList = new ArrayList<>();

        if (colonTokenIndex > -1) {
            String packageName = tokenStream.get(colonTokenIndex - 1).getText();

            List<SymbolInfo> searchList = symbols.stream()
                .filter(symbolInfo -> !(symbolInfo.getSymbol() instanceof BType)).collect(Collectors.toList());

            List<SymbolInfo> filteredSymbolInfoList = searchList.stream()
                    .filter(
                            symbolInfo -> symbolInfo.getSymbolName().contains(packageName)
                                    && (symbolInfo.getSymbol() instanceof NativePackageProxy
                                    || symbolInfo.getSymbol() instanceof NativeUnitProxy)
                    ).collect(Collectors.toList());

            for (SymbolInfo aFilteredSymbolInfoList : filteredSymbolInfoList) {
                if (aFilteredSymbolInfoList.getSymbol() instanceof NativePackageProxy) {
                    BLangPackage bLangPackage =
                            ((NativePackageProxy) aFilteredSymbolInfoList.getSymbol()).load();
                    bLangPackage.getSymbolMap().forEach((k, v) -> {
                        SymbolInfo symbolInfo = new SymbolInfo(k.getName(), v);
                        returnSymbolsInfoList.add(symbolInfo);
                    });
                } else if (aFilteredSymbolInfoList.getSymbol() instanceof NativeUnitProxy) {
                    NativeUnit nativeUnit = ((NativeUnitProxy) aFilteredSymbolInfoList.getSymbol()).load();
                    SymbolInfo symbolInfo = new SymbolInfo(((BLangSymbol) nativeUnit).getName(),
                            ((BLangSymbol) nativeUnit));
                    returnSymbolsInfoList.add(symbolInfo);
                }
            }
        }

        return returnSymbolsInfoList;
    }

    /**
     * Get the list of completion Items from the symbolInfo list
     * @return {@link ArrayList}
     */
    public ArrayList<CompletionItem> getCompletionItems(List<SymbolInfo> symbolInfoList,
                                                        SuggestionsFilterDataModel dataModel) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        boolean canSuggestClientConnector = false;
        String tokenStr = dataModel.getTokenStream().get(dataModel.getTokenIndex()).getText();

        // This is for the case where we have the code segment such as http:, xmls:, etc.. current token is http or xmls
        // in such cases we assume that the next token is for either : or .
        // TODO: Refactor after integrating the semantic analyzer
        if (!tokenStr.equals(":") || !tokenStr.equals(".")) {
            tokenStr = dataModel.getTokenStream().get(dataModel.getTokenIndex() + 1).getText();
        }
        List<SymbolInfo> symbolInfos = new ArrayList<>();
        List<SymbolInfo> connectorActions = symbolInfoList.stream()
                .filter(symbolInfo -> symbolInfo.getSymbolName().contains(".ClientConnector."))
                .collect(Collectors.toList());
        if (tokenStr.equals(":")) {
            symbolInfos = symbolInfoList.stream()
                    .filter(symbolInfo -> !symbolInfo.getSymbolName().contains(".ClientConnector."))
                    .collect(Collectors.toList());

            // TODO: Need to find an alternative way to find this
            if (connectorActions.size() > 0) {
                canSuggestClientConnector = true;
            }
        } else {
            symbolInfos.addAll(connectorActions);
        }

        for (SymbolInfo symbolInfo : symbolInfos) {
            CompletionItem completionItem = new CompletionItem();
            FunctionSignature functionSignature;
            if (symbolInfo.getSymbolName().contains(".ClientConnector.")) {
                String[] tokens = symbolInfo.getSymbolName().split("\\.");
                functionSignature = this.getFunctionSignature(tokens[tokens.length - 1],
                        ((NativeUnitProxy) symbolInfo.getSymbol()).load());
                completionItem.setInsertText(functionSignature.getInsertText());
                completionItem.setLabel(functionSignature.getLabel());
                completionItem.setDetail(ItemResolverConstants.ACTION_TYPE);
                completionItem.setSortText(ItemResolverConstants.PRIORITY_5);
            } else {
                functionSignature = this.getFunctionSignature(symbolInfo.getSymbolName(),
                        ((NativeUnitProxy) symbolInfo.getSymbol()).load());
                completionItem.setInsertText(functionSignature.getInsertText());
                completionItem.setLabel(functionSignature.getLabel());
                completionItem.setDetail(ItemResolverConstants.FUNCTION_TYPE);
                completionItem.setSortText(ItemResolverConstants.PRIORITY_6);
            }
            completionItems.add(completionItem);
        }

        completionItems.sort(Comparator.comparing(CompletionItem::getLabel));

        if (canSuggestClientConnector) {
            CompletionItem clientConnectorType = new CompletionItem();
            clientConnectorType.setInsertText("ClientConnector");
            clientConnectorType.setLabel("ClientConnector");
            clientConnectorType.setDetail(ItemResolverConstants.CLIENT_CONNECTOR_TYPE);
            clientConnectorType.setSortText(ItemResolverConstants.PRIORITY_7);
            completionItems.add(0, clientConnectorType);
        }
        return completionItems;
    }

    /**
     * Get the function signature
     * @param functionName - function name
     * @param nativeUnit - Native Unit
     * @return {@link FunctionSignature}
     */
    private FunctionSignature getFunctionSignature(String functionName, NativeUnit nativeUnit) {
        StringBuilder signature = new StringBuilder(functionName + "(");
        StringBuilder signatureInsertText = new StringBuilder(functionName + "(");

        // Create the parameter list
        SimpleTypeName[] argumentTypeNames = nativeUnit.getArgumentTypeNames();
        String[] argumentNames = nativeUnit.getArgumentNames();
        for (int itr = 0; itr < argumentTypeNames.length; itr++) {
            signature.append(argumentTypeNames[itr])
                    .append(" ")
                    .append(argumentNames[itr]);

            signatureInsertText.append("${")
                    .append(itr + 1)
                    .append(":")
                    .append(argumentNames[itr])
                    .append("}");
            if (itr < argumentTypeNames.length - 1) {
                signature.append(", ");
                signatureInsertText.append(", ");
            }
        }
        signature.append(")");
        signatureInsertText.append(");");

        SimpleTypeName[] returnParamTypeNames = nativeUnit.getReturnParamTypeNames();
        StringBuilder returnParams = new StringBuilder("(");

        for (int itr = 0; itr < returnParamTypeNames.length; itr++) {
            returnParams.append(returnParamTypeNames[itr].getName());
            if (itr != returnParamTypeNames.length - 1) {
                returnParams.append(", ");
            }
        }
        returnParams.append(")");
        signature.append(returnParams.toString());
        return new FunctionSignature(signatureInsertText.toString(), signature.toString());
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

    /**
     * Get the index of a certain token
     * @param tokenString - token string
     * @param from - start searching from
     * @param dataModel - suggestions filter data model
     * @return {@link Integer}
     */
    private int getIndexOfTokenString(String tokenString, int from, SuggestionsFilterDataModel dataModel) {
        TokenStream tokenStream = dataModel.getTokenStream();
        int resultTokenIndex = -1;
        int searchIndex = from;

        while (true) {
            if (searchIndex < 0 || tokenStream.size() - 1 < searchIndex) {
                break;
            }
            Token token = tokenStream.get(searchIndex);
            if (token.getChannel() != Token.DEFAULT_CHANNEL || !token.getText().equals(tokenString)) {
                searchIndex++;
            } else {
                resultTokenIndex = searchIndex;
                break;
            }
        }

        return resultTokenIndex;
    }
}
