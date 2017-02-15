/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.ballerina.lang.expressions;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.exception.SemanticException;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.values.BDouble;
import org.wso2.ballerina.core.model.values.BFloat;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BLong;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.utils.BTestUtils;
import org.ballerinalang.util.program.BLangFunctions;

/**
 * Primitive divide expression test.
 */
public class DivideExprTest {

    private BallerinaFile bFile;

    @BeforeClass
    public void setup() {
        bFile = BTestUtils.parseBalFile("lang/expressions/divide-expr.bal");
    }

    @Test(description = "Test two int divide expression")
    public void testIntDivideExpr() {
        BValue[] args = { new BInteger(2000), new BInteger(50) };
        BValue[] returns = BLangFunctions.invoke(bFile, "intDivide", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = ((BInteger) returns[0]).intValue();
        int expected = 40;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test two int divide expression", expectedExceptions = BallerinaException.class)
    public void testIntDivideByZeroExpr() {
        BValue[] args = { new BInteger(2000), new BInteger(0) };
        BLangFunctions.invoke(bFile, "intDivide", args);
    }

    @Test(description = "Test two long divide expression")
    public void testLongDivideExpr() {
        long a = Long.MAX_VALUE;
        long b = 123456789L;

        long expectedResult = a / b;

        BValue[] args = { new BLong(a), new BLong(b) };
        BValue[] returns = BLangFunctions.invoke(bFile, "longDivide", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BLong.class);

        long actual = ((BLong) returns[0]).longValue();
        Assert.assertEquals(actual, expectedResult);
    }

    @Test(description = "Test two float divide expression")
    public void testFloatDivideExpr() {
        float a = 8.5f;
        float b = 4.1f;

        float expectedResult = a / b;

        BValue[] args = { new BFloat(a), new BFloat(b) };
        BValue[] returns = BLangFunctions.invoke(bFile, "floatDivide", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);

        float actual = ((BFloat) returns[0]).floatValue();
        Assert.assertEquals(actual, expectedResult);
    }

    @Test(description = "Test two float divide expression", expectedExceptions = BallerinaException.class)
    public void testFloatDivideByZeroExpr() {
        BValue[] args = { new BFloat(300.0f), new BFloat(0) };
        BLangFunctions.invoke(bFile, "floatDivide", args);
    }

    @Test(description = "Test two double divide expression")
    public void testDoubleDivideExpr() {
        double a = 8.5;
        double b = 4.1;

        double expectedResult = a / b;

        BValue[] args = { new BDouble(a), new BDouble(b) };
        BValue[] returns = BLangFunctions.invoke(bFile, "doubleDivide", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BDouble.class);

        double actual = ((BDouble) returns[0]).doubleValue();
        Assert.assertEquals(actual, expectedResult);
    }

    @Test(description = "Test integer division by long")
    public void testIntDivideByLong() {
        int a = Integer.MAX_VALUE;
        long b = 123456789L;

        long expectedResult = a / b;

        BValue[] args = { new BInteger(a), new BLong(b) };
        BValue[] returns = BLangFunctions.invoke(bFile, "intDivideByLong", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BLong.class, "Return type of the division is invalid");

        long actualResult = ((BLong) returns[0]).longValue();
        Assert.assertEquals(actualResult, expectedResult, "Result of the division operation is incorrect");
    }

    @Test(description = "Test integer division by float")
    public void testIntDivideByFloat() {
        int a = Integer.MAX_VALUE;
        float b = 1.23456789f;

        float expectedResult = a / b;

        BValue[] args = { new BInteger(a), new BFloat(b) };
        BValue[] returns = BLangFunctions.invoke(bFile, "intDivideByFloat", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class, "Return type of the division is invalid");

        float actualResult = ((BFloat) returns[0]).floatValue();
        Assert.assertEquals(actualResult, expectedResult, "Result of the division operation is incorrect");
    }

    @Test(description = "Test integer division by double")
    public void testIntDivideByDouble() {
        int a = Integer.MAX_VALUE;
        double b = 1.23456789d;

        double expectedResult = a / b;

        BValue[] args = { new BInteger(a), new BDouble(b) };
        BValue[] returns = BLangFunctions.invoke(bFile, "intDivideByDouble", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BDouble.class, "Return type of the division is invalid");

        double actualResult = ((BDouble) returns[0]).doubleValue();
        Assert.assertEquals(actualResult, expectedResult, "Result of the division operation is incorrect");
    }

    @Test(description = "Test long number division by int")
    public void testLongDivideByInt() {
        long a = Long.MAX_VALUE;
        int b = 123456789;

        long expectedResult = a / b;

        BValue[] args = { new BLong(a), new BInteger(b) };
        BValue[] returns = BLangFunctions.invoke(bFile, "longDivideByInt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BLong.class, "Return type of the division is invalid");

        long actualResult = ((BLong) returns[0]).longValue();
        Assert.assertEquals(actualResult, expectedResult, "Result of the division operation is incorrect");
    }

    @Test(description = "Test long number division by float")
    public void testLongDivideByFloat() {
        long a = Long.MAX_VALUE;
        float b = 1.23456789f;

        float expectedResult = a / b;

        BValue[] args = { new BLong(a), new BFloat(b) };
        BValue[] returns = BLangFunctions.invoke(bFile, "longDivideByFloat", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class, "Return type of the division is invalid");

        float actualResult = ((BFloat) returns[0]).floatValue();
        Assert.assertEquals(actualResult, expectedResult, "Result of the division operation is incorrect");
    }

    @Test(description = "Test long number division by double")
    public void testLongDivideByDouble() {
        long a = Long.MAX_VALUE;
        double b = 1.23456789d;

        double expectedResult = a / b;

        BValue[] args = { new BLong(a), new BDouble(b) };
        BValue[] returns = BLangFunctions.invoke(bFile, "longDivideByDouble", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BDouble.class, "Return type of the division is invalid");

        double actualResult = ((BDouble) returns[0]).doubleValue();
        Assert.assertEquals(actualResult, expectedResult, "Result of the division operation is incorrect");
    }

    @Test(description = "Test float number division by integer")
    public void testFloatDivideByInt() {
        float a = Float.MAX_VALUE;
        int b = 123456789;

        float expectedResult = a / b;

        BValue[] args = { new BFloat(a), new BInteger(b) };
        BValue[] returns = BLangFunctions.invoke(bFile, "floatDivideByInt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class, "Return type of the division is invalid");

        float actualResult = ((BFloat) returns[0]).floatValue();
        Assert.assertEquals(actualResult, expectedResult, "Result of the division operation is incorrect");
    }

    @Test(description = "Test float number division by long value")
    public void testFloatDivideByLong() {
        float a = Float.MAX_VALUE;
        long b = 123456789L;

        float expectedResult = a / b;

        BValue[] args = { new BFloat(a), new BLong(b) };
        BValue[] returns = BLangFunctions.invoke(bFile, "floatDivideByLong", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class, "Return type of the division is invalid");

        float actualResult = ((BFloat) returns[0]).floatValue();
        Assert.assertEquals(actualResult, expectedResult, "Result of the division operation is incorrect");
    }

    @Test(description = "Test float number division by double value")
    public void testFloatDivideByDouble() {
        float a = Float.MAX_VALUE;
        double b = 1.23456789d;

        double expectedResult = a / b;

        BValue[] args = { new BFloat(a), new BDouble(b) };
        BValue[] returns = BLangFunctions.invoke(bFile, "floatDivideByDouble", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BDouble.class, "Return type of the division is invalid");

        double actualResult = ((BDouble) returns[0]).doubleValue();
        Assert.assertEquals(actualResult, expectedResult, "Result of the division operation is incorrect");
    }

    @Test(description = "Test double number division by integer")
    public void testDoubleDividedByInt() {
        double a = Double.MAX_VALUE;
        int b = 123456789;

        double expectedResult = a / b;

        BValue[] args = { new BDouble(a), new BInteger(b) };
        BValue[] returns = BLangFunctions.invoke(bFile, "doubleDividedByInt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BDouble.class, "Return type of the division is invalid");

        double actualResult = ((BDouble) returns[0]).doubleValue();
        Assert.assertEquals(actualResult, expectedResult, "Result of the division operation is incorrect");
    }

    @Test(description = "Test double number division by long value")
    public void testDoubleDividedByLong() {
        double a = Double.MAX_VALUE;
        long b = 123456789L;

        double expectedResult = a / b;

        BValue[] args = { new BDouble(a), new BLong(b) };
        BValue[] returns = BLangFunctions.invoke(bFile, "doubleDividedByLong", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BDouble.class, "Return type of the division is invalid");

        double actualResult = ((BDouble) returns[0]).doubleValue();
        Assert.assertEquals(actualResult, expectedResult, "Result of the division operation is incorrect");
    }

    @Test(description = "Test double number division by long value")
    public void testDoubleDividedByFloat() {
        double a = Double.MAX_VALUE;
        float b = 1.23456789f;

        double expectedResult = a / b;

        BValue[] args = { new BDouble(a), new BFloat(b) };
        BValue[] returns = BLangFunctions.invoke(bFile, "doubleDividedByFloat", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BDouble.class, "Return type of the division is invalid");

        double actualResult = ((BDouble) returns[0]).doubleValue();
        Assert.assertEquals(actualResult, expectedResult, "Result of the division operation is incorrect");
    }
    
    /*
     * Negative tests
     */
    
    @Test(description = "Test dividing values of two types",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "divide-incompatible-types.bal:5: invalid operation: " +
                    "incompatible types 'string' and 'float'")
    public void testAddIncompatibleTypes() {
        BTestUtils.parseBalFile("lang/expressions/divide-incompatible-types.bal");
    }
    
    @Test(description = "Test dividing values of unsupported types (json)",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "divide-unsupported-types.bal:10: invalid operation: " +
                    "operator / not defined on 'json'")
    public void testAddUnsupportedTypes() {
        BTestUtils.parseBalFile("lang/expressions/divide-unsupported-types.bal");
    }
}
