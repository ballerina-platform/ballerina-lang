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

    @Test(description = "Test 'absFloat' function in ballerina.lang.math package")
    public void testAbsFloat() {
        BValue[] args = {new BFloat(-152.2544)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "absFloatTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 152.2544, DELTA);
    }

    @Test(description = "Test 'absInt' function in ballerina.lang.math package")
    public void testAbsInt() {
        BValue[] args = {new BInteger(-152)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "absIntTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 152);
    }

    @Test(description = "Test 'acos' function in ballerina.lang.math package")
    public void testAcos() {
        BValue[] args = {new BFloat(0.027415567780803774)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "acosTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 1.5433773235341761);
    }

    @Test(description = "Test 'addExact' function in ballerina.lang.math package")
    public void testAddExact() {
        BValue[] args = {new BInteger(5), new BInteger(5)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "addExactTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
    }

    @Test(description = "Test 'asin' function in ballerina.lang.math package")
    public void testAsin() {
        BValue[] args = {new BFloat(0.027415567780803774)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "asinTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 0.02741900326072046);
    }

    @Test(description = "Test 'atan' function in ballerina.lang.math package")
    public void testAtan() {
        BValue[] args = {new BFloat(0.027415567780803774)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "atanTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 0.0274087022410345);
    }

    @Test(description = "Test 'atan2' function in ballerina.lang.math package")
    public void testAtan2() {
        BValue[] args = {new BFloat(45.0), new BFloat(30.0)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "atan2Test", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 0.982793723247329, DELTA);
    }

    @Test(description = "Test 'cbrt' function in ballerina.lang.math package")
    public void testCbrt() {
        BValue[] args = {new BFloat(-27)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "cbrtTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), -3.0, DELTA);
    }

    @Test(description = "Test 'ceil' function in ballerina.lang.math package")
    public void testCeil() {
        BValue[] args = {new BFloat(-100.675)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "ceilTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), -100.0, DELTA);
    }

    @Test(description = "Test 'copySign' function in ballerina.lang.math package")
    public void testCopySign() {
        BValue[] args = {new BFloat(-0.4873), new BFloat(125.9)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "copySignTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 0.4873, DELTA);
    }

    @Test(description = "Test 'cos' function in ballerina.lang.math package")
    public void testCos() {
        BValue[] args = {new BFloat(3.141592653589793)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "cosTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), -1.0);
    }

    @Test(description = "Test 'cosh' function in ballerina.lang.math package")
    public void testCosh() {
        BValue[] args = {new BFloat(3.141592653589793)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "coshTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 11.591953275521519, DELTA);
    }

    @Test(description = "Test 'decrementExact' function in ballerina.lang.math package")
    public void testDecrementExact() {
        BValue[] args = {new BInteger(-152)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "decrementExactTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 152);
    }

}
