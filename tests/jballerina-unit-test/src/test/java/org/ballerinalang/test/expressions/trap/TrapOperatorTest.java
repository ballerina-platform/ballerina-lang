/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.expressions.trap;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test cases for `trap` unary expression.
 *
 * @since 2.0.0
 */
public class TrapOperatorTest {

    CompileResult result;
    CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/trap/trap-expr.bal");
        resultNegative = BCompileUtil.compile("test-src/expressions/trap/trap_negative.bal");
    }

    @Test
    public void testTrapNegative() {
        int i = 0;
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'int', found '(int|error)'", 18, 13);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'int', found '(int|error)'", 19, 13);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'boolean', found '(boolean|error)'", 25, 8);
        BAssertUtil.validateWarning(resultNegative, i++, "invalid usage of the 'check' expression operator: " +
                        "no expression type is equivalent to error type", 29, 46);
        BAssertUtil.validateWarning(resultNegative, i++, "invalid usage of the 'check' expression operator: " +
                        "no expression type is equivalent to error type", 29, 56);
        Assert.assertEquals(resultNegative.getWarnCount(), 2);
        Assert.assertEquals(resultNegative.getErrorCount(), i - 2);
    }

    @Test(dataProvider = "functionsToTestTrapExpression", description = "Test trap expression")
    public void testTrapExpression(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @DataProvider
    public Object[] functionsToTestTrapExpression() {
        return new Object[]{
                "trapInsideFunctionArg",
                "testTrapInsideForLoop"
        };
    }
}
