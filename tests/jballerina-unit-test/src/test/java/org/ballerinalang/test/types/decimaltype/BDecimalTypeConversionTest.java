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

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BString;
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
 * This class is used to test the decimal to other types and other types to decimal conversion operations.
 * <p>
 * Valid conversions:
 * decimal --> int
 * decimal --> float
 * decimal --> string
 * decimal --> boolean
 * decimal --> any
 * decimal --> json
 * <p>
 * int --> decimal
 * float --> decimal
 * string --> decimal (Checked)
 * boolean --> decimal
 * any --> decimal (checked)
 * json --> decimal (checked)
 *
 * @since 0.985.0
 **/
public class BDecimalTypeConversionTest {

    private static final BigDecimal DELTA = new BigDecimal("1e-10", MathContext.DECIMAL128);
    private CompileResult result;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        result = BCompileUtil.compile("test-src/types/decimal/decimal_conversion.bal");
    }

    @Test(description = "Test decimal to other types conversion")
    public void testDecimalToOtherTypesConversion() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testDecimalToOtherTypesConversion", new Object[]{});
        Assert.assertEquals(returns.size(), 5);
        Assert.assertSame(returns.get(0).getClass(), Long.class);
        Assert.assertSame(returns.get(1).getClass(), Double.class);
        Assert.assertTrue(returns.get(2) instanceof BString);
        Assert.assertTrue(returns.get(3) instanceof BDecimal);
        Assert.assertTrue(returns.get(4) instanceof BDecimal);

        long intVal = (long) returns.get(0);
        double floatVal = (double) returns.get(1);
        BString stringVal = (BString) returns.get(2);
        BDecimal anyVal = (BDecimal) returns.get(3);
        BDecimal jsonVal = (BDecimal) returns.get(4);

        Assert.assertEquals(intVal, 23, "Invalid integer value returned.");
        Assert.assertEquals(floatVal, 23.456, "Invalid float value returned.");
        Assert.assertEquals(stringVal.toString(), "23.456", "Invalid string value returned.");
        Assert.assertEquals(anyVal.value().compareTo(new BigDecimal("23.456", MathContext.DECIMAL128)), 0,
                "Invalid value returned.");
        Assert.assertEquals(jsonVal.value().compareTo(new BigDecimal("23.456", MathContext.DECIMAL128)), 0,
                "Invalid value returned.");
    }

    @Test(description = "Test other types to decimal conversion")
    public void testOtherTypesToDecimalConversion() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testOtherTypesToDecimalConversion", new Object[]{});
        Assert.assertEquals(returns.size(), 3);
        for (int i = 0; i < 3; i++) {
            Assert.assertTrue(returns.get(i) instanceof BDecimal);
        }

        BDecimal val1 = (BDecimal) returns.get(0);
        BDecimal val2 = (BDecimal) returns.get(1);
        BDecimal val3 = (BDecimal) returns.get(2);

        Assert.assertEquals(val1.value().compareTo(new BigDecimal("12", MathContext.DECIMAL128)), 0,
                "Invalid decimal value returned.");
        Assert.assertTrue(val2.subtract(ValueCreator.createDecimalValue(new BigDecimal("-12.34",
                MathContext.DECIMAL128))).value().compareTo(DELTA) < 0, "Invalid decimal value returned.");
        Assert.assertEquals(val3.value().compareTo(new BigDecimal("23.456", MathContext.DECIMAL128)), 0,
                "Invalid decimal value returned.");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
