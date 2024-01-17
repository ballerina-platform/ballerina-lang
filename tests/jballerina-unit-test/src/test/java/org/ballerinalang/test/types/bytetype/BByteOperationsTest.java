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

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class tests the arithmetic operations supported by byte type. Following are the operations supported.
 * <p>
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
        Object returns = BRunUtil.invoke(result, "testByteAddition1");

        Assert.assertSame(returns.getClass(), Long.class);
        long value = (long) returns;
        Assert.assertEquals(value, 35, "Invalid int value returned.");
    }

    @Test(description = "Test byte addition")
    public void testByteAddition2() {
        Object returns = BRunUtil.invoke(result, "testByteAddition2");

        Assert.assertSame(returns.getClass(), Long.class);
        long value = (long) returns;
        Assert.assertEquals(value, 279, "Invalid int value returned.");
    }

    @Test(description = "Test byte, int addition")
    public void testByteIntAddition1() {
        Object returns = BRunUtil.invoke(result, "testByteIntAddition1");

        Assert.assertSame(returns.getClass(), Long.class);
        long value = (long) returns;
        Assert.assertEquals(value, 3499, "Invalid int value returned.");
    }

    @Test(description = "Test int, byte addition")
    public void testByteIntAddition2() {
        Object returns = BRunUtil.invoke(result, "testByteIntAddition2");

        Assert.assertSame(returns.getClass(), Long.class);
        long value = (long) returns;
        Assert.assertEquals(value, -389, "Invalid int value returned.");
    }

    @Test(description = "Test byte subtraction")
    public void testByteSubtraction1() {
        Object returns = BRunUtil.invoke(result, "testByteSubtraction1");

        Assert.assertSame(returns.getClass(), Long.class);
        long value = (long) returns;
        Assert.assertEquals(value, 200, "Invalid int value returned.");
    }

    @Test(description = "Test byte subtraction")
    public void testByteSubtraction2() {
        Object returns = BRunUtil.invoke(result, "testByteSubtraction2");

        Assert.assertSame(returns.getClass(), Long.class);
        long value = (long) returns;
        Assert.assertEquals(value, -200, "Invalid int value returned.");
    }

    @Test(description = "Test byte, int subtraction")
    public void testByteIntSubtraction1() {
        Object returns = BRunUtil.invoke(result, "testByteIntSubtraction1");

        Assert.assertSame(returns.getClass(), Long.class);
        long value = (long) returns;
        Assert.assertEquals(value, -2500, "Invalid int value returned.");
    }

    @Test(description = "Test int, byte subtraction")
    public void testByteIntSubtraction2() {
        Object returns = BRunUtil.invoke(result, "testByteIntSubtraction2");

        Assert.assertSame(returns.getClass(), Long.class);
        long value = (long) returns;
        Assert.assertEquals(value, -2610, "Invalid int value returned.");
    }

    @Test(description = "Test byte multiplication")
    public void testByteMultiplication1() {
        Object returns = BRunUtil.invoke(result, "testByteMultiplication1");

        Assert.assertSame(returns.getClass(), Long.class);
        long value = (long) returns;
        Assert.assertEquals(value, 240, "Invalid int value returned.");
    }

    @Test(description = "Test byte multiplication")
    public void testByteMultiplication2() {
        Object returns = BRunUtil.invoke(result, "testByteMultiplication2");

        Assert.assertSame(returns.getClass(), Long.class);
        long value = (long) returns;
        Assert.assertEquals(value, 27500, "Invalid int value returned.");
    }

    @Test(description = "Test byte, int multiplication")
    public void testByteIntMultiplication1() {
        Object returns = BRunUtil.invoke(result, "testByteIntMultiplication1");

        Assert.assertSame(returns.getClass(), Long.class);
        long value = (long) returns;
        Assert.assertEquals(value, -11000, "Invalid int value returned.");
    }

    @Test(description = "Test int, byte multiplication")
    public void testByteIntMultiplication2() {
        Object returns = BRunUtil.invoke(result, "testByteIntMultiplication2");

        Assert.assertSame(returns.getClass(), Long.class);
        long value = (long) returns;
        Assert.assertEquals(value, 15000, "Invalid int value returned.");
    }

    @Test(description = "Test byte division")
    public void testByteDivision1() {
        Object returns = BRunUtil.invoke(result, "testByteDivision1");

        Assert.assertSame(returns.getClass(), Long.class);
        long value = (long) returns;
        Assert.assertEquals(value, 2, "Invalid int value returned.");
    }

    @Test(description = "Test byte division")
    public void testByteDivision2() {
        Object returns = BRunUtil.invoke(result, "testByteDivision2");

        Assert.assertSame(returns.getClass(), Long.class);
        long value = (long) returns;
        Assert.assertEquals(value, 3, "Invalid int value returned.");
    }

    @Test(description = "Test byte, int division")
    public void testByteIntDivision1() {
        Object returns = BRunUtil.invoke(result, "testByteIntDivision1");

        Assert.assertSame(returns.getClass(), Long.class);
        long value = (long) returns;
        Assert.assertEquals(value, -7, "Invalid int value returned.");
    }

    @Test(description = "Test int, byte division")
    public void testByteIntDivision2() {
        Object returns = BRunUtil.invoke(result, "testByteIntDivision2");

        Assert.assertSame(returns.getClass(), Long.class);
        long value = (long) returns;
        Assert.assertEquals(value, 15, "Invalid int value returned.");
    }

    @Test(description = "Test byte modulus")
    public void testByteModulus1() {
        Object returns = BRunUtil.invoke(result, "testByteModulus1");

        Assert.assertSame(returns.getClass(), Long.class);
        long value = (long) returns;
        Assert.assertEquals(value, 2, "Invalid int value returned.");
    }

    @Test(description = "Test byte modulus")
    public void testByteModulus2() {
        Object returns = BRunUtil.invoke(result, "testByteModulus2");

        Assert.assertSame(returns.getClass(), Long.class);
        long value = (long) returns;
        Assert.assertEquals(value, 30, "Invalid int value returned.");
    }

    @Test(description = "Test byte, int modulus")
    public void testByteIntModulus1() {
        Object returns = BRunUtil.invoke(result, "testByteIntModulus1");

        Assert.assertSame(returns.getClass(), Long.class);
        long value = (long) returns;
        Assert.assertEquals(value, 1, "Invalid int value returned.");
    }

    @Test(description = "Test int, byte modulus")
    public void testByteIntModulus2() {
        Object returns = BRunUtil.invoke(result, "testByteIntModulus2");

        Assert.assertSame(returns.getClass(), Long.class);
        long value = (long) returns;
        Assert.assertEquals(value, 19, "Invalid int value returned.");
    }

    @Test(description = "Test complex expression")
    public void testByteComplexExpression() {
        Object returns = BRunUtil.invoke(result, "testByteComplexExpression");

        Assert.assertSame(returns.getClass(), Long.class);
        long value = (long) returns;
        Assert.assertEquals(value, -164, "Invalid int value returned.");
    }

    @Test(description = "Test bitwise AND between bytes")
    public void testByteBitwiseAnd() {
        Object returns = BRunUtil.invoke(result, "testByteBitwiseAnd");

        Assert.assertSame(returns.getClass(), Integer.class);
        int value = (int) returns;
        Assert.assertEquals(value, 24, "Invalid byte value returned.");
    }

    @Test(description = "Test bitwise AND between int and byte")
    public void testIntByteBitwiseAnd() {
        Object returns = BRunUtil.invoke(result, "testIntByteBitwiseAnd");

        Assert.assertSame(returns.getClass(), Long.class);
        long value = (long) returns;
        Assert.assertEquals(value, 24, "Invalid byte value returned.");
    }

    @Test(description = "Test bitwise AND between byte and int")
    public void testByteIntBitwiseAnd() {
        Object returns = BRunUtil.invoke(result, "testByteIntBitwiseAnd");

        Assert.assertSame(returns.getClass(), Long.class);
        long value = (long) returns;
        Assert.assertEquals(value, 88, "Invalid byte value returned.");
    }

    @Test(description = "Test bitwise AND between integers")
    public void testIntBitwiseAnd() {
        Object returns = BRunUtil.invoke(result, "testIntBitwiseAnd");

        Assert.assertSame(returns.getClass(), Long.class);
        long value = (long) returns;
        Assert.assertEquals(value, 447, "Invalid int value returned.");
    }

    @Test(description = "Test bitwise OR between bytes")
    public void testByteBitwiseOr() {
        Object returns = BRunUtil.invoke(result, "testByteBitwiseOr");

        Assert.assertSame(returns.getClass(), Integer.class);
        int value = (int) returns;
        Assert.assertEquals(value, 255, "Invalid byte value returned.");
    }

    @Test(description = "Test bitwise OR between int, byte")
    public void testIntByteBitwiseOr() {
        Object returns = BRunUtil.invoke(result, "testIntByteBitwiseOr");

        Assert.assertSame(returns.getClass(), Long.class);
        long value = (long) returns;
        Assert.assertEquals(value, 767, "Invalid int value returned.");
    }

    @Test(description = "Test bitwise OR between byte, int")
    public void testByteIntBitwiseOr() {
        Object returns = BRunUtil.invoke(result, "testByteIntBitwiseOr");

        Assert.assertSame(returns.getClass(), Long.class);
        long value = (long) returns;
        Assert.assertEquals(value, 760, "Invalid int value returned.");
    }

    @Test(description = "Test bitwise OR between integers")
    public void testIntBitwiseOr() {
        Object returns = BRunUtil.invoke(result, "testIntBitwiseOr");

        Assert.assertSame(returns.getClass(), Long.class);
        long value = (long) returns;
        Assert.assertEquals(value, -72, "Invalid int value returned.");
    }

    @Test(description = "Test bitwise XOR between bytes")
    public void testByteBitwiseXor() {
        Object returns = BRunUtil.invoke(result, "testByteBitwiseXor");

        Assert.assertSame(returns.getClass(), Integer.class);
        int value = (int) returns;
        Assert.assertEquals(value, 231, "Invalid byte value returned.");
    }

    @Test(description = "Test bitwise XOR between int, byte")
    public void testIntByteBitwiseXor() {
        Object returns = BRunUtil.invoke(result, "testIntByteBitwiseXor");

        Assert.assertSame(returns.getClass(), Long.class);
        long value = (long) returns;
        Assert.assertEquals(value, -150, "Invalid int value returned.");
    }

    @Test(description = "Test bitwise XOR between byte, int")
    public void testByteIntBitwiseXor() {
        Object returns = BRunUtil.invoke(result, "testByteIntBitwiseXor");

        Assert.assertSame(returns.getClass(), Long.class);
        long value = (long) returns;
        Assert.assertEquals(value, 148, "Invalid int value returned.");
    }

    @Test(description = "Test bitwise XOR between integers")
    public void testIntBitwiseXor() {
        Object returns = BRunUtil.invoke(result, "testIntBitwiseXor");

        Assert.assertSame(returns.getClass(), Long.class);
        long value = (long) returns;
        Assert.assertEquals(value, -262, "Invalid int value returned.");
    }

    @Test(description = "Test byte bitwise complement")
    public void testByteBitwiseComplement() {
        Object returns = BRunUtil.invoke(result, "testByteBitwiseComplement");

        Assert.assertSame(returns.getClass(), Long.class);
        long value = (long) returns;
        Assert.assertEquals(value, -192, "Invalid byte value returned.");
    }

    @Test(description = "Test int bitwise complement")
    public void testIntBitwiseComplement() {
        Object returns = BRunUtil.invoke(result, "testIntBitwiseComplement");

        Assert.assertSame(returns.getClass(), Long.class);
        long value = (long) returns;
        Assert.assertEquals(value, -192, "Invalid int value returned.");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
