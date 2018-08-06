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

import java.util.Formatter;

/**
 * @since 0.95.1
 */
public class HashUtils {

    private HashUtils() {

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
     * @throws BallerinaHashException Thrown if an odd number or illegal of characters is supplied
     */
    public static byte[] decodeHex(String hexString) throws BallerinaHashException {
        char[] data = hexString.toCharArray();

        final int len = data.length;

        if ((len & 0x01) != 0) {
            throw new BallerinaHashException("Odd number of characters.");
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
     * @throws BallerinaHashException Thrown if ch is an illegal hex character
     */
    private static int toDigit(final char ch, final int index) throws BallerinaHashException {
        final int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new BallerinaHashException("Illegal hexadecimal character " + ch + " at index " + index);
        }
        return digit;
    }
}
