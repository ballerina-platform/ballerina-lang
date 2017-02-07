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
import org.wso2.ballerina.core.exception.SemanticException;
import org.wso2.ballerina.core.model.BallerinaFile;
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
        intPow(1, 1, 1 ^ 1);
        intPow(4, 2, 4 ^ 2);
        intPow(2, 4, 2 ^ 4);
        intPow(-2, 4, -2 ^ 4);
    }

    @Test(description = "Test long pow expression")
    public void testLongDivideExpr() {
        longPow(1, 1, 1 ^ 1);
        longPow(4, 2, 4 ^ 2);
        longPow(2, 4, 2 ^ 4);
        longPow(-2, 4, -2 ^ 4);
    }

    @Test(description = "Test float pow expression",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "pow-float-expr.bal:2: invalid operation: operator \\^ " +
                    "not defined on 'float'")
    public void testFloatDivideExpr() {
        ParserUtils.parseBalFile("lang/expressions/pow-float-expr.bal");
    }

    @Test(description = "Test double pow expression",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "pow-double-expr.bal:2: invalid operation: operator \\^ " +
                    "not defined on 'double'")
    public void testDoubleDivideExpr() {
        ParserUtils.parseBalFile("lang/expressions/pow-double-expr.bal");
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
}
