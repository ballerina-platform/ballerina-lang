/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerinalang.compiler.parser.test.errorhandler;

import io.ballerina.compiler.internal.diagnostics.DiagnosticErrorCode;
import io.ballerina.compiler.internal.parser.BallerinaParserErrorHandler;
import io.ballerina.compiler.internal.parser.ParserRuleContext;
import io.ballerina.compiler.internal.parser.SyntaxErrors;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;

/**
 * Error handler validation tests.
 *
 * @since 2.0.0
 */
public class ErrorHandlerValidationTest {

    BallerinaParserErrorHandler errorHandler;

    @BeforeTest
    public void setup() {
        errorHandler = new BallerinaParserErrorHandler(null);
    }


    @Test
    public void testAlternativePathEntries()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Method hasAlternativePaths = BallerinaParserErrorHandler.class
                .getDeclaredMethod("hasAlternativePaths", ParserRuleContext.class);
        Method getShortestAlternative = BallerinaParserErrorHandler.class
                .getDeclaredMethod("getShortestAlternative", ParserRuleContext.class);

        hasAlternativePaths.setAccessible(true);
        getShortestAlternative.setAccessible(true);

        for (ParserRuleContext ctx : ParserRuleContext.values()) {
            if ((boolean) hasAlternativePaths.invoke(errorHandler, ctx)) {
                try {
                    getShortestAlternative.invoke(errorHandler, ctx);
                } catch (InvocationTargetException exception) {
                    Assert.fail("Alternative path entry not found");
                }
            }
        }
    }

    @Test
    public void testParseRuleCtxEntries()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Method getExpectedTokenKind = BallerinaParserErrorHandler.class
                .getDeclaredMethod("getExpectedTokenKind", ParserRuleContext.class);
        Method getErrorCode = SyntaxErrors.class.getDeclaredMethod("getErrorCode", ParserRuleContext.class);

        getExpectedTokenKind.setAccessible(true);
        getErrorCode.setAccessible(true);

        for (ParserRuleContext ctx : ParserRuleContext.values()) {
            SyntaxKind syntaxKind = (SyntaxKind) getExpectedTokenKind.invoke(errorHandler, ctx);
            if (syntaxKind != SyntaxKind.NONE) {
                DiagnosticErrorCode errorCode = (DiagnosticErrorCode) getErrorCode.invoke(SyntaxErrors.class, ctx);
                if (DiagnosticErrorCode.ERROR_SYNTAX_ERROR.equals(errorCode) && !skipContextList().contains(ctx)) {
                    Assert.fail("Error code not defined for the context: " + ctx);
                }
            }
        }
    }

    private HashSet<ParserRuleContext> skipContextList() {
        HashSet<ParserRuleContext> hashSet = new HashSet<>();
        // Ideally following cases should not be reached. Therefore no specific error code.
        hashSet.add(ParserRuleContext.EOF);
        hashSet.add(ParserRuleContext.ABSOLUTE_PATH_SINGLE_SLASH); // absolute-resource-path is optional
        return hashSet;
    }
}
