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

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BDecimal;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * This class is used to test the behaviours of BDecimal and BFloat types by comparing them.
 * This class contains tests,
 * 1) demonstrating how very large numbers can be represented using BDecimal type.
 * 2) demonstrating how BDecimal type solves the floating point error problem that exists in the BFloat type.
 *
 * @since 0.985.0
 */
public class BDecimalBFloatComparisonTest {
    private CompileResult result;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        result = BCompileUtil.compile("test-src/types/decimal/decimal_float_comparison.bal");
    }

    @Test(description = "Compare the representations of a very large number in BFloat type and BDecimal type")
    public void testLargeFloatingPointNumber() {
        BValue[] returns = BRunUtil.invoke(result, "testLargeFloatingPointNumber", new BValue[]{});
        Assert.assertEquals(returns.length, 2);

        Assert.assertSame(returns[0].getClass(), BFloat.class);
        BFloat bFloatVal = (BFloat) returns[0];
        Assert.assertEquals(bFloatVal.floatValue(), Double.POSITIVE_INFINITY, "Invalid float value returned.");

        Assert.assertSame(returns[1].getClass(), BDecimal.class);
        BDecimal bDecimalVal = (BDecimal) returns[1];
        BigDecimal actualDecimalVal = bDecimalVal.decimalValue();
        BigDecimal expectedDecimalVal = new BigDecimal("4.354224522222222222222222222222889E+384",
                                                       MathContext.DECIMAL128);
        Assert.assertEquals(actualDecimalVal.compareTo(expectedDecimalVal), 0, "Invalid decimal value returned.");
    }

    @Test(description = "Check the precision correctness of BFloat and BDecimal types using the boolean expression " +
            "(0.1 + 0.2 == 0.3)")
    public void testPrecisionCorrectness() {
        BValue[] returns = BRunUtil.invoke(result, "testPrecisionCorrectness", new BValue[]{});
        Assert.assertEquals(returns.length, 2);

        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        BBoolean result1 = (BBoolean) returns[0];
        // Expected: false
        Assert.assertFalse(result1.booleanValue(), "Invalid result returned.");

        Assert.assertSame(returns[1].getClass(), BBoolean.class);
        BBoolean result2 = (BBoolean) returns[1];
        // Expected: true
        Assert.assertTrue(result2.booleanValue(), "Invalid result returned.");
    }
}
