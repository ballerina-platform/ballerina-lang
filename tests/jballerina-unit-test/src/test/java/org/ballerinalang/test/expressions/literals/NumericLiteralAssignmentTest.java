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

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This class tests the assignment of numeric literals based on the expected types.
 *
 * @since 0.990.4
 */
public class NumericLiteralAssignmentTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/literals/numeric_literal_assignment.bal");
    }

    @Test(dataProvider = "intLiteralsAsSingleNumericTypeFunctions")
    public void testIntLiteralsAsSingleNumericType(String testFunctionName) {
        BValue[] returns = BRunUtil.invoke(result, testFunctionName);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
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
        BValue[] returns = BRunUtil.invoke(result, testFunctionName);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
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
        BValue[] returns = BRunUtil.invoke(result, testFunctionName);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @DataProvider(name = "intLiteralsAsNumericTypeInUnionFunctions")
    public Object[][] intLiteralsAsNumericTypeInUnionFunctions() {
        return new Object[][]{
                {"testIntLiteralAsIntInUnion"},
                {"testIntLiteralAsByteInUnion"},
                {"testIntLiteralAsByteInUnion_2"},
                {"testIntLiteralAsFloatInUnion"},
                {"testIntLiteralAsFloatInUnion_2"},
                {"testIntLiteralAsDecimalInUnion"},
                {"testIntLiteralAsDecimalInUnion_2"}
        };
    }

    @Test(dataProvider = "floatLiteralsAsNumericTypeInUnionFunctions")
    public void testFloatLiteralsAsNumericTypeInUnion(String testFunctionName) {
        BValue[] returns = BRunUtil.invoke(result, testFunctionName);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
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
        BValue[] returns = BRunUtil.invoke(result, testFunctionName);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
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
        BValue[] returns = BRunUtil.invoke(result, testFunctionName);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
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
        BValue[] returns = BRunUtil.invoke(result, "testIntLiteralAsIntWithBuiltinUnion");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testFloatLiteralAsIntWithBuiltinUnion() {
        BValue[] returns = BRunUtil.invoke(result, "testFloatLiteralAsFloatWithBuiltinUnion");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }
}
