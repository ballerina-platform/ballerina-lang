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
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
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
        Assert.assertEquals(negative.getErrorCount(), 4, negative.toString());
        BAssertUtil.validateError(negative, 0, "invalid usage of the 'checkpanic' expression " +
                "operator: no expression type is equivalent to error type", 11, 30);
        BAssertUtil.validateError(negative, 1, "invalid usage of the 'checkpanic' expression " +
                "operator: all expression types are equivalent to error type", 16, 30);
        BAssertUtil.validateError(negative, 2, "invalid usage of the 'checkpanic' expression " +
                "operator: all expression types are equivalent to error type", 29, 30);
        BAssertUtil.validateError(negative, 3, "incompatible types: expected '(string|error)'" +
                ", found '(string|int)'", 37, 30);
    }
}
