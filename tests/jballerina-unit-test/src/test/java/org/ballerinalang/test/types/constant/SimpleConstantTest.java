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

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BByte;
import org.ballerinalang.core.model.values.BDecimal;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Constant test cases.
 */
public class SimpleConstantTest {

    private static CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/constant/simple-literal-constant.bal");
    }

    @Test
    public void testNilWithoutType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNilWithoutType");
        Assert.assertNull(returns[0]);
    }

    @Test
    public void testNilWithType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNilWithType");
        Assert.assertNull(returns[0]);
    }

    @Test
    public void testConstWithTypeInReturn() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstWithTypeInReturn");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina");
    }

    @Test
    public void testConstWithoutTypeInReturn() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstWithoutTypeInReturn");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina");
    }

    @Test
    public void testConstWithTypeAsParam() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstWithTypeAsParam");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina");
    }

    @Test
    public void testConstWithoutTypeAsParam() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstWithoutTypeAsParam");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina");
    }

    @Test
    public void testConstInRecord() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstInRecord");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina");
    }

    @Test
    public void testConstWithTypeAssignmentToGlobalVariable() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstWithTypeAssignmentToGlobalVariable");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina");
    }

    @Test
    public void testConstWithTypeAssignmentToLocalVariable() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstWithTypeAssignmentToLocalVariable");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina");
    }

    @Test
    public void testConstWithoutTypeAssignmentToGlobalVariable() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstWithoutTypeAssignmentToGlobalVariable");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina");
    }

    @Test
    public void testConstWithoutTypeAssignmentToLocalVariable() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstWithoutTypeAssignmentToLocalVariable");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina");
    }

    @Test
    public void testConstWithTypeConcat() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstWithTypeConcat");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina rocks");
    }

    @Test
    public void testConstWithoutTypeConcat() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstWithoutTypeConcat");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina rocks");
    }

    @Test
    public void testTypeConstants() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testTypeConstants");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "GET");
    }

    @Test
    public void testConstWithTypeAssignmentToType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstWithTypeAssignmentToType");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "GET");
    }

    @Test
    public void testConstWithoutTypeAssignmentToType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstWithoutTypeAssignmentToType");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "GET");
    }

    @Test
    public void testConstAndTypeComparison() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstAndTypeComparison");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testTypeConstAsParam() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testTypeConstAsParam");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testEqualityWithConstWithType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testEqualityWithConstWithType");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testConstWithTypeInCondition() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstWithTypeInCondition");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testConstWithoutTypeInCondition() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstWithoutTypeInCondition");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testBooleanWithType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testBooleanWithType");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testBooleanWithoutType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testBooleanWithoutType");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testIntWithType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testIntWithType");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 40);
    }

    @Test
    public void testIntWithoutType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testIntWithoutType");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 20);
    }

    // Note - Byte without type cannot be specified.
    @Test
    public void testByteWithType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testByteWithType");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BByte) returns[0]).intValue(), 240);
    }

    @Test
    public void testFloatWithType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFloatWithType");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 4.0);
    }

    @Test
    public void testFloatWithoutType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFloatWithoutType");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 2.0);
    }

    // Note - Decimal without type cannot be specified.
    @Test
    public void testDecimalWithType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testDecimalWithType");
        Assert.assertNotNull(returns[0]);
        BigDecimal expected = new BigDecimal(50.0, MathContext.DECIMAL128);
        Assert.assertEquals(((BDecimal) returns[0]).decimalValue().compareTo(expected), 0);
    }

    @Test
    public void testStringWithType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStringWithType");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina is awesome");
    }

    @Test
    public void testStringWithoutType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStringWithoutType");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina rocks");
    }

    @Test
    public void testFloatAsFiniteType() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFloatAsFiniteType");
        Assert.assertNotNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 2.0);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 4.0);
    }

    @Test
    public void testConstInMapKey() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstInMapKey");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "value");
    }

    @Test
    public void testConstInMapValue() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstInMapValue");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "value");
    }

    @Test
    public void testConstInJsonKey() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstInJsonKey");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "value");
    }

    @Test
    public void testConstInJsonValue() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstInJsonValue");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "value");
    }

    @Test
    public void testBooleanConstInUnion() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testBooleanConstInUnion");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testIntConstInUnion() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testIntConstInUnion");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 40);
    }

    @Test
    public void testByteConstInUnion() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testByteConstInUnion");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BByte) returns[0]).intValue(), 240);
    }

    @Test
    public void testFloatConstInUnion() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFloatConstInUnion");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 4.0);
    }

    @Test
    public void testDecimalConstInUnion() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testDecimalConstInUnion");
        Assert.assertNotNull(returns[0]);
        BigDecimal expected = new BigDecimal(50.0, MathContext.DECIMAL128);
        Assert.assertEquals(((BDecimal) returns[0]).decimalValue().compareTo(expected), 0);
    }

    @Test
    public void testStringConstInUnion() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStringConstInUnion");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina is awesome");
    }

    @Test
    public void testBooleanConstInTuple() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testBooleanConstInTuple");
        Assert.assertNotNull(returns[0]);
        Assert.assertFalse(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testIntConstInTuple() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testIntConstInTuple");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 40);
    }

    @Test
    public void testByteConstInTuple() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testByteConstInTuple");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BByte) returns[0]).intValue(), 240);
    }

    @Test
    public void testFloatConstInTuple() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFloatConstInTuple");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 4.0);
    }

    @Test
    public void testDecimalConstInTuple() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testDecimalConstInTuple");
        Assert.assertNotNull(returns[0]);
        BigDecimal expected = new BigDecimal(50.0, MathContext.DECIMAL128);
        Assert.assertEquals(((BDecimal) returns[0]).decimalValue().compareTo(expected), 0);
    }

    @Test
    public void testStringConstInTuple() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStringConstInTuple");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina is awesome");
    }

    @Test
    public void testProperSubset() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testProperSubset");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "D");
    }

    @Test
    public void testBuiltinFunctionInvocation() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testBuiltinFunctionInvocation");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testBuiltinFunctionInvocationOnArrayElement() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testBuiltinFunctionInvocationOnArrayElement");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testBuiltinFunctionInvocationOnField() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testBuiltinFunctionInvocationOnField");
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testLabeling() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testLabeling");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "Ballerina");
    }

    @Test
    public void testBooleanConcat() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testBooleanConcat");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "true rocks");
    }

    @Test
    public void testIntConcat() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testIntConcat");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "24 rocks");
    }

    @Test
    public void testByteConcat() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testByteConcat");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "12 rocks");
    }

    @Test
    public void testFloatConcat() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFloatConcat");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "25.5 rocks");
    }

    @Test
    public void testDecimalConcat() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testDecimalConcat");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "25.5 rocks");
    }
}
