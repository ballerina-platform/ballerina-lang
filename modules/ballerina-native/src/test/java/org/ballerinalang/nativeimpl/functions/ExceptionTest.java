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
package org.ballerinalang.nativeimpl.functions;

import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for ballerina.model.exception
 */
public class ExceptionTest {

    BLangProgram bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("samples/exceptionTest.bal");
    }

    @Test(description = "test functionality.")
    public void testFunctionality() {
        BValue[] args = {new BString("John"), new BInteger(45),
                new BInteger(1000), new BInteger(10)};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "calculateLoanPayment", args);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "Your monthly payment is 100.0");
    }

    @Test(description = "test setMessage, getMessage, setCategory, getCategory native functions.")
    public void testExceptionGetAndSetFunctions() {
        BValue[] args = {new BString("Steve"), new BInteger(60),
                new BInteger(1000), new BInteger(10)};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "calculateLoanPayment", args);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(),
                "Error ..! age should be under 50");
    }

    @Test(description = "test set, setCause, getStackTrace native functions.")
    public void testExceptionCauseAndStackTrace() {
        BValue[] args = {new BString("Bob"), new BInteger(11),
                new BInteger(1000), new BInteger(10)};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "calculateLoanPayment", args);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0].stringValue().contains(
                "exception age-error: age Error"));
        Assert.assertTrue(returns[0].stringValue().contains(
                "caused by: exception low-age: age should be over 18"));
    }

    @Test(description = "testing divide by zero")
    public void testFailures() {
        BValue[] args = {new BString("Bob"), new BInteger(25),
                new BInteger(1000), new BInteger(0)};
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "calculateLoanPayment", args);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0].stringValue().contains("/ by zero"));

    }


}
