/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.types.map;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Map access expression test.
 *
 * @since 0.8.0
 */
public class MapAccessExprTest {
    private CompileResult compileResult;
    CompileResult incorrectCompileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/map/map-access-expr.bal");
        incorrectCompileResult = BCompileUtil.compile("test-src/types/map/map-access-negative.bal");
    }

    @Test(description = "Test map access expression")
    public void testMapAccessExpr() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BValue[] returns = BRunUtil.invoke(compileResult, "mapAccessTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 105;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test map access through var keyword")
    public void testAccessThroughVar() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testAccessThroughVar", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);

        String expectedStr = "x:a, y:b, z:c, ";
        String actualStr = returns[0].stringValue();
        Assert.assertEquals(actualStr, expectedStr);
    }

    @Test(description = "Test map return value")
    public void testArrayReturnValueTest() {
        BValue[] args = {new BString("Chanaka"), new BString("Fernando")};
        BValue[] returns = BRunUtil.invoke(compileResult, "mapReturnTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BMap.class);

        BMap mapValue = (BMap) returns[0];
        Assert.assertEquals(mapValue.size(), 3);

        Assert.assertEquals(mapValue.get("fname").stringValue(), "Chanaka");
        Assert.assertEquals(mapValue.get("lname").stringValue(), "Fernando");
        Assert.assertEquals(mapValue.get("ChanakaFernando").stringValue(), "ChanakaFernando");

    }

    @Test(description = "Test nested map access")
    public void testNestedMapAccess() {
        CompileResult incorrectCompileResult = BCompileUtil.compile("test-src/types/map/nested-map-access.bal");
        Assert.assertTrue(incorrectCompileResult.getDiagnostics().length == 2);
        Assert.assertEquals(incorrectCompileResult.getDiagnostics()[0].getMessage(),
                "invalid operation: type 'any' does not support field access");
        Assert.assertEquals(incorrectCompileResult.getDiagnostics()[1].getMessage(),
                "incompatible types: expected 'string', found 'other?'");
    }

    @Test(description = "Test array access expression as the index of a map")
    public void testArrayAccessAsIndexOfMapt() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testArrayAccessAsIndexOfMapt", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);

        Assert.assertEquals(returns[0].stringValue(), "Supun");
    }

    @Test(description = "Test map clear.")
    public void testMapClear() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testMapClear", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        Assert.assertEquals(((BInteger) returns[0]).value(), new Long(0));
    }

    @Test(description = "Test map has key positive.")
    public void testHasKeyPositive() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testHasKeyPositive", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);

        Assert.assertEquals(((BBoolean) returns[0]).value(), new Boolean(true));
    }

    @Test(description = "Test map has key negative.")
    public void testHasKeyNegative() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testHasKeyNegative", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);

        Assert.assertEquals(((BBoolean) returns[0]).value(), new Boolean(false));
    }

    // Failing because json to primitives not working due to reg-index incorrect assignment in safe assignment stmt
    @Test(description = "Test get map values.", enabled = false)
    public void testGetMapValues() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetMapValues", args);

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "Supun");
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertEquals(returns[1].stringValue(), "Colombo");
    }

    @Test(description = "Map access negative scenarios")
    public void negativeTest() {
        Assert.assertTrue(incorrectCompileResult.getDiagnostics().length == 2);

        // testMapAccessWithIndex
        Assert.assertEquals(incorrectCompileResult.getDiagnostics()[0].getMessage(),
                "incompatible types: expected 'string', found 'int'");

        // accessAllFields
        Assert.assertEquals(incorrectCompileResult.getDiagnostics()[1].getMessage(),
                "cannot get all fields from a map");
    }

    @Test(description = "Test map remove key positive.")
    public void testMapRemovePositive() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testMapRemovePositive", args);

        Assert.assertEquals(returns.length, 3);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertSame(returns[2].getClass(), BBoolean.class);

        Assert.assertEquals(((BBoolean) returns[0]).value(), new Boolean(true));
        Assert.assertEquals(((BBoolean) returns[1]).value(), new Boolean(true));
        Assert.assertEquals(((BBoolean) returns[2]).value(), new Boolean(false));
    }

    @Test(description = "Test map remove key negative.")
    public void testMapRemoveNegative() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testMapRemoveNegative", args);

        Assert.assertEquals(returns.length, 3);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertSame(returns[2].getClass(), BBoolean.class);

        Assert.assertEquals(((BBoolean) returns[0]).value(), new Boolean(false));
        Assert.assertEquals(((BBoolean) returns[1]).value(), new Boolean(false));
        Assert.assertEquals(((BBoolean) returns[2]).value(), new Boolean(false));
    }

}
