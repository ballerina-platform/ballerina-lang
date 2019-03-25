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

import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases to cover some basic types related tests on JBallerina.
 *
 * @since 0.955.0
 */
public class TypesTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/jvm/types.bal");
    }

    @Test
    public void testInt1() {
        BValue[] result = BRunUtil.invoke(compileResult, "testIntWithoutArgs", new BValue[] {});
        Assert.assertEquals(((BInteger) result[0]).intValue(), 7);
    }

    @Test
    public void testInt2() {
        BValue[] result = BRunUtil.invoke(compileResult, "testIntWithArgs", new BValue[] { new BInteger(5) });
        Assert.assertEquals(((BInteger) result[0]).intValue(), 10);
    }

    @Test
    public void testString1() {
        BValue[] result = BRunUtil.invoke(compileResult, "testStringWithoutArgs", new BValue[] {});
        Assert.assertEquals((result[0]).stringValue(), "Hello");
    }

    @Test
    public void testString2() {
        BValue[] result = BRunUtil.invoke(compileResult, "testStringWithArgs", new BValue[] { new BString("World") });
        Assert.assertEquals((result[0]).stringValue(), "HelloWorld");
    }

    @Test
    public void testArray() {
        BValue[] result = BRunUtil.invoke(compileResult, "testArray", new BValue[] { new BString("World") });
        Assert.assertEquals((result[0]).stringValue(), "3");
    }

    @Test
    public void getGlobalVar() {
        BValue[] returns = BRunUtil.invoke(compileResult, "getGlobalVar");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 7);
    }

    @Test
    public void testInt() {
        BValue[] result = BRunUtil.invoke(compileResult, "testByteWithoutArgs");
        Assert.assertEquals(((BInteger) result[0]).intValue(), 7);
    }

    @Test(description = "Test byte value assignment")
    public void testByteValue() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testByteValue", new BValue[] {});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BByte.class);
        BByte byteValue = (BByte) returns[0];
        Assert.assertEquals(byteValue.byteValue(), 34, "Invalid byte value returned.");
    }

    @Test(description = "Test byte value space")
    public void testByteValueSpace() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testByteValueSpace", new BValue[] {});
        BByte byteValue = (BByte) returns[0];
        Assert.assertEquals(byteValue.byteValue(), 234, "Invalid byte value returned.");
    }

    @Test(description = "Test byte default value")
    public void testByteDefaultValue() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testByteDefaultValue");
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
        BValue[] args = { new BByte(input) };
        BValue[] returns = BRunUtil.invoke(compileResult, functionName, args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BByte.class);
        BByte byteValue = (BByte) returns[0];
        Assert.assertEquals(byteValue.byteValue(), input, "Invalid byte value returned.");
    }

    @Test(description = "Test byte to integer cast")
    public void testByteToIntCast() {
        long input = 12;
        BValue[] args = { new BByte(input) };
        BValue[] returns = BRunUtil.invoke(compileResult, "testByteToIntCast", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        BInteger intValue = (BInteger) returns[0];
        Assert.assertEquals(intValue.intValue(), input, "Invalid integer value returned.");
    }

    @Test(description = "Test integer to byte cast")
    public void testIntToByteCast() {
        int input = 123;
        BValue[] args = { new BInteger(input) };
        BValue[] returns = BRunUtil.invoke(compileResult, "testIntToByteExplicitCast", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BByte.class);
        BByte bByte = (BByte) returns[0];
        Assert.assertEquals(bByte.byteValue(), input, "Invalid byte value returned.");
    }

    @Test(description = "Test integer to byte explicit cast")
    public void testIntToByteExplicitCast() {
        int input = 123;
        BValue[] args = { new BInteger(input) };
        BValue[] returns = BRunUtil.invoke(compileResult, "testIntToByteCast", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BByte.class);
        BByte bByte = (BByte) returns[0];
        Assert.assertEquals(bByte.byteValue(), input, "Invalid byte value returned.");
    }

    @Test(description = "Test byte array value")
    public void testByteArrayValue() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testByteArray", args);
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
        BValue[] args = { bByteArrayIn };

        BValue[] returns = BRunUtil.invoke(compileResult, "testByteArrayAssignment", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BValueArray.class);
        BValueArray bByteArrayOut = (BValueArray) returns[0];

        Assert.assertEquals(bByteArrayOut.getByte(0), input1);
        Assert.assertEquals(bByteArrayOut.getByte(1), input2);
        Assert.assertEquals(bByteArrayOut.getByte(2), input3);
        Assert.assertEquals(bByteArrayOut.getByte(3), input4);
    }

    @Test
    public void testTuple() {
        // Todo: revisit when tuple access and var type supported
        BValue[] result = BRunUtil.invoke(compileResult, "tupleTest");
        Assert.assertEquals((result[0]).stringValue(), "10");
    }

    @Test
    public void testRecords() {
        BValue[] result = BRunUtil.invoke(compileResult, "recordsTest");
        Assert.assertEquals((result[0]).stringValue(), "JBallerina");
    }

    @Test
    public void testUnions() {
        BValue[] result = BRunUtil.invoke(compileResult, "unionTest");
        Assert.assertEquals((result[0]).stringValue(), "10.5");
    }

    @Test
    public void testAny() {
        BValue[] result = BRunUtil.invoke(compileResult, "anyTest");
        Assert.assertEquals((result[0]).stringValue(), "{\"name\":\"Jbal\", \"physics\":75, \"chemistry\":89}");
    }

    @Test
    public void testAnyData() {
        BValue[] result = BRunUtil.invoke(compileResult, "anyDataTest");
        Assert.assertEquals((result[0]).stringValue(), "1000");
    }
}
