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
package org.ballerinalang.composer.service.workspace.suggetions;

import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.IntervalSet;
import org.ballerinalang.composer.service.workspace.langserver.dto.Position;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParserErrorStrategy;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.LinkedList;
import java.util.List;

/**
 * Capture possible errors from source
 */
public class CapturePossibleTokenStrategy extends BallerinaParserErrorStrategy {

    protected final Position cursorPosition;

    protected List<PossibleToken> possibleTokens;

    private SuggestionsFilterDataModel suggestionsFilterDataModel;

    public CapturePossibleTokenStrategy(CompilerContext compilerContext, Position cursorPosition, SuggestionsFilterDataModel filterDataModel) {
        super(compilerContext, null);
        this.cursorPosition = cursorPosition;
        possibleTokens = new LinkedList<>();
        this.suggestionsFilterDataModel = filterDataModel;
    }
    @Override
    public void reportInputMismatch(Parser parser, InputMismatchException e) {
        fetchPossibleTokens(parser, e.getOffendingToken(), e.getExpectedTokens());
    }

    @Override
    public void reportMissingToken(Parser parser) {
        fetchPossibleTokens(parser, parser.getCurrentToken(), parser.getExpectedTokens());
    }

    @Override
    public void reportNoViableAlternative(Parser parser, NoViableAltException e) {
        fetchPossibleTokens(parser, e.getOffendingToken(), e.getExpectedTokens());
    }

    @Override
    public void reportUnwantedToken(Parser parser) {
        fetchPossibleTokens(parser, parser.getCurrentToken(), parser.getExpectedTokens());
    }

    public List<PossibleToken> getPossibleTokens() {
        return possibleTokens;
    }

    protected void fetchPossibleTokens(Parser parser, Token currentToken, IntervalSet expectedTokens) {
        ParserRuleContext currentContext = parser.getContext();
        // Currently disabling the check since the possible token based implementation has been skipped

        if (isCursorBetweenGivenTokenAndLastNonHiddenToken(currentToken, parser)) {
            this.suggestionsFilterDataModel.initParserContext(parser, currentContext, this.possibleTokens);
        }
//        this.suggestionsFilterDataModel.initParserContext(parser, currentContext, this.possibleTokens);

    }
    /**
     * Checks whether cursor is within the whitespace region between current token to last token
     * @param token Token to be evaluated
     * @param parser Parser Instance
     * @return true|false
     */
    protected boolean isCursorBetweenGivenTokenAndLastNonHiddenToken(Token token, Parser parser) {
        this.setContextException(parser);
        boolean isCursorBetween = false;
        if (cursorPosition.equals(getSourcePosition(token))) {
            isCursorBetween = true;
        } else {
            Token lastNonHiddenToken = null;
            for (int tokenIdx = token.getTokenIndex() - 1; tokenIdx >= 0; tokenIdx--) {
                Token lastToken = parser.getTokenStream().get(tokenIdx);
                if (lastToken.getChannel() != Token.HIDDEN_CHANNEL) {
                    lastNonHiddenToken = lastToken;
                    break;
                }
            }
            if (lastNonHiddenToken != null) {
                if (cursorPosition.getLine() >= lastNonHiddenToken.getLine()
                        && cursorPosition.getLine() <= token.getLine()) {
                    if (cursorPosition.getLine() == lastNonHiddenToken.getLine()) {
                        isCursorBetween = cursorPosition.getCharacter() >=
                                (lastNonHiddenToken.getCharPositionInLine()
                                        + lastNonHiddenToken.getText().length());
                    } else if (cursorPosition.getLine() == token.getLine()) {
                        isCursorBetween = cursorPosition.getCharacter() <= token.getCharPositionInLine();
                    } else {
                        isCursorBetween = true;
                    }
                }
            }

        }
        return isCursorBetween;
    }

    @Override
    protected void setContextException(Parser parser) {
        // Here the type of the exception is not important.
        InputMismatchException e = new InputMismatchException(parser);
        ParserRuleContext context = parser.getContext();
        context.exception = e;
    }

    protected Position getSourcePosition(Token token) {
        Position position = new Position();
        position.setLine(token.getLine());
        position.setCharacter(token.getCharPositionInLine());
        return position;
    }
}
