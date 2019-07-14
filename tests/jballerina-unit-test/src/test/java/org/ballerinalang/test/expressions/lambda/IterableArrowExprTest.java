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

import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BFunctionPointer;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
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
@Test(groups = { "brokenOnLangLibChange" })
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
        Assert.assertEquals(((BValueArray) returns[0]).getString(0), "ANT");
        Assert.assertEquals(((BValueArray) returns[0]).getString(1), "BEAR");
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
        Assert.assertEquals(((BValueArray) returns[0]).getString(0), "ANT");
        Assert.assertEquals(((BValueArray) returns[0]).getString(1), "BEAR");
    }

    @Test(description = "Test arrow expression inside map() then filter() then map()")
    public void testTwoLevelMapIterableWithFilter() {
        BValue[] returns = BRunUtil.invoke(basic, "testTwoLevelMapIterableWithFilter");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BValueArray) returns[0]).getString(0), "BEAR");
    }

    @Test(description = "Test arrow expression with filter() first and then map")
    public void testFilterThenMap() {
        BValue[] returns = BRunUtil.invoke(basic, "testFilterThenMap");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(((BValueArray) returns[0]).getString(0), "ANT MAN");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 1);
    }

    @Test(description = "Test arrow expression inside map() with return a single string")
    public void testFilterWithArityOne() {
        BValue[] returns = BRunUtil.invoke(basic, "testFilterWithArityOne");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BValueArray) returns[0]).getString(0), "ANT");
        Assert.assertEquals(((BValueArray) returns[0]).getString(1), "TIGER");
    }

    @Test(description = "Test arrow expression inside map() which returns a lambda collection")
    public void testIterableReturnLambda() {
        BValue[] returns = BRunUtil.invoke(basic, "testIterableReturnLambda");
        Assert.assertNotNull(returns);
        Assert.assertTrue(returns[0] instanceof BFunctionPointer);
        Assert.assertTrue(returns[1] instanceof BFunctionPointer);
        Assert.assertTrue(returns[2] instanceof BFunctionPointer);
        Assert.assertEquals(returns[0].getType().toString(), "function (int) returns (boolean)");
        Assert.assertEquals(returns[1].getType().toString(), "function (int) returns (boolean)");
        Assert.assertEquals(returns[2].getType().toString(), "function (int) returns (boolean)");
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
                "function invocation on type '[string,string]' is not supported", 22, 42);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'int[]', found 'string[]'", 23, 31);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid number of parameters used in arrow expression. expected: '1' but found '2'", 24, 34);
        BAssertUtil.validateError(resultNegative, i++,
                "invalid number of parameters used in arrow expression. expected: '1' but found '2'", 26, 30);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'string', found 'float'", 27, 19);
        BAssertUtil.validateError(resultNegative, i++,
                "undefined function 'float.toUpper'", 28, 40);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'string', found '(string) collection'", 29, 31);
        BAssertUtil.validateError(resultNegative, i++,
                "function invocation on type '[string,string]' is not supported", 37, 75);
        BAssertUtil.validateError(resultNegative, i++,
                "incompatible types: expected 'string[]', found 'int'", 39, 20);
        BAssertUtil.validateError(resultNegative, i++,
                "arrow expression can not be used with 'foreach' iterable", 47, 16);
    }
}
