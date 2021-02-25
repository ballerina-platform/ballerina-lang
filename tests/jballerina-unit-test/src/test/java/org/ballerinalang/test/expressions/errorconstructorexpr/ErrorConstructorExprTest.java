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

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test error-constructor-expr.
 *
 * @since 2.0.0
 */
public class ErrorConstructorExprTest {

    private CompileResult result, negativeSemanticResult, negativeResult;

    @BeforeClass
    public void setUp() {
        result = BCompileUtil.compile("test-src/expressions/errorconstructorexpr/error-constructor-expr.bal");
        negativeSemanticResult = BCompileUtil.compile("test-src/expressions/errorconstructorexpr/error-constructor" +
                "-expr-negative.bal");
        negativeResult = BCompileUtil.compile("test-src/expressions/errorconstructorexpr/error-constructor-expr-code" +
                "-analysis-negative.bal");
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
                {"testErrorConstructorExpr7"},
                {"testErrorConstructorExpr8"},
                {"testErrorConstructorExpr9"},
        };
    }

    @Test
    public void testErrorConstructorExprNegative() {
        int i = 0;
        BAssertUtil.validateError(negativeSemanticResult, i++, "missing arg within parenthesis", 20, 21);
        BAssertUtil.validateError(negativeSemanticResult, i++, "incompatible types: expected 'string', found 'int'",
                21, 22);
        BAssertUtil.validateError(negativeSemanticResult, i++, "incompatible types: expected 'string', found 'error'"
                , 22, 22);
        BAssertUtil.validateError(negativeSemanticResult, i++, "incompatible types: expected 'error?', found " +
                "'string'", 23, 28);
        BAssertUtil.validateError(negativeSemanticResult, i++, "additional positional arg in error constructor", 24,
                27);
        BAssertUtil.validateError(negativeSemanticResult, i++, "missing arg within parenthesis", 26, 27);
        BAssertUtil.validateError(negativeSemanticResult, i++, "incompatible types: expected 'string', found 'int'",
                27, 28);
        BAssertUtil.validateError(negativeSemanticResult, i++, "incompatible types: expected 'string', found 'error'"
                , 28, 28);
        BAssertUtil.validateError(negativeSemanticResult, i++, "incompatible types: expected 'error?', found " +
                "'string'", 29, 34);
        BAssertUtil.validateError(negativeSemanticResult, i++, "additional positional arg in error constructor", 30,
                34);
        BAssertUtil.validateError(negativeSemanticResult, i++, "undefined error type descriptor 'MyError'", 36, 20);
        BAssertUtil.validateError(negativeSemanticResult, i++, "invalid arg type in error detail field 'c', expected " +
                "'string', found 'int'", 37, 46);
        Assert.assertEquals(negativeSemanticResult.getErrorCount(), i);
    }

    @Test
    public void testCodeAnalysisNegative() {
        BAssertUtil.validateError(negativeResult, 0, "unnecessary condition: expression will always evaluate to " +
                "'true'", 19, 37);
    }
}
