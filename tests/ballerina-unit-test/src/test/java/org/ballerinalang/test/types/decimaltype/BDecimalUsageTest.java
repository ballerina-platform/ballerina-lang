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
import org.ballerinalang.model.values.BDecimal;
import org.ballerinalang.model.values.BDecimalArray;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.semantics.model.types.util.Decimal;

/**
 * This test class will test the below usages of decimal type.
 * 1) Decimal array
 * 2) Decimal map
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
        Assert.assertSame(returns[0].getClass(), BDecimalArray.class);
        Assert.assertSame(returns[1].getClass(), BInteger.class);
        Assert.assertSame(returns[2].getClass(), BDecimal.class);
        Assert.assertSame(returns[3].getClass(), BDecimal.class);

        String arrayAsString = returns[0].stringValue();
        String expectedArrayString = "[12.3, 23.2, 34.534, 5.4]";
        Assert.assertEquals(arrayAsString, expectedArrayString, "Invalid array returned.");

        BInteger length = (BInteger) returns[1];
        Assert.assertEquals(length.intValue(), 4, "Invalid value returned.");

        BDecimal element0 = (BDecimal) returns[2];
        Assert.assertEquals(element0.decimalValue(), new Decimal("12.3"), "Invalid value returned.");

        BDecimal element1 = (BDecimal) returns[3];
        Assert.assertEquals(element1.decimalValue(), new Decimal("23.2"), "Invalid value returned.");
    }

    @Test(description = "Test decimal map")
    public void testDecimalMap() {
        BValue[] returns = BRunUtil.invoke(result, "testDecimalMap", new BValue[]{});
        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BMap.class);
        Assert.assertSame(returns[1].getClass(), BInteger.class);
        Assert.assertSame(returns[2].getClass(), BStringArray.class);
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
        Assert.assertEquals(element0.decimalValue(), new Decimal("12.45"), "Invalid value returned.");
    }
}
