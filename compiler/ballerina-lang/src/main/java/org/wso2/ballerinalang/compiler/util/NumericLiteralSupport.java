/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerinalang.compiler.util;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Common functions used for handling discriminated float/decimal literals.
 *
 * @since 0.995
 */
public final class NumericLiteralSupport {

    private NumericLiteralSupport() {
    }

    public static final String FLOAT_DISCRIMINATOR = "f";
    public static final String DECIMAL_DISCRIMINATOR = "d";

    /**
     * Strip float/decimal discriminator suffixes from the input, if no discriminator suffixes are found original
     * string is returned.
     *
     * @param literalValue numeric literal
     * @return numeric literal without the discriminator suffixes
     */
    public static String stripDiscriminator(String literalValue) {
        int length = literalValue.length();
        if (length < 2) {
            return literalValue;
        }
        char lastChar = literalValue.charAt(length - 1);
        if (lastChar == 'f' || lastChar == 'F' || lastChar == 'd' || lastChar == 'D') {
            return literalValue.substring(0, length - 1);
        }
        return literalValue;
    }

    /**
     * This method depends on the fact that we add HexExponentIndicator (p0|P0) to hex literals if it's not available.
     *
     * @param numericLiteral numeric literals to be tested
     * @return is this a hex literal
     */
    public static boolean isHexLiteral(String numericLiteral) {
        for (int i = 0; i < numericLiteral.length(); i++) {
            char c = numericLiteral.charAt(i);
            if (c == 'p' || c == 'P') {
                return true;
            }
        }
        return false;
    }

    /**
     * Check input for decimal indicator.
     *
     * @param literalValue literal to check
     * @return true if Hex prefix is present, false otherwise
     */
    public static boolean hasHexIndicator(String literalValue) {
        int length = literalValue.length();
        // There should be at least 3 characters to form hex literal.
        if (length < 3) {
            return false;
        }
        // Check whether hex prefix is with positive and negative inputs.
        char firstChar = literalValue.charAt(1);
        char secondChar = literalValue.charAt(2);
        return firstChar == 'x' || secondChar == 'x' || firstChar == 'X' || secondChar == 'X';
    }


    /**
     * Parse BigDecimal using DECIMAL128 math context.
     *
     * @param baseValue value to be parsed
     * @return BigDecimal number.
     */
    public static BigDecimal parseBigDecimal(Object baseValue) {
        String strValue = String.valueOf(baseValue);
        if (isDecimalDiscriminated(strValue)) {
            strValue = strValue.substring(0, strValue.length() - 1);
        }
        return new BigDecimal(strValue, MathContext.DECIMAL128);
    }

    /**
     * Check input for decimal discriminator suffix.
     *
     * @param literalValue literal to check
     * @return true if decimal suffix is present, false otherwise
     */
    public static boolean isDecimalDiscriminated(String literalValue) {
        int length = literalValue.length();
        // There should be at least 2 characters to form discriminated decimal literal.
        if (length < 2) {
            return false;
        }
        char lastChar = literalValue.charAt(length - 1);
        return (lastChar == 'd' || lastChar == 'D');
    }

    /**
     * Check input for float discriminator suffix.
     *
     * @param literalValue literal to check
     * @return true if float suffix is present, false otherwise
     */
    public static boolean isFloatDiscriminated(String literalValue) {
        int length = literalValue.length();
        // There should be at least 2 characters to form discriminated decimal literal.
        if (length < 2) {
            return false;
        }
        char lastChar = literalValue.charAt(length - 1);
        return (lastChar == 'f' || lastChar == 'F');
    }
}
