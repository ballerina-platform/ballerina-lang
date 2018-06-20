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

    public CompletionCustomErrorStrategy(LSContext context) {
        super(context);
        this.context = context;
    }

    @Override
    public void reportInputMismatch(Parser parser, InputMismatchException e) {
        logError(e.getMessage());
    }

    @Override
    public void reportMissingToken(Parser parser) {
        logError("");
    }

    @Override
    public void reportNoViableAlternative(Parser parser, NoViableAltException e) {
        logError(e.getMessage());
    }

    @Override
    public void reportUnwantedToken(Parser parser) {
        logError("");
    }

    @Override
    public void reportMatch(Parser recognizer) {
        super.reportMatch(recognizer);
        if (recognizer.getCurrentToken().getType() != BallerinaParser.EOF && isInLastTermination(recognizer)) {
            // -2 since Parser.match() consumes one extra + skip current token
            deleteTokensUpToCursor(recognizer, -2);
        }
    }

    @Override
    public void sync(Parser recognizer) throws RecognitionException {
        if (recognizer.getCurrentToken().getType() != BallerinaParser.EOF && isInFirstTokenOfCursorLine(recognizer)) {
            // -1 since skip current token
            deleteTokensUpToCursor(recognizer, -1);
        }
        super.sync(recognizer);
    }

    private boolean isInFirstTokenOfCursorLine(Parser recognizer) {
        Token currentToken = recognizer.getCurrentToken();
        Token firstTokenOfCursorLine = getFirstTokenOfCursorLine(recognizer.getInputStream());
        return firstTokenOfCursorLine != null && currentToken.getTokenIndex() == firstTokenOfCursorLine.getTokenIndex();
    }

    private boolean isInLastTermination(Parser recognizer) {
        Token currentToken = recognizer.getCurrentToken();
        Token lastTerminationToken = getLastTerminationToken(recognizer.getInputStream());
        return lastTerminationToken != null && currentToken.getTokenIndex() == lastTerminationToken.getTokenIndex();
    }

    private void deleteTokensUpToCursor(Parser recognizer, int tokenOffset) {
        Stack<Token> forceConsumedTokens = new Stack<>();
        Position cursorPosition = this.context.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        int cursorLine = cursorPosition.getLine() + 1;
        int cursorCol = cursorPosition.getCharacter() + 1;

        int index = 1;
        Token beforeCursorToken = recognizer.getInputStream().LT(index);
        int type = beforeCursorToken.getType();
        int tLine = beforeCursorToken.getLine();
        int tCol = beforeCursorToken.getCharPositionInLine();

        int needToRemoveTokenCount = tokenOffset;
        while (type != BallerinaParser.EOF && ((tLine < cursorLine) || (tLine == cursorLine && tCol < cursorCol))) {
            beforeCursorToken = recognizer.getInputStream().LT(index);
            type = beforeCursorToken.getType();
            tLine = beforeCursorToken.getLine();
            tCol = beforeCursorToken.getCharPositionInLine();
            index++;
            needToRemoveTokenCount++;
        }

        while (needToRemoveTokenCount > 0) {
            forceConsumedTokens.push(recognizer.consume());
            needToRemoveTokenCount--;
        }

        this.context.put(CompletionKeys.FORCE_CONSUMED_TOKENS_KEY, forceConsumedTokens);

//        Token currentToken = recognizer.getCurrentToken();
//        Token prevToken = recognizer.getInputStream().LT(-1);
//        if (currentToken.getLine() == prevToken.getLine()) {
//            //To avoid catching incorrect contexts
//            ParserRuleContext context = recognizer.getContext();
//            this.context.put(DocumentServiceKeys.TOKEN_INDEX_KEY, currentToken.getTokenIndex());
//            this.context.put(DocumentServiceKeys.PARSER_RULE_CONTEXT_KEY, context);
//            this.context.put(DocumentServiceKeys.TOKEN_STREAM_KEY, recognizer.getTokenStream());
//        }
    }

    private Token getFirstTokenOfCursorLine(TokenStream tokenStream) {
        Token firstCursorLineToken;
        int cursorLine = this.context.get(DocumentServiceKeys.POSITION_KEY).getPosition().getLine() + 1;

        int index = 1;
        Token beforeCursorToken = tokenStream.LT(index);
        int type = beforeCursorToken.getType();
        int tLine = beforeCursorToken.getLine();

        firstCursorLineToken = (tLine == cursorLine) ? beforeCursorToken : null;

        while (type != BallerinaParser.EOF && (tLine <= cursorLine)) {
            if (tLine == cursorLine) {
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
            beforeCursorToken = tokenStream.LT(index);
            type = beforeCursorToken.getType();
            tLine = beforeCursorToken.getLine();
            tCol = beforeCursorToken.getCharPositionInLine();
            if (beforeCursorToken.getTokenIndex() == 1 || type == BallerinaParser.SEMICOLON ||
                    type == BallerinaParser.LEFT_BRACE || type == BallerinaParser.RIGHT_BRACE ||
                    type == BallerinaParser.COMMA) {
                lastTerminationToken = beforeCursorToken;
            }
            index++;
        }
        return lastTerminationToken;
    }

    private void logError(String message) {
        logger.error("Invalid Source Fragment found: [" + message + " ]");
    }
}
