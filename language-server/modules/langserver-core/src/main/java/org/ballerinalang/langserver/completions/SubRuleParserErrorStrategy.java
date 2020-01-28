/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.ballerinalang.langserver.compiler.LSContext;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;

/**
 * Capture possible errors from source.
 */
public class SubRuleParserErrorStrategy extends DefaultErrorStrategy {

    private LSContext context;
    
    private boolean terminate = false;

    public SubRuleParserErrorStrategy(LSContext context) {
        this.context = context;
    }

    @Override
    public void reportInputMismatch(Parser parser, InputMismatchException e) {
        fillContext(parser.getContext());
    }

    @Override
    public void reportMissingToken(Parser parser) {
        fillContext(parser.getContext());
    }

    @Override
    public void reportNoViableAlternative(Parser parser, NoViableAltException e) {
        fillContext(parser.getContext());
    }

    @Override
    public void reportUnwantedToken(Parser parser) {
        fillContext(parser.getContext());
    }

    private void fillContext(ParserRuleContext parserRuleContext) {
        if (parserRuleContext == null || this.terminate) {
            return;
        }
        if (isInvalidExpressionContext(parserRuleContext)
                || parserRuleContext instanceof BallerinaParser.AnyIdentifierNameContext
                || parserRuleContext instanceof BallerinaParser.BlockFunctionBodyContext
                || parserRuleContext instanceof BallerinaParser.PackageNameContext
                || parserRuleContext instanceof BallerinaParser.TypeNameContext
                || parserRuleContext instanceof BallerinaParser.TypeInitExprContext
                || parserRuleContext instanceof BallerinaParser.PeerWorkerContext
                || parserRuleContext instanceof BallerinaParser.ConstantExpressionContext
                || parserRuleContext instanceof BallerinaParser.SimpleVariableReferenceContext
                || parserRuleContext instanceof BallerinaParser.VariableReferenceExpressionContext) {
            this.fillContext(parserRuleContext.getParent());
        } else {
            this.context.put(CompletionKeys.PARSER_RULE_CONTEXT_KEY, parserRuleContext);
            this.terminate = true;
        }
    }
    
    private boolean isInvalidExpressionContext(ParserRuleContext context) {
        return !(context instanceof BallerinaParser.WorkerSendSyncExpressionContext)
                && !(context instanceof BallerinaParser.WorkerReceiveExpressionContext) 
                && context instanceof BallerinaParser.ExpressionContext;
    }
}
