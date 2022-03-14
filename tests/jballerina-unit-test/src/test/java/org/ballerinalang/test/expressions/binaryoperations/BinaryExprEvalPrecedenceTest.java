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

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test functionality of binary expression evaluation precedence.
 */
public class BinaryExprEvalPrecedenceTest {

    CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/binaryoperations/binary-expr-precedence.bal");
    }

    @Test(description = "Test binary OR expression with left most expr evaluated to true expression.")
    public void testBinaryOrExprWithLeftMostExprTrue() {
        boolean one = true;
        boolean two = false;
        boolean three = false;

        boolean expectedResult = true;

        Object[] args = {(one), (two), (three)};
        Object returns = BRunUtil.invoke(result, "binaryOrExprWithLeftMostSubExprTrue", args);
        Assert.assertSame(returns.getClass(), Boolean.class);
        boolean actualResult = (boolean) returns;

        Assert.assertEquals(actualResult, expectedResult);
    }

    @Test(description = "Test binary OR expression with left most expr evaluated to false expression.")
    public void testBinaryOrExprWithLeftMostExprFalseNegativeCase() {
        boolean one = false;
        boolean two = false;
        boolean three = false;
        Object[] args = {(one), (two), (three)};
        Object returns = BRunUtil.invoke(result, "binaryOrExprWithLeftMostSubExprTrue", args);
        boolean actualResult = (boolean) returns;
        Assert.assertEquals(actualResult, one);
    }

    @Test(description = "Test binary AND expression with left most expr evaluated to false expression.")
    public void testBinaryAndExprWithLeftMostExprTrue() {
        boolean one = false;
        boolean two = false;
        boolean three = false;

        boolean expectedResult = false;

        Object[] args = {(one), (two), (three)};
        Object returns = BRunUtil.invoke(result, "binaryANDExprWithLeftMostSubExprFalse", args);
        Assert.assertSame(returns.getClass(), Boolean.class);
        boolean actualResult = (boolean) returns;

        Assert.assertEquals(actualResult, expectedResult);
    }

    @Test(description = "Test binary AND expression with left most expr evaluated to true expression.")
    public void testBinaryAndExprWithLeftMostExprFalseNegativeCase() {
        boolean one = true;
        boolean two = false;
        boolean three = false;
        Object[] args = {(one), (two), (three)};
        Object returns = BRunUtil.invoke(result, "binaryANDExprWithLeftMostSubExprFalse", args);
        boolean actualResult = (boolean) returns;
        Assert.assertFalse(actualResult);
    }

    @Test(description = "Test multi binary expression with OR sub expressions inside If condition.")
    public void testMultiBinaryORExpr() {
        boolean one = false;
        boolean two = false;
        boolean three = true;

        long expectedResult = 101;

        Object[] args = {(one), (two), (three)};
        Object returns = BRunUtil.invoke(result, "multiBinaryORExpr", args);
        Assert.assertSame(returns.getClass(), Long.class);
        long actualResult = (long) returns;

        Assert.assertEquals(actualResult, expectedResult);
    }

    @Test(description = "Test multi binary expression with OR sub expressions inside If condition.")
    public void testMultiBinaryORExprNegative() {
        boolean one = false;
        boolean two = false;
        boolean three = false;

        long expectedResult = 201;

        Object[] args = {(one), (two), (three)};
        Object returns = BRunUtil.invoke(result, "multiBinaryORExpr", args);
        Assert.assertSame(returns.getClass(), Long.class);
        long actualResult = (long) returns;

        Assert.assertEquals(actualResult, expectedResult);
    }

    @Test(description = "Test multi binary expression with OR sub expressions inside If condition.")
    public void testMultiBinaryANDExpr() {
        boolean one = true;
        boolean two = true;
        boolean three = true;

        long expectedResult = 101;

        Object[] args = {(one), (two), (three)};
        Object returns = BRunUtil.invoke(result, "multiBinaryANDExpr", args);
        Assert.assertSame(returns.getClass(), Long.class);
        long actualResult = (long) returns;

        Assert.assertEquals(actualResult, expectedResult);
    }

    @Test(description = "Test multi binary expression with OR sub expressions inside If condition.")
    public void testMultiBinaryANDExprNegative() {
        boolean one = true;
        boolean two = false;
        boolean three = false;

        long expectedResult = 201;

        Object[] args = {(one), (two), (three)};
        Object returns = BRunUtil.invoke(result, "multiBinaryANDExpr", args);
        Assert.assertSame(returns.getClass(), Long.class);
        long actualResult = (long) returns;

        Assert.assertEquals(actualResult, expectedResult);
    }
}
