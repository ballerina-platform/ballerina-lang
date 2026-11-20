/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.test;

import io.ballerina.runtime.internal.TypeChecker;
import io.ballerina.runtime.internal.TypeConverter;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;

/**
 * Tests that string-to-numeric conversions in {@link TypeConverter} accept only ASCII digits.
 */
public class NumericStringConversionTest {

    @Test(dataProvider = "nonAsciiNumericStrings", expectedExceptions = NumberFormatException.class)
    public void testStringToIntNonAscii(String value) {
        TypeConverter.stringToInt(value);
    }

    @Test(dataProvider = "nonAsciiNumericStrings", expectedExceptions = NumberFormatException.class)
    public void testStringToByteNonAscii(String value) {
        TypeConverter.stringToByte(value);
    }

    @Test(dataProvider = "nonAsciiNumericStrings", expectedExceptions = NumberFormatException.class)
    public void testStringToDecimalNonAscii(String value) {
        TypeConverter.stringToDecimal(value);
    }

    @Test(dataProvider = "nonAsciiNumericStrings", expectedExceptions = NumberFormatException.class)
    public void testAnyToDecimalNonAscii(String value) {
        TypeChecker.anyToDecimal(value);
    }

    @Test
    public void testAsciiNumericStrings() {
        Assert.assertEquals(TypeConverter.stringToInt("-123"), Long.valueOf(-123));
        Assert.assertEquals(TypeConverter.stringToByte("+12"), 12);
        Assert.assertEquals(TypeConverter.stringToDecimal("1.5").decimalValue(), new BigDecimal("1.5"));
        Assert.assertEquals(TypeChecker.anyToDecimal("1.5").decimalValue(), new BigDecimal("1.5"));
    }

    @DataProvider
    public Object[][] nonAsciiNumericStrings() {
        return new Object[][]{
                {"١٢٣"}, // ARABIC-INDIC DIGIT ONE..THREE
                {"१२"},       // DEVANAGARI DIGIT ONE, TWO
                {"-１"},            // FULLWIDTH DIGIT ONE
                {"1٢"},            // mixed ASCII and non-ASCII digits
        };
    }
}
