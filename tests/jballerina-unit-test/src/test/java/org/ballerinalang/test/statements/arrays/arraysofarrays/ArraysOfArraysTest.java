/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.statements.arrays.arraysofarrays;

import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for user arrays of arrays in ballerina.
 */
public class ArraysOfArraysTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/arrays/arraysofarrays/arrays-of-arrays.bal");
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testValueAssignmentAndRetrieval() {
//        BValue[] args = new BValue[]{new BInteger(1), new BInteger(2)};
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(result, "valueAssignmentAndRetrieval", args);
        Assert.assertEquals(returns[0].stringValue(), "3");
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testArrayInitializationAndRetrieval() {
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(result, "arrayInitializationAndRetrieval", args);
        Assert.assertEquals(returns[0].stringValue(), "1");
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testArrayToArrayAssignment() {
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(result, "arrayToArrayAssignment", args);
        Assert.assertEquals(returns[0].stringValue(), "9");
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testThreeDarrayTest() {
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(result, "threeDarray", args);
        Assert.assertEquals(returns[0].stringValue(), "2");
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testThreeDarrayValueAccess() {
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(result, "threeDarrayValueAccess", args);
        Assert.assertEquals(returns[0].stringValue(), "99");
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testThreeDarrayStringValueAccess() {
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(result, "threeDarrayStringValueAccess", args);
        Assert.assertEquals(returns[0].stringValue(), "string");
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testTwoDarrayFunctionCalltest() {
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(result, "twoDarrayFunctionCalltest", args);
        Assert.assertEquals(returns[0].stringValue(), "4");
    }

    @Test(description = "Test setting incorrect type")
    public void testAssignIncorrectValue() {
        CompileResult result =
                BCompileUtil.compile("test-src/statements/arrays/arraysofarrays/arrays-of-arrays-failures.bal");
        Assert.assertEquals(result.getErrorCount(), 1);
        BAssertUtil.validateError(result, 0, "incompatible types: expected 'string[]', found 'int[]'", 3, 22);
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testTwoDarrayStruct() {
//        BValue[] returns = BLangFunctions.invokeNew(programFile, "twoDarrayStructTest");
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(result, "twoDarrayStructTest", args);
        Assert.assertEquals(returns[0].stringValue(), "2");
    }

    @Test(description = "Test Nested array initializations")
    public void testVNestedArrayInit() {
//        BValue[] returns = BLangFunctions.invokeNew(programFile, "nestedArrayInit");
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(result, "nestedArrayInit", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 12);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 6);
    }

    @Test(description = "Test nested string array iteration")
    public void testStringArrayIterator() {
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(result, "testStringArrayIterator", args);
        Assert.assertEquals((returns[0]).stringValue(), "BBBBBB");
    }

    @Test(description = "Test nested string array iteration")
    public void testIntArrayIterator() {
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(result, "testIntArrayIterator", args);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 45);
    }

    @Test(description = "Test nested string array iteration")
    public void testFloatArrayIterator() {
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(result, "testFloatArrayIterator", args);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 27.1);
    }

    @Test(description = "Test nested string array iteration")
    public void testBlobArrayIterator() {
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(result, "testByteArrayIterator", args);
        assertJBytesWithBBytes(hexStringToByteArray("aa"), (BValueArray) returns[0]);
    }

    @Test(description = "Test nested string array iteration")
    public void testRefArrayIterator() {
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(result, "testRefArrayIterator", args);
        Assert.assertEquals(((BMap<String, BValue>) returns[0]).get("name").stringValue(), "ballerina");
    }

    @Test(description = "Test multi-dimension array unions")
    public void testArrayUnion() {
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(result, "testArrayUnion", args);
        Assert.assertEquals(((BValueArray) (((BValueArray) returns[0]).getRefValue(0))).getBoolean(0), 0);
        Assert.assertEquals(((BValueArray) (((BValueArray) returns[1]).getRefValue(0))).getString(0), "scope1");
    }

    @Test(description = "Test multi-dimension array unions with objects")
    public void testObjectArrayUnion() {
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(result, "testObjectArrayUnion", args);
        Assert.assertEquals(((BMap) (((BValueArray) returns[0]).getRefValue(0))).get("fooId1").stringValue(), "Foo1");
        Assert.assertEquals(((BMap) (((BValueArray) returns[0]).getRefValue(0))).get("fooId2").stringValue(), "Foo2");
    }

    private static byte[] hexStringToByteArray(String str) {
        int len = str.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i + 1), 16));
        }
        return data;
    }

    private void assertJBytesWithBBytes(byte[] jBytes, BValueArray bBytes) {
        for (int i = 0; i < jBytes.length; i++) {
            Assert.assertEquals(bBytes.getByte(i), jBytes[i], "Invalid byte value returned.");
        }
    }
}
