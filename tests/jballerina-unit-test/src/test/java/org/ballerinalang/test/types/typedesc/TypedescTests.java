/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.test.types.typedesc;

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BTypedesc;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * This class contains typedesc type related test cases.
 */
public class TypedescTests {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/typedesc/typedesc_positive.bal");
    }

    @Test(description = "Test basics types")
    public void testNegative() {
        final CompileResult compileResult = BCompileUtil.compile("test-src/types/typedesc/typedesc_negative.bal");
        int index = 0;
        validateError(compileResult, index++, "missing identifier", 2, 8);
        validateError(compileResult, index++, "undefined symbol 'i'", 7, 9);
        validateError(compileResult, index++, "invalid operation: type 'byte' does not support member access", 9, 18);
        validateError(compileResult, index++, "missing key expr in member access expr", 9, 23);
        validateError(compileResult, index++, "invalid operation: type 'int' does not support member access", 10, 18);
        validateError(compileResult, index++, "missing key expr in member access expr", 10, 22);
        validateError(compileResult, index++, "invalid operation: type 'string' does not support member access",
                10, 24);
        validateError(compileResult, index++, "missing key expr in member access expr", 10, 31);
        validateError(compileResult, index++, "incompatible types: expected 'typedesc<readonly>', found " +
                "'typedesc<IntArray>'", 21, 28);
        validateError(compileResult, index++, "incompatible types: expected 'typedesc<(string[]|boolean[])>', found " +
                "'typedesc<ImmutableIntArray>'", 22, 38);
        validateError(compileResult, index++, "incompatible types: expected 'typedesc<string>', " +
                "found 'typedesc<ImmutableIntArray>'", 23, 26);
        validateError(compileResult, index++, "incompatible types: expected 'typedesc<string[]>', found " +
                "'typedesc<\"foo\">'", 24, 28);
        validateError(compileResult, index++, "incompatible types: expected 'typedesc<string>', found " +
                "'typedesc<\"foo\"|1>'", 25, 26);
        validateError(compileResult, index++, "incompatible types: expected 'typedesc<string>', found " +
                "'typedesc<function (int) returns (string)>'", 26, 26);
        validateError(compileResult, index++, "incompatible types: expected 'typedesc<function (int) returns (string)" +
                ">', found 'typedesc<function () returns (string)>'", 27, 51);
        Assert.assertEquals(compileResult.getErrorCount(), index);
    }

    @Test(description = "Test basics types")
    public void testBasicTypes() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testBasicTypes");
        Assert.assertEquals(returns.size(), 5);
        Assert.assertEquals(returns.get(0).toString(), "typedesc int");
        Assert.assertEquals(returns.get(1).toString(), "typedesc string");
        Assert.assertEquals(returns.get(2).toString(), "typedesc float");
        Assert.assertEquals(returns.get(3).toString(), "typedesc boolean");
        Assert.assertEquals(returns.get(4).toString(), "typedesc byte");
    }

    @Test(description = "Test buildin ref types")
    public void testRefTypes() {
        BRunUtil.invoke(result, "testRefTypes");
    }

    @Test(description = "Test object types")
    public void testObjectTypes() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testObjectTypes");
        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(returns.get(0).toString(), "typedesc Person");
        Assert.assertTrue(returns.get(1) instanceof BTypedesc);
        Assert.assertEquals(TypeTags.OBJECT_TYPE_TAG, ((BTypedesc) returns.get(1)).getDescribingType().getTag());
    }

    @Test(description = "Test array types")
    public void testArrayTypes() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testArrayTypes");
        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(returns.get(0).toString(), "typedesc intArray");
        Assert.assertEquals(returns.get(1).toString(), "typedesc intArrayArray");
    }

    @Test(description = "Test tuple/union types")
    public void testTupleUnionTypes() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testTupleUnionTypes");
        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(returns.get(0).toString(), "typedesc stringOrPerson");
        Assert.assertEquals(returns.get(1).toString(), "typedesc intOrString");
    }

    @Test(description = "Test tuples with expressions")
    public void testTuplesWithExpressions() {
        Object returns = BRunUtil.invoke(result, "testTuplesWithExpressions");
        Assert.assertEquals(returns.toString(), "typedesc [string,int,[string,string,string],string,int]");
    }

    @Test(description = "Test Record types")
    public void testRecordTypes() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testRecordTypes");
        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(returns.get(0).toString(), "typedesc RecordA");
        Assert.assertTrue(returns.get(1) instanceof BTypedesc);
        Assert.assertEquals(TypeTags.RECORD_TYPE_TAG, ((BTypedesc) returns.get(1)).getDescribingType().getTag());
    }

    @Test(description = "Test any to typedesc cast")
    public void testAnyToTypedescCast() {
        Object returns = BRunUtil.invoke(result, "testAnyToTypedesc");
        Assert.assertEquals(returns.toString(), "typedesc int");
    }

    @Test(description = "Test module level typedesc definition")
    public void testModuleLevelTypeDesc() {
        Object returns = BRunUtil.invoke(result, "testModuleLevelTypeDesc");
        Assert.assertTrue(returns instanceof BTypedesc);
        Assert.assertEquals(TypeTags.JSON_TAG, ((BTypedesc) returns).getDescribingType().getTag());
    }

    @Test(description = "Test method level typedesc definition")
    public void testMethodLevelTypeDesc() {
        Object returns = BRunUtil.invoke(result, "testMethodLevelTypeDesc");
        Assert.assertTrue(returns instanceof BTypedesc);
        Assert.assertEquals(TypeTags.JSON_TAG, ((BTypedesc) returns).getDescribingType().getTag());
    }

    @Test(description = "Test custom error typedesc")
    public void testCustomErrorTypeDesc() {
        Object returns = BRunUtil.invoke(result, "testCustomErrorTypeDesc");
    }

    @Test
    public void testBasicTypesWithoutTypedescConstraint() {
        BRunUtil.invoke(result, "testBasicTypesWithoutTypedescConstraint");
    }

    @Test
    public void testRefTypesWithoutTypedescConstraint() {
        BRunUtil.invoke(result, "testRefTypesWithoutTypedescConstraint");
    }

    @Test
    public void testObjectTypesWithoutTypedescConstraint() {
        BRunUtil.invoke(result, "testObjectTypesWithoutTypedescConstraint");
    }

    @Test
    public void testArrayTypesWithoutTypedescConstraint() {
        BRunUtil.invoke(result, "testArrayTypesWithoutTypedescConstraint");
    }

    @Test
    public void testRecordTypesWithoutTypedescConstraint() {
        BRunUtil.invoke(result, "testRecordTypesWithoutTypedescConstraint");
    }

    @Test
    public void testTuplesWithExpressionsWithoutTypedescConstraint() {
        BRunUtil.invoke(result, "testTuplesWithExpressionsWithoutTypedescConstraint");
    }

    @Test
    public void testAnyToTypedescWithoutConstraint() {
        BRunUtil.invoke(result, "testAnyToTypedescWithoutConstraint");
    }

    @Test
    public void testModuleLevelTypeDescWithoutConstraint() {
        BRunUtil.invoke(result, "testModuleLevelTypeDescWithoutConstraint");
    }

    @Test
    public void testMethodLevelTypeDescWithoutConstraint() {
        BRunUtil.invoke(result, "testMethodLevelTypeDescWithoutConstraint");
    }

    @Test
    public void testCustomErrorTypeDescWithoutConstraint() {
        BRunUtil.invoke(result, "testCustomErrorTypeDescWithoutConstraint");
    }

    @Test
    public void testTypeDefWithIntersectionTypeDescAsTypedesc() {
        BRunUtil.invoke(result, "testTypeDefWithIntersectionTypeDescAsTypedesc");
    }

    @Test
    public void testFiniteTypeAsTypedesc() {
        BRunUtil.invoke(result, "testFiniteTypeAsTypedesc");
    }

    @Test
    public void testTypeDefWithFunctionTypeDescAsTypedesc() {
        BRunUtil.invoke(result, "testTypeDefWithFunctionTypeDescAsTypedesc");
    }

    @Test
    public void testTypeDefWithIntersectionTypeDescAsTypedescNegative() {
        BRunUtil.invoke(result, "testTypeDefWithIntersectionTypeDescAsTypedescNegative");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
