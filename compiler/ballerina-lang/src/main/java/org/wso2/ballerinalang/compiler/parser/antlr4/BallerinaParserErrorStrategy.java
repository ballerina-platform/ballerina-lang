/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerinalang.compiler.parser.antlr4;

import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.FailedPredicateException;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.IntervalSet;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.parser.BLangParserListener;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.diagnotic.BDiagnosticSource;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLogHelper;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

/**
 * Error Handling strategy for {@link BallerinaParser}.
 */
public class BallerinaParserErrorStrategy extends DefaultErrorStrategy {

    private BLangDiagnosticLogHelper dlog;
    protected BDiagnosticSource diagnosticSrc;
    
    public BallerinaParserErrorStrategy(CompilerContext context, BDiagnosticSource diagnosticSrc) {
        this.dlog = BLangDiagnosticLogHelper.getInstance(context);
        this.diagnosticSrc = diagnosticSrc;
    }

    @Override
    public void reportInputMismatch(Parser parser, InputMismatchException e) {
        setErrorState(parser);
        Token offendingToken = e.getOffendingToken();
        String mismatchedToken = getTokenErrorDisplay(offendingToken);
        String expectedToken = e.getExpectedTokens().toString(parser.getVocabulary());
        DiagnosticPos pos = getPosition(offendingToken);
        dlog.error(pos, DiagnosticCode.MISMATCHED_INPUT, mismatchedToken, expectedToken);
    }

    @Override
    public void reportNoViableAlternative(Parser parser, NoViableAltException e) {
        setErrorState(parser);
        String offendingToken = e.getOffendingToken().getText();
        DiagnosticPos pos = getPosition(e.getOffendingToken());
        dlog.error(pos, DiagnosticCode.INVALID_TOKEN, escapeWSAndQuote(offendingToken));
    }

    @Override
    public void reportFailedPredicate(Parser parser, FailedPredicateException e) {
        setErrorState(parser);
        DiagnosticPos pos = getPosition(getMissingSymbol(parser));
        if (parser.getContext() instanceof BallerinaParser.ShiftExprPredicateContext) {
            dlog.error(pos, DiagnosticCode.INVALID_SHIFT_OPERATOR);
        } else if (parser.getContext() instanceof BallerinaParser.RestDescriptorPredicateContext) {
            dlog.error(pos, DiagnosticCode.INVALID_RECORD_REST_DESCRIPTOR);
        } else {
            dlog.error(pos, DiagnosticCode.FAILED_PREDICATE, e.getMessage());
        }
    }

    @Override
    public void reportMissingToken(Parser parser) {
        if (parser.getContext().exception != null || inErrorRecoveryMode(parser)) {
            return;
        }
        beginErrorCondition(parser);
        
        setErrorState(parser);
        Token token = parser.getCurrentToken();
        IntervalSet expecting = getExpectedTokens(parser);
        String missingToken = expecting.toString(parser.getVocabulary());
        DiagnosticPos pos = getPosition(getMissingSymbol(parser));
        dlog.error(pos, DiagnosticCode.MISSING_TOKEN, missingToken, getTokenErrorDisplay(token));
    }

    @Override
    public void reportUnwantedToken(Parser parser) {
        if (parser.getContext().exception != null || inErrorRecoveryMode(parser)) {
            return;
        }
        beginErrorCondition(parser);

        setErrorState(parser);
        Token token = parser.getCurrentToken();
        DiagnosticPos pos = getPosition(getMissingSymbol(parser));
        dlog.error(pos, DiagnosticCode.EXTRANEOUS_INPUT, getTokenErrorDisplay(token));
    }

    public void reportError(Parser parser, RecognitionException e) {
        if (inErrorRecoveryMode(parser)) {
            return;
        }
        beginErrorCondition(parser);

        if (e instanceof NoViableAltException) {
            reportNoViableAlternative(parser, (NoViableAltException) e);
        } else if (e instanceof InputMismatchException) {
            reportInputMismatch(parser, (InputMismatchException) e);
        } else if (e instanceof FailedPredicateException) {
            reportFailedPredicate(parser, (FailedPredicateException) e);
        } else {
            setErrorState(parser);
            DiagnosticPos pos = getPosition(getMissingSymbol(parser));
            dlog.error(pos, DiagnosticCode.INVALID_TOKEN, e.getMessage());
        }
    }

    /**
     * Set the diagnostic source for the error strategy.
     *
     * @param diagnosticSrc     Diagnostic source to be set
     */
    public void setDiagnosticSrc(BDiagnosticSource diagnosticSrc) {
        this.diagnosticSrc = diagnosticSrc;
    }

    /**
     * Set the error state to the listener. This state will be used at the 
     * {@link org.wso2.ballerinalang.compiler.parser.BLangParserListener} 
     * level to determine whether a syntax error has occurred and is in error state.
     * 
     * @param parser Current parser
     */
    protected void setErrorState(Parser parser) {
        getListener(parser).setErrorState();
    }

    protected BLangParserListener getListener(Parser parser) {
        return (BLangParserListener) parser.getParseListeners().get(0);
    }

    private DiagnosticPos getPosition(Token token) {
        int startLine = token.getLine();
        int startCol = token.getCharPositionInLine() + 1;
        int endLine = -1;
        int endCol = -1;
        return new DiagnosticPos(diagnosticSrc, startLine, endLine, startCol, endCol);
    }
}
