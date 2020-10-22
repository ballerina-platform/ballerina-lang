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

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Map access expression test.
 *
 * @since 0.8.0
 */
public class MapAccessExprTest {
    private CompileResult compileResult, resultNegative, resultSemanticsNegative;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/map/map-access-expr.bal");
        resultNegative = BCompileUtil.compile("test-src/types/map/map-access-negative.bal");
        resultSemanticsNegative = BCompileUtil.compile("test-src/types/map/map-access-semantics-negative.bal");
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
        Assert.assertEquals(incorrectCompileResult.getDiagnostics().length, 1);
        BAssertUtil.validateError(incorrectCompileResult, 0, "invalid operation: type 'any' does not support " +
                "indexing", 4, 12);
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
    public void testMapRemoveAll() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testMapRemoveAll", args);

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

        Assert.assertEquals(((BBoolean) returns[0]).value(), Boolean.TRUE);
    }

    @Test(description = "Test map has key negative.")
    public void testHasKeyNegative() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testHasKeyNegative", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);

        Assert.assertEquals(((BBoolean) returns[0]).value(), Boolean.FALSE);
    }

    @Test(description = "Test get map values.")
    public void testGetMapValues() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetMapValues", args);

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "Supun");
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertEquals(returns[1].stringValue(), "Colombo");
    }

    @Test(description = "Map access negative scenarios", groups = { "disableOnOldParser" })
    public void testNegativeSemantics() {
        Assert.assertEquals(resultSemanticsNegative.getDiagnostics().length, 4);
        int index = 0;
        BAssertUtil.validateError(resultSemanticsNegative, index++,
                "incompatible types: expected 'string', found " + "'int'", 4, 20);
        BAssertUtil.validateError(resultSemanticsNegative, index++,
                "invalid operation: type 'map' does not support field access", 9, 13);
        BAssertUtil.validateError(resultSemanticsNegative, index++, "missing identifier", 9, 20);
        BAssertUtil.validateError(resultSemanticsNegative, index++, "missing identifier", 9, 21);
    }

    @Test(description = "Map access negative scenarios")
    public void negativeTest() {
        Assert.assertEquals(resultNegative.getDiagnostics().length, 3);
        int index = 0;

        // uninitialized map access
        BAssertUtil.validateError(resultNegative, index++, "variable 'ints' is not initialized", 9, 5);
        BAssertUtil.validateError(resultNegative, index++, "variable 'ints' is not initialized", 11, 41);
        BAssertUtil.validateError(resultNegative, index, "variable 'm4' is not initialized", 34, 12);
    }

    @Test(description = "Test map remove key positive.")
    public void testMapRemovePositive() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testMapRemovePositive", args);

        Assert.assertEquals(returns.length, 3);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertSame(returns[2].getClass(), BBoolean.class);

        Assert.assertEquals(((BBoolean) returns[0]).value(), Boolean.TRUE);
        Assert.assertEquals(((BBoolean) returns[1]).value(), Boolean.TRUE);
        Assert.assertEquals(((BBoolean) returns[2]).value(), Boolean.FALSE);
    }

    @Test(expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: \\{ballerina/lang.map\\}KeyNotFound \\{\"message\":\"cannot " +
                    "find key 'fname2'.*")
    public void testMapRemoveNegative() {
        BRunUtil.invoke(compileResult, "testMapRemoveNegative");
    }

    @Test(description = "Test removeIfHasKey if key exists.")
    public void testRemoveIfHasKeyPositive1() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testRemoveIfHasKeyPositive1");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected booleans to be identified as equal");
    }

    @Test(description = "Test removeIfHasKey if key does not exist.")
    public void testRemoveIfHasKeyNegative1() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testRemoveIfHasKeyNegative1");
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(), "Expected booleans to be identified as equal");
    }

    @Test(description = "Test removeIfHasKey if key exists.")
    public void testRemoveIfHasKeyPositive2() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testRemoveIfHasKeyPositive2");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue(), "Expected booleans to be identified as equal");
    }

    @Test(description = "Test removeIfHasKey if key does not exist.")
    public void testRemoveIfHasKeyNegative2() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testRemoveIfHasKeyNegative2");
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue(), "Expected booleans to be identified as equal");
    }

    @Test(description = "Test to check toString for map of maps.")
    public void testMapToString() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMapToString");
        BString value = (BString) returns[0];
        Assert.assertEquals(value.stringValue(), "typedesc map<map<json>>");
    }
}
