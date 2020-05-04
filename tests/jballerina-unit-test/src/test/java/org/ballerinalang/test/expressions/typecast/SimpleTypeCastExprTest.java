/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.expressions.typecast;

import org.ballerinalang.core.model.values.BDecimal;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;

/**
 * Test Cases for simple type casting.
 */
public class SimpleTypeCastExprTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/typecast/simple-type-cast.bal");
    }

    @Test
    public void intToFloatExplicit() {
        testIntToFloatCast(-1, -1.0);
        testIntToFloatCast(0, 0.0);
        testIntToFloatCast(1, 1.0);
        testIntToFloatCast(150000000, 150000000.0);
    }

    private void testIntToFloatCast(int input, double expected) {
        BValue[] args = { new BInteger(input) };
        BValue[] returns = BRunUtil.invoke(result, "intToFloatExplicit", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].getClass(), BFloat.class);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), expected);
    }

    @Test
    public void testFloatToIntCast() {
        testFloatToIntCast(4.5, 4);
        testFloatToIntCast(5.5, 6);
        testFloatToIntCast(3.2, 3);
        testFloatToIntCast(6.7, 7);
    }

    private void testFloatToIntCast(double input, long expected) {
        BValue[] args = {new BFloat(input)};
        BValue[] returns = BRunUtil.invoke(result, "floatToIntCast", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), expected);
    }

    @Test
    public void testDecimalToIntCast() {
        testDecimalToIntCast(new BigDecimal(4.5), 4);
        testDecimalToIntCast(new BigDecimal(5.5), 6);
        testDecimalToIntCast(new BigDecimal(3.2), 3);
        testDecimalToIntCast(new BigDecimal(6.7), 7);
    }

    private void testDecimalToIntCast(BigDecimal input, long expected) {
        BValue[] args = {new BDecimal(input)};
        BValue[] returns = BRunUtil.invoke(result, "decimalToIntCast", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), expected);
    }
}
