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
package org.wso2.ballerina.nativeimpl.functions;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.nativeimpl.util.Functions;
import org.wso2.ballerina.nativeimpl.util.ParserUtils;

/**
 * Test cases for ballerina.lang.exception
 */
public class ExceptionTest {

    BallerinaFile bFile;

    @BeforeClass
    public void setup() {
        bFile = ParserUtils.parseBalFile("samples/exceptionTest.bal");
    }

    @Test(description = "test functionality.")
    public void testFunctionality() {
        BValue[] args = {new BString("John"), new BInteger(45),
                new BInteger(1000), new BInteger(10)};
        BValue[] returns = Functions.invoke(bFile, "calculateLoanPayment", args);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "Your monthly payment is 100.0");
    }

    @Test(description = "test setMessage, getMessage, setCategory, getCategory native functions.")
    public void testExceptionGetAndSetFunctions() {
        BValue[] args = {new BString("Steve"), new BInteger(60),
                new BInteger(1000), new BInteger(10)};
        BValue[] returns = Functions.invoke(bFile, "calculateLoanPayment", args);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(),
                "Error ..! You should be under 50 years old to apply loan.");
    }

    @Test(description = "test set, setCause, getStackTrace native functions.")
    public void testExceptionCauseAndStackTrace() {
        BValue[] args = {new BString("Bob"), new BInteger(11),
                new BInteger(1000), new BInteger(10)};
        BValue[] returns = Functions.invoke(bFile, "calculateLoanPayment", args);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0].stringValue().contains(
                "exception age-error: You are too young to apply a loan."));
        Assert.assertTrue(returns[0].stringValue().contains(
                "caused by: exception low-age: You should be over 18 years old to apply loan."));
    }

    @Test(description = "testing divide by zero")
    public void testFailures() {
        BValue[] args = {new BString("Bob"), new BInteger(25),
                new BInteger(1000), new BInteger(0)};
        BValue[] returns = Functions.invoke(bFile, "calculateLoanPayment", args);
        Assert.assertNotNull(returns[0]);
        Assert.assertTrue(returns[0].stringValue().contains("/ by zero"));

    }


}
