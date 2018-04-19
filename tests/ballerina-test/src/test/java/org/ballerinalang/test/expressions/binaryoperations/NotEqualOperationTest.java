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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test functionality of not equal operators.
 */
public class NotEqualOperationTest {

    CompileResult result;
    CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/binaryoperations/not-equal-operation.bal");
        resultNegative = BCompileUtil.compile("test-src/expressions/binaryoperations/not-equal-operation-negative.bal");
    }

    @Test(description = "Test two int equality")
    public void testIntNotEqualExpr() {
        BValue[] args = { new BInteger(5), new BInteger(5) };
        BValue[] returns = BRunUtil.invoke(result, "checkIntEquality", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        long actual = ((BInteger) returns[0]).intValue();
        long expected = 1;
        Assert.assertEquals(actual, expected);

        args = new BValue[] { new BInteger(5), new BInteger(6) };
        returns = BRunUtil.invoke(result, "checkIntEquality", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        actual = ((BInteger) returns[0]).intValue();
        expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test two float equality")
    public void testFloatNotEqualExpr() {
        BValue[] args = { new BFloat(5.34f), new BFloat(5.34f) };
        BValue[] returns = BRunUtil.invoke(result, "checkFloatEquality", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        long actual = ((BInteger) returns[0]).intValue();
        long expected = 1;
        Assert.assertEquals(actual, expected);

        args = new BValue[] { new BFloat(8.0001f), new BFloat(6.9992f) };
        returns = BRunUtil.invoke(result, "checkFloatEquality", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        actual = ((BInteger) returns[0]).intValue();
        expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test two boolean equality")
    public void testBooleanNotEqualExpr() {
        BValue[] args = { new BBoolean(true), new BBoolean(true) };
        BValue[] returns = BRunUtil.invoke(result, "checkBooleanEquality", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        long actual = ((BInteger) returns[0]).intValue();
        long expected = 1;
        Assert.assertEquals(actual, expected);

        args = new BValue[] { new BBoolean(true), new BBoolean(false) };
        returns = BRunUtil.invoke(result, "checkBooleanEquality", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        actual = ((BInteger) returns[0]).intValue();
        expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test two string equality")
    public void testStringNotEqualExpr() {
        BValue[] args = { new BString("This is Ballerina !!!"), new BString("This is Ballerina !!!") };
        BValue[] returns = BRunUtil.invoke(result, "checkStringEquality", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        long actual = ((BInteger) returns[0]).intValue();
        long expected = 1;
        Assert.assertEquals(actual, expected);

        args = new BValue[] { new BString("This is Ballerina !!!"),
                              new BString("This is Ballerina !!!. No it's not.") };
        returns = BRunUtil.invoke(result, "checkStringEquality", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        actual = ((BInteger) returns[0]).intValue();
        expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test not equal expression statement with errors")
    public void testNotEqualStmtNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resultNegative, 0, "operator '==' not defined for 'int' and 'string'", 4, 12);
        //BAssertUtil.validateError(resultNegative, 1, "operator '==' not defined for 'json' and 'json'", 13, 12);
        //BAssertUtil.validateError(resultNegative, 2, "operator '!=' not defined for 'json' and 'json'", 22, 12);
    }
}
