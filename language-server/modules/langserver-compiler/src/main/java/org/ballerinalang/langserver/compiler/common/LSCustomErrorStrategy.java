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
package org.ballerinalang.langserver.compiler.common;

import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParserErrorStrategy;

/**
 * Custom error strategy for language server.
 */
public class LSCustomErrorStrategy extends BallerinaParserErrorStrategy {
    public LSCustomErrorStrategy(LSContext context) {
        super(context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY), null);
    }

    @Override
    public void reportInputMismatch(Parser parser, InputMismatchException e) {
        setContextException(parser);
    }

    @Override
    public void reportMissingToken(Parser parser) {
        setContextException(parser);
    }

    @Override
    public void reportNoViableAlternative(Parser parser, NoViableAltException e) {
        setContextException(parser);
    }

    @Override
    public void reportUnwantedToken(Parser parser) {
        setContextException(parser);
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
        } else if (context instanceof BallerinaParser.SimpleVariableReferenceContext) {
            context.exception = null;
        } else {
            context.exception = e;
        }
        // Note: Following check added, when the context is variable definition and the type name context is hit,
        // We need to set the error for the variable definition as well.
        if (context.getParent() instanceof BallerinaParser.VariableDefinitionStatementContext) {
            context.getParent().exception = e;
        } else if (context instanceof BallerinaParser.ExpressionContext) {
            setContextIfConditionalStatement(context, e);
        } else {
            setContextIfCheckedExpression(context, e);
        }
    }

    /**
     * Check the context and identify if the particular context is a child of a connector init and set the exception.
     *
     * @param context current parser rule context
     * @param e       exception to set
     */
    private void setContextIfCheckedExpression(ParserRuleContext context, InputMismatchException e) {
        ParserRuleContext parentContext = context.getParent();
        if (parentContext != null && parentContext instanceof BallerinaParser.CheckedExpressionContext) {
            context.getParent().exception = e;
            context.getParent().getParent().exception = e;
        } else if (parentContext instanceof BallerinaParser.VariableReferenceExpressionContext
                && parentContext.getParent() instanceof BallerinaParser.CheckedExpressionContext) {
            parentContext.exception = e;
            parentContext.getParent().exception = e;
            parentContext.getParent().getParent().exception = e;
        }
    }

    /**
     * Set the context if the statement is a conditional statement such as if-else, while or catch.
     *
     * @param context Current parser rule context
     * @param e       Exception of the parser context
     */
    private void setContextIfConditionalStatement(ParserRuleContext context, InputMismatchException e) {
        ParserRuleContext conditionalContext = context.getParent();
        if (conditionalContext == null) {
            return;
        }
        if (conditionalContext instanceof BallerinaParser.IfClauseContext) {
            conditionalContext.getParent().exception = e;
        } else if (conditionalContext instanceof BallerinaParser.WhileStatementContext) {
            conditionalContext.exception = e;
        } else if (conditionalContext instanceof BallerinaParser.BinaryEqualExpressionContext) {
            setContextIfConditionalStatement(conditionalContext, e);
        } else if (conditionalContext instanceof BallerinaParser.CheckedExpressionContext) {
            setContextIfCheckedExpression(context, e);
        }
    }
}
