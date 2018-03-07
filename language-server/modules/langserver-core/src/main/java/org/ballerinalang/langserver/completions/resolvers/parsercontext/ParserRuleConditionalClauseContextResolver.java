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
import org.ballerinalang.langserver.TextDocumentServiceContext;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.resolvers.AbstractItemResolver;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.langserver.completions.util.sorters.ConditionalStatementItemSorter;
import org.ballerinalang.langserver.completions.util.sorters.ItemSorters;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Item resolver for the if clause.
 * @since 0.965.0
 */
public class ParserRuleConditionalClauseContextResolver extends AbstractItemResolver {
    
    private static final String IF_KEY_WORD = "if";

    private static final String WHILE_KEY_WORD = "while";
    
    private static final String OPEN_BRACKET_KEY_WORD = "(";
    
    @Override
    public ArrayList<CompletionItem> resolveItems(TextDocumentServiceContext completionContext) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        List<SymbolInfo> symbolInfos =
                this.filterConditionalSymbols(completionContext.get(CompletionKeys.VISIBLE_SYMBOLS_KEY));

        if (this.isWithinBrackets(completionContext)) {
            this.populateCompletionItemList(symbolInfos, completionItems);
            this.populateTrueFalseKeywords(completionItems);
        }

        ItemSorters.getSorterByClass(ConditionalStatementItemSorter.class)
                .sortItems(completionContext, completionItems);

        return completionItems;
    }

    private boolean isWithinBrackets(TextDocumentServiceContext context) {
        int currentTokenIndex = context.get(DocumentServiceKeys.TOKEN_INDEX_KEY);
        TokenStream tokenStream = context.get(DocumentServiceKeys.TOKEN_STREAM_KEY);
        Token previousToken = tokenStream.get(currentTokenIndex);
        Token currentToken;
        while (true) {
            if (currentTokenIndex < 0) {
                break;
            }
            currentToken = CommonUtil.getPreviousDefaultToken(tokenStream, currentTokenIndex);
            if (currentToken.getText().equals(IF_KEY_WORD) || currentToken.getText().equals(WHILE_KEY_WORD)) {
                break;
            }
            previousToken = currentToken;
            currentTokenIndex = currentToken.getTokenIndex();
        }

        if (previousToken != null && previousToken.getText().equals(OPEN_BRACKET_KEY_WORD)) {
            Position position = context.get(DocumentServiceKeys.POSITION_KEY).getPosition();
            Token closeBracket = context.get(DocumentServiceKeys.TOKEN_STREAM_KEY)
                    .get(context.get(DocumentServiceKeys.TOKEN_INDEX_KEY));
            int cursorLine = position.getLine();
            int cursorCol = position.getCharacter();
            int startBracketLine = previousToken.getLine() - 1;
            int startBracketCol = previousToken.getCharPositionInLine();
            int closeBracketLine = closeBracket.getLine() - 1;
            int closeBracketCol = closeBracket.getCharPositionInLine();

            return (cursorLine >= startBracketLine && cursorLine < closeBracketLine) 
                    || (cursorLine > startBracketLine && cursorLine <= closeBracketLine)
                    || (cursorLine == startBracketLine && cursorLine == closeBracketLine
                    && cursorCol > startBracketCol && cursorCol <= closeBracketCol);
        }

        return false;
    }

    private List<SymbolInfo> filterConditionalSymbols(List<SymbolInfo> symbolInfoList) {
        return symbolInfoList.stream().filter(symbolInfo -> {
            BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
            return (bSymbol instanceof BTypeSymbol && bSymbol instanceof BPackageSymbol)
                    || (bSymbol instanceof BVarSymbol && !(bSymbol instanceof BInvokableSymbol))
                    || (bSymbol instanceof BInvokableSymbol && !(bSymbol instanceof BOperatorSymbol)
                    && (((BInvokableSymbol) bSymbol).receiverSymbol == null));
        }).collect(Collectors.toList());
    }
    
    private void populateTrueFalseKeywords(List<CompletionItem> completionItems) {
        CompletionItem trueItem = new CompletionItem();
        CompletionItem falseItem = new CompletionItem();
        
        trueItem.setLabel(ItemResolverConstants.TRUE_KEYWORD);
        trueItem.setInsertText(ItemResolverConstants.TRUE_KEYWORD);
        trueItem.setDetail(ItemResolverConstants.KEYWORD_TYPE);

        falseItem.setLabel(ItemResolverConstants.FALSE_KEYWORD);
        falseItem.setInsertText(ItemResolverConstants.FALSE_KEYWORD);
        falseItem.setDetail(ItemResolverConstants.KEYWORD_TYPE);
        
        completionItems.add(trueItem);
        completionItems.add(falseItem);
    }
}
