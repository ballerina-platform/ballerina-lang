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

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/literals/numeric_literal_assignment.bal");
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

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
