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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * LHS token traverser.
 * 
 * @since 0.995.0
 */
class LHSTokenTraverser extends AbstractTokenTraverser {
    private List<Integer> lhsTraverseTerminals;
    private List<Integer> definitionKWTerminals;
    private boolean removeDefinition;
    private int closeParenthesisCount;
    private int lessThanSymbolCount;

    LHSTokenTraverser(List<Integer> traverseTerminals, List<Integer> kwTerminals) {
        this.lhsTraverseTerminals = traverseTerminals;
        this.definitionKWTerminals = kwTerminals;
        this.removeDefinition = false;
        this.closeParenthesisCount = 0;
        this.lessThanSymbolCount = 0;
    }

    void traverseLHS(TokenStream tokenStream, int tokenIndex) {
        Optional<Token> token = Optional.of(tokenStream.get(tokenIndex));
        while (token.isPresent()) {
            int type = token.get().getType();
            if (this.lhsTraverseTerminals.contains(type)) {
                boolean terminate = terminateLHSTraverse(token.get(), tokenStream);
                if (terminate) {
                    break;
                }
            }
            if (this.definitionKWTerminals.contains(type)) {
                this.removeDefinition = true;
            }
            alterTokenText(token.get());
            token = CommonUtil.getPreviousDefaultToken(tokenStream, token.get().getTokenIndex());
        }
    }

    private boolean terminateLHSTraverse(Token token, TokenStream tokenStream) {
        int type = token.getType();
        if (type == BallerinaParser.RIGHT_PARENTHESIS) {
            this.closeParenthesisCount++;
            this.alterTokenText(token);
            return false;
        }
        if (type == BallerinaParser.LEFT_PARENTHESIS) {
            if (this.closeParenthesisCount > 0) {
                this.closeParenthesisCount--;
                this.alterTokenText(token);
                return false;
            } else {
                return true;
            }
        }
        /*
        Specially capture the LT token in order to avoid Right token removal during the following case
        public annotation <resource,r> ...
        RHS Token traverser will check for the less than symbol count when a GT token found
         */
        if (type == BallerinaParser.LT) {
            this.alterTokenText(token);
            this.lessThanSymbolCount++;
            return false;
        }
        // Handle the ON token replacing since this is used in both service and JSON streaming input
        boolean onServiceRule = CommonUtil.getNDefaultTokensToLeft(tokenStream, 2, token.getTokenIndex()).stream()
                .map(Token::getType)
                .collect(Collectors.toList())
                .contains(BallerinaParser.SERVICE);
        if (token.getType() == BallerinaParser.COMMA || (!onServiceRule && token.getType() == BallerinaParser.ON)) {
            this.alterTokenText(token);
        }
        
        return true;
    }

    /**
     * Removed the definition or not.
     * 
     * @return {@link Boolean} removed the definition or not
     */
    public boolean isRemoveDefinition() {
        return removeDefinition;
    }

    /**
     * Get the number of less than symbols found while removing the tokens.
     *
     * @return {@link Integer}  number of LT tokens found during the left traversal
     */
    public int getLessThanSymbolCount() {
        return lessThanSymbolCount;
    }
}
