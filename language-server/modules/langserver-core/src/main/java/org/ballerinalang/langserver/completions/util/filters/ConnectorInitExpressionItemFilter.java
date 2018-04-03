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
package org.ballerinalang.langserver.completions.util.filters;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.langserver.DocumentServiceKeys;
import org.ballerinalang.langserver.LSServiceOperationContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.wso2.ballerinalang.compiler.semantics.model.types.BPackageType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filter the items for connector init expression, based on the current context.
 */
public class ConnectorInitExpressionItemFilter extends AbstractSymbolFilter {

    private static final String CREATE_KEYWORD = "create";

    public List<SymbolInfo> filterItems(LSServiceOperationContext context) {
        TokenStream tokenStream = context.get(DocumentServiceKeys.TOKEN_STREAM_KEY);
        int capturedTokenIndex = context.get(DocumentServiceKeys.TOKEN_INDEX_KEY);
        // Since the lang server is zero based indexing, we need to increase the line number to align with antlr token
        int cursorLine = context.get(DocumentServiceKeys.POSITION_KEY).getPosition().getLine() + 1;
        int cursorChar = context.get(DocumentServiceKeys.POSITION_KEY).getPosition().getCharacter();

        Token capturedToken = tokenStream.get(capturedTokenIndex);
        Token evaluatorToken = null;
        int loopIncrementer = 1;
        int tokenIterator = capturedTokenIndex;

        if (capturedToken.getLine() > cursorLine
                || (capturedToken.getLine() == cursorLine && capturedToken.getCharPositionInLine() >= cursorChar)) {
            loopIncrementer = -1;
        }

        tokenIterator += loopIncrementer;

        while (true) {
            if (tokenIterator < 0) {
                break;
            }
            Token tempToken = tokenStream.get(tokenIterator);
            if (tempToken.getLine() == cursorLine && tempToken.getCharPositionInLine() == cursorChar) {
                evaluatorToken = getFirstNonHiddenToken(tempToken.getTokenIndex(), -1, tokenStream);
                break;
            }
            tokenIterator += loopIncrementer;
        }

        if (evaluatorToken == null) {
            return new ArrayList<>();
        }

        return getItemsList(evaluatorToken, context);
    }

    /**
     * Get the first non-hidden token from the start index.
     * @param startIndex        Start index
     * @param loopIncrementer   decider value used for iterating either left or right from startIndex
     * @param tokenStream       Token Stream
     * @return {@link Token}    Not Hidden Token
     */
    private static Token getFirstNonHiddenToken(int startIndex, int loopIncrementer, TokenStream tokenStream) {
        int searchIndex = startIndex + loopIncrementer;
        while (true) {
            if (searchIndex < 0) {
                return null;
            }
            if (tokenStream.get(searchIndex).getChannel() == Token.DEFAULT_CHANNEL) {
                return tokenStream.get(searchIndex);
            }
            searchIndex += loopIncrementer;
        }
    }

    /**
     * Get the suggesting item list from the visible symbols. (As a rule these are packages such as http, etc).
     * @param evaluatorToken    Token which is used to determine this is a connector init
     * @param context           TextDocumentServiceCOntext
     * @return  {@link List}    List of filtered SymbolsInfos
     */
    private List<SymbolInfo> getItemsList(Token evaluatorToken, LSServiceOperationContext context) {
        List<SymbolInfo> filteredList = new ArrayList<>();
        if (evaluatorToken == null) {
            return filteredList;
        }
        String tokenValue = evaluatorToken.getText();
        if (tokenValue.equals(CREATE_KEYWORD)) {
            List<SymbolInfo> visibleSymbols = context.get(CompletionKeys.VISIBLE_SYMBOLS_KEY);
            filteredList.addAll(visibleSymbols
                    .stream()
                    .filter(symbolInfo -> symbolInfo.getScopeEntry().symbol.type instanceof BPackageType)
                    .collect(Collectors.toList())
            );
        }

        return filteredList;
    }
}
