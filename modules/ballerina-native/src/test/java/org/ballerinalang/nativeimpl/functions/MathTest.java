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
    private static final double DELTA = 0.0000000001;

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
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 1.5433773235341761, DELTA);
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
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 0.02741900326072046, DELTA);
    }

    @Test(description = "Test 'atan' function in ballerina.lang.math package")
    public void testAtan() {
        BValue[] args = {new BFloat(0.027415567780803774)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "atanTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 0.0274087022410345, DELTA);
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
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), -1.0, DELTA);
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
        Assert.assertEquals(((BInteger) returns[0]).intValue(), -153);
    }

    @Test(description = "Test 'expm1' function in ballerina.lang.math package")
    public void testExpm1() {
        BValue[] args = {new BFloat(0.5)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "expm1Test", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 0.6487212707001282, DELTA);
    }

    @Test(description = "Test 'floor' function in ballerina.lang.math package")
    public void testFloor() {
        BValue[] args = {new BFloat(-100.675)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "floorTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), -101.0, DELTA);
    }

    @Test(description = "Test 'floorDiv' function in ballerina.lang.math package")
    public void testFloorDiv() {
        BValue[] args = {new BInteger(-4), new BInteger(3)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "floorDivTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), -2);
    }

    @Test(description = "Test 'floorMod' function in ballerina.lang.math package")
    public void testFloorMod() {
        BValue[] args = {new BInteger(-4), new BInteger(3)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "floorModTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
    }

    @Test(description = "Test 'getExponent' function in ballerina.lang.math package")
    public void testGetExponent() {
        BValue[] args = {new BFloat(60984.1)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "getExponentTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 15);
    }

    @Test(description = "Test 'hypot' function in ballerina.lang.math package")
    public void testHypot() {
        BValue[] args = {new BFloat(60984.1), new BFloat(-497.99)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "hypotTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 60986.133234122164, DELTA);
    }

    @Test(description = "Test 'IEEEremainder' function in ballerina.lang.math package")
    public void testIEEEremainder() {
        BValue[] args = {new BFloat(60984.1), new BFloat(-497.99)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "IEEEremainderTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 229.31999999999744, DELTA);
    }

    @Test(description = "Test 'incrementExact' function in ballerina.lang.math package")
    public void testIncrementExact() {
        BValue[] args = {new BInteger(5)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "incrementExactTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 6);
    }

    @Test(description = "Test 'log' function in ballerina.lang.math package")
    public void testLog() {
        BValue[] args = {new BFloat(60984.1)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "logTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 11.018368453441132, DELTA);
    }

    @Test(description = "Test 'log10' function in ballerina.lang.math package")
    public void testLog10() {
        BValue[] args = {new BFloat(60984.1)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "log10Test", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 4.78521661890635, DELTA);
    }

    @Test(description = "Test 'log1p' function in ballerina.lang.math package")
    public void testLog1p() {
        BValue[] args = {new BFloat(1000)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "log1pTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 6.90875477931522, DELTA);
    }

    @Test(description = "Test 'maxInt' function in ballerina.lang.math package")
    public void testMaxInt() {
        BValue[] args = {new BInteger(5), new BInteger(10)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "maxIntTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(((BInteger) returns[0]).intValue() == 10);
    }

    @Test(description = "Test 'maxFloat' function in ballerina.lang.math package")
    public void testMaxFloat() {
        BValue[] args = {new BFloat(60984.1), new BFloat(-497.99)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "maxFloatTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(((BFloat) returns[0]).floatValue() == 60984.1);
    }

    @Test(description = "Test 'minInt' function in ballerina.lang.math package")
    public void testMinInt() {
        BValue[] args = {new BInteger(5), new BInteger(10)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "minIntTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(((BInteger) returns[0]).intValue() == 5);
    }

    @Test(description = "Test 'minFloat' function in ballerina.lang.math package")
    public void testMinFloat() {
        BValue[] args = {new BFloat(60984.1), new BFloat(-497.99)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "minFloatTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(((BFloat) returns[0]).floatValue() == -497.99);
    }

    @Test(description = "Test 'multiplyExact' function in ballerina.lang.math package")
    public void testMultiplyExact() {
        BValue[] args = {new BInteger(-4), new BInteger(3)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "multiplyExactTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), -12);
    }

    @Test(description = "Test 'negateExact' function in ballerina.lang.math package")
    public void testNegateExact() {
        BValue[] args = {new BInteger(-4)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "negateExactTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 4);
    }

    @Test(description = "Test 'nextAfter' function in ballerina.lang.math package")
    public void testNextAfter() {
        BValue[] args = {new BFloat(98759.765), new BFloat(154.28764)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "nextAfterTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 98759.76499999998, DELTA);
    }

    @Test(description = "Test 'nextDown' function in ballerina.lang.math package")
    public void testNextDown() {
        BValue[] args = {new BFloat(60984.1)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "nextDownTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 60984.09999999999, DELTA);
    }

    @Test(description = "Test 'nextUp' function in ballerina.lang.math package")
    public void testNextUp() {
        BValue[] args = {new BFloat(-100.675)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "nextUpTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), -100.67499999999998, DELTA);
    }

    @Test(description = "Test 'rint' function in ballerina.lang.math package")
    public void testRint() {
        BValue[] args = {new BFloat(2.50)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "rintTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 2.0, DELTA);
    }

    @Test(description = "Test 'round' function in ballerina.lang.math package")
    public void testRound() {
        BValue[] args = {new BFloat(2.50)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "roundTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 3);
    }

    @Test(description = "Test 'scalb' function in ballerina.lang.math package")
    public void testScalb() {
        BValue[] args = {new BFloat(50.14), new BInteger(4)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "scalbTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 802.24, DELTA);
    }

    @Test(description = "Test 'signum' function in ballerina.lang.math package")
    public void testSignum() {
        BValue[] args = {new BFloat(50.14)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "signumTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 1.0, DELTA);
    }

    @Test(description = "Test 'sin' function in ballerina.lang.math package")
    public void testSin() {
        BValue[] args = {new BFloat(0.7853981633974483)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "sinTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 0.7071067811865475, DELTA);
    }

    @Test(description = "Test 'sinh' function in ballerina.lang.math package")
    public void testSinh() {
        BValue[] args = {new BFloat(-3.141592653589793)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "sinhTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), -11.548739357257748, DELTA);
    }

    @Test(description = "Test 'subtractExact' function in ballerina.lang.math package")
    public void testSubtractExact() {
        BValue[] args = {new BInteger(5), new BInteger(10)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "subtractExactTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), -5);
    }

    @Test(description = "Test 'tan' function in ballerina.lang.math package")
    public void testTan() {
        BValue[] args = {new BFloat(0.7853981633974483)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "tanTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 0.9999999999999999, DELTA);
    }

    @Test(description = "Test 'tanh' function in ballerina.lang.math package")
    public void testTanh() {
        BValue[] args = {new BFloat(-3.141592653589793)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "tanhTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), -0.99627207622075, DELTA);
    }

    @Test(description = "Test 'toDegrees' function in ballerina.lang.math package")
    public void testToDegrees() {
        BValue[] args = {new BFloat(2578.3100780887044)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "toDegreesTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 147726.28575052848, DELTA);
    }

    @Test(description = "Test 'toRadians' function in ballerina.lang.math package")
    public void testToRadians() {
        BValue[] args = {new BFloat(45.0)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "toRadiansTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 0.7853981633974483, DELTA);
    }

    @Test(description = "Test 'ulp' function in ballerina.lang.math package")
    public void testUlp() {
        BValue[] args = {new BFloat(956.294)};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "ulpTest", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 1.1368683772161603E-13, DELTA);
    }

}
