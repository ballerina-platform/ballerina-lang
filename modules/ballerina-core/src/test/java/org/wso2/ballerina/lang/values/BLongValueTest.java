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
package org.wso2.ballerina.lang.values;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.values.BLong;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.utils.Functions;
import org.wso2.ballerina.core.utils.ParserUtils;

/**
 * This test class will test the behaviour of long values with expressions.
 * Addition
 * Multiplication
 * Division
 * Subtraction
 *
 * Defining a long value
 * long b;
 * b = 10.1L;
 */
public class BLongValueTest {
    private BallerinaFile bFile;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        bFile = ParserUtils.parseBalFile("lang/values/long-value.bal");
    }

    @Test(description = "Test long value assignment")
    public void testLongValue() {
        BValue[] returns = Functions.invoke(bFile, "testLongValue");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BLong.class);
        BLong longValue = (BLong) returns[0];
        Assert.assertEquals(longValue.longValue(), 10L, "Invalid long value returned.");
    }

    @Test(description = "Test negative long value assignment")
    public void testNegativeLongValue() {
        BValue[] returns = Functions.invoke(bFile, "testNegativeLongValue");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BLong.class);
        BLong longValue = (BLong) returns[0];
        Assert.assertEquals(longValue.longValue(), (-10L), "Invalid long value returned.");
    }

    @Test(description = "Test long value assignment from a value returned by function")
    public void testLongValueAssignmentByReturnValue() {
        BValue[] returns = Functions.invoke(bFile, "testLongValueAssignmentByReturnValue");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BLong.class);
        BLong longValue = (BLong) returns[0];
        Assert.assertEquals(longValue.longValue(), 10L, "Invalid long value returned.");
    }

    @Test(description = "Test long value assignment")
    public void testLongParameter() {
        BValue[] args = {new BLong(20)};
        BValue[] returns = Functions.invoke(bFile, "testLongParameter", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BLong.class);
        BLong longValue = (BLong) returns[0];
        Assert.assertEquals(longValue.longValue(), 20L, "Invalid long value returned.");
    }

    @Test(description = "Test long value Addition")
    public void testLongValueAddition() {
        BValue[] returns = Functions.invoke(bFile, "testLongAddition");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BLong.class);
        BLong longValue = (BLong) returns[0];
        Assert.assertEquals(longValue.longValue(), 19L, "Invalid long value returned.");
    }

    @Test(description = "Test long value Subtraction")
    public void testLongValueSubtraction() {
        BValue[] returns = Functions.invoke(bFile, "testLongSubtraction");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BLong.class);
        BLong longValue = (BLong) returns[0];
        Assert.assertEquals(longValue.longValue(), 10L, "Invalid long value returned.");
    }

    @Test(description = "Test long value Multiplication")
    public void testLongValueMultiplication() {
        BValue[] returns = Functions.invoke(bFile, "testLongMultiplication");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BLong.class);
        BLong longValue = (BLong) returns[0];
        Assert.assertEquals(longValue.longValue(), 10L, "Invalid long value returned.");
    }

    @Test(description = "Test long value Division")
    public void testLongValueDivision() {
        BValue[] returns = Functions.invoke(bFile, "testLongDivision");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BLong.class);
        BLong longValue = (BLong) returns[0];
        Assert.assertEquals(longValue.longValue(), 5L, "Invalid long value returned.");
    }
}
