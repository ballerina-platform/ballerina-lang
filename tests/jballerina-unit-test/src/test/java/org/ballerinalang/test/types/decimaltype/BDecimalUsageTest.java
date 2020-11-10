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

import org.ballerinalang.core.model.values.BDecimal;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * This test class will test the below usages of decimal type.
 * 1) Decimal array
 * 2) Decimal map
 * 3) Record with decimal type fields
 * 4) Object with decimal type fields
 *
 * @since 0.985.0
 */
public class BDecimalUsageTest {
    private CompileResult result;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        result = BCompileUtil.compile("test-src/types/decimal/decimal_usage.bal");
    }

    @Test(description = "Test decimal array")
    public void testDecimalArray() {
        BValue[] returns = BRunUtil.invoke(result, "testDecimalArray", new BValue[]{});
        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BValueArray.class);
        Assert.assertSame(returns[1].getClass(), BInteger.class);
        Assert.assertSame(returns[2].getClass(), BDecimal.class);
        Assert.assertSame(returns[3].getClass(), BDecimal.class);

        String arrayAsString = returns[0].stringValue();
        String expectedArrayString = "[12.3, 23.2, 34.534, 5.4]";
        Assert.assertEquals(arrayAsString, expectedArrayString, "Invalid array returned.");

        BInteger length = (BInteger) returns[1];
        Assert.assertEquals(length.intValue(), 4, "Invalid value returned.");

        BDecimal element0 = (BDecimal) returns[2];
        Assert.assertEquals(element0.decimalValue(), new BigDecimal("12.3", MathContext.DECIMAL128),
                "Invalid value returned.");

        BDecimal element1 = (BDecimal) returns[3];
        Assert.assertEquals(element1.decimalValue(), new BigDecimal("23.2", MathContext.DECIMAL128),
                "Invalid value returned.");
    }

    @Test(description = "Test decimal map")
    public void testDecimalMap() {
        BValue[] returns = BRunUtil.invoke(result, "testDecimalMap", new BValue[]{});
        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BMap.class);
        Assert.assertSame(returns[1].getClass(), BInteger.class);
        Assert.assertSame(returns[2].getClass(), BValueArray.class);
        Assert.assertSame(returns[3].getClass(), BDecimal.class);

        String mapAsString = returns[0].stringValue();
        String expectedMapString = "{\"element0\":12.45, \"element1\":34.3, \"element2\":2314.31}";
        Assert.assertEquals(mapAsString, expectedMapString, "Invalid map returned.");

        BInteger length = (BInteger) returns[1];
        Assert.assertEquals(length.intValue(), 3, "Invalid value returned.");

        String keys = returns[2].stringValue();
        String expectedKeys = "[\"element0\", \"element1\", \"element2\"]";
        Assert.assertEquals(keys, expectedKeys, "Invalid map keys returned.");

        BDecimal element0 = (BDecimal) returns[3];
        Assert.assertEquals(element0.decimalValue(), new BigDecimal("12.45", MathContext.DECIMAL128),
                "Invalid value returned.");
    }

    @Test(description = "Test record with decimal fields")
    public void testDecimalRecord() {
        BValue[] returns = BRunUtil.invoke(result, "testDecimalRecord", new BValue[]{});
        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BDecimal.class);
        Assert.assertSame(returns[1].getClass(), BDecimal.class);

        BDecimal weight = (BDecimal) returns[0];
        BDecimal height = (BDecimal) returns[1];

        Assert.assertEquals(weight.decimalValue(), new BigDecimal("23.45", MathContext.DECIMAL128),
                "Invalid decimal value returned.");
        Assert.assertEquals(height.decimalValue(), new BigDecimal("120.43", MathContext.DECIMAL128),
                "Invalid decimal value returned.");
    }

    @Test(description = "Test object with decimal fields")
    public void testDecimalObject() {
        BValue[] returns = BRunUtil.invoke(result, "testDecimalObject", new BValue[]{});
        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BString.class);
        Assert.assertSame(returns[1].getClass(), BInteger.class);
        Assert.assertSame(returns[2].getClass(), BDecimal.class);
        Assert.assertSame(returns[3].getClass(), BDecimal.class);

        BString name = (BString) returns[0];
        BInteger age = (BInteger) returns[1];
        BDecimal weight = (BDecimal) returns[2];
        BDecimal height = (BDecimal) returns[3];

        Assert.assertEquals(name.stringValue(), "Bob", "Invalid string value returned.");
        Assert.assertEquals(age.intValue(), 25, "Invalid integer value returned.");
        Assert.assertEquals(weight.decimalValue(), new BigDecimal("57.25", MathContext.DECIMAL128),
                "Invalid decimal value returned.");
        Assert.assertEquals(height.decimalValue(), new BigDecimal("168.67", MathContext.DECIMAL128),
                "Invalid decimal value returned.");
    }
}
