/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.jvm;

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for testing functionality of type test expressions on JBallerina.
 *
 * @since 0.995.0
 */
public class TypeTestExprTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/jvm/type-test-expr.bal");
    }

    @Test
    public void testValueTypeInUnion() {
        Object returns = BRunUtil.invoke(compileResult, "testValueTypeInUnion");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "string");
    }

    @Test
    public void testUnionTypeInUnion() {
        Object returns = BRunUtil.invoke(compileResult, "testUnionTypeInUnion");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "numeric");
    }

    @Test
    public void testNestedTypeCheck() {
        Object val = BRunUtil.invoke(compileResult, "testNestedTypeCheck");
        BArray returns = (BArray) val;
        Assert.assertEquals(returns.size(), 3);
        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertTrue(returns.get(2) instanceof BString);
        Assert.assertEquals(returns.get(0).toString(), "boolean");
        Assert.assertEquals(returns.get(1).toString(), "int");
        Assert.assertEquals(returns.get(2).toString(), "string");
    }

    @Test
    public void testTypeInAny() {
        Object returns = BRunUtil.invoke(compileResult, "testTypeInAny");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "string value: This is working");
    }

    @Test
    public void testNilType() {
        Object returns = BRunUtil.invoke(compileResult, "testNilType");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "nil");
    }

    @Test
    public void testSimpleRecordTypes_1() {
        Object returns = BRunUtil.invoke(compileResult, "testSimpleRecordTypes_1");

        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "a is A1");
    }

    @Test
    public void testSimpleRecordTypes_2() {
        Object val = BRunUtil.invoke(compileResult, "testSimpleRecordTypes_2");
        BArray returns = (BArray) val;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertSame(returns.get(0).getClass(), Boolean.class);
        Assert.assertSame(returns.get(1).getClass(), Boolean.class);
        Assert.assertTrue((Boolean) returns.get(0));
        Assert.assertTrue((Boolean) returns.get(1));
    }

    @Test
    public void testSimpleRecordTypes_3() {
        Object val = BRunUtil.invoke(compileResult, "testSimpleRecordTypes_3");
        BArray returns = (BArray) val;
        Assert.assertEquals(returns.size(), 2);
        Assert.assertSame(returns.get(0).getClass(), Boolean.class);
        Assert.assertSame(returns.get(0).getClass(), Boolean.class);
        Assert.assertTrue((Boolean) returns.get(0));
        Assert.assertTrue((Boolean) returns.get(1));
    }

    @Test
    public void testAnyJsonTypes() {
        Object val = BRunUtil.invoke(compileResult, "testAnyJsonTypes");
        BArray returns = (BArray) val;
        Assert.assertEquals(returns.size(), 4);
        Assert.assertSame(returns.get(0).getClass(), Boolean.class);
        Assert.assertSame(returns.get(1).getClass(), Boolean.class);
        Assert.assertSame(returns.get(2).getClass(), Boolean.class);
        Assert.assertSame(returns.get(3).getClass(), Boolean.class);
        Assert.assertTrue((Boolean) returns.get(0));
        Assert.assertTrue((Boolean) returns.get(1));
        Assert.assertTrue((Boolean) returns.get(2));
        Assert.assertTrue((Boolean) returns.get(3));
    }

    @Test
    public void testIsLikeForTupleWithRestDescriptor() {
        BRunUtil.invoke(compileResult, "testIsLikeForTupleWithRestDescriptor");
    }

    @Test
    public void testIsLikeForTupleWithOutRestDescriptor() {
        BRunUtil.invoke(compileResult, "testIsLikeForTupleWithOutRestDescriptor");
    }

    @Test
    public void testTypeTestingInReadonlyRecord() {
        BRunUtil.invoke(compileResult, "testTypeTestingInReadonlyRecord");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
