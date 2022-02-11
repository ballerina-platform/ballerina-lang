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
package org.ballerinalang.test.bala.constant;

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.JvmRunUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Test cases for reading constants.
 */
public class SimpleConstantInBalaTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project");
        compileResult = BCompileUtil.compile("test-src/bala/test_bala/constant/simple-literal-constant.bal");
    }

    @Test
    public void testNilWithoutType() {
        Object returns = JvmRunUtil.invoke(compileResult, "testNilWithoutType");
        Assert.assertNull(returns);
    }

    @Test
    public void testNilWithType() {
        Object returns = JvmRunUtil.invoke(compileResult, "testNilWithType");
        Assert.assertNull(returns);
    }

    @Test
    public void testConstWithTypeInReturn() {
        Object returns = JvmRunUtil.invoke(compileResult, "testConstWithTypeInReturn");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "Ballerina");
    }

    @Test
    public void testConstWithoutTypeInReturn() {
        Object returns = JvmRunUtil.invoke(compileResult, "testConstWithoutTypeInReturn");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "Ballerina");
    }

    @Test
    public void testConstWithTypeAsParam() {
        Object returns = JvmRunUtil.invoke(compileResult, "testConstWithTypeAsParam");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "Ballerina");
    }

    @Test
    public void testConstWithoutTypeAsParam() {
        Object returns = JvmRunUtil.invoke(compileResult, "testConstWithoutTypeAsParam");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "Ballerina");
    }

    @Test
    public void testConstInRecord() {
        Object returns = JvmRunUtil.invoke(compileResult, "testConstInRecord");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "Ballerina");
    }

    @Test
    public void testConstWithTypeAssignmentToGlobalVariable() {
        Object returns = JvmRunUtil.invoke(compileResult, "testConstWithTypeAssignmentToGlobalVariable");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "Ballerina");
    }

    @Test
    public void testConstWithTypeAssignmentToLocalVariable() {
        Object returns = JvmRunUtil.invoke(compileResult, "testConstWithTypeAssignmentToLocalVariable");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "Ballerina");
    }

    @Test
    public void testConstWithoutTypeAssignmentToGlobalVariable() {
        Object returns = JvmRunUtil.invoke(compileResult, "testConstWithoutTypeAssignmentToGlobalVariable");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "Ballerina");
    }

    @Test
    public void testConstWithoutTypeAssignmentToLocalVariable() {
        Object returns = JvmRunUtil.invoke(compileResult, "testConstWithoutTypeAssignmentToLocalVariable");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "Ballerina");
    }

    @Test
    public void testConstWithTypeConcat() {
        Object returns = JvmRunUtil.invoke(compileResult, "testConstWithTypeConcat");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "Ballerina rocks");
    }

    @Test
    public void testConstWithoutTypeConcat() {
        Object returns = JvmRunUtil.invoke(compileResult, "testConstWithoutTypeConcat");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "Ballerina rocks");
    }

    @Test
    public void testTypeConstants() {
        Object returns = JvmRunUtil.invoke(compileResult, "testTypeConstants");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "GET");
    }

    @Test
    public void testConstWithTypeAssignmentToType() {
        Object returns = JvmRunUtil.invoke(compileResult, "testConstWithTypeAssignmentToType");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "GET");
    }

    @Test
    public void testConstWithoutTypeAssignmentToType() {
        Object returns = JvmRunUtil.invoke(compileResult, "testConstWithoutTypeAssignmentToType");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "GET");
    }

    @Test
    public void testConstAndTypeComparison() {
        Object returns = JvmRunUtil.invoke(compileResult, "testConstAndTypeComparison");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testTypeConstAsParam() {
        Object returns = JvmRunUtil.invoke(compileResult, "testTypeConstAsParam");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testEqualityWithConstWithType() {
        Object returns = JvmRunUtil.invoke(compileResult, "testEqualityWithConstWithType");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testConstWithTypeInCondition() {
        Object returns = JvmRunUtil.invoke(compileResult, "testConstWithTypeInCondition");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testConstWithoutTypeInCondition() {
        Object returns = JvmRunUtil.invoke(compileResult, "testConstWithoutTypeInCondition");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testBooleanWithType() {
        Object returns = JvmRunUtil.invoke(compileResult, "testBooleanWithType");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    @Test
    public void testBooleanWithoutType() {
        Object returns = JvmRunUtil.invoke(compileResult, "testBooleanWithoutType");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testIntWithType() {
        Object returns = JvmRunUtil.invoke(compileResult, "testIntWithType");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns, 40L);
    }

    @Test
    public void testIntWithoutType() {
        Object returns = JvmRunUtil.invoke(compileResult, "testIntWithoutType");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns, 20L);
    }

    // Note - Byte without type cannot be specified.
    @Test
    public void testByteWithType() {
        Object returns = JvmRunUtil.invoke(compileResult, "testByteWithType");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns, 240);
    }

    @Test
    public void testFloatWithType() {
        Object returns = JvmRunUtil.invoke(compileResult, "testFloatWithType");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns, 4.0);
    }

    @Test
    public void testFloatWithoutType() {
        Object returns = JvmRunUtil.invoke(compileResult, "testFloatWithoutType");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns, 2.0);
    }

    // Note - Decimal without type cannot be specified.
    @Test
    public void testDecimalWithType() {
        Object returns = JvmRunUtil.invoke(compileResult, "testDecimalWithType");
        Assert.assertNotNull(returns);
        BigDecimal expected = new BigDecimal(50.0, MathContext.DECIMAL128);
        Assert.assertEquals(returns, ValueCreator.createDecimalValue(expected));
    }

    @Test
    public void testStringWithType() {
        Object returns = JvmRunUtil.invoke(compileResult, "testStringWithType");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "Ballerina is awesome");
    }

    @Test
    public void testStringWithoutType() {
        Object returns = JvmRunUtil.invoke(compileResult, "testStringWithoutType");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "Ballerina rocks");
    }

    @Test
    public void testFloatAsFiniteType() {
        Object result = JvmRunUtil.invoke(compileResult, "testFloatAsFiniteType");
        BArray returns = (BArray) result;
        Assert.assertNotNull(returns.get(0));
        Assert.assertNotNull(returns.get(1));
        Assert.assertEquals(returns.get(0), 2.0);
        Assert.assertEquals(returns.get(1), 4.0);
    }

    @Test
    public void testConstInMapKey() {
        Object returns = JvmRunUtil.invoke(compileResult, "testConstInMapKey");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "value");
    }

    @Test
    public void testConstInMapValue() {
        Object returns = JvmRunUtil.invoke(compileResult, "testConstInMapValue");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "value");
    }

    @Test
    public void testConstInJsonKey() {
        Object returns = JvmRunUtil.invoke(compileResult, "testConstInJsonKey");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "value");
    }

    @Test
    public void testConstInJsonValue() {
        Object returns = JvmRunUtil.invoke(compileResult, "testConstInJsonValue");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "value");
    }

    @Test
    public void testBooleanConstInUnion() {
        Object returns = JvmRunUtil.invoke(compileResult, "testBooleanConstInUnion");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    @Test
    public void testIntConstInUnion() {
        Object returns = JvmRunUtil.invoke(compileResult, "testIntConstInUnion");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns, 40L);
    }

    @Test
    public void testByteConstInUnion() {
        Object returns = JvmRunUtil.invoke(compileResult, "testByteConstInUnion");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns, 240);
    }

    @Test
    public void testFloatConstInUnion() {
        Object returns = JvmRunUtil.invoke(compileResult, "testFloatConstInUnion");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns, 4.0);
    }

    @Test
    public void testStringConstInUnion() {
        Object returns = JvmRunUtil.invoke(compileResult, "testStringConstInUnion");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "Ballerina is awesome");
    }

    @Test
    public void testBooleanConstInTuple() {
        Object returns = JvmRunUtil.invoke(compileResult, "testBooleanConstInTuple");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    @Test
    public void testIntConstInTuple() {
        Object returns = JvmRunUtil.invoke(compileResult, "testIntConstInTuple");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns, 40L);
    }

    @Test
    public void testByteConstInTuple() {
        Object returns = JvmRunUtil.invoke(compileResult, "testByteConstInTuple");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns, 240);
    }

    @Test
    public void testFloatConstInTuple() {
        Object returns = JvmRunUtil.invoke(compileResult, "testFloatConstInTuple");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns, 4.0);
    }

    @Test
    public void testStringConstInTuple() {
        Object returns = JvmRunUtil.invoke(compileResult, "testStringConstInTuple");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "Ballerina is awesome");
    }

    @Test
    public void testProperSubset() {
        Object returns = JvmRunUtil.invoke(compileResult, "testProperSubset");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "D");
    }

    @Test
    public void testBuiltinFunctionInvocation() {
        Object returns = JvmRunUtil.invoke(compileResult, "testBuiltinFunctionInvocation");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testBuiltinFunctionInvocationOnArrayElement() {
        Object returns = JvmRunUtil.invoke(compileResult, "testBuiltinFunctionInvocationOnArrayElement");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testBuiltinFunctionInvocationOnField() {
        Object returns = JvmRunUtil.invoke(compileResult, "testBuiltinFunctionInvocationOnField");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testLabeling() {
        Object returns = JvmRunUtil.invoke(compileResult, "testLabeling");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "Ballerina");
    }
}
