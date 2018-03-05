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
import org.ballerinalang.langserver.DocumentServiceKeys;
import org.ballerinalang.langserver.LanguageServerContext;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParserErrorStrategy;

/**
 * Capture possible errors from source.
 */
public class BallerinaCustomErrorStrategy extends BallerinaParserErrorStrategy {
    private LanguageServerContext context;
    
    public BallerinaCustomErrorStrategy(LanguageServerContext context) {
        super(context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY), null);
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
        /*
        TODO: Specific check for callable unit body is added in order to handle the completion inside an 
        endpoint definition. This particular case need to remove after introducing a proper handling mechanism or with
         the introduction of BNF grammar
         */
        if (isCursorBetweenGivenTokenAndLastNonHiddenToken(currentToken, parser)
                || (this.context.get(DocumentServiceKeys.PARSER_RULE_CONTEXT_KEY) != null
                && currentContext instanceof BallerinaParser.CallableUnitBodyContext)) {
            this.context.put(DocumentServiceKeys.PARSER_RULE_CONTEXT_KEY, currentContext);
            this.context.put(DocumentServiceKeys.TOKEN_STREAM_KEY, parser.getTokenStream());
            this.context.put(DocumentServiceKeys.VOCABULARY_KEY, parser.getVocabulary());
            this.context.put(DocumentServiceKeys.TOKEN_INDEX_KEY, parser.getCurrentToken().getTokenIndex());
        }
    }
    /**
     * Checks whether cursor is within the whitespace region between current token to last token.
     * @param token Token to be evaluated
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

    @Override
    protected void setContextException(Parser parser) {
        // Here the type of the exception is not important.
        InputMismatchException e = new InputMismatchException(parser);
        ParserRuleContext context = parser.getContext();
        // Note: Here we forcefully set the exception to null, in order to avoid the callable unit body being null at
        // the run time
        if (context instanceof BallerinaParser.CallableUnitBodyContext) {
            context.exception = null;
            return;
        }
        context.exception = e;
        // Note: Following check added, when the context is variable definition and the type name context is hit,
        // We need to set the error for the variable definition as well.
        if (context.getParent() instanceof BallerinaParser.VariableDefinitionStatementContext) {
            context.getParent().exception = e;
            return;
        }

        if (context instanceof BallerinaParser.NameReferenceContext) {
            setContextIfConnectorInit(context, e);
        }
    }

    /**
     * Check the context and identify if the particular context is a child of a connector init and set the exception.
     * @param context   current parser rule context
     * @param e         exception to set
     */
    private void setContextIfConnectorInit(ParserRuleContext context, InputMismatchException e) {
        ParserRuleContext connectorInitContext = context.getParent().getParent().getParent();
        if (connectorInitContext instanceof BallerinaParser.ConnectorInitExpressionContext) {
            ParserRuleContext tempContext = context;
            while (true) {
                tempContext.exception = e;
                tempContext = tempContext.getParent();
                if (tempContext.equals(connectorInitContext)) {
                    tempContext.getParent().exception = e;
                    break;
                }
            }
            connectorInitContext.getParent().exception = e;
        }
    }
}
