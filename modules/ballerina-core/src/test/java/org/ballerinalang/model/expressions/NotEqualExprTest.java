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
public class NotEqualExprTest {

    private ProgramFile programFile;

    @BeforeClass
    public void setup() {
        programFile = BTestUtils.getProgramFile("lang/expressions/not-equal-expr.bal");
    }

    @Test(description = "Test two int equality")
    public void testIntNotEqualExpr() {
        BValue[] args = { new BInteger(5), new BInteger(5) };
        BValue[] returns = BLangFunctions.invokeNew(programFile, "checkIntEquality", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        long actual = ((BInteger) returns[0]).intValue();
        long expected = 1;
        Assert.assertEquals(actual, expected);

        args = new BValue[] { new BInteger(5), new BInteger(6) };
        returns = BLangFunctions.invokeNew(programFile, "checkIntEquality", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        actual = ((BInteger) returns[0]).intValue();
        expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test two float equality")
    public void testFloatNotEqualExpr() {
        BValue[] args = { new BFloat(5.34f), new BFloat(5.34f) };
        BValue[] returns = BLangFunctions.invokeNew(programFile, "checkFloatEquality", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        long actual = ((BInteger) returns[0]).intValue();
        long expected = 1;
        Assert.assertEquals(actual, expected);

        args = new BValue[] { new BFloat(8.0001f), new BFloat(6.9992f) };
        returns = BLangFunctions.invokeNew(programFile, "checkFloatEquality", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        actual = ((BInteger) returns[0]).intValue();
        expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test two boolean equality")
    public void testBooleanNotEqualExpr() {
        BValue[] args = { new BBoolean(true), new BBoolean(true) };
        BValue[] returns = BLangFunctions.invokeNew(programFile, "checkBooleanEquality", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        long actual = ((BInteger) returns[0]).intValue();
        long expected = 1;
        Assert.assertEquals(actual, expected);

        args = new BValue[] { new BBoolean(true), new BBoolean(false) };
        returns = BLangFunctions.invokeNew(programFile, "checkBooleanEquality", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        actual = ((BInteger) returns[0]).intValue();
        expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test two string equality")
    public void testStringNotEqualExpr() {
        BValue[] args = { new BString("This is Ballerina !!!"), new BString("This is Ballerina !!!") };
        BValue[] returns = BLangFunctions.invokeNew(programFile, "checkStringEquality", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        long actual = ((BInteger) returns[0]).intValue();
        long expected = 1;
        Assert.assertEquals(actual, expected);

        args = new BValue[] { new BString("This is Ballerina !!!"),
                              new BString("This is Ballerina !!!. No it's not.") };
        returns = BLangFunctions.invokeNew(programFile, "checkStringEquality", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        actual = ((BInteger) returns[0]).intValue();
        expected = 2;
        Assert.assertEquals(actual, expected);
    }

    /*
     * Negative tests
     */

    @Test(description = "Test checking equality of two types",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "lang[/\\\\]expressions[/\\\\]btype[/\\\\]incompatible[/\\\\]eq[/\\\\]"
                    + "incompatible-type-equal-expr.bal:6: invalid operation: incompatible types 'int' and 'string'")
    public void testIncompatibleEquality() {
        BTestUtils.getProgramFile("lang/expressions/btype/incompatible/eq");
    }
    
    @Test(description = "Test checking equality of unsupported types (json)",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp =
                  "lang[/\\\\]expressions[/\\\\]btype[/\\\\]unsupported[/\\\\]eq[/\\\\]" +
                  "unsupported-type-equal-expr.bal:9: invalid operation: operator == not defined on 'json'")
    public void testUnsupportedTypeEquality() {
        BTestUtils.getProgramFile("lang/expressions/btype/unsupported/eq");
    }
    
    @Test(description = "Test checking not-equality of unsupported types (json)",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp =
                  "lang[/\\\\]expressions[/\\\\]btype[/\\\\]unsupported[/\\\\]neq[/\\\\]" +
                  "unsupported-type-not-equal-expr.bal:9: invalid operation: operator != not defined on 'json'")
    public void testUnsupportedTypeNotEquality() {
        BTestUtils.getProgramFile("lang/expressions/btype/unsupported/neq");
    }
}
