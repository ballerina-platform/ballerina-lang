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

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BDecimal;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
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
        BValue[] returns = BRunUtil.invoke(result, "testDecimalToOtherTypesConversion", new BValue[]{});
        Assert.assertEquals(returns.length, 6);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BFloat.class);
        Assert.assertSame(returns[2].getClass(), BString.class);
        Assert.assertSame(returns[3].getClass(), BBoolean.class);
        Assert.assertSame(returns[4].getClass(), BDecimal.class);
        Assert.assertSame(returns[5].getClass(), BDecimal.class);

        BInteger intVal = (BInteger) returns[0];
        BFloat floatVal = (BFloat) returns[1];
        BString stringVal = (BString) returns[2];
        BBoolean boolVal = (BBoolean) returns[3];
        BDecimal anyVal = (BDecimal) returns[4];
        BDecimal jsonVal = (BDecimal) returns[5];

        Assert.assertEquals(intVal.intValue(), 23, "Invalid integer value returned.");
        Assert.assertEquals(floatVal.floatValue(), 23.456, "Invalid float value returned.");
        Assert.assertEquals(stringVal.stringValue(), "23.456", "Invalid string value returned.");
        Assert.assertTrue(boolVal.booleanValue(), "Invalid boolean value returned.");
        Assert.assertTrue(anyVal.decimalValue().compareTo(new BigDecimal("23.456", MathContext.DECIMAL128)) == 0,
                "Invalid value returned.");
        Assert.assertTrue(jsonVal.decimalValue().compareTo(new BigDecimal("23.456", MathContext.DECIMAL128)) == 0,
                "Invalid value returned.");
    }

    @Test(description = "Test other types to decimal conversion")
    public void testOtherTypesToDecimalConversion() {
        BValue[] returns = BRunUtil.invoke(result, "testOtherTypesToDecimalConversion", new BValue[]{});
        Assert.assertEquals(returns.length, 6);
        for (int i = 0; i < 6; i++) {
            Assert.assertSame(returns[i].getClass(), BDecimal.class);
        }

        BDecimal val1 = (BDecimal) returns[0];
        BDecimal val2 = (BDecimal) returns[1];
        BDecimal val3 = (BDecimal) returns[2];
        BDecimal val4 = (BDecimal) returns[3];
        BDecimal val5 = (BDecimal) returns[4];
        BDecimal val6 = (BDecimal) returns[5];

        Assert.assertTrue(val1.decimalValue().compareTo(new BigDecimal("12", MathContext.DECIMAL128)) == 0,
                "Invalid decimal value returned.");
        Assert.assertTrue(val2.decimalValue().subtract(new BigDecimal("-12.34", MathContext.DECIMAL128)).
                        compareTo(DELTA) < 0, "Invalid decimal value returned.");
        Assert.assertTrue(val3.decimalValue().compareTo(new BigDecimal("23.456", MathContext.DECIMAL128)) == 0,
                "Invalid decimal value returned.");
        Assert.assertTrue(val4.decimalValue().compareTo(new BigDecimal("1.0", MathContext.DECIMAL128)) == 0,
                "Invalid decimal value returned.");
        Assert.assertTrue(val5.decimalValue().compareTo(new BigDecimal("12.3", MathContext.DECIMAL128)) == 0,
                "Invalid decimal value returned.");
        Assert.assertTrue(val6.decimalValue().compareTo(new BigDecimal("23.4", MathContext.DECIMAL128)) == 0,
                "Invalid decimal value returned.");
    }
}
