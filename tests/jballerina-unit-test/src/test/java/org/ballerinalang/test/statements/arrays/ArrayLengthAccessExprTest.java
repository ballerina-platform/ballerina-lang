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

package org.ballerinalang.test.statements.arrays;

import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Array length access expression test.
 *
 * @since 0.87
 */
public class ArrayLengthAccessExprTest {

    private CompileResult compilerResult;

    @BeforeClass
    public void setup() {
        compilerResult = BCompileUtil.compile("test-src/statements/arrays/array-length-access-expr.bal");
    }

    @Test(description = "Test array length access expression")
    public void testArrayLengthAccessExpr() {
        Object[] args = {(100), (5)};
        Object returns = BRunUtil.invoke(compilerResult, "arrayLengthAccessTestAssignmentCase", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test array length access expression when present in Function invocation statement.")
    public void testArrayLengthAccessExprFunctionInvocationCase() {
        Object[] args = {(100), (5)};
        Object returns = BRunUtil.invoke(compilerResult, "arrayLengthAccessTestFunctionInvocationCase", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test array length access expression when present in Variable definition statement.")
    public void testArrayLengthAccessExprVariableDefinitionCase() {
        Object[] args = {(100), (5)};
        Object returns = BRunUtil.invoke(compilerResult, "arrayLengthAccessTestVariableDefinitionCase", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test array length access expression when present in Array initialization statement.")
    public void testArrayLengthAccessExprArrayInitializationCase() {
        Object[] args = {(100), (5)};
        Object returns = BRunUtil.invoke(compilerResult, "arrayLengthAccessTestArrayInitializerCase", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test array length access expression when present in Map initialization statement.")
    public void testArrayLengthAccessExprMapInitializationCase() {
        Object[] args = {(100), (5)};
        Object returns = BRunUtil.invoke(compilerResult, "arrayLengthAccessTestMapInitializerCase", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test array length access expression when present in Return statement.")
    public void testArrayLengthAccessExprReturnExpressionCase() {
        Object[] args = {(100), (5)};
        Object returns = BRunUtil.invoke(compilerResult, "arrayLengthAccessTestReturnStatementCase", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test array length access expression when present in multi Return statement.")
    public void testArrayLengthAccessExprMultiReturnExpressionCase() {
        Object[] args = {(100), (5)};
        BArray returns = (BArray) BRunUtil.invoke(compilerResult, "arrayLengthAccessTestMultiReturnStatementCase",
                args);

        Assert.assertEquals(returns.size(), 3);
        Assert.assertSame(returns.get(0).getClass(), Long.class);

        long actualFirst = (long) returns.get(0);
        long actualSecond = (long) returns.get(1);
        long actualThird = (long) returns.get(2);

        long expectedFirst = 3;
        long expectedSecond = 1;
        long expectedThird = 2;

        Assert.assertEquals(actualFirst, expectedFirst);
        Assert.assertEquals(actualSecond, expectedSecond);
        Assert.assertEquals(actualThird, expectedThird);
    }

    @Test(description = "Test array length access expression when present in Type cast expression.")
    public void testArrayLengthAccessExprTypeCastExpressionCase() {
        Object[] args = {(100), (5)};
        Object returns = BRunUtil.invoke(compilerResult, "arrayLengthAccessTestTypeCastExpressionCase", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test array length access expression when present in If condition.")
    public void testArrayLengthAccessExprIfConditionExpressionCase() {
        Object[] args = {(100), (5)};
        Object returns = BRunUtil.invoke(compilerResult, "arrayLengthAccessTestIfConditionCase", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test array length access expression when present in Binary expression.")
    public void testArrayLengthAccessExpBinaryExpressionCase() {
        Object[] args = {(100), (5)};
        Object returns = BRunUtil.invoke(compilerResult, "arrayLengthAccessTestBinaryExpressionCase", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test array length access expression when present in Struct field access expression.")
    public void testArrayLengthAccessExpStructFieldAccessCase() {
        Object[] args = {(100), (5)};
        Object returns = BRunUtil.invoke(compilerResult, "arrayLengthAccessTestStructFieldAccessCase", args);

        Assert.assertSame(returns.getClass(), Long.class);

        long actual = (long) returns;
        long expected = 3;
        Assert.assertEquals(actual, expected);
    }

    @AfterClass
    public void tearDown() {
        compilerResult = null;
    }
}
