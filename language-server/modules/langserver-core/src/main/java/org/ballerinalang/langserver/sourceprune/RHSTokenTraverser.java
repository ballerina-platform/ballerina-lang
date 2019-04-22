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
    private int openBraceCount = 0;
    private List<Integer> rhsTraverseTerminals;
    private boolean definitionRemoved;
    private int lhsLTTokenCount;

    RHSTokenTraverser(List<Integer> traverseTerminals, boolean definitionRemoved, int lhsLTTokenCount) {
        this.rhsTraverseTerminals = traverseTerminals;
        this.definitionRemoved = definitionRemoved;
        this.lhsLTTokenCount = lhsLTTokenCount;
    }

    void traverseRHS(TokenStream tokenStream, int tokenIndex) {
        Optional<Token> token = Optional.of(tokenStream.get(tokenIndex));
        while (token.isPresent()) {
            int type = token.get().getType();
            if (BallerinaParser.RIGHT_BRACE == type && openBraceCount > 0) {
                openBraceCount--;
            } else if (BallerinaParser.LEFT_BRACE == type && this.definitionRemoved) {
                openBraceCount++;
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
        if (token.getType() == BallerinaParser.SEMICOLON || token.getType() == BallerinaParser.COMMA) {
            this.alterTokenText(token);
        }
        /*
        Address the case of removing the LT symbol during the LHS traversal
        Ex: map<> ...
            annotation <r> ...
         */
        if (token.getType() == BallerinaParser.GT && this.lhsLTTokenCount > 0) {
            this.lhsLTTokenCount--;
            this.alterTokenText(token);
            return false;
        }
        
        return true;
    }
}
