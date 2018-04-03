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
package org.ballerinalang.langserver.completions.resolvers.parsercontext;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.langserver.DocumentServiceKeys;
import org.ballerinalang.langserver.LSContext;
import org.ballerinalang.langserver.LSServiceOperationContext;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.resolvers.AbstractItemResolver;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BConnectorType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Item resolver for the Callable Unit Body Context related item resolving.
 */
public class ParserRuleCallableUnitBodyContextResolver extends AbstractItemResolver {
    
    private static final String ENDPOINT_KEYWORD = "endpoint";
    @Override
    public ArrayList<CompletionItem> resolveItems(LSServiceOperationContext completionContext) {
        int tokenIndex = completionContext.get(DocumentServiceKeys.TOKEN_INDEX_KEY);
        int endpointTokenIndex = -1;
        String tokenString;
        TokenStream tokenStream = completionContext.get(DocumentServiceKeys.TOKEN_STREAM_KEY);
        Position position = completionContext.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        
        while (true) {
            if (tokenIndex > tokenStream.size() - 1) {
                break;
            }
            tokenString = tokenStream.get(tokenIndex).getText();
            if (isCursorBeforeToken(position, tokenStream.get(tokenIndex))) {
                break;
            } else if (tokenString.equals(ENDPOINT_KEYWORD)) {
                endpointTokenIndex = tokenIndex;
            }
            tokenIndex++;
        }
        
        if (endpointTokenIndex > 0) {
            Token tokenBeforeCursor = CommonUtil.getPreviousDefaultToken(tokenStream, tokenIndex - 1);
            switch (tokenBeforeCursor.getText()) {
                case "create":
                case "<":
                    this.populateCompletionItemList(getPackagesAndConnectors(completionContext), completionItems);
                    break;
                default:
                    break;
            }
        }
        
        return completionItems;
    }
    
    private static boolean isCursorBeforeToken(Position cursor, Token token) {
        int cursorLine = cursor.getLine();
        int cursorCol = cursor.getCharacter();
        int tokenLine = token.getLine() - 1;
        int tokenCol = token.getCharPositionInLine();
        
        return cursorLine < tokenLine || (cursorLine == tokenLine && cursorCol <= tokenCol);
    }
    
    private static List<SymbolInfo> getPackagesAndConnectors(LSContext context) {
        List<SymbolInfo> symbolInfoList = context.get(CompletionKeys.VISIBLE_SYMBOLS_KEY);
        return symbolInfoList.stream()
                .filter(symbolInfo -> {
                    BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
                    return bSymbol.getType() instanceof BConnectorType || bSymbol instanceof BPackageSymbol;
                })
                .collect(Collectors.toList());
    }
}
