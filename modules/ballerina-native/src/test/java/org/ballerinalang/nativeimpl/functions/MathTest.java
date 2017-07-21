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
package org.ballerinalang.nativeimpl.functions;

import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test Native functions in ballerina.lang.math.
 *
 * @since 0.90
 */
public class MathTest {

    private ProgramFile programFile;
    private static final double DELTA = 0.01;

    @BeforeClass
    public void setup() {
        programFile = BTestUtils.getProgramFile("lang/math/math.bal");
    }

    @Test(description = "Test 'exp' function in ballerina.lang.math package")
    public void testMathExp() {
        BValue[] args = {new BFloat(5.0)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "expTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 148.4131591025766, DELTA);
    }

    @Test(description = "Test 'pow' function in ballerina.lang.math package")
    public void testMathPow() {
        BValue[] args = {new BFloat(5.0), new BFloat(5.0)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "powTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 3125.0, DELTA);
    }

    @Test(description = "Test 'random' function in ballerina.lang.math package")
    public void testMathRandom() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "randomTest");

        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(((BFloat) returns[0]).floatValue() > 0 && ((BFloat) returns[0]).floatValue() < 1);
    }

    @Test(description = "Test 'sqrt' function in ballerina.lang.math package")
    public void testMathSqrt() {
        BValue[] args = {new BFloat(25.0)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "sqrtTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 5.0, DELTA);
    }

    @Test(description = "Test 'randomRange' function in ballerina.lang.math package")
    public void testRandomRange() {
        BValue[] args = {new BInteger(5), new BInteger(10)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "randomInRangeTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(((BInteger) returns[0]).floatValue() >= 5 && ((BInteger) returns[0]).floatValue() < 10);
    }
}
