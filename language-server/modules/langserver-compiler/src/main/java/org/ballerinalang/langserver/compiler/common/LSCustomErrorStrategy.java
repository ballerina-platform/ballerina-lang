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

import org.antlr.v4.runtime.FailedPredicateException;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
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
    public void reportFailedPredicate(Parser parser, FailedPredicateException e) {
        setErrorState(parser);
    }
}
