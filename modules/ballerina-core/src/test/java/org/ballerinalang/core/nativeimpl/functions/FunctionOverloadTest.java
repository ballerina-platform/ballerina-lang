/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.core.nativeimpl.functions;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BLong;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test overloading nature of functions.
 */
public class FunctionOverloadTest {

    private BLangProgram bLangProgram;
    private String overloadingFunctionName = "testOverloading";

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("lang/functions/function-overloading.bal");
    }

    @Test(description = "Test function overloading which has different argument counts")
    public void testFunctionOverloadingDifferentArgCount() {

        // function testOverloading()
        BValue[] returnsStep1 = BLangFunctions.invoke(bLangProgram, overloadingFunctionName);

        Assert.assertEquals(returnsStep1.length, 0);

        // function testOverloading(string a) (string)
        String a = "test";
        BValue[] argsStep2 = {new BString(a)};
        BValue[] returnsStep2 = BLangFunctions.invoke(bLangProgram, overloadingFunctionName, argsStep2);

        Assert.assertEquals(returnsStep2.length, 1);
        Assert.assertSame(returnsStep2[0].getClass(), BString.class);
        String actualResultStep2 = returnsStep2[0].stringValue();

        Assert.assertEquals(actualResultStep2, a);

        // function testOverLoading(string a, int b) (string)
        String d = "test";
        int e = 5;

        String expectedResultStep3 = d + e;

        BValue[] argsStep3 = {new BString(d), new BInteger(e)};
        BValue[] returnsStep3 = BLangFunctions.invoke(bLangProgram, overloadingFunctionName, argsStep3);

        Assert.assertEquals(returnsStep3.length, 1);
        Assert.assertSame(returnsStep3[0].getClass(), BString.class);
        String actualResultStep5 = returnsStep3[0].stringValue();

        Assert.assertEquals(actualResultStep5, expectedResultStep3);

    }

    @Test(description = "Test functino overloading which has same argument count")
    public void testFunctionOverloadingSameArgCountTest() {

        // Single arg
        // function testOverloading(string a) (string)
        String a = "test";
        BValue[] argsStep1 = {new BString(a)};
        BValue[] returnsStep1 = BLangFunctions.invoke(bLangProgram, overloadingFunctionName, argsStep1);

        Assert.assertEquals(returnsStep1.length, 1);
        Assert.assertSame(returnsStep1[0].getClass(), BString.class);
        String actualResultStep1 = returnsStep1[0].stringValue();

        Assert.assertEquals(actualResultStep1, a);

        // function testOverloading(int a) (int)
        int b = 5;
        BValue[] argsStep2 = {new BInteger(b)};
        BValue[] returnsStep2 = BLangFunctions.invoke(bLangProgram, overloadingFunctionName, argsStep2);

        Assert.assertEquals(returnsStep2.length, 1);
        Assert.assertSame(returnsStep2[0].getClass(), BInteger.class);
        int actualResultStep2 = ((BInteger) returnsStep2[0]).intValue();

        Assert.assertEquals(actualResultStep2, b);

        // function testOverloading(long a)
        long c = 5L;
        BValue[] argsStep3 = {new BLong(c)};
        BValue[] returnsStep3 = BLangFunctions.invoke(bLangProgram, overloadingFunctionName, argsStep3);

        Assert.assertEquals(returnsStep3.length, 0);

        // Two args

        // function testOverLoading(string a, int b) (string)
        String d = "test";
        int e = 5;

        String expectedResultStep4 = d + e;

        BValue[] argsStep4 = {new BString(d), new BInteger(e)};
        BValue[] returnsStep4 = BLangFunctions.invoke(bLangProgram, overloadingFunctionName, argsStep4);

        Assert.assertEquals(returnsStep4.length, 1);
        Assert.assertSame(returnsStep4[0].getClass(), BString.class);
        String actualResultStep4 = returnsStep4[0].stringValue();

        Assert.assertEquals(actualResultStep4, expectedResultStep4);

        // function testOverLoading(int a, int b) (int)
        int f = 5;
        int g = 5;

        int expectedResultStep5 = f + g;

        BValue[] argsStep5 = {new BInteger(f), new BInteger(g)};
        BValue[] returnsStep5 = BLangFunctions.invoke(bLangProgram, overloadingFunctionName, argsStep5);

        Assert.assertEquals(returnsStep5.length, 1);
        Assert.assertSame(returnsStep5[0].getClass(), BInteger.class);
        int actualResultStep5 = ((BInteger) returnsStep5[0]).intValue();

        Assert.assertEquals(actualResultStep5, expectedResultStep5);
    }

    @Test(description = "Test if incorrect function overloading produces errors")
    public void testInvalidFunctionOverloading() {
        Exception ex = null;

        try {
            BTestUtils.parseBalFile("lang/functions/invalid-function-overloading.bal");
        } catch (Exception e) {
            ex = e;
        } finally {
            Assert.assertTrue(ex != null);
            Assert.assertTrue(ex instanceof BallerinaException, "Expected a " + BallerinaException.class.getName() +
                    ", but found: " + ex.getClass() + ".");
            Assert.assertEquals(ex.getMessage(),
                    "invalid-function-overloading.bal:5: redeclared symbol 'testOverloading'");
        }
    }

}
