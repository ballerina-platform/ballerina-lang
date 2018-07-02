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

package org.ballerinalang.test.utils;

import org.ballerinalang.model.values.BByteArray;
import org.testng.Assert;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * A util class that is useful with byte array related operations.
 */
public class ByteArrayUtils {

    public static byte[] decodeBase64(String b64) {
        return Base64.getDecoder().decode(b64.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] hexStringToByteArray(String str) {
        int len = str.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i + 1), 16));
        }
        return data;
    }

    public static String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static BByteArray createBByteArray(byte[] bytes) {
        BByteArray byteArray = new BByteArray();
        for (int i = 0; i < bytes.length; i++) {
            byteArray.add(i, bytes[i]);
        }
        return byteArray;
    }

    public static void assertJBytesWithBBytes(byte[] jBytes, BByteArray bBytes) {
        for (int i = 0; i < jBytes.length; i++) {
            Assert.assertEquals(bBytes.get(i), jBytes[i], "Invalid byte value returned.");
        }
    }

    public static void assertJBytesWithBBytes(byte[] jBytes, byte[] bBytes) {
        for (int i = 0; i < jBytes.length; i++) {
            Assert.assertEquals(bBytes[i], jBytes[i], "Invalid byte value returned.");
        }
    }
}
