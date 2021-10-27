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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Class to test functionality of modules operator.
 */
public class ModOperationTest {

    CompileResult result;
    CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/binaryoperations/mod-operation.bal");
        resultNegative = BCompileUtil.compile("test-src/expressions/binaryoperations/mod_operation_negative.bal");
    }

    @Test(description = "Test two int mod expression")
    public void testIntMultiplyExpr() {
        intMod(1, 1, 0);
        intMod(10, 4, 2);
        intMod(4, 10, 4);
        intMod(-4, 10, -4);
    }

    @Test(description = "Test two float mod expression")
    public void testFloatDivideExpr() {
        floatMod(1, 1, 0);
        floatMod(10, 4, 2);
        floatMod(4, 10, 4);
        floatMod(-4, 10, -4);
    }

    private void intMod(int val1, int val2, long expected) {
        BValue[] args = { new BInteger(val1), new BInteger(val2) };
        BValue[] returns = BRunUtil.invoke(result, "intMod", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        Assert.assertEquals(actual, expected);
    }

    private void floatMod(float val1, float val2, double expected) {
        BValue[] args = { new BFloat(val1), new BFloat(val2) };
        BValue[] returns = BRunUtil.invoke(result, "floatMod", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);

        double actual = ((BFloat) returns[0]).floatValue();
        Assert.assertEquals(actual, expected);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: \\{ballerina\\}DivisionByZero \\{\"message\":\" / " +
                    "by zero\"\\}.*")
    public void testIntModZero() {
        BRunUtil.invoke(result, "intMod", new BValue[]{new BInteger(2000), new BInteger(0)});
    }

    @Test
    public void testFloatModZero() {
        BValue[] returns = BRunUtil.invoke(result, "floatMod", new BValue[]{new BFloat(200.1), new BFloat(0.0)});
        Assert.assertEquals(returns[0].stringValue(), "NaN");
    }

    @Test(dataProvider = "dataToTestModWithTypes", description = "Test mod with types")
    public void testModWithTypes(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @DataProvider
    public Object[] dataToTestModWithTypes() {
        return new Object[]{
                "testModWithTypes",
                "testModSingleton"
        };
    }

    @Test(description = "Test mod operation negative scenarios")
    public void testModStmtNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 10);
        BAssertUtil.validateError(resultNegative, 0, "operator '%' not defined for 'C' and 'string'", 28, 14);
        BAssertUtil.validateError(resultNegative, 1, "operator '%' not defined for 'C' and '(float|int)'", 29, 14);
        BAssertUtil.validateError(resultNegative, 2, "operator '%' not defined for 'string' and " +
                "'(string|string:Char)'", 30, 17);
        BAssertUtil.validateError(resultNegative, 3, "operator '%' not defined for 'float' and 'decimal'", 37, 14);
        BAssertUtil.validateError(resultNegative, 4, "operator '%' not defined for 'float' and 'decimal'", 38, 14);
        BAssertUtil.validateError(resultNegative, 5, "operator '%' not defined for 'float' and 'int'", 39, 14);
        BAssertUtil.validateError(resultNegative, 6, "operator '%' not defined for 'decimal' and 'int'", 40, 14);
        BAssertUtil.validateError(resultNegative, 7, "operator '%' not defined for 'int' and 'float'", 41, 18);
        BAssertUtil.validateError(resultNegative, 8, "operator '%' not defined for 'C' and 'float'", 45, 14);
        BAssertUtil.validateError(resultNegative, 9, "operator '%' not defined for 'C' and 'float'", 46, 14);
    }

    @Test(description = "Test mod of nullable values")
    public void testModNullable() {
        BRunUtil.invoke(result, "testModNullable");
    }
}
