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
import io.ballerina.runtime.api.values.BMap;
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
        BArray returns = (BArray) BRunUtil.invoke(result, "testDecimalArray", new Object[]{});
        Assert.assertEquals(returns.size(), 4);
        Assert.assertTrue(returns.get(0) instanceof BArray);
        Assert.assertSame(returns.get(1).getClass(), Long.class);
        Assert.assertTrue(returns.get(2) instanceof BDecimal);
        Assert.assertTrue(returns.get(3) instanceof BDecimal);

        String arrayAsString = returns.get(0).toString();
        String expectedArrayString = "[12.3,23.2,34.534,5.4]";
        Assert.assertEquals(arrayAsString, expectedArrayString, "Invalid array returned.");

        long length = (long) returns.get(1);
        Assert.assertEquals(length, 4, "Invalid value returned.");

        BDecimal element0 = (BDecimal) returns.get(2);
        Assert.assertEquals(element0, ValueCreator.createDecimalValue(new BigDecimal("12.3", MathContext.DECIMAL128)),
                "Invalid value returned.");

        BDecimal element1 = (BDecimal) returns.get(3);
        Assert.assertEquals(element1, ValueCreator.createDecimalValue(new BigDecimal("23.2", MathContext.DECIMAL128)),
                "Invalid value returned.");
    }

    @Test(description = "Test decimal map")
    public void testDecimalMap() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testDecimalMap", new Object[]{});
        Assert.assertEquals(returns.size(), 4);
        Assert.assertTrue(returns.get(0) instanceof BMap);
        Assert.assertSame(returns.get(1).getClass(), Long.class);
        Assert.assertTrue(returns.get(2) instanceof BArray);
        Assert.assertTrue(returns.get(3) instanceof BDecimal);

        String mapAsString = returns.get(0).toString();
        String expectedMapString = "{\"element0\":12.45,\"element1\":34.3,\"element2\":2314.31}";
        Assert.assertEquals(mapAsString, expectedMapString, "Invalid map returned.");

        long length = (long) returns.get(1);
        Assert.assertEquals(length, 3, "Invalid value returned.");

        String keys = returns.get(2).toString();
        String expectedKeys = "[\"element0\",\"element1\",\"element2\"]";
        Assert.assertEquals(keys, expectedKeys, "Invalid map keys returned.");

        BDecimal element0 = (BDecimal) returns.get(3);
        Assert.assertEquals(element0.value(), new BigDecimal("12.45", MathContext.DECIMAL128),
                "Invalid value returned.");
    }

    @Test(description = "Test record with decimal fields")
    public void testDecimalRecord() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testDecimalRecord", new Object[]{});
        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof BDecimal);
        Assert.assertTrue(returns.get(1) instanceof BDecimal);

        BDecimal weight = (BDecimal) returns.get(0);
        BDecimal height = (BDecimal) returns.get(1);

        Assert.assertEquals(weight.value(), new BigDecimal("23.45", MathContext.DECIMAL128),
                "Invalid decimal value returned.");
        Assert.assertEquals(height.value(), new BigDecimal("120.43", MathContext.DECIMAL128),
                "Invalid decimal value returned.");
    }

    @Test(description = "Test object with decimal fields")
    public void testDecimalObject() {
        BArray returns = (BArray) BRunUtil.invoke(result, "testDecimalObject", new Object[]{});
        Assert.assertEquals(returns.size(), 4);
        Assert.assertTrue(returns.get(0) instanceof BString);
        Assert.assertSame(returns.get(1).getClass(), Long.class);
        Assert.assertTrue(returns.get(2) instanceof BDecimal);
        Assert.assertTrue(returns.get(3) instanceof BDecimal);

        BString name = (BString) returns.get(0);
        long age = (long) returns.get(1);
        BDecimal weight = (BDecimal) returns.get(2);
        BDecimal height = (BDecimal) returns.get(3);

        Assert.assertEquals(name.toString(), "Bob", "Invalid string value returned.");
        Assert.assertEquals(age, 25, "Invalid integer value returned.");
        Assert.assertEquals(weight.value(), new BigDecimal("57.25", MathContext.DECIMAL128),
                "Invalid decimal value returned.");
        Assert.assertEquals(height.value(), new BigDecimal("168.67", MathContext.DECIMAL128),
                "Invalid decimal value returned.");
    }

    @Test(description = "Test decimal defaultable parameters")
    public void testDecimalDefaultable() {
        BRunUtil.invoke(result, "testDecimalDefaultable");
    }

    @Test(description = "Test decimal values with too many exponents")
    public void testDecimalNegativeLargeExponents() {
        BRunUtil.invoke(result, "testDecimalNegativeLargeExponents");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
