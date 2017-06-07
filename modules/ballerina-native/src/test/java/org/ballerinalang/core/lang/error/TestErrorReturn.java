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
package org.ballerinalang.core.lang.error;

import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for validating returning an error.
 */
public class TestErrorReturn {

    ProgramFile programValidIgnore;

    @BeforeClass
    public void setup() {
        programValidIgnore = BTestUtils.getProgramFile("lang/errors/valid-ignore.bal");
    }

    @Test(description = "Testing test1 method")
    public void testValidateIgnoreReturn1() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programValidIgnore, "test1", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "a");
        Assert.assertNotNull(returns[1]);
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 2.0);
    }

    @Test(description = "Testing test2 method")
    public void testValidateIgnoreReturn2() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programValidIgnore, "test2", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "a");
        Assert.assertNotNull(returns[1]);
        Assert.assertEquals(returns[1].stringValue(), "b");
    }

    @Test(description = "Testing test3 method")
    public void testValidateIgnoreReturn3() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programValidIgnore, "test3", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 1.0);
    }


    @Test(description = "validate ignored error struct type.")
    public void testValidateErrorReturn() {
        ProgramFile programFile = BTestUtils.getProgramFile("lang/errors/error_return.bal");
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testReturnError", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "FOO:10.5");
        Assert.assertNotNull(returns[1]);
        Assert.assertEquals(returns[1].stringValue(), "QUX:-1.0");
        Assert.assertNotNull(returns[2]);
        Assert.assertEquals(returns[2].stringValue(), "BAZ:0.0");
        Assert.assertNotNull(returns[3]);
        Assert.assertEquals(returns[3].stringValue(), "BAR:11.5");
    }

    @Test(description = "test throwing a returned error type..")
    public void testValidateErrorReturnAndThrow() {
        ProgramFile programFile = BTestUtils.getProgramFile("lang/errors/error_return.bal");
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testReturnAndThrowError", args);
        Assert.assertNotNull(returns);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "invalid name");
    }

    @Test(description = "negative test case for validating when all variables are ignored. ", expectedExceptions =
            BallerinaException.class, expectedExceptionsMessageRegExp = ".*assignment statement should have at least " +
            "one variable assignment")
    public void testValidateIgnoreAll() {
        ProgramFile programFile = BTestUtils.getProgramFile("lang/errors/invalid-all-ignore.bal");
        BValue[] args = {};
        BLangFunctions.invokeNew(programFile, "testReturnAndThrowError", args);
    }

    @Test(description = "negative test case for validating when all variables are ignored. ", expectedExceptions =
            BallerinaException.class, expectedExceptionsMessageRegExp = ".*assignment statement should have at least " +
            "one variable assignment")
    public void testValidateIgnore() {
        ProgramFile programFile = BTestUtils.getProgramFile("lang/errors/invalid-ignore.bal");
        BValue[] args = {};
        BLangFunctions.invokeNew(programFile, "testReturnAndThrowError", args);
    }

}
