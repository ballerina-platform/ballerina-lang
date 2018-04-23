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
 * Test class for system package.
 */
public class SystemTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/nativeimpl/functions/system-test.bal");
    }

    @Test
    public void testValidEnv() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testValidEnv");
        Assert.assertTrue(returns[0] instanceof BString);
        String expectedValue = System.getenv("JAVA_HOME");
        Assert.assertEquals(returns[0].stringValue(), expectedValue);
    }

    @Test
    public void testEmptyEnv() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testEmptyEnv");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertNull(returns[0].stringValue());
    }

    @Test
    public void testGetCurrentDirectory() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetCurrentDirectory");
        Assert.assertTrue(returns[0] instanceof BString);
        String expectedValue = System.getProperty("user.dir");
        Assert.assertEquals(returns[0].stringValue(), expectedValue);
    }

    @Test
    public void testGetUserHome() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetUserHome");
        Assert.assertTrue(returns[0] instanceof BString);
        String expectedValue = System.getProperty("user.home");
        Assert.assertEquals(returns[0].stringValue(), expectedValue);
    }

    @Test
    public void testGetUserame() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetUsername");
        Assert.assertTrue(returns[0] instanceof BString);
        String expectedValue = System.getProperty("user.name");
        Assert.assertEquals(returns[0].stringValue(), expectedValue);
    }

    @Test
    public void testRandomString() {
        BValue[] returnValues = BRunUtil.invoke(compileResult, "testRandomString");
        Assert.assertFalse(returnValues == null || returnValues.length == 0 || returnValues[0] == null,
                "Invalid return value");
    }
}
