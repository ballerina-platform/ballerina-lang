/*
  Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package org.ballerinalang.langserver.sourceprune;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * LHS token traverser.
 * 
 * @since 0.995.0
 */
class LHSTokenTraverser extends AbstractTokenTraverser {
    private List<Integer> lhsTraverseTerminals;
    private List<Integer> blockRemoveKWTerminals;
    private boolean removeBlock;
    private int rightParenthesisCount;
    private int rightBraceCount;
    private int rightBracketCount;
    private int ltSymbolCount;
    private boolean capturedAssignToken;
    private SourcePruneContext sourcePruneContext;

    LHSTokenTraverser(SourcePruneContext sourcePruneContext, boolean pruneTokens) {
        super(pruneTokens);
        this.sourcePruneContext = sourcePruneContext;
        this.lhsTraverseTerminals = sourcePruneContext.get(SourcePruneKeys.LHS_TRAVERSE_TERMINALS_KEY);
        this.blockRemoveKWTerminals = sourcePruneContext.get(SourcePruneKeys.BLOCK_REMOVE_KW_TERMINALS_KEY);

        this.removeBlock = false;
        this.capturedAssignToken = false;
        this.rightParenthesisCount = 0;
        this.rightBraceCount = 0;
        this.rightBracketCount = 0;
        this.ltSymbolCount = 0;
        this.processedTokens = new ArrayList<>();
    }

    List<CommonToken> traverseLHS(TokenStream tokenStream, int tokenIndex) {
        Optional<Token> token = Optional.of(tokenStream.get(tokenIndex));
        while (token.isPresent()) {
            int type = token.get().getType();
            if (this.lhsTraverseTerminals.contains(type)) {
                boolean terminate = terminateLHSTraverse(token.get(), tokenStream);
                if (terminate) {
                    break;
                }
            }
            if (this.blockRemoveKWTerminals.contains(type)) {
                this.removeBlock = true;
            } else if (type == BallerinaParser.ASSIGN) {
                this.capturedAssignToken = true;
            } else if (BallerinaParser.RIGHT_BRACKET == type) {
                rightBracketCount++;
            }
            processToken(token.get());
            tokenIndex = token.get().getTokenIndex() - 1;
            token = tokenIndex < 0 ? Optional.empty() : Optional.of(tokenStream.get(tokenIndex));
        }
        
        sourcePruneContext.put(SourcePruneKeys.REMOVE_DEFINITION_KEY, this.removeBlock);
        sourcePruneContext.put(SourcePruneKeys.RIGHT_PARAN_COUNT_KEY, this.rightParenthesisCount);
        sourcePruneContext.put(SourcePruneKeys.RIGHT_BRACE_COUNT_KEY, this.rightBraceCount);
        sourcePruneContext.put(SourcePruneKeys.LT_COUNT_KEY, this.ltSymbolCount);
        Collections.reverse(this.processedTokens);
        return this.processedTokens;
    }

    private boolean terminateLHSTraverse(Token token, TokenStream tokenStream) {
        int type = token.getType();
        if (type == BallerinaParser.RIGHT_PARENTHESIS) {
            this.rightParenthesisCount++;
            this.processToken(token);
            return false;
        }
        if (type == BallerinaParser.LEFT_PARENTHESIS) {
            Optional<Token> tokenToLeft = CommonUtil.getPreviousDefaultToken(tokenStream, token.getTokenIndex());
            if (processedTokens.isEmpty()) {
                //TODO: Add a test case for this
                int leftParenthesisCount = sourcePruneContext.get(SourcePruneKeys.LEFT_PARAN_COUNT_KEY);
                sourcePruneContext.put(SourcePruneKeys.LEFT_PARAN_COUNT_KEY, ++leftParenthesisCount);
                return false;
            } else if (this.rightParenthesisCount > 0) {
                this.rightParenthesisCount--;
                this.processToken(token);
                return false;
            } else if (tokenToLeft.isPresent() && 
                    (BallerinaParser.IF == tokenToLeft.get().getType() ||
                            BallerinaParser.WHILE == tokenToLeft.get().getType())) {
                // Cursor is within the if/ else if/ while condition
                this.replaceCondition(tokenStream, token.getTokenIndex());
            }
            return true;
        }
        /*
        Right brace is used as a terminal token and should be used as a terminating point for block statements.
        in cases such as following example,
            xyz {f1:1, f2:{f3:4}} = 
        right brace cannot consider as a terminal token, therefore we track the previous token and consider that to
        identify blocks. Until the right brace count is zero all the following right braces are altered (refer example)
         */
        if (type == BallerinaParser.RIGHT_BRACE
                && (this.lastProcessedToken == BallerinaParser.ASSIGN
                || this.lastProcessedToken == BallerinaParser.EQUAL_GT || this.rightBraceCount > 0)) {
            this.processToken(token);
            this.rightBraceCount++;
            return false;
        }
        if (type == BallerinaParser.LEFT_BRACE && this.rightBraceCount > 0) {
            this.processToken(token);
            this.rightBraceCount--;
            return false;
        }
        if (type == BallerinaParser.LEFT_BRACKET && this.rightBracketCount > 0) {
            this.processToken(token);
            this.rightBracketCount--;
            return false;
        }
        /*
        Specially capture the LT token in order to avoid Right token removal during the following case
        public annotation <resource,r> ...
        RHS Token traverser will check for the less than symbol count when a GT token found
         */
        if (type == BallerinaParser.LT) {
            this.processToken(token);
            this.ltSymbolCount++;
            return false;
        }
        if (type == BallerinaParser.RETURNS) {
            this.processToken(token);
            // If we return true, then always we need to check the right parenthesis count and the right braces count
            return !this.capturedAssignToken && this.rightParenthesisCount == 0 && this.rightBraceCount == 0;
        }
        // Handle the ON token replacing since this is used in both service and JSON streaming input
        boolean onServiceRule = CommonUtil.getNDefaultTokensToLeft(tokenStream, 2, token.getTokenIndex()).stream()
                .map(Token::getType)
                .collect(Collectors.toList())
                .contains(BallerinaParser.SERVICE);
        if (token.getType() == BallerinaParser.COMMA || (!onServiceRule && token.getType() == BallerinaParser.ON)) {
            this.processToken(token);
        }
        
        return rightParenthesisCount == 0 && this.rightBraceCount == 0 && rightBracketCount == 0;
    }
}
