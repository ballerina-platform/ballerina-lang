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
package org.ballerinalang.langserver.compiler.format;

import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.wso2.ballerinalang.compiler.parser.BLangParserListener;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParserErrorStrategy;

/**
 * Custom error strategy for the Formatter.
 * 
 * @since 1.1.0
 */
public class FormatterCustomErrorStrategy extends BallerinaParserErrorStrategy {
    private static final String ACTION_INVOCATION_SYMBOL = "->";
    
    public FormatterCustomErrorStrategy(LSContext context) {
        super(context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY), null);
    }

    @Override
    public void reportInputMismatch(Parser parser, InputMismatchException e) {
        setErrorState(parser);
    }

    @Override
    public void reportMissingToken(Parser parser) {
        setErrorState(parser);
    }

    @Override
    public void reportNoViableAlternative(Parser parser, NoViableAltException e) {
        setErrorState(parser);
    }

    @Override
    public void reportUnwantedToken(Parser parser) {
        setErrorState(parser);
    }

    @Override
    protected void setErrorState(Parser parser) {
        BLangParserListener listener = getListener(parser);
        // Here the type of the exception is not important.
        ParserRuleContext context = parser.getContext();
        // Note: Here we forcefully set the exception to null, in order to avoid the callable unit body being null at
        // the run time
        if (context instanceof BallerinaParser.CallableUnitBodyContext) {
            listener.unsetErrorState();
            return;
        } else if (context instanceof BallerinaParser.SimpleVariableReferenceContext
                && parser.getCurrentToken().getText().equals(ACTION_INVOCATION_SYMBOL)) {
            listener.unsetErrorState();
        }

        // Note: Following check added, when the context is variable definition and the type name context is hit,
        // We need to set the error for the variable definition as well.
        if (context.getParent() instanceof BallerinaParser.VariableDefinitionStatementContext) {
            listener.setErrorState();
        } else if (context instanceof BallerinaParser.ExpressionContext) {
            setContextIfConditionalStatement(context, listener);
        } else {
            setContextIfCheckedExpression(context, listener);
        }
    }

    /**
     * Check the context and identify if the particular context is a child of a connector init and set the exception.
     *
     * @param context       current parser rule context
     * @param listener      Parser Listener instance
     */
    private void setContextIfCheckedExpression(ParserRuleContext context, BLangParserListener listener) {
        ParserRuleContext parentContext = context.getParent();
        if (parentContext != null && parentContext instanceof BallerinaParser.CheckedExpressionContext) {
            listener.setErrorState();
        } else if (parentContext instanceof BallerinaParser.VariableReferenceExpressionContext
                && parentContext.getParent() instanceof BallerinaParser.CheckedExpressionContext) {
            listener.setErrorState();
        }
    }

    /**
     * Set the context if the statement is a conditional statement such as if-else, while or catch.
     *
     * @param context       Current parser rule context
     * @param listener      Parser Listener instance
     */
    private void setContextIfConditionalStatement(ParserRuleContext context, BLangParserListener listener) {
        ParserRuleContext conditionalContext = context.getParent();
        if (conditionalContext == null) {
            return;
        }
        if (conditionalContext instanceof BallerinaParser.IfClauseContext) {
            listener.setErrorState();
        } else if (conditionalContext instanceof BallerinaParser.WhileStatementContext ||
                conditionalContext instanceof BallerinaParser.TypeConversionExpressionContext) {
            listener.setErrorState();
        } else if (conditionalContext instanceof BallerinaParser.BinaryEqualExpressionContext) {
            setContextIfConditionalStatement(conditionalContext, listener);
        } else if (conditionalContext instanceof BallerinaParser.CheckedExpressionContext) {
            setContextIfCheckedExpression(context, listener);
        } else if (conditionalContext instanceof BallerinaParser.ThrowStatementContext) {
            // TODO: need to migrate this check to a top layer
            listener.setErrorState();
        }
    }
}
