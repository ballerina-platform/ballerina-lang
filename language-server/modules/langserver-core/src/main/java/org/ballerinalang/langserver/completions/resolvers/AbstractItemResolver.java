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

package org.ballerinalang.langserver.completions.resolvers;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.langserver.DocumentServiceKeys;
import org.ballerinalang.langserver.TextDocumentServiceContext;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.consts.ItemResolverConstants;
import org.ballerinalang.langserver.completions.consts.Priority;
import org.ballerinalang.model.symbols.SymbolKind;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Interface for completion item resolvers.
 */
public abstract class AbstractItemResolver {
    public abstract ArrayList<CompletionItem> resolveItems(TextDocumentServiceContext completionContext);

    /**
     * Populate the completion item list by considering the.
     * @param symbolInfoList - list of symbol information
     * @param completionItems - completion item list to populate
     */
    public void populateCompletionItemList(List<SymbolInfo> symbolInfoList, List<CompletionItem> completionItems) {

        symbolInfoList.forEach(symbolInfo -> {
            CompletionItem completionItem = null;
            if (symbolInfo.getScopeEntry().symbol instanceof BInvokableSymbol
                    && ((BInvokableSymbol) symbolInfo.getScopeEntry().symbol).kind != null
                    && !((BInvokableSymbol) symbolInfo.getScopeEntry().symbol).kind.equals(SymbolKind.WORKER)) {
                completionItem = this.populateBallerinaFunctionCompletionItem(symbolInfo);
            } else if (!(symbolInfo.getScopeEntry().symbol instanceof BInvokableSymbol)
                    && symbolInfo.getScopeEntry().symbol instanceof BVarSymbol) {
                completionItem = this.populateVariableDefCompletionItem(symbolInfo);
            } else if (symbolInfo.getScopeEntry().symbol instanceof BTypeSymbol) {
                completionItem = this.populateBTypeCompletionItem(symbolInfo);
            }

            if (completionItem != null) {
                completionItems.add(completionItem);
            }
        });
    }

    /**
     * Populate the Ballerina Function Completion Item.
     * @param symbolInfo - symbol information
     * @return completion item
     */
    private CompletionItem populateBallerinaFunctionCompletionItem(SymbolInfo symbolInfo) {
        CompletionItem completionItem = new CompletionItem();
        BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
        assert bSymbol instanceof BInvokableSymbol;
        BInvokableSymbol bInvokableSymbol = (BInvokableSymbol) bSymbol;
        if (bInvokableSymbol.getName().getValue().contains("<")
                || bInvokableSymbol.getName().getValue().contains("<") ||
                bInvokableSymbol.getName().getValue().equals("main")) {
            return null;
        }
        FunctionSignature functionSignature = getFunctionSignature(bInvokableSymbol);
        completionItem.setInsertTextFormat(InsertTextFormat.Snippet);
        completionItem.setLabel(functionSignature.getLabel());
        completionItem.setInsertText(functionSignature.getInsertText());
        completionItem.setDetail(ItemResolverConstants.FUNCTION_TYPE);
        completionItem.setSortText(Priority.PRIORITY6.name());
        completionItem.setKind(CompletionItemKind.Function);

        return completionItem;
    }

    /**
     * Populate the Variable Definition Completion Item.
     * @param symbolInfo - symbol information
     * @return completion item
     */
    private CompletionItem populateVariableDefCompletionItem(SymbolInfo symbolInfo) {
        CompletionItem completionItem = new CompletionItem();
        completionItem.setLabel(symbolInfo.getSymbolName());
        String[] delimiterSeparatedTokens = (symbolInfo.getSymbolName()).split("\\.");
        completionItem.setInsertText(delimiterSeparatedTokens[delimiterSeparatedTokens.length - 1]);
        String typeName = symbolInfo.getScopeEntry().symbol.type.toString();
        completionItem.setDetail((typeName.equals("")) ? ItemResolverConstants.NONE : typeName);

        completionItem.setSortText(Priority.PRIORITY7.name());
        completionItem.setKind(CompletionItemKind.Unit);

        return completionItem;
    }

    /**
     * Populate the BType Completion Item.
     * @param symbolInfo - symbol information
     * @return completion item
     */
    CompletionItem populateBTypeCompletionItem(SymbolInfo symbolInfo) {
        CompletionItem completionItem = new CompletionItem();
        completionItem.setLabel(symbolInfo.getSymbolName());
        String[] delimiterSeparatedTokens = (symbolInfo.getSymbolName()).split("\\.");
        completionItem.setInsertText(delimiterSeparatedTokens[delimiterSeparatedTokens.length - 1]);
        completionItem.setDetail(ItemResolverConstants.B_TYPE);

        return completionItem;
    }

    /**
     * Populate the Namespace declaration Completion Item.
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
     * Get the function signature.
     * @param bInvokableSymbol - ballerina function instance
     * @return {@link String}
     */
    private FunctionSignature getFunctionSignature(BInvokableSymbol bInvokableSymbol) {
        String functionName = bInvokableSymbol.getName().getValue();

        // If there is a receiver symbol, then the name comes with the package name and struct name appended.
        // Hence we need to remove it
        if (bInvokableSymbol.receiverSymbol != null) {
            String receiverType = bInvokableSymbol.receiverSymbol.getType().toString();
            functionName = functionName.replace(receiverType + ".", "");
        }
        StringBuffer signature = new StringBuffer(functionName + "(");
        StringBuffer insertText = new StringBuffer(functionName + "(");
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
        for (int itr = 0; itr < returnTypes.size(); itr++) {
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
     * Check whether the token stream corresponds to a action invocation or a function invocation.
     * @param documentServiceContext - Completion operation context
     * @return {@link Boolean}
     */
    protected boolean isActionOrFunctionInvocationStatement(TextDocumentServiceContext documentServiceContext) {
        ArrayList<String> terminalTokens = new ArrayList<>(Arrays.asList(new String[]{";", "}", "{"}));
        TokenStream tokenStream = documentServiceContext.get(DocumentServiceKeys.TOKEN_STREAM_KEY);
        int searchTokenIndex = documentServiceContext.get(DocumentServiceKeys.TOKEN_INDEX_KEY);
        String currentTokenStr = tokenStream.get(searchTokenIndex).getText();

        if (terminalTokens.contains(currentTokenStr)) {
            searchTokenIndex -= 1;
            while (true) {
                if (tokenStream.get(searchTokenIndex).getChannel() == Token.DEFAULT_CHANNEL) {
                    break;
                } else {
                    searchTokenIndex -= 1;
                }
            }
        }

        while (true) {
            if (searchTokenIndex >= tokenStream.size()) {
                return false;
            }
            String tokenString = tokenStream.get(searchTokenIndex).getText();
            if (terminalTokens.contains(tokenString)) {
                return false;
            } else if (tokenString.equals(".") || tokenString.equals(":")) {
                return true;
            } else {
                searchTokenIndex++;
            }
        }
    }

    /**
     * Check whether the token stream contains an annotation start (@).
     * @param documentServiceContext - Completion operation context
     * @return {@link Boolean}
     */
    boolean isAnnotationContext(TextDocumentServiceContext documentServiceContext) {
        return findPreviousToken(documentServiceContext, "@", 3) >= 0;
    }

    /**
     * Inner static class for the Function Signature. This holds both the insert text and the label for the FUnction.
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

    int findPreviousToken(TextDocumentServiceContext documentServiceContext, String needle, int maxSteps) {
        TokenStream tokenStream = documentServiceContext.get(DocumentServiceKeys.TOKEN_STREAM_KEY);
        if (tokenStream == null) {
            return -1;
        }
        int searchIndex = documentServiceContext.get(DocumentServiceKeys.TOKEN_INDEX_KEY) - 1;

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
     * Assign the Priorities to the completion items.
     * @param itemPriorityMap - Map of item priorities against the Item type
     * @param completionItems - list of completion items
     */
    protected void assignItemPriorities(HashMap<String, String> itemPriorityMap, List<CompletionItem> completionItems) {
        completionItems.forEach(completionItem -> {
            if (itemPriorityMap.containsKey(completionItem.getDetail())) {
                completionItem.setSortText(itemPriorityMap.get(completionItem.getDetail()));
            }
        });
    }

    /**
     * Populate a completion item with the given data and return it.
     * @param insertText insert text
     * @param type type of the completion item
     * @param priority completion item priority
     * @param label completion item label
     * @return {@link CompletionItem}
     */
    protected CompletionItem populateCompletionItem(String insertText, String type, String priority, String label) {
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
                        Priority.PRIORITY4.name(), insertText));
            }
        });
    }
}
