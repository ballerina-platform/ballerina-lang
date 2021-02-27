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

package org.ballerinalang.stdlib.system;

import org.apache.commons.lang3.SystemUtils;
import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
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
        compileResult = BCompileUtil.compileOffline("test-src/system-test.bal");
    }

    @Test
    public void testValidEnv() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testValidEnv");
        Assert.assertTrue(returns[0] instanceof BString);
        String expectedValue = System.getenv("JAVA_HOME");
        Assert.assertEquals(returns[0].stringValue(), expectedValue == null ? "" : expectedValue);
    }

    @Test
    public void testEmptyEnv() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testEmptyEnv");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "");
    }

    @Test
    public void testGetUserHome() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetUserHome");
        Assert.assertTrue(returns[0] instanceof BString);
        String expectedValue = System.getProperty("user.home");
        Assert.assertEquals(returns[0].stringValue(), expectedValue);
    }

    @Test
    public void testGetUsername() {
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
    
    @Test
    public void testExecInUnixLike1() {
        if (SystemUtils.IS_OS_UNIX) {
            BValue[] returns = BRunUtil.invoke(compileResult, "testExecInUnixLike1");
            Assert.assertEquals(returns[0].stringValue().trim(), "BAL_EXEC_TEST_VAR=X");
            Assert.assertEquals(returns[1].stringValue(), "0");
            Assert.assertEquals(returns[2].stringValue(), "0");
        }
    }
    
    @Test
    public void testExecInUnixLike2() {
        if (SystemUtils.IS_OS_UNIX) {
            BValue[] returns = BRunUtil.invoke(compileResult, "testExecInUnixLike2");
            Assert.assertEquals(returns[0].stringValue().trim(), "/");
        }
    }
    
    @Test
    public void testExecInUnixLike3() {
        if (SystemUtils.IS_OS_UNIX) {
            BValue[] returns = BRunUtil.invoke(compileResult, "testExecInUnixLike3");
            Assert.assertEquals(returns[0].stringValue().trim(), "BAL_TEST");
        }
    }
    
    @Test
    public void testExecInUnixLike4() {
        if (SystemUtils.IS_OS_UNIX) {
            BValue[] returns = BRunUtil.invoke(compileResult, "testExecInUnixLike4");
            Assert.assertEquals(returns[0].stringValue().trim(), "1");
        }
    }

    @Test(description = "test process execute error")
    public void testExecWithError() {
        String expectedError = "Cannot run program \"eee\": error=2, No such file or directory";
        if (SystemUtils.IS_OS_UNIX) {
            BValue[] returns = BRunUtil.invoke(compileResult, "testExecWithError");
            Assert.assertNotNull(returns[0]);
            Assert.assertTrue(returns[0] instanceof BError);
            Assert.assertEquals(((BError) returns[0]).getMessage().trim(), expectedError);
        }
    }
}
