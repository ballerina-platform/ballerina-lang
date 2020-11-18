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

import org.ballerinalang.core.model.types.BTypes;
import org.ballerinalang.core.model.values.BDecimal;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Test cases for the lang.float library.
 *
 * @since 1.0
 */
public class LangLibDecimalTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/decimallib_test.bal");
    }

    @Test(dataProvider = "dualDecimalProvider")
    public void testSum(BValue[] args, String expected) {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSum", args);
        assertEquals(((BDecimal) returns[0]).decimalValue(), new BigDecimal(expected));
    }

    @Test(dataProvider = "decimalProvider")
    public void testSingleArgMax(BValue arg, String expected) {
        BValue[] returns = BRunUtil.invoke(compileResult, "testOneArgMax", new BValue[] {arg});
        assertEquals(((BDecimal) returns[0]).decimalValue(), new BigDecimal(expected));
    }

    @Test(dataProvider = "decimalArrayProvider")
    public void testMax(BValue x, BValue xs, String expected) {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMultiArgMax", new BValue[] {x, xs});
        assertEquals(((BDecimal) returns[0]).decimalValue(), new BigDecimal(expected));
    }

    @Test(dataProvider = "decimalArrayProvider")
    public void testMaxAsMethodInvok(BValue x, BValue xs, String expected) {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMaxAsMethodInvok", new BValue[] {x, xs});
        assertEquals(((BDecimal) returns[0]).decimalValue(), new BigDecimal(expected));
    }

    @Test(dataProvider = "decimalMinProvider")
    public void testSingleArgMin(BValue arg, String expected) {
        BValue[] returns = BRunUtil.invoke(compileResult, "testOneArgMin", new BValue[] {arg});
        assertEquals(((BDecimal) returns[0]).decimalValue(), new BigDecimal(expected));
    }

    @Test(dataProvider = "decimalMinArrayProvider")
    public void testMin(BValue x, BValue xs, String expected) {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMultiArgMin", new BValue[] {x, xs});
        assertEquals(((BDecimal) returns[0]).decimalValue(), new BigDecimal(expected));
    }

    @Test(dataProvider = "decimalMinArrayProvider")
    public void testMinAsMethodInvok(BValue x, BValue xs, String expected) {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMinAsMethodInvok", new BValue[] {x, xs});
        assertEquals(((BDecimal) returns[0]).decimalValue(), new BigDecimal(expected));
    }

    @Test(dataProvider = "decimalAbsProvider")
    public void testAbs(BValue arg, String expected) {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAbs", new BValue[] {arg});
        assertEquals(((BDecimal) returns[0]).decimalValue(), new BigDecimal(expected));
    }

    @Test(dataProvider = "decimalAbsProvider")
    public void testAbsAsMethodInvok(BValue arg, String expected) {
        BValue[] returns = BRunUtil.invoke(compileResult, "testAbsAsMethodInvok", new BValue[] {arg});
        assertEquals(((BDecimal) returns[0]).decimalValue(), new BigDecimal(expected));
    }

    @DataProvider(name = "dualDecimalProvider")
    public static Object[][] dualDecimalProvider() {
        return new Object[][] {
                { new BValue[] {new BDecimal("0"),   new BDecimal("0")},   "0.0"},
                { new BValue[] {new BDecimal("0.0"), new BDecimal("0.0")}, "0.0"},
                { new BValue[] {new BDecimal("0"),   new BDecimal("1")},   "1"},
                { new BValue[] {new BDecimal("-1"),  new BDecimal("1")},   "0"},
                { new BValue[] {new BDecimal("-0"),  new BDecimal("1")},   "1"}
        };
    }

    @DataProvider(name = "decimalProvider")
    public static Object[][] singleDecimalProvider() {
        return new Object[][] {
                { new BDecimal("0"),   "0.0"},
                { new BDecimal("0.0"), "0.0"},
                { new BDecimal("0"),   "0.0"},
                { new BDecimal("-1"),  "-1"},
                { new BDecimal("-0"),  "0.0"},
                { new BDecimal("5"),   "5"}
        };
    }

    @DataProvider(name = "decimalMinProvider")
    public static Object[][] singleDecimalMinProvider() {
        return new Object[][] {
                { new BDecimal("0"),   "0.0"},
                { new BDecimal("0.0"), "0.0"},
                { new BDecimal("0"),   "0.0"},
                { new BDecimal("-1"),  "-1"},
                { new BDecimal("-0"),  "0.0"},
                { new BDecimal("5"),   "5"}
        };
    }

    @DataProvider(name = "decimalArrayProvider")
    public static Object[][] decimalArrayProvider() {
        return new Object[][] {
                { new BDecimal("0"),   getBArray("0"), "0"},
                { new BDecimal("0.0"), getBArray("0"), "0"},
                { new BDecimal("0"),   getBArray("1"), "1"},
                { new BDecimal("-1"),  getBArray("0"), "0"},
                { new BDecimal("-0"),  getBArray("0"), "0"},
                { new BDecimal("5"),   getBArray("0", "2", "-2"), "5"},
                { new BDecimal("-511111111111199999999999222222222.2222222"), getBArray("0"), "0"}
        };
    }

    @DataProvider(name = "decimalMinArrayProvider")
    public static Object[][] decimalArrayMinProvider() {
        return new Object[][] {
                { new BDecimal("0"),   getBArray("0"), "0"},
                { new BDecimal("0.0"), getBArray("0"), "0"},
                { new BDecimal("0"),   getBArray("1"), "0.0"},
                { new BDecimal("-1"),  getBArray("0"), "-1"},
                { new BDecimal("-0"),  getBArray("0"), "0"},
                { new BDecimal("5"),   getBArray("0", "2", "-2"), "-2"},
                { new BDecimal("-51111111111119999999999922222.2222222"), getBArray("0"),
                        "-51111111111119999999999922222.22222"}
        };
    }

    private static BValueArray getBArray(String ...xs) {
        BDecimal[] decimals = new BDecimal[xs.length];
        for (int i = 0; i < xs.length; i++) {
            decimals[i] = new BDecimal(xs[i]);
        }
        BValueArray bValueArray = new BValueArray(decimals, BTypes.typeDecimal);
        bValueArray.elementType = BTypes.typeDecimal;
        return bValueArray;
    }

    @DataProvider(name = "decimalAbsProvider")
    public static Object[][] decimalAbsProvider() {
        return new Object[][] {
                { new BDecimal("0"),      "0.0"},
                { new BDecimal("0.0"),    "0.0"},
                { new BDecimal("-0"),     "0.0"},
                { new BDecimal("-1"),     "1"},
                { new BDecimal("-0.0"),   "0.0"},
                { new BDecimal("-100.1"), "100.1"},
                { new BDecimal("100.1"),  "100.1"},
                { new BDecimal("5"),      "5"},
                { new BDecimal("-504023030303030303030.3030303"), "504023030303030303030.3030303"}
        };
    }

    @Test(dataProvider = "decimalRoundValueProvider")
    public void testRound(BValue arg, String expected) {
        BValue[] returns = BRunUtil.invoke(compileResult, "testRound", new BValue[] {arg});
        assertEquals(((BDecimal) returns[0]).decimalValue(), new BigDecimal(expected));
    }

    @Test(dataProvider = "decimalRoundValueProvider")
    public void testRoundAsMethodInvok(BValue arg, String expected) {
        BValue[] returns = BRunUtil.invoke(compileResult, "testRoundAsMethodInvok", new BValue[] {arg});
        assertEquals(((BDecimal) returns[0]).decimalValue(), new BigDecimal(expected));
    }

    @DataProvider(name = "decimalRoundValueProvider")
    public static Object[][] decimalRoundValueProvider() {
        return new Object[][] {
                { new BDecimal("0"),      "0"},
                { new BDecimal("0.0"),    "0"},
                { new BDecimal("-0"),     "0"},
                { new BDecimal("-1"),     "-1"},
                { new BDecimal("-0.0"),   "0"},
                { new BDecimal("-100.1"), "-100"},
                { new BDecimal("100.1"),  "100"},
                { new BDecimal("5"),      "5"},
                { new BDecimal("504023030303030303030.3030303"), "504023030303030303030"},
                { new BDecimal("-504023030303030303030.3030303"), "-504023030303030303030"}
        };
    }

    @Test(dataProvider = "decimalFloorValueProvider")
    public void testFloor(BValue arg, String expected) {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFloor", new BValue[] {arg});
        assertEquals(((BDecimal) returns[0]).decimalValue(), new BigDecimal(expected));
    }


    @Test(dataProvider = "decimalFloorValueProvider")
    public void testFloorAsMethodInvok(BValue arg, String expected) {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFloorAsMethodInvok", new BValue[] {arg});
        assertEquals(((BDecimal) returns[0]).decimalValue(), new BigDecimal(expected));
    }

    @DataProvider(name = "decimalFloorValueProvider")
    public static Object[][] decimalFloorValueProvider() {
        return new Object[][] {
                { new BDecimal("0"),      "0"},
                { new BDecimal("0.0"),    "0"},
                { new BDecimal("-0"),     "0"},
                { new BDecimal("-1"),     "-1"},
                { new BDecimal("-0.0"),   "0"},
                { new BDecimal("-100.1"), "-101"},
                { new BDecimal("100.1"),  "100"},
                { new BDecimal("5"),      "5"},
                { new BDecimal("504023030303030303030.3030303"), "504023030303030303030"},
                { new BDecimal("-504023030303030303030.3030303"), "-504023030303030303031"}
        };
    }

    @Test(dataProvider = "decimalCeilingValueProvider")
    public void testCeiling(BValue arg, String expected) {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCeiling", new BValue[] {arg});
        assertEquals(((BDecimal) returns[0]).decimalValue(), new BigDecimal(expected));
    }

    @Test(dataProvider = "decimalCeilingValueProvider")
    public void testCeilingAsMethodInvok(BValue arg, String expected) {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCeilingAsMethodInvok", new BValue[] {arg});
        assertEquals(((BDecimal) returns[0]).decimalValue(), new BigDecimal(expected));
    }

    @DataProvider(name = "decimalCeilingValueProvider")
    public static Object[][] decimalCeilingValueProvider() {
        return new Object[][] {
                { new BDecimal("0"),      "0"},
                { new BDecimal("0.0"),    "0"},
                { new BDecimal("-0"),     "0"},
                { new BDecimal("-1"),     "-1"},
                { new BDecimal("-0.0"),   "0"},
                { new BDecimal("-100.1"), "-100"},
                { new BDecimal("100.1"),  "101"},
                { new BDecimal("5"),      "5"},
                { new BDecimal("504023030303030303030.3030303"), "504023030303030303031"},
                { new BDecimal("-504023030303030303030.3030303"), "-504023030303030303030"}
        };
    }

    @Test(dataProvider = "decimalFromStringValueProvider")
    public void testFromString(String arg, String expected) {
        BValue[] returns = BRunUtil.invoke(compileResult, "testFromString",
                new BValue[] {new BString(arg)});
        assertEquals(((BDecimal) returns[0]).decimalValue(), new BigDecimal(expected));
    }

    @DataProvider(name = "decimalFromStringValueProvider")
    public static Object[][] decimalFromStringValueProvider() {
        return new Object[][] {
                { "0",      "0"},
                { "0.0",    "0.0"},
                { "-0",     "0"},
                { "-1",     "-1"},
                { "-0.0",   "0.0"},
                { "-100.1", "-100.1"},
                { "100.1",  "100.1"},
                { "5",      "5"},
                { "504023030303030303030.3030303", "504023030303030303030.3030303"},
                { "-504023030303030303030.3030303", "-504023030303030303030.3030303"}
        };
    }
}
