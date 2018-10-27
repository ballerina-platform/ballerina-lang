/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.expressions.builtinfunctions;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This class tests the freeze() and isFrozen() builtin functions.
 *
 * @since 0.985.0
 */
public class FreezeAndIsFrozenTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/builtinfunctions/freeze-and-isfrozen.bal");
    }

    @Test(dataProvider = "booleanValues")
    public void testBooleanFreeze(boolean i) {
        BValue[] returns = BRunUtil.invoke(result, "testBooleanFreeze", new BValue[]{new BBoolean(i)});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected booleans to be the same");
    }

    @Test(dataProvider = "intValues")
    public void testIntFreeze(int i) {
        BValue[] returns = BRunUtil.invoke(result, "testIntFreeze", new BValue[]{new BInteger(i)});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected ints to be the same");
    }

    @Test(dataProvider = "byteValues")
    public void testByteFreeze(int i) {
        BValue[] returns = BRunUtil.invoke(result, "testByteFreeze", new BValue[]{new BByte((byte) i)});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected ints to be the same");
    }

    @Test(dataProvider = "floatValues")
    public void testFloatFreeze(double i) {
        BValue[] returns = BRunUtil.invoke(result, "testFloatFreeze", new BValue[]{new BFloat(i)});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected floats to be the same");
    }

    @Test(dataProvider = "stringValues")
    public void testStringFreeze(String i) {
        BValue[] returns = BRunUtil.invoke(result, "testStringFreeze", new BValue[]{new BString(i)});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected strings to be the same");
    }

    @Test
    public void testBasicTypesAsJson() {
        BValue[] returns = BRunUtil.invoke(result, "testBasicTypesAsJson", new BValue[0]);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected json values to be the same");
    }

    @DataProvider(name = "booleanValues")
    public Object[][] booleanValues() {
        return new Object[][]{
                {true},
                {false}
        };
    }

    @DataProvider(name = "intValues")
    public Object[][] intValues() {
        return new Object[][]{
                {-123457},
                {0},
                {1},
                {53456032}
        };
    }

    @DataProvider(name = "byteValues")
    public Object[][] byteValues() {
        return new Object[][]{
                {0},
                {1},
                {255}
        };
    }

    @DataProvider(name = "floatValues")
    public Object[][] floatValues() {
        return new Object[][]{
                {-1234.57},
                {0.0},
                {1.1},
                {53456.032}
        };
    }

    @DataProvider(name = "stringValues")
    public Object[][] stringValues() {
        return new Object[][]{
                {"a"},
                {"Hello, from Ballerina!"}
        };
    }
}
