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
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.langserver.DocumentServiceKeys;
import org.ballerinalang.langserver.LSContext;
import org.ballerinalang.langserver.common.LSCustomErrorStrategy;
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;

import java.util.Arrays;
import java.util.List;

/**
 * Capture possible errors from source.
 */
public class CompletionCustomErrorStrategy extends LSCustomErrorStrategy {

    private LSContext context;

    private boolean overriddenTokenIndex = false;

    public CompletionCustomErrorStrategy(LSContext context) {
        super(context);
        this.context = context;
    }

    @Override
    public void reportInputMismatch(Parser parser, InputMismatchException e) {
        fillContext(parser, e.getOffendingToken());
    }

    @Override
    public void reportMissingToken(Parser parser) {
        fillContext(parser, parser.getCurrentToken());
    }

    @Override
    public void reportNoViableAlternative(Parser parser, NoViableAltException e) {
        fillContext(parser, e.getOffendingToken());
    }

    @Override
    public void reportUnwantedToken(Parser parser) {
        fillContext(parser, parser.getCurrentToken());
    }

    private void fillContext(Parser parser, Token currentToken) {
        ParserRuleContext currentContext = parser.getContext();
        overriddenTokenIndex = false;
        /*
        TODO: Specific check  is added in order to handle the completion inside an 
        endpoint context. This particular case need to remove after introducing a proper handling mechanism or with
         the introduction of BNF grammar. Also check the 
         */
        boolean isWithinEndpointContext = this.context.get(DocumentServiceKeys.OPERATION_META_CONTEXT_KEY)
                .get(CompletionKeys.META_CONTEXT_IS_ENDPOINT_KEY) == null ? false :
                this.context.get(DocumentServiceKeys.OPERATION_META_CONTEXT_KEY)
                        .get(CompletionKeys.META_CONTEXT_IS_ENDPOINT_KEY);
        if (isCursorBetweenGivenTokenAndLastNonHiddenToken(currentToken, parser)
                || (!isWithinEndpointContext && this.isWithinEndpointContext(parser))) {
            this.context.put(DocumentServiceKeys.PARSER_RULE_CONTEXT_KEY, currentContext);
            this.context.put(DocumentServiceKeys.TOKEN_STREAM_KEY, parser.getTokenStream());
            this.context.put(DocumentServiceKeys.VOCABULARY_KEY, parser.getVocabulary());
            if (!overriddenTokenIndex) {
                this.context.put(DocumentServiceKeys.TOKEN_INDEX_KEY, parser.getCurrentToken().getTokenIndex());
            }
        }
    }

    /**
     * Checks whether cursor is within the whitespace region between current token to last token.
     *
     * @param token  Token to be evaluated
     * @param parser Parser Instance
     * @return true|false
     */
    private boolean isCursorBetweenGivenTokenAndLastNonHiddenToken(Token token, Parser parser) {
        this.setContextException(parser);
        boolean isCursorBetween = false;
        int line = this.context.get(DocumentServiceKeys.POSITION_KEY).getPosition().getLine();
        int character = this.context.get(DocumentServiceKeys.POSITION_KEY).getPosition().getCharacter();

        Token lastNonHiddenToken = null;
        for (int tokenIdx = token.getTokenIndex() - 1; tokenIdx >= 0; tokenIdx--) {
            Token lastToken = parser.getTokenStream().get(tokenIdx);
            if (lastToken.getChannel() != Token.HIDDEN_CHANNEL) {
                lastNonHiddenToken = lastToken;
                break;
            }
        }
        if (lastNonHiddenToken != null) {
            Token tokenBefore = CommonUtil.getPreviousDefaultToken(parser.getTokenStream(),
                    lastNonHiddenToken.getTokenIndex());
            if (tokenBefore != null && UtilSymbolKeys.ANNOTATION_START_SYMBOL_KEY.equals(tokenBefore.getText())
                    && parser.getContext() instanceof BallerinaParser.ServiceBodyContext) {
                overriddenTokenIndex = true;
                this.context.put(DocumentServiceKeys.TOKEN_INDEX_KEY, tokenBefore.getTokenIndex());
                return true;
            }

            // Convert the token lines and char positions to zero based indexing
            int lastNonHiddenTokenLine = lastNonHiddenToken.getLine() - 1;
            int lastNonHiddenTokenChar = lastNonHiddenToken.getCharPositionInLine();
            int tokenLine = token.getLine() - 1;
            int tokenChar = token.getCharPositionInLine();

            if (line >= lastNonHiddenTokenLine && line <= tokenLine) {
                if (line == lastNonHiddenTokenLine) {
                    isCursorBetween = character >= (lastNonHiddenTokenChar + lastNonHiddenToken.getText().length());
                } else {
                    isCursorBetween = line != tokenLine || character <= tokenChar;
                }
            }
        }
        return isCursorBetween;
    }

    private boolean isWithinEndpointContext(Parser parser) {
        int currentTokenIndex = parser.getCurrentToken().getTokenIndex();
        TokenStream tokenStream = parser.getTokenStream();
        List<String> terminalTokens = Arrays.asList("{", ";", "}", "(", ")");
        String tokenString = tokenStream.get(currentTokenIndex).getText();
        boolean isWithinEndpoint = false;
        
        if (tokenString.equals(UtilSymbolKeys.OPEN_BRACE_KEY)) {
            currentTokenIndex -= 1;
            boolean cursorAfterEndpointKeyword = false;
            String endpointName = CommonUtil.getPreviousDefaultToken(tokenStream, currentTokenIndex).getText();

            // Set the endpoint name to the meta info context
            this.context.get(DocumentServiceKeys.OPERATION_META_CONTEXT_KEY)
                    .put(CompletionKeys.META_CONTEXT_ENDPOINT_NAME_KEY, endpointName);

            while (true) {
                if (currentTokenIndex < 0) {
                    // If this stage occurred, then the current context is not able to resolve the endpoint start
                    return false;
                }
                Token previousDefaultToken = CommonUtil.getPreviousDefaultToken(tokenStream, currentTokenIndex);
                tokenString = previousDefaultToken.getText();
                if (terminalTokens.contains(tokenString)) {
                    break;
                } else if (tokenString.equals(UtilSymbolKeys.ENDPOINT_KEYWORD_KEY)) {
                    cursorAfterEndpointKeyword = true;
                    currentTokenIndex = parser.getCurrentToken().getTokenIndex();
                    break;
                }

                currentTokenIndex = previousDefaultToken.getTokenIndex();
            }

            if (cursorAfterEndpointKeyword) {
                while (true) {
                    Token nextDefaultToken = CommonUtil.getNextDefaultToken(tokenStream, currentTokenIndex);
                    tokenString = nextDefaultToken.getText();
                    
                    if (tokenString.equals(UtilSymbolKeys.CLOSE_BRACE_KEY)) {
                        int cursorLine = this.context.get(DocumentServiceKeys.POSITION_KEY).getPosition().getLine();
                        int cursorCol = this.context.get(DocumentServiceKeys.POSITION_KEY).getPosition().getCharacter();
                        int startTokenLine = tokenStream.get(parser.getCurrentToken().getTokenIndex()).getLine() - 1;
                        int startTokenCol = tokenStream
                                .get(parser.getCurrentToken().getTokenIndex()).getCharPositionInLine();
                        int endTokenLine = nextDefaultToken.getLine() - 1;
                        int endTokenCol = nextDefaultToken.getCharPositionInLine();
                        
                        isWithinEndpoint = (cursorLine > startTokenLine && cursorLine < endTokenLine)
                                || (cursorLine == startTokenLine && cursorCol >= startTokenCol
                                && cursorLine < endTokenLine)
                                || (cursorLine > startTokenLine && cursorLine == endTokenLine
                                && cursorCol <= endTokenCol)
                                || (cursorLine == startTokenLine && cursorLine == endTokenLine
                                && cursorCol > startTokenCol && cursorCol < endTokenCol);
                        break;
                    } else if (terminalTokens.contains(tokenString)) {
                        break;
                    }

                    currentTokenIndex = nextDefaultToken.getTokenIndex();
                }
            }
        }

        this.context.get(DocumentServiceKeys.OPERATION_META_CONTEXT_KEY)
                .put(CompletionKeys.META_CONTEXT_IS_ENDPOINT_KEY, isWithinEndpoint);
        return isWithinEndpoint;
    }
}
