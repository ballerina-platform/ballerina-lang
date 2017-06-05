/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.model.expressions;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ArrayGrowTest {
    private BLangProgram bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("lang/expressions/array-grow.bal");
    }

    @Test(description = "Test array growth #1")
    public void testArrayGrow1() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "arrayGrow1");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 1801;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test array growth #2")
    public void testArrayGrow2() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "arrayGrow2");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 2001;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test array growth #3")
    public void testArrayGrow3() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "arrayGrow3");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 2101;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test array growth #4")
    public void testArrayGrow4() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "arrayGrow4");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 2001;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test array growth #5")
    public void testArrayGrow5() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "arrayGrow5");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 7101;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test array growth #6")
    public void testArrayGrow6() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "arrayGrow6");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);

        long actual = ((BInteger) returns[0]).intValue();
        long expected = 2001;
        Assert.assertEquals(actual, expected);
    }
}
