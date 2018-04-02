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
package org.ballerinalang.test.nativeimpl.functions;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for user package.
 */
public class UserTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/nativeimpl/functions/user-test.bal");
    }

    @Test
    public void testGetHome() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetHome");
        Assert.assertTrue(returns[0] instanceof BString);
        String expectedValue = System.getProperty("user.home");
        Assert.assertEquals(returns[0].stringValue(), expectedValue);
    }

    @Test
    public void testGetName() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetName");
        Assert.assertTrue(returns[0] instanceof BString);
        String expectedValue = System.getProperty("user.name");
        Assert.assertEquals(returns[0].stringValue(), expectedValue);
    }

    @Test
    public void testGetLanguage() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetLanguage");
        Assert.assertTrue(returns[0] instanceof BString);
        String expectedValue = System.getProperty("user.language");
        Assert.assertEquals(returns[0].stringValue(), expectedValue);
    }

    @Test
    public void testGetCountry() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetCountry");
        Assert.assertTrue(returns[0] instanceof BString);
        String expectedValue = System.getProperty("user.country");
        Assert.assertEquals(returns[0].stringValue(), expectedValue);
    }

    @Test
    public void testGetLocale() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetLocale");
        Assert.assertTrue(returns.length == 2);
        Assert.assertTrue(returns[0] instanceof BString);
        String expectedLanguage = System.getProperty("user.language");
        Assert.assertEquals(returns[0].stringValue(), expectedLanguage);

        Assert.assertTrue(returns[1] instanceof BString);
        String expectedCountryCode = System.getProperty("user.country");
        Assert.assertEquals(returns[1].stringValue(), expectedCountryCode);
    }
}
