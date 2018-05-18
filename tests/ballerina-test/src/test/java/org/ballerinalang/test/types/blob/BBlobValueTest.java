/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.types.blob;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBlob;
import org.ballerinalang.model.values.BBlobArray;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * This test class will test the blob values.
 */
public class BBlobValueTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/blob/blob-value.bal");
    }

    @Test(description = "Test blob value assignment")
    public void testBlobParameter() {
        byte[] bytes = "string".getBytes();
        BValue[] args = {new BBlob(bytes)};
        BValue[] returns = BRunUtil.invoke(result, "testBlobParameter", args);
        Assert.assertEquals(returns.length, 1);
        assertResult(bytes, returns[0]);
    }

    @Test(description = "Test blob array")
    public void testBlobArray() {
        byte[] bytes1 = "string1".getBytes();
        byte[] bytes2 = "string2".getBytes();
        BValue[] args = {new BBlob(bytes1), new BBlob(bytes2)};
        BValue[] returns = BRunUtil.invoke(result, "testBlobParameterArray", args);
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBlob.class);
        Assert.assertSame(returns[1].getClass(), BBlob.class);
        BBlob blob1 = (BBlob) returns[0];
        BBlob blob2 = (BBlob) returns[1];
        Assert.assertEquals(blob1.blobValue(), bytes1, "Invalid byte value returned.");
        Assert.assertEquals(blob2.blobValue(), bytes2, "Invalid byte value returned.");
    }

    @Test(description = "Test blob global variable1")
    public void testBlobGlobalVariable1() {
        byte[] bytes = "string".getBytes();
        BValue[] args = {new BBlob(bytes)};
        BValue[] returns = BRunUtil.invoke(result, "testGlobalVariable1", args);
        Assert.assertEquals(returns.length, 1);
        assertResult(bytes, returns[0]);
    }

    @Test(description = "Test blob global variable2")
    public void testBlobGlobalVariable2() {
        String b16 = "aeeecdefabcd12345567888822";
        byte[] bytes = hexStringToByteArray(b16);
        BValue[] returns = BRunUtil.invoke(result, "testGlobalVariable2", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        assertResult(bytes, returns[0]);
    }

    @Test(description = "Test blob global variable3")
    public void testBlobGlobalVariable3() {
        String b64 = "aGVsbG8gYmFsbGVyaW5hICEhIQ==";
        byte[] bytes = decode(b64);
        BValue[] returns = BRunUtil.invoke(result, "testGlobalVariable3", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        assertResult(bytes, returns[0]);
    }

    @Test(description = "Test blob tuple return 1")
    public void testBlobReturnTuple1() {
        String b1 = "aaabafac23345678";
        String b2 = "a4f5njn/jnfvr+d=";
        byte[] bytes1 = hexStringToByteArray(b1);
        byte[] bytes2 = decode(b2);
        BValue[] returns = BRunUtil.invoke(result, "testBlobReturnTuple1", new BValue[]{});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BBlob.class);
        Assert.assertSame(returns[1].getClass(), BBlob.class);
        BBlob blob1 = (BBlob) returns[0];
        BBlob blob2 = (BBlob) returns[1];
        Assert.assertEquals(blob1.blobValue(), bytes1, "Invalid byte value returned.");
        Assert.assertEquals(blob2.blobValue(), bytes2, "Invalid byte value returned.");
    }

    @Test(description = "Test blob tuple return 2")
    public void testBlobReturnTuple2() {
        String b0 = "aaab";
        String b1 = "a4f5";
        String b2 = "aeeecdefabcd12345567888822";
        String b3 = "aGVsbG8gYmFsbGVyaW5hICEhIQ==";
        byte[] bytes0 = hexStringToByteArray(b0);
        byte[] bytes1 = decode(b1);
        byte[] bytes2 = hexStringToByteArray(b2);
        byte[] bytes3 = decode(b3);
        BValue[] returns = BRunUtil.invoke(result, "testBlobReturnTuple2", new BValue[]{});
        Assert.assertEquals(returns.length, 4);
        assertResult(bytes0, returns[0]);
        assertResult(bytes1, returns[1]);
        assertResult(bytes2, returns[2]);
        assertResult(bytes3, returns[3]);
    }

    private void assertResult(byte[] bytes, BValue aReturn) {
        Assert.assertSame(aReturn.getClass(), BBlob.class);
        BBlob blob = (BBlob) aReturn;
        Assert.assertEquals(blob.blobValue(), bytes, "Invalid byte value returned.");
    }

    @Test(description = "Test return blob array")
    public void testBlobReturnArray() {
        String b1 = "aaab34dfca1267";
        String b2 = "aaabcfccadafcd34bdfabcdferf=";
        byte[] bytes1 = hexStringToByteArray(b1);
        byte[] bytes2 = decode(b2);
        BValue[] returns = BRunUtil.invoke(result, "testBlobReturnArray", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBlobArray.class);
        BBlobArray blobArray = (BBlobArray) returns[0];
        Assert.assertEquals(blobArray.get(0), bytes1, "Invalid byte value returned.");
        Assert.assertEquals(blobArray.get(1), bytes2, "Invalid byte value returned.");
    }

    @Test(description = "Test blob field variable 1")
    public void testBlobField1() {
        byte[] bytes = "string".getBytes();
        BValue[] args = {new BBlob(bytes)};
        BValue[] returns = BRunUtil.invoke(result, "testBlobField1", args);
        Assert.assertEquals(returns.length, 1);
        assertResult(bytes, returns[0]);
    }

    @Test(description = "Test blob global variable 2")
    public void testBlobField2() {
        String b16 = "aaabcfccadafcd341a4bdfabcd8912df";
        byte[] bytes = hexStringToByteArray(b16);
        BValue[] returns = BRunUtil.invoke(result, "testBlobField2", new BValue[]{});
        Assert.assertEquals(returns.length, 1);
        assertResult(bytes, returns[0]);
    }

    @Test(description = "Test blob hexadecimal format")
    public void testBlobHexFormat() {
        String b16 = "aaabcfccadafcd341a4bdfabcd8912df";
        String b64 = "aGVsbG8gYmFsbGVyaW5hICEhIQ==";
        String b64HexStr = byteArrayToHexString(decode(b64));
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
        byte[] bytes1 = hexStringToByteArray(b16);
        byte[] bytes2 = decode(b64);
        BValue[] returns = BRunUtil.invoke(result, "testBlobAssign", new BValue[]{});
        Assert.assertEquals(returns.length, 2);
        BBlob blob1 = (BBlob) returns[0];
        BBlob blob2 = (BBlob) returns[1];
        Assert.assertEquals(blob1.blobValue(), bytes2, "Invalid value returned.");
        Assert.assertEquals(blob2.blobValue(), bytes1, "Invalid value returned.");
    }

    @Test(description = "Test blob default value")
    public void testBlobDefaultValue() {
        String b0 = "aaab";
        String b1 = "aGVsbG8gYmFsbGVyaW5hICEhIQ==";
        byte[] empty = new byte[0];
        byte[] bytes0 = hexStringToByteArray(b0);
        byte[] bytes1 = decode(b1);
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

    private static byte[] decode(String b64) {
        return Base64.getDecoder().decode(b64.getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] hexStringToByteArray(String str) {
        int len = str.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i + 1), 16));
        }
        return data;
    }

    private static String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
