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

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
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
        BValue[] returns = BRunUtil.invoke(compileResult, "testValueTypeInUnion");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "string");
    }

    @Test
    public void testUnionTypeInUnion() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testUnionTypeInUnion");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "numeric");
    }

    @Test
    public void testNestedTypeCheck() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNestedTypeCheck");
        Assert.assertEquals(returns.length, 3);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "boolean");
        Assert.assertEquals(returns[1].stringValue(), "int");
        Assert.assertEquals(returns[2].stringValue(), "string");
    }

    @Test
    public void testTypeInAny() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testTypeInAny");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "string value: This is working");
    }

    @Test
    public void testNilType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNilType");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "nil");
    }

    @Test
    public void testSimpleRecordTypes_1() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleRecordTypes_1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertEquals(returns[0].stringValue(), "a is A1");
    }

    @Test
    public void testSimpleRecordTypes_2() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleRecordTypes_2");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
    }

    @Test
    public void testSimpleRecordTypes_3() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSimpleRecordTypes_3");
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
    }

    @Test
    public void testAnyJsonTypes() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAnyJsonTypes");
        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        Assert.assertSame(returns[2].getClass(), BBoolean.class);
        Assert.assertSame(returns[3].getClass(), BBoolean.class);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[1]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[2]).booleanValue());
        Assert.assertTrue(((BBoolean) returns[3]).booleanValue());
    }

    @Test
    public void testIsLikeForTupleWithRestDescriptor() {
        BRunUtil.invoke(compileResult, "testIsLikeForTupleWithRestDescriptor");
    }

    @Test
    public void testIsLikeForTupleWithOutRestDescriptor() {
        BRunUtil.invoke(compileResult, "testIsLikeForTupleWithOutRestDescriptor");
    }
}
