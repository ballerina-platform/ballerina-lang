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


import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BDecimal;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;

import static org.testng.Assert.assertEquals;

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

    @DataProvider(name = "decimalArrayProvider")
    public static Object[][] decimalArrayProvider() {
        return new Object[][] {
                { new BDecimal("0"),   getBArray("0"), "0"},
                { new BDecimal("0.0"), getBArray("0"), "0"},
                { new BDecimal("0"),   getBArray("1"), "1"},
                { new BDecimal("-1"),  getBArray("0"), "0"},
                { new BDecimal("-0"),  getBArray("0"), "0"},
                { new BDecimal("5"),   getBArray("0", "2", "-2"), "5"},
                { new BDecimal("-511111111111199999999999222222222222222.2222222"), getBArray("0"), "0"}
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
}
