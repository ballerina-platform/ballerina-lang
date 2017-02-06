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
import org.wso2.ballerina.core.model.values.BDouble;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.utils.Functions;
import org.wso2.ballerina.core.utils.ParserUtils;

/**
 * This test class will test the behaviour of double values with expressions.
 * Addition
 * Multiplication
 * Division
 * Subtraction
 *
 * Defining a double value
 * double b;
 * b = 10.1d;
 */
public class BDoubleValueTest {
    private BallerinaFile bFile;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        bFile = ParserUtils.parseBalFile("lang/values/double-value.bal");
    }

    @Test(description = "Test double value assignment")
    public void testDoubleValue() {
        BValue[] returns = Functions.invoke(bFile, "testDoubleValue");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BDouble.class);
        BDouble doubleValue = (BDouble) returns[0];
        Assert.assertEquals(doubleValue.doubleValue(), 10.1, "Invalid double value returned.");
    }

    @Test(description = "Test negative double value assignment")
    public void testNegativeDoubleValue() {
        BValue[] returns = Functions.invoke(bFile, "testNegativeDoubleValue");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BDouble.class);
        BDouble doubleValue = (BDouble) returns[0];
        Assert.assertEquals(doubleValue.doubleValue(), (-10.1), "Invalid double value returned.");
    }

    @Test(description = "Test double value assignment from a value returned by function")
    public void testDoubleValueAssignmentByReturnValue() {
        BValue[] returns = Functions.invoke(bFile, "testDoubleValueAssignmentByReturnValue");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BDouble.class);
        BDouble doubleValue = (BDouble) returns[0];
        Assert.assertEquals(doubleValue.doubleValue(), 10.1, "Invalid double value returned.");
    }

    @Test(description = "Test double value assignment")
    public void testDoubleParameter() {
        BValue[] args = {new BDouble(3.3d)};
        BValue[] returns = Functions.invoke(bFile, "testDoubleParameter", args);
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BDouble.class);
        BDouble doubleValue = (BDouble) returns[0];
        Assert.assertEquals(doubleValue.doubleValue(), 3.3, "Invalid double value returned.");
    }

    @Test(description = "Test double value Addition")
    public void testDoubleValueAddition() {
        BValue[] returns = Functions.invoke(bFile, "testDoubleAddition");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BDouble.class);
        BDouble doubleValue = (BDouble) returns[0];
        Assert.assertEquals(doubleValue.doubleValue(), 20.0, "Invalid double value returned.");
    }

    @Test(description = "Test double value Subtraction")
    public void testDoubleValueSubtraction() {
        BValue[] returns = Functions.invoke(bFile, "testDoubleSubtraction");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BDouble.class);
        BDouble doubleValue = (BDouble) returns[0];
        Assert.assertEquals(doubleValue.doubleValue(), 10.0, "Invalid double value returned.");
    }

    @Test(description = "Test double value Multiplication")
    public void testDoubleValueMultiplication() {
        BValue[] returns = Functions.invoke(bFile, "testDoubleMultiplication");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BDouble.class);
        BDouble doubleValue = (BDouble) returns[0];
        Assert.assertEquals(doubleValue.doubleValue(), 13.75, "Invalid double value returned.");
    }

    @Test(description = "Test double value Division")
    public void testDoubleValueDivision() {
        BValue[] returns = Functions.invoke(bFile, "testDoubleDivision");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BDouble.class);
        BDouble doubleValue = (BDouble) returns[0];
        Assert.assertEquals(doubleValue.doubleValue(), 5.0, "Invalid double value returned.");
    }
}
