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

import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for user package.
 */
public class UserTest {

    private ProgramFile programFile;

    @BeforeClass
    public void setup() {
        programFile = BTestUtils.getProgramFile("samples/userTest.bal");
    }

    @Test
    public void testGetHome() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetHome", args);
        Assert.assertTrue(returns[0] instanceof BString);
        String expectedValue = System.getProperty("user.home");
        Assert.assertEquals(returns[0].stringValue(), expectedValue);
    }

    @Test
    public void testGetName() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetName", args);
        Assert.assertTrue(returns[0] instanceof BString);
        String expectedValue = System.getProperty("user.name");
        Assert.assertEquals(returns[0].stringValue(), expectedValue);
    }

    @Test
    public void testGetLanguage() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetLanguage", args);
        Assert.assertTrue(returns[0] instanceof BString);
        String expectedValue = System.getProperty("user.language");
        Assert.assertEquals(returns[0].stringValue(), expectedValue);
    }

    @Test
    public void testGetCountry() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetCountry", args);
        Assert.assertTrue(returns[0] instanceof BString);
        String expectedValue = System.getProperty("user.country");
        Assert.assertEquals(returns[0].stringValue(), expectedValue);
    }

    @Test
    public void testGetLocale() {
        BValue[] args = {};
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testGetLocale", args);
        Assert.assertTrue(returns.length == 2);
        Assert.assertTrue(returns[0] instanceof BString);
        String expectedLanguage = System.getProperty("user.language");
        Assert.assertEquals(returns[0].stringValue(), expectedLanguage);

        Assert.assertTrue(returns[1] instanceof BString);
        String expectedCountryCode = System.getProperty("user.country");
        Assert.assertEquals(returns[1].stringValue(), expectedCountryCode);
    }
}
