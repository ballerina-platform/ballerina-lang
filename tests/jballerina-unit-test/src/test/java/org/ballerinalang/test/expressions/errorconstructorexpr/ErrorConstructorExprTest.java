/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.ballerinalang.test.expressions.errorconstructorexpr;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.ballerinalang.test.BAssertUtil.validateHint;
import static org.ballerinalang.test.BAssertUtil.validateWarning;

/**
 * Test error-constructor-expr.
 *
 * @since 2.0.0
 */
@Ignore
public class ErrorConstructorExprTest {

    private CompileResult result;

    @BeforeClass
    public void setUp() {
        result = BCompileUtil.compile("test-src/expressions/errorconstructorexpr/error-constructor-expr.bal");
        Assert.assertEquals(result.getErrorCount(), 0, result.getDiagnosticResult().diagnostics().toString());
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }

    @Test(dataProvider = "ErrorConstructorExprFunctions")
    public void testErrorConstructorExpr(String funcName) {
        BRunUtil.invoke(result, funcName);
    }

    @DataProvider(name = "ErrorConstructorExprFunctions")
    public Object[][] getTestFunctions() {
        return new Object[][]{
                {"testErrorConstructorExpr1"},
                {"testErrorConstructorExpr2"},
                {"testErrorConstructorExpr3"},
                {"testErrorConstructorExpr4"},
                {"testErrorConstructorExpr5"},
                {"testErrorConstructorExpr6"},
                {"testErrorConstructorExpr8"},
                {"testErrorConstructorExpr9"},
                {"testContextuallyExpectedErrorCtor"},
        };
    }

    @Test
    public void testErrorConstructorExprNegative() {
        CompileResult negativeSemanticResult = BCompileUtil.compile(
                "test-src/expressions/errorconstructorexpr/error-constructor-expr-negative.bal");
        int i = 0;
        validateError(negativeSemanticResult, i++, "missing arg within parenthesis", 20, 21);
        validateError(negativeSemanticResult, i++, "incompatible types: expected 'string', found 'int'", 21, 22);
        validateError(negativeSemanticResult, i++, "incompatible types: expected 'string', found 'error'", 22, 22);
        validateError(negativeSemanticResult, i++, "incompatible types: expected 'error?', found 'string'", 23, 28);
        validateError(negativeSemanticResult, i++, "additional positional arg in error constructor", 24, 40);
        validateError(negativeSemanticResult, i++, "missing arg within parenthesis", 26, 27);
        validateError(negativeSemanticResult, i++, "incompatible types: expected 'string', found 'int'", 27, 28);
        validateError(negativeSemanticResult, i++, "incompatible types: expected 'string', found 'error'", 28, 28);
        validateError(negativeSemanticResult, i++, "incompatible types: expected 'error?', found 'string'", 29, 34);
        validateError(negativeSemanticResult, i++, "additional positional arg in error constructor", 30, 47);
        validateError(negativeSemanticResult, i++, "undefined error type descriptor 'MyError'", 36, 26);
        validateError(negativeSemanticResult, i++, "invalid arg type in error detail field 'c', expected " +
                "'string', found 'int'", 37, 46);
        validateError(negativeSemanticResult, i++, "invalid arg type in error detail field 'j', expected " +
                "'string', found 'int'", 42, 33);
        validateError(negativeSemanticResult, i++,
                "error constructor does not accept additional detail args 'k' when error detail type " +
                        "'record {| int i; string j; anydata...; |}' contains individual field descriptors", 42, 40);
        validateError(negativeSemanticResult, i++, "missing error detail arg for error detail field 'j'", 43, 14);
        validateError(negativeSemanticResult, i++, "cannot infer type of the error from '(ErrorA|ErrorB)'", 50, 20);
        validateError(negativeSemanticResult, i++, "cannot infer type of the error from '(ErrorA|ErrorB)'", 51, 20);
        validateError(negativeSemanticResult, i++, "unknown type 'Blah'", 60, 27);
        validateError(negativeSemanticResult, i++, "cannot create a new error value from 'ErrorU1'", 62, 19);
        validateError(negativeSemanticResult, i++, "cannot create a new error value from 'ErrorU2'", 63, 19);
        validateError(negativeSemanticResult, i++, "undefined error type descriptor 'ErrorU3'", 64, 19);
        validateError(negativeSemanticResult, i++, "incompatible types: expected '(int|string)', found 'error'",
                66, 18);
        validateError(negativeSemanticResult, i++, "compatible type for error constructor expression not " +
                "found in type '(int|string)'", 67, 16);
        Assert.assertEquals(negativeSemanticResult.getErrorCount(), i);
    }

    @Test
    public void testCodeAnalysisNegative() {
        CompileResult negativeResult = BCompileUtil.compile(
                "test-src/expressions/errorconstructorexpr/error-constructor-expr-code-analysis-negative.bal");
        int i = 0;
        validateWarning(negativeResult, i++, "unused variable 'er'", 19, 5);
        validateHint(negativeResult, i++, "unnecessary condition: expression will always evaluate to " +
                "'true'", 19, 37);
        Assert.assertEquals(negativeResult.getDiagnostics().length, i);
    }
}
