/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.expressions.typecast;

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test Cases for type casting.
 */
public class ValueTypeCastExprTest {
    private static final double DELTA = 0.01;
    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/typecast/value-type-casting.bal");
    }

    @Test
    public void testIntToFloat() {
        BValue[] args = {new BInteger(55555555)};
        BValue[] returns = BRunUtil.invoke(result, "intToFloat", args);
        Assert.assertTrue(returns[0] instanceof BFloat);
        double expected = 5.5555555E7;
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), expected, DELTA);
    }

    @Test
    public void testIntToString() {
        BValue[] args = {new BInteger(111)};
        BValue[] returns = BRunUtil.invoke(result, "intToString", args);
        Assert.assertTrue(returns[0] instanceof BString);
        final String expected = "111";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testIntToAny() {
        BValue[] args = {new BInteger(1)};
        BValue[] returns = BRunUtil.invoke(result, "intToAny", args);
        Assert.assertTrue(returns[0] instanceof BInteger);
    }

    @Test
    public void testFloatToInt() {
        BValue[] args = {new BFloat(222222.44444f)};
        BValue[] returns = BRunUtil.invoke(result, "floatToInt", args);
        Assert.assertTrue(returns[0] instanceof BInteger);
        final String expected = "222222";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testFloatToString() {
        BValue[] args = {new BFloat(111.333f)};
        BValue[] returns = BRunUtil.invoke(result, "floatToString", args);
        Assert.assertTrue(returns[0] instanceof BString);
        final String expected = "111.333";
        Assert.assertEquals(returns[0].stringValue().substring(0, 7), expected);
    }

    @Test
    public void testFloatToAny() {
        BValue[] args = {new BFloat(111.333f)};
        BValue[] returns = BRunUtil.invoke(result, "floatToAny", args);
        Assert.assertTrue(returns[0] instanceof BFloat);
    }

    @Test
    public void testStringToInt() {
        BValue[] args = {new BString("100")};
        BValue[] returns = BRunUtil.invoke(result, "stringToInt", args);
        Assert.assertTrue(returns[0] instanceof BInteger);
        final long expected = 100;
        Assert.assertEquals(((BInteger) returns[0]).intValue(), expected);
    }

    @Test
    public void testStringToFloat() {
        BValue[] args = {new BString("2222.333f")};
        BValue[] returns = BRunUtil.invoke(result, "stringToFloat", args);
        Assert.assertTrue(returns[0] instanceof BFloat);
        double expected = 2222.333;
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), expected, DELTA);
    }

    @Test
    public void testStringToAny() {
        BValue[] args = {new BString("adfs sadfasd")};
        BValue[] returns = BRunUtil.invoke(result, "stringToAny", args);
        Assert.assertTrue(returns[0] instanceof BString);
    }

    @Test
    public void testBooleanToString() {
        BValue[] args = {new BBoolean(true)};
        BValue[] returns = BRunUtil.invoke(result, "booleanToString", args);
        Assert.assertTrue(returns[0] instanceof BString);
        final String expected = "true";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testBooleanToAny() {
        BValue[] args = {new BBoolean(true)};
        BValue[] returns = BRunUtil.invoke(result, "booleanToAny", args);
        Assert.assertTrue(returns[0] instanceof BBoolean);
    }

    @Test
    public void testBooleanAppendToString() {
        BValue[] args = {new BBoolean(true)};
        BValue[] returns = BRunUtil.invoke(result, "booleanappendtostring", args);
        Assert.assertTrue(returns[0] instanceof BString);
        final String expected = "true-append-true";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testAnyToInt() {
        BValue[] returns = BRunUtil.invoke(result, "anyToInt", new BValue[]{});
        Assert.assertTrue(returns[0] instanceof BInteger);
        final int expected = 5;
        Assert.assertEquals(((BInteger) returns[0]).intValue(), expected);
    }

    @Test
    public void testAnyToFloat() {
        BValue[] returns = BRunUtil.invoke(result, "anyToFloat", new BValue[]{});
        Assert.assertTrue(returns[0] instanceof BFloat);
        final double expected = 5.0;
        Assert.assertEquals(((BFloat) returns[0]).intValue(), expected, DELTA);
    }

    @Test
    public void testAnyToString() {
        BValue[] returns = BRunUtil.invoke(result, "anyToString", new BValue[]{});
        Assert.assertTrue(returns[0] instanceof BString);
        final String expected = "test";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }

    @Test
    public void testAnyToBoolean() {
        BValue[] returns = BRunUtil.invoke(result, "anyToBoolean", new BValue[]{});
        Assert.assertTrue(returns[0] instanceof BBoolean);
        final boolean expected = false;
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), expected);
    }
}
