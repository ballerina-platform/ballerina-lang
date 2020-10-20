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

import org.ballerinalang.core.model.types.TypeTags;
import org.ballerinalang.core.model.values.BTypeDescValue;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class contains typedesc type related test cases.
 */
public class TypedescTests {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/typedesc/typedesc_positive.bal");
    }

    @Test(description = "Test basics types", groups = { "disableOnOldParser" })
    public void testNegative() {
        final CompileResult compileResult = BCompileUtil.compile("test-src/types/typedesc/typedesc_negative.bal");
        int index = 0;
        BAssertUtil.validateError(compileResult, index++, "missing identifier", 2, 8);
        BAssertUtil.validateError(compileResult, index++, "undefined symbol 'i'", 7, 9);
        BAssertUtil.validateError(compileResult, index++,
                "invalid operation: type 'byte' does not support indexing", 9, 18);
        BAssertUtil.validateError(compileResult, index++,
                "missing key expr in member access expr", 9, 23);
        BAssertUtil.validateError(compileResult, index++,
                "invalid operation: type 'int' does not support indexing", 10, 18);
        BAssertUtil.validateError(compileResult, index++,
                "missing key expr in member access expr", 10, 22);
        BAssertUtil.validateError(compileResult, index++,
                "invalid operation: type 'string' does not support indexing", 10, 24);
        BAssertUtil.validateError(compileResult, index++,
                "missing key expr in member access expr", 10, 31);
        Assert.assertEquals(compileResult.getErrorCount(), index);
    }

    @Test(description = "Test basics types")
    public void testBasicTypes() {
        BValue[] returns = BRunUtil.invoke(result, "testBasicTypes");
        Assert.assertEquals(returns.length, 5);
        Assert.assertEquals(returns[0].stringValue(), "int");
        Assert.assertEquals(returns[1].stringValue(), "string");
        Assert.assertEquals(returns[2].stringValue(), "float");
        Assert.assertEquals(returns[3].stringValue(), "boolean");
        Assert.assertEquals(returns[4].stringValue(), "byte");
    }

    @Test(description = "Test buildin ref types")
    public void testRefTypes() {
        BRunUtil.invoke(result, "testRefTypes");
    }

    @Test(description = "Test object types")
    public void testObjectTypes() {
        BValue[] returns = BRunUtil.invoke(result, "testObjectTypes");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "Person");
        Assert.assertTrue(returns[1] instanceof BTypeDescValue);
        Assert.assertEquals(TypeTags.OBJECT_TYPE_TAG, ((BTypeDescValue) returns[1]).value().getTag());
    }

    @Test(description = "Test array types")
    public void testArrayTypes() {
        BValue[] returns = BRunUtil.invoke(result, "testArrayTypes");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "int[]");
        Assert.assertEquals(returns[1].stringValue(), "int[][]");
    }

    @Test(description = "Test tuple/union types")
    public void testTupleUnionTypes() {
        BValue[] returns = BRunUtil.invoke(result, "testTupleUnionTypes");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "[string,Person]");
        Assert.assertEquals(returns[1].stringValue(), "int|string");
    }

    @Test(description = "Test tuples with expressions")
    public void testTuplesWithExpressions() {
        BValue[] returns = BRunUtil.invoke(result, "testTuplesWithExpressions");
        Assert.assertEquals(returns[0].stringValue(), "[string,int,[string,string,string],string,int]");
    }

    @Test(description = "Test Record types")
    public void testRecordTypes() {
        BValue[] returns = BRunUtil.invoke(result, "testRecordTypes");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "RecordA");
        Assert.assertTrue(returns[1] instanceof BTypeDescValue);
        Assert.assertEquals(TypeTags.RECORD_TYPE_TAG, ((BTypeDescValue) returns[1]).value().getTag());
    }

    @Test(description = "Test any to typedesc cast")
    public void testAnyToTypedescCast() {
        BValue[] returns = BRunUtil.invoke(result, "testAnyToTypedesc");
        Assert.assertEquals(returns[0].stringValue(), "int");
    }

    @Test(description = "Test module level typedesc definition")
    public void testModuleLevelTypeDesc() {
        BValue[] returns = BRunUtil.invoke(result, "testModuleLevelTypeDesc");
        Assert.assertTrue(returns[0] instanceof BTypeDescValue);
        Assert.assertEquals(TypeTags.JSON_TAG, ((BTypeDescValue) returns[0]).value().getTag());
    }

    @Test(description = "Test method level typedesc definition")
    public void testMethodLevelTypeDesc() {
        BValue[] returns = BRunUtil.invoke(result, "testMethodLevelTypeDesc");
        Assert.assertTrue(returns[0] instanceof BTypeDescValue);
        Assert.assertEquals(TypeTags.JSON_TAG, ((BTypeDescValue) returns[0]).value().getTag());
    }

    @Test(description = "Test custom error typedesc")
    public void testCustomErrorTypeDesc() {
        BValue[] returns = BRunUtil.invoke(result, "testCustomErrorTypeDesc");
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
}
