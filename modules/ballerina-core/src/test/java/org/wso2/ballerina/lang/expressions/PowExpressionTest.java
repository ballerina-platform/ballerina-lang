/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.lang.expressions;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.values.BDouble;
import org.wso2.ballerina.core.model.values.BFloat;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BLong;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.utils.ParserUtils;
import org.wso2.ballerina.lang.util.Functions;

/**
 * Primitive pow expression test.
 */
public class PowExpressionTest {

    private BallerinaFile powExprFile;

    @BeforeClass
    public void setup() {
        powExprFile = ParserUtils.parseBalFile("lang/expressions/pow-expr.bal");
    }

    @Test(description = "Test int pow expression")
    public void testIntMultiplyExpr() {
        intPow(2, 0, (int) Math.pow(2, 0));
        intPow(1, 1, (int) Math.pow(1, 1));
        intPow(4, 2, (int) Math.pow(4, 2));
        intPow(2, 4, (int) Math.pow(2, 4));
        intPow(-2, 4, (int) Math.pow(-2, 4));
    }

    @Test(description = "Test long pow expression")
    public void testLongDivideExpr() {
        longPow(2, 0, (long) Math.pow(2, 0));
        longPow(1, 1, (long) Math.pow(1, 1));
        longPow(4, 2, (long) Math.pow(4, 2));
        longPow(2, 4, (long) Math.pow(2, 4));
        longPow(-2, 4, (long) Math.pow(-2, 4));
    }

    @Test(description = "Test float pow expression")
    public void testFloatDivideExpr() {
        floatPow(2, 0, (float) Math.pow(2, 0));
        floatPow(1, 1, (float) Math.pow(1, 1));
        floatPow(4, 2, (float) Math.pow(4, 2));
        floatPow(2, 4, (float) Math.pow(2, 4));
        floatPow(-2, 4, (float) Math.pow(-2, 4));
    }

    @Test(description = "Test double pow expression")
    public void testDoubleDivideExpr() {
        doublePow(2, 0, Math.pow(2, 0));
        doublePow(1, 1, Math.pow(1, 1));
        doublePow(4, 2, Math.pow(4, 2));
        doublePow(2, 4, Math.pow(2, 4));
        doublePow(-2, 4, Math.pow(-2, 4));
    }

    private void intPow(int val1, int val2, int expected) {
        BValue[] args = { new BInteger(val1), new BInteger(val2) };
        BValue[] returns = Functions.invoke(powExprFile, "intPow", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = ((BInteger) returns[0]).intValue();
        Assert.assertEquals(actual, expected);
    }

    private void longPow(long val1, long val2, long expected) {
        BValue[] args = { new BLong(val1), new BLong(val2) };
        BValue[] returns = Functions.invoke(powExprFile, "longPow", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BLong.class);

        long actual = ((BLong) returns[0]).longValue();
        Assert.assertEquals(actual, expected);
    }

    private void floatPow(float val1, float val2, float expected) {
        BValue[] args = { new BFloat(val1), new BFloat(val2) };
        BValue[] returns = Functions.invoke(powExprFile, "floatPow", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);

        float actual = ((BFloat) returns[0]).floatValue();
        Assert.assertEquals(actual, expected);
    }

    private void doublePow(double val1, double val2, double expected) {
        BValue[] args = { new BDouble(val1), new BDouble(val2) };
        BValue[] returns = Functions.invoke(powExprFile, "doublePow", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BDouble.class);

        double actual = ((BDouble) returns[0]).doubleValue();
        Assert.assertEquals(actual, expected);
    }
}
