/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerinalang.compiler.internal.parser;

import io.ballerinalang.compiler.syntax.tree.SyntaxKind;

/**
 * Error handler to recover from XML parsing errors.
 * 
 * @since 2.0.0
 */
public class XMLParserErrorHandler extends AbstractParserErrorHandler {

    public XMLParserErrorHandler(AbstractTokenReader tokenReader) {
        super(tokenReader);
    }

    @Override
    protected boolean isProductionWithAlternatives(ParserRuleContext currentCtx) {
        switch (currentCtx) {
            default:
                return false;
        }
    }

    @Override
    protected Result seekMatch(ParserRuleContext currentCtx, int lookahead, int currentDepth, boolean isEntryPoint) {
        return null;
    }

    @Override
    protected ParserRuleContext getNextRule(ParserRuleContext currentCtx, int nextLookahead) {
        switch (currentCtx) {
            default:
                return null;
        }
    }

    @Override
    protected SyntaxKind getExpectedTokenKind(ParserRuleContext context) {
        switch (context) {
            case LT_TOKEN:
                return SyntaxKind.LT_TOKEN;
            case GT_TOKEN:
                return SyntaxKind.GT_TOKEN;
            case SLASH:
                return SyntaxKind.SLASH_TOKEN;
            case XML_KEYWORD:
                return SyntaxKind.XML_KEYWORD;
            default:
                return null;
        }
    }

    @Override
    public ParserRuleContext findBestPath(ParserRuleContext context) {
        throw new UnsupportedOperationException();
    }

}
