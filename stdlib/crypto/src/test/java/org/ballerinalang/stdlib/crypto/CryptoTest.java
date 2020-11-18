/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.stdlib.crypto;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test cases for ballerina.crypto native functions.
 */
public class CryptoTest {

    private static final int KEY_SIZE = 16; // Set to 16 to ensure compatibility with older JDKs

    private CompileResult compileResult;
    private Path confRoot;

    @BeforeClass
    public void setup() {
        String resourceRoot = Paths.get("src", "test", "resources").toAbsolutePath().toString();
        Path sourceRoot = Paths.get(resourceRoot, "test-src");
        confRoot = Paths.get(resourceRoot, "datafiles");
        compileResult = BCompileUtil.compileOffline(sourceRoot.resolve("crypto-test.bal").toString());
    }

    @Test(description = "Test hmac generation functions")
    public void testHmac() throws DecoderException {
        byte[] message = "Ballerina HMAC test".getBytes(StandardCharsets.UTF_8);
        byte[] key = "abcdefghijk".getBytes(StandardCharsets.UTF_8);

        BValueArray messageValue = new BValueArray(message);
        BValueArray keyValue = new BValueArray(key);

        byte[] expectedMD5Hash = Hex.decodeHex("3D5AC29160F2905A5C8153597798A4C1".toCharArray());
        byte[] expectedSHA1Hash = Hex.decodeHex("13DD8D54D0EB702EDC6E8EDCAF616837D3A51499".toCharArray());
        byte[] expectedSHA256Hash = Hex
                .decodeHex("2651203E18BF0088D3EF1215022D147E2534FD4BAD5689C9E5F12436E9758B15".toCharArray());
        byte[] expectedSHA384Hash = Hex.decodeHex(("c27a281dffed3d4d176646d7261e9f6268a3d40a237cd274fc2f5970f637f1c" +
                "bc20a3835d7b7aa7401308737f23a9bf7").toCharArray());
        byte[] expectedSHA512Hash = Hex.decodeHex(("78d99bf3e5277fc893af6cd6b0487c33ed3abc4f956fdd1fada302f135b012a" +
                "3c71cadaaeb462e51ff281202bdfa8807719b91f69742c3f71f036c469ac5b918").toCharArray());

        BValue[] args = {messageValue, keyValue};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testHmacWithMD5", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), expectedMD5Hash);

        args = new BValue[]{messageValue, keyValue};
        returnValues = BRunUtil.invoke(compileResult, "testHmacWithSHA1", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), expectedSHA1Hash);

        args = new BValue[]{messageValue, keyValue};
        returnValues = BRunUtil.invoke(compileResult, "testHmacWithSHA256", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), expectedSHA256Hash);

        args = new BValue[]{messageValue, keyValue};
        returnValues = BRunUtil.invoke(compileResult, "testHmacWithSHA384", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), expectedSHA384Hash);

        args = new BValue[]{messageValue, keyValue};
        returnValues = BRunUtil.invoke(compileResult, "testHmacWithSHA512", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), expectedSHA512Hash);
    }

    @Test(description = "Test hmac generation with an empty password", expectedExceptions = BLangRuntimeException.class)
    public void testHmacNegativeInvalidKey() {
        BValue[] args = {new BValueArray("Ballerina HMAC test".getBytes(StandardCharsets.UTF_8)),
                new BValueArray("".getBytes(StandardCharsets.UTF_8))};
        BRunUtil.invoke(compileResult, "testHmacWithSHA1", args);
    }

    @Test(description = "Test hashing functions")
    public void testHashing() throws DecoderException {
        byte[] expectedMd5Hash = Hex.decodeHex("3B12196DB784CD9F86CC635D32764FDF".toCharArray());
        byte[] expectedSha1Hash = Hex.decodeHex("73FBC15DB28D52C03359EDE7A7DC40B4A83DF207".toCharArray());
        byte[] expectedSha256Hash = Hex
                .decodeHex("68F6CA0B55B55099331BF4EAA659B8BDC94FBDCE2F54D94FD90DA8240797A5D7".toCharArray());
        byte[] expectedSha384Hash = Hex.decodeHex(("F00B4A8C67B38E7E32FF8B1AB570345743878F7ADED9B5FA02518DDD84E16CBC" +
                "A344AF42CB60A1FD5C48C5FEDCFF7F24").toCharArray());
        byte[] expectedSha512Hash = Hex.decodeHex(("1C9BED7C87E7D17BA07ADD67F59B4A29AFD2B046409B65429E77D0CEE53A33C5" +
                "E26731DC1CB091FAADA8C5D6433CB1544690804CC046A55D6AFED8BE0B901062").toCharArray());

        BValue[] args = {new BValueArray("Ballerina test".getBytes(StandardCharsets.UTF_8))};

        BValue[] returnValues = BRunUtil.invoke(compileResult, "testHashWithMD5", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), expectedMd5Hash);

        returnValues = BRunUtil.invoke(compileResult, "testHashWithSHA1", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), expectedSha1Hash);

        returnValues = BRunUtil.invoke(compileResult, "testHashWithSHA256", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), expectedSha256Hash);

        returnValues = BRunUtil.invoke(compileResult, "testHashWithSHA384", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), expectedSha384Hash);

        returnValues = BRunUtil.invoke(compileResult, "testHashWithSHA512", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), expectedSha512Hash);
    }

    @Test(description = "Test CRC32b generation")
    public void testCRC32() {
        byte[] payload = "Ballerina test".getBytes(StandardCharsets.UTF_8);
        String expectedCRC32Hash = "d37b9692";

        BValue[] returnValues = BRunUtil.invoke(compileResult, "testHashWithCRC32b",
                new BValue[]{new BValueArray(payload)});
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(returnValues[0].stringValue(), expectedCRC32Hash);
    }

    @Test(description = "Test RSA-SHA1 signing")
    public void testSignRsaSha1() throws DecoderException {
        byte[] expectedSignature = Hex.decodeHex(("70728d6d37fd83704bcb2649d93cfd20dbadb83a9d2169965d2a241795a131f" +
                "cfdb8b1b4f35f5de3c1f6f1d71ea0c9f80e494627b4c01d6e670ae4698b774171e8a017d62847c92aa47e868c230532af" +
                "9fc3a681387eead94578d2287674940df2e2f4a28f59688257254dfaab81c17617357ae05b42898412136abed116d6b86" +
                "eab68ff4ace029b67c7e4c5784a9bad00129b69d5afb6a89cb596cad56e8c98a1642eab87cb337980cc987708800e62a4" +
                "27c6f61828437d5491549b05025e9a98bf27825dc6002068678dde1e7d365407881b2b1a4d4e522a53f69e5b43202299e" +
                "02f7840f8991b8c335b0332b3b4bd658030ec3007f6f36c190b8663d3b746")
                .toCharArray());

        byte[] payload = "Ballerina test".getBytes(StandardCharsets.UTF_8);

        BValue[] returnValues = BRunUtil.invoke(compileResult, "testSignRsaSha1",
                new BValue[]{new BValueArray(payload),
                        new BString(confRoot.resolve("testKeystore.p12").toString()),
                        new BString("ballerina"), new BString("ballerina"), new BString("ballerina")});
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), expectedSignature);

        returnValues = BRunUtil.invoke(compileResult, "testVerifyRsaSha1",
                new BValue[]{new BValueArray(payload), returnValues[0],
                        new BString(confRoot.resolve("testKeystore.p12").toString()),
                        new BString("ballerina"), new BString("ballerina")});
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BBoolean) returnValues[0]).booleanValue(), true);
    }

    @Test(description = "Test RSA-SHA256 signing")
    public void testSignRsaSha256() throws DecoderException {
        byte[] expectedSignature = Hex.decodeHex(("34477f0e0a5457ca1a95049da10d59baa33ee4fa9e1bb8be3d3c70d82b980850" +
                "fd017a1c9984a97384736aacfe33d39ff8d63e01b952972910c86135b7558a2274c6d772f0d2fcdc0ac4aabc75f3978edb" +
                "d4aabd17d6447fb88e83b055bbff24d8212125b760c8bf88e9e4908645434f53a2ab0e3d5517c8e3241d8ebabbc767e7d9" +
                "24b5481621831f3a63e06c393c9378d782406705cd8823e12d3b4042a3cb738b8a8bb5731ff2934394c928c4262d130af6" +
                "6a2b507fc538bd16bccabc2f3b95137370dcca31e80866533bf445cf7f63aec6a9fa596333abb3a59d9b327891c7e6016e" +
                "0c11ef2a0d32088d4683d915005c9dcc8137611e5bff9dc4a5db6f87")
                .toCharArray());

        byte[] payload = "Ballerina test".getBytes(StandardCharsets.UTF_8);

        BValue[] returnValues = BRunUtil.invoke(compileResult, "testSignRsaSha256",
                new BValue[]{new BValueArray(payload),
                        new BString(confRoot.resolve("testKeystore.p12").toString()),
                        new BString("ballerina"), new BString("ballerina"), new BString("ballerina")});
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), expectedSignature);

        returnValues = BRunUtil.invoke(compileResult, "testVerifyRsaSha256",
                new BValue[]{new BValueArray(payload), returnValues[0],
                        new BString(confRoot.resolve("testKeystore.p12").toString()),
                        new BString("ballerina"), new BString("ballerina")});
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BBoolean) returnValues[0]).booleanValue(), true);
    }

    @Test(description = "Test RSA-384 signing")
    public void testSignRsaSha384() throws DecoderException {
        byte[] expectedSignature = Hex.decodeHex(("4981CC5213F384E8DB7950BF76C97AE20FA2A34244A517FC585B2381B9E88" +
                "278E447B92F6F452332BCA65DD5D6CCE04B5AC51D92E7E820B6FB826870DFBA437BBDA7F0E5850C02F72A8644DA8382" +
                "237E8C1ABD50A4BAEE179C8C838EA4AC53D2223B3C57D7D463A8E1BBFFC43F3F3C44494850377A8668E156B2D23B6E0" +
                "D8132632E3D79D68A391F619EF2E1E986A455F8F27092C66029C98D001A81FFE3E4B00991E7F0C0141D0635275544FC" +
                "5BF70A40C12B7BC765F6209C9640A60B9E978AD8DEC551983F5773A72327DF1A6256BEB8DF50A03F89443123E1354A9" +
                "EF7D8F8BF0659E1D6B77916B4AEEC79989AFDAA2F5B8983DE476C1A0FFBB2B647DE449E")
                .toCharArray());

        byte[] payload = "Ballerina test".getBytes(StandardCharsets.UTF_8);

        BValue[] returnValues = BRunUtil.invoke(compileResult, "testSignRsaSha384",
                new BValue[]{new BValueArray(payload),
                        new BString(confRoot.resolve("testKeystore.p12").toString()),
                        new BString("ballerina"), new BString("ballerina"), new BString("ballerina")});
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), expectedSignature);

        returnValues = BRunUtil.invoke(compileResult, "testVerifyRsaSha384",
                new BValue[]{new BValueArray(payload), returnValues[0],
                        new BString(confRoot.resolve("testKeystore.p12").toString()),
                        new BString("ballerina"), new BString("ballerina")});
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BBoolean) returnValues[0]).booleanValue(), true);
    }

    @Test(description = "Test RSA-512 signing")
    public void testSignRsaSha512() throws DecoderException {
        byte[] expectedSignature = Hex.decodeHex(("6995ba8d2382a8c4f0ed513033126b2305df419a8b105ee60483243229d2c496" +
                "b7f670783c52068cd2b4b8c2392f2932c682f30057cb4d8d616ba3a142356b0394747b2a3642da4d23447bb997eacb086f" +
                "173b4045ee8ee014e1e667e34522defb7a4ac1b5b3f175d40a409d947d562fcf7b2b2631d273751a0f8c658bd8c1d1d23a" +
                "0dbe685b15e13abf45f998114577c85a6478d915a445645a6360944e4962c56bee79d2363931c77f8040c620692debc747" +
                "4c1e62d9d4b0b39fa664b8c3a32155c7c1966ef3d55993ad8f7f3bf4d929cf047ab91344facefeba944b043e1e31496753" +
                "9cb2e6e669ec3352073a8933a2a0cac6056b4997b3628132f7a7e553")
                .toCharArray());

        byte[] payload = "Ballerina test".getBytes(StandardCharsets.UTF_8);

        BValue[] returnValues = BRunUtil.invoke(compileResult, "testSignRsaSha512",
                new BValue[]{new BValueArray(payload),
                        new BString(confRoot.resolve("testKeystore.p12").toString()),
                        new BString("ballerina"), new BString("ballerina"), new BString("ballerina")});
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), expectedSignature);

        returnValues = BRunUtil.invoke(compileResult, "testVerifyRsaSha512",
                new BValue[]{new BValueArray(payload), returnValues[0],
                        new BString(confRoot.resolve("testKeystore.p12").toString()),
                        new BString("ballerina"), new BString("ballerina")});
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BBoolean) returnValues[0]).booleanValue(), true);
    }

    @Test(description = "Test RSA-MD5 signing")
    public void testSignRsaMd5() throws DecoderException {
        byte[] expectedSignature = Hex.decodeHex(("457050eca794baf2149f53631f373525fbc7b40de83e0af5b03473e7b726064b" +
                "3eb6a8b7ce48218e4adaf2b598429236192a458ad5cef1ab2f456164f2646ba57a1ce6b858403504ddc49915bf8bf34558" +
                "0366bd9f7d1d777572fcacd3aa935267af6cf5dc988668b8cea0f57cd0e286658f0ca7c060d7a68b6330bc590b6db59489" +
                "aa676b1c539e5bb0116c64a963f8a03789b9fd7e689bac5576eea15d93d45be3547aef7c7dc26251dfa7bdf23b47c6a346" +
                "ae3603c158cbd32ff9298df71f930cebdda8564199e948f1ac03173e9f9d425240c7f99857d5f469dd0b23c0248b4fa42e" +
                "67145ec0e6e8abfc3f7f10122cc278b5469eb970034483839f290eec")
                .toCharArray());

        byte[] payload = "Ballerina test".getBytes(StandardCharsets.UTF_8);

        BValue[] returnValues = BRunUtil.invoke(compileResult, "testSignRsaMd5",
                new BValue[]{new BValueArray(payload),
                        new BString(confRoot.resolve("testKeystore.p12").toString()),
                        new BString("ballerina"), new BString("ballerina"), new BString("ballerina")});
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), expectedSignature);

        returnValues = BRunUtil.invoke(compileResult, "testVerifyRsaMd5",
                new BValue[]{new BValueArray(payload), returnValues[0],
                        new BString(confRoot.resolve("testKeystore.p12").toString()),
                        new BString("ballerina"), new BString("ballerina")});
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BBoolean) returnValues[0]).booleanValue(), true);
    }

    //
    // RSA Signing Related Tests
    //

    @Test(description = "Test RSA-SHA1 signing with an invalid private key")
    public void testSignRsaSha1WithInvalidKey() {
        byte[] payload = "Ballerina test".getBytes(StandardCharsets.UTF_8);
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testSignRsaSha1WithInvalidKey",
                new BValue[]{new BValueArray(payload)});
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BError) returnValues[0]).getMessage(), "Uninitialized private key: Key must not be null");
    }

    @Test(description = "Test RSA-SHA256 signing with an invalid private key")
    public void testSignRsaSha256WithInvalidKey() {
        byte[] payload = "Ballerina test".getBytes(StandardCharsets.UTF_8);
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testSignRsaSha256WithInvalidKey",
                new BValue[]{new BValueArray(payload)});
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertTrue(((BError) returnValues[0]).getMessage().contains("Uninitialized private key:"));
    }

    @Test(description = "Test RSA-SHA384 signing with an invalid private key")
    public void testSignRsaSha384WithInvalidKey() {
        byte[] payload = "Ballerina test".getBytes(StandardCharsets.UTF_8);
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testSignRsaSha384WithInvalidKey",
                new BValue[]{new BValueArray(payload)});
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertTrue(((BError) returnValues[0]).getMessage().contains("Uninitialized private key:"));
    }

    @Test(description = "Test RSA-SHA512 signing with an invalid private key")
    public void testSignRsaSha512WithInvalidKey() {
        byte[] payload = "Ballerina test".getBytes(StandardCharsets.UTF_8);
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testSignRsaSha512WithInvalidKey",
                new BValue[]{new BValueArray(payload)});
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertTrue(((BError) returnValues[0]).getMessage().contains("Uninitialized private key:"));
    }

    @Test(description = "Test RSA-MD5 signing with an invalid private key")
    public void testSignRsaMd5WithInvalidKey() {
        byte[] payload = "Ballerina test".getBytes(StandardCharsets.UTF_8);
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testSignRsaMd5WithInvalidKey",
                new BValue[]{new BValueArray(payload)});
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BError) returnValues[0]).getMessage(), "Uninitialized private key: Key must not be null");
    }

    //
    // AES CBC Encryption Related Tests
    //

    @Test(description = "Test encrypt and decrypt with AES CBC NoPadding")
    public void testEncryptAesCbcNoPadding() {
        byte[] message = "Ballerina crypto test           ".getBytes(StandardCharsets.UTF_8);
        byte[] key = new byte[KEY_SIZE];
        for (int i = 0; i < KEY_SIZE; i++) {
            key[i] = (byte) i;
        }

        byte[] iv = new byte[16];
        for (int i = 0; i < 16; i++) {
            iv[i] = (byte) i;
        }
        BValueArray ivValue = new BValueArray(iv);
        BValueArray messageValue = new BValueArray(message);
        BValueArray keyValue = new BValueArray(key);

        BValue[] args = {messageValue, keyValue, ivValue, new BString("NONE")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testEncryptAesCbc", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertTrue(returnValues[0] instanceof BValueArray);

        BValue[] args1 = {returnValues[0], keyValue, ivValue, new BString("NONE")};
        returnValues = BRunUtil.invoke(compileResult, "testDecryptAesCbc", args1);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), message);
    }

    @Test(description = "Test encrypt and decrypt with AES CBC NoPadding using invalid key size")
    public void testEncryptAesCbcNoPaddingWithInvalidKeySize() {
        byte[] message = "Ballerina crypto test           ".getBytes(StandardCharsets.UTF_8);
        byte[] key = new byte[31];
        for (int i = 0; i < 31; i++) {
            key[i] = (byte) i;
        }

        byte[] iv = new byte[16];
        for (int i = 0; i < 16; i++) {
            iv[i] = (byte) i;
        }
        BValueArray ivValue = new BValueArray(iv);
        BValueArray messageValue = new BValueArray(message);
        BValueArray keyValue = new BValueArray(key);

        BValue[] args = {messageValue, keyValue, ivValue, new BString("NONE")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testEncryptAesCbc", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertTrue(returnValues[0] instanceof BError);
        Assert.assertEquals(((BError) returnValues[0]).getMessage(),
                "Invalid key size. valid key sizes in bytes: [16, 24, 32]");
    }

    @Test(description = "Test encrypt and decrypt with AES CBC NoPadding using invalid IV length")
    public void testEncryptAesCbcNoPaddingWithInvalidIvLength() {
        byte[] message = "Ballerina crypto test           ".getBytes(StandardCharsets.UTF_8);
        byte[] key = new byte[KEY_SIZE];
        for (int i = 0; i < KEY_SIZE; i++) {
            key[i] = (byte) i;
        }

        byte[] iv = new byte[15];
        for (int i = 0; i < 15; i++) {
            iv[i] = (byte) i;
        }
        BValueArray ivValue = new BValueArray(iv);
        BValueArray messageValue = new BValueArray(message);
        BValueArray keyValue = new BValueArray(key);

        BValue[] args = {messageValue, keyValue, ivValue, new BString("NONE")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testEncryptAesCbc", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertTrue(returnValues[0] instanceof BError);
        Assert.assertEquals(((BError) returnValues[0]).getMessage(),
                "Error occurred while AES encrypt/decrypt: Wrong IV length: must be 16 bytes long");
    }

    @Test(description = "Test encrypt and decrypt with AES CBC NoPadding using invalid input length")
    public void testEncryptAesCbcNoPaddingWithInvalidInputLength() {
        byte[] message = "Ballerina crypto test".getBytes(StandardCharsets.UTF_8);
        byte[] key = new byte[KEY_SIZE];
        for (int i = 0; i < KEY_SIZE; i++) {
            key[i] = (byte) i;
        }

        byte[] iv = new byte[16];
        for (int i = 0; i < 16; i++) {
            iv[i] = (byte) i;
        }
        BValueArray ivValue = new BValueArray(iv);
        BValueArray messageValue = new BValueArray(message);
        BValueArray keyValue = new BValueArray(key);

        BValue[] args = {messageValue, keyValue, ivValue, new BString("NONE")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testEncryptAesCbc", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertTrue(returnValues[0] instanceof BError);
        Assert.assertEquals(((BError) returnValues[0]).getMessage(),
                "Error occurred while AES encrypt/decrypt: Input length not multiple of 16 bytes");
    }

    @Test(description = "Test encrypt and decrypt with AES CBC PKCS5")
    public void testEncryptAesCbcPkcs5() {
        byte[] message = "Ballerina crypto test".getBytes(StandardCharsets.UTF_8);
        byte[] key = new byte[KEY_SIZE];
        for (int i = 0; i < KEY_SIZE; i++) {
            key[i] = (byte) i;
        }

        byte[] iv = new byte[16];
        for (int i = 0; i < 16; i++) {
            iv[i] = (byte) i;
        }
        BValueArray ivValue = new BValueArray(iv);
        BValueArray messageValue = new BValueArray(message);
        BValueArray keyValue = new BValueArray(key);

        BValue[] args = {messageValue, keyValue, ivValue, new BString("PKCS5")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testEncryptAesCbc", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertTrue(returnValues[0] instanceof BValueArray);

        BValue[] args1 = {returnValues[0], keyValue, ivValue, new BString("PKCS5")};
        returnValues = BRunUtil.invoke(compileResult, "testDecryptAesCbc", args1);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), message);
    }

    @Test(description = "Test encrypt and decrypt with AES CBC PKCS1")
    public void testEncryptAesCbcPkcs1() {
        byte[] message = "Ballerina crypto test".getBytes(StandardCharsets.UTF_8);
        byte[] key = new byte[32];
        for (int i = 0; i < 32; i++) {
            key[i] = (byte) i;
        }

        byte[] iv = new byte[16];
        for (int i = 0; i < 16; i++) {
            iv[i] = (byte) i;
        }
        BValueArray ivValue = new BValueArray(iv);
        BValueArray messageValue = new BValueArray(message);
        BValueArray keyValue = new BValueArray(key);

        BValue[] args = {messageValue, keyValue, ivValue, new BString("PKCS1")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testEncryptAesCbc", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertTrue(returnValues[0] instanceof BError);
        Assert.assertEquals(((BError) returnValues[0]).getMessage(),
                "Unsupported algorithm: AES CBC PKCS1: Cannot find any provider supporting AES/CBC/PKCS1Padding");
    }

    //
    // AES ECB Encryption Related Tests
    //

    @Test(description = "Test encrypt and decrypt with AES ECB NoPadding")
    public void testEncryptAesEcbNoPadding() {
        byte[] message = "Ballerina crypto test           ".getBytes(StandardCharsets.UTF_8);
        byte[] key = new byte[KEY_SIZE];
        for (int i = 0; i < KEY_SIZE; i++) {
            key[i] = (byte) i;
        }

        BValueArray messageValue = new BValueArray(message);
        BValueArray keyValue = new BValueArray(key);

        BValue[] args = {messageValue, keyValue, new BString("NONE")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testEncryptAesEcb", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertTrue(returnValues[0] instanceof BValueArray);

        BValue[] args1 = {returnValues[0], keyValue, new BString("NONE")};
        returnValues = BRunUtil.invoke(compileResult, "testDecryptAesEcb", args1);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), message);
    }

    @Test(description = "Test encrypt and decrypt with AES ECB NoPadding using invalid key size")
    public void testEncryptAesEcbNoPaddingWithInvalidKeySize() {
        byte[] message = "Ballerina crypto test           ".getBytes(StandardCharsets.UTF_8);
        byte[] key = new byte[31];
        for (int i = 0; i < 31; i++) {
            key[i] = (byte) i;
        }

        BValueArray messageValue = new BValueArray(message);
        BValueArray keyValue = new BValueArray(key);

        BValue[] args = {messageValue, keyValue, new BString("NONE")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testEncryptAesEcb", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertTrue(returnValues[0] instanceof BError);
        Assert.assertEquals(((BError) returnValues[0]).getMessage(),
                "Invalid key size. valid key sizes in bytes: [16, 24, 32]");
    }

    @Test(description = "Test encrypt and decrypt with AES ECB NoPadding using invalid input length")
    public void testEncryptAesEcbNoPaddingWithInvalidInputLength() {
        byte[] message = "Ballerina crypto test".getBytes(StandardCharsets.UTF_8);
        byte[] key = new byte[KEY_SIZE];
        for (int i = 0; i < KEY_SIZE; i++) {
            key[i] = (byte) i;
        }

        BValueArray messageValue = new BValueArray(message);
        BValueArray keyValue = new BValueArray(key);

        BValue[] args = {messageValue, keyValue, new BString("NONE")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testEncryptAesEcb", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertTrue(returnValues[0] instanceof BError);
        Assert.assertEquals(((BError) returnValues[0]).getMessage(),
                "Error occurred while AES encrypt/decrypt: Input length not multiple of 16 bytes");
    }

    @Test(description = "Test encrypt and decrypt with AES ECB PKCS5")
    public void testEncryptAesEcbPkcs5() {
        byte[] message = "Ballerina crypto test".getBytes(StandardCharsets.UTF_8);
        byte[] key = new byte[KEY_SIZE];
        for (int i = 0; i < KEY_SIZE; i++) {
            key[i] = (byte) i;
        }

        BValueArray messageValue = new BValueArray(message);
        BValueArray keyValue = new BValueArray(key);

        BValue[] args = {messageValue, keyValue, new BString("PKCS5")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testEncryptAesEcb", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertTrue(returnValues[0] instanceof BValueArray);

        BValue[] args1 = {returnValues[0], keyValue, new BString("PKCS5")};
        returnValues = BRunUtil.invoke(compileResult, "testDecryptAesEcb", args1);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), message);
    }

    @Test(description = "Test encrypt and decrypt with AES ECB PKCS1")
    public void testEncryptAesEcbPkcs1() {
        byte[] message = "Ballerina crypto test".getBytes(StandardCharsets.UTF_8);
        byte[] key = new byte[32];
        for (int i = 0; i < 32; i++) {
            key[i] = (byte) i;
        }

        BValueArray messageValue = new BValueArray(message);
        BValueArray keyValue = new BValueArray(key);

        BValue[] args = {messageValue, keyValue, new BString("PKCS1")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testEncryptAesEcb", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertTrue(returnValues[0] instanceof BError);
        Assert.assertEquals(((BError) returnValues[0]).getMessage(),
                "Unsupported algorithm: AES ECB PKCS1: Cannot find any provider supporting AES/ECB/PKCS1Padding");
    }

    //
    // AES GCM Encryption Related Tests
    //

    @Test(description = "Test encrypt and decrypt with AES GCM NoPadding")
    public void testEncryptAesGcmNoPadding() {
        byte[] message = "Ballerina crypto test           ".getBytes(StandardCharsets.UTF_8);
        byte[] key = new byte[KEY_SIZE];
        for (int i = 0; i < KEY_SIZE; i++) {
            key[i] = (byte) i;
        }

        byte[] iv = new byte[16];
        for (int i = 0; i < 16; i++) {
            iv[i] = (byte) i;
        }
        BValueArray ivValue = new BValueArray(iv);
        BValueArray messageValue = new BValueArray(message);
        BValueArray keyValue = new BValueArray(key);

        BValue[] args = {messageValue, keyValue, ivValue, new BString("NONE"), new BInteger(128)};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testEncryptAesGcm", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertTrue(returnValues[0] instanceof BValueArray);

        BValue[] args1 = {returnValues[0], keyValue, ivValue, new BString("NONE"), new BInteger(128)};
        returnValues = BRunUtil.invoke(compileResult, "testDecryptAesGcm", args1);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), message);
    }


    @Test(description = "Test encrypt and decrypt with AES GCM NoPadding using invalid key size")
    public void testEncryptAesGcmNoPaddingWithInvalidKeySize() {
        byte[] message = "Ballerina crypto test           ".getBytes(StandardCharsets.UTF_8);
        byte[] key = new byte[31];
        for (int i = 0; i < 31; i++) {
            key[i] = (byte) i;
        }

        byte[] iv = new byte[16];
        for (int i = 0; i < 16; i++) {
            iv[i] = (byte) i;
        }
        BValueArray ivValue = new BValueArray(iv);
        BValueArray messageValue = new BValueArray(message);
        BValueArray keyValue = new BValueArray(key);

        BValue[] args = {messageValue, keyValue, ivValue, new BString("NONE"), new BInteger(128)};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testEncryptAesGcm", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertTrue(returnValues[0] instanceof BError);
        Assert.assertEquals(((BError) returnValues[0]).getMessage(),
                "Invalid key size. valid key sizes in bytes: [16, 24, 32]");
    }

    @Test(description = "Test encrypt and decrypt with AES GCM NoPadding using invalid input length")
    public void testEncryptAesGcmNoPaddingWithInvalidInputLength() {
        byte[] message = "Ballerina crypto test".getBytes(StandardCharsets.UTF_8);
        byte[] key = new byte[KEY_SIZE];
        for (int i = 0; i < KEY_SIZE; i++) {
            key[i] = (byte) i;
        }

        byte[] iv = new byte[16];
        for (int i = 0; i < 16; i++) {
            iv[i] = (byte) i;
        }
        BValueArray ivValue = new BValueArray(iv);
        BValueArray messageValue = new BValueArray(message);
        BValueArray keyValue = new BValueArray(key);

        BValue[] args = {messageValue, keyValue, ivValue, new BString("NONE"), new BInteger(128)};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testEncryptAesGcm", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertTrue(returnValues[0] instanceof BValueArray);

        BValue[] args1 = {returnValues[0], keyValue, ivValue, new BString("NONE"), new BInteger(128)};
        returnValues = BRunUtil.invoke(compileResult, "testDecryptAesGcm", args1);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), message);
    }

    @Test(description = "Test encrypt and decrypt with AES GCM PKCS5")
    public void testEncryptAesGcmPkcs5() {
        byte[] message = "Ballerina crypto test".getBytes(StandardCharsets.UTF_8);
        byte[] key = new byte[KEY_SIZE];
        for (int i = 0; i < KEY_SIZE; i++) {
            key[i] = (byte) i;
        }

        byte[] iv = new byte[16];
        for (int i = 0; i < 16; i++) {
            iv[i] = (byte) i;
        }
        BValueArray ivValue = new BValueArray(iv);
        BValueArray messageValue = new BValueArray(message);
        BValueArray keyValue = new BValueArray(key);

        BValue[] args = {messageValue, keyValue, ivValue, new BString("PKCS5"), new BInteger(128)};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testEncryptAesGcm", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertTrue(returnValues[0] instanceof BValueArray);

        BValue[] args1 = {returnValues[0], keyValue, ivValue, new BString("PKCS5"), new BInteger(128)};
        returnValues = BRunUtil.invoke(compileResult, "testDecryptAesGcm", args1);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), message);
    }

    @Test(description = "Test encrypt and decrypt with AES GCM PKCS1")
    public void testEncryptAesGcmPkcs1() {
        byte[] message = "Ballerina crypto test".getBytes(StandardCharsets.UTF_8);
        byte[] key = new byte[32];
        for (int i = 0; i < 32; i++) {
            key[i] = (byte) i;
        }

        byte[] iv = new byte[16];
        for (int i = 0; i < 16; i++) {
            iv[i] = (byte) i;
        }
        BValueArray ivValue = new BValueArray(iv);
        BValueArray messageValue = new BValueArray(message);
        BValueArray keyValue = new BValueArray(key);

        BValue[] args = {messageValue, keyValue, ivValue, new BString("PKCS1"), new BInteger(128)};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testEncryptAesGcm", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertTrue(returnValues[0] instanceof BError);
        Assert.assertEquals(((BError) returnValues[0]).getMessage(),
                "Unsupported algorithm: AES GCM PKCS1: Cannot find any provider supporting AES/GCM/PKCS1Padding");
    }

    @Test(description = "Test encrypt and decrypt with AES GCM PKCS5 with invalid tag value")
    public void testEncryptAesGcmPkcs5WithInvalidTagLength() {
        byte[] message = "Ballerina crypto test".getBytes(StandardCharsets.UTF_8);
        byte[] key = new byte[32];
        for (int i = 0; i < 32; i++) {
            key[i] = (byte) i;
        }

        byte[] iv = new byte[16];
        for (int i = 0; i < 16; i++) {
            iv[i] = (byte) i;
        }
        BValueArray ivValue = new BValueArray(iv);
        BValueArray messageValue = new BValueArray(message);
        BValueArray keyValue = new BValueArray(key);

        BValue[] args = {messageValue, keyValue, ivValue, new BString("PKCS5"), new BInteger(500)};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testEncryptAesGcm", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertTrue(returnValues[0] instanceof BError);
        Assert.assertTrue(
                ((BError) returnValues[0]).getMessage().startsWith("Invalid tag size. valid tag sizes in bytes:"));
    }

    //
    // RSA CBC Encryption Related Tests
    //

    @Test(description = "Test encrypt and decrypt with RSA ECB PKCS1")
    public void testEncryptRsaEcbPkcs1() {
        byte[] message = "Ballerina crypto test           ".getBytes(StandardCharsets.UTF_8);
        BValueArray messageValue = new BValueArray(message);

        BValue[] args = {messageValue, new BString(confRoot.resolve("testKeystore.p12").toString()),
                new BString("ballerina"), new BString("ballerina"), new BString("PKCS1")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testEncryptRsaEcb", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertTrue(returnValues[0] instanceof BValueArray);

        BValue[] args1 = {returnValues[0], new BString(confRoot.resolve("testKeystore.p12").toString()),
                new BString("ballerina"), new BString("ballerina"), new BString("ballerina"), new BString("PKCS1")};
        returnValues = BRunUtil.invoke(compileResult, "testDecryptRsaEcb", args1);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), message);
    }

    @Test(description = "Test encrypt and decrypt with RSA ECB OAEPwithMD5andMGF1")
    public void testEncryptRsaEcbOAEPwithMD5andMGF1() {
        byte[] message = "Ballerina crypto test           ".getBytes(StandardCharsets.UTF_8);
        BValueArray messageValue = new BValueArray(message);

        BValue[] args = {messageValue, new BString(confRoot.resolve("testKeystore.p12").toString()),
                new BString("ballerina"), new BString("ballerina"), new BString("OAEPwithMD5andMGF1")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testEncryptRsaEcb", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertTrue(returnValues[0] instanceof BValueArray);

        BValue[] args1 = {returnValues[0], new BString(confRoot.resolve("testKeystore.p12").toString()),
                new BString("ballerina"), new BString("ballerina"), new BString("ballerina"),
                new BString("OAEPwithMD5andMGF1")};
        returnValues = BRunUtil.invoke(compileResult, "testDecryptRsaEcb", args1);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), message);
    }

    @Test(description = "Test encrypt and decrypt with RSA ECB OAEPWithSHA1AndMGF1")
    public void testEncryptRsaEcbOAEPWithSHA1AndMGF1() {
        byte[] message = "Ballerina crypto test           ".getBytes(StandardCharsets.UTF_8);
        BValueArray messageValue = new BValueArray(message);

        BValue[] args = {messageValue, new BString(confRoot.resolve("testKeystore.p12").toString()),
                new BString("ballerina"), new BString("ballerina"), new BString("OAEPWithSHA1AndMGF1")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testEncryptRsaEcb", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertTrue(returnValues[0] instanceof BValueArray);

        BValue[] args1 = {returnValues[0], new BString(confRoot.resolve("testKeystore.p12").toString()),
                new BString("ballerina"), new BString("ballerina"), new BString("ballerina"),
                new BString("OAEPWithSHA1AndMGF1")};
        returnValues = BRunUtil.invoke(compileResult, "testDecryptRsaEcb", args1);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), message);
    }

    @Test(description = "Test encrypt and decrypt with RSA ECB OAEPWithSHA256AndMGF1")
    public void testEncryptRsaEcbOAEPWithSHA256AndMGF1() {
        byte[] message = "Ballerina crypto test           ".getBytes(StandardCharsets.UTF_8);
        BValueArray messageValue = new BValueArray(message);

        BValue[] args = {messageValue, new BString(confRoot.resolve("testKeystore.p12").toString()),
                new BString("ballerina"), new BString("ballerina"), new BString("OAEPWithSHA256AndMGF1")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testEncryptRsaEcb", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertTrue(returnValues[0] instanceof BValueArray);

        BValue[] args1 = {returnValues[0], new BString(confRoot.resolve("testKeystore.p12").toString()),
                new BString("ballerina"), new BString("ballerina"), new BString("ballerina"),
                new BString("OAEPWithSHA256AndMGF1")};
        returnValues = BRunUtil.invoke(compileResult, "testDecryptRsaEcb", args1);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), message);
    }

    @Test(description = "Test encrypt and decrypt with RSA ECB OAEPwithSHA384andMGF1")
    public void testEncryptRsaEcbOAEPwithSHA384andMGF1() {
        byte[] message = "Ballerina crypto test           ".getBytes(StandardCharsets.UTF_8);
        BValueArray messageValue = new BValueArray(message);

        BValue[] args = {messageValue, new BString(confRoot.resolve("testKeystore.p12").toString()),
                new BString("ballerina"), new BString("ballerina"), new BString("OAEPwithSHA384andMGF1")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testEncryptRsaEcb", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertTrue(returnValues[0] instanceof BValueArray);

        BValue[] args1 = {returnValues[0], new BString(confRoot.resolve("testKeystore.p12").toString()),
                new BString("ballerina"), new BString("ballerina"), new BString("ballerina"),
                new BString("OAEPwithSHA384andMGF1")};
        returnValues = BRunUtil.invoke(compileResult, "testDecryptRsaEcb", args1);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), message);
    }

    @Test(description = "Test encrypt and decrypt with RSA ECB OAEPwithSHA512andMGF1")
    public void testEncryptRsaEcbOAEPwithSHA512andMGF1() {
        byte[] message = "Ballerina crypto test           ".getBytes(StandardCharsets.UTF_8);
        BValueArray messageValue = new BValueArray(message);

        BValue[] args = {messageValue, new BString(confRoot.resolve("testKeystore.p12").toString()),
                new BString("ballerina"), new BString("ballerina"), new BString("OAEPwithSHA512andMGF1")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testEncryptRsaEcb", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertTrue(returnValues[0] instanceof BValueArray);

        BValue[] args1 = {returnValues[0], new BString(confRoot.resolve("testKeystore.p12").toString()),
                new BString("ballerina"), new BString("ballerina"), new BString("ballerina"),
                new BString("OAEPwithSHA512andMGF1")};
        returnValues = BRunUtil.invoke(compileResult, "testDecryptRsaEcb", args1);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), message);
    }

    @Test(description = "Test encrypt with private key and decrypt with public key using RSA ECB PKCS1")
    public void testEncryptRsaEcbWithPrivateKeyPkcs1() {
        byte[] message = "Ballerina crypto test           ".getBytes(StandardCharsets.UTF_8);
        BValueArray messageValue = new BValueArray(message);

        BValue[] args = {messageValue, new BString(confRoot.resolve("testKeystore.p12").toString()),
                new BString("ballerina"), new BString("ballerina"), new BString("ballerina"), new BString("PKCS1")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testEncryptRsaEcbWithPrivateKey", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertTrue(returnValues[0] instanceof BValueArray);

        BValue[] args1 = {returnValues[0], new BString(confRoot.resolve("testKeystore.p12").toString()),
                new BString("ballerina"), new BString("ballerina"), new BString("PKCS1")};
        returnValues = BRunUtil.invoke(compileResult, "testDecryptRsaEcbWithPublicKey", args1);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), message);
    }

    @Test(description = "Test encrypt and decrypt with RSA ECB PKCS1 with an invalid key")
    public void testEncryptRsaEcbWithPrivateKeyPkcs1WithInvalidKey() {
        byte[] message = "Ballerina crypto test           ".getBytes(StandardCharsets.UTF_8);
        BValueArray messageValue = new BValueArray(message);

        BValue[] args = {messageValue, new BString("PKCS1")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testEncryptRsaEcbWithInvalidKey", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BError) returnValues[0]).getMessage(), "Uninitialized private/public key");
    }

    @Test(description = "Test encrypt and decrypt with RSA ECB PKCS1 with invalid padding")
    public void testEncryptRsaEcbWithPrivateKeyPkcs1WithInvalidPadding() {
        byte[] message = "Ballerina crypto test           ".getBytes(StandardCharsets.UTF_8);
        BValueArray messageValue = new BValueArray(message);

        BValue[] args = {messageValue, new BString(confRoot.resolve("testKeystore.p12").toString()),
                new BString("ballerina"), new BString("ballerina"), new BString("PKCS99")};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testEncryptRsaEcb", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BError) returnValues[0]).getMessage(),
                "Error occurred while RSA encrypt/decrypt: Unsupported padding: PKCS99");
    }
}
