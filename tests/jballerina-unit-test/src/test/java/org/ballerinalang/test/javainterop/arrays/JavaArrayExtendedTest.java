/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.javainterop.arrays;

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BHandleValue;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for mapping Ballerina arrays to/from Java array handle references.
 *
 * @since 1.2.4
 */
public class JavaArrayExtendedTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/javainterop/arrays/java_array_handle_tests.bal");
    }

    @Test(description = "Test java.arrays:toHandle function with String[]")
    public void testToHandleWithString() {
        String[] values = {"Five", "Two", "Nine", "Three", "Seven"};
        BValue[] returns = BRunUtil.invoke(result, "testToHandleWithString");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BHandleValue) returns[0]).getValue(), values);
    }

    @Test(description = "Test java.arrays:toHandle function with Byte[]")
    public void testToHandleWithByte() {
        Byte[] values1 = {80, 65, 78, 32, 65};
        byte[] values2 = {80, 65, 78, 32, 65};
        BValue[] returns = BRunUtil.invoke(result, "testToHandleWithByte");
        assertTest(values1, values2, returns);
    }

    @Test(description = "Test java.arrays:toHandle function with Character[] from Ballerina byte[]")
    public void testToHandleWithByteChar() {
        Character[] values1 = {80, 65, 78, 32, 65};
        char[] values2 = {80, 65, 78, 32, 65};
        BValue[] returns = BRunUtil.invoke(result, "testToHandleWithByteChar");
        assertTest(values1, values2, returns);
    }

    @Test(description = "Test java.arrays:toHandle function with Short[] from Ballerina byte[]")
    public void testToHandleWithByteShort() {
        Short[] values1 = {80, 65, 78, 32, 65};
        short[] values2 = {80, 65, 78, 32, 65};
        BValue[] returns = BRunUtil.invoke(result, "testToHandleWithByteShort");
        assertTest(values1, values2, returns);
    }

    @Test(description = "Test java.arrays:toHandle function with Long[] from Ballerina byte[]")
    public void testToHandleWithByteLong() {
        Long[] values1 = {80L, 65L, 78L, 32L, 65L};
        long[] values2 = {80, 65, 78, 32, 65};
        BValue[] returns = BRunUtil.invoke(result, "testToHandleWithByteLong");
        assertTest(values1, values2, returns);
    }

    @Test(description = "Test java.arrays:toHandle function with Integer[] from Ballerina byte[]")
    public void testToHandleWithByteInt() {
        Integer[] values1 = {80, 65, 78, 32, 65};
        int[] values2 = {80, 65, 78, 32, 65};
        BValue[] returns = BRunUtil.invoke(result, "testToHandleWithByteInt");
        assertTest(values1, values2, returns);
    }

    @Test(description = "Test java.arrays:toHandle function with Float[] from Ballerina byte[]")
    public void testToHandleWithByteFloat() {
        Float[] values1 = {80.0f, 65f, 78f, 32f, 65f};
        float[] values2 = {80, 65, 78, 32, 65};
        BValue[] returns = BRunUtil.invoke(result, "testToHandleWithByteFloat");
        assertTest(values1, values2, returns);
    }

    @Test(description = "Test java.arrays:toHandle function with Double[] from Ballerina byte[]")
    public void testToHandleWithByteDouble() {
        Double[] values1 = {80.0, 65.0, 78.0, 32.0, 65.0};
        double[] values2 = {80.0, 65.0, 78.0, 32.0, 65.0};
        BValue[] returns = BRunUtil.invoke(result, "testToHandleWithByteDouble");
        assertTest(values1, values2, returns);
    }

    @Test(description = "Test java.arrays:toHandle function with Int[]")
    public void testToHandleWithInt() {
        Integer[] values1 = {8, 25, 79, 34, 2};
        int[] values2 = {8, 25, 79, 34, 2};
        BValue[] returns = BRunUtil.invoke(result, "testToHandleWithInt");
        assertTest(values1, values2, returns);
    }

    @Test(description = "Test java.arrays:toHandle function with Byte[] from Ballerina int[]")
    public void testToHandleWithIntByte() {
        Byte[] values1 = {8, 25, 79, 34, 2};
        byte[] values2 = {8, 25, 79, 34, 2};
        BValue[] returns = BRunUtil.invoke(result, "testToHandleWithIntByte");
        assertTest(values1, values2, returns);
    }

    @Test(description = "Test java.arrays:toHandle function with Character[] from Ballerina int[]")
    public void testToHandleWithIntChar() {
        Character[] values1 = {8, 25, 79, 34, 2};
        char[] values2 = {8, 25, 79, 34, 2};
        BValue[] returns = BRunUtil.invoke(result, "testToHandleWithIntChar");
        assertTest(values1, values2, returns);
    }

    @Test(description = "Test java.arrays:toHandle function with Short[] from Ballerina int[]")
    public void testToHandleWithIntShort() {
        Short[] values1 = {8, 25, 79, 34, 2};
        short[] values2 = {8, 25, 79, 34, 2};
        BValue[] returns = BRunUtil.invoke(result, "testToHandleWithIntShort");
        assertTest(values1, values2, returns);
    }

    @Test(description = "Test java.arrays:toHandle function with Long[] from Ballerina int[]")
    public void testToHandleWithIntLong() {
        Long[] values1 = {8L, 25L, 79L, 34L, 2L};
        long[] values2 = {8, 25, 79, 34, 2};
        BValue[] returns = BRunUtil.invoke(result, "testToHandleWithIntLong");
        assertTest(values1, values2, returns);
    }

    @Test(description = "Test java.arrays:toHandle function with Float[]")
    public void testToHandleWithFloat() {
        Float[] values1 = {8.7f, 25.2f, 79.1f, 34.6f, 2f};
        float[] values2 = {8.7f, 25.2f, 79.1f, 34.6f, 2f};
        BValue[] returns = BRunUtil.invoke(result, "testToHandleWithFloat");
        assertTest(values1, values2, returns);
    }

    @Test(description = "Test java.arrays:toHandle function with Character[] from Ballerina float[]")
    public void testToHandleWithFloatChar() {
        Character[] values1 = {8, 25, 79, 34, 2};
        char[] values2 = {8, 25, 79, 34, 2};
        BValue[] returns = BRunUtil.invoke(result, "testToHandleWithFloatChar");
        assertTest(values1, values2, returns);
    }

    @Test(description = "Test java.arrays:toHandle function with Byte[] from Ballerina float[]")
    public void testToHandleWithFloatByte() {
        Byte[] values1 = {8, 25, 79, 34, 2};
        byte[] values2 = {8, 25, 79, 34, 2};
        BValue[] returns = BRunUtil.invoke(result, "testToHandleWithFloatByte");
        assertTest(values1, values2, returns);
    }

    @Test(description = "Test java.arrays:toHandle function with Short[] from Ballerina float[]")
    public void testToHandleWithFloatShort() {
        Short[] values1 = {8, 25, 79, 34, 2};
        short[] values2 = {8, 25, 79, 34, 2};
        BValue[] returns = BRunUtil.invoke(result, "testToHandleWithFloatShort");
        assertTest(values1, values2, returns);
    }

    @Test(description = "Test java.arrays:toHandle function with Long[] from Ballerina float[]")
    public void testToHandleWithFloatLong() {
        Long[] values1 = {8L, 25L, 79L, 34L, 2L};
        long[] values2 = {8, 25, 79, 34, 2};
        BValue[] returns = BRunUtil.invoke(result, "testToHandleWithFloatLong");
        assertTest(values1, values2, returns);
    }

    @Test(description = "Test java.arrays:toHandle function with Integer[] from Ballerina float[]")
    public void testToHandleWithFloatInt() {
        Integer[] values1 = {8, 25, 79, 34, 2};
        int[] values2 = {8, 25, 79, 34, 2};
        BValue[] returns = BRunUtil.invoke(result, "testToHandleWithFloatInt");
        assertTest(values1, values2, returns);
    }

    @Test(description = "Test java.arrays:toHandle function with Double[] from Ballerina float[]")
    public void testToHandleWithFloatDouble() {
        Double[] values1 = {8.7, 25.2, 79.1, 34.6, 2.0};
        double[] values2 = {8.7, 25.2, 79.1, 34.6, 2};
        BValue[] returns = BRunUtil.invoke(result, "testToHandleWithFloatDouble");
        assertTest(values1, values2, returns);
    }

    @Test(description = "Test java.arrays:toHandle function with Boolean[]")
    public void testToHandleWithBoolean() {
        Boolean[] values1 = {true, true, false, true};
        boolean[] values2 = {true, true, false, true};
        BValue[] returns = BRunUtil.invoke(result, "testToHandleWithBoolean");
        assertTest(values1, values2, returns);
    }

    @Test(description = "Test java.arrays:toHandle function with JObject[]")
    public void testToHandleWithJObject() {
        BValue[] returns = BRunUtil.invoke(result, "testToHandleWithJObject");
        Assert.assertEquals(returns.length, 1);
        BHandleValue handleValue = (BHandleValue) returns[0];
        String[] parts = (String[]) handleValue.getValue();
        String[] partsExpected = {"Welcome", "To", "Ballerina", "Language"};
        Assert.assertEquals(parts.length, 4);
        Assert.assertEquals(parts, partsExpected);
    }

    @Test(description = "Test java.arrays:fromHandle function with String[]")
    public void testFromHandleWithString() {
        String[] values = {"Five", "Two", "Nine", "Three", "Seven"};
        invokeFunction("testFromHandleWithString", new BHandleValue(values));
    }

    @Test(description = "Test java.arrays:fromHandle function with Boolean[]")
    public void testFromHandleWithBoolean() {
        Boolean[] values1 = {true, true, false, true};
        boolean[] values2 = {true, true, false, true};
        invokeFunction("testFromHandleWithBoolean", new BHandleValue(values1));
        invokeFunction("testFromHandleWithBoolean", new BHandleValue(values2));
    }

    @Test(description = "Test java.arrays:fromHandle function with Float[]")
    public void testFromHandleWithFloat() {
        Float[] values1 = {8.7f, 25.2f, 79.1f, 34.6f, 2f};
        float[] values2 = {8.7f, 25.2f, 79.1f, 34.6f, 2f};
        invokeFunction("testFromHandleWithFloat", new BHandleValue(values1));
        invokeFunction("testFromHandleWithFloat", new BHandleValue(values2));
    }

    @Test(description = "Test java.arrays:fromHandle function with Double[]")
    public void testFromHandleWithDouble() {
        Double[] values1 = {8.7, 25.2, 79.1, 34.6, 2.0};
        double[] values2 = {8.7, 25.2, 79.1, 34.6, 2};
        invokeFunction("testFromHandleWithDouble", new BHandleValue(values1));
        invokeFunction("testFromHandleWithDouble", new BHandleValue(values2));
    }

    @Test(description = "Test java.arrays:fromHandle function with Integer[]")
    public void testFromHandleWithInt() {
        Integer[] values1 = {34, 76, 12, 90, 45};
        int[] values2 = {34, 76, 12, 90, 45};
        invokeFunction("testFromHandleWithInt", new BHandleValue(values1));
        invokeFunction("testFromHandleWithInt", new BHandleValue(values2));
    }

    @Test(description = "Test java.arrays:fromHandle function with Integer[] to Ballerina float[]")
    public void testFromHandleWithIntFloat() {
        Integer[] values1 = {34, 76, 12, 90, 45};
        int[] values2 = {34, 76, 12, 90, 45};
        invokeFunction("testFromHandleWithIntFloat", new BHandleValue(values1));
        invokeFunction("testFromHandleWithIntFloat", new BHandleValue(values2));
    }

    @Test(description = "Test java.arrays:fromHandle function with Byte[]")
    public void testFromHandleWithByte() {
        Byte[] values1 = {80, 65, 78, 32, 65};
        byte[] values2 = {80, 65, 78, 32, 65};
        invokeFunction("testFromHandleWithByte", new BHandleValue(values1));
        invokeFunction("testFromHandleWithByte", new BHandleValue(values2));
    }

    @Test(description = "Test java.arrays:fromHandle function with Byte[] to Ballerina float[]")
    public void testFromHandleWithByteFloat() {
        Byte[] values1 = {80, 65, 78, 32, 65};
        byte[] values2 = {80, 65, 78, 32, 65};
        invokeFunction("testFromHandleWithByteFloat", new BHandleValue(values1));
        invokeFunction("testFromHandleWithByteFloat", new BHandleValue(values2));
    }

    @Test(description = "Test java.arrays:fromHandle function with Byte[] to Ballerina int[]")
    public void testFromHandleWithByteInt() {
        Byte[] values1 = {80, 65, 78, 32, 65};
        byte[] values2 = {80, 65, 78, 32, 65};
        invokeFunction("testFromHandleWithByteInt", new BHandleValue(values1));
        invokeFunction("testFromHandleWithByteInt", new BHandleValue(values2));
    }

    @Test(description = "Test java.arrays:fromHandle function with Short[]")
    public void testFromHandleWithShort() {
        Short[] values1 = {34, 76, 12, 90, 45};
        short[] values2 = {34, 76, 12, 90, 45};
        invokeFunction("testFromHandleWithShort", new BHandleValue(values1));
        invokeFunction("testFromHandleWithShort", new BHandleValue(values2));
    }

    @Test(description = "Test java.arrays:fromHandle function with Short[] to Ballerina float[]")
    public void testFromHandleWithShortFloat() {
        Short[] values1 = {34, 76, 12, 90, 45};
        short[] values2 = {34, 76, 12, 90, 45};
        invokeFunction("testFromHandleWithShortFloat", new BHandleValue(values1));
        invokeFunction("testFromHandleWithShortFloat", new BHandleValue(values2));
    }

    @Test(description = "Test java.arrays:fromHandle function with Long[]")
    public void testFromHandleWithLong() {
        Long[] values1 = {34L, 76L, 12L, 90L, 45L};
        long[] values2 = {34, 76, 12, 90, 45};
        invokeFunction("testFromHandleWithLong", new BHandleValue(values1));
        invokeFunction("testFromHandleWithLong", new BHandleValue(values2));
    }

    @Test(description = "Test java.arrays:fromHandle function with Long[] to Ballerina float[]")
    public void testFromHandleWithLongFloat() {
        Long[] values1 = {34L, 76L, 12L, 90L, 45L};
        long[] values2 = {34, 76, 12, 90, 45};
        invokeFunction("testFromHandleWithLongFloat", new BHandleValue(values1));
        invokeFunction("testFromHandleWithLongFloat", new BHandleValue(values2));
    }

    @Test(description = "Test java.arrays:fromHandle function with Character[]")
    public void testFromHandleWithCharacter() {
        Character[] values1 = {'s', 'k', 'p', 'w', 'i'};
        char[] values2 = {'s', 'k', 'p', 'w', 'i'};
        invokeFunction("testFromHandleWithCharacter", new BHandleValue(values1));
        invokeFunction("testFromHandleWithCharacter", new BHandleValue(values2));
    }

    @Test(description = "Test java.arrays:fromHandle function with Character[]")
    public void testFromHandleWithCharacterFloat() {
        Character[] values1 = {'s', 'k', 'p', 'w', 'i'};
        char[] values2 = {'s', 'k', 'p', 'w', 'i'};
        invokeFunction("testFromHandleWithCharacterFloat", new BHandleValue(values1));
        invokeFunction("testFromHandleWithCharacterFloat", new BHandleValue(values2));
    }

    @Test(description = "Test java.arrays:fromHandle function with String[]")
    public void testFromHandleWithHandle() {
        String[] values = {"Welcome", "To", "Ballerina", "Language"};
        invokeFunction("testFromHandleWithHandle", new BHandleValue(values));
    }

    private void assertTest(Object value1, Object value2, BValue[] returns) {
        Assert.assertEquals(returns.length, 1);
        Object returnObj = ((BHandleValue) returns[0]).getValue();
        Assert.assertEquals(returnObj, value1);
        Assert.assertEquals(returnObj, value2);
    }

    private void invokeFunction(String functionName, BHandleValue value) {
        BValue[] args = new BValue[1];
        args[0] = value;
        BValue[] returns = BRunUtil.invoke(result, functionName, args);
        Assert.assertEquals(returns.length, 1);
        BBoolean result = (BBoolean) returns[0];
        Assert.assertTrue(result.booleanValue());
    }
}
