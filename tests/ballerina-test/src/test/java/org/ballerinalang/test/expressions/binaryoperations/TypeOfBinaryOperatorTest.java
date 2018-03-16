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
package org.ballerinalang.test.expressions.binaryoperations;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test functionality of typeof binary expression.
 *
 * @since 0.966.0
 */
public class TypeOfBinaryOperatorTest {

    CompileResult result;
    CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile(this, "test-src", "expressions/binaryoperations/typeof-binary-operation.bal");
        resultNegative = BCompileUtil.compile(this, "test-src",
                "expressions/binaryoperations/typeof-binary-operation-negative.bal");
    }

    @Test(description = "Test compare with value type within if condition")
    public void testCompareWithValueTypeBasicIf() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "compareWithValueTypeBasicIf", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 1;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test compare with type type within if condition")
    public void testCompareWithTypeTypeBasicIf() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result,
                "compareWithTypeTypeBasicIf", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 1;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test compare with type type when declared with var")
    public void testCompareWithTypeTypeBasicIfWithVar() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result,
                "compareWithTypeTypeBasicIfWithVar", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 1;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test compare int with string basic if")
    public void testCompareIntWithStringBasicIf() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "compareIntWithStringBasicIf", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test comparing type defined as any")
    public void testCompareIntDefinedAsAnyBasicIf() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "compareIntDefinedAsAnyBasicIf", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test compare int value inside a map")
    public void testCompareIntInsideMapBasicIf() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "compareIntInsideMapBasicIf", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 2;
        Assert.assertEquals(actual, expected);
    }


    @Test(description = "Test compare int inside array basic if")
    public void testCompareIntInsideArrayBasicIf() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "compareIntInsideArrayBasicIf", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test compare array type false test")
    public void testCompareArrayTypeBasicIf() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "compareArrayTypeBasicIfFalse", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test compare array type success scenario")
    public void testCompareArrayTypeBasicIfTrue() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "compareArrayTypeBasicIfTrue", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 1;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test compare int value inside the struct")
    public void testCompareIntInsideStruct() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "compareIntInsideStruct", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test compare int value inside the struct true case")
    public void testCompareIntInsideStructTrueCase() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "compareIntInsideStructTrue", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 1;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test compare struct types")
    public void testCompareStructTypeBasicIfTrue() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "compareStructTypeBasicIfTrue", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 1;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test compare struct types fail scenario")
    public void testCompareStructTypeBasicIfFail() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "compareStructTypeBasicIfFail", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test compare json array inside if condition")
    public void testCompareJsonArrayBasicIf() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "compareJsonArrayBasicIf", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        Assert.assertEquals(actual, 1);
    }

    @Test(description = "Test compare json object inside if condition")
    public void testCompareJsonObjectBasicIf() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "compareJsonObjectBasicIf", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        Assert.assertEquals(actual, 1);
    }

    @Test(description = "Test compare type of json array as function return value")
    public void testCompareAsReturnJsonArray() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "compareAsReturnJsonArray", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        Assert.assertEquals(actual, 1);
    }

    @Test(description = "Test compare type of json object as function return value")
    public void testCompareAsReturnJsonObject() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "compareAsReturnJsonObject", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        Assert.assertEquals(actual, 2);
    }

    @Test(description = "Test compare type of json multi dimensional array negative case")
    public void testCompareMultiArrayJsonNegative() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "compareMultiArrayJsonNegative", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 2;
        Assert.assertEquals(actual, expected);
    }

//    @Test(description = "Test compare type of json multi dimensional array positive case") //TODO fix
    public void testRefTypeAccessTestMultiArrayJsonPositiveCase() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "compareMultiArrayJsonPositive", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 1;
        Assert.assertEquals(actual, expected);
    }

//    @Test(description = "Test compare different dimensions in json arrays case") //TODO fix
    public void testCompareMultiArrayJsonDifferentDimensions() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "compareMultiArrayJsonDifferentDimensions", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 1;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test compare type of int multi dimensional array negative case")
    public void testCompareMultiArrayIntNegative() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "compareMultiArrayIntNegative", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test compare type of int multi dimensional array positive case")
    public void testRefTypeAccessTestMultiArrayPositiveCase() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "compareMultiArrayIntPositive", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 1;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test compare different dimensions in int arrays case")
    public void testCompareMultiArrayDifferentDimensions() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "compareMultiArrayIntDifferentDimensions", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        int actual = (int) ((BInteger) returns[0]).intValue();
        int expected = 2;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test type of struct array")
    public void testTypeOfStructArray() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeOfStructArray");

        Assert.assertEquals(returns.length, 3);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertSame(returns[2].getClass(), BBoolean.class);

        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());
    }

    @Test(description = "Test type of struct after casting")
    public void testTypeOfStructAfterCast() {
        BValue[] returns = BRunUtil.invoke(result, "getTypePreserveWhenCast");

        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertSame(returns[3].getClass(), BBoolean.class);

        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        Assert.assertFalse(((BBoolean) returns[2]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[3]).booleanValue());
    }

    @Test(description = "Test recursive typeof")
    public void testRecursiveTypeOf() {
        BValue[] returns = BRunUtil.invoke(result, "recursiveTypeOf");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);

        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(description = "Test typeof as parameter")
    public void testTypeOfAsParam() {
        BValue[] returns = BRunUtil.invoke(result, "testTypeOfAsParam");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        Assert.assertEquals(((BInteger) returns[0]).intValue(), 1);
    }

    @Test(description = "Test typeof negative scenarios")
    public void testTypeOfNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 2);
        BAssertUtil.validateError(resultNegative, 0, "unknown type 'abc'", 3, 35);
        BAssertUtil.validateError(resultNegative, 1, "redeclared symbol 'person'", 6, 5);
    }

}
