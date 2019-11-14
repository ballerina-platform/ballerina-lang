/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.parser.antlr4;

import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.FailedPredicateException;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.wso2.ballerinalang.compiler.parser.BLangReferenceParserListener;

/**
 * Class is responsible for issuing warnings when Documentation Reference is in an invalid format
 *
 * @since 1.1.0
 */
public class SilentParserErrorStrategy extends DefaultErrorStrategy {

    @Override
    public void reportInputMismatch(Parser parser, InputMismatchException e) {
        setErrorState(parser);
    }

    @Override
    public void reportNoViableAlternative(Parser parser, NoViableAltException e) {
        setErrorState(parser);
    }

    @Override
    public void reportMissingToken(Parser parser) {
        if (parser.getContext().exception != null || inErrorRecoveryMode(parser)) {
            return;
        }
        beginErrorCondition(parser);

        setErrorState(parser);
    }

    @Override
    public void reportUnwantedToken(Parser parser) {
        if (parser.getContext().exception != null || inErrorRecoveryMode(parser)) {
            return;
        }
        beginErrorCondition(parser);

        setErrorState(parser);
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
        }
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

    protected BLangReferenceParserListener getListener(Parser parser) {
        return (BLangReferenceParserListener) parser.getParseListeners().get(0);
    }
}
