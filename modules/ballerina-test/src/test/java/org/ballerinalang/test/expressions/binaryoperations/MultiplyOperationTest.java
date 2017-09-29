/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.expressions.binaryoperations;

import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class MultiplyOperationTest {

    CompileResult result;
    CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        result = BTestUtils.compile("test-src/expressions/binaryoperations/multiply-operation.bal");
        resultNegative = BTestUtils.compile("test-src/expressions/binaryoperations/multiply-operation-negative.bal");
    }

    @Test(description = "Test two int multiply expression")
    public void testIntMultiplyExpr() {
        BValue[] args = { new BInteger(100), new BInteger(50) };
        BValue[] returns = BTestUtils.invoke(result, "intMultiply", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 5000;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test two float multiply expression")
    public void testFloatMultiplyExpr() {
        BValue[] args = { new BFloat(40.0f), new BFloat(40.0f) };
        BValue[] returns = BTestUtils.invoke(result, "floatMultiply", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);

        double actual = ((BFloat) returns[0]).floatValue();
        double expected = 1600.0f;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test binary statement with errors")
    public void testSubtractStmtNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 2);
        BTestUtils.validateError(resultNegative, 0, "operator '*' not defined for 'json' and 'json'", 8, 9);
        BTestUtils.validateError(resultNegative, 1, "operator '*' not defined for 'float' and 'string'", 14, 8);
    }
}
