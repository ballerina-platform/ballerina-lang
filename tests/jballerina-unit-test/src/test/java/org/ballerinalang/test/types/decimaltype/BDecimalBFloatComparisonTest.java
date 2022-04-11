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

package org.ballerinalang.test.types.decimaltype;

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BDecimal;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * This class is used to test the behaviours of BDecimal and double types by comparing them.
 * This class contains tests,
 * 1) demonstrating how very large numbers can be represented using BDecimal type.
 * 2) demonstrating how BDecimal type solves the floating point error problem that exists in the double type.
 *
 * @since 0.985.0
 */
public class BDecimalBFloatComparisonTest {
    private CompileResult result;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        result = BCompileUtil.compile("test-src/types/decimal/decimal_float_comparison.bal");
    }

    @Test(description = "Compare the representations of a very large number in double type and BDecimal type")
    public void testLargeFloatingPointNumber() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testLargeFloatingPointNumber", new Object[]{});
        Assert.assertEquals(returns.size(), 2);

        Assert.assertSame(returns.get(0).getClass(), Double.class);
        double bFloatVal = (double) returns.get(0);
        Assert.assertEquals(bFloatVal, 4.3534534534534643E92, "Invalid float value returned.");

        Assert.assertTrue(returns.get(1) instanceof BDecimal);
        BDecimal bDecimalVal = (BDecimal) returns.get(1);
        BigDecimal actualDecimalVal = bDecimalVal.value();
        BigDecimal expectedDecimalVal = new BigDecimal("4.354224522222222222222222222222889E+384",
                                                       MathContext.DECIMAL128);
        Assert.assertEquals(actualDecimalVal.compareTo(expectedDecimalVal), 0, "Invalid decimal value returned.");
    }

    @Test(description = "Check the precision correctness of double and BDecimal types using the boolean expression " +
            "(0.1 + 0.2 == 0.3)")
    public void testPrecisionCorrectness() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testPrecisionCorrectness", new Object[]{});
        Assert.assertEquals(returns.size(), 2);

        Assert.assertSame(returns.get(0).getClass(), Boolean.class);
        boolean result1 = (boolean) returns.get(0);
        // Expected: false
        Assert.assertFalse(result1, "Invalid result returned.");

        Assert.assertSame(returns.get(1).getClass(), Boolean.class);
        boolean result2 = (boolean) returns.get(1);
        // Expected: true
        Assert.assertTrue(result2, "Invalid result returned.");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
