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
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.completion.BInvokableSymbolUtil;
import org.ballerinalang.langserver.common.utils.completion.BPackageSymbolUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.util.CompletionUtil;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.model.symbols.SymbolKind;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BServiceSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
            if (!(bSymbol != null && bSymbol.getName().getValue().startsWith("$"))) {
                if ((bSymbol instanceof BInvokableSymbol
                        && SymbolKind.FUNCTION.equals(((BInvokableSymbol) bSymbol).kind))
                        || symbolInfo.isIterableOperation()) {
                    completionItem = this.populateBallerinaFunctionCompletionItem(symbolInfo);
                } else if (!(bSymbol instanceof BInvokableSymbol)
                        && bSymbol instanceof BVarSymbol && !"_".equals(bSymbol.name.getValue())) {
                    completionItem = this.populateVariableDefCompletionItem(symbolInfo);
                } else if (bSymbol instanceof BTypeSymbol
                        && !bSymbol.getName().getValue().equals(UtilSymbolKeys.NOT_FOUND_TYPE)
                        && !(bSymbol instanceof BAnnotationSymbol)
                        && !(bSymbol.getName().getValue().equals("runtime"))
                        && !(bSymbol instanceof BServiceSymbol)) {
                    completionItem = BPackageSymbolUtil.getBTypeCompletionItem(symbolInfo.getSymbolName());
                }
            }

            if (completionItem != null) {
                completionItems.add(completionItem);
            }
        });
    }

    /**
     * Populate the completion item list by either list.
     * @param listEither            Either List of completion items or symbol info
     * @param completionItems       Completion Items List
     */
    protected void populateCompletionItemList(Either<List<CompletionItem>, List<SymbolInfo>> listEither,
                                              List<CompletionItem> completionItems) {
        if (listEither.isLeft()) {
            completionItems.addAll(listEither.getLeft());
        } else {
            this.populateCompletionItemList(listEither.getRight(), completionItems);
        }
    }

    /**
     * Populate the Ballerina Function Completion Item.
     * @param symbolInfo - symbol information
     * @return completion item
     */
    private CompletionItem populateBallerinaFunctionCompletionItem(SymbolInfo symbolInfo) {
        if (symbolInfo.isIterableOperation()) {
            return BInvokableSymbolUtil.getFunctionCompletionItem(
                    symbolInfo.getIterableOperationSignature().getInsertText(),
                    symbolInfo.getIterableOperationSignature().getLabel());
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
            return BInvokableSymbolUtil.getFunctionCompletionItem(bInvokableSymbol);
        }
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
     * Check whether the token stream corresponds to a action invocation or a function invocation.
     * @param documentServiceContext - Completion operation context
     * @return {@link Boolean}
     */
    protected boolean isInvocationOrFieldAccess(LSServiceOperationContext documentServiceContext) {
        ArrayList<String> terminalTokens = new ArrayList<>(Arrays.asList(new String[]{";", "}", "{", "(", ")", "="}));
        Position position = documentServiceContext.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        int cursorLine = position.getLine();
        TokenStream tokenStream = documentServiceContext.get(DocumentServiceKeys.TOKEN_STREAM_KEY);
        if (tokenStream == null) {
            String lineSegment = documentServiceContext.get(CompletionKeys.CURRENT_LINE_SEGMENT_KEY);
            String tokenString = CompletionUtil.getDelimiterTokenFromLineSegment(documentServiceContext, lineSegment);
            return (UtilSymbolKeys.DOT_SYMBOL_KEY.equals(tokenString)
                    || UtilSymbolKeys.PKG_DELIMITER_KEYWORD.equals(tokenString)
                    || UtilSymbolKeys.ACTION_INVOCATION_SYMBOL_KEY.equals(tokenString));
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
            Token token = tokenStream.get(searchTokenIndex);
            String tokenString = token.getText();
            if (terminalTokens.contains(tokenString)
                    && documentServiceContext.get(DocumentServiceKeys.TOKEN_INDEX_KEY) <= searchTokenIndex) {
                documentServiceContext.put(CompletionKeys.INVOCATION_STATEMENT_KEY, false);
                return false;
            } else if ((UtilSymbolKeys.DOT_SYMBOL_KEY.equals(tokenString)
                    || UtilSymbolKeys.PKG_DELIMITER_KEYWORD.equals(tokenString)
                    || UtilSymbolKeys.ACTION_INVOCATION_SYMBOL_KEY.equals(tokenString))
                    && cursorLine == token.getLine() - 1) {
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
                    && !bSymbol.getName().getValue().startsWith(UtilSymbolKeys.ANON_STRUCT_CHECKER)
                    && !((bSymbol instanceof BPackageSymbol) && bSymbol.pkgID.getName().getValue().equals("runtime"))
                    && !(bSymbol instanceof BAnnotationSymbol)
                    && !(bSymbol instanceof BServiceSymbol)) {
                completionItems.add(BPackageSymbolUtil.getBTypeCompletionItem(symbolInfo.getSymbolName()));
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
        // ballerina.coordinator, ballerina/runtime, and anonStructs
        ArrayList<String> invalidPkgs = new ArrayList<>(Arrays.asList("runtime",
                "transactions"));
        symbolInfoList.removeIf(symbolInfo -> {
            BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
            String symbolName = bSymbol.getName().getValue();
            return (bSymbol instanceof BInvokableSymbol && (((BInvokableSymbol) bSymbol).receiverSymbol) != null
                    && !bSymbol.kind.equals(SymbolKind.RESOURCE))
                    || (bSymbol instanceof BPackageSymbol && invalidPkgs.contains(symbolName))
                    || (symbolName.startsWith("$anonStruct"));
        });
        
        return symbolInfoList;
    }

    /**
     * Get variable definition context related completion items. This will extract the completion items analyzing the
     * variable definition context properties.
     * 
     * @param completionContext     Completion context
     * @return {@link List}         List of resolved completion items
     */
    protected List<CompletionItem> getVariableDefinitionCompletionItems(LSServiceOperationContext completionContext) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        List<SymbolInfo> filteredList;
        String tokenString = CommonUtil.getPreviousDefaultToken(
                completionContext.get(DocumentServiceKeys.TOKEN_STREAM_KEY),
                completionContext.get(DocumentServiceKeys.TOKEN_INDEX_KEY)).getText();
        if (ItemResolverConstants.NEW.equals(tokenString)) {
            filteredList = completionContext.get(CompletionKeys.VISIBLE_SYMBOLS_KEY).stream()
                    .filter(symbolInfo -> symbolInfo.getScopeEntry().symbol.kind == SymbolKind.OBJECT)
                    .collect(Collectors.toList());
            filteredList.forEach(symbolInfo -> {
                BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) symbolInfo.getScopeEntry().symbol;
                BInvokableSymbol initializerFunction = objectTypeSymbol.initializerFunc.symbol;
                initializerFunction.name = new Name(objectTypeSymbol.getName().getValue());
                completionItems.add(BInvokableSymbolUtil.getFunctionCompletionItem(initializerFunction));
            });
            this.populateCompletionItemList(filteredList, completionItems);
        } else {
            // Add the check keyword
            CompletionItem createKeyword = new CompletionItem();
            createKeyword.setInsertText(Snippet.CHECK_KEYWORD_SNIPPET.toString());
            createKeyword.setLabel(ItemResolverConstants.CHECK_KEYWORD);
            createKeyword.setDetail(ItemResolverConstants.KEYWORD_TYPE);

            // Add But keyword item
            CompletionItem butKeyword = new CompletionItem();
            butKeyword.setInsertText(Snippet.BUT.toString());
            butKeyword.setLabel(ItemResolverConstants.BUT);
            butKeyword.setInsertTextFormat(InsertTextFormat.Snippet);
            butKeyword.setDetail(ItemResolverConstants.STATEMENT_TYPE);

            filteredList = completionContext.get(CompletionKeys.VISIBLE_SYMBOLS_KEY)
                    .stream()
                    .filter(symbolInfo -> {
                        BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
                        SymbolKind symbolKind = bSymbol.kind;

                        // Here we return false if the BType is not either a package symbol or ENUM
                        return !((bSymbol instanceof BTypeSymbol) && !(bSymbol instanceof BPackageSymbol
                                || SymbolKind.ENUM.equals(symbolKind)));
                    })
                    .collect(Collectors.toList());

            // Remove the functions without a receiver symbol
            filteredList.removeIf(symbolInfo -> {
                BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
                return bSymbol instanceof BInvokableSymbol && ((BInvokableSymbol) bSymbol).receiverSymbol != null;
            });
            populateCompletionItemList(filteredList, completionItems);
            completionItems.add(createKeyword);
            completionItems.add(butKeyword);
        }
        
        return completionItems;
    }
}
