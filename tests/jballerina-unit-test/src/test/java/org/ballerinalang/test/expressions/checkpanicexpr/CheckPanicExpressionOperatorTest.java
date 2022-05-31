/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.expressions.checkpanicexpr;

import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for `checkpanic` unary expression.
 */
public class CheckPanicExpressionOperatorTest {

    private CompileResult result, negative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/checkpanicexpr/check_panic_expr.bal");
        negative = BCompileUtil.compile("test-src/expressions/checkpanicexpr/check_panic_expr_negative.bal");
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: Generic Error.*")
    public void testSafeAssignmentBasics1() {
        Object[] arg = {(1)};
        Object returns = BRunUtil.invoke(result, "testBasicCheckpanic", arg);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: Generic Error \\{\"fatal\":true," +
                    "\"message\":\"Something Went Wrong.*")
    public void testSafeAssignmentBasics2() {
        Object[] arg = {(2)};
        BRunUtil.invoke(result, "testBasicCheckpanic", arg);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: Generic Error.*")
    public void testSafeAssignmentBasics3() {
        Object[] arg = {(3)};
        BRunUtil.invoke(result, "testBasicCheckpanic", arg);
    }

    @Test
    public void testSafeAssignmentBasics4() {
        Object[] arg = {(4)};
        Object returns = BRunUtil.invoke(result, "testBasicCheckpanic", arg);
        Assert.assertEquals(returns, 2.2);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.array\\}IndexOutOfRange " +
                    "\\{\"message\":\"array index out of range: index: 4, size: 2.*")
    public void testSafeAssignmentBasics5() {
        Object[] arg = {(5)};
        BRunUtil.invoke(result, "testBasicCheckpanic", arg);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: My Error \\{\"code\":12.*")
    public void testSafeAssignmentBasics6() {
        Object[] arg = {(6)};
        BRunUtil.invoke(result, "testBasicCheckpanic", arg);
    }

    @Test
    public void testSemanticErrors() {
        int i = 0;
        BAssertUtil.validateWarning(negative, i++, "invalid usage of the 'checkpanic' expression " +
                "operator: no expression type is equivalent to error type", 6, 30);
        BAssertUtil.validateError(negative, i++, "incompatible types: expected '(string|error)'" +
                ", found '(string|int)'", 19, 30);
        BAssertUtil.validateWarning(negative, i++, "invalid usage of the 'checkpanic' expression " +
                "operator: no expression type is equivalent to error type", 34, 24);
        BAssertUtil.validateWarning(negative, i++, "invalid usage of the 'checkpanic' expression " +
                "operator: no expression type is equivalent to error type", 39, 31);
        BAssertUtil.validateWarning(negative, i++, "invalid usage of the 'checkpanic' expression " +
                "operator: no expression type is equivalent to error type", 39, 48);
        Assert.assertEquals(negative.getErrorCount(), 1);
        Assert.assertEquals(negative.getWarnCount(), i - 1);
    }

    @Test
    public void testCodeAnalysisErrors() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/expressions/checkpanicexpr/check_panic_expr_code_analysis_negative.bal");
        Assert.assertEquals(compileResult.getErrorCount(), 2);
        BAssertUtil.validateError(compileResult, 0, "expression of type 'never' or equivalent to type 'never' " +
                "not allowed here", 18, 16);
        BAssertUtil.validateError(compileResult, 1, "expression of type 'never' or equivalent to type 'never' " +
                "not allowed here", 28, 16);
    }

    @Test
    public void testCheckingExprWithNoErrorType() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/expressions/checkpanicexpr/check_panic_expr_with_no_error_type.bal");

        int i = 0;
        BAssertUtil.validateWarning(compileResult, i++, "invalid usage of the 'checkpanic' expression " +
                "operator: no expression type is equivalent to error type", 23, 19);
        BAssertUtil.validateWarning(compileResult, i++, "invalid usage of the 'checkpanic' expression " +
                "operator: no expression type is equivalent to error type", 37, 29);
        BAssertUtil.validateWarning(compileResult, i++, "invalid usage of the 'checkpanic' expression " +
                "operator: no expression type is equivalent to error type", 41, 29);
        BAssertUtil.validateWarning(compileResult, i++, "invalid usage of the 'checkpanic' expression " +
                "operator: no expression type is equivalent to error type", 45, 29);
        BAssertUtil.validateWarning(compileResult, i++, "invalid usage of the 'checkpanic' expression " +
                "operator: no expression type is equivalent to error type", 49, 29);
        Assert.assertEquals(compileResult.getWarnCount(), i);

        BRunUtil.invoke(compileResult, "testCheckingExprWithNoErrorType");
    }

    @AfterClass
    public void tearDown() {
        result = null;
        negative = null;
    }
}
