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
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.SemanticException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Primitive add expression test.
 */
public class AddExprTest {

    private ProgramFile programFile;

    @BeforeClass
    public void setup() {
        programFile = BTestUtils.getProgramFile("lang/expressions/add-expr.bal");
    }

    @Test(description = "Test two int add expression")
    public void testIntAddExpr() {
        BValue[] args = {new BInteger(100), new BInteger(200)};

        BValue[] returns = BLangFunctions.invokeNew(programFile, "intAdd", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        long actual = ((BInteger) returns[0]).intValue();
        long expected = 300;
        Assert.assertEquals(actual, expected);

        returns = BLangFunctions.invokeNew(programFile, "intSubtract", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        actual = ((BInteger) returns[0]).intValue();
        expected = -100;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test two float add expression")
    public void testFloatAddExpr() {
        BValue[] args = {new BFloat(100.0f), new BFloat(200.0f)};

        BValue[] returns = BLangFunctions.invokeNew(programFile, "floatAdd", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        double actual = ((BFloat) returns[0]).floatValue();
        double expected = 300.0f;
        Assert.assertEquals(actual, expected);

        returns = BLangFunctions.invokeNew(programFile, "floatSubtract", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);
        actual = ((BFloat) returns[0]).floatValue();
        expected = -100.0f;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test two string add expression")
    public void testStringAddExpr() {
        BValue[] args = {new BString("WSO2"), new BString(" Inc.")};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "stringAdd", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);

        String actual = returns[0].stringValue();
        String expected = "WSO2 Inc.";
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test adding negative values")
    public void testNegativeValues() {
        int a = -10;
        int b = -20;

        long expectedResult = a + b;

        BValue[] args = {new BInteger(a), new BInteger(b)};

        BValue[] returns = BLangFunctions.invokeNew(programFile, "intAdd", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        long actualResult = ((BInteger) returns[0]).intValue();
        Assert.assertEquals(actualResult, expectedResult);


        // Subtract
        expectedResult = a - b;
        returns = BLangFunctions.invokeNew(programFile, "intSubtract", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        actualResult = ((BInteger) returns[0]).intValue();
        Assert.assertEquals(actualResult, expectedResult);
    }

    @Test(description = "Test two int add expression")
    public void testStringAndIntAddExpr() {
        String a = "test";
        int b = 10;

        String expectedResult = a + b;

        BValue[] args = {new BString(a), new BInteger(b)};

        BValue[] returns = BLangFunctions.invokeNew(programFile, "stringAndIntAdd", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        String actualResult = ((BString) returns[0]).stringValue();
        Assert.assertEquals(actualResult, expectedResult);
    }
    
    
    /*
     * Negative tests
     */

    @Test(description = "Test adding values of two types",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "add-incompatible-types.bal:5: incompatible types: "
                    + "'string' cannot be assigned to 'int'")
    public void testAddIncompatibleTypes() {
        BTestUtils.parseBalFile("lang/expressions/add-incompatible-types.bal");
    }

    @Test(description = "Test adding values of unsupported types (json)",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "add-unsupported-types.bal:10: invalid operation: " +
                    "operator \\+ not defined on 'json'")
    public void testAddUnsupportedTypes() {
        BTestUtils.parseBalFile("lang/expressions/add-unsupported-types.bal");
    }
}
