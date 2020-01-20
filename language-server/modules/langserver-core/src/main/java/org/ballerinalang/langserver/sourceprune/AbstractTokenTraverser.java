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

/**
 * Abstract token traverser.
 * 
 * @since 0.995.0
 */
public abstract class AbstractTokenTraverser implements TokenTraverser {
    protected Token lastProcessedToken = null;
    protected boolean pruneTokens;
    protected List<CommonToken> processedTokens = new ArrayList<>();

    public AbstractTokenTraverser(boolean pruneTokens) {
        this.pruneTokens = pruneTokens;
    }

    protected boolean processToken(Token token) {
        this.processedTokens.add(new CommonToken(token));
        if (token.getType() == BallerinaParser.NEW_LINE || token.getType() == BallerinaParser.EOF ||
                token.getChannel() != Token.DEFAULT_CHANNEL || token.getType() == BallerinaParser.WS) {
            return false;
        }
        // Otherwise only capture the processed tokens
        this.lastProcessedToken = new CommonToken(token);
        if (this.pruneTokens) {
            // If the pruneTokens flag is true, replace the token text with empty spaces
            ((CommonToken) token).setText(getNCharLengthEmptyLine(token.getText().length()));
            ((CommonToken) token).setType(BallerinaParser.WS);
            return true;
        }
        return false;
    }

    protected static String getNCharLengthEmptyLine(int n) {
        return String.join("", Collections.nCopies(n, " "));
    }

    /**
     * Replace the condition of an if/ else if/ while statement.
     * @param tokenStream   Current token stream
     * @param tokenIndex    Token index of the if token or open parenthesis
     */
    protected void replaceCondition(TokenStream tokenStream, int tokenIndex) {
        Optional<Token> nextDefaultToken = CommonUtil.getNextDefaultToken(tokenStream, tokenIndex);
        nextDefaultToken.ifPresent(token -> ((CommonToken) token)
                .setText(String.join("_", Collections.nCopies(token.getText().length() / 2, "a"))));
    }
}
