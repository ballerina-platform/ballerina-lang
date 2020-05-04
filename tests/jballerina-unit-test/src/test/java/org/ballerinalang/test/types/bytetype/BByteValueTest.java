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
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.types.bytetype;

import org.ballerinalang.core.model.types.BTypes;
import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BByte;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This test class will test the behaviour of byte values.
 * <p>
 * Defining a byte value
 * byte b;
 * b = 23;
 *
 * @since 0.980
 */

public class BByteValueTest {
    private CompileResult result;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        result = BCompileUtil.compile("test-src/types/byte/byte-value.bal");
    }

    @Test(description = "Test byte value assignment")
    public void testByteValue() {
        BValue[] returns = BRunUtil.invoke(result, "testByteValue", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BByte.class);
        BByte byteValue = (BByte) returns[0];
        Assert.assertEquals(byteValue.byteValue(), 34, "Invalid byte value returned.");
    }

    @Test(description = "Test byte value space")
    public void testByteValueSpace() {
        BValue[] returns = BRunUtil.invoke(result, "testByteValueSpace", new BValue[]{});
        BByte byteValue = (BByte) returns[0];
        Assert.assertEquals(byteValue.byteValue(), 234, "Invalid byte value returned.");
    }

    @Test(description = "Test byte default value")
    public void testByteDefaultValue() {
        BValue[] returns = BRunUtil.invoke(result, "testByteDefaultValue");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BByte.class);
        BByte byteValue = (BByte) returns[0];
        Assert.assertEquals(byteValue.byteValue(), 0, "Invalid byte value returned.");
    }

    @Test(description = "Test byte function parameter")
    public void testByteParameter() {
        invokeByteInputFunction("testByteParam");
    }

    @Test(description = "Test global byte value assignment")
    public void testGlobalByte() {
        invokeByteInputFunction("testGlobalByte");
    }

    private void invokeByteInputFunction(String functionName) {
        long input = 34;
        BValue[] args = {new BByte(input)};
        BValue[] returns = BRunUtil.invoke(result, functionName, args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BByte.class);
        BByte byteValue = (BByte) returns[0];
        Assert.assertEquals(byteValue.byteValue(), input, "Invalid byte value returned.");
    }

    @Test(description = "Test byte to integer cast")
    public void testByteToIntCast() {
        long input = 12;
        BValue[] args = {new BByte(input)};
        BValue[] returns = BRunUtil.invoke(result, "testByteToIntCast", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger intValue = (BInteger) returns[0];
        Assert.assertEquals(intValue.intValue(), input, "Invalid integer value returned.");
    }

    @Test(description = "Test integer to byte cast")
    public void testIntToByteCast() {
        int input = 123;
        BValue[] args = {new BInteger(input)};
        BValue[] returns = BRunUtil.invoke(result, "testIntToByteCast", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BByte.class);
        BByte bByte = (BByte) returns[0];
        Assert.assertEquals(bByte.byteValue(), (long) input, "Invalid byte value returned.");
    }

    @Test(description = "Test integer to byte explicit cast")
    public void testIntToByteExplicitCast() {
        int input = 123;
        BValue[] args = {new BInteger(input)};
        BValue[] returns = BRunUtil.invoke(result, "testIntToByteExplicitCast", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BByte.class);
        BByte bByte = (BByte) returns[0];
        Assert.assertEquals(bByte.byteValue(), (long) input, "Invalid byte value returned.");
    }

    @Test(description = "Test integer to byte conversion")
    public void testIntToByteConversion() {
        int input = 123;
        BValue[] args = {new BInteger(input)};
        BValue[] returns = BRunUtil.invoke(result, "testIntToByteConversion", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BByte.class);
        BByte bByte = (BByte) returns[0];
        Assert.assertEquals(bByte.byteValue(), (long) input, "Invalid byte value returned.");
    }

    @Test(description = "Test byte to integer conversion")
    public void testByteToIntConversion() {
        long input = 45;
        BValue[] args = {new BByte(input)};
        BValue[] returns = BRunUtil.invoke(result, "testByteToIntConversion", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger bInteger = (BInteger) returns[0];
        Assert.assertEquals(bInteger.intValue(), input, "Invalid integer value returned.");
    }

    @Test(description = "Test byte to int safe conversion")
    public void testSafeCasting() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testSafeCasting", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger bInteger = (BInteger) returns[0];
        Assert.assertEquals(bInteger.intValue(), 6, "Invalid integer value returned.");
    }

    @Test(description = "Test byte to integer conversion")
    public void testAnyToByteCasting() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testAnyToByteCasting", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BByte.class);
        BByte bByte = (BByte) returns[0];
        Assert.assertEquals(bByte.byteValue(), 45, "Invalid byte value returned.");
    }

    @Test(description = "Test byte array value")
    public void testByteArrayValue() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testByteArray", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BValueArray.class);
    }

    @Test(description = "Test byte array assignment")
    public void testByteArrayAssignment() {
        byte input1 = 2;
        byte input2 = 56;
        byte input3 = 89;
        byte input4 = 23;

        BValueArray bByteArrayIn = new BValueArray(BTypes.typeByte);
        bByteArrayIn.add(0, input1);
        bByteArrayIn.add(1, input2);
        bByteArrayIn.add(2, input3);
        bByteArrayIn.add(3, input4);
        BValue[] args = {bByteArrayIn};

        BValue[] returns = BRunUtil.invoke(result, "testByteArrayAssignment", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BValueArray.class);
        BValueArray bByteArrayOut = (BValueArray) returns[0];

        Assert.assertEquals(bByteArrayOut.getByte(0), input1);
        Assert.assertEquals(bByteArrayOut.getByte(1), input2);
        Assert.assertEquals(bByteArrayOut.getByte(2), input3);
        Assert.assertEquals(bByteArrayOut.getByte(3), input4);
    }


    @Test(description = "Test byte array length")
    public void testByteArrayLength() {
        invokeArrayLengthFunction("testByteArrayLength", 4);
    }

    @Test(description = "Test byte array zero length")
    public void testByteArrayZeroLength() {
        invokeArrayLengthFunction("testByteArrayZeroLength", 0);
    }

    private void invokeArrayLengthFunction(String functionName, int length) {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, functionName, args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger bInteger = (BInteger) returns[0];
        Assert.assertEquals(length, bInteger.intValue(), "Invalid array size");
    }

    @Test(description = "Test byte array size increase")
    public void testByteArrayIncreaseSize() {
        invokeArrayLengthFunction("testByteArrayIncreaseSize", 10);
    }

    @Test(description = "Test byte array of array")
    public void testByteArrayOfArray() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testByteArrayOfArray", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BInteger.class);
        BInteger bInteger = (BInteger) returns[0];
        BInteger bInteger1 = (BInteger) returns[1];
        Assert.assertEquals(3, bInteger.intValue(), "Invalid array size");
        Assert.assertEquals(4, bInteger1.intValue(), "Invalid array size");
    }

    @Test(description = "Test byte array iteration")
    public void testByteArrayIteration() {
        byte input1 = 1;
        byte input2 = 2;
        byte input3 = 3;
        byte input4 = 4;
        byte input5 = 5;

        BValueArray bByteArrayIn = new BValueArray(BTypes.typeByte);
        bByteArrayIn.add(0, input1);
        bByteArrayIn.add(1, input2);
        bByteArrayIn.add(2, input3);
        bByteArrayIn.add(3, input4);
        bByteArrayIn.add(4, input5);
        BValue[] args = {bByteArrayIn};

        BValue[] returns = BRunUtil.invoke(result, "testByteArrayIteration", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BValueArray.class);
        BValueArray bByteArrayOut = (BValueArray) returns[0];

        Assert.assertEquals(bByteArrayOut.getByte(0), input1);
        Assert.assertEquals(bByteArrayOut.getByte(1), input2);
        Assert.assertEquals(bByteArrayOut.getByte(2), input3);
        Assert.assertEquals(bByteArrayOut.getByte(3), input4);
        Assert.assertEquals(bByteArrayOut.getByte(4), input5);
    }

    @Test(description = "Test byte equal operation")
    public void testByteEqual() {
        byte b1 = 2;
        byte b2 = 3;
        BValue[] args = {new BByte(b1), new BByte(b2), new BByte(b1)};
        BValue[] returns = BRunUtil.invoke(result, "testByteBinaryOperation", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        BBoolean boolean1 = (BBoolean) returns[0];
        BBoolean boolean2 = (BBoolean) returns[1];
        Assert.assertFalse(boolean1.booleanValue(), "Invalid result");
        Assert.assertTrue(boolean2.booleanValue(), "Invalid result");
    }

    @Test(description = "Test byte not equal operation")
    public void testByteNotEqual() {
        byte b1 = 12;
        byte b2 = 32;
        BValue[] args = {new BByte(b1), new BByte(b2), new BByte(b1)};
        BValue[] returns = BRunUtil.invoke(result, "testByteBinaryNotEqualOperation", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        BBoolean boolean1 = (BBoolean) returns[0];
        BBoolean boolean2 = (BBoolean) returns[1];
        Assert.assertTrue(boolean1.booleanValue(), "Invalid result");
        Assert.assertFalse(boolean2.booleanValue(), "Invalid result");
    }

    @Test
    public void simpleWorkerMessagePassingTest() {
        BRunUtil.invoke(result, "testWorkerWithByteVariable", new BValue[0]);
    }

    @Test(description = "Test byte to int safe conversion")
    public void testByteOrIntMatch1() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testByteOrIntMatch1", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BByte.class);
        BByte bByte = (BByte) returns[0];
        Assert.assertEquals(bByte.byteValue(), 12, "Invalid byte value returned.");
    }

    @Test(description = "Test byte to int safe conversion")
    public void testByteOrIntMatch2() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testByteOrIntMatch2", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger bInteger = (BInteger) returns[0];
        Assert.assertEquals(bInteger.intValue(), 266, "Invalid integer value returned.");
    }

    @Test(description = "Test byte to int safe conversion")
    public void testByteOrIntMatch3() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testByteOrIntMatch3", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger bInteger = (BInteger) returns[0];
        Assert.assertEquals(bInteger.intValue(), 456, "Invalid byte value returned.");
    }

    @Test(description = "Test byte to int safe conversion")
    public void testByteOrIntMatch4() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testByteOrIntMatch4", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger bInteger = (BInteger) returns[0];
        Assert.assertEquals(bInteger.intValue(), -123, "Invalid integer value returned.");
    }


    @Test(description = "Test bitwise and operator 1")
    public void testBitwiseAndOperator1() {
        byte a = 12;
        byte b = 34;
        int i = 234;
        int j = -456;
        invokeBitwiseAndTestFunction(a, b, i, j);
    }

    @Test(description = "Test bitwise and operator 2")
    public void testBitwiseAndOperator2() {
        byte a = -97;
        byte b = 12;
        int i = -456832;
        int j = 34;
        invokeBitwiseAndTestFunction(a, b, i, j);
    }

    @Test(description = "Test bitwise and operator 3")
    public void testBitwiseAndOperator3() {
        byte a = -97;
        byte b = -23;
        int i = -456832;
        int j = -3456;
        invokeBitwiseAndTestFunction(a, b, i, j);
    }

    @Test(description = "Test bitwise and operator 4")
    public void testBitwiseAndOperator4() {
        byte a = (byte) 159;
        byte b = (byte) 233;
        int i = -456832;
        int j = -3456;
        invokeBitwiseAndTestFunction(a, b, i, j);
    }


    private void invokeBitwiseAndTestFunction(byte a, byte b, int i, int j) {
        BValue[] args = {new BByte(a), new BByte(b), new BInteger(i), new BInteger(j)};
        BValue[] returns = BRunUtil.invoke(result, "testBitwiseAndOperator", args);
        Assert.assertEquals(returns.length, 5);
        BByte bByte = (BByte) returns[0];
        BInteger bInteger1 = (BInteger) returns[1];
        BInteger bInteger2 = (BInteger) returns[2];
        BInteger bInteger3 = (BInteger) returns[3];
        BInteger bInteger4 = (BInteger) returns[4];
        Assert.assertEquals(bByte.value().byteValue(), a & b, "Invalid result");
        Assert.assertEquals(bInteger1.intValue(), Byte.toUnsignedInt(a) & Byte.toUnsignedInt(b), "Invalid result");
        Assert.assertEquals(bInteger2.intValue(), Byte.toUnsignedInt(a) & i, "Invalid result");
        Assert.assertEquals(bInteger3.intValue(), i & j, "Invalid result");
        Assert.assertEquals(bInteger4.intValue(), Byte.toUnsignedInt(a) & i & Byte.toUnsignedInt(b) & j,
                "Invalid result");
    }

    @Test(description = "Test bitwise and operator 1")
    public void testBitwiseOrOperator1() {
        byte a = 12;
        byte b = 34;
        int i = 234;
        int j = -456;
        testBitwiseOrTestFunction(a, b, i, j);
    }

    @Test(description = "Test bitwise and operator 2")
    public void testBitwiseOrOperator2() {
        byte a = -97;
        byte b = 12;
        int i = -456832;
        int j = 34;
        testBitwiseOrTestFunction(a, b, i, j);
    }

    @Test(description = "Test bitwise and operator 3")
    public void testBitwiseOrOperator3() {
        byte a = -97;
        byte b = -23;
        int i = -456832;
        int j = -3456;
        testBitwiseOrTestFunction(a, b, i, j);
    }

    private void testBitwiseOrTestFunction(byte a, byte b, int i, int j) {
        BValue[] args = {new BByte(a), new BByte(b), new BInteger(i), new BInteger(j)};
        BValue[] returns = BRunUtil.invoke(result, "testBitwiseOrOperator", args);
        Assert.assertEquals(returns.length, 5);
        BByte bByte = (BByte) returns[0];
        BInteger bInteger1 = (BInteger) returns[1];
        BInteger bInteger2 = (BInteger) returns[2];
        BInteger bInteger3 = (BInteger) returns[3];
        BInteger bInteger4 = (BInteger) returns[4];
        Assert.assertEquals(bByte.value().byteValue(), a | b, "Invalid result");
        Assert.assertEquals(bInteger1.intValue(), Byte.toUnsignedInt(a) | Byte.toUnsignedInt(b), "Invalid result");
        Assert.assertEquals(bInteger2.intValue(), Byte.toUnsignedInt(a) | i, "Invalid result");
        Assert.assertEquals(bInteger3.intValue(), i | j, "Invalid result");
        Assert.assertEquals(bInteger4.intValue(), Byte.toUnsignedInt(a) | i | Byte.toUnsignedInt(b) | j,
                "Invalid result");
    }

    @Test(description = "Test bitwise and operator 1")
    public void testBitwiseXorOperator1() {
        byte a = 12;
        byte b = 34;
        int i = 234;
        int j = -456;
        testBitwiseXorTestFunction(a, b, i, j);
    }

    @Test(description = "Test bitwise and operator 2")
    public void testBitwiseXorOperator2() {
        byte a = -97;
        byte b = 12;
        int i = -456832;
        int j = 34;
        testBitwiseXorTestFunction(a, b, i, j);
    }

    @Test(description = "Test bitwise and operator 3")
    public void testBitwiseXorOperator3() {
        byte a = -97;
        byte b = -23;
        int i = -456832;
        int j = -3456;
        testBitwiseXorTestFunction(a, b, i, j);
    }


    private void testBitwiseXorTestFunction(byte a, byte b, int i, int j) {
        BValue[] args = {new BByte(a), new BByte(b), new BInteger(i), new BInteger(j)};
        BValue[] returns = BRunUtil.invoke(result, "testBitwiseXorOperator", args);
        Assert.assertEquals(returns.length, 5);
        BByte bByte = (BByte) returns[0];
        BInteger bInteger1 = (BInteger) returns[1];
        BInteger bInteger2 = (BInteger) returns[2];
        BInteger bInteger3 = (BInteger) returns[3];
        BInteger bInteger4 = (BInteger) returns[4];
        Assert.assertEquals(bByte.value().byteValue(), a ^ b, "Invalid result");
        Assert.assertEquals(bInteger1.intValue(), Byte.toUnsignedInt(a) ^ Byte.toUnsignedInt(b), "Invalid result");
        Assert.assertEquals(bInteger2.intValue(), Byte.toUnsignedInt(a) ^ i, "Invalid result");
        Assert.assertEquals(bInteger3.intValue(), i ^ j, "Invalid result");
        Assert.assertEquals(bInteger4.intValue(), Byte.toUnsignedInt(a) ^ i ^ Byte.toUnsignedInt(b) ^ j,
                "Invalid result");
    }

    @Test(description = "Test bitwise right shift operator")
    public void testBitwiseRightShiftOperator1() {
        int a = 123;
        int b = 4;
        long i = 234;
        long j = 3;

        BValue[] args = {new BByte(a), new BByte(b), new BInteger(i), new BInteger(j)};
        BValue[] returns = BRunUtil.invoke(result, "testBitwiseRightShiftOperator1", args);
        Assert.assertEquals(returns.length, 3);
        BByte bByte1 = (BByte) returns[0];
        BInteger bInteger2 = (BInteger) returns[1];
        BByte bByte2 = (BByte) returns[2];
        Assert.assertEquals(bByte1.value().intValue(), 7, "Invalid result");
        Assert.assertEquals(bByte1.intValue(), 7, "Invalid result");
        Assert.assertEquals(bInteger2.intValue(), (i >> j), "Invalid result");
        Assert.assertEquals(bByte2.intValue(), 15, "Invalid result");

        BValue[] args2 = {new BByte(a), new BByte(b), new BInteger(i), new BInteger(j)};
        returns = BRunUtil.invoke(result, "testBitwiseRightShiftOperator2", args2);
        Assert.assertEquals(returns.length, 3);
        BInteger bInteger1 = (BInteger) returns[0];
        bInteger2 = (BInteger) returns[1];
        BInteger bInteger3 = (BInteger) returns[2];
        Assert.assertEquals(bInteger1.intValue(), 7, "Invalid result");
        Assert.assertEquals(bInteger2.intValue(), i >> j, "Invalid result");
        Assert.assertEquals(bInteger3.intValue(), 15, "Invalid result");
    }

    @Test(description = "Test bitwise right shift operator")
    public void testBitwiseRightShiftOperator2() {
        int a = 228;
        int b = 6;
        long i = -45678776;
        long j = 4;

        BValue[] args = {new BByte(a), new BByte(b), new BInteger(i), new BInteger(j)};
        BValue[] returns = BRunUtil.invoke(result, "testBitwiseRightShiftOperator1", args);
        Assert.assertEquals(returns.length, 3);
        BByte bByte1 = (BByte) returns[0];
        BInteger bInteger2 = (BInteger) returns[1];
        BByte bByte2 = (BByte) returns[2];
        Assert.assertEquals(bByte1.value().intValue(), 3, "Invalid result");
        Assert.assertEquals(bByte1.intValue(),  3, "Invalid result");
        Assert.assertEquals(bInteger2.intValue(), (i >> j), "Invalid result");
        Assert.assertEquals(bByte2.intValue(), 14, "Invalid result");

        BValue[] args2 = {new BByte(a), new BByte(b), new BInteger(i), new BInteger(j)};
        returns = BRunUtil.invoke(result, "testBitwiseRightShiftOperator2", args2);
        Assert.assertEquals(returns.length, 3);
        BInteger bInteger1 = (BInteger) returns[0];
        bInteger2 = (BInteger) returns[1];
        BInteger bInteger3 = (BInteger) returns[2];
        Assert.assertEquals(bInteger1.intValue(), 3, "Invalid result");
        Assert.assertEquals(bInteger2.intValue(), i >> j, "Invalid result");
        Assert.assertEquals(bInteger3.intValue(), 14, "Invalid result");
    }

    @Test(description = "Test bitwise unsigned right shift operator 1")
    public void testBitwiseUnsignedRightShiftOperator1() {
        byte a = 12;
        int i = 234736486;
        int j = 6;
        invokeUnsignedRightShiftOperatorTestFunction(a, i, j);
    }

    @Test(description = "Test bitwise unsigned right shift operator 2")
    public void testBitwiseUnsignedRightShiftOperator2() {
        byte a = 12;
        long i = -23445834;
        long j = 5;
        invokeUnsignedRightShiftOperatorTestFunction(a, i, j);
    }

    private void invokeUnsignedRightShiftOperatorTestFunction(byte a, long i, long j) {
        BValue[] args = {new BByte(a), new BInteger(i), new BInteger(j)};
        BValue[] returns = BRunUtil.invoke(result, "testBitwiseUnsignedRightShiftOperator", args);
        Assert.assertEquals(returns.length, 2);
        BInteger bInteger1 = (BInteger) returns[0];
        BInteger bInteger2 = (BInteger) returns[1];
        Assert.assertEquals(bInteger1.intValue(), i >>> j, "Invalid result");
        Assert.assertEquals(bInteger2.intValue(), i >>> a, "Invalid result");
    }

    @Test(description = "Test bitwise left shift operator 1")
    public void testBitwiseLeftShiftOperator1() {
        int a = 23;
        int b = 6;
        long i = 4567;
        long j = 7;
        invokeLeftShiftOperatorTestFunction1(a, b, i, j);
        invokeLeftShiftOperatorTestFunction2(a, b, i, j);
    }


    @Test(description = "Test bitwise left shift operator 2")
    public void testBitwiseLeftShiftOperator2() {
        int a = 228;
        int b = 6;
        long i = -45678776;
        long j = 4;
        invokeLeftShiftOperatorTestFunction1(a, b, i, j);
        invokeLeftShiftOperatorTestFunction2(a, b, i, j);
    }

    private void invokeLeftShiftOperatorTestFunction1(int a, int b, long i, long j) {
        BValue[] args = {new BByte(a), new BByte(b), new BInteger(i), new BInteger(j)};
        BValue[] returns = BRunUtil.invoke(result, "testBitwiseLeftShiftOperator1", args);
        Assert.assertEquals(returns.length, 3);
        BInteger bInteger1 = (BInteger) returns[0];
        BInteger bInteger2 = (BInteger) returns[1];
        BInteger bInteger3 = (BInteger) returns[2];
        Assert.assertEquals(bInteger1.intValue(), a << b, "Invalid result");
        Assert.assertEquals(bInteger2.intValue(), i << j, "Invalid result");
        Assert.assertEquals(bInteger3.intValue(), a << j, "Invalid result");
    }

    private void invokeLeftShiftOperatorTestFunction2(int a, int b, long i, long j) {
        BValue[] args = {new BByte(a), new BByte(b), new BInteger(i), new BInteger(j)};
        BValue[] returns = BRunUtil.invoke(result, "testBitwiseLeftShiftOperator2", args);
        Assert.assertEquals(returns.length, 3);
        BInteger bInteger1 = (BInteger) returns[0];
        BInteger bInteger2 = (BInteger) returns[1];
        BInteger bInteger3 = (BInteger) returns[2];
        Assert.assertEquals(bInteger1.intValue(), a << b, "Invalid result");
        Assert.assertEquals(bInteger2.intValue(), i << j, "Invalid result");
        Assert.assertEquals(bInteger3.intValue(), a << j, "Invalid result");
    }

    @Test(description = "Test byte shift")
    public void testByteShift() {
        int a = 129;
        int c =  (a << 1);
        int d =  (c >> 1);
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(result, "testByteShift", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger bInteger = (BInteger) returns[0];
        Assert.assertEquals(bInteger.intValue(), d, "Invalid byte value returned.");
    }

    @Test(description = "Test bitwise Complement operator 1")
    public void testBitwiseComplementOperator1() {
        byte a = 34;
        int b = 234;
        invokeBitwiseComplementOperatorFunction(a, b);
    }

    @Test(description = "Test bitwise Complement operator 2")
    public void testBitwiseComplementOperator2() {
        byte a = (byte) 156;
        int b = -232224;
        invokeBitwiseComplementOperatorFunction(a, b);
    }

    @Test(description = "Test bitwise Complement operator 3")
    public void testBitwiseComplementOperator3() {
        byte a = -112;
        int b = 567849302;
        invokeBitwiseComplementOperatorFunction(a, b);
    }


    private void invokeBitwiseComplementOperatorFunction(byte a, int b) {
        BValue[] args = {new BByte(a), new BInteger(b)};
        BValue[] returns = BRunUtil.invoke(result, "testBitwiseNotOperator", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BByte.class);
        Assert.assertSame(returns[1].getClass(), BInteger.class);
        BByte bByte = (BByte) returns[0];
        BInteger bInteger = (BInteger) returns[1];
        byte a1 = (byte) ~a;
        Assert.assertEquals(bByte.value().byteValue(), a1, "Invalid byte value returned.");
        Assert.assertEquals(bInteger.intValue(), ~b, "Invalid int value returned.");
    }

    @Test(description = "Test bitwise operator precedence 1")
    public void testBitwiseOperatorPrecedence1() {
        byte a = 127;
        byte b = 4;
        byte c = 5;
        byte expected = (byte) (~a & b >> c);
        invokeBitwisePrecedenceTestFunctionForByte(a, b, c, "testBitwiseOperatorPrecedence1", expected);
    }

    @Test(description = "Test bitwise operator precedence 2")
    public void testBitwiseOperatorPrecedence2() {
        byte a = (byte) 233;
        byte b = 23;
        byte c = 3;
        byte expected = (byte) (~a & b >> c);
        invokeBitwisePrecedenceTestFunctionForByte(a, b, c, "testBitwiseOperatorPrecedence1", expected);
    }

    @Test(description = "Test bitwise operator precedence 3")
    public void testBitwiseOperatorPrecedence3() {
        byte a = 23;
        byte b = 4;
        byte c = 5;
        byte expected = (byte) (b & ~a >> c);
        invokeBitwisePrecedenceTestFunctionForByte(a, b, c, "testBitwiseOperatorPrecedence2", expected);
    }

    @Test(description = "Test bitwise operator precedence 4")
    public void testBitwiseOperatorPrecedence4() {
        byte a = (byte) 233;
        byte b = 23;
        byte c = 3;
        byte expected = (byte) (b & ~a >> c);
        invokeBitwisePrecedenceTestFunctionForByte(a, b, c, "testBitwiseOperatorPrecedence2", expected);
    }

    @Test(description = "Test bitwise operator precedence 5")
    public void testBitwiseOperatorPrecedence5() {
        byte a = 23;
        byte b = 4;
        byte c = 5;
        byte expected = (byte) (b >> c & ~a);
        invokeBitwisePrecedenceTestFunctionForByte(a, b, c, "testBitwiseOperatorPrecedence3", expected);
    }


    @Test(description = "Test bitwise operator precedence 6")
    public void testBitwiseOperatorPrecedence6() {
        byte a = (byte) 233;
        byte b = 23;
        byte c = 3;
        byte expected = (byte) (b >> c & ~a);
        invokeBitwisePrecedenceTestFunctionForByte(a, b, c, "testBitwiseOperatorPrecedence3", expected);
    }

    @Test(description = "Test bitwise operator precedence 7")
    public void testBitwiseOperatorPrecedence7() {
        int a = 3546782;
        int b = 4;
        int c = 5;
        int expected = ~a & b >> c;
        invokeBitwisePrecedenceTestFunctionForInt(a, b, c, "testBitwiseOperatorPrecedence4", expected);
    }

    @Test(description = "Test bitwise operator precedence 8")
    public void testBitwiseOperatorPrecedence8() {
        int a = -2334353;
        int b = 23;
        int c = -3;
        int expected = ~a & b >> c;
        invokeBitwisePrecedenceTestFunctionForInt(a, b, c, "testBitwiseOperatorPrecedence4", expected);
    }

    @Test(description = "Test bitwise operator precedence 9")
    public void testBitwiseOperatorPrecedence9() {
        int a = 245623;
        int b = 4;
        int c = -5;
        int expected = b & ~a >> c;
        invokeBitwisePrecedenceTestFunctionForInt(a, b, c, "testBitwiseOperatorPrecedence5", expected);
    }


    @Test(description = "Test bitwise operator precedence 10")
    public void testBitwiseOperatorPrecedence10() {
        int a = -2667533;
        int b = 23;
        int c = 3;
        int expected = b & ~a >> c;
        invokeBitwisePrecedenceTestFunctionForInt(a, b, c, "testBitwiseOperatorPrecedence5", expected);
    }

    @Test(description = "Test bitwise operator precedence 11")
    public void testBitwiseOperatorPrecedence11() {
        int a = 23;
        int b = 4;
        int c = 5;
        int expected = b >> c & ~a;
        invokeBitwisePrecedenceTestFunctionForInt(a, b, c, "testBitwiseOperatorPrecedence6", expected);
    }


    @Test(description = "Test bitwise operator precedence 12")
    public void testBitwiseOperatorPrecedence12() {
        int a = 23334233;
        int b = 23;
        int c = 3;
        int expected = b >> c & ~a;
        invokeBitwisePrecedenceTestFunctionForInt(a, b, c, "testBitwiseOperatorPrecedence6", expected);
    }

    private void invokeBitwisePrecedenceTestFunctionForByte(byte a, byte b, byte c, String functionName,
                                                            byte expected) {
        BValue[] args = {new BByte(a), new BByte(b), new BByte(c)};
        BValue[] returns = BRunUtil.invoke(result, functionName, args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BByte.class);
        BByte bByte = (BByte) returns[0];
        Assert.assertEquals(bByte.value().byteValue(), expected, "Invalid byte value returned.");
    }

    private void invokeBitwisePrecedenceTestFunctionForInt(int a, int b, int c, String functionName, int expected) {
        BValue[] args = {new BInteger(a), new BInteger(b), new BInteger(c)};
        BValue[] returns = BRunUtil.invoke(result, functionName, args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger bInteger = (BInteger) returns[0];
        Assert.assertEquals(bInteger.intValue(), expected, "Invalid byte value returned.");
    }

    @Test(description = "Test byte value return as int in lambda")
    public void testByteReturnAsIntInLambda() {
        BRunUtil.invoke(result, "testByteReturnAsIntInLambda1");
        BRunUtil.invoke(result, "testByteReturnAsIntInLambda2");
        BRunUtil.invoke(result, "testByteReturnAsIntInLambda3");
    }
}
