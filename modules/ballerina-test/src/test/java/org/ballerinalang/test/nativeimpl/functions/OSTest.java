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
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Test class for os package.
 */
public class OSTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/nativeimpl/functions/osTest.bal");
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
        Assert.assertEquals(returns[0].stringValue(), "");
    }

    @Test
    public void testValidMultivaluedEnv() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testValidMultivaluedEnv");
        Assert.assertFalse(returns == null || returns.length == 0 || returns[0] == null,
                           "Invalid Return Values.");
        Assert.assertTrue(returns[0] instanceof BStringArray);

        String pathValue = System.getenv("PATH");
        String pathSeparator = System.getProperty("path.separator");
        String[] paths = pathValue.split(pathSeparator);
        paths = Arrays.stream(paths).map(value -> "\"" + value + "\"").toArray(String[]::new);

        Assert.assertEquals(returns[0].stringValue(), Arrays.toString(paths));
    }

    @Test
    public void testEmptyMultivaluedEnv() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testEmptyMultivaluedEnv");
        Assert.assertFalse(returns == null || returns.length == 0 || returns[0] == null,
                           "Invalid Return Values.");
        Assert.assertTrue(returns[0] instanceof BStringArray);
        Assert.assertTrue(((BStringArray) returns[0]).size() == 0);
    }

    @Test
    public void getName() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetName");
        Assert.assertTrue(returns[0] instanceof BString);
        String expectedValue = System.getProperty("os.name");
        Assert.assertEquals(returns[0].stringValue(), expectedValue);
    }

    @Test
    public void getVersion() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetVersion");
        Assert.assertTrue(returns[0] instanceof BString);
        String expectedValue = System.getProperty("os.version");
        Assert.assertEquals(returns[0].stringValue(), expectedValue);
    }

    @Test
    public void getArchitecture() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetArchitecture");
        Assert.assertTrue(returns[0] instanceof BString);
        String expectedValue = System.getProperty("os.arch");
        Assert.assertEquals(returns[0].stringValue(), expectedValue);
    }
}
