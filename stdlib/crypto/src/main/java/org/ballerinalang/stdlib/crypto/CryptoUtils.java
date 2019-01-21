/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.stdlib.crypto.util;

import org.ballerinalang.bre.Context;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Formatter;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * @since 0.95.1
 */
public class HashUtils {

    private HashUtils() {

    }

    public static byte[] hmac(Context context, byte[] input, byte[] key, String hmacAlgorithm) {
        try {
            SecretKey secretKey = new SecretKeySpec(key, hmacAlgorithm);
            Mac mac = Mac.getInstance(hmacAlgorithm);
            mac.init(secretKey);
            return mac.doFinal(input);
        } catch (IllegalArgumentException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new BallerinaException("Error occurred while calculating HMAC: " + e.getMessage(), context);
        }
    }

    public static byte[] hash(Context context, String algorithm, byte[] input) {
        try {
            MessageDigest messageDigest;
            messageDigest = MessageDigest.getInstance(algorithm);
            messageDigest.update(input);
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new BallerinaException("Error occurred while calculating hash: " + e.getMessage(), context);
        }
    }

    public static byte[] sign(Context context, String algorithm, PrivateKey privateKey, byte[] input) {
        try {
            Signature sig = Signature.getInstance(algorithm);
            sig.initSign(privateKey);
            sig.update(input);
            return sig.sign();
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            throw new BallerinaException("Error occurred while calculating signature: " + e.getMessage(), context);
        }
    }

    /**
     * Converts the given byte array to a Hex formatted string.
     *
     * @param bytes array of bytes
     * @return Hex formatted string.
     */
    public static String toHexString(byte[] bytes) {
        Formatter hexStringFormatter = new Formatter();
        for (byte b : bytes) {
            hexStringFormatter.format("%02X", b);
        }
        return hexStringFormatter.toString();
    }

    /**
     * Converts a String representing hexadecimal values into an array of bytes of those same values.
     *
     * @param hexString A String containing hexadecimal digits
     * @return A byte array containing binary data decoded from the supplied string.
     * @throws IllegalArgumentException Thrown if an odd number or illegal number of characters is supplied
     */
    public static byte[] decodeHex(String hexString) throws IllegalArgumentException {
        char[] data = hexString.toCharArray();

        final int len = data.length;

        if ((len & 0x01) != 0) {
            throw new IllegalArgumentException("Odd number of characters.");
        }

        final byte[] out = new byte[len >> 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; j < len; i++) {
            int f = toDigit(data[j], j) << 4;
            j++;
            f = f | toDigit(data[j], j);
            j++;
            out[i] = (byte) (f & 0xFF);
        }

        return out;
    }


    /**
     * Converts a hexadecimal character to an integer.
     *
     * @param ch A character to convert to an integer digit
     * @param index The index of the character in the source
     * @return An integer
     * @throws IllegalArgumentException Thrown if ch is an illegal hex character
     */
    private static int toDigit(final char ch, final int index) throws IllegalArgumentException {
        final int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new IllegalArgumentException("Illegal hexadecimal character " + ch + " at index " + index);
        }
        return digit;
    }
}
