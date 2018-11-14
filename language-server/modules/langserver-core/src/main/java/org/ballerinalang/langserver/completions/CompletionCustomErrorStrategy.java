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
package org.ballerinalang.langserver.completions;

import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.common.LSCustomErrorStrategy;
import org.eclipse.lsp4j.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;

import java.util.Stack;

/**
 * Capture possible errors from source.
 */
public class CompletionCustomErrorStrategy extends LSCustomErrorStrategy {

    private static final Logger logger = LoggerFactory.getLogger(CompletionCustomErrorStrategy.class);

    private LSContext context;

    private int removeTokenCount = 0;

    private Token removeStartToken = null;

    private Stack<Token> forceConsumedTokens = new Stack<>();

    private Token lastTerminationToken = null;

    private Token firstTokenOfCursorLine = null;

    private enum TokenRemovalStrategy { MATCH, SYNC }

    public CompletionCustomErrorStrategy(LSContext context) {
        super(context);
        this.context = context;
    }

    @Override
    public void reportInputMismatch(Parser parser, InputMismatchException e) {
        if (!parser.getContext().start.getTokenSource().getSourceName()
                .equals(context.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY))) {
            return;
        }
        this.context.put(CompletionKeys.TOKEN_STREAM_KEY, parser.getTokenStream());
    }

    @Override
    public void reportMissingToken(Parser parser) {
        if (!parser.getContext().start.getTokenSource().getSourceName()
                .equals(context.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY))) {
            return;
        }
        this.context.put(CompletionKeys.TOKEN_STREAM_KEY, parser.getTokenStream());
    }

    @Override
    public void reportNoViableAlternative(Parser parser, NoViableAltException e) {
        if (!parser.getContext().start.getTokenSource().getSourceName()
                .equals(context.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY))) {
            return;
        }
        this.context.put(CompletionKeys.TOKEN_STREAM_KEY, parser.getTokenStream());
    }

    @Override
    public void reportUnwantedToken(Parser parser) {
        if (!parser.getContext().start.getTokenSource().getSourceName()
                .equals(context.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY))) {
            return;
        }
        this.context.put(CompletionKeys.TOKEN_STREAM_KEY, parser.getTokenStream());
    }

    @Override
    public void reportMatch(Parser recognizer) {
        if (!recognizer.getContext().start.getTokenSource().getSourceName()
                .equals(context.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY))) {
            super.reportMatch(recognizer);
            return;
        }
        removePendingTokensAfterThisToken(recognizer, removeStartToken, TokenRemovalStrategy.MATCH);
        boolean inFirstTokenOfCursorLine = isInFirstTokenOfCursorLine(recognizer);
        boolean inLastTermination = isInLastTermination(recognizer);
        super.reportMatch(recognizer);
        if (recognizer.getCurrentToken().getType() != BallerinaParser.EOF && inLastTermination) {
            deleteTokensUpToCursor(recognizer, inLastTermination, inFirstTokenOfCursorLine);
        }
    }

    @Override
    public void sync(Parser recognizer) throws RecognitionException {
        if (!recognizer.getContext().start.getTokenSource().getSourceName()
                .equals(context.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY))) {
            super.sync(recognizer);
            return;
        }
        removePendingTokensAfterThisToken(recognizer, removeStartToken, TokenRemovalStrategy.SYNC);
        boolean inFirstTokenOfCursorLine = isInFirstTokenOfCursorLine(recognizer);
        boolean inLastTermination = isInLastTermination(recognizer);
        if (recognizer.getCurrentToken().getType() != BallerinaParser.EOF && inFirstTokenOfCursorLine) {
            deleteTokensUpToCursor(recognizer, inLastTermination, inFirstTokenOfCursorLine);
        } else if (recognizer.getCurrentToken().getType() != BallerinaParser.EOF && inLastTermination) {
            deleteTokensUpToCursor(recognizer, inLastTermination, inFirstTokenOfCursorLine);
        }
        super.sync(recognizer);
    }

    private void removePendingTokensAfterThisToken(Parser recognizer, Token token,
                                                   TokenRemovalStrategy currentStrategy) {
        int currentTokenIndex = recognizer.getCurrentToken().getTokenIndex();
        if (token != null && (isInLastTermination(recognizer) ||
                TokenRemovalStrategy.SYNC.equals(currentStrategy) && currentTokenIndex < token.getTokenIndex()
                || TokenRemovalStrategy.MATCH.equals(currentStrategy) && currentTokenIndex <= token.getTokenIndex())) {
            return;
        }
        while (removeTokenCount > 0) {
            forceConsumedTokens.push(recognizer.consume());
            removeTokenCount--;
        }
        this.context.put(CompletionKeys.FORCE_CONSUMED_TOKENS_KEY, forceConsumedTokens);
    }

    private boolean isInFirstTokenOfCursorLine(Parser recognizer) {
        Token currentToken = recognizer.getCurrentToken();
        if (firstTokenOfCursorLine == null) {
            firstTokenOfCursorLine = getFirstTokenOfCursorLine(recognizer);
        }
        return firstTokenOfCursorLine != null && currentToken.getTokenIndex() == firstTokenOfCursorLine.getTokenIndex();
    }

    private boolean isInLastTermination(Parser recognizer) {
        Token currentToken = recognizer.getCurrentToken();
        if (lastTerminationToken == null) {
            lastTerminationToken = getLastTerminationToken(recognizer.getInputStream());
        }
        return lastTerminationToken != null && currentToken.getTokenIndex() == lastTerminationToken.getTokenIndex();
    }

    private void deleteTokensUpToCursor(Parser recognizer, boolean isInLastTermination,
                                        boolean isInFirstTokenOfCursorLine) {
        Position cursorPosition = this.context.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        int cursorLine = cursorPosition.getLine() + 1;
        int cursorCol = cursorPosition.getCharacter() + 1;

        int index = 1;
        Token beforeCursorToken = recognizer.getInputStream().LT(index);
        int type = beforeCursorToken.getType();
        int tLine = beforeCursorToken.getLine();
        int tCol = beforeCursorToken.getCharPositionInLine();

        boolean noTerminationToken = ((isInFirstTokenOfCursorLine && lastTerminationToken == null)
                || (firstTokenOfCursorLine != null && lastTerminationToken.getTokenIndex()
                > firstTokenOfCursorLine.getTokenIndex()));
        int needToRemoveTokenCount = noTerminationToken ? 0 : -1;
        while (type != BallerinaParser.EOF && ((tLine < cursorLine) || (tLine == cursorLine && tCol < cursorCol))) {
            index++;
            needToRemoveTokenCount++;
            beforeCursorToken = recognizer.getInputStream().LT(index);
            type = beforeCursorToken.getType();
            tLine = beforeCursorToken.getLine();
            tCol = beforeCursorToken.getCharPositionInLine();
        }

        if (isInLastTermination) {
            removeTokenCount = needToRemoveTokenCount;
            removeStartToken = lastTerminationToken;
        } else if (noTerminationToken) {
            removeTokenCount = needToRemoveTokenCount;
            removeStartToken = recognizer.getInputStream().LT(1);
            removePendingTokensAfterThisToken(recognizer, removeStartToken, TokenRemovalStrategy.SYNC);
        }
    }

    private Token getFirstTokenOfCursorLine(Parser recognizer) {
        TokenStream tokenStream = recognizer.getInputStream();
        Token firstCursorLineToken = null;
        int cursorLine = this.context.get(DocumentServiceKeys.POSITION_KEY).getPosition().getLine() + 1;
        int cursorCol = this.context.get(DocumentServiceKeys.POSITION_KEY).getPosition().getCharacter() + 1;

        int index = 1;
        Token beforeCursorToken = tokenStream.LT(index);
        int type = beforeCursorToken.getType();
        int tLine = beforeCursorToken.getLine();
        int tCol = beforeCursorToken.getCharPositionInLine();

        if (cursorLine < tLine || (cursorLine == tLine && cursorCol <= tCol)) {
            return null;
        }

        firstCursorLineToken = (tLine == cursorLine) ? beforeCursorToken : null;

        while (type != BallerinaParser.EOF && (tLine <= cursorLine)) {
            int tokenIndex = beforeCursorToken.getTokenIndex();
            if (tLine == cursorLine &&
                    (firstCursorLineToken == null || firstCursorLineToken.getTokenIndex() > tokenIndex)) {
                firstCursorLineToken = beforeCursorToken;
            }
            index++;
            beforeCursorToken = tokenStream.LT(index);
            type = beforeCursorToken.getType();
            tLine = beforeCursorToken.getLine();
        }
        return firstCursorLineToken;
    }

    private Token getLastTerminationToken(TokenStream tokenStream) {
        Token lastTerminationToken = null;
        Position cursorPosition = this.context.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        int cursorLine = cursorPosition.getLine() + 1;
        int cursorCol = cursorPosition.getCharacter() + 1;

        int index = 1;
        Token beforeCursorToken = tokenStream.LT(index);
        int type = beforeCursorToken.getType();
        int tLine = beforeCursorToken.getLine();
        int tCol = beforeCursorToken.getCharPositionInLine();

        while (type != BallerinaParser.EOF && ((tLine < cursorLine) || (tLine == cursorLine && tCol < cursorCol))) {
            // Checking for terminating tokens with the type number is inconsistent. Thus, uses string comparisons
            String tokenText = beforeCursorToken.getText();
            if (beforeCursorToken.getTokenIndex() == 1 ||
                    UtilSymbolKeys.SEMI_COLON_SYMBOL_KEY.equals(tokenText) ||
                    UtilSymbolKeys.OPEN_BRACE_KEY.equals(tokenText) ||
                    UtilSymbolKeys.CLOSE_BRACE_KEY.equals(tokenText) ||
                    UtilSymbolKeys.COMMA_SYMBOL_KEY.equals(tokenText)) {
                lastTerminationToken = beforeCursorToken;
            }
            index++;
            beforeCursorToken = tokenStream.LT(index);
            type = beforeCursorToken.getType();
            tLine = beforeCursorToken.getLine();
            tCol = beforeCursorToken.getCharPositionInLine();
        }
        return lastTerminationToken;
    }
}
