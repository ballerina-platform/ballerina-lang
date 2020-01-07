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
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.sourceprune.AbstractTokenTraverser;
import org.ballerinalang.langserver.sourceprune.SourcePruneContext;
import org.ballerinalang.langserver.sourceprune.SourcePruneKeys;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * LHS token traverser.
 *
 * @since 1.1.1
 */
class LHSSignatureTokenTraverser extends AbstractTokenTraverser {
    private List<Integer> lhsTraverseTerminals;
    private SourcePruneContext sourcePruneContext;
    private boolean forcedProcessedToken = false;
    private boolean capturedFirstLeftParenthesis = false;
    private boolean capturedFirstColon = false;
    private int pendingRightParenthesis = 1;
    private int pendingLeftParenthesis = 1;
    private int pendingLeftBrace = 0;
    private int pendingLeftBracket = 0;
    private boolean isCommaTerminal = true;

    private boolean isCapturingEnabled = true;
    private boolean captureStatement = false;
    private boolean addSemiColon = false;

    LHSSignatureTokenTraverser(SourcePruneContext sourcePruneContext, boolean pruneTokens) {
        super(pruneTokens);
        this.sourcePruneContext = sourcePruneContext;
        this.lhsTraverseTerminals = sourcePruneContext.get(SourcePruneKeys.LHS_TRAVERSE_TERMINALS_KEY);
        this.processedTokens = new ArrayList<>();
    }

    @Override
    public List<CommonToken> traverse(TokenStream tokenStream, int tokenIndex) {
        Optional<Token> token = Optional.of(tokenStream.get(tokenIndex));
        if (token.isPresent()) {
            int type = token.get().getType();
            if (type == BallerinaParser.COMMA) {
                this.isCommaTerminal = false;
            }
        }
        while (token.isPresent()) {
            int type = token.get().getType();
            if (token.get().getChannel() != 0 && this.capturedFirstLeftParenthesis) {
                // if the first left parenthesis '(' is already captured, when we are getting non default channel token
                // we disable capturing tokens but continue pruning
                Optional<Token> prevToken = CommonUtil.getPreviousDefaultToken(tokenStream, tokenIndex);

                // Exception is made for capturing explicit object initializations, eg. new Object()
                boolean explicitInit = prevToken.isPresent() && prevToken.get().getType() == BallerinaParser.NEW;

                // Exception is made for capturing implicit object initializations, eg. Object obj = new()
                if (!this.captureStatement && this.processedTokens.size() >= 2) {
                    CommonToken nToken = this.processedTokens.get(this.processedTokens.size() - 1);
                    CommonToken nnToken = this.processedTokens.get(this.processedTokens.size() - 2);
                    this.captureStatement = nToken.getType() == BallerinaParser.NEW &&
                            nnToken.getType() == BallerinaParser.LEFT_PARENTHESIS;
                }
                if (!explicitInit && !this.captureStatement && this.pendingLeftParenthesis == 0) {
                    this.isCapturingEnabled = false;
                }
            }
            if (this.lhsTraverseTerminals.contains(type)) {
                boolean terminate = terminateLHSTraverse(token.get(), tokenStream);
                if (terminate) {
                    break;
                }
            }
            // If match statement, avoid adding semi-colon
            this.addSemiColon = this.addSemiColon || token.get().getType() == BallerinaParser.MATCH;
            if (!this.forcedProcessedToken) {
                processToken(token.get());
            }
            this.forcedProcessedToken = false;
            tokenIndex = token.get().getTokenIndex() - 1;
            token = tokenIndex < 0 ? Optional.empty() : Optional.of(tokenStream.get(tokenIndex));
        }
        sourcePruneContext.put(SourcePruneKeys.LEFT_PARAN_COUNT_KEY, this.pendingLeftParenthesis);
        sourcePruneContext.put(SourcePruneKeys.RIGHT_PARAN_COUNT_KEY, this.pendingRightParenthesis);
        sourcePruneContext.put(SourcePruneKeys.LEFT_BRACE_COUNT_KEY, this.pendingLeftBrace);
        sourcePruneContext.put(SourcePruneKeys.ADD_SEMICOLON_COUNT_KEY, this.addSemiColon);
        Collections.reverse(this.processedTokens);

        removeProceedingNonDefaultTokens(this.processedTokens);
        return this.processedTokens;
    }

    private void removeProceedingNonDefaultTokens(List<CommonToken> processedTokens) {
        int len = processedTokens.size();
        for (int i = 0; i < len; i++) {
            CommonToken token = processedTokens.get(i);
            if (token.getChannel() != 0) {
                processedTokens.remove(token);
                i--;
                len--;
            } else {
                break;
            }
        }
    }

    private boolean terminateLHSTraverse(Token token, TokenStream tokenStream) {
        int type = token.getType();
        String text = token.getText();
        if (type == BallerinaParser.RIGHT_PARENTHESIS) {
            // Exception for field access expr. eg. stub.bar(1, false).foo()
            boolean isFieldAccess = this.lastProcessedToken.getType() == BallerinaParser.DOT ||
                    this.lastProcessedToken.getType() == BallerinaParser.OPTIONAL_FIELD_ACCESS; // ').' or ')?.'
            if (isFieldAccess) {
                this.isCommaTerminal = false;
            }
            if (this.capturedFirstLeftParenthesis && !isFieldAccess) {
                return true;
            }
            this.pendingLeftParenthesis++;
            this.processToken(token);
            this.forcedProcessedToken = true;
            return false;
        } else if (type == BallerinaParser.LEFT_PARENTHESIS) {
            Optional<Token> tokenToLeft = CommonUtil.getPreviousDefaultToken(tokenStream, token.getTokenIndex());
            if (this.pendingLeftParenthesis > 0) {
                this.pendingLeftParenthesis--;
                this.isCommaTerminal = true;
                this.capturedFirstLeftParenthesis = true;
                return false;
            } else {
                this.isCommaTerminal = true;
                if (this.capturedFirstLeftParenthesis) {
                    return true;
                } else {
                    this.capturedFirstLeftParenthesis = true;
                }
            }
            if (tokenToLeft.isPresent() &&
                    (BallerinaParser.IF == tokenToLeft.get().getType() ||
                            BallerinaParser.WHILE == tokenToLeft.get().getType())) {
                // Cursor is within the if/ else if/ while condition
                this.replaceCondition(tokenStream, token.getTokenIndex());
            }
            this.isCommaTerminal = true;
            this.capturedFirstLeftParenthesis = true;
            return false;
        } else if (type == BallerinaParser.RIGHT_BRACE) {
            this.pendingLeftBrace++;
            this.processToken(token);
            this.forcedProcessedToken = true;
            return false;
        } else if (type == BallerinaParser.LEFT_BRACE) {
            if (this.pendingLeftBrace > 0) {
                this.pendingLeftBrace--;
                return false;
            } else {
                return true;
            }
        } else if (type == BallerinaParser.XMLTemplateText && "${".equals(text)) {
            this.pendingLeftBrace++;
            this.processToken(token);
            this.forcedProcessedToken = true;
            return false;
        } else if (type == BallerinaParser.RIGHT_BRACKET) {
            this.pendingLeftBracket++;
            this.processToken(token);
            this.forcedProcessedToken = true;
            return false;
        } else if (type == BallerinaParser.LEFT_BRACKET) {
            if (this.pendingLeftBracket > 0) {
                this.pendingLeftBracket--;
                return false;
            } else {
                return true;
            }
        } else if (type == BallerinaParser.COMMA) {
            if (this.isCommaTerminal && this.capturedFirstLeftParenthesis) {
                this.processToken(token);
                this.forcedProcessedToken = true;
                return true;
            }
            return false;
        } else if (type == BallerinaParser.ASSIGN) {
            if (this.capturedFirstLeftParenthesis) {
                return true;
            }
            this.processToken(token);
            this.forcedProcessedToken = true;
            return false;
        } else if (type == BallerinaParser.SEMICOLON) {
            this.forcedProcessedToken = true;
            return true;
        } else if (type == BallerinaParser.COLON) {
            if (this.capturedFirstColon) {
                this.forcedProcessedToken = true;
                return true;
            }
            this.capturedFirstColon = true;
        }
        return pendingLeftParenthesis == 0 && pendingRightParenthesis == 0;
    }

    protected boolean processToken(Token token) {
        if (this.isCapturingEnabled) {
            return super.processToken(token);
        } else {
            if (token.getType() == BallerinaParser.NEW_LINE || token.getType() == BallerinaParser.EOF ||
                    token.getChannel() != Token.DEFAULT_CHANNEL || token.getType() == BallerinaParser.WS) {
                return false;
            }
            this.lastProcessedToken = token;
            if (pruneTokens) {
                // If the pruneTokens flag is true, replace the token text with empty spaces
                ((CommonToken) token).setText(getNCharLengthEmptyLine(token.getText().length()));
                ((CommonToken) token).setType(BallerinaParser.WS);
                return true;
            }
            return false;
        }
    }
}
