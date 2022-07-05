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

package org.ballerinalang.test.types.constant;

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Constant test cases.
 */
public class SimpleConstantTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/constant/simple-literal-constant.bal");
    }

    @Test
    public void testNilWithoutType() {
        Object returns = BRunUtil.invoke(compileResult, "testNilWithoutType");
        Assert.assertNull(returns);
    }

    @Test
    public void testNilWithType() {
        Object returns = BRunUtil.invoke(compileResult, "testNilWithType");
        Assert.assertNull(returns);
    }

    @Test
    public void testConstWithTypeInReturn() {
        Object returns = BRunUtil.invoke(compileResult, "testConstWithTypeInReturn");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "Ballerina");
    }

    @Test
    public void testConstWithoutTypeInReturn() {
        Object returns = BRunUtil.invoke(compileResult, "testConstWithoutTypeInReturn");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "Ballerina");
    }

    @Test
    public void testConstWithTypeAsParam() {
        Object returns = BRunUtil.invoke(compileResult, "testConstWithTypeAsParam");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "Ballerina");
    }

    @Test
    public void testConstWithoutTypeAsParam() {
        Object returns = BRunUtil.invoke(compileResult, "testConstWithoutTypeAsParam");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "Ballerina");
    }

    @Test
    public void testConstInRecord() {
        Object returns = BRunUtil.invoke(compileResult, "testConstInRecord");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "Ballerina");
    }

    @Test
    public void testConstWithTypeAssignmentToGlobalVariable() {
        Object returns = BRunUtil.invoke(compileResult, "testConstWithTypeAssignmentToGlobalVariable");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "Ballerina");
    }

    @Test
    public void testConstWithTypeAssignmentToLocalVariable() {
        Object returns = BRunUtil.invoke(compileResult, "testConstWithTypeAssignmentToLocalVariable");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "Ballerina");
    }

    @Test
    public void testConstWithoutTypeAssignmentToGlobalVariable() {
        Object returns = BRunUtil.invoke(compileResult, "testConstWithoutTypeAssignmentToGlobalVariable");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "Ballerina");
    }

    @Test
    public void testConstWithoutTypeAssignmentToLocalVariable() {
        Object returns = BRunUtil.invoke(compileResult, "testConstWithoutTypeAssignmentToLocalVariable");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "Ballerina");
    }

    @Test
    public void testConstWithTypeConcat() {
        Object returns = BRunUtil.invoke(compileResult, "testConstWithTypeConcat");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "Ballerina rocks");
    }

    @Test
    public void testConstWithoutTypeConcat() {
        Object returns = BRunUtil.invoke(compileResult, "testConstWithoutTypeConcat");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "Ballerina rocks");
    }

    @Test
    public void testTypeConstants() {
        Object returns = BRunUtil.invoke(compileResult, "testTypeConstants");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "GET");
    }

    @Test
    public void testConstWithTypeAssignmentToType() {
        Object returns = BRunUtil.invoke(compileResult, "testConstWithTypeAssignmentToType");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "GET");
    }

    @Test
    public void testConstWithoutTypeAssignmentToType() {
        Object returns = BRunUtil.invoke(compileResult, "testConstWithoutTypeAssignmentToType");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "GET");
    }

    @Test
    public void testConstAndTypeComparison() {
        Object returns = BRunUtil.invoke(compileResult, "testConstAndTypeComparison");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testTypeConstAsParam() {
        Object returns = BRunUtil.invoke(compileResult, "testTypeConstAsParam");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testEqualityWithConstWithType() {
        Object returns = BRunUtil.invoke(compileResult, "testEqualityWithConstWithType");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testConstWithTypeInCondition() {
        Object returns = BRunUtil.invoke(compileResult, "testConstWithTypeInCondition");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testConstWithoutTypeInCondition() {
        Object returns = BRunUtil.invoke(compileResult, "testConstWithoutTypeInCondition");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testBooleanWithType() {
        Object returns = BRunUtil.invoke(compileResult, "testBooleanWithType");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    @Test
    public void testBooleanWithoutType() {
        Object returns = BRunUtil.invoke(compileResult, "testBooleanWithoutType");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testIntWithType() {
        Object returns = BRunUtil.invoke(compileResult, "testIntWithType");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns, 40L);
    }

    @Test
    public void testIntWithoutType() {
        Object returns = BRunUtil.invoke(compileResult, "testIntWithoutType");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns, 20L);
    }

    // Note - Byte without type cannot be specified.
    @Test
    public void testByteWithType() {
        Object returns = BRunUtil.invoke(compileResult, "testByteWithType");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns, 240);
    }

    @Test
    public void testFloatWithType() {
        Object returns = BRunUtil.invoke(compileResult, "testFloatWithType");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns, 4.0);
    }

    @Test
    public void testFloatWithoutType() {
        Object returns = BRunUtil.invoke(compileResult, "testFloatWithoutType");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns, 2.0);
    }

    // Note - Decimal without type cannot be specified.
    @Test
    public void testDecimalWithType() {
        Object returns = BRunUtil.invoke(compileResult, "testDecimalWithType");
        Assert.assertNotNull(returns);
        BigDecimal expected = new BigDecimal(50.0, MathContext.DECIMAL128);
        Assert.assertEquals(returns, ValueCreator.createDecimalValue(expected));
    }

    @Test
    public void testStringWithType() {
        Object returns = BRunUtil.invoke(compileResult, "testStringWithType");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "Ballerina is awesome");
    }

    @Test
    public void testStringWithoutType() {
        Object returns = BRunUtil.invoke(compileResult, "testStringWithoutType");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "Ballerina rocks");
    }

    @Test
    public void testFloatAsFiniteType() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testFloatAsFiniteType");
        Assert.assertNotNull(returns.get(0));
        Assert.assertNotNull(returns.get(1));
        Assert.assertEquals(returns.get(0), 2.0);
        Assert.assertEquals(returns.get(1), 4.0);
    }

    @Test
    public void testConstInMapKey() {
        Object returns = BRunUtil.invoke(compileResult, "testConstInMapKey");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "value");
    }

    @Test
    public void testConstInMapValue() {
        Object returns = BRunUtil.invoke(compileResult, "testConstInMapValue");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "value");
    }

    @Test
    public void testConstInJsonKey() {
        Object returns = BRunUtil.invoke(compileResult, "testConstInJsonKey");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "value");
    }

    @Test
    public void testConstInJsonValue() {
        Object returns = BRunUtil.invoke(compileResult, "testConstInJsonValue");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "value");
    }

    @Test
    public void testBooleanConstInUnion() {
        Object returns = BRunUtil.invoke(compileResult, "testBooleanConstInUnion");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    @Test
    public void testIntConstInUnion() {
        Object returns = BRunUtil.invoke(compileResult, "testIntConstInUnion");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns, 40L);
    }

    @Test
    public void testByteConstInUnion() {
        Object returns = BRunUtil.invoke(compileResult, "testByteConstInUnion");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns, 240);
    }

    @Test
    public void testFloatConstInUnion() {
        Object returns = BRunUtil.invoke(compileResult, "testFloatConstInUnion");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns, 4.0);
    }

    @Test
    public void testDecimalConstInUnion() {
        Object returns = BRunUtil.invoke(compileResult, "testDecimalConstInUnion");
        Assert.assertNotNull(returns);
        BigDecimal expected = new BigDecimal(50.0, MathContext.DECIMAL128);
        Assert.assertEquals(returns, ValueCreator.createDecimalValue(expected));
    }

    @Test
    public void testStringConstInUnion() {
        Object returns = BRunUtil.invoke(compileResult, "testStringConstInUnion");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "Ballerina is awesome");
    }

    @Test
    public void testBooleanConstInTuple() {
        Object returns = BRunUtil.invoke(compileResult, "testBooleanConstInTuple");
        Assert.assertNotNull(returns);
        Assert.assertFalse((Boolean) returns);
    }

    @Test
    public void testIntConstInTuple() {
        Object returns = BRunUtil.invoke(compileResult, "testIntConstInTuple");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns, 40L);
    }

    @Test
    public void testByteConstInTuple() {
        Object returns = BRunUtil.invoke(compileResult, "testByteConstInTuple");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns, 240);
    }

    @Test
    public void testFloatConstInTuple() {
        Object returns = BRunUtil.invoke(compileResult, "testFloatConstInTuple");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns, 4.0);
    }

    @Test
    public void testDecimalConstInTuple() {
        Object returns = BRunUtil.invoke(compileResult, "testDecimalConstInTuple");
        Assert.assertNotNull(returns);
        BigDecimal expected = new BigDecimal(50.0, MathContext.DECIMAL128);
        Assert.assertEquals(returns, ValueCreator.createDecimalValue(expected));
    }

    @Test
    public void testStringConstInTuple() {
        Object returns = BRunUtil.invoke(compileResult, "testStringConstInTuple");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "Ballerina is awesome");
    }

    @Test
    public void testProperSubset() {
        Object returns = BRunUtil.invoke(compileResult, "testProperSubset");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "D");
    }

    @Test
    public void testBuiltinFunctionInvocation() {
        Object returns = BRunUtil.invoke(compileResult, "testBuiltinFunctionInvocation");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testBuiltinFunctionInvocationOnArrayElement() {
        Object returns = BRunUtil.invoke(compileResult, "testBuiltinFunctionInvocationOnArrayElement");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testBuiltinFunctionInvocationOnField() {
        Object returns = BRunUtil.invoke(compileResult, "testBuiltinFunctionInvocationOnField");
        Assert.assertNotNull(returns);
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testLabeling() {
        Object returns = BRunUtil.invoke(compileResult, "testLabeling");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "Ballerina");
    }

    @Test
    public void testBooleanConcat() {
        Object returns = BRunUtil.invoke(compileResult, "testBooleanConcat");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "true rocks");
    }

    @Test
    public void testIntConcat() {
        Object returns = BRunUtil.invoke(compileResult, "testIntConcat");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "24 rocks");
    }

    @Test
    public void testByteConcat() {
        Object returns = BRunUtil.invoke(compileResult, "testByteConcat");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "12 rocks");
    }

    @Test
    public void testFloatConcat() {
        Object returns = BRunUtil.invoke(compileResult, "testFloatConcat");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "25.5 rocks");
    }

    @Test
    public void testDecimalConcat() {
        Object returns = BRunUtil.invoke(compileResult, "testDecimalConcat");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.toString(), "25.5 rocks");
    }

    @Test
    public void testDecimalConstsWithTypeReferenceTypeDescriptorsDefinedWithNumericLiterals() {
        BRunUtil.invoke(compileResult,
                        "testDecimalConstsWithTypeReferenceTypeDescriptorsDefinedWithNumericLiterals");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}
