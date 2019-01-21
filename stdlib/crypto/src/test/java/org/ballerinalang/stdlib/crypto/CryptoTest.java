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
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;

/**
 * Test cases for ballerina.crypto native functions.
 */
public class CryptoTest {

    private CompileResult compileResult;

    private static final byte [] PRIVATE_KEY_BYTES = ("-----BEGIN RSA PRIVATE KEY-----\n" +
            "MIICXAIBAAKBgQCqGKukO1De7zhZj6+H0qtjTkVxwTCpvKe4eCZ0FPqri0cb2JZfXJ/DgYSF6vUp\n" +
            "wmJG8wVQZKjeGcjDOL5UlsuusFncCzWBQ7RKNUSesmQRMSGkVb1/3j+skZ6UtW+5u09lHNsj6tQ5\n" +
            "1s1SPrCBkedbNf0Tp0GbMJDyR4e9T04ZZwIDAQABAoGAFijko56+qGyN8M0RVyaRAXz++xTqHBLh\n" +
            "3tx4VgMtrQ+WEgCjhoTwo23KMBAuJGSYnRmoBZM3lMfTKevIkAidPExvYCdm5dYq3XToLkkLv5L2\n" +
            "pIIVOFMDG+KESnAFV7l2c+cnzRMW0+b6f8mR1CJzZuxVLL6Q02fvLi55/mbSYxECQQDeAw6fiIQX\n" +
            "GukBI4eMZZt4nscy2o12KyYner3VpoeE+Np2q+Z3pvAMd/aNzQ/W9WaI+NRfcxUJrmfPwIGm63il\n" +
            "AkEAxCL5HQb2bQr4ByorcMWm/hEP2MZzROV73yF41hPsRC9m66KrheO9HPTJuo3/9s5p+sqGxOlF\n" +
            "L0NDt4SkosjgGwJAFklyR1uZ/wPJjj611cdBcztlPdqoxssQGnh85BzCj/u3WqBpE2vjvyyvyI5k\n" +
            "X6zk7S0ljKtt2jny2+00VsBerQJBAJGC1Mg5Oydo5NwD6BiROrPxGo2bpTbu/fhrT8ebHkTz2epl\n" +
            "U9VQQSQzY1oZMVX8i1m5WUTLPz2yLJIBQVdXqhMCQBGoiuSoSjafUhV7i1cEGpb88h5NBYZzWXGZ\n" +
            "37sJ5QsW+sJyoNde3xH8vdXhzU7eT82D6X/scw9RZz+/6rCJ4p0=\n" +
            "-----END RSA PRIVATE KEY-----").getBytes(StandardCharsets.UTF_8);

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/crypto/crypto-test.bal");
    }

    @Test
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

    @Test(expectedExceptions = BLangRuntimeException.class)
    public void testHmacNegativeInvalidKey() {
        BValue[] args = {new BValueArray("Ballerina HMAC test".getBytes(StandardCharsets.UTF_8)),
                new BValueArray("".getBytes(StandardCharsets.UTF_8))};
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testHmacWithSHA1", args);
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                "Invalid return value");
    }

    @Test
    public void testHashing() throws DecoderException {
        byte[] expectedMd5Hash = Hex.decodeHex("3B12196DB784CD9F86CC635D32764FDF".toCharArray());
        byte[] expectedSha1Hash = Hex.decodeHex("73FBC15DB28D52C03359EDE7A7DC40B4A83DF207".toCharArray());
        byte[] expectedSha256Hash = Hex
                .decodeHex("68F6CA0B55B55099331BF4EAA659B8BDC94FBDCE2F54D94FD90DA8240797A5D7".toCharArray());
        byte[] expectedSha384Hash = Hex.decodeHex(("F00B4A8C67B38E7E32FF8B1AB570345743878F7ADED9B5FA02518DDD84E16CBC" +
                "A344AF42CB60A1FD5C48C5FEDCFF7F24").toCharArray());
        byte[] expectedSha512hash = Hex.decodeHex(("1C9BED7C87E7D17BA07ADD67F59B4A29AFD2B046409B65429E77D0CEE53A33C5" +
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
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), expectedSha512hash);
    }

    @Test(description = "Testing CRC32b generation")
    public void testCRC32() {
        byte[] payload = "Ballerina test".getBytes(StandardCharsets.UTF_8);
        String expectedCRC32Hash = "d37b9692";

        BValue[] returnValues = BRunUtil.invoke(compileResult, "testHashWithCRC32b",
                new BValue[]{new BValueArray(payload)});
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(returnValues[0].stringValue(), expectedCRC32Hash);
    }

    @Test
    public void testSignRsaSha1() throws DecoderException {
        byte[] expectedSignature = Hex.decodeHex(("529789110a9841ea54ad6ea99e7577d2521a925a920a44a0b55f8a5025f556f1" +
                "e150e47294038889b0aff859f87cac05b5d8b26c7aac4086c3a39deb4e3b265f222f21bdf8cba3ac557b6090ebdfcb04e1" +
                "0ba35957c3a7dbf27637f97cca5de5ca8839e56801d75b065206f0d3f079255adc6b53696dc81164e099b563cb8232")
                .toCharArray());

        byte[] payload = "Ballerina test".getBytes(StandardCharsets.UTF_8);

        BValue[] returnValues = BRunUtil.invoke(compileResult, "testSignRsaSha1",
                new BValue[]{new BValueArray(payload), new BValueArray(PRIVATE_KEY_BYTES)});
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), expectedSignature);
    }

    @Test
    public void testSignRsaSha256() throws DecoderException {
        byte[] expectedSignature = Hex.decodeHex(("4deba1d6e2a8a2b5789ca8fe6f8fe9a259b93eefc01452b89c99aea8c0dee08b64" +
                "112b83c5a70ed1175416305a756ac753e615b3482d7a72baeee3452a27e5f2f2e129f95ead265e46bfdb79000b3c9efd3dbb" +
                "5f7e50aef62940f9d9216457a39a30158c1f2ff991cbd3e87afef81409370a3421919f051364fc0274e093c425")
                .toCharArray());

        byte[] payload = "Ballerina test".getBytes(StandardCharsets.UTF_8);

        BValue[] returnValues = BRunUtil.invoke(compileResult, "testSignRsaSha256",
                new BValue[]{new BValueArray(payload), new BValueArray(PRIVATE_KEY_BYTES)});
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), expectedSignature);
    }

    @Test
    public void testSignRsaSha512() throws DecoderException {
        byte[] expectedSignature = Hex.decodeHex(("a00f6cf07a13ef256dfbaa0120949047d1be036d16221bbc63750032189279b0" +
                "a2496554bed85b185ee6e71190e0a475f9c9889d19e8ea38a39686f31253df1f7367fb2df44644af270685abca188a4d51" +
                "02a9211ef9e777aebec6d41a22ad810f24c22a6f0d61072123ed77b6dca633ec3828278116377b8bedd90b0fcbb473")
                .toCharArray());

        byte[] payload = "Ballerina test".getBytes(StandardCharsets.UTF_8);

        BValue[] returnValues = BRunUtil.invoke(compileResult, "testSignRsaSha512",
                new BValue[]{new BValueArray(payload), new BValueArray(PRIVATE_KEY_BYTES)});
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), expectedSignature);
    }

    @Test
    public void testSignRsaMd5() throws DecoderException {
        byte[] expectedSignature = Hex.decodeHex(("2db5cacf8c26a3f963309dc8722ada3b3ee8bdb1da39d8b9036ef6c0b932d9ef" +
                "a5ae42304f9b88781a3850ea455bc68cbfb4cacc7d0f6ceb57a1e255f6c11dc8d9753eae0fbfc52058462c32feedd52bf4" +
                "b7d85a91e3b1a58f73b974eec266efedab85edf5281d0147c6e2f226fddeeaa7d44629c89375f9c154c6e31018e75f")
                .toCharArray());

        byte[] payload = "Ballerina test".getBytes(StandardCharsets.UTF_8);

        BValue[] returnValues = BRunUtil.invoke(compileResult, "testSignRsaMd5",
                new BValue[]{new BValueArray(payload), new BValueArray(PRIVATE_KEY_BYTES)});
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null);
        Assert.assertEquals(((BValueArray) returnValues[0]).getBytes(), expectedSignature);
    }
}
