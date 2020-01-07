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
package org.ballerinalang.langserver.signature.sourceprune;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.langserver.sourceprune.AbstractTokenTraverser;
import org.ballerinalang.langserver.sourceprune.SourcePruneContext;
import org.ballerinalang.langserver.sourceprune.SourcePruneKeys;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * RHS Token Traverser.
 *
 * @since 1.1.1
 */
class RHSSignatureTokenTraverser extends AbstractTokenTraverser {
    private final List<Integer> rhsTraverseTerminals;
    private final boolean addSemiColon;
    private int pendingRightParenthesis;
    private int pendingLeftBrace;
    private boolean forcedProcessedToken;
    private boolean capturedLastRightParenthesis;

    RHSSignatureTokenTraverser(SourcePruneContext sourcePruneContext, boolean pruneTokens) {
        super(pruneTokens);
        this.pendingRightParenthesis = sourcePruneContext.get(SourcePruneKeys.RIGHT_PARAN_COUNT_KEY);
        this.rhsTraverseTerminals = sourcePruneContext.get(SourcePruneKeys.RHS_TRAVERSE_TERMINALS_KEY);
        this.pendingLeftBrace = sourcePruneContext.get(SourcePruneKeys.LEFT_BRACE_COUNT_KEY);
        this.addSemiColon = sourcePruneContext.get(SourcePruneKeys.ADD_SEMICOLON_COUNT_KEY);
        this.forcedProcessedToken = false;
        this.capturedLastRightParenthesis = false;
        this.processedTokens = new ArrayList<>();
    }

    @Override
    public List<CommonToken> traverse(TokenStream tokenStream, int tokenIndex) {
        Optional<Token> token = Optional.of(tokenStream.get(tokenIndex));
        while (token.isPresent()) {
            int type = token.get().getType();
            if (rhsTraverseTerminals.contains(type)) {
                boolean terminateRHSTraverse = terminateRHSTraverse(token.get());
                if (terminateRHSTraverse) {
                    break;
                }
            }
            if (type == BallerinaParser.NEW_LINE && this.capturedLastRightParenthesis) {
                break;
            }
            if (!this.forcedProcessedToken) {
                processToken(token.get());
            }
            this.forcedProcessedToken = false;
            tokenIndex = token.get().getTokenIndex() + 1;
            token = tokenIndex > tokenStream.size() - 1 ? Optional.empty() : Optional.of(tokenStream.get(tokenIndex));
        }
        // Check for semi-colon[:], if not found add
        if (token.isPresent() && this.addSemiColon) {
            checkForSemiColon(tokenStream, tokenIndex, token.get());
        }
        return this.processedTokens;
    }

    private boolean terminateRHSTraverse(Token token) {
        int type = token.getType();
        if (type == BallerinaParser.RIGHT_PARENTHESIS) {
            boolean result;
            if (this.pendingRightParenthesis > 0) {
                this.pendingRightParenthesis--;
                result = false;
            } else {
                this.processToken(token);
                result = true;
            }
            if (this.pendingRightParenthesis == 0) {
                this.capturedLastRightParenthesis = true;
            }
            return result;
        } else if (type == BallerinaParser.RIGHT_BRACE && this.pendingLeftBrace > 0) {
            this.pendingLeftBrace--;
            return false;
        } else if (type == BallerinaParser.AT &&
                this.lastProcessedToken.getType() == BallerinaParser.RIGHT_PARENTHESIS) {
            // To handle, map<string>? attrs = fooX(1,)@;
            return false;
        }
        return true;
    }

    /**
     * Check and add semi-colon to the end.
     *
     *  @param tokenStream token stream
     * @param tokenIndex  token index
     * @param token       current token
     */
    private void checkForSemiColon(TokenStream tokenStream, int tokenIndex, Token token) {
        if (token == null || this.lastProcessedToken == null) {
            return;
        }
        while (token.getChannel() != Token.DEFAULT_CHANNEL) {
            tokenIndex++;
            if (tokenIndex < 0) {
                return;
            }
            token = tokenStream.get(tokenIndex);
        }
        boolean isWS = this.lastProcessedToken.getType() == BallerinaParser.WS;
        if (token.getType() != BallerinaParser.SEMICOLON && isWS) {
            CommonToken lastProcessedToken = (CommonToken) this.lastProcessedToken;
            lastProcessedToken.setText(";");
            lastProcessedToken.setType(BallerinaParser.SEMICOLON);
            lastProcessedToken.setChannel(Token.DEFAULT_CHANNEL);
        }
    }
}
