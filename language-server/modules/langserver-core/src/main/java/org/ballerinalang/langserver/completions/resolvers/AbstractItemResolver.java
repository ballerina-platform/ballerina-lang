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
import org.ballerinalang.langserver.LSServiceOperationContext;
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.model.symbols.SymbolKind;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Interface for completion item resolvers.
 */
public abstract class AbstractItemResolver {
    
    public abstract ArrayList<CompletionItem> resolveItems(LSServiceOperationContext completionContext);

    /**
     * Populate the completion item list by considering the.
     * @param symbolInfoList - list of symbol information
     * @param completionItems - completion item list to populate
     */
    protected void populateCompletionItemList(List<SymbolInfo> symbolInfoList, List<CompletionItem> completionItems) {

        symbolInfoList.forEach(symbolInfo -> {
            CompletionItem completionItem = null;
            BSymbol bSymbol = symbolInfo.getScopeEntry() != null ? symbolInfo.getScopeEntry().symbol : null;
            if ((bSymbol instanceof BInvokableSymbol
                    && ((BInvokableSymbol) bSymbol).kind != null
                    && !((BInvokableSymbol) bSymbol).kind.equals(SymbolKind.WORKER))
                    || symbolInfo.isIterableOperation())  {
                completionItem = this.populateBallerinaFunctionCompletionItem(symbolInfo);
            } else if (!(bSymbol instanceof BInvokableSymbol)
                    && bSymbol instanceof BVarSymbol) {
                completionItem = this.populateVariableDefCompletionItem(symbolInfo);
            } else if (bSymbol instanceof BTypeSymbol
                    && !bSymbol.getName().getValue().equals(UtilSymbolKeys.NOT_FOUND_TYPE)
                    && !(bSymbol instanceof BAnnotationSymbol)) {
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
        String insertText;
        String label;
        CompletionItem completionItem = new CompletionItem();
        
        if (symbolInfo.isIterableOperation()) {
            insertText = symbolInfo.getIterableOperationSignature().getInsertText();
            label = symbolInfo.getIterableOperationSignature().getLabel();
        } else {
            BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
            if (!(bSymbol instanceof BInvokableSymbol)) {
                return null;
            }
            BInvokableSymbol bInvokableSymbol = (BInvokableSymbol) bSymbol;
            if (bInvokableSymbol.getName().getValue().contains("<")
                    || bInvokableSymbol.getName().getValue().contains("<") ||
                    bInvokableSymbol.getName().getValue().equals("main")) {
                return null;
            }
            FunctionSignature functionSignature = getFunctionSignature(bInvokableSymbol);
            
            insertText = functionSignature.getInsertText();
            label = functionSignature.getLabel();
        }
        completionItem.setInsertTextFormat(InsertTextFormat.Snippet);
        completionItem.setLabel(label);
        completionItem.setInsertText(insertText);
        completionItem.setDetail(ItemResolverConstants.FUNCTION_TYPE);
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
        completionItem.setKind(CompletionItemKind.Unit);

        return completionItem;
    }

    /**
     * Populate the BType Completion Item.
     * @param symbolInfo - symbol information
     * @return completion item
     */
    private CompletionItem populateBTypeCompletionItem(SymbolInfo symbolInfo) {
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
        String[] funcNameComponents = bInvokableSymbol.getName().getValue().split("\\.");
        String functionName = funcNameComponents[funcNameComponents.length - 1];

        // If there is a receiver symbol, then the name comes with the package name and struct name appended.
        // Hence we need to remove it
        if (bInvokableSymbol.receiverSymbol != null) {
            String receiverType = bInvokableSymbol.receiverSymbol.getType().toString();
            functionName = functionName.replace(receiverType + ".", "");
        }
        StringBuilder signature = new StringBuilder(functionName + "(");
        StringBuilder insertText = new StringBuilder(functionName + "(");
        List<BVarSymbol> parameterDefs = bInvokableSymbol.getParameters();

        for (int itr = 0; itr < parameterDefs.size(); itr++) {
            BType paramType = parameterDefs.get(itr).getType();
            String typeName;
            if (paramType instanceof BInvokableType) {
                // Check for the case when we can give a function as a parameter
                typeName = parameterDefs.get(itr).type.toString();
            } else {
                BTypeSymbol tSymbol;
                tSymbol = (paramType instanceof BArrayType) ?
                        ((BArrayType) paramType).eType.tsymbol : paramType.tsymbol;
                List<Name> nameComps = tSymbol.pkgID.nameComps;
                if (tSymbol.pkgID.getName().getValue().equals(Names.BUILTIN_PACKAGE.getValue())
                        || tSymbol.pkgID.getName().getValue().equals(Names.DOT.getValue())) {
                    typeName = tSymbol.getName().getValue();
                } else {
                    typeName = nameComps.get(nameComps.size() - 1).getValue() + UtilSymbolKeys.PKG_DELIMITER_KEYWORD
                            + tSymbol.getName().getValue();
                }
            }

            signature.append(typeName).append(" ")
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
        String endString = ")";

        BType returnType = bInvokableSymbol.type.getReturnType();
        if (returnType != null && !(returnType instanceof BNilType)) {
            signature.append(initString).append(returnType.toString());
            signature.append(endString);
        }
        return new FunctionSignature(insertText.toString(), signature.toString());
    }

    /**
     * Check whether the token stream corresponds to a action invocation or a function invocation.
     * @param documentServiceContext - Completion operation context
     * @return {@link Boolean}
     */
    protected boolean isInvocationOrFieldAccess(LSServiceOperationContext documentServiceContext) {
        ArrayList<String> terminalTokens = new ArrayList<>(Arrays.asList(new String[]{";", "}", "{", "(", ")"}));
        TokenStream tokenStream = documentServiceContext.get(DocumentServiceKeys.TOKEN_STREAM_KEY);
        if (tokenStream == null) {
            return false;
        }
        int searchTokenIndex = documentServiceContext.get(DocumentServiceKeys.TOKEN_INDEX_KEY);
        
        /*
        In order to avoid the token index inconsistencies, current token index offsets from two default tokens
         */
        Token offsetToken = CommonUtil.getNthDefaultTokensToLeft(tokenStream, searchTokenIndex, 2);
        if (!terminalTokens.contains(offsetToken.getText())) {
            searchTokenIndex = offsetToken.getTokenIndex();
        }

        while (true) {
            if (searchTokenIndex >= tokenStream.size()) {
                documentServiceContext.put(CompletionKeys.INVOCATION_STATEMENT_KEY, false);
                return false;
            }
            String tokenString = tokenStream.get(searchTokenIndex).getText();
            if (terminalTokens.contains(tokenString)
                    && documentServiceContext.get(DocumentServiceKeys.TOKEN_INDEX_KEY) <= searchTokenIndex) {
                documentServiceContext.put(CompletionKeys.INVOCATION_STATEMENT_KEY, false);
                return false;
            } else if (UtilSymbolKeys.DOT_SYMBOL_KEY.equals(tokenString)
                    || UtilSymbolKeys.PKG_DELIMITER_KEYWORD.equals(tokenString)
                    || UtilSymbolKeys.ACTION_INVOCATION_SYMBOL_KEY.equals(tokenString)) {
                documentServiceContext.put(CompletionKeys.INVOCATION_STATEMENT_KEY, true);
                return true;
            } else {
                searchTokenIndex++;
            }
        }
    }

    /**
     * Check whether the token stream contains an annotation start (@).
     * @param ctx - Completion operation context
     * @return {@link Boolean}
     */
    protected boolean isAnnotationContext(LSServiceOperationContext ctx) {
        return ctx.get(DocumentServiceKeys.PARSER_RULE_CONTEXT_KEY) != null
                && UtilSymbolKeys.ANNOTATION_START_SYMBOL_KEY
                .equals(ctx.get(DocumentServiceKeys.TOKEN_STREAM_KEY).get(ctx.get(DocumentServiceKeys.TOKEN_INDEX_KEY))
                        .getText());
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

    int findPreviousToken(LSServiceOperationContext documentServiceContext, String needle, int maxSteps) {
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
     * Populate a completion item with the given data and return it.
     * @param insertText insert text
     * @param type type of the completion item
     * @param label completion item label
     * @return {@link CompletionItem}
     */
    protected CompletionItem populateCompletionItem(String insertText, String type, String label) {
        CompletionItem completionItem = new CompletionItem();
        completionItem.setInsertText(insertText);
        completionItem.setDetail(type);
        completionItem.setLabel(label);
        return completionItem;
    }

    protected void populateBasicTypes(List<CompletionItem> completionItems, List<SymbolInfo> visibleSymbols) {
        visibleSymbols.forEach(symbolInfo -> {
            BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
            if (bSymbol instanceof BTypeSymbol
                    && !bSymbol.getName().getValue().equals(UtilSymbolKeys.NOT_FOUND_TYPE)
                    && !(bSymbol instanceof BAnnotationSymbol)) {
                completionItems.add(this.populateBTypeCompletionItem(symbolInfo));
            }
        });
    }

    /**
     * Remove the invalid symbols such as anon types, injected packages and invokable symbols without receiver.
     * @param symbolInfoList    Symbol info list to be filtered
     * @return {@link List}     List of filtered symbols
     */
    protected List<SymbolInfo> removeInvalidStatementScopeSymbols(List<SymbolInfo> symbolInfoList) {
        // We need to remove the functions having a receiver symbol and the bTypes of the following
        // ballerina.coordinator, ballerina.runtime, and anonStructs
        ArrayList<String> invalidPkgs = new ArrayList<>(Arrays.asList("runtime",
                "transactions"));
        symbolInfoList.removeIf(symbolInfo -> {
            BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
            String symbolName = bSymbol.getName().getValue();
            return (bSymbol instanceof BInvokableSymbol && ((BInvokableSymbol) bSymbol).receiverSymbol != null)
                    || (bSymbol instanceof BPackageSymbol && invalidPkgs.contains(symbolName))
                    || (symbolName.startsWith("$anonStruct"));
        });
        
        return symbolInfoList;
    }
}
