/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.encoding;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;

import java.util.Formatter;

/**
 * Utility functions relevant to encoding operations.
 *
 * @since 0.991.0
 */
public class EncodingUtil {

    private EncodingUtil() {

    }

    /**
     * Converts the given byte array to a Hex formatted string.
     *
     * @param bytes array of bytes
     * @return Hex formatted string.
     */
    public static String encodeHex(byte[] bytes) {
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

    /**
     * Create encoding error.
     *
     * @param context Represent ballerina context
     * @param errMsg  Error description
     * @return conversion error
     */
    public static BError createEncodingError(Context context, String errMsg) {
        BMap<String, BValue> errorRecord = BLangConnectorSPIUtil.createBStruct(context, Constants.ENCODING_PACKAGE,
                Constants.ENCODING_ERROR);
        errorRecord.put(Constants.MESSAGE, new BString(errMsg));
        return BLangVMErrors.createError(context, true, BTypes.typeError, Constants.ENCODING_ERROR_CODE, errorRecord);
    }
}
