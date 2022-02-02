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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.JvmRunUtil;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
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
        Object[] args = {(55555555)};
        Object returns = JvmRunUtil.invoke(result, "intToFloat", args);
        Assert.assertTrue(returns instanceof Double);
        double expected = 5.5555555E7;
        Assert.assertEquals((Double) returns, expected, DELTA);
    }

    @Test
    public void testIntToString() {
        Object[] args = {(111)};
        Object returns = JvmRunUtil.invoke(result, "intToString", args);
        Assert.assertTrue(returns instanceof BString);
        final String expected = "111";
        Assert.assertEquals(returns.toString(), expected);
    }

    @Test
    public void testIntToAny() {
        Object[] args = {(1)};
        Object returns = JvmRunUtil.invoke(result, "intToAny", args);
        Assert.assertTrue(returns instanceof Long);
    }

    @Test
    public void testFloatToInt() {
        Object[] args = {(222222.44444f)};
        Object returns = JvmRunUtil.invoke(result, "floatToInt", args);
        Assert.assertTrue(returns instanceof Long);
        final String expected = "222222";
        Assert.assertEquals(returns.toString(), expected);
    }

    @Test
    public void testFloatToString() {
        Object[] args = {(111.333f)};
        Object returns = JvmRunUtil.invoke(result, "floatToString", args);
        Assert.assertTrue(returns instanceof BString);
        final String expected = "111.333";
        Assert.assertEquals(returns.toString().substring(0, 7), expected);
    }

    @Test
    public void testFloatToAny() {
        Object[] args = {(111.333f)};
        Object returns = JvmRunUtil.invoke(result, "floatToAny", args);
        Assert.assertTrue(returns instanceof Double);
    }

    @Test
    public void testStringToInt() {
        Object[] args = {StringUtils.fromString("100")};
        Object returns = JvmRunUtil.invoke(result, "stringToInt", args);
        Assert.assertTrue(returns instanceof Long);
        final long expected = 100;
        Assert.assertEquals(returns, expected);
    }

    @Test
    public void testIncompatibleStringToFloat() {
        Object[] args = {StringUtils.fromString("2222.333f")};
        Object returns = JvmRunUtil.invoke(result, "stringToFloat", args);
        Assert.assertTrue(returns instanceof BError);
        BError error = (BError) returns;
        String errorMsg = ((BMap) error.getDetails()).get(StringUtils.fromString("message")).toString();
        Assert.assertEquals(errorMsg, "'string' value '2222.333f' cannot be converted to 'float'");
    }

    @Test
    public void testStringToAny() {
        Object[] args = {StringUtils.fromString("adfs sadfasd")};
        Object returns = JvmRunUtil.invoke(result, "stringToAny", args);
        Assert.assertTrue(returns instanceof BString);
    }

    @Test
    public void testBooleanToString() {
        Object[] args = {(true)};
        Object returns = JvmRunUtil.invoke(result, "booleanToString", args);
        Assert.assertTrue(returns instanceof BString);
        final String expected = "true";
        Assert.assertEquals(returns.toString(), expected);
    }

    @Test
    public void testBooleanToAny() {
        Object[] args = {(true)};
        Object returns = JvmRunUtil.invoke(result, "booleanToAny", args);
        Assert.assertTrue(returns instanceof Boolean);
    }

    @Test
    public void testBooleanAppendToString() {
        Object[] args = {(true)};
        Object returns = JvmRunUtil.invoke(result, "booleanappendtostring", args);
        Assert.assertTrue(returns instanceof BString);
        final String expected = "true-append-true";
        Assert.assertEquals(returns.toString(), expected);
    }

    @Test
    public void testAnyToInt() {
        Object returns = JvmRunUtil.invoke(result, "anyToInt", new Object[]{});
        Assert.assertTrue(returns instanceof Long);
        final long expected = 5;
        Assert.assertEquals(returns, expected);
    }

    @Test
    public void testAnyToFloat() {
        Object returns = JvmRunUtil.invoke(result, "anyToFloat", new Object[]{});
        Assert.assertTrue(returns instanceof Double);
        final double expected = 5.0;
        Assert.assertEquals((Double) returns, expected, DELTA);
    }

    @Test
    public void testAnyToString() {
        Object returns = JvmRunUtil.invoke(result, "anyToString", new Object[]{});
        Assert.assertTrue(returns instanceof BString);
        final String expected = "test";
        Assert.assertEquals(returns.toString(), expected);
    }

    @Test
    public void testAnyToBoolean() {
        Object returns = JvmRunUtil.invoke(result, "anyToBoolean", new Object[]{});
        Assert.assertTrue(returns instanceof Boolean);
        final boolean expected = false;
        Assert.assertEquals(returns, expected);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
