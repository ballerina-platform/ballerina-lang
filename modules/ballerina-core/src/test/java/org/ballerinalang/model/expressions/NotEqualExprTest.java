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
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BDouble;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.SemanticException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Primitive add expression test.
 */
public class NotEqualExprTest {

    private BLangProgram bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("lang/expressions/not-equal-expr.bal");
    }

    @Test(description = "Test two int equality")
    public void testIntNotEqualExpr() {
        BValue[] args = { new BInteger(5), new BInteger(5) };
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "checkIntEquality", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        int actual = ((BInteger) returns[0]).intValue();
        int expected = 1;
        Assert.assertEquals(actual, expected);

        args = new BValue[] { new BInteger(5), new BInteger(6) };
        returns = BLangFunctions.invoke(bLangProgram, "checkIntEquality", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        actual = ((BInteger) returns[0]).intValue();
        expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test two float equality")
    public void testFloatNotEqualExpr() {
        BValue[] args = { new BFloat(5.34f), new BFloat(5.34f) };
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "checkFloatEquality", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        int actual = ((BInteger) returns[0]).intValue();
        int expected = 1;
        Assert.assertEquals(actual, expected);

        args = new BValue[] { new BFloat(8.0001f), new BFloat(6.9992f) };
        returns = BLangFunctions.invoke(bLangProgram, "checkFloatEquality", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        actual = ((BInteger) returns[0]).intValue();
        expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test two boolean equality")
    public void testBooleanNotEqualExpr() {
        BValue[] args = { new BBoolean(true), new BBoolean(true) };
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "checkBooleanEquality", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        int actual = ((BInteger) returns[0]).intValue();
        int expected = 1;
        Assert.assertEquals(actual, expected);

        args = new BValue[] { new BBoolean(true), new BBoolean(false) };
        returns = BLangFunctions.invoke(bLangProgram, "checkBooleanEquality", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        actual = ((BInteger) returns[0]).intValue();
        expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test two string equality")
    public void testStringNotEqualExpr() {
        BValue[] args = { new BString("This is Ballerina !!!"), new BString("This is Ballerina !!!") };
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "checkStringEquality", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        int actual = ((BInteger) returns[0]).intValue();
        int expected = 1;
        Assert.assertEquals(actual, expected);

        args = new BValue[] { new BString("This is Ballerina !!!"),
                              new BString("This is Ballerina !!!. No it's not.") };
        returns = BLangFunctions.invoke(bLangProgram, "checkStringEquality", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        actual = ((BInteger) returns[0]).intValue();
        expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test Float and Double equal expression")
    public void testFloatAndDoubleEqualExpr() {
        float a = 20.2f;
        double b = 20.2d;

        // Should be false since float is a approximation, not an exact number
        boolean expectedResult = (a == b);

        BValue[] args = {new BFloat(a), new BDouble(b)};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "checkFloatAndDoubleEquality", args);


        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);

        boolean actualResult = ((BBoolean) returns[0]).booleanValue();

        Assert.assertEquals(actualResult, expectedResult);
    }
    
    /*
     * Negative tests
     */

    @Test(description = "Test checking equality of two types",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp =
                  "lang/expressions/type/incompatible/eq/incompatible-type-equal-expr.bal:6: "
                          + "invalid operation: incompatible types 'int' and 'boolean'")
    public void testIncompatibleEquality() {
        BTestUtils.parseBalFile("lang/expressions/type/incompatible/eq");
    }
    
    @Test(description = "Test checking equality of unsupported types (json)",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp =
                  "lang/expressions/type/unsupported/eq/unsupported-type-equal-expr.bal:9: "
                          + "invalid operation: operator == not defined on 'json'")
    public void testUnsupportedTypeEquality() {
        BTestUtils.parseBalFile("lang/expressions/type/unsupported/eq");
    }
    
    @Test(description = "Test checking not-equality of unsupported types (json)",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp =
                  "lang/expressions/type/unsupported/neq/unsupported-type-not-equal-expr.bal:9: "
                          + "invalid operation: operator != not defined on 'json'")
    public void testUnsupportedTypeNotEquality() {
        BTestUtils.parseBalFile("lang/expressions/type/unsupported/neq");
    }
}
