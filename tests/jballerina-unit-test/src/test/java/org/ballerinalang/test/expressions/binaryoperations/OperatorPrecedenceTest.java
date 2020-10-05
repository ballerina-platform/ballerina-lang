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

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test operator precedence.
 */
public class OperatorPrecedenceTest {

    CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/binaryoperations/operator-precedence.bal");
    }

    @Test
    public void testAddSubPrecedence() {
        addSubPrecedence(10, 5, 1);
        addSubPrecedence(-6, 8, 6);
        addSubPrecedence(-3, -7, 5);
        addSubPrecedence(-9, -10, -4);
        addSubPrecedence(2, -13, 11);
        addSubPrecedence(14, 6, -17);
        addSubPrecedence(0, 2, 5);
    }

    private void addSubPrecedence(int a, int b, int c) {
        long expected = a - b + c;
        BValue[] args = { new BInteger(a), new BInteger(b), new BInteger(c) };

        BValue[] returns = BRunUtil.invoke(result, "addSubPrecedence", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        long actual = ((BInteger) returns[0]).intValue();

        Assert.assertEquals(actual, expected);
    }

    @Test
    public void testAddSubMultPrecedence() {
        addSubMultPrecedence(10, 5, 1, 20, 8, 3);
        addSubMultPrecedence(-6, 9, 12, 35, 10, 78);
        addSubMultPrecedence(5, -39, 54, 65, 13, 10);
        addSubMultPrecedence(25, 3, -6, 9, 7, 10);
        addSubMultPrecedence(7, 4, 6, -3, 8, 40);
        addSubMultPrecedence(13, 12, 74, 65, -10, 37);
        addSubMultPrecedence(0, 12, 74, 65, 10, -37);
        addSubMultPrecedence(-4, 0, -14, 0, -10, -63);
    }

    private void addSubMultPrecedence(int a, int b, int c, int d, int e, int f) {
        long expected = a * b - c + d * e - f;
        BValue[] args = {
                new BInteger(a), new BInteger(b), new BInteger(c), new BInteger(d), new BInteger(e), new BInteger(f)
        };

        BValue[] returns = BRunUtil.invoke(result, "addSubMultPrecedence", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        long actual = ((BInteger) returns[0]).intValue();
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void testMultDivisionPrecedence() {
        // c != 0  , f != 0
        multDivisionPrecedence(10, 5, 7, 0, 8, 3);
        multDivisionPrecedence(18, -3, 6, 74, 4, -40);
        multDivisionPrecedence(0, 9, -7, 41, 9, -40);
        multDivisionPrecedence(0, -9, -7, -41, -9, -40);
        multDivisionPrecedence(80, -1, 2, 3, -4, 5);
        multDivisionPrecedence(2, -13, 28, -36, 74, -9);
        multDivisionPrecedence(54, 96, 45, -36, 74, -9);
        multDivisionPrecedence(93, 81, 3, 74, 0, -9);
    }

    private void multDivisionPrecedence(int a, int b, int c, int d, int e, int f) {
        long expected = a * b / c * d * e / f;
        BValue[] args = {
                new BInteger(a), new BInteger(b), new BInteger(c), new BInteger(d), new BInteger(e), new BInteger(f)
        };

        BValue[] returns = BRunUtil.invoke(result, "multDivisionPrecedence", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        long actual = ((BInteger) returns[0]).intValue();
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void testAddMultPrecedence() {
        addMultPrecedence(5, 6, 7, 8, 9, 10);
        addMultPrecedence(64, -79, 14, -36, 39, -40);
        addMultPrecedence(-15, 86, -67, 38, -79, 20);
        addMultPrecedence(9, 8, 0, 6, -9, 3);
        addMultPrecedence(0, 0, 0, 0, 0, 0);
    }

    private void addMultPrecedence(int a, int b, int c, int d, int e, int f) {
        long expected = a * b * c + d * e + f;
        BValue[] args = {
                new BInteger(a), new BInteger(b), new BInteger(c), new BInteger(d), new BInteger(e), new BInteger(f)
        };

        BValue[] returns = BRunUtil.invoke(result, "addMultPrecedence", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        long actual = ((BInteger) returns[0]).intValue();
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void testAddDivisionPrecedence() {
        // b != 0, c != 0, e != 0
        addDivisionPrecedence(5, 6, 7, 8, 9, 10);
        addDivisionPrecedence(64, -79, 14, -36, 39, -40);
        addDivisionPrecedence(-15, 86, -67, 38, -79, 20);
    }

    private void addDivisionPrecedence(int a, int b, int c, int d, int e, int f) {
        long expected = a / b / c + d / e + f;
        BValue[] args = {
                new BInteger(a), new BInteger(b), new BInteger(c), new BInteger(d), new BInteger(e), new BInteger(f)
        };

        BValue[] returns = BRunUtil.invoke(result, "addDivisionPrecedence", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        long actual = ((BInteger) returns[0]).intValue();
        Assert.assertEquals(actual, expected);
    }

    @Test
    public void testComparatorPrecedence() {

        // false && true || false
        comparatorPrecedence(10, 20, 30, 40, 50, 60);

        // true && true || false
        comparatorPrecedence(0, 5, 5, 9, 34, 80);

        // true && true || true
        comparatorPrecedence(0, 5, 5, 9, 34, 6);
    }

    private void comparatorPrecedence(int a, int b, int c, int d, int e, int f) {
        boolean expected = (a > b) && (c < d) || (e > f);

        BValue[] args = {
                new BInteger(a), new BInteger(b), new BInteger(c), new BInteger(d), new BInteger(e), new BInteger(f)
        };

        BValue[] returns = BRunUtil.invoke(result, "comparatorPrecedence", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        boolean actual = ((BBoolean) returns[0]).booleanValue();
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test if addition and subtraction takes same precedence")
    public void testAdditionAndSubtractionPrecedence() {

        int a = 10;
        int b = 20;
        int c = 30;
        int d = 40;

        long expectedResult = a - b + c - d;

        BValue[] args = {new BInteger(a), new BInteger(b), new BInteger(c), new BInteger(d)};

        BValue[] returns = BRunUtil.invoke(result, "intAdditionAndSubtractionPrecedence", args);

        long actualResult = ((BInteger) returns[0]).intValue();

        Assert.assertEquals(actualResult, expectedResult, "The results of addition and " +
                "subtraction operation differ");
    }

    @Test(description = "Test if multiplication takes precedence over addition and substraction")
    public void testMultiplicationPrecedence() {

        int a = 10;
        int b = 20;
        int c = 30;
        int d = 40;

        int x = (a + b) * (c * d) + a * b;
        int y = (a + b) * (c * d) - a * b;
        int z = a + b * c * d - a * b;

        long expectedResult = x + y - z;
        BValue[] args = {new BInteger(a), new BInteger(b), new BInteger(c), new BInteger(d)};

        BValue[] returns = BRunUtil.invoke(result, "intMultiplicationPrecedence", args);

        long actualResult = ((BInteger) returns[0]).intValue();

        Assert.assertEquals(actualResult, expectedResult, "The results of multiplication operation differ");
    }

    @Test(description = "Test if division takes precedence over addition and subtraction")
    public void testDivisionPrecedence() {

        int a = 10;
        int b = 20;
        int c = 30;
        int d = 40;

        int x = (a + b) / (c + d) + a / b;
        int y = (a + b) / (c - d) - a / b;
        int z = a + b / c - d - a / b;

        long expectedResult = x - y + z;

        BValue[] args = {new BInteger(a), new BInteger(b), new BInteger(c), new BInteger(d)};

        BValue[] returns = BRunUtil.invoke(result, "intDivisionPrecedence", args);

        long actualResult = ((BInteger) returns[0]).intValue();

        Assert.assertEquals(actualResult, expectedResult, "The results of division operation differ");
    }


    @Test(description = "Test if division and multiplication takes same precedence over addition and subtraction")
    public void testDivisionAndMultiplicationPrecedence() {

        int a = 10;
        int b = 20;
        int c = 30;
        int d = 40;

        int x = (a + b) * (c + d) + a / b;
        int y = (a + b) / (c - d) - a * b;
        int z = a + b / c - d + a * b;

        long expectedResult = x + y + z;

        BValue[] args = {new BInteger(a), new BInteger(b), new BInteger(c), new BInteger(d)};

        BValue[] returns = BRunUtil.invoke(result, "intMultiplicationAndDivisionPrecedence", args);

        long actualResult = ((BInteger) returns[0]).intValue();

        Assert.assertEquals(actualResult, expectedResult, "The results of multiplication and " +
                "division operation differ");
    }
}
