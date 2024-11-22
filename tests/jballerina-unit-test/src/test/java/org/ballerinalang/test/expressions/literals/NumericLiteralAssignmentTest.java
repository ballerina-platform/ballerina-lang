/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.expressions.literals;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This class tests the assignment of numeric literals based on the expected types.
 *
 * @since 0.990.4
 */
public class NumericLiteralAssignmentTest {

    private CompileResult result, negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/literals/numeric_literal_assignment.bal");
        negativeResult = BCompileUtil
                .compile("test-src/expressions/literals/numeric_literal_assignment_negative.bal");
    }

    @Test(dataProvider = "intLiteralsAsSingleNumericTypeFunctions")
    public void testIntLiteralsAsSingleNumericType(String testFunctionName) {
        Object returns = BRunUtil.invoke(result, testFunctionName);
        Assert.assertTrue((Boolean) returns);
    }

    @DataProvider(name = "intLiteralsAsSingleNumericTypeFunctions")
    public Object[][] intLiteralsAsSingleNumericTypeFunctions() {
        return new Object[][]{
                {"testIntLiteralAsInt"},
                {"testIntLiteralAsByte"},
                {"testIntLiteralAsFloat"},
                {"testIntLiteralAsDecimal"}
        };
    }

    @Test(dataProvider = "floatLiteralsAsSingleNumericTypeFunctions")
    public void testFloatLiteralsAsSingleNumericType(String testFunctionName) {
        Object returns = BRunUtil.invoke(result, testFunctionName);
        Assert.assertTrue((Boolean) returns);
    }

    @DataProvider(name = "floatLiteralsAsSingleNumericTypeFunctions")
    public Object[][] floatLiteralsAsSingleNumericTypeFunctions() {
        return new Object[][]{
                {"testFloatLiteralAsFloat"},
                {"testFloatLiteralAsDecimal"}
        };
    }

    @Test(dataProvider = "intLiteralsAsNumericTypeInUnionFunctions")
    public void testIntLiteralsAsNumericTypeInUnion(String testFunctionName) {
        Object returns = BRunUtil.invoke(result, testFunctionName);
        Assert.assertTrue((Boolean) returns);
    }

    @DataProvider(name = "intLiteralsAsNumericTypeInUnionFunctions")
    public Object[][] intLiteralsAsNumericTypeInUnionFunctions() {
        return new Object[][]{
                {"testIntLiteralAsIntInUnion"},
                {"testIntLiteralAsByteInUnion"},
                {"testIntLiteralAsByteInUnion_2"},
                {"testIntLiteralAsFloatInUnion"},
                {"testIntLiteralAsFloatInUnion_2"},
                {"testIntLiteralAsDecimalInUnion"}
        };
    }

    @Test(dataProvider = "floatLiteralsAsNumericTypeInUnionFunctions")
    public void testFloatLiteralsAsNumericTypeInUnion(String testFunctionName) {
        Object returns = BRunUtil.invoke(result, testFunctionName);
        Assert.assertTrue((Boolean) returns);
    }

    @DataProvider(name = "floatLiteralsAsNumericTypeInUnionFunctions")
    public Object[][] floatLiteralsAsNumericTypeInUnionFunctions() {
        return new Object[][]{
                {"testFloatLiteralAsFloatInUnion"},
                {"testFloatLiteralAsDecimalInUnion"},
                {"testFloatLiteralAsDecimalInUnion_2"}
        };
    }

    @Test(dataProvider = "intLiteralsAsNumericTypeViaFiniteTypeFunctions")
    public void testIntLiteralsAsNumericTypeViaFiniteType(String testFunctionName) {
        Object returns = BRunUtil.invoke(result, testFunctionName);
        Assert.assertTrue((Boolean) returns);
    }

    @DataProvider(name = "intLiteralsAsNumericTypeViaFiniteTypeFunctions")
    public Object[][] intLiteralsAsNumericTypeViaFiniteTypeFunctions() {
        return new Object[][]{
                {"testIntLiteralAsIntViaFiniteType"},
                {"testIntLiteralAsByteViaFiniteType"},
                {"testIntLiteralAsFloatViaFiniteType"},
                {"testIntLiteralAsDecimalViaFiniteType"}
        };
    }

    @Test(dataProvider = "floatLiteralsAsNumericTypeViaFiniteTypeFunctions")
    public void testFloatLiteralsAsNumericTypeViaFiniteType(String testFunctionName) {
        Object returns = BRunUtil.invoke(result, testFunctionName);
        Assert.assertTrue((Boolean) returns);
    }

    @DataProvider(name = "floatLiteralsAsNumericTypeViaFiniteTypeFunctions")
    public Object[][] floatLiteralsAsNumericTypeViaFiniteTypeFunctions() {
        return new Object[][]{
                {"testFloatLiteralAsFloatViaFiniteType"},
                {"testFloatLiteralAsDecimalViaFiniteType"}
        };
    }

    @Test
    public void testIntLiteralAsIntWithBuiltinUnion() {
        Object returns = BRunUtil.invoke(result, "testIntLiteralAsIntWithBuiltinUnion");
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testFloatLiteralAsIntWithBuiltinUnion() {
        Object returns = BRunUtil.invoke(result, "testFloatLiteralAsFloatWithBuiltinUnion");
        Assert.assertTrue((Boolean) returns);
    }

    @Test(description = "Test numeric literal assignment statement with errors")
    public void testNumericLiteralAssignmentNegativeCases() {
        int index = 0;
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo', found 'float'", 24, 13);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo', found 'float'", 27, 13);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo', found 'float'", 28, 13);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo', found 'float'", 30, 13);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo2', found 'int'", 34, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo2', found 'int'", 35, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo2', found 'int'", 36, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo2', found 'int'", 37, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo2', found 'int'", 39, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo2', found 'float'", 40, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo3', found 'float'", 43, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo3', found 'float'", 44, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo3', found 'float'", 45, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo3', found 'float'", 46, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo5', found 'int'", 54, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo5', found 'float'", 55, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo5', found 'float'", 56, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo5', found 'float'", 57, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo5', found 'float'", 58, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo5', found 'float'", 60, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo5', found 'int'", 63, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo5', found 'float'", 64, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo5', found 'float'", 65, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo5', found 'float'", 68, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo5', found 'int'", 71, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo5', found 'float'", 72, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo5', found 'float'", 73, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo5', found 'float'", 76, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo5', found 'int'", 79, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo5', found 'float'", 80, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo5', found 'float'", 81, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo5', found 'float'", 82, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo5', found 'float'", 84, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo5', found 'float'", 89, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo5', found 'float'", 90, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo5', found 'float'", 92, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo5', found 'float'", 93, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo5', found 'float'", 94, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo5', found 'float'", 96, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'Foo5', found 'int'", 97, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(decimal|4d)', found 'float'", 104, 20);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(decimal|4d)', found 'float'", 109, 20);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(decimal|4d)', found 'float'", 111, 20);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(decimal|2f)', found 'float'", 121, 20);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(decimal|2f)', found 'float'", 122, 20);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(decimal|2f)', found 'float'", 124, 20);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(decimal|2f)', found 'float'", 127, 20);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(decimal|2f)', found 'float'", 128, 20);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(decimal|2f)', found 'float'", 131, 20);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(decimal|3.0f)', found 'float'", 133, 21);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(decimal|3.0f)', found 'float'", 134, 21);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(decimal|3.0f)', found 'float'", 136, 21);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(decimal|3.0f)', found 'float'", 139, 21);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(decimal|3.0f)', found 'float'", 140, 21);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(decimal|3.0f)', found 'float'", 143, 21);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(decimal|1)', found 'float'", 148, 19);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(decimal|1)', found 'int'", 150, 19);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(decimal|1)', found 'float'", 153, 19);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(decimal|1)', found 'float'", 155, 19);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(decimal|byte)', found 'float'", 160, 22);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(decimal|2.1f)', found 'float'", 162, 21);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(decimal|2.1f)', found 'float'", 163, 21);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(decimal|2.1f)', found 'float'", 165, 21);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(float|4d)', found 'decimal'", 176, 18);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(float|4d)', found 'decimal'", 179, 18);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|2f', found 'float'", 182, 15);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|2f', found 'float'", 183, 15);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|2f', found 'float'", 184, 15);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|2f', found 'float'", 185, 15);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|2f', found 'float'", 186, 15);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|2f', found 'float'", 188, 15);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|2f', found 'decimal'", 189, 15);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|2f', found 'float'", 192, 15);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|2f', found 'float'", 193, 15);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|2f', found 'decimal'", 195, 15);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|2f', found 'float'", 196, 15);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|2f', found 'decimal'", 197, 15);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|3.0f', found 'float'", 198, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|3.0f', found 'float'", 199, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|3.0f', found 'float'", 200, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|3.0f', found 'float'", 201, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|3.0f', found 'float'", 202, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|3.0f', found 'float'", 204, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|3.0f', found 'decimal'", 205, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|3.0f', found 'float'", 208, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|3.0f', found 'float'", 209, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|3.0f', found 'decimal'", 211, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|3.0f', found 'float'", 212, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|3.0f', found 'decimal'", 213, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|1', found 'int'", 214, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|1', found 'decimal'", 216, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|1', found 'decimal'", 217, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|1', found 'float'", 218, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|1', found 'float'", 220, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|1', found 'decimal'", 221, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|1', found 'int'", 223, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|1', found 'decimal'", 224, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|1', found 'decimal'", 225, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|1', found 'float'", 226, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|1', found 'decimal'", 227, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|1', found 'float'", 228, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|1', found 'decimal'", 229, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(byte|4d)', found 'decimal'", 233, 17);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(byte|4d)', found 'decimal'", 234, 17);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(byte|4d)', found 'float'", 235, 17);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(byte|4d)', found 'float'", 237, 17);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(byte|4d)', found 'decimal'", 238, 17);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|2.1f', found 'float'", 240, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|2.1f', found 'float'", 241, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|2.1f', found 'float'", 242, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|2.1f', found 'float'", 243, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|2.1f', found 'float'", 244, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|2.1f', found 'float'", 246, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|2.1f', found 'decimal'", 247, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'4d|2.1f', found 'decimal'", 250, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(float|2f)', found 'decimal'", 253, 18);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(float|2f)', found 'decimal'", 260, 18);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(float|2f)', found 'decimal'", 262, 18);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(float|3.0f)', found 'decimal'", 265, 19);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(float|3.0f)', found 'decimal'", 272, 19);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(float|3.0f)', found 'decimal'", 274, 19);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(float|1)', found 'decimal'", 277, 17);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(float|1)', found 'int'", 280, 17);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(float|1)', found 'decimal'", 284, 17);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(float|1)', found 'decimal'", 286, 17);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(float|byte)', found 'decimal'", 289, 20);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(float|2.1f)', found 'decimal'", 294, 19);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(float|2.1f)', found 'decimal'", 298, 19);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'2f|3.0f', found 'float'", 301, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'2f|3.0f', found 'float'", 302, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'2f|3.0f', found 'decimal'", 304, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'2f|3.0f', found 'float'", 305, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'2f|3.0f', found 'decimal'", 306, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'2f|3.0f', found 'float'", 309, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'2f|3.0f', found 'float'", 310, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'2f|3.0f', found 'decimal'", 312, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'2f|3.0f', found 'float'", 313, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'2f|3.0f', found 'decimal'", 314, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'2f|1', found 'int'", 316, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'2f|1', found 'float'", 317, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'2f|1', found 'float'", 318, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'2f|1', found 'decimal'", 320, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'2f|1', found 'float'", 321, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'2f|1', found 'decimal'", 322, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'2f|1', found 'int'", 324, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'2f|1', found 'float'", 325, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'2f|1', found 'float'", 326, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'2f|1', found 'float'", 327, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'2f|1', found 'decimal'", 328, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'2f|1', found 'float'", 329, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'2f|1', found 'decimal'", 330, 14);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(byte|2f)', found 'float'", 333, 17);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(byte|2f)', found 'float'", 334, 17);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(byte|2f)', found 'decimal'", 336, 17);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(byte|2f)', found 'float'", 337, 17);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(byte|2f)', found 'decimal'", 338, 17);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'2f|2.1f', found 'float'", 342, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'2f|2.1f', found 'float'", 343, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'2f|2.1f', found 'decimal'", 345, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'2f|2.1f', found 'float'", 346, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'2f|2.1f', found 'decimal'", 347, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'2f|2.1f', found 'decimal'", 350, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'3.0f|1', found 'int'", 352, 15);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'3.0f|1', found 'float'", 353, 15);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'3.0f|1', found 'float'", 354, 15);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'3.0f|1', found 'decimal'", 356, 15);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'3.0f|1', found 'float'", 357, 15);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'3.0f|1', found 'decimal'", 358, 15);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'3.0f|1', found 'int'", 360, 15);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'3.0f|1', found 'float'", 361, 15);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'3.0f|1', found 'float'", 362, 15);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'3.0f|1', found 'float'", 363, 15);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'3.0f|1', found 'decimal'", 364, 15);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'3.0f|1', found 'float'", 365, 15);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'3.0f|1', found 'decimal'", 366, 15);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(byte|3.0f)', found 'float'", 369, 18);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(byte|3.0f)', found 'float'", 370, 18);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(byte|3.0f)', found 'decimal'", 372, 18);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(byte|3.0f)', found 'float'", 373, 18);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(byte|3.0f)', found 'decimal'", 374, 18);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'3.0f|2.1f', found 'float'", 378, 17);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'3.0f|2.1f', found 'float'", 379, 17);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'3.0f|2.1f', found 'decimal'", 381, 17);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'3.0f|2.1f', found 'float'", 382, 17);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'3.0f|2.1f', found 'decimal'", 383, 17);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'3.0f|2.1f', found 'decimal'", 386, 17);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'3.0f|2.1f', found 'float'", 387, 17);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(byte|1)', found 'float'", 390, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(byte|1)', found 'float'", 391, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(byte|1)', found 'float'", 392, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(byte|1)', found 'decimal'", 393, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(byte|1)', found 'float'", 394, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(byte|1)', found 'decimal'", 395, 16);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'1|2.1f', found 'int'", 398, 15);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'1|2.1f', found 'float'", 399, 15);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'1|2.1f', found 'float'", 400, 15);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'1|2.1f', found 'float'", 401, 15);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'1|2.1f', found 'decimal'", 402, 15);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'1|2.1f', found 'float'", 403, 15);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'1|2.1f', found 'decimal'", 404, 15);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'1|2.1f', found 'decimal'", 407, 15);
        BAssertUtil.validateError(negativeResult, index++, "incompatible types: expected " +
                "'(byte|2.1f)', found 'decimal'", 411, 18);
        Assert.assertEquals(negativeResult.getErrorCount(), index);
    }

    @AfterClass
    public void tearDown() {
        result = null;
        negativeResult = null;
    }
}
