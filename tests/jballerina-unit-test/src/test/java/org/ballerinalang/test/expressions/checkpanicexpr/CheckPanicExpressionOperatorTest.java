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

import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
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
        BValue[] arg = {new BInteger(1)};
        BValue[] returns = BRunUtil.invoke(result, "testBasicCheckpanic", arg);
        Assert.assertEquals(returns.length, 1);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: Generic Error \\{\"fatal\":true," +
                    "\"message\":\"Something Went Wrong.*")
    public void testSafeAssignmentBasics2() {
        BValue[] arg = {new BInteger(2)};
        BRunUtil.invoke(result, "testBasicCheckpanic", arg);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: Generic Error.*")
    public void testSafeAssignmentBasics3() {
        BValue[] arg = {new BInteger(3)};
        BRunUtil.invoke(result, "testBasicCheckpanic", arg);
    }

    @Test
    public void testSafeAssignmentBasics4() {
        BValue[] arg = {new BInteger(4)};
        BValue[] returns = BRunUtil.invoke(result, "testBasicCheckpanic", arg);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 2.2);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.array\\}IndexOutOfRange " +
                    "\\{\"message\":\"array index out of range: index: 4, size: 2.*")
    public void testSafeAssignmentBasics5() {
        BValue[] arg = {new BInteger(5)};
        BRunUtil.invoke(result, "testBasicCheckpanic", arg);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: My Error \\{\"code\":12.*")
    public void testSafeAssignmentBasics6() {
        BValue[] arg = {new BInteger(6)};
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
}
