/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.types.finite;

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;

/**
 * Test finite type.
 */
public class FiniteTypeTest {

    private CompileResult result;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        result = BCompileUtil.compile("test-src/types/finite/finite-type.bal");
    }

    @Test()
    public void finiteAssignmentStateType() {
        Object returns = BRunUtil.invoke(result, "finiteAssignmentStateType");

        Assert.assertNotNull(returns);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "off");
    }

    @Test()
    public void finiteAssignmentNumberSetType() {
        Object returns = BRunUtil.invoke(result, "finiteAssignmentNumberSetType");

        Assert.assertNotNull(returns);
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 5L);
    }

    @Test()
    public void finiteAssignmentStringOrIntSetType() {
        Object returns = BRunUtil.invoke(result, "finiteAssignmentStringOrIntSetType");

        Assert.assertNotNull(returns);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals((returns.toString()), "This is a string");
    }

    @Test()
    public void finiteAssignmentStringOrIntSetTypeCaseTwo() {
        Object returns = BRunUtil.invoke(result, "finiteAssignmentStringOrIntSetTypeCaseTwo");

        Assert.assertNotNull(returns);
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 111L);
    }

    @Test()
    public void finiteAssignmentIntSetType() {
        Object returns = BRunUtil.invoke(result, "finiteAssignmentIntSetType");

        Assert.assertNotNull(returns);
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 222L);
    }

    @Test()
    public void finiteAssignmentIntArrayType() {
        Object returns = BRunUtil.invoke(result, "finiteAssignmentIntArrayType");

        Assert.assertNotNull(returns);
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 9989L);
    }

    @Test()
    public void finiteAssignmentStateSameTypeComparison() {
        Object returns = BRunUtil.invoke(result, "finiteAssignmentStateSameTypeComparison");

        Assert.assertNotNull(returns);
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 2L);
    }

    @Test()
    public void finiteAssignmentStateSameTypeComparisonCaseTwo() {
        Object returns = BRunUtil.invoke(result, "finiteAssignmentStateSameTypeComparisonCaseTwo");

        Assert.assertNotNull(returns);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals((returns.toString()), "on");
    }

    @Test()
    public void finiteAssignmentRefValueType() {
        Object returns = BRunUtil.invoke(result, "finiteAssignmentRefValueType");

        Assert.assertNotNull(returns);
        Assert.assertTrue(returns instanceof BMap);
    }

    @Test()
    public void finiteAssignmentRefValueTypeCaseTwo() {
        Object returns = BRunUtil.invoke(result, "finiteAssignmentRefValueTypeCaseTwo");

        Assert.assertNotNull(returns);
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 4L);
    }

    @Test()
    public void testFiniteTypeWithTypeCheck() {
        Object returns = BRunUtil.invoke(result, "testFiniteTypeWithTypeCheck");

        Assert.assertNotNull(returns);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals((returns.toString()), "ss");
    }

    @Test()
    public void testFiniteTypesWithDefaultValues() {
        Object returns = BRunUtil.invoke(result, "testFiniteTypesWithDefaultValues");

        Assert.assertNotNull(returns);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals((returns.toString()), "on");
    }

    @Test()
    public void testFiniteTypesWithUnion() {
        Object returns = BRunUtil.invoke(result, "testFiniteTypesWithUnion");

        Assert.assertNotNull(returns);
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 1L);
    }

    @Test()
    public void testFiniteTypesWithUnionCaseOne() {
        Object returns = BRunUtil.invoke(result, "testFiniteTypesWithUnionCaseOne");

        Assert.assertNotNull(returns);
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 100L);
    }

    @Test()
    public void testFiniteTypesWithUnionCaseTwo() {
        Object returns = BRunUtil.invoke(result, "testFiniteTypesWithUnionCaseTwo");

        Assert.assertNotNull(returns);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals((returns.toString()), "on");
    }

    @Test()
    public void testFiniteTypesWithUnionCaseThree() {
        Object returns = BRunUtil.invoke(result, "testFiniteTypesWithUnionCaseThree");

        Assert.assertNotNull(returns);
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 1001L);
    }

    @Test()
    public void testFiniteTypesWithTuple() {
        Object returns = BRunUtil.invoke(result, "testFiniteTypesWithTuple");

        Assert.assertNotNull(returns);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals((returns.toString()), "on");
    }

    @Test()
    public void testTypeAliasing() {
        Object returns = BRunUtil.invoke(result, "testTypeAliasing");

        Assert.assertNotNull(returns);
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals((returns.toString()), "Anonymous name");
    }

    @Test()
    public void testTypeAliasingCaseOne() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testTypeAliasingCaseOne");
        Assert.assertEquals(returns.size(), 2);
        Assert.assertNotNull(returns.get(0));
        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertEquals(returns.get(0), 100L);
        Assert.assertNotNull(returns.get(1));
        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertEquals(returns.get(1).toString(), "hundred");
    }

    @Test()
    public void testTypeDefinitionWithVarArgs() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testTypeDefinitionWithVarArgs");
        Assert.assertEquals(returns.size(), 2);
        Assert.assertNotNull(returns.get(0));
        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertEquals(returns.get(0).toString(), "John");
        Assert.assertNotNull(returns.get(1));
        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertEquals(returns.get(1).toString(), "Anne");
    }

    @Test()
    public void testTypeDefinitionWithArray() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testTypeDefinitionWithArray");
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertTrue(returns.get(1) instanceof Long);
        Assert.assertEquals((returns.get(0)), 2L);
        Assert.assertEquals((returns.get(1)), 23L);
    }

    @Test
    public void testTypeDefWithFunctions() {
        Object returns = BRunUtil.invoke(result, "testTypeDefWithFunctions");
        Assert.assertEquals(returns, (long) "Hello".length());
    }

    @Test
    public void testTypeDefWithFunctions2() {
        Object returns = BRunUtil.invoke(result, "testTypeDefWithFunctions2");
        Assert.assertEquals(returns, (long) "Hello".length());
    }

    @Test
    public void testFiniteTypeWithConstants() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testFiniteTypeWithConstants");
        Assert.assertTrue(returns.get(0) instanceof Long, "Type mismatch");
        Assert.assertEquals(returns.get(0), 5L, "Value mismatch");
        Assert.assertTrue(returns.get(1) instanceof BString, "Type mismatch");
        Assert.assertEquals(returns.get(1).toString(), "s", "Value mismatch");
    }

    @Test
    public void testFiniteTypeWithNumericConstants() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testFiniteTypeWithNumericConstants");
        Assert.assertTrue(returns.get(0) instanceof Long, "Type mismatch");
        Assert.assertEquals(returns.get(0), 5L, "Value mismatch");
        Assert.assertTrue(returns.get(1) instanceof Double, "Type mismatch");
        Assert.assertEquals(returns.get(1), 5.0d, "Value mismatch");
    }

    @Test
    public void testAssigningIntLiteralToByteFiniteType() {
        Object returns = BRunUtil.invoke(result, "testAssigningIntLiteralToByteFiniteType");
        Assert.assertTrue(returns instanceof Integer, "Type mismatch");
        Assert.assertEquals(returns, 5, "Value mismatch");
    }

    @Test
    public void testAssigningIntLiteralToFloatFiniteType() {
        Object returns = BRunUtil.invoke(result, "testAssigningIntLiteralToFloatFiniteType");
        Assert.assertTrue(returns instanceof Double, "Type mismatch");
        Assert.assertEquals(returns, 5.0, "Value mismatch");
    }

    @Test
    public void testAssigningIntLiteralToDecimalFiniteType() {
        Object returns = BRunUtil.invoke(result, "testAssigningIntLiteralToDecimalFiniteType");
        Assert.assertTrue(returns instanceof BDecimal, "Type mismatch");
        Assert.assertEquals(((BDecimal) returns).value().compareTo(new BigDecimal("5")), 0, "Value mismatch");
    }

    @Test
    public void testAssigningFloatLiteralToDecimalFiniteType() {
        Object returns = BRunUtil.invoke(result, "testAssigningFloatLiteralToDecimalFiniteType");
        Assert.assertTrue(returns instanceof BDecimal, "Type mismatch");
        Assert.assertEquals(((BDecimal) returns).value().compareTo(new BigDecimal("5.0")), 0,
                "Value mismatch");
    }

    @Test
    public void testDifferentPrecisionFloatAssignment() {
        Object returns = BRunUtil.invoke(result, "testDifferentPrecisionFloatAssignment");
        Assert.assertTrue(returns instanceof Double, "Type mismatch");
        Assert.assertEquals(returns, 5.0, "Value mismatch");
    }

    @Test
    public void testDifferentPrecisionFloatConstantAssignment() {
        Object returns = BRunUtil.invoke(result, "testDifferentPrecisionFloatConstantAssignment");
        Assert.assertTrue(returns instanceof Double, "Type mismatch");
        Assert.assertEquals(returns, 5.0, "Value mismatch");
    }

    @Test
    public void testDifferentPrecisionDecimalAssignment() {
        Object returns = BRunUtil.invoke(result, "testDifferentPrecisionDecimalAssignment");
        Assert.assertTrue(returns instanceof BDecimal, "Type mismatch");
        Assert.assertEquals(((BDecimal) returns).value().compareTo(new BigDecimal("5.0")), 0,
                "Value mismatch");
    }

    @Test
    public void testDifferentPrecisionDecimalConstantAssignment() {
        Object returns = BRunUtil.invoke(result, "testDifferentPrecisionDecimalConstantAssignment");
        Assert.assertTrue(returns instanceof BDecimal, "Type mismatch");
        Assert.assertEquals(((BDecimal) returns).value().compareTo(new BigDecimal("5")), 0, "Value mismatch");
    }

    @Test(dataProvider = "assignmentToBroaderTypeFunctions")
    public void testFiniteTypeAssignmentToBroaderType(String function) {
        Object returns = BRunUtil.invoke(result, function);
        Assert.assertTrue((Boolean) returns);
    }

    @DataProvider(name = "assignmentToBroaderTypeFunctions")
    public Object[][] assignmentToBroaderTypeFunctions() {
        return new Object[][]{
                {"testStringOnlyFiniteTypeAssignmentToTypeWithString"},
                {"testIntOnlyFiniteTypeAssignmentToTypeWithInt"},
                {"testFloatOnlyFiniteTypeAssignmentToTypeWithFloat"},
                {"testBooleanOnlyFiniteTypeAssignmentToTypeWithBoolean"},
                {"testByteOnlyFiniteTypeAssignmentToTypeWithByte"},
                {"testFiniteTypeAssignmentToBroaderType"},
                {"testFiniteTypeWithConstAssignmentToBroaderType"},
                {"testFiniteTypeWithConstAndTypeAssignmentToBroaderType"},
                {"testFiniteTypesAsUnionsAsBroaderTypes_1"},
                {"testFiniteTypesAsUnionsAsBroaderTypes_2"}
        };
    }

    @Test(description = "Test finite type where float/decimal discriminated literals as members")
    public void testFiniteTypeWithDiscriminatedMembers() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testFiniteTypesWithDiscriminatedMembers");
        Assert.assertEquals(returns.get(0), 1.0);
        Assert.assertEquals(returns.get(1), ValueCreator.createDecimalValue("1.0"));
    }

    @Test(description = "Test finite type where typename is escaped identifier")
    public void testEscapedTypeName() {
        Object returns = BRunUtil.invoke(result, "testEscapedTypeName");
        Assert.assertEquals(returns.toString(), "-");
    }

    @Test(description = "Test finite type where integer literals with positive sign as members")
    public void testFiniteTypesWithPositiveIntegers() {
        BRunUtil.invoke(result, "testFiniteTypesWithPositiveIntegers");
    }

    @Test(description = "Test finite type where float literals with positive sign as members")
    public void testFiniteTypesWithPositiveFloats() {
        BRunUtil.invoke(result, "testFiniteTypesWithPositiveFloats");
    }

    @Test
    public void testNilFiniteType() {
        BRunUtil.invoke(result, "testNilFiniteType");
    }

    @Test
    public void testOutOfRangeFiniteType() {
        BRunUtil.invoke(result, "testOutOfRangeFiniteType");
    }

    @Test
    public void testRecordStringEquality() {
        BRunUtil.invoke(result, "testRecordStringEquality");
    }

    @Test
    public void testFiniteType() {
        BRunUtil.invoke(result, "testFiniteType");
    }

    @Test
    public void testFiniteTypeWithUnaryMinus() {
        BRunUtil.invoke(result, "testFiniteTypeWithUnaryMinus");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
