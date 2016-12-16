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
package org.wso2.ballerina.core.parser;

import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.FailedPredicateException;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.IntervalSet;

/**
 * Error Handling strategy for {@link BallerinaParser}.
 */
public class BallerinaParserErrorStrategy extends DefaultErrorStrategy {
    
    @Override
    public void recover(Parser parser, RecognitionException e) {
        throw new ParserException(e);
    }

    @Override
    public void reportInputMismatch(Parser parser, InputMismatchException e) {
        int line = getMissingSymbol(parser).getLine();
        int position = getMissingSymbol(parser).getCharPositionInLine();
        String mismatchedToken = getTokenErrorDisplay(e.getOffendingToken());
        String expectedToken = e.getExpectedTokens().toString(parser.getVocabulary());
        String msg = "Mismatched input " + mismatchedToken + " in " + parser.getSourceName() + "["+ line + ":" +
                position + "]. Expecting one of {" + expectedToken + "}";
        throw new ParserException(msg);
    }

    @Override
    public void reportMissingToken(Parser parser) {
        Token t = parser.getCurrentToken();
        IntervalSet expecting = getExpectedTokens(parser);
        int line = getMissingSymbol(parser).getLine();
        int position = getMissingSymbol(parser).getCharPositionInLine();
        String missingToken = expecting.toString(parser.getVocabulary());
        String msg = "Missing " + missingToken + " before " + getTokenErrorDisplay(t) + " in " + parser.getSourceName()
                + "["+ line + ":" + position + "]";
        throw new ParserException(msg);
    }
    
    @Override
    public void reportNoViableAlternative(Parser parser, NoViableAltException e) {
        Token t = parser.getCurrentToken();
        int line = getMissingSymbol(parser).getLine();
        int position = getMissingSymbol(parser).getCharPositionInLine();
        String msg = "Invalid identifier " + getTokenErrorDisplay(t) +  " in " + parser.getSourceName() + "["+ line +
                ":" + position + "]";
        throw new ParserException(msg);
    }
    
    @Override
    public void reportUnwantedToken(Parser parser) {
        Token t = parser.getCurrentToken();
        int line = getMissingSymbol(parser).getLine();
        int position = getMissingSymbol(parser).getCharPositionInLine();
        String msg = "Unwanted token " + getTokenErrorDisplay(t) + " in " + parser.getSourceName() + "["+ line + ":" 
                + position + "]";
        throw new ParserException(msg);
    }
    
    @Override
    public void reportFailedPredicate(Parser parser, FailedPredicateException e) {
        int line = getMissingSymbol(parser).getLine();
        int position = getMissingSymbol(parser).getCharPositionInLine();
        throw new ParserException(e.getMessage() + " in " + parser.getSourceName() + "["+ line + ":" + position + "]");
    }
}
