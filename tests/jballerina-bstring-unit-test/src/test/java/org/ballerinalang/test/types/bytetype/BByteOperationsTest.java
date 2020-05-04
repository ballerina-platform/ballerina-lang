/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.types.bytetype;

import org.ballerinalang.core.model.values.BByte;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class tests the arithmetic operations supported by byte type. Following are the operations supported.
 *
 * Addition
 * Multiplication
 * Division
 * Subtraction
 * Modulus
 * Negation
 * Bitwise operations
 * <p>
 *
 * @since 0.995.0
 */
public class BByteOperationsTest {
    private CompileResult result;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        result = BCompileUtil.compile("test-src/types/byte/byte_operations.bal");
    }

    @Test(description = "Test byte addition")
    public void testByteAddition1() {
        BValue[] returns = BRunUtil.invoke(result, "testByteAddition1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger value = (BInteger) returns[0];
        Assert.assertEquals(value.intValue(), 35, "Invalid int value returned.");
    }

    @Test(description = "Test byte addition")
    public void testByteAddition2() {
        BValue[] returns = BRunUtil.invoke(result, "testByteAddition2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger value = (BInteger) returns[0];
        Assert.assertEquals(value.intValue(), 279, "Invalid int value returned.");
    }

    @Test(description = "Test byte, int addition")
    public void testByteIntAddition1() {
        BValue[] returns = BRunUtil.invoke(result, "testByteIntAddition1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger value = (BInteger) returns[0];
        Assert.assertEquals(value.intValue(), 3499, "Invalid int value returned.");
    }

    @Test(description = "Test int, byte addition")
    public void testByteIntAddition2() {
        BValue[] returns = BRunUtil.invoke(result, "testByteIntAddition2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger value = (BInteger) returns[0];
        Assert.assertEquals(value.intValue(), -389, "Invalid int value returned.");
    }

    @Test(description = "Test byte subtraction")
    public void testByteSubtraction1() {
        BValue[] returns = BRunUtil.invoke(result, "testByteSubtraction1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger value = (BInteger) returns[0];
        Assert.assertEquals(value.intValue(), 200, "Invalid int value returned.");
    }

    @Test(description = "Test byte subtraction")
    public void testByteSubtraction2() {
        BValue[] returns = BRunUtil.invoke(result, "testByteSubtraction2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger value = (BInteger) returns[0];
        Assert.assertEquals(value.intValue(), -200, "Invalid int value returned.");
    }

    @Test(description = "Test byte, int subtraction")
    public void testByteIntSubtraction1() {
        BValue[] returns = BRunUtil.invoke(result, "testByteIntSubtraction1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger value = (BInteger) returns[0];
        Assert.assertEquals(value.intValue(), -2500, "Invalid int value returned.");
    }

    @Test(description = "Test int, byte subtraction")
    public void testByteIntSubtraction2() {
        BValue[] returns = BRunUtil.invoke(result, "testByteIntSubtraction2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger value = (BInteger) returns[0];
        Assert.assertEquals(value.intValue(), -2610, "Invalid int value returned.");
    }

    @Test(description = "Test byte multiplication")
    public void testByteMultiplication1() {
        BValue[] returns = BRunUtil.invoke(result, "testByteMultiplication1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger value = (BInteger) returns[0];
        Assert.assertEquals(value.intValue(), 240, "Invalid int value returned.");
    }

    @Test(description = "Test byte multiplication")
    public void testByteMultiplication2() {
        BValue[] returns = BRunUtil.invoke(result, "testByteMultiplication2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger value = (BInteger) returns[0];
        Assert.assertEquals(value.intValue(), 27500, "Invalid int value returned.");
    }

    @Test(description = "Test byte, int multiplication")
    public void testByteIntMultiplication1() {
        BValue[] returns = BRunUtil.invoke(result, "testByteIntMultiplication1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger value = (BInteger) returns[0];
        Assert.assertEquals(value.intValue(), -11000, "Invalid int value returned.");
    }

    @Test(description = "Test int, byte multiplication")
    public void testByteIntMultiplication2() {
        BValue[] returns = BRunUtil.invoke(result, "testByteIntMultiplication2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger value = (BInteger) returns[0];
        Assert.assertEquals(value.intValue(), 15000, "Invalid int value returned.");
    }

    @Test(description = "Test byte division")
    public void testByteDivision1() {
        BValue[] returns = BRunUtil.invoke(result, "testByteDivision1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger value = (BInteger) returns[0];
        Assert.assertEquals(value.intValue(), 2, "Invalid int value returned.");
    }

    @Test(description = "Test byte division")
    public void testByteDivision2() {
        BValue[] returns = BRunUtil.invoke(result, "testByteDivision2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger value = (BInteger) returns[0];
        Assert.assertEquals(value.intValue(), 3, "Invalid int value returned.");
    }

    @Test(description = "Test byte, int division")
    public void testByteIntDivision1() {
        BValue[] returns = BRunUtil.invoke(result, "testByteIntDivision1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger value = (BInteger) returns[0];
        Assert.assertEquals(value.intValue(), -7, "Invalid int value returned.");
    }

    @Test(description = "Test int, byte division")
    public void testByteIntDivision2() {
        BValue[] returns = BRunUtil.invoke(result, "testByteIntDivision2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger value = (BInteger) returns[0];
        Assert.assertEquals(value.intValue(), 15, "Invalid int value returned.");
    }

    @Test(description = "Test byte modulus")
    public void testByteModulus1() {
        BValue[] returns = BRunUtil.invoke(result, "testByteModulus1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger value = (BInteger) returns[0];
        Assert.assertEquals(value.intValue(), 2, "Invalid int value returned.");
    }

    @Test(description = "Test byte modulus")
    public void testByteModulus2() {
        BValue[] returns = BRunUtil.invoke(result, "testByteModulus2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger value = (BInteger) returns[0];
        Assert.assertEquals(value.intValue(), 30, "Invalid int value returned.");
    }

    @Test(description = "Test byte, int modulus")
    public void testByteIntModulus1() {
        BValue[] returns = BRunUtil.invoke(result, "testByteIntModulus1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger value = (BInteger) returns[0];
        Assert.assertEquals(value.intValue(), 1, "Invalid int value returned.");
    }

    @Test(description = "Test int, byte modulus")
    public void testByteIntModulus2() {
        BValue[] returns = BRunUtil.invoke(result, "testByteIntModulus2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger value = (BInteger) returns[0];
        Assert.assertEquals(value.intValue(), 19, "Invalid int value returned.");
    }

    @Test(description = "Test complex expression")
    public void testByteComplexExpression() {
        BValue[] returns = BRunUtil.invoke(result, "testByteComplexExpression");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger value = (BInteger) returns[0];
        Assert.assertEquals(value.intValue(), -164, "Invalid int value returned.");
    }

    @Test(description = "Test bitwise AND between bytes")
    public void testByteBitwiseAnd() {
        BValue[] returns = BRunUtil.invoke(result, "testByteBitwiseAnd");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BByte.class);
        BByte value = (BByte) returns[0];
        Assert.assertEquals(value.byteValue(), 24, "Invalid byte value returned.");
    }

    @Test(description = "Test bitwise AND between int and byte")
    public void testIntByteBitwiseAnd() {
        BValue[] returns = BRunUtil.invoke(result, "testIntByteBitwiseAnd");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger value = (BInteger) returns[0];
        Assert.assertEquals(value.byteValue(), 24, "Invalid byte value returned.");
    }

    @Test(description = "Test bitwise AND between byte and int")
    public void testByteIntBitwiseAnd() {
        BValue[] returns = BRunUtil.invoke(result, "testByteIntBitwiseAnd");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger value = (BInteger) returns[0];
        Assert.assertEquals(value.byteValue(), 88, "Invalid byte value returned.");
    }

    @Test(description = "Test bitwise AND between integers")
    public void testIntBitwiseAnd() {
        BValue[] returns = BRunUtil.invoke(result, "testIntBitwiseAnd");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger value = (BInteger) returns[0];
        Assert.assertEquals(value.intValue(), 447, "Invalid int value returned.");
    }

    @Test(description = "Test bitwise OR between bytes")
    public void testByteBitwiseOr() {
        BValue[] returns = BRunUtil.invoke(result, "testByteBitwiseOr");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BByte.class);
        BByte value = (BByte) returns[0];
        Assert.assertEquals(value.byteValue(), 255, "Invalid byte value returned.");
    }

    @Test(description = "Test bitwise OR between int, byte")
    public void testIntByteBitwiseOr() {
        BValue[] returns = BRunUtil.invoke(result, "testIntByteBitwiseOr");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger value = (BInteger) returns[0];
        Assert.assertEquals(value.intValue(), 767, "Invalid int value returned.");
    }

    @Test(description = "Test bitwise OR between byte, int")
    public void testByteIntBitwiseOr() {
        BValue[] returns = BRunUtil.invoke(result, "testByteIntBitwiseOr");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger value = (BInteger) returns[0];
        Assert.assertEquals(value.intValue(), 760, "Invalid int value returned.");
    }

    @Test(description = "Test bitwise OR between integers")
    public void testIntBitwiseOr() {
        BValue[] returns = BRunUtil.invoke(result, "testIntBitwiseOr");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger value = (BInteger) returns[0];
        Assert.assertEquals(value.intValue(), -72, "Invalid int value returned.");
    }

    @Test(description = "Test bitwise XOR between bytes")
    public void testByteBitwiseXor() {
        BValue[] returns = BRunUtil.invoke(result, "testByteBitwiseXor");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BByte.class);
        BByte value = (BByte) returns[0];
        Assert.assertEquals(value.byteValue(), 231, "Invalid byte value returned.");
    }

    @Test(description = "Test bitwise XOR between int, byte")
    public void testIntByteBitwiseXor() {
        BValue[] returns = BRunUtil.invoke(result, "testIntByteBitwiseXor");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger value = (BInteger) returns[0];
        Assert.assertEquals(value.intValue(), -150, "Invalid int value returned.");
    }

    @Test(description = "Test bitwise XOR between byte, int")
    public void testByteIntBitwiseXor() {
        BValue[] returns = BRunUtil.invoke(result, "testByteIntBitwiseXor");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger value = (BInteger) returns[0];
        Assert.assertEquals(value.intValue(), 148, "Invalid int value returned.");
    }

    @Test(description = "Test bitwise XOR between integers")
    public void testIntBitwiseXor() {
        BValue[] returns = BRunUtil.invoke(result, "testIntBitwiseXor");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger value = (BInteger) returns[0];
        Assert.assertEquals(value.intValue(), -262, "Invalid int value returned.");
    }

    @Test(description = "Test byte bitwise complement")
    public void testByteBitwiseComplement() {
        BValue[] returns = BRunUtil.invoke(result, "testByteBitwiseComplement");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BByte.class);
        BByte value = (BByte) returns[0];
        Assert.assertEquals(value.byteValue(), 64, "Invalid byte value returned.");
    }

    @Test(description = "Test int bitwise complement")
    public void testIntBitwiseComplement() {
        BValue[] returns = BRunUtil.invoke(result, "testIntBitwiseComplement");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger value = (BInteger) returns[0];
        Assert.assertEquals(value.intValue(), -192, "Invalid int value returned.");
    }
}
