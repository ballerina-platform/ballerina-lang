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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BFunctionPointer;
import io.ballerina.runtime.api.values.BMap;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.ballerina.runtime.api.utils.TypeUtils.getType;

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
        Object returns = BRunUtil.invoke(basic, "testMapIterable");
        Assert.assertEquals(((BMap) returns).get(StringUtils.fromString("a")).toString(), "ANT");
        Assert.assertEquals(((BMap) returns).get(StringUtils.fromString("b")).toString(), "BEAR");
    }

    @Test(description = "Test arrow expression inside filter() and then inside map() followed by average()")
    public void testFilterIterable() {
        Object returns = BRunUtil.invoke(basic, "testFilterIterable");
        Assert.assertEquals(returns, 9.0);
    }

    @Test(description = "Test arrow expression inside map() followed by map()")
    public void testTwoLevelMapIterable() {
        Object returns = BRunUtil.invoke(basic, "testTwoLevelMapIterable");
        Assert.assertEquals(((BMap) returns).get(StringUtils.fromString("a")).toString(), "ANT");
        Assert.assertEquals(((BMap) returns).get(StringUtils.fromString("b")).toString(), "BEAR");
    }

    @Test(description = "Test arrow expression inside map() then filter() then map()")
    public void testTwoLevelMapIterableWithFilter() {
        Object returns = BRunUtil.invoke(basic, "testTwoLevelMapIterableWithFilter");
        Assert.assertEquals(((BMap) returns).get(StringUtils.fromString("b")).toString(), "BEAR");
    }

    @Test(description = "Test arrow expression with filter() first and then map")
    public void testFilterThenMap() {
        Object arr = BRunUtil.invoke(basic, "testFilterThenMap");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(returns.get(0).toString(), "{\"a\":\"N MAN\"}");
        Assert.assertEquals(returns.get(1), 1L);
    }

    @Test(description = "Test arrow expression inside map() with return a single string")
    public void testFilterWithArityOne() {
        Object returns = BRunUtil.invoke(basic, "testFilterWithArityOne");
        Assert.assertEquals(((BMap) returns).get(StringUtils.fromString("a")).toString(), "ANT");
        Assert.assertEquals(((BMap) returns).get(StringUtils.fromString("c")).toString(), "TIGER");
    }

    @Test(description = "Test arrow expression inside map() which returns a lambda collection")
    public void testIterableReturnLambda() {
        Object returns = BRunUtil.invoke(basic, "testIterableReturnLambda");
        Assert.assertNotNull(returns);
        BMap res = (BMap) returns;
        Assert.assertTrue(res.get(StringUtils.fromString("a")) instanceof BFunctionPointer);
        Assert.assertTrue(res.get(StringUtils.fromString("b")) instanceof BFunctionPointer);
        Assert.assertTrue(res.get(StringUtils.fromString("c")) instanceof BFunctionPointer);
        Assert.assertEquals(getType(res.get(StringUtils.fromString("a"))).toString(),
                "isolated function (int) returns (boolean)");
        Assert.assertEquals(getType(res.get(StringUtils.fromString("b"))).toString(),
                "isolated function (int) returns (boolean)");
        Assert.assertEquals(getType(res.get(StringUtils.fromString("c"))).toString(),
                "isolated function (int) returns (boolean)");
    }

    @Test(description = "Test arrow expression inside map() with return string collection and then count()")
    public void testCountFunction() {
        Object returns = BRunUtil.invoke(basic, "testCountFunction");
        Assert.assertEquals(returns, 3L);
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

    @AfterClass
    public void tearDown() {
        basic = null;
        resultNegative = null;
    }
}
