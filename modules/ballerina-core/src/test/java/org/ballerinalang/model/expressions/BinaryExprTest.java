/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.model.expressions;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Binary expression test.
 */
public class BinaryExprTest {

    private BLangProgram bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("lang/expressions/binary-expr.bal");
    }

    @Test(description = "Test binary logical expression")
    public void testBinaryExpr() {
        // stone + value
        boolean stone = true;
        boolean value = true;
        BValue[] args = { new BBoolean(stone), new BBoolean(value) };
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "makeChild", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        boolean actual = ((BBoolean) returns[0]).booleanValue();
        boolean expected = true;
        Assert.assertEquals(actual, expected);

        // stone + !value
        stone = true;
        value = false;
        args = new BValue[] { new BBoolean(stone), new BBoolean(value) };
        returns = BLangFunctions.invoke(bLangProgram, "makeChild", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        actual = ((BBoolean) returns[0]).booleanValue();
        expected = false;
        Assert.assertEquals(actual, expected);

        // !stone + value
        stone = false;
        value = true;
        args = new BValue[] { new BBoolean(stone), new BBoolean(value) };
        returns = BLangFunctions.invoke(bLangProgram, "makeChild", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        actual = ((BBoolean) returns[0]).booleanValue();
        expected = false;
        Assert.assertEquals(actual, expected);

        // !stone + !value
        stone = false;
        value = false;
        args = new BValue[] { new BBoolean(stone), new BBoolean(value) };
        returns = BLangFunctions.invoke(bLangProgram, "makeChild", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        actual = ((BBoolean) returns[0]).booleanValue();
        expected = false;
        Assert.assertEquals(actual, expected);
    }

    @Test(description = "Test multiple binary AND expressions")
    public void multiBinaryAndExpressionTest() {
        boolean one = true;
        boolean two = false;
        boolean three = true;

        boolean expectedResult = one & two & three;

        BValue[] args = { new BBoolean(one), new BBoolean(two), new BBoolean(three) };
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "multiBinaryANDExpression", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        boolean actualResult = ((BBoolean) returns[0]).booleanValue();

        Assert.assertEquals(actualResult, expectedResult);
    }

    @Test(description = "Test multiple binary OR expressions")
    public void multiBinaryORExpressionTest() {
        boolean one = true;
        boolean two = false;
        boolean three = true;

        boolean expectedResult = one || two || three;

        BValue[] args = { new BBoolean(one), new BBoolean(two), new BBoolean(three) };
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "multiBinaryORExpression", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        boolean actualResult = ((BBoolean) returns[0]).booleanValue();

        Assert.assertEquals(actualResult, expectedResult);
    }

    @Test(description = "Test multiple binary expressions")
    public void multiBinaryExpressionTest() {
        boolean one = true;
        boolean two = false;
        boolean three = true;

        boolean expectedResult = (!one || (two && three)) || (!three || (one && two));

        BValue[] args = { new BBoolean(one), new BBoolean(two), new BBoolean(three) };
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "multiBinaryExpression", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BBoolean.class);
        boolean actualResult = ((BBoolean) returns[0]).booleanValue();

        Assert.assertEquals(actualResult, expectedResult);
    }

}
