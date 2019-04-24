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

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;

import java.util.List;
import java.util.Optional;

/**
 * RHS Token Traverser.
 * 
 * @since 0.995.0
 */
class RHSTokenTraverser extends AbstractTokenTraverser {
    private int leftBraceCount;
    private int leftParenthesisCount;
    private List<Integer> rhsTraverseTerminals;
    private boolean definitionRemoved;
    private int ltTokenCount;

    RHSTokenTraverser(SourcePruneContext sourcePruneContext) {
        this.leftBraceCount = sourcePruneContext.get(SourcePruneKeys.LEFT_BRACE_COUNT_KEY);
        this.leftParenthesisCount = sourcePruneContext.get(SourcePruneKeys.LEFT_PARAN_COUNT_KEY);
        this.rhsTraverseTerminals = sourcePruneContext.get(SourcePruneKeys.RHS_TRAVERSE_TERMINALS_KEY);
        this.definitionRemoved = sourcePruneContext.get(SourcePruneKeys.REMOVE_DEFINITION_KEY);
        this.ltTokenCount = sourcePruneContext.get(SourcePruneKeys.LT_COUNT_KEY);
    }

    void traverseRHS(TokenStream tokenStream, int tokenIndex) {
        Optional<Token> token = Optional.of(tokenStream.get(tokenIndex));
        while (token.isPresent()) {
            int type = token.get().getType();
            if (BallerinaParser.RIGHT_BRACE == type && leftBraceCount > 0) {
                leftBraceCount--;
            } else if (BallerinaParser.LEFT_BRACE == type && this.definitionRemoved) {
                leftBraceCount++;
            } else if (BallerinaParser.LEFT_PARENTHESIS == type) {
                this.leftParenthesisCount++;
            } else if (rhsTraverseTerminals.contains(type)) {
                boolean terminateRHSTraverse = terminateRHSTraverse(token.get());
                if (terminateRHSTraverse) {
                    break;
                }
            }
            this.alterTokenText(token.get());
            token = CommonUtil.getNextDefaultToken(tokenStream, token.get().getTokenIndex());
        }
    }

    private boolean terminateRHSTraverse(Token token) {
        int type = token.getType();
        /*
        Address the case of removing the LT symbol during the LHS traversal
        Ex: map<> ...
            annotation <r> ...
         */
        if (type == BallerinaParser.GT && this.ltTokenCount > 0) {
            this.ltTokenCount--;
            this.alterTokenText(token);
            return false;
        }
        if (type == BallerinaParser.RIGHT_PARENTHESIS && this.leftParenthesisCount > 0) {
            this.leftParenthesisCount--;
            this.alterTokenText(token);
            return false;
        }
        if (type == BallerinaParser.SEMICOLON || type == BallerinaParser.COMMA) {
            this.alterTokenText(token);
        }
        
        return true;
    }
}
