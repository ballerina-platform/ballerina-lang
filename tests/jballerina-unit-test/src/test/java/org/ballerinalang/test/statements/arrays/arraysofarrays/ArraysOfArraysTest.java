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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
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
//        Object[] args = new Object[]{(1), (2)};
        Object[] args = new Object[0];
        Object returns = BRunUtil.invoke(result, "valueAssignmentAndRetrieval", args);
        Assert.assertEquals(returns.toString(), "3");
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testArrayInitializationAndRetrieval() {
        Object[] args = new Object[0];
        Object returns = BRunUtil.invoke(result, "arrayInitializationAndRetrieval", args);
        Assert.assertEquals(returns.toString(), "1");
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testArrayToArrayAssignment() {
        Object[] args = new Object[0];
        Object returns = BRunUtil.invoke(result, "arrayToArrayAssignment", args);
        Assert.assertEquals(returns.toString(), "9");
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testThreeDarrayTest() {
        Object[] args = new Object[0];
        Object returns = BRunUtil.invoke(result, "threeDarray", args);
        Assert.assertEquals(returns.toString(), "2");
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testThreeDarrayValueAccess() {
        Object[] args = new Object[0];
        Object returns = BRunUtil.invoke(result, "threeDarrayValueAccess", args);
        Assert.assertEquals(returns.toString(), "99");
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testThreeDarrayStringValueAccess() {
        Object[] args = new Object[0];
        Object returns = BRunUtil.invoke(result, "threeDarrayStringValueAccess", args);
        Assert.assertEquals(returns.toString(), "string");
    }

    @Test(description = "Test Basic arrays of arrays operations")
    public void testTwoDarrayFunctionCalltest() {
        Object[] args = new Object[0];
        Object returns = BRunUtil.invoke(result, "twoDarrayFunctionCalltest", args);
        Assert.assertEquals(returns.toString(), "4");
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
//        Object returns = BLangFunctions.invokeNew(programFile, "twoDarrayStructTest");
        Object[] args = new Object[0];
        Object returns = BRunUtil.invoke(result, "twoDarrayStructTest", args);
        Assert.assertEquals(returns.toString(), "2");
    }

    @Test(description = "Test Nested array initializations")
    public void testVNestedArrayInit() {
//        Object returns = BLangFunctions.invokeNew(programFile, "nestedArrayInit");
        Object[] args = new Object[0];
        BArray returns = (BArray) BRunUtil.invoke(result, "nestedArrayInit", args);
        Assert.assertEquals(returns.get(0), 12L);
        Assert.assertEquals(returns.get(1), 6L);
    }

    @Test(description = "Test nested string array iteration")
    public void testStringArrayIterator() {
        Object[] args = new Object[0];
        Object returns = BRunUtil.invoke(result, "testStringArrayIterator", args);
        Assert.assertEquals((returns).toString(), "BBBBBB");
    }

    @Test(description = "Test nested string array iteration")
    public void testIntArrayIterator() {
        Object[] args = new Object[0];
        Object returns = BRunUtil.invoke(result, "testIntArrayIterator", args);
        Assert.assertEquals(returns, 45L);
    }

    @Test(description = "Test nested string array iteration")
    public void testFloatArrayIterator() {
        Object[] args = new Object[0];
        Object returns = BRunUtil.invoke(result, "testFloatArrayIterator", args);
        Assert.assertEquals(returns, 27.1);
    }

    @Test(description = "Test nested string array iteration")
    public void testBlobArrayIterator() {
        Object[] args = new Object[0];
        Object returns = BRunUtil.invoke(result, "testByteArrayIterator", args);
        assertJBytesWithBBytes(hexStringToByteArray("aa"), (BArray) returns);
    }

    @Test(description = "Test nested string array iteration")
    public void testRefArrayIterator() {
        Object[] args = new Object[0];
        Object returns = BRunUtil.invoke(result, "testRefArrayIterator", args);
        Assert.assertEquals(((BMap<String, Object>) returns).get(StringUtils.fromString("name")).toString(),
                "ballerina");
    }

    @Test(description = "Test multi-dimension array unions")
    public void testArrayUnion() {
        Object[] args = new Object[0];
        BArray returns = (BArray) BRunUtil.invoke(result, "testArrayUnion", args);
        Assert.assertFalse(((BArray) (((BArray) returns.get(0)).getRefValue(0))).getBoolean(0));
        Assert.assertEquals(((BArray) (((BArray) returns.get(1)).getRefValue(0))).getString(0), "scope1");
    }

    @Test(description = "Test multi-dimension array unions with objects")
    public void testObjectArrayUnion() {
        Object[] args = new Object[0];
        Object returns = BRunUtil.invoke(result, "testObjectArrayUnion", args);
        BObject bMap = (BObject) ((BArray) (((BArray) returns).getRefValue(0))).get(0);
        Assert.assertEquals(bMap.get(StringUtils.fromString("fooId1")).toString(), "Foo1");
        Assert.assertEquals(bMap.get(StringUtils.fromString("fooId2")).toString(), "Foo2");
    }

    private static byte[] hexStringToByteArray(String str) {
        int len = str.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i + 1), 16));
        }
        return data;
    }

    private void assertJBytesWithBBytes(byte[] jBytes, BArray bBytes) {
        for (int i = 0; i < jBytes.length; i++) {
            Assert.assertEquals(bBytes.getByte(i), jBytes[i], "Invalid byte value returned.");
        }
    }
}
