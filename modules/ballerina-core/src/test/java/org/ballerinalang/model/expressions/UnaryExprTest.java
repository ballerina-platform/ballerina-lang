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
package org.ballerinalang.model.expressions;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.SemanticException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test Unary expression.
 *
 * @since 0.8.0
 */
public class UnaryExprTest {

    private ProgramFile programFile;


    @BeforeClass
    public void setup() {
        programFile = BTestUtils.getProgramFile("lang/expressions/unary-expr.bal");
    }

    @Test(description = "Test unary negative expression")
    public void integerUnaryExprTest() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "negativeIntTest");

        Assert.assertEquals(returns.length, 2);

        BInteger x = (BInteger) returns[0];
        Assert.assertSame(x.getClass(), BInteger.class, "Invalid class type returned.");
        Assert.assertEquals(x.intValue(), (-5), "Invalid value returned.");

        BInteger y = (BInteger) returns[1];
        Assert.assertSame(y.getClass(), BInteger.class, "Invalid class type returned.");
        Assert.assertEquals(y.intValue(), 5, "Invalid value returned.");
    }

    @Test(description = "Test int positive unary expression")
    public void positiveIntegerUnaryExprTest() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "positiveIntTest");

        Assert.assertEquals(returns.length, 2);

        BInteger x = (BInteger) returns[0];
        Assert.assertSame(x.getClass(), BInteger.class, "Invalid class type returned.");
        Assert.assertEquals(x.intValue(), (+5), "Invalid value returned.");

        BInteger y = (BInteger) returns[1];
        Assert.assertSame(y.getClass(), BInteger.class, "Invalid class type returned.");
        Assert.assertEquals(y.intValue(), +5, "Invalid value returned.");
    }

    @Test(description = "Test float unary negative expression")
    public void floatUnaryExprTest() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "negativeFloatTest");

        Assert.assertEquals(returns.length, 2);

        BFloat x = (BFloat) returns[0];
        Assert.assertSame(x.getClass(), BFloat.class, "Invalid class type returned.");
        Assert.assertEquals(x.floatValue(), -5.0D, "Invalid value returned.");

        BFloat y = (BFloat) returns[1];
        Assert.assertSame(y.getClass(), BFloat.class, "Invalid class type returned.");
        Assert.assertEquals(y.floatValue(), 5.0D, "Invalid value returned.");
    }

    @Test(description = "Test float positive unary expression")
    public void positiveFloatUnaryExprTest() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "positiveFloatTest");

        Assert.assertEquals(returns.length, 2);

        BFloat x = (BFloat) returns[0];
        Assert.assertSame(x.getClass(), BFloat.class, "Invalid class type returned.");
        Assert.assertEquals(x.floatValue(), +5D, "Invalid value returned.");

        BFloat y = (BFloat) returns[1];
        Assert.assertSame(y.getClass(), BFloat.class, "Invalid class type returned.");
        Assert.assertEquals(y.floatValue(), +5D, "Invalid value returned.");
    }

    @Test(description = "Test unary boolean not expression")
    public void booleanUnaryExprTest() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "booleanNotTest");

        Assert.assertEquals(returns.length, 3);

        BBoolean x = (BBoolean) returns[0];
        Assert.assertSame(x.getClass(), BBoolean.class, "Invalid class type returned.");
        Assert.assertEquals(x.booleanValue(), false, "Invalid value returned.");

        BBoolean y = (BBoolean) returns[1];
        Assert.assertSame(y.getClass(), BBoolean.class, "Invalid class type returned.");
        Assert.assertEquals(y.booleanValue(), true, "Invalid value returned.");

        BBoolean z = (BBoolean) returns[2];
        Assert.assertSame(z.getClass(), BBoolean.class, "Invalid class type returned.");
        Assert.assertEquals(z.booleanValue(), true, "Invalid value returned.");
    }

    @Test(description = "Test unary boolean not expression in if else")
    public void unaryExprInIfConditionTest() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "unaryExprInIfConditionTest");

        Assert.assertEquals(returns.length, 1);

        BBoolean x = (BBoolean) returns[0];
        Assert.assertSame(x.getClass(), BBoolean.class, "Invalid class type returned.");
        Assert.assertEquals(x.booleanValue(), true, "Invalid value returned.");
    }

    @Test(description = "Test unary negation expression")
    public void unaryNegationTest() {
        long a = 3;
        long b = 2;

        long expectedResult = a - -b;

        BValue[] args = {new BInteger(a), new BInteger(b)};

        BValue[] returns = BLangFunctions.invokeNew(programFile, "unaryNegationTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class, "Invalid class type returned.");

        long actualResult = ((BInteger) returns[0]).intValue();

        Assert.assertEquals(actualResult, expectedResult);

    }

    @Test(description = "Test unary positive negation expression")
    public void unaryPositiveNegationTest() {
        long a = 3;

        long expectedResult = +-a;

        BValue[] args = {new BInteger(a)};

        BValue[] returns = BLangFunctions.invokeNew(programFile, "unaryPositiveNegationTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class, "Invalid class type returned.");

        long actualResult = ((BInteger) returns[0]).intValue();

        Assert.assertEquals(actualResult, expectedResult);

    }
    
    /* negative Tests */
    
    @Test(description = "Test unary positive for unsupported types (json)",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "unsupported-types-unary-positive.bal:5: invalid operation: " +
                    "operator \\+ not defined on 'json'")
    public void testUnaryPositiveForUnsupportedTypes() {
        BTestUtils.getProgramFile("lang/expressions/unsupported-types-unary-positive.bal");
    }

    @Test(description = "Test unary negative for unsupported types (json)",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "unsupported-types-unary-negative.bal:5: invalid operation: " +
                    "operator - not defined on 'json'")
    public void testUnaryNegativeForUnsupportedTypes() {
        BTestUtils.getProgramFile("lang/expressions/unsupported-types-unary-negative.bal");
    }

    @Test(description = "Test unary not for unsupported types (json)",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "unsupported-types-unary-not.bal:5: invalid operation: " +
                    "operator ! not defined on 'json'")
    public void testUnaryNotForUnsupportedTypes() {
        BTestUtils.getProgramFile("lang/expressions/unsupported-types-unary-not.bal");
    }
}
