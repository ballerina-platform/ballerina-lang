/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.composer.service.workspace.rest.datamodel;

import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.IntervalSet;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class BallerinaComposerErrorStrategy extends DefaultErrorStrategy {

    public static final String EOF = "'<EOF>'";

    public List<SyntaxError> getErrorTokens() {
        return errorTokens;
    }

    public void setErrorTokens(List<SyntaxError> errorTokens) {
        this.errorTokens = errorTokens;
    }

    List<SyntaxError> errorTokens = new ArrayList<SyntaxError>();

    public BallerinaComposerErrorStrategy() {
    }

    @Override
    public void reportInputMismatch(Parser parser, InputMismatchException e) {
        Token missingSymbol = getMissingSymbol(parser);
        int line = missingSymbol.getLine();
        int position = missingSymbol.getCharPositionInLine();
        String mismatchedToken = getTokenErrorDisplay(e.getOffendingToken());
        String expectedToken = e.getExpectedTokens().toString(parser.getVocabulary());
        String msg = "mismatched input " + mismatchedToken + ". Expecting" + " one of " + expectedToken;
        // FixMe: This need to be handled by grammar itself
        if (!EOF.equals(mismatchedToken)) {
            errorTokens.add(createError(line, position, msg));
        }
    }

    @Override
    public void reportMissingToken(Parser parser) {
        Token token = parser.getCurrentToken();
        IntervalSet expecting = getExpectedTokens(parser);
        Token missingSymbol = getMissingSymbol(parser);
        int line = missingSymbol.getLine();
        int position = getMissingSymbol(parser).getCharPositionInLine();
        String missingToken = expecting.toString(parser.getVocabulary());
        String msg = "missing " + missingToken + " before " + getTokenErrorDisplay(token);
        errorTokens.add(createError(line, position, msg));
    }

    @Override
    public void reportNoViableAlternative(Parser parser, NoViableAltException e) {
        Token token = parser.getCurrentToken();
        int line = getMissingSymbol(parser).getLine();
        int position = getMissingSymbol(parser).getCharPositionInLine();
        String msg = "invalid identifier " + getTokenErrorDisplay(token);
        if (!EOF.equals(getTokenErrorDisplay(token))) {
            errorTokens.add(createError(line, position, msg));
        }
    }

    @Override
    public void reportUnwantedToken(Parser parser) {
        Token token = parser.getCurrentToken();
        int line = getMissingSymbol(parser).getLine();
        int position = getMissingSymbol(parser).getCharPositionInLine();
        String msg = "unwanted token " + getTokenErrorDisplay(token);
        if (!EOF.equals(getTokenErrorDisplay(token))) {
            errorTokens.add(createError(line, position, msg));
        }
    }

    private SyntaxError createError(int line, int position, String message) {
        return new SyntaxError(line, position, message);
    }

}
