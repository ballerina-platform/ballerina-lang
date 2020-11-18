/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.ballerinalang.test.expressions.lambda;

import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BFunctionPointer;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * TestCases for Arrow Expressions used in Iterable Functions.
 *
 * @since 0.982.0
 */
@Test
public class IterableArrowExprTest {

    private CompileResult basic, resultNegative;

    @BeforeClass
    public void setup() {
        basic = BCompileUtil.compile("test-src/expressions/lambda/iterable/iterable-arrow-expression.bal");
        resultNegative = BCompileUtil
                .compile("test-src/expressions/lambda/iterable/iterable-arrow-expression-negative.bal");
    }

    @Test(description = "Test arrow expression inside map() with return string collection")
    public void testMapIterable() {
        BValue[] returns = BRunUtil.invoke(basic, "testMapIterable");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BMap) returns[0]).get("a").stringValue(), "ANT");
        Assert.assertEquals(((BMap) returns[0]).get("b").stringValue(), "BEAR");
    }

    @Test(description = "Test arrow expression inside filter() and then inside map() followed by average()")
    public void testFilterIterable() {
        BValue[] returns = BRunUtil.invoke(basic, "testFilterIterable");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 9.0);
    }

    @Test(description = "Test arrow expression inside map() followed by map()")
    public void testTwoLevelMapIterable() {
        BValue[] returns = BRunUtil.invoke(basic, "testTwoLevelMapIterable");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BMap) returns[0]).get("a").stringValue(), "ANT");
        Assert.assertEquals(((BMap) returns[0]).get("b").stringValue(), "BEAR");
    }

    @Test(description = "Test arrow expression inside map() then filter() then map()")
    public void testTwoLevelMapIterableWithFilter() {
        BValue[] returns = BRunUtil.invoke(basic, "testTwoLevelMapIterableWithFilter");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BMap) returns[0]).get("b").stringValue(), "BEAR");
    }

    @Test(description = "Test arrow expression with filter() first and then map")
    public void testFilterThenMap() {
        BValue[] returns = BRunUtil.invoke(basic, "testFilterThenMap");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(((BMap) returns[0]).stringValue(), "{\"a\":\"N MAN\"}");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 1);
    }

    @Test(description = "Test arrow expression inside map() with return a single string")
    public void testFilterWithArityOne() {
        BValue[] returns = BRunUtil.invoke(basic, "testFilterWithArityOne");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BMap) returns[0]).get("a").stringValue(), "ANT");
        Assert.assertEquals(((BMap) returns[0]).get("c").stringValue(), "TIGER");
    }

    @Test(description = "Test arrow expression inside map() which returns a lambda collection")
    public void testIterableReturnLambda() {
        BValue[] returns = BRunUtil.invoke(basic, "testIterableReturnLambda");
        Assert.assertNotNull(returns);
        BMap res = (BMap) returns[0];
        Assert.assertTrue(res.get("a") instanceof BFunctionPointer);
        Assert.assertTrue(res.get("b") instanceof BFunctionPointer);
        Assert.assertTrue(res.get("c") instanceof BFunctionPointer);
        Assert.assertEquals(res.get("a").getType().toString(), "function (int) returns (boolean)");
        Assert.assertEquals(res.get("b").getType().toString(), "function (int) returns (boolean)");
        Assert.assertEquals(res.get("c").getType().toString(), "function (int) returns (boolean)");
    }

    @Test(description = "Test arrow expression inside map() with return string collection and then count()")
    public void testCountFunction() {
        BValue[] returns = BRunUtil.invoke(basic, "testCountFunction");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 3);
    }

    @Test(description = "Negative test cases for arrow expression used for functional iterations")
    public void testNegativeArrowExpr() {
        int i = 0;
        Assert.assertEquals(resultNegative.getErrorCount(), 10);

        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'int[]', found 'map<string>'", 22, 21);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'string[]', found 'map<other>'", 23, 24);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid number of parameters used in arrow expression. expected: '1' but found '2'", 23, 34);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid number of parameters used in arrow expression. expected: '1' but found '2'", 25, 28);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'string', found 'int'", 26, 19);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'string', found 'int'", 27, 19);
        BAssertUtil.validateError(resultNegative, i++,
                "undefined function 'toUpperAscii' in type 'float'", 27, 46);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'string', found 'string[]'", 28, 19);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'string[]', found 'map<string>'", 34, 24);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'string[]', found 'int'", 35, 20);
    }
}
