/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.ballerinalang.langlib.test;

import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for the lang.float library.
 *
 * @since 1.0
 */
public class LangLibFloatTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/floatlib_test.bal");
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }

    @Test
    public void testIsFinite() {
        Object returns = BRunUtil.invoke(compileResult, "testIsFinite");
        BArray result = (BArray) returns;
        assertTrue(result.getBoolean(0));
        assertFalse(result.getBoolean(1));
    }

    @Test
    public void testIsInfinite() {
        Object returns = BRunUtil.invoke(compileResult, "testIsInfinite");
        BArray result = (BArray) returns;
        assertFalse(result.getBoolean(0));
        assertTrue(result.getBoolean(1));
    }

    @Test
    public void testSum() {

        Object returns = BRunUtil.invoke(compileResult, "testSum");
        assertEquals(returns, 70.35d);
    }

    @Test
    public void testFloatConsts() {

        Object returns = BRunUtil.invoke(compileResult, "testFloatConsts");
        BArray result = (BArray) returns;
        assertEquals(result.getFloat(0), Double.NaN);
        assertEquals(result.getFloat(1), Double.POSITIVE_INFINITY);
    }

    @Test
    public void testLangLibCallOnFiniteType() {
        BRunUtil.invoke(compileResult, "testLangLibCallOnFiniteType");
    }

    @Test(dataProvider = "functionsWithFloatEqualityChecks")
    public void testFunctionsWithFloatEqualityChecks(String function) {
        BRunUtil.invoke(compileResult, function);
    }

    @DataProvider
    public  Object[] functionsWithFloatEqualityChecks() {
        return new String[] {
                "testFloatEquality",
                "testFloatNotEquality",
                "testFloatExactEquality",
                "testFloatNotExactEquality"
        };
    }

    @Test
    public void testFromHexString() {
        BRunUtil.invoke(compileResult, "testFromHexString");
    }

    @Test
    public void testMinAndMaxWithNaN() {
        BRunUtil.invoke(compileResult, "testMinAndMaxWithNaN");
    }

    @Test(dataProvider = "functionsWithFromStringTests")
    public void testFromString(String function) {
        BRunUtil.invoke(compileResult, function);
    }

    @DataProvider
    public  Object[] functionsWithFromStringTests() {
        return new String[] {
                "testFromStringPositive",
                "testFromStringNegative"
        };
    }

    @Test(dataProvider = "functionsToTestToFixedString")
    public void testToFixedString(String function) {
        BRunUtil.invoke(compileResult, function);
    }

    @DataProvider
    public  Object[] functionsToTestToFixedString() {
        return new String[] {
                "testToFixedStringWithPositiveFloat",
                "testToFixedStringWithNegativeFloat",
                "testToFixedStringWithInfinity",
                "testToFixedStringWithNaN",
                "testToFixedStringWhenFractionDigitsIsLessThanZero",
                "testToFixedStringWhenFractionDigitsIsZero",
                "testToFixedStringWhenFractionDigitsIsNil",
                "testToFixedStringWhenFractionDigitsIsVeryLargeInt",
                "testToFixedStringWhenFractionDigitsIsIntMax",
                "testToFixedStringWithMorePositiveFloats",
                "testToFixedStringWithMoreNegativeFloats",
                "testToFixedStringWithVerySmallAndLargePositiveFloats",
                "testToFixedStringWithVerySmallAndLargeNegativeFloats",
                "testToFixedStringWithHexaDecimalFloatingPoints"
        };
    }

    @Test(dataProvider = "functionsToTestToExpString")
    public void testToExpString(String function) {
        BRunUtil.invoke(compileResult, function);
    }

    @DataProvider
    public  Object[] functionsToTestToExpString() {
        return new String[] {
                "testToExpStringWithPositiveFloat",
                "testToExpStringWithNegativeFloat",
                "testToExpStringWithInfinity",
                "testToExpStringWithNaN",
                "testToExpStringWhenFractionDigitsIsLessThanZero",
                "testToExpStringWhenFractionDigitsIsZero",
                "testToExpStringWhenFractionDigitsIsNil",
                "testToExpStringWhenFractionDigitsIsVeryLargeInt",
                "testToExpStringWhenFractionDigitsIsIntMax",
                "testToExpStringWithMorePositiveFloats",
                "testToExpStringWithVerySmallAndLargePositiveFloats",
                "testToExpStringWithMoreNegativeFloats",
                "testToExpStringWithVerySmallAndLargeNegativeFloats",
                "testToExpStringWithHexaDecimalFloatingPoints"
        };
    }

    @Test
    public void testRound() {
        BRunUtil.invoke(compileResult, "testRound");
    }
}
