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

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BByteArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.ByteArrayUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;

/**
 * This test class will test byte array values.
 */
public class BByteArrayValueTest {

    private CompileResult result;

    private static final String content = "This is a sample string";

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/byte/byte-array-value.bal");
    }

    @Test(description = "Test blob value assignment")
    public void testBlobParameter() {
        byte[] bytes = "string".getBytes();
        BValue[] args = {ByteArrayUtils.createBByteArray(bytes)};
        BValue[] returns = BRunUtil.invoke(result, "testBlobParameter", args);
        Assert.assertEquals(returns.length, 1);
        assertResult(bytes, returns[0]);
    }

    @Test(description = "Test blob array")
    public void testBlobArray() {
        byte[] bytes1 = "string1".getBytes();
        byte[] bytes2 = "string2".getBytes();
        BByteArray byteArray1 = ByteArrayUtils.createBByteArray(bytes1);
        BByteArray byteArray2 = ByteArrayUtils.createBByteArray(bytes2);
        BValue[] args = {byteArray1, byteArray2};
        BValue[] returns = BRunUtil.invoke(result, "testBlobParameterArray", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BByteArray.class);
        Assert.assertSame(returns[1].getClass(), BByteArray.class);
        BByteArray blob1 = (BByteArray) returns[0];
        BByteArray blob2 = (BByteArray) returns[1];
        ByteArrayUtils.assertJBytesWithBBytes(bytes1, blob1);
        ByteArrayUtils.assertJBytesWithBBytes(bytes2, blob2);
    }

    @Test(description = "Test blob global variable1")
    public void testBlobGlobalVariable1() {
        byte[] bytes = "string".getBytes();
        BValue[] args = {ByteArrayUtils.createBByteArray(bytes)};
        BValue[] returns = BRunUtil.invoke(result, "testGlobalVariable1", args);
        Assert.assertEquals(returns.length, 1);
        assertResult(bytes, returns[0]);
    }

    @Test(description = "Test blob global variable2")
    public void testBlobGlobalVariable2() {
        String b16 = "aeeecdefabcd12345567888822";
        byte[] bytes = ByteArrayUtils.hexStringToByteArray(b16);
        BValue[] returns = BRunUtil.invoke(result, "testGlobalVariable2", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        assertResult(bytes, returns[0]);
    }

    @Test(description = "Test blob global variable3")
    public void testBlobGlobalVariable3() {
        String b64 = "aGVsbG8gYmFsbGVyaW5hICEhIQ==";
        byte[] bytes = ByteArrayUtils.decodeBase64(b64);
        BValue[] returns = BRunUtil.invoke(result, "testGlobalVariable3", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        assertResult(bytes, returns[0]);
    }

    @Test(description = "Test blob tuple return 1")
    public void testBlobReturnTuple1() {
        String b1 = "aaabafac23345678";
        String b2 = "a4f5njn/jnfvr+d=";
        byte[] bytes1 = ByteArrayUtils.hexStringToByteArray(b1);
        byte[] bytes2 = ByteArrayUtils.decodeBase64(b2);
        BValue[] returns = BRunUtil.invoke(result, "testBlobReturnTuple1", new BValue[]{});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BByteArray.class);
        Assert.assertSame(returns[1].getClass(), BByteArray.class);
        BByteArray blob1 = (BByteArray) returns[0];
        BByteArray blob2 = (BByteArray) returns[1];
        ByteArrayUtils.assertJBytesWithBBytes(bytes1, blob1);
        ByteArrayUtils.assertJBytesWithBBytes(bytes2, blob2);
    }

    @Test(description = "Test blob tuple return 2")
    public void testBlobReturnTuple2() {
        String b0 = "aaab";
        String b1 = "a4f5";
        String b2 = "aeeecdefabcd12345567888822";
        String b3 = "aGVsbG8gYmFsbGVyaW5hICEhIQ==";
        byte[] bytes0 = ByteArrayUtils.hexStringToByteArray(b0);
        byte[] bytes1 = ByteArrayUtils.decodeBase64(b1);
        byte[] bytes2 = ByteArrayUtils.hexStringToByteArray(b2);
        byte[] bytes3 = ByteArrayUtils.decodeBase64(b3);
        BValue[] returns = BRunUtil.invoke(result, "testBlobReturnTuple2", new BValue[]{});
        Assert.assertEquals(returns.length, 4);
        assertResult(bytes0, returns[0]);
        assertResult(bytes1, returns[1]);
        assertResult(bytes2, returns[2]);
        assertResult(bytes3, returns[3]);
    }

    private void assertResult(byte[] bytes, BValue aReturn) {
        Assert.assertSame(aReturn.getClass(), BByteArray.class);
        BByteArray blob = (BByteArray) aReturn;
        ByteArrayUtils.assertJBytesWithBBytes(bytes, blob);
    }

    @Test(description = "Test return blob array")
    public void testBlobReturnArray() {
        String b1 = "aaab34dfca1267";
        String b2 = "aaabcfccadafcd34bdfabcdferf=";
        byte[] bytes1 = ByteArrayUtils.hexStringToByteArray(b1);
        byte[] bytes2 = ByteArrayUtils.decodeBase64(b2);
        BValue[] returns = BRunUtil.invoke(result, "testBlobReturnArray", new BValue[]{});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BByteArray.class);
        Assert.assertSame(returns[1].getClass(), BByteArray.class);
        BByteArray blobArray1 = (BByteArray) returns[0];
        BByteArray blobArray2 = (BByteArray) returns[1];
        ByteArrayUtils.assertJBytesWithBBytes(bytes1, blobArray1);
        ByteArrayUtils.assertJBytesWithBBytes(bytes2, blobArray2);
    }

    @Test(description = "Test blob field variable 1")
    public void testBlobField1() {
        byte[] bytes = "string".getBytes();
        BValue[] args = {ByteArrayUtils.createBByteArray(bytes)};
        BValue[] returns = BRunUtil.invoke(result, "testBlobField1", args);
        Assert.assertEquals(returns.length, 1);
        assertResult(bytes, returns[0]);
    }

    @Test(description = "Test blob global variable 2")
    public void testBlobField2() {
        String b16 = "aaabcfccadafcd341a4bdfabcd8912df";
        byte[] bytes = ByteArrayUtils.hexStringToByteArray(b16);
        BValue[] returns = BRunUtil.invoke(result, "testBlobField2", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        assertResult(bytes, returns[0]);
    }

    @Test(description = "Test blob hexadecimal format")
    public void testBlobHexFormat() {
        String b16 = "aaabcfccadafcd341a4bdfabcd8912df";
        String b64 = "aGVsbG8gYmFsbGVyaW5hICEhIQ==";
        String b64HexStr = ByteArrayUtils.byteArrayToHexString(ByteArrayUtils.decodeBase64(b64));
        BValue[] returns = BRunUtil.invoke(result, "testBlobHexFormat", new BValue[]{});
        Assert.assertEquals(returns.length, 3);
        BString blob1 = (BString) returns[0];
        BString blob2 = (BString) returns[1];
        BString blob3 = (BString) returns[2];
        Assert.assertEquals(blob1.stringValue(), b16, "Invalid value returned.");
        Assert.assertEquals(blob2.stringValue(), b16.toUpperCase(), "Invalid value returned.");
        Assert.assertEquals(blob3.stringValue(), b64HexStr, "Invalid value returned.");
    }

    @Test(description = "Test blob assign")
    public void testBlobAssign() {
        String b16 = "aaabcfccadafcd341a4bdfabcd8912df";
        String b64 = "aGVsbG8gYmFsbGVyaW5hICEhIQ==";
        byte[] bytes1 = ByteArrayUtils.hexStringToByteArray(b16);
        byte[] bytes2 = ByteArrayUtils.decodeBase64(b64);
        BValue[] returns = BRunUtil.invoke(result, "testBlobAssign", new BValue[]{});
        Assert.assertEquals(returns.length, 2);
        BByteArray blob1 = (BByteArray) returns[0];
        BByteArray blob2 = (BByteArray) returns[1];
        ByteArrayUtils.assertJBytesWithBBytes(bytes2, blob1);
        ByteArrayUtils.assertJBytesWithBBytes(bytes1, blob2);
    }

    @Test(description = "Test blob default value")
    public void testBlobDefaultValue() {
        String b0 = "aaab";
        String b1 = "aGVsbG8gYmFsbGVyaW5hICEhIQ==";
        byte[] empty = new byte[0];
        byte[] bytes0 = ByteArrayUtils.hexStringToByteArray(b0);
        byte[] bytes1 = ByteArrayUtils.decodeBase64(b1);
        BValue[] returns = BRunUtil.invoke(result, "testBlobDefaultValue", new BValue[]{});
        Assert.assertEquals(returns.length, 8);
        assertResult(empty, returns[0]);
        assertResult(empty, returns[1]);
        assertResult(bytes0, returns[2]);
        assertResult(bytes1, returns[3]);
        assertResult(empty, returns[4]);
        assertResult(bytes0, returns[5]);
        assertResult(bytes1, returns[6]);
        assertResult(empty, returns[7]);
    }

    @Test(description = "Test byte array literal value")
    public void testByteArrayLiteral() {
        byte[] bytes = new byte[]{1, 27, 34, (byte) 145, (byte) 224};
        BValue[] returns = BRunUtil.invoke(result, "testByteArrayLiteral", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        assertResult(bytes, returns[0]);
    }

    @Test(description = "Test equality of byte array returned and their size")
    public void testByteArrayReturn() {
        byte[] bytes1 = ByteArrayUtils.hexStringToByteArray("aaabcfccadafcd341a4bdfabcd8912df");
        byte[] bytes2 = ByteArrayUtils.decodeBase64("aGVsbG8gYmFsbGVyaW5hICEhIQ==");
        byte[] bytes3 = new byte[]{3, 4, 5, 6, 7, 8, 9};
        BValue[] returns = BRunUtil.invoke(result, "testByteArrayReturn", new BValue[]{});
        Assert.assertEquals(returns.length, 3);
        Assert.assertSame(returns[0].getClass(), BByteArray.class);
        Assert.assertSame(returns[1].getClass(), BByteArray.class);
        Assert.assertSame(returns[2].getClass(), BByteArray.class);
        BByteArray byteArray1 = (BByteArray) returns[0];
        BByteArray byteArray2 = (BByteArray) returns[1];
        BByteArray byteArray3 = (BByteArray) returns[2];
        Assert.assertEquals(bytes1.length, byteArray1.size());
        ByteArrayUtils.assertJBytesWithBBytes(bytes1, byteArray1.getBytes());
        Assert.assertEquals(bytes2.length, byteArray2.size());
        ByteArrayUtils.assertJBytesWithBBytes(bytes2, byteArray2.getBytes());
        Assert.assertEquals(bytes3.length, byteArray3.size());
        ByteArrayUtils.assertJBytesWithBBytes(bytes3, byteArray3.getBytes());
    }

    @Test(description = "Get string representation of byte array 1")
    public void testByteArrayToString1() throws UnsupportedEncodingException {

        BValue[] args = {new BByteArray(content.getBytes("UTF-8"))};
        BValue[] returns = BRunUtil.invoke(result, "testByteArrayToString1", args);

        Assert.assertEquals(returns[0].stringValue(), content);
    }

    @Test(description = "Get string representation of byte array 2")
    public void testByteArrayToString2() throws UnsupportedEncodingException {

        BValue[] args = {new BByteArray(content.getBytes("UTF-8")), new BString("UTF-8")};
        BValue[] returns = BRunUtil.invoke(result, "testByteArrayToString2", args);

        Assert.assertEquals(returns[0].stringValue(), content);
    }

    @Test(description = "Get string representation of byte array 3")
    public void testByteArrayToString3() throws UnsupportedEncodingException {

        BValue[] args = {new BByteArray(content.getBytes("UTF-16")), new BString("UTF-16")};
        BValue[] returns = BRunUtil.invoke(result, "testByteArrayToString2", args);

        Assert.assertEquals(returns[0].stringValue(), content);
    }
}
