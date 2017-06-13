/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.model.expressions;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Array length access expression test.
 *
 * @since 0.87
 */
public class ArrayLengthAccessExprTest {

    private ProgramFile bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.getProgramFile("lang/expressions/array-length-access-expr.bal");
    }

    @Test(description = "Test array length access expression")
    public void testArrayLengthAccessExpr() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "arrayLengthAccessTestAssignmentCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test array length access expression when present in Function invocation statement.")
    public void testArrayLengthAccessExprFunctionInvocationCase() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "arrayLengthAccessTestFunctionInvocationCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test array length access expression when present in Variable definition statement.")
    public void testArrayLengthAccessExprVariableDefinitionCase() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "arrayLengthAccessTestVariableDefinitionCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test array length access expression when present in Array initialization statement.")
    public void testArrayLengthAccessExprArrayInitializationCase() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "arrayLengthAccessTestArrayInitializerCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test array length access expression when present in Map initialization statement.")
    public void testArrayLengthAccessExprMapInitializationCase() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "arrayLengthAccessTestMapInitializerCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test array length access expression when present in Return statement.")
    public void testArrayLengthAccessExprReturnExpressionCase() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "arrayLengthAccessTestReturnStatementCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test array length access expression when present in multi Return statement.")
    public void testArrayLengthAccessExprMultiReturnExpressionCase() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram,
                "arrayLengthAccessTestMultiReturnStatementCase", args);

        Assert.assertEquals(returns.length, 3);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actualFirst = (int) ((BInteger) returns[0]).intValue();
        int actualSecond = (int) ((BInteger) returns[1]).intValue();
        int actualThird = (int) ((BInteger) returns[2]).intValue();

        int expectedFirst = 3;
        int expectedSecond = 1;
        int expectedThird = 2;

        Assert.assertEquals(actualFirst, expectedFirst);
        Assert.assertEquals(actualSecond, expectedSecond);
        Assert.assertEquals(actualThird, expectedThird);
    }

    @Test(description = "Test array length access expression when present in Type cast expression.")
    public void testArrayLengthAccessExprTypeCastExpressionCase() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "arrayLengthAccessTestTypeCastExpressionCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test array length access expression when present in If condition.")
    public void testArrayLengthAccessExprIfConditionExpressionCase() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "arrayLengthAccessTestIfConditionCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test array length access expression when present in Binary expression.")
    public void testArrayLengthAccessExpBinaryExpressionCase() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "arrayLengthAccessTestBinaryExpressionCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test array length access expression when present in Struct field access expression.")
    public void testArrayLengthAccessExpStructFieldAccessCase() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BLangFunctions.invokeNew(bLangProgram, "arrayLengthAccessTestStructFieldAccessCase", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 3;
        Assert.assertEquals(actual, expected);
    }
}
