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

import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BTypeDescValue;
import org.ballerinalang.model.values.BValue;
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

    @Test(description = "Test basics types")
    public void testNegative() {
        final CompileResult compile = BCompileUtil.compile("test-src/types/typedesc/typedesc_negative.bal");
        Assert.assertEquals(compile.getErrorCount(), 2);
        BAssertUtil.validateError(compile, 0, "variable assignment is required", 2, 5);
        BAssertUtil.validateError(compile, 1, "undefined symbol 'i'", 7, 9);
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
        BValue[] returns = BRunUtil.invoke(result, "testRefTypes");
        Assert.assertEquals(returns.length, 4);
        Assert.assertEquals(returns[0].stringValue(), "xml");
        Assert.assertEquals(returns[1].stringValue(), "json");
        Assert.assertEquals(returns[2].stringValue(), "map");
        Assert.assertEquals(returns[3].stringValue(), "table<Employee>");
    }


    @Test(description = "Test object types")
    public void testObjectTypes() {
        BValue[] returns = BRunUtil.invoke(result, "testObjectTypes");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "Person");
        Assert.assertTrue(returns[1] instanceof BTypeDescValue);
        Assert.assertTrue(((BTypeDescValue) returns[1]).value().getTag() == TypeTags.OBJECT_TYPE_TAG);
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
        Assert.assertEquals(returns[0].stringValue(), "[string,int,string[],string,int]");
    }

    @Test(description = "Test Record types")
    public void testRecordTypes() {
        BValue[] returns = BRunUtil.invoke(result, "testRecordTypes");
        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(returns[0].stringValue(), "RecordA");
        Assert.assertTrue(returns[1] instanceof BTypeDescValue);
        Assert.assertTrue(((BTypeDescValue) returns[1]).value().getTag() == TypeTags.RECORD_TYPE_TAG);
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
}
